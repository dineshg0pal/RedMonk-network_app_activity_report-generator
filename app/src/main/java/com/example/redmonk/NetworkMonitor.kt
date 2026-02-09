package com.example.redmonk

import android.app.ActivityManager
import android.app.usage.NetworkStats
import android.app.usage.NetworkStatsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.net.ConnectivityManager
import android.os.Handler
import android.os.Looper

class NetworkMonitor(
    private val context: Context,
    private val callback: (List<AppUsage>) -> Unit
) {

    private val handler = Handler(Looper.getMainLooper())
    private var running = false

    private val runnable = object : Runnable {
        override fun run() {
            if (!running) return
            callback(readUsage())
            handler.postDelayed(this, 4000)
        }
    }

    fun start() {
        running = true
        handler.post(runnable)
    }

    fun stop() {
        running = false
        handler.removeCallbacks(runnable)
    }

    private fun readUsage(): List<AppUsage> {
        val pm = context.packageManager
        val nsm =
            context.getSystemService(Context.NETWORK_STATS_SERVICE) as NetworkStatsManager
        val usm =
            context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

        val end = System.currentTimeMillis()

        // 🔥 BIG WINDOW = apps will appear
        val start = end - (24 * 60 * 60 * 1000)

        // ===== LAST USED MAP =====
        val usageStats = usm.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            start,
            end
        )

        val lastUsedMap = mutableMapOf<String, Long>()
        usageStats?.forEach {
            lastUsedMap[it.packageName] = it.lastTimeUsed
        }

        // ===== FOREGROUND APP =====
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningApp = am.runningAppProcesses?.firstOrNull()?.processName

        // ===== AGGREGATE TRAFFIC =====
        data class Temp(val sent: Long, val received: Long)
        val traffic = mutableMapOf<String, Temp>()

        fun read(type: Int) {
            try {
                val stats = nsm.querySummary(type, null, start, end)
                val bucket = NetworkStats.Bucket()

                while (stats.hasNextBucket()) {
                    stats.getNextBucket(bucket)

                    val uid = bucket.uid
                    if (uid <= 0) continue

                    val packages = pm.getPackagesForUid(uid) ?: continue
                    val pkg = packages[0]

                    val old = traffic[pkg]
                    if (old == null) {
                        traffic[pkg] = Temp(bucket.txBytes, bucket.rxBytes)
                    } else {
                        traffic[pkg] = Temp(
                            old.sent + bucket.txBytes,
                            old.received + bucket.rxBytes
                        )
                    }
                }

                stats.close()
            } catch (_: Exception) {
            }
        }

        read(ConnectivityManager.TYPE_WIFI)
        read(ConnectivityManager.TYPE_MOBILE)

        // ===== BUILD FINAL LIST =====
        val result = mutableListOf<AppUsage>()

        traffic.forEach { (packageName, t) ->
            try {
                val appInfo = pm.getApplicationInfo(packageName, 0)
                val appName = pm.getApplicationLabel(appInfo).toString()

                val isRunning = packageName == runningApp
                val lastUsed = lastUsedMap[packageName] ?: 0L

                result.add(
                    AppUsage(
                        appName = appName,
                        icon = pm.getApplicationIcon(appInfo),
                        sent = t.sent,
                        received = t.received,
                        protocol = "TCP/IP",
                        time = System.currentTimeMillis(),
                        isRunningNow = isRunning,
                        lastUsedTime = lastUsed
                    )
                )
            } catch (_: Exception) {
            }
        }

        // biggest consumer on top
        return result.sortedByDescending { it.sent + it.received }
    }
}
