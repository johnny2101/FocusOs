package com.example.focusos.domain.model

enum class AppTier(val id: Int) {
    UTILITY(0),
    STANDARD(1),
    DOPAMINE(2);

    companion object {
        fun fromId(id: Int): AppTier {
            return entries.find { it.id == id } ?: AppTier.STANDARD

        }
    }
}