package com.ilmariware.currencyconverterwidget.data.models

enum class UpdateFrequency(val displayName: String, val hours: Long) {
    TWELVE_HOURS("Every 12 hours", 12),
    DAILY("Daily", 24),
    WEEKLY("Weekly", 168);

    companion object {
        fun fromHours(hours: Long): UpdateFrequency {
            return values().find { it.hours == hours } ?: DAILY
        }
    }
}



