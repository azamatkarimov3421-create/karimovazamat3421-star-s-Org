package com.example.ui

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Custom modern palette matching Nova Launcher aesthetics
val CosmicDarkBg = Color(0xFF0F111A)
val SolidCardBg = Color(0xFF1E2235)
val CyberAccent = Color(0xFF3F51B5)
val VividAmethyst = Color(0xFF8A2BE2)
val SafeEmerald = Color(0xFF10B981)
val BlockCrimson = Color(0xFFEF4444)

@Composable
fun WallpaperBackground(theme: String, customWebUrl: String = "") {
    if (customWebUrl.isNotEmpty()) {
        Box(modifier = Modifier.fillMaxSize()) {
            coil.compose.AsyncImage(
                model = customWebUrl,
                contentDescription = "Nova X Web Wallpaper",
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            // Beautiful translucent safety overlay so text maintains outstanding accessibility contrast
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.55f))
            )
        }
    } else {
        val brush = when (theme) {
            "space_sky" -> Brush.verticalGradient(
                colors = listOf(Color(0xFF0F0C20), Color(0xFF1A1635), Color(0xFF0A0812))
            )
            "neon_sunset" -> Brush.verticalGradient(
                colors = listOf(Color(0xFF22051E), Color(0xFF4C0A4B), Color(0xFF6B1A40))
            )
            "nordic_forest" -> Brush.verticalGradient(
                colors = listOf(Color(0xFF041814), Color(0xFF062B22), Color(0xFF0A1009))
            )
            "charcoal_elegance" -> Brush.verticalGradient(
                colors = listOf(Color(0xFF141416), Color(0xFF252529), Color(0xFF111112))
            )
            "aura_holo" -> Brush.verticalGradient(
                colors = listOf(Color(0xFF0A071E), Color(0xFF1E1430), Color(0xFF0A153A))
            )
            "crimson_shadow" -> Brush.verticalGradient(
                colors = listOf(Color(0xFF1D0303), Color(0xFF3D090E), Color(0xFF0F0102))
            )
            else -> Brush.verticalGradient(
                colors = listOf(Color(0xFF0F0C20), Color(0xFF1A1635), Color(0xFF0A0812))
            )
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brush)
        )
    }
}

@Composable
fun SafeLauncherRootUi(viewModel: LauncherViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val uiAlert by viewModel.uiAlert.collectAsState()
    val blockedAppOverlay by viewModel.blockedAppOverlay.collectAsState()
    val wallpaperTheme by viewModel.wallpaperValue.collectAsState()
    val webWallpaperUrl by viewModel.novaXWebWallpaperUrl.collectAsState()
    val isSystemDark = androidx.compose.foundation.isSystemInDarkTheme()
    val primaryColor = MaterialTheme.colorScheme.primary

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        WallpaperBackground(wallpaperTheme, webWallpaperUrl)

        // Base atmospheric transparent background that supports system wallpaper natively
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
                .drawBehind {
                    if (isSystemDark) {
                        drawRect(Color.Black.copy(alpha = 0.25f))
                    } else {
                        drawRect(Color.White.copy(alpha = 0.15f))
                    }
                }
        ) {
        // Render screens inside our Single-Screen setup flow
        when (val screen = currentScreen) {
            is LauncherScreen.Home -> HomeScreen(viewModel)
            is LauncherScreen.Drawer -> AllAppsDrawerScreen(viewModel)
            is LauncherScreen.EnterPin -> EnterPinScreen(viewModel, screen.nextScreen)
            is LauncherScreen.SetupPin -> SetupPinScreen(viewModel)
            is LauncherScreen.ParentalDashboard -> ParentalDashboardScreen(viewModel)
            is LauncherScreen.NovaX -> NovaXScreen(viewModel)
        }

        // Live Blocked Overlay Layer (when kids trigger locks inside Launcher)
        AnimatedVisibility(
            visible = blockedAppOverlay != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            blockedAppOverlay?.let { blockedApp ->
                BlockedOverlayScreen(blockedApp, viewModel)
            }
        }

        // Beautiful fleeting global notification alerts
        AnimatedVisibility(
            visible = uiAlert != null,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 96.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = primaryColor),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(8.dp),
                modifier = Modifier.padding(horizontal = 24.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Info, contentDescription = "Alert", tint = Color.White)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = uiAlert ?: "",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            LaunchedEffect(uiAlert) {
                if (uiAlert != null) {
                    delay(3000)
                    viewModel.setUiAlert(null)
                }
            }
        }
    }
}
}

@Composable
fun HomeScreenAppItem(
    app: AppItemUi,
    itemIconSize: androidx.compose.ui.unit.Dp,
    itemFontSize: androidx.compose.ui.unit.TextUnit,
    context: Context,
    viewModel: LauncherViewModel,
    onAppLongClick: (AppItemUi) -> Unit
) {
    val iconTintName by viewModel.novaXIconTint.collectAsState()
    val themeStyle by viewModel.novaXThemeStyle.collectAsState()

    // Determine specific styling modifiers based on the active Nova X theme style
    val itemBgModifier = when (themeStyle) {
        "glass_shaffof" -> Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.08f))
            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
        "neon_cyber" -> Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF120E2E).copy(alpha = 0.85f))
            .border(1.dp, Color(0xFFE040FB), RoundedCornerShape(8.dp))
        "oled_dark" -> Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(Color.Black)
            .border(1.dp, Color.White, RoundedCornerShape(4.dp))
        "pixel_8bit" -> Modifier
            .background(Color(0xFF321F0D))
            .border(2.dp, Color(0xFFFFCC00))
        "cosmic_aura" -> Modifier
            .clip(CircleShape)
            .background(Color(0xFF1E1B4B).copy(alpha = 0.5f))
            .border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.4f), CircleShape)
        else -> Modifier
    }

    val textColor = when (themeStyle) {
        "neon_cyber" -> Color(0xFF00E5FF)
        "pixel_8bit" -> Color(0xFFFFCC00)
        else -> Color.White
    }

    val textStyle = when (themeStyle) {
        "pixel_8bit" -> androidx.compose.ui.text.TextStyle(
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.sp
        )
        else -> androidx.compose.ui.text.TextStyle(
            fontWeight = FontWeight.Medium
        )
    }

    Column(
        modifier = Modifier
            .testTag("app_${app.packageName}")
            .pointerInput(app.packageName) {
                detectTapGestures(
                    onTap = { viewModel.selectAndLaunchApp(context, app) },
                    onLongPress = { onAppLongClick(app) }
                )
            }
            .then(itemBgModifier)
            .padding(vertical = 12.dp, horizontal = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AppIconView(packageName = app.packageName, size = itemIconSize, iconTintName = iconTintName)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = app.label,
            color = textColor,
            fontSize = itemFontSize,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            style = textStyle
        )
        // Time restriction warning beneath icon
        if (app.limitMinutes > 0) {
            val remaining = (app.limitMinutes - app.usedMinutesToday).coerceAtLeast(0)
            Text(
                text = "${remaining}m",
                color = if (remaining < 10) BlockCrimson else SafeEmerald,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// 1. HOME SCREEN / THE DESKTOP
@Composable
fun HomeScreen(viewModel: LauncherViewModel) {
    val context = LocalContext.current
    val apps by viewModel.allowedHomeApps.collectAsState()
    val isParentalMode by viewModel.isParentalModeEnabled.collectAsState()
    val storedPin by viewModel.storedPin.collectAsState()
    val layoutMode by viewModel.launcherLayoutMode.collectAsState()

    val primaryAccent = MaterialTheme.colorScheme.primary
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()

    // Dynamic Dock Favorites (top 4 allowed apps)
    val dockApps = remember(apps) { apps.take(4) }

    var activeAppForActions by remember { mutableStateOf<AppItemUi?>(null) }
    var showWallpaperDialog by remember { mutableStateOf(false) }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val containerWidth = maxWidth

        // Invisible background layer for empty space long press detection
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = {
                            showWallpaperDialog = true
                        }
                    )
                }
        )

        val columnGridCount by viewModel.novaXColumnGridCount.collectAsState()

        // Let's decide how many columns to show
        val columnsCount = columnGridCount

        // Font and Icon sizes depending on mode and width
        val itemIconSize = when (layoutMode) {
            "simple" -> 64.dp
            "classic" -> 56.dp
            else -> if (containerWidth < 500.dp) 56.dp else 64.dp
        }
        val itemFontSize = when (layoutMode) {
            "simple" -> 14.sp
            "classic" -> 12.sp
            else -> if (containerWidth < 500.dp) 12.sp else 14.sp
        }

        val dockIconSize = when (layoutMode) {
            "simple" -> 60.dp
            "classic" -> 48.dp
            else -> if (containerWidth < 500.dp) 48.dp else 60.dp
        }
        val dockFontSize = when (layoutMode) {
            "simple" -> 13.sp
            "classic" -> 11.sp
            else -> if (containerWidth < 500.dp) 11.sp else 13.sp
        }
        val searchFontSize = when (layoutMode) {
            "simple" -> 15.sp
            "classic" -> 13.sp
            else -> if (containerWidth < 500.dp) 13.sp else 15.sp
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(top = 16.dp, bottom = 16.dp)
                .pointerInput(Unit) {
                    detectVerticalDragGestures { _, dragAmount ->
                        if (dragAmount < -15) { // Swipe Up detected anywhere on home desktop
                            viewModel.navigateTo(LauncherScreen.Drawer)
                        }
                    }
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Upper section containing Top header + Clock + Center Grid
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Quick Header tools (Parent options / status)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Shield status representation
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isParentalMode) SafeEmerald.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.1f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Shield",
                            tint = if (isParentalMode) SafeEmerald else Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isParentalMode) "Himoya Faol" else "Nazorat O'chiq",
                            color = if (isParentalMode) SafeEmerald else Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Customizers and settings
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Nova X Custom Studio Launcher
                        IconButton(
                            onClick = {
                                viewModel.navigateTo(LauncherScreen.NovaX)
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(Color(0xFFE040FB), Color(0xFF00E5FF))))
                                .testTag("novax_studio_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Nova X Customizer",
                                tint = Color.White
                            )
                        }

                        // Parental settings launcher
                        IconButton(
                            onClick = {
                                if (storedPin.isNullOrEmpty()) {
                                    viewModel.navigateTo(LauncherScreen.SetupPin)
                                } else {
                                    viewModel.navigateTo(LauncherScreen.EnterPin(LauncherScreen.ParentalDashboard))
                                }
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.11f))
                                .testTag("parental_settings_button")
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = "Ota-ona xonasi", tint = Color.White)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Large Digital Clock & Date
                ClockWidget()

                Spacer(modifier = Modifier.height(28.dp))

                // Grid of child allowed packages / apps
                if (apps.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = "No apps",
                                tint = Color.White.copy(alpha = 0.5f),
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Ruxsat berilgan ilovalar mavjud emas.",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 15.sp,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Aktivlashtirish uchun ota-ona PIN kodi bilan kiring.",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                } else {
                    when (layoutMode) {
                        "horizontal_pager" -> {
                            val itemsPerPage = columnsCount * 3
                            val pages = remember(apps, itemsPerPage) { apps.chunked(itemsPerPage) }
                            val pagerState = rememberPagerState(pageCount = { pages.size })

                            Column(
                                modifier = Modifier.weight(1f).fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                HorizontalPager(
                                    state = pagerState,
                                    modifier = Modifier.weight(1f).fillMaxWidth()
                                ) { pageIndex ->
                                    val pageApps = pages.getOrNull(pageIndex) ?: emptyList()
                                    LazyVerticalGrid(
                                        columns = GridCells.Fixed(columnsCount),
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(horizontal = 16.dp),
                                        contentPadding = PaddingValues(16.dp),
                                        userScrollEnabled = false,
                                        verticalArrangement = Arrangement.spacedBy(24.dp),
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                    items(pageApps, key = { it.packageName }) { app ->
                                        HomeScreenAppItem(app, itemIconSize, itemFontSize, context, viewModel) { selectedApp ->
                                            activeAppForActions = selectedApp
                                        }
                                    }
                                    }
                                }

                                if (pages.size > 1) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier
                                            .height(24.dp)
                                            .fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        repeat(pages.size) { iteration ->
                                            val color = if (pagerState.currentPage == iteration) VividAmethyst else Color.White.copy(alpha = 0.3f)
                                            val width = if (pagerState.currentPage == iteration) 16.dp else 6.dp
                                            Box(
                                                modifier = Modifier
                                                    .padding(4.dp)
                                                    .clip(CircleShape)
                                                    .background(color)
                                                    .size(width = width, height = 6.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        "carousel_row" -> {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                LazyRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    items(apps, key = { it.packageName }) { app ->
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
                                            shape = RoundedCornerShape(20.dp),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                                            modifier = Modifier
                                                .width(115.dp)
                                                .pointerInput(app.packageName) {
                                                    detectTapGestures(
                                                        onTap = { viewModel.selectAndLaunchApp(context, app) },
                                                        onLongPress = { activeAppForActions = app }
                                                    )
                                                }
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(12.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Center
                                            ) {
                                                AppIconView(packageName = app.packageName, size = itemIconSize)
                                                Spacer(modifier = Modifier.height(10.dp))
                                                Text(
                                                    text = app.label,
                                                    color = Color.White,
                                                    fontSize = itemFontSize,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    textAlign = TextAlign.Center
                                                )
                                                if (app.limitMinutes > 0) {
                                                    val remaining = (app.limitMinutes - app.usedMinutesToday).coerceAtLeast(0)
                                                    Text(
                                                        text = "${remaining}m",
                                                        color = if (remaining < 10) BlockCrimson else SafeEmerald,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(top = 4.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        "niagara_list" -> {
                            val sortedApps = remember(apps) { apps.sortedBy { it.label.uppercase() } }
                            val listState = androidx.compose.foundation.lazy.rememberLazyListState()

                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                            ) {
                                LazyColumn(
                                    state = listState,
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    contentPadding = PaddingValues(vertical = 12.dp)
                                ) {
                                    items(sortedApps, key = { it.packageName }) { app ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(Color.White.copy(alpha = 0.05f))
                                                .pointerInput(app.packageName) {
                                                    detectTapGestures(
                                                        onTap = { viewModel.selectAndLaunchApp(context, app) },
                                                        onLongPress = { activeAppForActions = app }
                                                    )
                                                }
                                                .padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            AppIconView(packageName = app.packageName, size = 42.dp)
                                            Spacer(modifier = Modifier.width(16.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = app.label,
                                                    color = Color.White,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Medium
                                                )
                                                if (app.limitMinutes > 0) {
                                                    val remaining = (app.limitMinutes - app.usedMinutesToday).coerceAtLeast(0)
                                                    Text(
                                                        text = "Cheklov: ${app.limitMinutes}m / qoldi: ${remaining}m",
                                                        color = if (remaining < 10) BlockCrimson else SafeEmerald,
                                                        fontSize = 11.sp
                                                    )
                                                }
                                            }
                                            Icon(
                                                imageVector = Icons.Default.ArrowBack,
                                                contentDescription = null,
                                                tint = Color.White.copy(alpha = 0.3f),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }

                                val letters = remember(sortedApps) {
                                    sortedApps.map { it.label.take(1).uppercase() }.distinct()
                                }
                                if (letters.isNotEmpty()) {
                                    Column(
                                        modifier = Modifier
                                            .width(28.dp)
                                            .fillMaxHeight()
                                            .padding(vertical = 16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.SpaceEvenly
                                    ) {
                                        letters.forEach { char ->
                                            Text(
                                                text = char,
                                                color = Color.White.copy(alpha = 0.7f),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier
                                                    .clickable {
                                                        val targetIndex = sortedApps.indexOfFirst { it.label.startsWith(char, ignoreCase = true) }
                                                        if (targetIndex >= 0) {
                                                            coroutineScope.launch {
                                                                listState.animateScrollToItem(targetIndex)
                                                            }
                                                        }
                                                    }
                                                    .padding(vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        "dense_grid" -> {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(5),
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(apps, key = { it.packageName }) { app ->
                                    HomeScreenAppItem(app, 44.dp, 11.sp, context, viewModel) { selectedApp ->
                                        activeAppForActions = selectedApp
                                    }
                                }
                            }
                        }
                        else -> { // classic, simple, auto
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(columnsCount),
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(24.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(apps, key = { it.packageName }) { app ->
                                    HomeScreenAppItem(app, itemIconSize, itemFontSize, context, viewModel) { selectedApp ->
                                        activeAppForActions = selectedApp
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Native Launcher Dock + Search Drawer trigger area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Hotseat Dock - Houses shortcut favorites (duplicating/exhibiting the first 4 frequently-used allowed apps)
                if (dockApps.isNotEmpty()) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.28f),
                        shape = RoundedCornerShape(26.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 14.dp, horizontal = 12.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            dockApps.forEach { app ->
                                Column(
                                    modifier = Modifier
                                        .pointerInput(app.packageName) {
                                            detectTapGestures(
                                                onTap = { viewModel.selectAndLaunchApp(context, app) },
                                                onLongPress = { activeAppForActions = app }
                                            )
                                        }
                                        .padding(horizontal = 6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    AppIconView(packageName = app.packageName, size = dockIconSize)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = app.label,
                                        color = Color.White,
                                        fontSize = dockFontSize,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.width(62.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Glowing Search Bar (Mimics Google Pixel Search Widget / Swipe-up visual affordance)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .clip(RoundedCornerShape(26.dp))
                        .background(Color.White.copy(alpha = 0.16f))
                        .clickable { viewModel.navigateTo(LauncherScreen.Drawer) }
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (layoutMode == "simple") "Ilovalarni qidirish..." else "Ilovalarni qidirish va boshqarish...",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = searchFontSize,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Custom Overlays for Wallpapers and App Management
        if (activeAppForActions != null) {
            val app = activeAppForActions!!
            androidx.compose.ui.window.Dialog(
                onDismissRequest = { activeAppForActions = null }
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SolidCardBg),
                    shape = RoundedCornerShape(24.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, VividAmethyst.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Ilova Haqida va Joylashuv",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )

                        AppIconView(packageName = app.packageName, size = 64.dp)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(app.label, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp, textAlign = TextAlign.Center)
                            Text(app.packageName, color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp, textAlign = TextAlign.Center)
                        }

                        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                        Text("Ekrandagi Joylashuvni O'zgartirish", color = VividAmethyst, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = {
                                    viewModel.moveAppLeftOrUp(app.packageName, apps)
                                    viewModel.setUiAlert("${app.label} chapga/tepaga surildi")
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f)),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(10.dp)
                            ) {
                                Text("← Chapga", color = Color.White, fontSize = 12.sp)
                            }

                            Button(
                                onClick = {
                                    viewModel.moveAppRightOrDown(app.packageName, apps)
                                    viewModel.setUiAlert("${app.label} o'ngga/pastga surildi")
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f)),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(10.dp)
                            ) {
                                Text("O'ngga →", color = Color.White, fontSize = 12.sp)
                            }
                        }

                        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                        Button(
                            onClick = {
                                try {
                                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                        data = android.net.Uri.fromParts("package", app.packageName, null)
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(intent)
                                    activeAppForActions = null
                                } catch (e: Exception) {
                                    viewModel.setUiAlert("Xatolik: ${e.message}")
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = VividAmethyst),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Tizim Sozlamalari (App Info)", color = Color.White, fontSize = 12.sp)
                            }
                        }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("Nazorat Holati:", color = Color.White.copy(alpha = 0.7f), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                val blockStatus = if (app.isBlocked) "Bloklangan" else "Ruxsat berilgan"
                                val limitStatus = if (app.limitMinutes > 0) "${app.limitMinutes} daqiqa" else "Cheklov yo'q"
                                Text("• Blokirovka: $blockStatus", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                                Text("• Kunlik limit: $limitStatus (Sarflangan: ${app.usedMinutesToday}m)", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                            }
                        }

                        OutlinedButton(
                            onClick = { activeAppForActions = null },
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Yopish", fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        if (showWallpaperDialog) {
            androidx.compose.ui.window.Dialog(
                onDismissRequest = { showWallpaperDialog = false }
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SolidCardBg),
                    shape = RoundedCornerShape(24.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, VividAmethyst.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Ishchi Stol Sozlamalari",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text(
                            text = "Orqa fon rasmini o'zgartirish:",
                            color = Color.White.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp
                        )

                        val wallpapers = listOf(
                            Pair("space_sky", "Koinot"),
                            Pair("neon_sunset", "Pushti Neofon"),
                            Pair("nordic_forest", "Zulmat O'rmon"),
                            Pair("charcoal_elegance", "Matviy Qora"),
                            Pair("aura_holo", "Kosmik Aura"),
                            Pair("crimson_shadow", "Qizil Soya")
                        )

                        val currentWallpaper by viewModel.wallpaperValue.collectAsState()

                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(wallpapers) { (key, name) ->
                                val isSelected = currentWallpaper == key
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            when (key) {
                                                "space_sky" -> Brush.verticalGradient(
                                                    colors = listOf(Color(0xFF0F0C20), Color(0xFF1A1635))
                                                )
                                                "neon_sunset" -> Brush.verticalGradient(
                                                    colors = listOf(Color(0xFF22051E), Color(0xFF4C0A4B))
                                                )
                                                "nordic_forest" -> Brush.verticalGradient(
                                                    colors = listOf(Color(0xFF041814), Color(0xFF062B22))
                                                )
                                                "charcoal_elegance" -> Brush.verticalGradient(
                                                    colors = listOf(Color(0xFF141416), Color(0xFF252529))
                                                )
                                                "aura_holo" -> Brush.verticalGradient(
                                                    colors = listOf(Color(0xFF0A071E), Color(0xFF1E1430))
                                                )
                                                "crimson_shadow" -> Brush.verticalGradient(
                                                    colors = listOf(Color(0xFF1D0303), Color(0xFF3D090E))
                                                )
                                                else -> Brush.verticalGradient(
                                                    colors = listOf(Color(0xFF0F0C20), Color(0xFF1A1635))
                                                )
                                            }
                                        )
                                        .border(
                                            2.dp,
                                            if (isSelected) VividAmethyst else Color.White.copy(alpha = 0.15f),
                                            RoundedCornerShape(12.dp)
                                        )
                                        .clickable {
                                            viewModel.setWallpaperValue(key)
                                            viewModel.setUiAlert("Orqa fon yangilandi: $name")
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        name,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        maxLines = 1
                                    )
                                }
                            }
                        }

                        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                        Text(
                            text = "Android Tizim Funksiyalari:",
                            color = Color.White.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                Button(
                                    onClick = {
                                        try {
                                            context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
                                            showWallpaperDialog = false
                                        } catch (e: Exception) {
                                            viewModel.setUiAlert("Xatolik: ${e.message}")
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.08f)),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(10.dp)
                                ) {
                                    Text("Wi-Fi Sozlamalari", color = Color.White, fontSize = 11.sp, maxLines = 1)
                                }

                                Button(
                                    onClick = {
                                        try {
                                            context.startActivity(Intent(Settings.ACTION_DISPLAY_SETTINGS).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
                                            showWallpaperDialog = false
                                        } catch (e: Exception) {
                                            viewModel.setUiAlert("Xatolik: ${e.message}")
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.08f)),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(10.dp)
                                ) {
                                    Text("Ekran / Yorug'lik", color = Color.White, fontSize = 11.sp, maxLines = 1)
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                Button(
                                    onClick = {
                                        try {
                                            context.startActivity(Intent(Settings.ACTION_SETTINGS).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
                                            showWallpaperDialog = false
                                        } catch (e: Exception) {
                                            viewModel.setUiAlert("Xatolik: ${e.message}")
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.08f)),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(10.dp)
                                ) {
                                    Text("Tizim Sozlamalari", color = Color.White, fontSize = 11.sp, maxLines = 1)
                                }

                                Button(
                                    onClick = {
                                        try {
                                            context.startActivity(Intent(Settings.ACTION_HOME_SETTINGS).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
                                            showWallpaperDialog = false
                                        } catch (e: Exception) {
                                            viewModel.setUiAlert("Xatolik: ${e.message}")
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.08f)),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(10.dp)
                                ) {
                                    Text("Asosiy Launcher", color = Color.White, fontSize = 11.sp, maxLines = 1)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        OutlinedButton(
                            onClick = { showWallpaperDialog = false },
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Yopish", fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ClockWidget() {
    var currentTime by remember { mutableStateOf("") }
    var currentDate by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            currentDate = SimpleDateFormat("EEEE, d-MMMM", Locale.getDefault()).format(Date())
            delay(1000)
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = currentTime,
            color = Color.White,
            fontSize = 64.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-2).sp
        )
        Text(
            text = currentDate,
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

// 2. ALL IMAGES / ALL APPS DRAWER SCREEN
@Composable
fun AllAppsDrawerScreen(viewModel: LauncherViewModel) {
    val context = LocalContext.current
    val apps by viewModel.filteredDrawerApps.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isParentalMode by viewModel.isParentalModeEnabled.collectAsState()
    val layoutMode by viewModel.launcherLayoutMode.collectAsState()
    val primaryAccent = MaterialTheme.colorScheme.primary

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val containerWidth = maxWidth

        val columnsCount = when (layoutMode) {
            "classic" -> 4
            "simple" -> 3
            else -> { // auto
                if (containerWidth < 600.dp) 4 else if (containerWidth < 840.dp) 6 else 8
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(16.dp)
                .pointerInput(Unit) {
                    detectVerticalDragGestures { _, dragAmount ->
                        if (dragAmount > 15) { // Swipe down to exit drawer and go home
                            viewModel.setSearchQuery("")
                            viewModel.navigateTo(LauncherScreen.Home)
                        }
                    }
                }
        ) {
            // Top Search Field Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        viewModel.setSearchQuery("")
                        viewModel.navigateTo(LauncherScreen.Home)
                    },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.1f))
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back home", tint = Color.White)
                }

                Spacer(modifier = Modifier.width(12.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("Ilova nomini kiriting...", color = Color.White.copy(alpha = 0.5f)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primaryAccent,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                        focusedContainerColor = Color.White.copy(alpha = 0.08f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(28.dp),
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .testTag("app_search_field"),
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.White)
                            }
                        } else {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.White.copy(alpha = 0.5f))
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // All apps search result grid
            if (apps.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Bunday ilova topilmadi.",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 15.sp
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(columnsCount),
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(apps, key = { it.packageName }) { app ->
                        AndroViewBox(app, viewModel, context, isParentalMode)
                    }
                }
            }
        }
    }
}

@Composable
fun AndroViewBox(
    app: AppItemUi,
    viewModel: LauncherViewModel,
    context: android.content.Context,
    isParentalMode: Boolean
) {
    val iconTintName by viewModel.novaXIconTint.collectAsState()
    val themeStyle by viewModel.novaXThemeStyle.collectAsState()

    val textColor = when (themeStyle) {
        "neon_cyber" -> Color(0xFF00E5FF)
        "pixel_8bit" -> Color(0xFFFFCC00)
        else -> Color.White
    }

    val textStyle = when (themeStyle) {
        "pixel_8bit" -> androidx.compose.ui.text.TextStyle(
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.sp
        )
        else -> androidx.compose.ui.text.TextStyle(
            fontWeight = FontWeight.Medium
        )
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { viewModel.selectAndLaunchApp(context, app) }
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AppIconView(packageName = app.packageName, size = 52.dp, iconTintName = iconTintName)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = app.label,
                color = textColor,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                style = textStyle
            )

            // Locked badge indicator if parent lock is ON
            if (isParentalMode) {
                if (app.isBlocked) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(BlockCrimson.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = "Locked", tint = BlockCrimson, modifier = Modifier.size(10.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("Bloklangan", color = BlockCrimson, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    }
                } else if (app.limitMinutes > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    val left = (app.limitMinutes - app.usedMinutesToday).coerceAtLeast(0)
                    Text(
                        text = "Limit: ${left}m",
                        color = if (left == 0) BlockCrimson else SafeEmerald,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (left == 0) BlockCrimson.copy(alpha = 0.2f) else SafeEmerald.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

// 3. ENTER PIN KEYPAD SCREEN (Authorize parent)
@Composable
fun EnterPinScreen(viewModel: LauncherViewModel, nextScreen: LauncherScreen) {
    val storedPin by viewModel.storedPin.collectAsState()
    var inputPin by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.navigateTo(LauncherScreen.Home) },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.1f))
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Orqaga", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text("Ota-ona Ruxsati", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        // Title text and status dots
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val primaryAccent = MaterialTheme.colorScheme.primary
            Icon(Icons.Default.Lock, contentDescription = "Lock", tint = primaryAccent, modifier = Modifier.size(64.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Tizimni o'zgartirish uchun ota-ona PIN kodini kiriting",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Beautiful status display circles
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 1..4) {
                    val active = i <= inputPin.length
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(
                                if (pinError) BlockCrimson
                                else if (active) primaryAccent
                                else Color.White.copy(alpha = 0.2f)
                            )
                    )
                }
            }

            if (pinError) {
                Text(
                    text = "PIN kod noto'g'ri, qayta urinib ko'ring!",
                    color = BlockCrimson,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }

        // Graphical clean Keypad (touch target >= 48.dp guaranteed by design)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val keys = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("C", "0", "OK")
            )

            keys.forEach { rowKeys ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    rowKeys.forEach { key ->
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.1f))
                                .clickable {
                                    pinError = false
                                    when (key) {
                                        "C" -> {
                                            if (inputPin.isNotEmpty()) {
                                                inputPin = inputPin.dropLast(1)
                                            }
                                        }

                                        "OK" -> {
                                            if (inputPin == storedPin) {
                                                inputPin = ""
                                                viewModel.navigateTo(nextScreen)
                                            } else {
                                                pinError = true
                                                inputPin = ""
                                            }
                                        }

                                        else -> {
                                            if (inputPin.length < 4) {
                                                inputPin += key
                                                if (inputPin.length == 4) {
                                                    // Auto trigger OK check if 4 digits
                                                    if (inputPin == storedPin) {
                                                        inputPin = ""
                                                        viewModel.navigateTo(nextScreen)
                                                    } else {
                                                        pinError = true
                                                        inputPin = ""
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                .testTag("pin_key_$key"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = key,
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

// 4. SETUP CODE SCREEN (First-launch configuration)
@Composable
fun SetupPinScreen(viewModel: LauncherViewModel) {
    var pinValue by remember { mutableStateOf("") }
    var confirmValue by remember { mutableStateOf("") }
    var stepConfirm by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.navigateTo(LauncherScreen.Home) },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.1f))
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Orqaga", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text("PIN o'rnatish", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val primaryAccent = MaterialTheme.colorScheme.primary
            Icon(Icons.Default.Lock, contentDescription = "PIN secure", tint = primaryAccent, modifier = Modifier.size(64.dp))
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (!stepConfirm) "Yangi 4 xonali ota-ona PIN kodini o'rnating:" else "PIN kodni qaytadan kiritib tasdiqlang:",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Numeric visual display dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val lengthToDraw = if (!stepConfirm) pinValue.length else confirmValue.length
                for (i in 1..4) {
                    val active = i <= lengthToDraw
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(
                                if (active) primaryAccent else Color.White.copy(alpha = 0.2f)
                            )
                    )
                }
            }

            if (errorMsg != null) {
                Text(
                    text = errorMsg ?: "",
                    color = BlockCrimson,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 16.dp),
                    textAlign = TextAlign.Center
                )
            }
        }

        // Graphical numeric keypad
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val keys = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("C", "0", "OK")
            )

            keys.forEach { rowKeys ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    rowKeys.forEach { key ->
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.1f))
                                .clickable {
                                    errorMsg = null
                                    when (key) {
                                        "C" -> {
                                            if (!stepConfirm) {
                                                if (pinValue.isNotEmpty()) pinValue = pinValue.dropLast(1)
                                            } else {
                                                if (confirmValue.isNotEmpty()) confirmValue = confirmValue.dropLast(1)
                                            }
                                        }

                                        "OK" -> {
                                            if (!stepConfirm) {
                                                if (pinValue.length == 4) {
                                                    stepConfirm = true
                                                } else {
                                                    errorMsg = "PIN kod 4 ta raqamdan iborat bo'lishi kerak!"
                                                }
                                            } else {
                                                if (confirmValue == pinValue) {
                                                    viewModel.setupPin(pinValue)
                                                } else {
                                                    errorMsg = "PIN kodlar mos kelmadi, boshidan urinib ko'ring!"
                                                    pinValue = ""
                                                    confirmValue = ""
                                                    stepConfirm = false
                                                }
                                            }
                                        }

                                        else -> {
                                            if (!stepConfirm) {
                                                if (pinValue.length < 4) {
                                                    pinValue += key
                                                    if (pinValue.length == 4 && key != "OK") {
                                                        // Automatically request confirm step
                                                        stepConfirm = true
                                                    }
                                                }
                                            } else {
                                                if (confirmValue.length < 4) {
                                                    confirmValue += key
                                                    if (confirmValue.length == 4 && key != "OK") {
                                                        if (confirmValue == pinValue) {
                                                            viewModel.setupPin(pinValue)
                                                        } else {
                                                            errorMsg = "PIN kodlar mos kelmadi!"
                                                            pinValue = ""
                                                            confirmValue = ""
                                                            stepConfirm = false
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = key,
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

// 5. PARENTAL MASTER CONTROL DASHBOARD
@Composable
fun ParentalDashboardScreen(viewModel: LauncherViewModel) {
    val isParentalMode by viewModel.isParentalModeEnabled.collectAsState()
    val restrictions by viewModel.appListCombined.collectAsState()
    val logs by viewModel.logsList.collectAsState()
    val context = LocalContext.current

    var selectedTab by remember { mutableStateOf(0) } // 0 = App list, 1 = Activity Logs, 2 = Pin Config
    val activeAppToLimit by viewModel.activeAppToLimit.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        // Core dashboard header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { viewModel.navigateTo(LauncherScreen.Home) },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.1f))
                ) {
                    Icon(Icons.Default.Home, contentDescription = "Home desk", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Ota-ona Boshqaruvi",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Simple Master mode toggle
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Nazorat",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Switch(
                    checked = isParentalMode,
                    onCheckedChange = { viewModel.toggleParentalMode(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = SafeEmerald,
                        checkedTrackColor = SafeEmerald.copy(alpha = 0.4f),
                        uncheckedThumbColor = Color.White.copy(alpha = 0.5f),
                        uncheckedTrackColor = Color.White.copy(alpha = 0.1f)
                    ),
                    modifier = Modifier.testTag("parental_master_switch")
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Navigation tab chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val tabs = listOf("Check List", "Nova X", "Tizim Jurnali", "PIN Sozlama")
            tabs.forEachIndexed { idx, title ->
                val selected = selectedTab == idx
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selected) VividAmethyst else Color.White.copy(alpha = 0.08f))
                        .clickable { selectedTab = idx }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        color = if (selected) Color.White else Color.White.copy(alpha = 0.7f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Main Tab Content Area
        Box(modifier = Modifier.weight(1f)) {
            when (selectedTab) {
                0 -> {
                    // TAB 0: APP LIST & INDIVIDUAL LOCKS / LIMITS
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Alert panel if Android Usage Access permission state is denied
                        if (!viewModel.isUsageAccessGranted()) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = SolidCardBg),
                                border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Warning, contentDescription = "Warn", tint = BlockCrimson)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Tashqi ilova vaqtini aniq hisoblash",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Ushbu qurilma orqali bolani real vaqtda avtomatik kuzatish uchun tizim sozlamalaridan Usage Statistics ruxsatini yoqing.",
                                        color = Color.White.copy(alpha = 0.7f),
                                        fontSize = 11.sp
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Button(
                                        onClick = {
                                            try {
                                                context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                                            } catch (e: Exception) {
                                                viewModel.setUiAlert("Tizim sozlamalarini ochib bo'lmadi! Iltimos qo'lda yoqing.")
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = CyberAccent),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.height(36.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                                    ) {
                                        Text("Sozlamalarni ochish", color = Color.White, fontSize = 11.sp)
                                    }
                                }
                            }
                        }

                        // App Limits List
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(restrictions) { app ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            AppIconView(packageName = app.packageName, size = 44.dp)
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Text(
                                                    text = app.label,
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = if (app.limitMinutes == 0) "Cheklovsiz" else "Limit: ${app.limitMinutes} daqiqa",
                                                    color = if (app.limitMinutes > 0) VividAmethyst else Color.White.copy(alpha = 0.5f),
                                                    fontSize = 11.sp
                                                )
                                                if (app.usedMinutesToday > 0) {
                                                    Text(
                                                        text = "Bugun ishlatildi: ${app.usedMinutesToday}m",
                                                        color = SafeEmerald,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                }
                                            }
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            // Configuration limiting button
                                            IconButton(
                                                onClick = { viewModel.selectAppToLimit(app) },
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(CircleShape)
                                                    .background(Color.White.copy(alpha = 0.08f))
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Add,
                                                    contentDescription = "Set limit",
                                                    tint = Color.White
                                                )
                                            }

                                            Spacer(modifier = Modifier.width(8.dp))

                                            // Quick toggle block package status
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Switch(
                                                    checked = app.isBlocked,
                                                    onCheckedChange = { viewModel.updateAppLock(app.packageName, app.label, it) },
                                                    colors = SwitchDefaults.colors(
                                                        checkedThumbColor = BlockCrimson,
                                                        checkedTrackColor = BlockCrimson.copy(alpha = 0.4f),
                                                        uncheckedThumbColor = Color.White.copy(alpha = 0.3f),
                                                        uncheckedTrackColor = Color.White.copy(alpha = 0.05f)
                                                    ),
                                                    modifier = Modifier.testTag("lock_switch_${app.packageName}")
                                                )
                                                Text(
                                                    text = if (app.isBlocked) "Yopiq" else "Ochiq",
                                                    color = if (app.isBlocked) BlockCrimson else Color.White.copy(alpha = 0.5f),
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // TAB 1: NOVA X SETTINGS
                    NovaXScreen(viewModel, isTabMode = true)
                }

                2 -> {
                    // TAB 2: DEVICE USAGE LOGS AND JOURNAL
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Qurilmadagi so'nggi harakatlar", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                            IconButton(onClick = { viewModel.clearAllLogs() }) {
                                Icon(Icons.Default.Delete, contentDescription = "Clear logs", tint = BlockCrimson)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (logs.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Jurnallar bo'sh", color = Color.White.copy(alpha = 0.5f), fontSize = 14.sp)
                            }
                        } else {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(logs) { log ->
                                    val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(log.timestamp))
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.04f)),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(log.appName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                Text(
                                                    text = when (log.action) {
                                                        "LAUNCHED" -> "Ilova ishga tushirildi"
                                                        "BLOCKED_ATTEMPT" -> "Taqiqlangan ilovaga ruxsat so'raldi"
                                                        "LIMIT_EXCEEDED_ATTEMPT" -> "Limit tugagan ilovaga ruxsat so'raldi"
                                                        "RESTRICTION_BLOCKED" -> "Blok ro’yxatiga qo'shildi"
                                                        "RESTRICTION_UNBLOCKED" -> "Ruxsat etilgan ilova"
                                                        "PARENTAL_MODE_ON" -> "Ota-ona nazorati faollashtirildi"
                                                        "PARENTAL_MODE_OFF" -> "Nazorat faolsizlantirildi"
                                                        else -> log.action
                                                    },
                                                    color = when (log.action) {
                                                        "BLOCKED_ATTEMPT", "LIMIT_EXCEEDED_ATTEMPT" -> BlockCrimson
                                                        "LAUNCHED" -> SafeEmerald
                                                        else -> Color.White.copy(alpha = 0.7f)
                                                    },
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                            Text(time, color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                3 -> {
                    // TAB 3: PARENTAL SETTINGS AND PIN RECONFIGURATION
                    var showChangePin by remember { mutableStateOf(false) }
                    var newPinInput by remember { mutableStateOf("") }
                    val currentPin by viewModel.storedPin.collectAsState()

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Ota-ona xavfsizligi", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Ota-ona PIN kodi bolalaringiz ushbu nazorat dasturi sozlamalarini mustaqil ravishda o'zgartira olmasliklarini taminlaydi.",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 12.sp
                                )
                            }
                        }

                        // Launcher Mode Section
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text("Launcher Ishlash Rejimi", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text(
                                    "Foydalanish qulayligi uchun ota-ona rejimlarida mos tushadigan dizaynni yoki telefon ekraniga moslashtirish rejimini tanlang.",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 12.sp
                                )

                                val activeMode by viewModel.launcherLayoutMode.collectAsState()
                                val modes = listOf(
                                    Triple("horizontal_pager", "Gorizontal Sahifalar (Tavsiya!)", "Ilovalarni varaqlash uchun o'ngga-chapga suring. Ekrandagi barcha ilovalarni yonga harakatlantiradi."),
                                    Triple("carousel_row", "Gorizontal Karusel Slider", "Ilovalar bir qatorda yonga chiroyli aylanma tarzda joylashadi."),
                                    Triple("niagara_list", "A-Z Alfavitli Ro'yxat", "Niagara minimalistik uslubidagi tartiblangan ro'yxat va yon alfavit paneli."),
                                    Triple("dense_grid", "Zichlashtirilgan Kichik Setka", "5 ta ustunli kichikroq piktogrammaga ega ixcham o'lchamli dizayn."),
                                    Triple("auto", "Moslashuvchan Avtomatik", "Qurilma o'lchamiga qarab ustunlarni avtomatik tanlaydi."),
                                    Triple("classic", "Klassik Standart Setka", "Barcha telefonlarda barqaror 4 ta ustunli qulay an'anaviy dizayn."),
                                    Triple("simple", "Soddalashtirilgan (Katta)", "Kattalashtirilgan piktogrammalar bilan 3 ta ustunli juda sodda dizayn.")
                                )

                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    modes.forEach { (modeKey, title, desc) ->
                                        val isCurrent = activeMode == modeKey
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(if (isCurrent) VividAmethyst.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.03f))
                                                .border(
                                                    1.dp,
                                                    if (isCurrent) VividAmethyst else Color.Transparent,
                                                    RoundedCornerShape(12.dp)
                                                )
                                                .clickable { viewModel.setLauncherLayoutMode(modeKey) }
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            RadioButton(
                                                selected = isCurrent,
                                                onClick = { viewModel.setLauncherLayoutMode(modeKey) },
                                                colors = androidx.compose.material3.RadioButtonDefaults.colors(
                                                    selectedColor = VividAmethyst,
                                                    unselectedColor = Color.White.copy(alpha = 0.5f)
                                                )
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(title, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                                Text(desc, color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (!showChangePin) {
                            Button(
                                onClick = { showChangePin = true },
                                colors = ButtonDefaults.buttonColors(containerColor = VividAmethyst),
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = "Change")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("PIN kodni yangilash", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        } else {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text("Yangi PIN Kod Kiriting", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    OutlinedTextField(
                                        value = newPinInput,
                                        onValueChange = {
                                            if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                                                newPinInput = it
                                            }
                                        },
                                        placeholder = { Text("M-n: 1234", color = Color.White.copy(alpha = 0.4f)) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                        visualTransformation = PasswordVisualTransformation(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            focusedBorderColor = VividAmethyst
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        OutlinedButton(
                                            onClick = {
                                                showChangePin = false
                                                newPinInput = ""
                                            },
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                                        ) {
                                            Text("Bekor qilish")
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Button(
                                            onClick = {
                                                if (newPinInput.length == 4) {
                                                    viewModel.setupPin(newPinInput)
                                                    showChangePin = false
                                                    newPinInput = ""
                                                } else {
                                                    viewModel.setUiAlert("PIN kod 4 ta raqam bo'lishi shart!")
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = SafeEmerald)
                                        ) {
                                            Text("Saqlash")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Interactive slider inside a parent overlay dialog to configure app minutes limits
    val sliderApp = activeAppToLimit
    if (sliderApp != null) {
        var sliderValue by remember(sliderApp.packageName) { mutableStateOf(sliderApp.limitMinutes.toFloat()) }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
                .clickable { viewModel.selectAppToLimit(null) },
            contentAlignment = Alignment.Center
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SolidCardBg),
                elevation = CardDefaults.cardElevation(16.dp),
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .clickable(enabled = false) {}
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    AppIconView(packageName = sliderApp.packageName, size = 52.dp)
                    Text(
                        text = "${sliderApp.label} cheklovi",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = if (sliderValue.toInt() == 0) "Kunlik foydalanish cheklanmagan" else "Kunlik limit: ${sliderValue.toInt()} daqiqa",
                        color = if (sliderValue > 0) VividAmethyst else Color.White.copy(alpha = 0.7f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Slider(
                        value = sliderValue,
                        onValueChange = { sliderValue = it },
                        valueRange = 0f..180f,
                        steps = 11, // 0, 15, 30, 45, 60, 75, 90, 105, 120, 135, 150, 165, 180
                        colors = SliderDefaults.colors(
                            activeTrackColor = VividAmethyst,
                            activeTickColor = Color.White,
                            thumbColor = VividAmethyst
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.selectAppToLimit(null) },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Bekor qilish")
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Button(
                            onClick = { viewModel.updateAppLimit(sliderApp.packageName, sliderApp.label, sliderValue.toInt()) },
                            colors = ButtonDefaults.buttonColors(containerColor = SafeEmerald),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Tasdiqlash")
                        }
                    }
                }
            }
        }
    }
}

// 6. BLOCKED SCREEN OVERLAY (Activated when limits are broken or app is locked)
@Composable
fun BlockedOverlayScreen(app: AppItemUi, viewModel: LauncherViewModel) {
    val context = LocalContext.current
    var isVerifyingPin by remember { mutableStateOf(false) }
    var inputPin by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }
    val storedPin by viewModel.storedPin.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CosmicDarkBg)
            .statusBarsPadding()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (!isVerifyingPin) {
                // Warning panel mode
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(BlockCrimson.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Warning Lock",
                        tint = BlockCrimson,
                        modifier = Modifier.size(44.dp)
                    )
                }

                Text(
                    text = "Ilova Taqiqlangan!",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                // App display Info card
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AppIconView(packageName = app.packageName, size = 48.dp)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(app.label, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(
                                text = if (app.isBlocked) "Ushbu dastur ota-onangiz tomonidan butunlay bloklangan!"
                                else "Ushbu ilovaning kunlik limiti (${app.limitMinutes}m) bugun uchun tugagan!",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Options
                Button(
                    onClick = {
                        viewModel.closeBlockOverlay()
                        viewModel.navigateTo(LauncherScreen.Home)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberAccent),
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Bosh ekranga qaytish", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                OutlinedButton(
                    onClick = { isVerifyingPin = true },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Lock, contentDescription = "Password unlock")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ota-ona PIN kodi bilan ochish", fontSize = 13.sp)
                }
            } else {
                // PIN Validation keypad mode (Touch target size compliant)
                Text(
                    text = "Ota-ona PIN kodini kiriting",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 1..4) {
                        val active = i <= inputPin.length
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(
                                    if (pinError) BlockCrimson
                                    else if (active) VividAmethyst
                                    else Color.White.copy(alpha = 0.2f)
                                )
                        )
                    }
                }

                if (pinError) {
                    Text("PIN kod xato! Qayta urinib ko'ring.", color = BlockCrimson, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(0.85f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val keys = listOf(
                        listOf("1", "2", "3"),
                        listOf("4", "5", "6"),
                        listOf("7", "8", "9"),
                        listOf("C", "0", "OK")
                    )

                    keys.forEach { rowKeys ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            rowKeys.forEach { key ->
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.1f))
                                        .clickable {
                                            pinError = false
                                            when (key) {
                                                "C" -> {
                                                    if (inputPin.isNotEmpty()) inputPin = inputPin.dropLast(1)
                                                }

                                                "OK" -> {
                                                    if (inputPin == storedPin) {
                                                        // Temporarily unlocks this app session (reset daily log metadata row or temporary bypass)
                                                        viewModel.closeBlockOverlay()
                                                        // Actually launch the application bypassed

                                                        try {
                                                            val intent = context.packageManager.getLaunchIntentForPackage(app.packageName)
                                                            if (intent != null) {
                                                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                                context.startActivity(intent)
                                                            }
                                                        } catch (e: Exception) {
                                                            // handled
                                                        }
                                                    } else {
                                                        pinError = true
                                                        inputPin = ""
                                                    }
                                                }

                                                else -> {
                                                    if (inputPin.length < 4) {
                                                        inputPin += key
                                                        if (inputPin.length == 4) {
                                                            if (inputPin == storedPin) {
                                                                viewModel.closeBlockOverlay()
                                                                try {
                                                                    val intent = context.packageManager.getLaunchIntentForPackage(app.packageName)
                                                                    if (intent != null) {
                                                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                                        context.startActivity(intent)
                                                                    }
                                                                } catch (e: Exception) {
                                                                    // handled
                                                                }
                                                            } else {
                                                                pinError = true
                                                                inputPin = ""
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(key, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = {
                        isVerifyingPin = false
                        inputPin = ""
                        pinError = false
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    modifier = Modifier.fillMaxWidth(0.5f)
                ) {
                    Text("Orqaga")
                }
            }
        }
    }
}

@Composable
fun NovaXScreen(viewModel: LauncherViewModel, isTabMode: Boolean = false) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val currentThemeStyle by viewModel.novaXThemeStyle.collectAsState()
    val webWallpaperUrl by viewModel.novaXWebWallpaperUrl.collectAsState()
    val iconTintName by viewModel.novaXIconTint.collectAsState()
    val columnGridCount by viewModel.novaXColumnGridCount.collectAsState()
    
    val installedApps by viewModel.appListCombined.collectAsState()
    val customAppLabels by viewModel.customAppLabels.collectAsState()

    var customUrlInput by remember { mutableStateOf(webWallpaperUrl) }
    var selectedAppToRename by remember { mutableStateOf<AppItemUi?>(null) }
    var newLabelInput by remember { mutableStateOf("") }
    
    var wallpaperStatusMessage by remember { mutableStateOf("") }

    val glassCardModifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(24.dp))
        .background(Color.White.copy(alpha = 0.08f))
        .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(24.dp))
        .padding(18.dp)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .then(if (isTabMode) Modifier else Modifier.systemBarsPadding())
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = if (isTabMode) 0.dp else 16.dp)
        ) {
            if (!isTabMode) {
                // Header Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.navigateTo(LauncherScreen.Home) },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.12f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Orqaga",
                            tint = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = "Nova X Launcher Studio",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Smartfoningizni eksklyuziv personalizatsiya qiling",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Scrollable Content
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                // Section 1: The themed menus
                item {
                    Column(modifier = glassCardModifier) {
                        Text(
                            text = "1. Dizayn Mavzulari (5 xil Menyular)",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Ilovangizning asosiy uslubini o'zgartiring. Har bir dizayn elementlari maxsus moslashtirilgan.",
                            color = Color.White.copy(alpha = 0.65f),
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        val themesList = listOf(
                            Triple("glass_shaffof", "Glassmorphism Shaffof", "Yarim-shaffof oyna effekti, yorqin neon burchaklar (Mavzu 1)"),
                            Triple("neon_cyber", "Cyberpunk Neon Glow", "Pushti va ko'k yorug'liklar, kiber-kataklar dizayni (Mavzu 2)"),
                            Triple("oled_dark", "Minimalist OLED Black", "To'liq qora fon, minimal oq chiziqlar va toza shrift (Mavzu 3)"),
                            Triple("pixel_8bit", "Retro Classic Pixel", "Sariq neon retro o'yin ekrani, pikselli kvadrat chiziqlar (Mavzu 4)"),
                            Triple("cosmic_aura", "Modern Cosmic Aura", "Binafsharang aura bulutlar, dumaloq organik shakllar (Mavzu 5)")
                        )

                        themesList.forEach { (styleKey, titleName, desc) ->
                            val isSelected = currentThemeStyle == styleKey
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        if (isSelected) Color.White.copy(alpha = 0.16f) else Color.Transparent
                                    )
                                    .clickable {
                                        viewModel.setNovaXThemeStyle(styleKey)
                                        entityManagerSoundEffect(context)
                                    }
                                    .border(
                                        width = if (isSelected) 1.5.dp else 0.dp,
                                        color = if (isSelected) Color(0xFF00E5FF) else Color.Transparent,
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isSelected) Color(0xFF00E5FF) else Color.White.copy(alpha = 0.2f)
                                        )
                                )
                                Spacer(modifier = Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = titleName,
                                        color = if (isSelected) Color(0xFF00E5FF) else Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = desc,
                                        color = Color.White.copy(alpha = 0.5f),
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // Section 2: Online wallies
                item {
                    Column(modifier = glassCardModifier) {
                        Text(
                            text = "2. Google & Premium Web Fon Tasvirlari",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Istagan Google rasm havolasini kiriting yoki pastdagi premium saralangan rasmlardan tanlang",
                            color = Color.White.copy(alpha = 0.65f),
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        // Custom URL textfield
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            androidx.compose.material3.TextField(
                                value = customUrlInput,
                                onValueChange = { customUrlInput = it },
                                placeholder = { Text("https://url_rasm_manzili.jpg", fontSize = 12.sp, color = Color.White.copy(alpha = 0.4f)) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                colors = androidx.compose.material3.TextFieldDefaults.colors(
                                    focusedContainerColor = Color.White.copy(alpha = 0.08f),
                                    unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                ),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    viewModel.setNovaXWebWallpaperUrl(customUrlInput)
                                    entityManagerSoundEffect(context)
                                    if (customUrlInput.isNotEmpty()) {
                                        wallpaperStatusMessage = "Web rasm muvaffaqiyatli o'rnatildi!"
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE040FB)),
                                modifier = Modifier.height(52.dp)
                            ) {
                                Text("O'rnatish", fontSize = 12.sp, color = Color.White)
                            }
                        }

                        // Web Wallpaper presets
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Yoki saralangan premium internet rasmlari:", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(8.dp))

                        val presets = listOf(
                            Quadruple("https://images.unsplash.com/photo-1507525428034-b723cf961d3e?q=80&w=400", "Okean Sohili", "Oltin quyosh to'lqini", "sunset"),
                            Quadruple("https://images.unsplash.com/photo-1515621061946-eff1c2a352bd?q=80&w=400", "Cyber Tokyo", "Neonli kiber tunda shahar", "neon"),
                            Quadruple("https://images.unsplash.com/photo-1462331940025-496dfbfc7564?q=80&w=400", "Koinot Tubsizligi", "Galaktika va rangdor tuman", "galaxy"),
                            Quadruple("https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?q=80&w=400", "Yashil Tog'lar", "Tumanli tabiat go'zalligi", "mountain")
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            presets.forEach { (url, label, subtitle, labelKey) ->
                                val isSelected = webWallpaperUrl == url
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(90.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(Color.DarkGray)
                                        .border(
                                            width = if (isSelected) 2.dp else 0.dp,
                                            color = if (isSelected) Color(0xFF00E5FF) else Color.Transparent,
                                            shape = RoundedCornerShape(14.dp)
                                        )
                                        .clickable {
                                            customUrlInput = url
                                            viewModel.setNovaXWebWallpaperUrl(url)
                                            entityManagerSoundEffect(context)
                                            wallpaperStatusMessage = "'$label' foni faol!"
                                        }
                                ) {
                                    coil.compose.AsyncImage(
                                        model = url,
                                        contentDescription = label,
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                Brush.verticalGradient(
                                                    listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f))
                                                )
                                            )
                                    )
                                    Text(
                                        text = label,
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier
                                            .align(Alignment.BottomCenter)
                                            .padding(bottom = 6.dp)
                                    )
                                }
                            }
                        }

                        if (webWallpaperUrl.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    customUrlInput = ""
                                    viewModel.setNovaXWebWallpaperUrl("")
                                    wallpaperStatusMessage = "Asl rangli fon tiklandi"
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.15f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Web fonni tozalash va gradient fonni qaytarish", color = Color.White, fontSize = 12.sp)
                            }
                        }

                        if (wallpaperStatusMessage.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(wallpaperStatusMessage, color = Color(0xFF00E5FF), fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterHorizontally))
                        }
                    }
                }

                // Section 3: Custom app renamer
                item {
                    Column(modifier = glassCardModifier) {
                        Text(
                            text = "3. Ilovani Qayta Nomlash (Custom App Renamer)",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Istagan ilovangizni o'zingiz hohlagan nomga o'zgartiring (masalan: 'Telegram' ni 'Maxfiy Xabar' qiling)",
                            color = Color.White.copy(alpha = 0.65f),
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        if (selectedAppToRename == null) {
                            Text("Nomini o'zgartirish uchun pastdagi ro'yxatdan ilova tanlang:", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Scrollable inline app grid selector
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White.copy(alpha = 0.04f))
                                    .padding(8.dp)
                            ) {
                                androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                                    columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(4),
                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    items(installedApps.size) { index ->
                                        val app = installedApps[index]
                                        val hasCustomLabel = customAppLabels.containsKey(app.packageName)
                                        Column(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (hasCustomLabel) Color(0xFFE040FB).copy(alpha = 0.2f) else Color.Transparent)
                                                .clickable {
                                                    selectedAppToRename = app
                                                    newLabelInput = app.label
                                                }
                                                .padding(6.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            AppIconView(packageName = app.packageName, size = 32.dp, iconTintName = iconTintName)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = app.label,
                                                color = Color.White,
                                                fontSize = 9.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            val app = selectedAppToRename!!
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White.copy(alpha = 0.08f))
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AppIconView(packageName = app.packageName, size = 40.dp, iconTintName = iconTintName)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Tanlangan ilova:", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
                                    Text(app.label, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    Text(app.packageName, color = Color.White.copy(alpha = 0.3f), fontSize = 9.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .clickable { selectedAppToRename = null }
                                        .padding(6.dp)
                                ) {
                                    Icon(androidx.compose.material.icons.Icons.Default.Close, contentDescription = "Yopish", tint = Color.LightGray)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                androidx.compose.material3.TextField(
                                    value = newLabelInput,
                                    onValueChange = { newLabelInput = it },
                                    placeholder = { Text("Yangi nom kiring...", color = Color.White.copy(alpha = 0.4f), fontSize = 12.sp) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(52.dp)
                                        .clip(RoundedCornerShape(12.dp)),
                                    colors = androidx.compose.material3.TextFieldDefaults.colors(
                                        focusedContainerColor = Color.White.copy(alpha = 0.1f),
                                        unfocusedContainerColor = Color.White.copy(alpha = 0.06f),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent
                                    ),
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = {
                                        viewModel.setCustomAppLabel(app.packageName, newLabelInput)
                                        entityManagerSoundEffect(context)
                                        selectedAppToRename = null
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                                    modifier = Modifier.height(52.dp)
                                ) {
                                    Text("Saqlash", fontSize = 12.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                                }
                            }

                            if (customAppLabels.containsKey(app.packageName)) {
                                Spacer(modifier = Modifier.height(8.dp))
                                androidx.compose.material3.TextButton(
                                    onClick = {
                                        viewModel.setCustomAppLabel(app.packageName, "")
                                        entityManagerSoundEffect(context)
                                        selectedAppToRename = null
                                    }
                                ) {
                                    Text("Asl holatiga (original nomiga) qaytarish", color = Color(0xFFEF4444), fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }

                // Section 4: Icon tints
                item {
                    Column(modifier = glassCardModifier) {
                        Text(
                            text = "4. Belgilar Ranglari (Icon Tints)",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Tanlangan rang orqali telefoningizdagi barcha ilova piktogrammalarini bir xil rang sxemasiga bo'yang.",
                            color = Color.White.copy(alpha = 0.65f),
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        val tints = listOf(
                            Pair("none", "Hech qanday (Original)"),
                            Pair("golden_glow", "Amber Oltin"),
                            Pair("cyan_neon", "Neon Kiber Ko'k"),
                            Pair("emerald_green", "Zumrad Yashil"),
                            Pair("rose_petal", "Guldor Pushti"),
                            Pair("cyber_punk_pink", "To'q Binafsha / Magenta")
                        )

                        tints.forEach { (tintKey, label) ->
                            val isSelected = iconTintName == tintKey
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) Color.White.copy(alpha = 0.1f) else Color.Transparent)
                                    .clickable {
                                        viewModel.setNovaXIconTint(tintKey)
                                        entityManagerSoundEffect(context)
                                    }
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when (tintKey) {
                                                "none" -> Color.LightGray
                                                "golden_glow" -> Color(255, 215, 0)
                                                "cyan_neon" -> Color(0, 255, 255)
                                                "emerald_green" -> Color(16, 185, 129)
                                                "rose_petal" -> Color(244, 114, 182)
                                                "cyber_punk_pink" -> Color(255, 0, 128)
                                                else -> Color.Gray
                                            }
                                        )
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = label,
                                    color = if (isSelected) Color(0xFF00E5FF) else Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                // Section 5: Dynamic columns slider
                item {
                    Column(modifier = glassCardModifier) {
                        Text(
                            text = "5. Ish stoli To'ri (Desktop Columns Grid Layout)",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Home screen dagi piktogrammalar ustunlar sonini kengaytiring (kichik yoki katta qilish)",
                            color = Color.White.copy(alpha = 0.65f),
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            listOf(3, 4, 5, 6).forEach { cols ->
                                val isSelected = columnGridCount == cols
                                Button(
                                    onClick = {
                                        viewModel.setNovaXColumnGridCount(cols)
                                        entityManagerSoundEffect(context)
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isSelected) Color(0xFF00E5FF) else Color.White.copy(alpha = 0.1f),
                                        contentColor = if (isSelected) Color.Black else Color.White
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 4.dp)
                                ) {
                                    Text("${cols} ustun", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Special generic data class wrapper
data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)

private fun entityManagerSoundEffect(context: Context) {
    try {
        val soundUri = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION)
        val ringtone = android.media.RingtoneManager.getRingtone(context, soundUri)
        ringtone?.play()
    } catch (e: Exception) {
        // Safe play
    }
}
