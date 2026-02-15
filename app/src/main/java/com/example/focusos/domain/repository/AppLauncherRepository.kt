package com.example.focusos.domain.repository

import com.example.focusos.domain.model.AppItem

interface AppLauncherRepository {
    suspend fun getInstalledApps(): List<AppItem>
}