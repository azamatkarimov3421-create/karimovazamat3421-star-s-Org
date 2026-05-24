package com.example.ui

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
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
fun SafeLauncherRootUi(viewModel: LauncherViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val uiAlert by viewModel.uiAlert.collectAsState()
    val blockedAppOverlay by viewModel.blockedAppOverlay.collectAsState()

    // Base cosmic atmospheric gradient
    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF231B41), CosmicDarkBg),
                        center = Offset(size.width * 0.7f, size.height * 0.2f),
                        radius = size.maxDimension * 0.8f
                    )
                )
            }
    ) {
        // Render screens inside our Single-Screen setup flow
        when (val screen = currentScreen) {
            is LauncherScreen.Home -> HomeScreen(viewModel)
            is LauncherScreen.Drawer -> AllAppsDrawerScreen(viewModel)
            is LauncherScreen.EnterPin -> EnterPinScreen(viewModel, screen.nextScreen)
            is LauncherScreen.SetupPin -> SetupPinScreen(viewModel)
            is LauncherScreen.ParentalDashboard -> ParentalDashboardScreen(viewModel)
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
                colors = CardDefaults.cardColors(containerColor = VividAmethyst),
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

// 1. HOME SCREEN / THE DESKTOP
@Composable
fun HomeScreen(viewModel: LauncherViewModel) {
    val context = LocalContext.current
    val apps by viewModel.allowedHomeApps.collectAsState()
    val isParentalMode by viewModel.isParentalModeEnabled.collectAsState()
    val storedPin by viewModel.storedPin.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(top = 16.dp, bottom = 48.dp),
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
                    .background(Color.White.copy(alpha = 0.1f))
                    .testTag("parental_settings_button")
            ) {
                Icon(Icons.Default.Settings, contentDescription = "Ota-ona xonasi", tint = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Large Digital Clock & Date
        ClockWidget()

        Spacer(modifier = Modifier.height(40.dp))

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
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(apps) { app ->
                    Column(
                        modifier = Modifier
                            .testTag("app_${app.packageName}")
                            .clickable { viewModel.selectAndLaunchApp(context, app) }
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AppIconView(packageName = app.packageName, size = 56.dp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = app.label,
                            color = Color.White,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center
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
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Glowing Search Bar (Mimics native search drawer launcher widget)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(Color.White.copy(alpha = 0.12f))
                .clickable { viewModel.navigateTo(LauncherScreen.Drawer) }
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.White.copy(alpha = 0.7f))
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Barcha ilovalarni qidirish...",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 14.sp
                )
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp)
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
                    focusedBorderColor = VividAmethyst,
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
                columns = GridCells.Fixed(4),
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(apps) { app ->
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
                            AppIconView(packageName = app.packageName, size = 52.dp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = app.label,
                                color = Color.White,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center
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
            Icon(Icons.Default.Lock, contentDescription = "Lock", tint = VividAmethyst, modifier = Modifier.size(64.dp))
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
                                else if (active) VividAmethyst
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
            Icon(Icons.Default.Lock, contentDescription = "PIN secure", tint = VividAmethyst, modifier = Modifier.size(64.dp))
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
                                if (active) VividAmethyst else Color.White.copy(alpha = 0.2f)
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
            val tabs = listOf("Check List", "Tizim Jurnali", "PIN Sozlama")
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
                    // TAB 1: DEVICE USAGE LOGS AND JOURNAL
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

                2 -> {
                    // TAB 2: PARENTAL SETTINGS AND PIN RECONFIGURATION
                    var showChangePin by remember { mutableStateOf(false) }
                    var newPinInput by remember { mutableStateOf("") }
                    val currentPin by viewModel.storedPin.collectAsState()

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp),
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
