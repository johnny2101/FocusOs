package com.example.focusos.model

data class AppUsageDiff(
    val packageName: String,
    val appName: String,
    val currentWeekCount: Int,
    val previousWeekCount: Int,
    val diff: Int
)