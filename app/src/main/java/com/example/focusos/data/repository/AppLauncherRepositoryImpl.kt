package com.example.focusos.data.repository

import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.focusos.domain.model.AppItem
import com.example.focusos.domain.repository.AppLauncherRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AppLauncherRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : AppLauncherRepository {
    override suspend fun getInstalledApps(): List<AppItem> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val apps = pm.queryIntentActivities(intent, 0)

        apps.mapNotNull { resolveInfo ->
            val packageName = resolveInfo.activityInfo.packageName

            if (packageName == context.packageName) return@mapNotNull null

            val label = resolveInfo.loadLabel(pm).toString()
            AppItem(label, packageName)
        }.sortedBy { it.label.lowercase() }
    }

    override fun launchApp(packageName: String) {
        try {
            val intent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            } else {
                Toast.makeText(context, "App not found", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}