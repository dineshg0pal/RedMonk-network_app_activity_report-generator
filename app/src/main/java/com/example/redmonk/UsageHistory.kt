package com.example.redmonk

import android.content.Context

object UsageHistory {

    private const val PREF = "usage_history"

    fun save(context: Context, list: List<AppUsage>) {
        val pref = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        val editor = pref.edit()

        list.forEach {
            val total = it.sent + it.received
            val old = pref.getLong(it.appName, 0L)

            val newAvg =
                if (old == 0L) total
                else (old + total) / 2

            editor.putLong(it.appName, newAvg)
        }

        editor.apply()
    }

    fun getAverageUsage(context: Context, appName: String): Long {
        val pref = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        return pref.getLong(appName, 0L)
    }
}
