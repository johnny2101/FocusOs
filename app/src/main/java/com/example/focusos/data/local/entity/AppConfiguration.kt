package com.example.focusos.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.focusos.domain.model.AppTier

@Entity(tableName = "app_configurations")
data class AppConfiguration (
    @PrimaryKey val packageName: String,
    val tier: Int = AppTier.STANDARD.id,
    val isHidden: Boolean = false
)