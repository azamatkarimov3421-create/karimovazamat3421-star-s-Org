package com.example.data

import android.app.usage.UsageStatsManager
import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.Calendar

class LauncherRepository(private val db: AppDatabase) {

    val allRestrictions: Flow<List<AppRestriction>> = db.restrictionDao().getAllRestrictions()
    val allLogs: Flow<List<AppUsageLog>> = db.usageLogDao().getAllLogs()

    suspend fun saveRestriction(restriction: AppRestriction) = withContext(Dispatchers.IO) {
        db.restrictionDao().saveRestriction(restriction)
    }

    suspend fun saveRestrictions(restrictions: List<AppRestriction>) = withContext(Dispatchers.IO) {
        db.restrictionDao().saveRestrictions(restrictions)
    }

    suspend fun getRestriction(packageName: String): AppRestriction? = withContext(Dispatchers.IO) {
        db.restrictionDao().getRestriction(packageName)
    }

    suspend fun deleteRestriction(packageName: String) = withContext(Dispatchers.IO) {
        db.restrictionDao().deleteRestriction(packageName)
    }

    suspend fun addLog(packageName: String, appName: String, action: String) = withContext(Dispatchers.IO) {
        db.usageLogDao().insertLog(AppUsageLog(packageName = packageName, appName = appName, action = action))
    }

    suspend fun clearLogs() = withContext(Dispatchers.IO) {
        db.usageLogDao().clearLogs()
    }

    suspend fun getParentPin(): String? = withContext(Dispatchers.IO) {
        db.settingDao().getSetting("parent_pin")?.value
    }

    suspend fun saveParentPin(pin: String) = withContext(Dispatchers.IO) {
        db.settingDao().saveSetting(ParentSetting("parent_pin", pin))
    }

    suspend fun isParentalModeEnabled(): Boolean = withContext(Dispatchers.IO) {
        db.settingDao().getSetting("parental_mode_enabled")?.value?.toBoolean() ?: true
    }

    suspend fun setParentalModeEnabled(enabled: Boolean) = withContext(Dispatchers.IO) {
        db.settingDao().saveSetting(ParentSetting("parental_mode_enabled", enabled.toString()))
    }

    fun getRealAppUsageMinutes(context: Context, packageName: String): Int {
        try {
            val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager ?: return 0
            val endTime = System.currentTimeMillis()
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val startTime = calendar.timeInMillis
            val stats = usageStatsManager.queryAndAggregateUsageStats(startTime, endTime)
            val usage = stats[packageName] ?: return 0
            return (usage.totalTimeInForeground / (1000 * 60)).toInt()
        } catch (e: Exception) {
            return 0
        }
    }
}
