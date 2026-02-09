package com.example.redmonk

import android.graphics.drawable.Drawable

data class AppUsage(
    val appName: String,
    val icon: Drawable,
    val sent: Long,
    val received: Long,
    val protocol: String,
    val time: Long,

    // forensic / intelligence fields
    val riskLevel: String = "LOW",
    val riskReason: String = "Normal behaviour",

    // NEW
    val isRunningNow: Boolean = false,
    val lastUsedTime: Long = 0L
)
