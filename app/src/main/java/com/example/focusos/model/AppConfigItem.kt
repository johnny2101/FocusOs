package com.example.focusos.model

import com.example.focusos.domain.model.AppTier

data class AppConfigItem (
    val packageName: String,
    val label: String,
    val tier: AppTier
)