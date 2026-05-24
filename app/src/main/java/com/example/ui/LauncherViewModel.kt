package com.example.ui

import android.app.AppOpsManager
import android.content.Context
import android.os.Build
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Process
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.AppRestriction
import com.example.data.LauncherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class LauncherScreen {
    object Home : LauncherScreen()
    object Drawer : LauncherScreen()
    data class EnterPin(val nextScreen: LauncherScreen) : LauncherScreen()
    object SetupPin : LauncherScreen()
    object ParentalDashboard : LauncherScreen()
    object NovaX : LauncherScreen()
}

data class AppItemUi(
    val packageName: String,
    val label: String,
    val isBlocked: Boolean,
    val limitMinutes: Int,
    val usedMinutesToday: Int,
    val isSystemApp: Boolean
)

class LauncherViewModel(
    private val context: Context,
    private val repository: LauncherRepository
) : ViewModel() {

    // Main navigation flow inside our Single-Screen setup
    private val _currentScreen = MutableStateFlow<LauncherScreen>(LauncherScreen.Home)
    val currentScreen: StateFlow<LauncherScreen> = _currentScreen

    // Error messages/parent alerts
    private val _uiAlert = MutableStateFlow<String?>(null)
    val uiAlert: StateFlow<String?> = _uiAlert

    // Search query inside the Drawer
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    // Parental PIN state
    private val _storedPin = MutableStateFlow<String?>(null)
    val storedPin: StateFlow<String?> = _storedPin

    private val _isParentalModeEnabled = MutableStateFlow(true)
    val isParentalModeEnabled: StateFlow<Boolean> = _isParentalModeEnabled

    private val _launcherLayoutMode = MutableStateFlow("horizontal_pager")
    val launcherLayoutMode: StateFlow<String> = _launcherLayoutMode

    // Customizable Wallpaper theme state: default 'space_sky'
    private val _wallpaperValue = MutableStateFlow("space_sky")
    val wallpaperValue: StateFlow<String> = _wallpaperValue

    // Nova X themes / custom settings
    private val _novaXThemeStyle = MutableStateFlow("glass_shaffof")
    val novaXThemeStyle: StateFlow<String> = _novaXThemeStyle

    private val _novaXWebWallpaperUrl = MutableStateFlow("")
    val novaXWebWallpaperUrl: StateFlow<String> = _novaXWebWallpaperUrl

    private val _customAppLabels = MutableStateFlow<Map<String, String>>(emptyMap())
    val customAppLabels: StateFlow<Map<String, String>> = _customAppLabels

    private val _novaXIconTint = MutableStateFlow("none")
    val novaXIconTint: StateFlow<String> = _novaXIconTint

    private val _novaXColumnGridCount = MutableStateFlow(4)
    val novaXColumnGridCount: StateFlow<Int> = _novaXColumnGridCount

    // Custom App placements/ordering indices
    private val _appOrders = MutableStateFlow<Map<String, Int>>(emptyMap())
    val appOrders: StateFlow<Map<String, Int>> = _appOrders

    // Temporary storage for app limits
    private val _activeAppToLimit = MutableStateFlow<AppItemUi?>(null)
    val activeAppToLimit: StateFlow<AppItemUi?> = _activeAppToLimit

    // Overlay lock state (active if a child clicked a blocked app)
    private val _blockedAppOverlay = MutableStateFlow<AppItemUi?>(null)
    val blockedAppOverlay: StateFlow<AppItemUi?> = _blockedAppOverlay

    // Physical installed apps info on the device
    private val _installedLauncherApps = MutableStateFlow<List<LauncherAppInfo>>(emptyList())

    // Tracks last launched package and launch timestamp for fallback local timer
    private var lastLaunchedPackage: String? = null
    private var lastLaunchTimeMs: Long = 0L

    init {
        viewModelScope.launch {
            _storedPin.value = repository.getParentPin()
            _isParentalModeEnabled.value = repository.isParentalModeEnabled()
            _launcherLayoutMode.value = repository.getLauncherLayoutMode() ?: "horizontal_pager"
            _wallpaperValue.value = repository.getWallpaperValue() ?: "space_sky"

            // Nova X Loads
            _novaXThemeStyle.value = repository.getGenericSetting("novax_theme_style") ?: "glass_shaffof"
            _novaXWebWallpaperUrl.value = repository.getGenericSetting("novax_web_wallpaper") ?: ""
            _novaXIconTint.value = repository.getGenericSetting("novax_icon_tint") ?: "none"
            _novaXColumnGridCount.value = repository.getGenericSetting("novax_column_grid_count")?.toIntOrNull() ?: 4

            refreshAppsList()
        }
    }

    // Combine manual app information with database locks, usage limits, and custom labels
    val appListCombined: StateFlow<List<AppItemUi>> = combine(
        _installedLauncherApps,
        repository.allRestrictions,
        _isParentalModeEnabled,
        _customAppLabels
    ) { installedApps, restrictions, parentalEnabled, customLabels ->
        val restrictionMap = restrictions.associateBy { it.packageName }
        installedApps.map { app ->
            val restriction = restrictionMap[app.packageName]
            val limit = restriction?.limitMinutes ?: 0
            val isBlocked = restriction?.isBlocked ?: false

            // Get live usage: either from System UsageStats or our Room fallback cached timer
            val usedToday = if (parentalEnabled) {
                val systemUsage = repository.getRealAppUsageMinutes(context, app.packageName)
                if (systemUsage > 0) {
                    systemUsage
                } else {
                    restriction?.remainingMinutesToday ?: 0 // Fallback: locally accumulated duration
                }
            } else {
                0
            }

            val finalLabel = customLabels[app.packageName] ?: app.label

            AppItemUi(
                packageName = app.packageName,
                label = finalLabel,
                isBlocked = isBlocked,
                limitMinutes = limit,
                usedMinutesToday = usedToday,
                isSystemApp = app.isSystemApp
            )
        }
    }.combine(_appOrders) { appsList, orders ->
        appsList.sortedBy { orders[it.packageName] ?: Int.MAX_VALUE }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Filtered apps list for launcher desk/drawer
    val filteredDrawerApps: StateFlow<List<AppItemUi>> = combine(
        appListCombined,
        _searchQuery
    ) { apps, query ->
        if (query.trim().isEmpty()) {
            apps
        } else {
            apps.filter { it.label.contains(query, ignoreCase = true) }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Allowed (visible/unlocked) apps grid on child's homescreen
    val allowedHomeApps: StateFlow<List<AppItemUi>> = appListCombined
        .combine(_isParentalModeEnabled) { apps, parentalEnabled ->
            if (parentalEnabled) {
                // Filter out apps that are fully blocked OR have reached their daily limit
                apps.filter { !it.isBlocked && (it.limitMinutes == 0 || it.usedMinutesToday < it.limitMinutes) }
            } else {
                apps
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val logsList = repository.allLogs.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun navigateTo(screen: LauncherScreen) {
        _currentScreen.value = screen
    }

    fun setUiAlert(message: String?) {
        _uiAlert.value = message
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun closeBlockOverlay() {
        _blockedAppOverlay.value = null
    }

    fun setLauncherLayoutMode(mode: String) {
        viewModelScope.launch {
            repository.saveLauncherLayoutMode(mode)
            _launcherLayoutMode.value = mode
        }
    }

    fun setWallpaperValue(theme: String) {
        viewModelScope.launch {
            repository.saveWallpaperValue(theme)
            _wallpaperValue.value = theme
        }
    }

    fun moveAppLeftOrUp(packageName: String, visibleApps: List<AppItemUi>) {
        viewModelScope.launch {
            val currentIndex = visibleApps.indexOfFirst { it.packageName == packageName }
            if (currentIndex > 0) {
                val otherApp = visibleApps[currentIndex - 1]
                val currentOrderVal = _appOrders.value[packageName] ?: currentIndex
                val otherOrderVal = _appOrders.value[otherApp.packageName] ?: (currentIndex - 1)

                val finalCurrentVal = if (currentOrderVal == otherOrderVal) currentOrderVal + 1 else currentOrderVal

                repository.saveAppOrder(packageName, otherOrderVal)
                repository.saveAppOrder(otherApp.packageName, finalCurrentVal)

                val newMap = _appOrders.value.toMutableMap()
                newMap[packageName] = otherOrderVal
                newMap[otherApp.packageName] = finalCurrentVal
                _appOrders.value = newMap
            }
        }
    }

    fun moveAppRightOrDown(packageName: String, visibleApps: List<AppItemUi>) {
        viewModelScope.launch {
            val currentIndex = visibleApps.indexOfFirst { it.packageName == packageName }
            if (currentIndex >= 0 && currentIndex < visibleApps.size - 1) {
                val otherApp = visibleApps[currentIndex + 1]
                val currentOrderVal = _appOrders.value[packageName] ?: currentIndex
                val otherOrderVal = _appOrders.value[otherApp.packageName] ?: (currentIndex + 1)

                val finalCurrentVal = if (currentOrderVal == otherOrderVal) currentOrderVal - 1 else currentOrderVal

                repository.saveAppOrder(packageName, otherOrderVal)
                repository.saveAppOrder(otherApp.packageName, finalCurrentVal)

                val newMap = _appOrders.value.toMutableMap()
                newMap[packageName] = otherOrderVal
                newMap[otherApp.packageName] = finalCurrentVal
                _appOrders.value = newMap
            }
        }
    }

    // Refresh PIN state
    fun refreshPinState() {
        viewModelScope.launch {
            _storedPin.value = repository.getParentPin()
        }
    }

    // Change parental monitoring toggle
    fun toggleParentalMode(enabled: Boolean) {
        viewModelScope.launch {
            repository.setParentalModeEnabled(enabled)
            _isParentalModeEnabled.value = enabled
            repository.addLog("system", "Nova Safe Launcher", if (enabled) "PARENTAL_MODE_ON" else "PARENTAL_MODE_OFF")
        }
    }

    // Save/register parental PIN
    fun setupPin(pin: String) {
        viewModelScope.launch {
            repository.saveParentPin(pin)
            _storedPin.value = pin
            _currentScreen.value = LauncherScreen.ParentalDashboard
            setUiAlert("PIN mufaqqiyatli o'rnatildi!")
        }
    }

    // Update individual restriction
    fun updateAppLock(packageName: String, label: String, isBlocked: Boolean) {
        viewModelScope.launch {
            val existing = repository.getRestriction(packageName)
            val newRestriction = AppRestriction(
                packageName = packageName,
                appName = label,
                isBlocked = isBlocked,
                limitMinutes = existing?.limitMinutes ?: 0,
                remainingMinutesToday = existing?.remainingMinutesToday ?: 0
            )
            repository.saveRestriction(newRestriction)
            repository.addLog(packageName, label, if (isBlocked) "RESTRICTION_BLOCKED" else "RESTRICTION_UNBLOCKED")
        }
    }

    fun selectAppToLimit(app: AppItemUi?) {
        _activeAppToLimit.value = app
    }

    fun updateAppLimit(packageName: String, label: String, limitMinutes: Int) {
        viewModelScope.launch {
            val existing = repository.getRestriction(packageName)
            val newRestriction = AppRestriction(
                packageName = packageName,
                appName = label,
                isBlocked = existing?.isBlocked ?: false,
                limitMinutes = limitMinutes,
                remainingMinutesToday = existing?.remainingMinutesToday ?: 0
            )
            repository.saveRestriction(newRestriction)
            _activeAppToLimit.value = null
            repository.addLog(packageName, label, "LIMIT_SET_${limitMinutes}M")
        }
    }

    fun clearAllLogs() {
        viewModelScope.launch {
            repository.clearLogs()
        }
    }

    // Main launcher logic: launch target package or trigger blocked overlay
    fun selectAndLaunchApp(context: Context, app: AppItemUi) {
        if (app.packageName == "com.novax.settings") {
            navigateTo(LauncherScreen.NovaX)
            return
        }
        viewModelScope.launch {
            // Check parental locks
            if (_isParentalModeEnabled.value) {
                if (app.isBlocked) {
                    _blockedAppOverlay.value = app
                    repository.addLog(app.packageName, app.label, "BLOCKED_ATTEMPT")
                    return@launch
                }

                if (app.limitMinutes > 0 && app.usedMinutesToday >= app.limitMinutes) {
                    _blockedAppOverlay.value = app
                    repository.addLog(app.packageName, app.label, "LIMIT_EXCEEDED_ATTEMPT")
                    return@launch
                }
            }

            // Launch the application activity
            try {
                val pm = context.packageManager
                val intent = pm.getLaunchIntentForPackage(app.packageName)
                if (intent != null) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)

                    // Track details for fallback timer
                    lastLaunchedPackage = app.packageName
                    lastLaunchTimeMs = System.currentTimeMillis()

                    repository.addLog(app.packageName, app.label, "LAUNCHED")
                } else {
                    setUiAlert("${app.label} ilovasini ishga tushirib bo'lmadi!")
                }
            } catch (e: Exception) {
                setUiAlert("Xatolik yuz berdi: ${e.message}")
            }
        }
    }

    // Called whenever default Home Activity resumes (means kid has closed/left other apps and came back)
    fun onHomeResumed() {
        viewModelScope.launch {
            val packageToUpdate = lastLaunchedPackage
            val startMs = lastLaunchTimeMs
            if (packageToUpdate != null && startMs > 0L) {
                val elapsedMinutes = ((System.currentTimeMillis() - startMs) / (1000 * 60)).toInt()
                if (elapsedMinutes > 0) {
                    val restriction = repository.getRestriction(packageToUpdate)
                    if (restriction != null) {
                        val accumulatedUsed = restriction.remainingMinutesToday + elapsedMinutes
                        repository.saveRestriction(
                            restriction.copy(remainingMinutesToday = accumulatedUsed)
                        )
                        repository.addLog(
                            packageToUpdate,
                            restriction.appName,
                            "SESSION_CLOSED_USED_${elapsedMinutes}M"
                        )
                    }
                }
                lastLaunchedPackage = null
                lastLaunchTimeMs = 0L
            }
        }
    }

    // Check if system USAGE_STATS permission is granted
    fun isUsageAccessGranted(): Boolean {
        return try {
            val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
            val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                appOps.unsafeCheckOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    Process.myUid(),
                    context.packageName
                )
            } else {
                appOps.checkOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    Process.myUid(),
                    context.packageName
                )
            }
            mode == AppOpsManager.MODE_ALLOWED
        } catch (e: Exception) {
            false
        }
    }

    // Query physical PackageManager and synchronize existing config rows in Room
    fun refreshAppsList() {
        viewModelScope.launch {
            val pm = context.packageManager
            val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val resolveInfos = pm.queryIntentActivities(mainIntent, 0)
            val physicalApps = resolveInfos.map { resolveInfo ->
                val packageName = resolveInfo.activityInfo.packageName
                val label = resolveInfo.loadLabel(pm).toString()
                val isSystemApp = (resolveInfo.activityInfo.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0
                LauncherAppInfo(packageName, label, isSystemApp)
            }.distinctBy { it.packageName }
                .filter { it.packageName != context.packageName } // exclude self
                .toMutableList()

            // Inject the virtual "Nova Sozlamalari" app so it sits in the app lists like a native app
            physicalApps.add(
                LauncherAppInfo(
                    packageName = "com.novax.settings",
                    label = "Nova Sozlamalari",
                    isSystemApp = true
                )
            )

            _installedLauncherApps.value = physicalApps
            loadCustomAppLabels(physicalApps)

            // Populate system-wide custom sorting orders
            val orders = mutableMapOf<String, Int>()
            physicalApps.forEachIndexed { index, app ->
                val savedOrder = repository.getAppOrder(app.packageName)
                if (savedOrder != null) {
                    orders[app.packageName] = savedOrder
                } else {
                    orders[app.packageName] = index
                    repository.saveAppOrder(app.packageName, index)
                }
            }
            _appOrders.value = orders

            // Sync with Room database so any new app gets default settings
            repository.allRestrictions.collect { existingRestrictions ->
                val existingMap = existingRestrictions.associateBy { it.packageName }
                val newEntries = mutableListOf<AppRestriction>()
                physicalApps.forEach { app ->
                    if (!existingMap.containsKey(app.packageName)) {
                        newEntries.add(
                            AppRestriction(
                                packageName = app.packageName,
                                appName = app.label,
                                isBlocked = false,
                                limitMinutes = 0,
                                remainingMinutesToday = 0
                            )
                        )
                    }
                }
                if (newEntries.isNotEmpty()) {
                    repository.saveRestrictions(newEntries)
                }
            }
        }
    }

    private fun loadCustomAppLabels(installed: List<LauncherAppInfo>) {
        viewModelScope.launch {
            val map = mutableMapOf<String, String>()
            installed.forEach { app ->
                val custom = repository.getGenericSetting("app_label_${app.packageName}")
                if (!custom.isNullOrEmpty()) {
                    map[app.packageName] = custom
                }
            }
            _customAppLabels.value = map
        }
    }

    fun setNovaXThemeStyle(style: String) {
        viewModelScope.launch {
            repository.saveGenericSetting("novax_theme_style", style)
            _novaXThemeStyle.value = style
        }
    }

    fun setNovaXWebWallpaperUrl(url: String) {
        viewModelScope.launch {
            repository.saveGenericSetting("novax_web_wallpaper", url)
            _novaXWebWallpaperUrl.value = url
        }
    }

    fun setNovaXIconTint(tint: String) {
        viewModelScope.launch {
            repository.saveGenericSetting("novax_icon_tint", tint)
            _novaXIconTint.value = tint
        }
    }

    fun setNovaXColumnGridCount(count: Int) {
        viewModelScope.launch {
            repository.saveGenericSetting("novax_column_grid_count", count.toString())
            _novaXColumnGridCount.value = count
        }
    }

    fun setCustomAppLabel(packageName: String, newLabel: String) {
        viewModelScope.launch {
            repository.saveGenericSetting("app_label_$packageName", newLabel)
            val updated = _customAppLabels.value.toMutableMap()
            if (newLabel.isEmpty()) {
                updated.remove(packageName)
            } else {
                updated[packageName] = newLabel
            }
            _customAppLabels.value = updated
        }
    }
}

data class LauncherAppInfo(
    val packageName: String,
    val label: String,
    val isSystemApp: Boolean
)

@Suppress("UNCHECKED_CAST")
class LauncherViewModelFactory(
    private val context: Context,
    private val repository: LauncherRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LauncherViewModel::class.java)) {
            return LauncherViewModel(context, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
