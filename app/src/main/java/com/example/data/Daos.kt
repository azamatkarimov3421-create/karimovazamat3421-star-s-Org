package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppRestrictionDao {
    @Query("SELECT * FROM app_restrictions ORDER BY appName ASC")
    fun getAllRestrictions(): Flow<List<AppRestriction>>

    @Query("SELECT * FROM app_restrictions WHERE packageName = :packageName")
    suspend fun getRestriction(packageName: String): AppRestriction?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveRestriction(restriction: AppRestriction)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveRestrictions(restrictions: List<AppRestriction>)

    @Query("DELETE FROM app_restrictions WHERE packageName = :packageName")
    suspend fun deleteRestriction(packageName: String)
}

@Dao
interface AppUsageLogDao {
    @Query("SELECT * FROM app_usage_logs ORDER BY timestamp DESC LIMIT 60")
    fun getAllLogs(): Flow<List<AppUsageLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: AppUsageLog)

    @Query("DELETE FROM app_usage_logs")
    suspend fun clearLogs()
}

@Dao
interface ParentSettingDao {
    @Query("SELECT * FROM parent_settings WHERE `key` = :key")
    suspend fun getSetting(key: String): ParentSetting?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSetting(setting: ParentSetting)
}
