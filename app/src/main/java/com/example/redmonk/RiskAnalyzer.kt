package com.example.redmonk

import android.content.Context

object RiskAnalyzer {

    data class Result(
        val level: String,
        val reason: String
    )

    fun analyze(context: Context, app: AppUsage): Result {

        val total = app.sent + app.received

        // ===== BASIC RULES =====
        if (total > 300 * 1024 * 1024) {
            return Result(
                "HIGH",
                "Extremely high data transfer"
            )
        }

        if (!app.isRunningNow && total > 30 * 1024 * 1024) {
            return Result(
                "MEDIUM",
                "Background network activity"
            )
        }

        if (app.lastUsedTime == 0L) {
            return Result(
                "MEDIUM",
                "Usage history unavailable"
            )
        }

        // ===== HISTORY BASED CHECK =====
        val average = UsageHistory.getAverageUsage(context, app.appName)

        if (average > 0 && total > average * 3) {
            return Result(
                "HIGH",
                "Unusual spike compared to normal behaviour"
            )
        }

        return Result("LOW", "Normal behaviour")
    }
}
