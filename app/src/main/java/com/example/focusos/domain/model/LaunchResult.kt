package com.example.focusos.domain.model

sealed class LaunchResult {
    data object Allowed: LaunchResult()
    data class Blocked(val packageName: String) : LaunchResult()
}
