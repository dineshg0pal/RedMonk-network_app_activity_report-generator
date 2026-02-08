package com.example.redmonk

import android.app.usage.NetworkStats
import android.app.usage.NetworkStatsManager
import android.content.Context
import android.net.ConnectivityManager
import android.os.Handler
import android.os.Looper
import java.util.*

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
        val list = mutableListOf<AppUsage>()
        val pm = context.packageManager
        val nsm =
            context.getSystemService(Context.NETWORK_STATS_SERVICE) as NetworkStatsManager

        val end = System.currentTimeMillis()
        val start = end - (1000 * 60 * 60)

        fun read(type: Int) {
            try {
                val stats = nsm.querySummary(type, null, start, end)
                val bucket = NetworkStats.Bucket()

                while (stats.hasNextBucket()) {
                    stats.getNextBucket(bucket)

                    val uid = bucket.uid
                    if (uid <= 0) continue

                    val packages = pm.getPackagesForUid(uid) ?: continue
                    val appInfo = pm.getApplicationInfo(packages[0], 0)

                    list.add(
                        AppUsage(
                            appName = pm.getApplicationLabel(appInfo).toString(),
                            icon = pm.getApplicationIcon(appInfo),
                            sent = bucket.txBytes,
                            received = bucket.rxBytes,
                            protocol = "TCP/IP",
                            time = System.currentTimeMillis()
                        )
                    )
                }

                stats.close()

            } catch (_: Exception) {
            }
        }

        read(ConnectivityManager.TYPE_WIFI)
        read(ConnectivityManager.TYPE_MOBILE)

        return list
    }

}
