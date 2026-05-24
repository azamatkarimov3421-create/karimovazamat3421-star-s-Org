package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_restrictions")
data class AppRestriction(
    @PrimaryKey val packageName: String,
    val appName: String,
    val isBlocked: Boolean = false,
    val limitMinutes: Int = 0, // 0 = no limit, otherwise minutes
    val remainingMinutesToday: Int = 0
)

@Entity(tableName = "app_usage_logs")
data class AppUsageLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val packageName: String,
    val appName: String,
    val action: String, // "LAUNCHED", "BLOCKED", "LIMIT_EXCEEDED"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "parent_settings")
data class ParentSetting(
    @PrimaryKey val key: String,
    val value: String
)
