package com.example.focusos.util

import android.content.pm.PackageManager

fun getAppName(packageName: String, packageManager: PackageManager): String {
    return try {
        val info = packageManager.getApplicationInfo(packageName, 0)
        packageManager.getApplicationLabel(info).toString()
    } catch (e: PackageManager.NameNotFoundException) {
        packageName
    }
}