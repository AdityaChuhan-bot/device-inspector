@file:Suppress("DEPRECATION")
package com.example.device

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.util.Locale

// --- Color Palette (High Density Theme) ---
object InspectorTheme {
    var isDark by mutableStateOf(true) // Start in dark mode by default (fits CPU X styles)

    val DarkBg: Color @Composable get() = if (isDark) Color(0xFF0B0E14) else Color(0xFFF3F4F9)      // Sleek dark command center vs light high-density background
    val PanelBg: Color @Composable get() = if (isDark) Color(0xFF131924) else Color(0xFFFFFFFF)     // High-density panel cards
    val DividerBg: Color @Composable get() = if (isDark) Color(0xFF222B3D) else Color(0xFFE0E2EC)   // Outline borders
    
    val NeonCyan: Color @Composable get() = if (isDark) Color(0xFF00E5FF) else Color(0xFF6750A4)    // Purple vs Cyan accent
    val NeonBlue: Color @Composable get() = if (isDark) Color(0xFF007FFF) else Color(0xFF21005D)    // Dark contrast indigo/blue
    val NeonTeal: Color @Composable get() = if (isDark) Color(0xFF00FFA1) else Color(0xFF1D6D3F)    // Standard optimal green
    val WarmCoral: Color @Composable get() = if (isDark) Color(0xFFFF4B61) else Color(0xFFB3261E)   // Critical load scarlet
    val AmberGlow: Color @Composable get() = if (isDark) Color(0xFFFFB300) else Color(0xFFE65100)   // Bright warning orange
    
    val TextWhite: Color @Composable get() = if (isDark) Color(0xFFF1F5F9) else Color(0xFF1B1B1F)   // Primary contrast text
    val TextMuted: Color @Composable get() = if (isDark) Color(0xFF94A3B8) else Color(0xFF44474E)   // Slate gray sub-labels
    val TextDark: Color @Composable get() = if (isDark) Color(0xFF475569) else Color(0xFF74777F)    // Deep disabled/accent gray
}

@Composable
fun ThemeToggleIcon(isDark: Boolean, tint: Color, modifier: Modifier = Modifier) {
    if (isDark) {
        Canvas(modifier = modifier) {
            // Smooth custom path rendering for crescent moon
            drawCircle(
                color = tint,
                radius = size.minDimension / 2.2f,
                center = center
            )
            drawCircle(
                color = Color.Transparent,
                radius = size.minDimension / 2.3f,
                center = center.copy(x = center.x - size.minDimension / 4f, y = center.y - size.minDimension / 4f),
                blendMode = androidx.compose.ui.graphics.BlendMode.Clear
            )
        }
    } else {
        // Aesthetic custom rendering for shiny sun
        Canvas(modifier = modifier) {
            drawCircle(
                color = tint,
                radius = size.minDimension / 4.4f,
                center = center
            )
            val rayLength = size.minDimension / 2f
            val numRays = 8
            for (i in 0 until numRays) {
                val angle = (i * 2 * Math.PI / numRays).toFloat()
                val startX = center.x + (size.minDimension / 3.4f) * kotlin.math.cos(angle)
                val startY = center.y + (size.minDimension / 3.4f) * kotlin.math.sin(angle)
                val endX = center.x + (size.minDimension / 2.2f) * kotlin.math.cos(angle)
                val endY = center.y + (size.minDimension / 2.2f) * kotlin.math.sin(angle)
                
                drawLine(
                    color = tint,
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }
    }
}

@Composable
fun GridBackground(modifier: Modifier = Modifier) {
    val isDark = InspectorTheme.isDark
    val gridColor = if (isDark) Color(0xFF131B2A).copy(alpha = 0.5f) else Color(0xFFE2E8F0).copy(alpha = 0.6f)
    val dotColor = if (isDark) Color(0xFF00E5FF).copy(alpha = 0.08f) else Color(0xFF6750A4).copy(alpha = 0.05f)

    val infiniteTransition = rememberInfiniteTransition(label = "gridSweep")
    val sweepProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweepProgress"
    )

    val neonColor = if (isDark) Color(0xFF00E5FF) else Color(0xFF6750A4)

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val stepX = 24.dp.toPx()
        val stepY = 24.dp.toPx()

        // Vertical grid lines
        var x = 0f
        while (x < width) {
            drawLine(
                color = gridColor,
                start = Offset(x, 0f),
                end = Offset(x, height),
                strokeWidth = 0.5.dp.toPx()
            )
            x += stepX
        }

        // Horizontal grid lines
        var y = 0f
        while (y < height) {
            drawLine(
                color = gridColor,
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 0.5.dp.toPx()
            )
            y += stepY
        }

        // Accent dots at intersections
        x = 0f
        while (x < width) {
            y = 0f
            while (y < height) {
                drawCircle(
                    color = dotColor,
                    radius = 2.dp.toPx(),
                    center = Offset(x, y)
                )
                y += stepY
            }
            x += stepX
        }

        // Dynamic futuristic laser scanline vertical sweep
        val sweepY = sweepProgress * height
        drawLine(
            color = neonColor.copy(alpha = 0.18f),
            start = Offset(0f, sweepY),
            end = Offset(width, sweepY),
            strokeWidth = 1.5.dp.toPx()
        )

        // Trailing gradient laser bloom
        val trailHeight = 150f
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    neonColor.copy(alpha = 0.05f),
                    Color.Transparent
                ),
                startY = (sweepY - trailHeight).coerceAtLeast(0f),
                endY = sweepY
            ),
            topLeft = Offset(0f, (sweepY - trailHeight).coerceAtLeast(0f)),
            size = androidx.compose.ui.geometry.Size(width, trailHeight.coerceAtMost(sweepY))
        )
    }
}

@Composable
fun TelemetryActiveDot(color: Color, modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )
    
    Canvas(modifier = modifier.size(10.dp)) {
        drawCircle(color = color.copy(alpha = alpha * 0.35f), radius = size.minDimension / 2f)
        drawCircle(color = color, radius = size.minDimension / 4.4f)
    }
}

@Composable
fun RightChevron(tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val path = androidx.compose.ui.graphics.Path().apply {
            moveTo(w * 0.35f, h * 0.25f)
            lineTo(w * 0.65f, h * 0.5f)
            lineTo(w * 0.35f, h * 0.75f)
        }
        drawPath(
            path = path,
            color = tint,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Companion.Round)
        )
    }
}


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun DeviceInspectorApp(viewModel: DeviceViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val context = LocalContext.current

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = InspectorTheme.DarkBg
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            GridBackground()
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    if (initialState == "splash") {
                        fadeIn(animationSpec = tween(250)) with fadeOut(animationSpec = tween(150))
                    } else {
                        val isGoBack = targetState == "dashboard" || targetState == "splash"
                        slideInHorizontally(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioLowBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            ),
                            initialOffsetX = { if (isGoBack) -it else it }
                        ) + fadeIn(animationSpec = tween(200)) with slideOutHorizontally(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            ),
                            targetOffsetX = { if (isGoBack) it else -it }
                        ) + fadeOut(animationSpec = tween(150))
                    }
                },
                label = "ScreenTransition"
            ) { targetScreen ->
                when (targetScreen) {
                    "splash" -> SplashScreen()
                    "dashboard" -> DashboardScreen(viewModel)
                    "cpu" -> CpuDetailScreen(viewModel)
                    "ram" -> RamDetailScreen(viewModel)
                    "battery" -> BatteryDetailScreen(viewModel)
                    "storage" -> StorageDetailScreen(viewModel)
                    "network" -> NetworkDetailScreen(viewModel)
                    "sensors" -> SensorsDetailScreen(viewModel)
                    "display" -> DisplayDetailScreen(viewModel)
                    "benchmark" -> BenchmarkDetailScreen(viewModel)
                    "system_test" -> SystemTestScreen(viewModel)
                    else -> DashboardScreen(viewModel)
                }
            }
        }
    }
}

// ================= SPLASH SCREEN =================

@Composable
fun SplashScreen() {
    var animStart by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (animStart) 1f else 0.7f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "LogoScale"
    )
    val opacity by animateFloatAsState(
        targetValue = if (animStart) 1f else 0f,
        animationSpec = tween(1000),
        label = "LogoOpacity"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "splashScanner")
    
    // Smooth angle rotation for sweep arm
    val scanAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scanAngle"
    )

    // Pulse radius fraction
    val pulseFraction by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseOutQuad),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseFraction"
    )

    LaunchedEffect(Unit) {
        animStart = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(InspectorTheme.DarkBg),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            // Animated Radar Scanner Ring
            val cyanColor = InspectorTheme.NeonCyan
            val blueColor = InspectorTheme.NeonBlue
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        alpha = opacity
                    }
                    .drawBehind {
                        val centerOffset = center
                        val maxRadius = 75.dp.toPx()
                        
                        // Reference circles (HUD scope circles)
                        drawCircle(
                            color = blueColor.copy(alpha = 0.15f),
                            radius = maxRadius,
                            style = Stroke(width = 1.dp.toPx())
                        )
                        drawCircle(
                            color = blueColor.copy(alpha = 0.25f),
                            radius = maxRadius * 0.7f,
                            style = Stroke(width = 1.dp.toPx())
                        )
                        drawCircle(
                            color = blueColor.copy(alpha = 0.35f),
                            radius = maxRadius * 0.4f,
                            style = Stroke(width = 1.dp.toPx())
                        )
                        
                        // Expanding active pulse sonar wave
                        drawCircle(
                            color = cyanColor,
                            radius = maxRadius * pulseFraction,
                            alpha = (1f - pulseFraction) * 0.4f,
                            style = Stroke(width = 2.dp.toPx())
                        )
                        
                        // Tactical rotating scanner arm
                        val armAngleRad = Math.toRadians((scanAngle - 90).toDouble())
                        val endX = centerOffset.x + maxRadius * kotlin.math.cos(armAngleRad).toFloat()
                        val endY = centerOffset.y + maxRadius * kotlin.math.sin(armAngleRad).toFloat()
                        drawLine(
                            color = cyanColor.copy(alpha = 0.7f),
                            start = centerOffset,
                            end = Offset(endX, endY),
                            strokeWidth = 2.dp.toPx()
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Inspector Logo",
                    tint = InspectorTheme.NeonCyan,
                    modifier = Modifier.size(64.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "DEVICE INSPECTOR",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = InspectorTheme.TextWhite,
                letterSpacing = 4.sp,
                modifier = Modifier.testTag("splash_title")
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "REAL-TIME DIAGNOSTIC SHIELD",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = InspectorTheme.NeonTeal,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Simulated terminal scan log
            var lText by remember { mutableStateOf("Initializing system telemetry indexes...") }
            LaunchedEffect(Unit) {
                delay(600)
                lText = "Evaluating multicore CPU configurations..."
                delay(600)
                lText = "Registering sensory hardware streams..."
            }

            Text(
                text = "▶ $lText",
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                color = InspectorTheme.TextMuted,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun StaggeredFadeInContainer(
    delayMillis: Int,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(delayMillis.toLong())
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(220, easing = LinearOutSlowInEasing)) + 
                slideInVertically(
                    initialOffsetY = { 20 }, 
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                ) +
                scaleIn(
                    initialScale = 0.95f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                ),
        exit = fadeOut(animationSpec = tween(150))
    ) {
        content()
    }
}

// ================= DASHBOARD SCREEN =================

@Composable
fun DashboardScreen(viewModel: DeviceViewModel) {
    val summary by viewModel.deviceSummary.collectAsState()
    val cpu by viewModel.cpuInfo.collectAsState()
    val ram by viewModel.ramInfo.collectAsState()
    val battery by viewModel.batteryInfo.collectAsState()
    val storage by viewModel.storageInfo.collectAsState()
    val network by viewModel.networkInfo.collectAsState()
    val sensors by viewModel.sensorList.collectAsState()
    val display by viewModel.displayInfo.collectAsState()
    
    val diagState by viewModel.diagnosticState.collectAsState()
    val diagTests by viewModel.diagnosticTests.collectAsState()

    var refreshTrigger by remember { mutableStateOf(0) }
    val rotationAnim by animateFloatAsState(
        targetValue = refreshTrigger * 360f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "refreshRotation"
    )

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(InspectorTheme.DarkBg)
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Device Inspector",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = InspectorTheme.TextWhite
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            TelemetryActiveDot(color = InspectorTheme.NeonTeal)
                            Text(
                                text = "System Status: Optimal • ${summary.deviceName}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = InspectorTheme.TextMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { InspectorTheme.isDark = !InspectorTheme.isDark },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (InspectorTheme.isDark) Color(0xFF312E4A) else Color(0xFFEADDFF))
                                .testTag("dashboard_theme_switcher")
                        ) {
                            ThemeToggleIcon(
                                isDark = InspectorTheme.isDark,
                                tint = if (InspectorTheme.isDark) Color(0xFFD0BCFF) else Color(0xFF21005D),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                refreshTrigger++
                                viewModel.refreshStaticData()
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (InspectorTheme.isDark) Color(0xFF312E4A) else Color(0xFFEADDFF))
                                .testTag("dashboard_refresh_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh System",
                                tint = if (InspectorTheme.isDark) Color(0xFFD0BCFF) else Color(0xFF21005D),
                                modifier = Modifier
                                    .size(20.dp)
                                    .graphicsLayer {
                                        rotationZ = rotationAnim
                                    }
                            )
                        }
                    }
                }
            }
        },
        containerColor = InspectorTheme.DarkBg
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // Overview Device Information Banner
            item {
                val bannerBg = if (InspectorTheme.isDark) Color(0xFF211B35) else Color(0xFFEADDFF)
                val bannerText = if (InspectorTheme.isDark) Color(0xFFEADDFF) else Color(0xFF21005D)
                val iconBg = if (InspectorTheme.isDark) Color(0xFF38354A) else Color.White
                val iconTint = if (InspectorTheme.isDark) Color(0xFFD0BCFF) else Color(0xFF6750A4)
                val dividerColor = bannerText.copy(alpha = 0.15f)

                val infiniteTransition = rememberInfiniteTransition(label = "bannerBorder")
                val glowAlpha by infiniteTransition.animateFloat(
                    initialValue = 0.3f,
                    targetValue = 0.85f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(2200, easing = EaseInOutSine),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "glowAlpha"
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = bannerBg),
                    border = BorderStroke(1.5.dp, InspectorTheme.NeonCyan.copy(alpha = glowAlpha))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(iconBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Manufacturer",
                                    tint = iconTint,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = summary.manufacturer,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = bannerText
                                )
                                Text(
                                    text = "Model: ${summary.modelNumber}",
                                    fontSize = 13.sp,
                                    color = bannerText.copy(alpha = 0.8f)
                                )
                            }
                        }

                        Divider(
                            modifier = Modifier.padding(vertical = 16.dp),
                            color = dividerColor
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "OS Version",
                                    fontSize = 11.sp,
                                    color = bannerText.copy(alpha = 0.6f)
                                )
                                Text(
                                    text = summary.androidVersion,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = bannerText
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "System Uptime",
                                    fontSize = 11.sp,
                                    color = bannerText.copy(alpha = 0.6f)
                                )
                                Text(
                                    text = summary.uptime,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = bannerText
                                )
                            }
                        }
                    }
                }
            }

            // Quick Hardware Sections Grid Title
            item {
                Text(
                    text = "TELEMETRY CHANNELS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.5.sp,
                    color = InspectorTheme.TextMuted,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            // High Fidelity Navigation Cards List
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // CPU Card
                    StaggeredFadeInContainer(delayMillis = 50) {
                        DashboardLinkCard(
                            title = "Central Processor Unit",
                            subtitle = cpu.model,
                            infoText = "Load: ${(cpu.currentLoad * 100).toInt()}% • ${cpu.temperature}",
                            badge = "${cpu.coreCount} Cores",
                            badgeBg = InspectorTheme.NeonTeal,
                            icon = Icons.Default.Home,
                            testTag = "btn_nav_cpu",
                            onClick = { viewModel.navigateTo("cpu") }
                        )
                    }
 
                    // RAM Card
                    val ramPercent = ((ram.usedBytes.toDouble() / ram.totalBytes.toDouble()) * 100).toInt()
                    StaggeredFadeInContainer(delayMillis = 100) {
                        DashboardLinkCard(
                            title = "Memory Space (RAM)",
                            subtitle = "Used: ${formatBytes(ram.usedBytes)} / ${formatBytes(ram.totalBytes)}",
                            infoText = "Available Free: ${formatBytes(ram.freeBytes)}",
                            badge = "$ramPercent% Full",
                            badgeBg = InspectorTheme.NeonCyan,
                            icon = Icons.Default.Info,
                            testTag = "btn_nav_ram",
                            onClick = { viewModel.navigateTo("ram") }
                        )
                    }
 
                    // Battery Card
                    StaggeredFadeInContainer(delayMillis = 150) {
                        DashboardLinkCard(
                            title = "Battery Management",
                            subtitle = "Charge: ${battery.percentage}% • ${battery.chargingStatus}",
                            infoText = "Temp: ${battery.temperatureCelsius}°C • ${battery.healthStatus}",
                            badge = "Health ${battery.healthEstimate}%",
                            badgeBg = if (battery.healthEstimate > 80) InspectorTheme.NeonTeal else InspectorTheme.WarmCoral,
                            icon = Icons.Default.Warning,
                            testTag = "btn_nav_battery",
                            onClick = { viewModel.navigateTo("battery") }
                        )
                    }
 
                    // Storage Space Card
                    val storagePercent = ((storage.usedBytes.toDouble() / storage.totalBytes.toDouble()) * 100).toInt()
                    StaggeredFadeInContainer(delayMillis = 200) {
                        DashboardLinkCard(
                            title = "Storage Device",
                            subtitle = "Total: ${formatBytes(storage.totalBytes)}",
                            infoText = "Available: ${formatBytes(storage.freeBytes)}",
                            badge = "$storagePercent% Used",
                            badgeBg = InspectorTheme.AmberGlow,
                            icon = Icons.Default.Info,
                            testTag = "btn_nav_storage",
                            onClick = { viewModel.navigateTo("storage") }
                        )
                    }
 
                    // Network Telemetry
                    StaggeredFadeInContainer(delayMillis = 250) {
                        DashboardLinkCard(
                            title = "Network Interfaces",
                            subtitle = "Type: ${network.networkType} • IP: ${network.ipAddress}",
                            infoText = "WiFi: ${network.wifiStatus} • Data: ${network.mobileDataStatus}",
                            badge = "Signal: ${network.signalStrength}",
                            badgeBg = InspectorTheme.NeonBlue,
                            icon = Icons.Default.Refresh,
                            testTag = "btn_nav_network",
                            onClick = { viewModel.navigateTo("network") }
                        )
                    }
 
                    // Sensor Channels
                    StaggeredFadeInContainer(delayMillis = 300) {
                        DashboardLinkCard(
                            title = "Physical Sensors",
                            subtitle = "${sensors.size} Sensors Present",
                            infoText = "Streams: Accelerometer, Gyroscope, Magnets...",
                            badge = "Interactive",
                            badgeBg = InspectorTheme.NeonCyan,
                            icon = Icons.Default.PlayArrow,
                            testTag = "btn_nav_sensors",
                            onClick = { viewModel.navigateTo("sensors") }
                        )
                    }
 
                    // Display Information Card
                    StaggeredFadeInContainer(delayMillis = 350) {
                        DashboardLinkCard(
                            title = "Display & Screen",
                            subtitle = "${display.resolution} Pixel density",
                            infoText = "Diagonal: ${String.format(Locale.getDefault(), "%.1f\"", display.screenSizeInches)} • Speed: ${display.refreshRate.toInt()} Hz",
                            badge = "${display.dpi} DPI",
                            badgeBg = InspectorTheme.NeonTeal,
                            icon = Icons.Default.Info,
                            testTag = "btn_nav_display",
                            onClick = { viewModel.navigateTo("display") }
                        )
                    }
 
                    // Benchmark Performance Unit
                    StaggeredFadeInContainer(delayMillis = 400) {
                        DashboardLinkCard(
                            title = "Performance Benchmark",
                            subtitle = "Execute real-time hardware stress checks",
                            infoText = "Stresses CPU, speed-checks RAM & cache arrays",
                            badge = "Run Probe",
                            badgeBg = InspectorTheme.AmberGlow,
                            icon = Icons.Default.Star,
                            testTag = "btn_nav_benchmark",
                            onClick = { viewModel.navigateTo("benchmark") }
                        )
                    }
 
                    // System & Sensors Diagnostic Tests Card
                    val passedCount = diagTests.count { it.status == "PASSED" }
                    val totalCount = diagTests.size
                    val diagBadge = when (diagState) {
                        DiagnosticState.IDLE -> "START SCAN"
                        DiagnosticState.RUNNING -> "TESTING..."
                        DiagnosticState.FINISHED -> "$passedCount/$totalCount PASSED"
                    }
                    val diagBadgeBg = when (diagState) {
                        DiagnosticState.IDLE -> InspectorTheme.NeonCyan
                        DiagnosticState.RUNNING -> InspectorTheme.AmberGlow
                        DiagnosticState.FINISHED -> InspectorTheme.NeonTeal
                    }
                    val diagInfoText = when (diagState) {
                        DiagnosticState.IDLE -> "Verify operational registers & dynamic sensor streams"
                        DiagnosticState.RUNNING -> "Actively testing device registers & drivers"
                        DiagnosticState.FINISHED -> "Diagnostics block completed successfully"
                    }
 
                    StaggeredFadeInContainer(delayMillis = 450) {
                        DashboardLinkCard(
                            title = "System & Sensors Tests",
                            subtitle = "Diagnostic suite for all system blocks and hardware",
                            infoText = diagInfoText,
                            badge = diagBadge,
                            badgeBg = diagBadgeBg,
                            icon = Icons.Default.CheckCircle,
                            testTag = "btn_nav_system_test",
                            onClick = { viewModel.navigateTo("system_test") }
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
fun DashboardLinkCard(
    title: String,
    subtitle: String,
    infoText: String,
    badge: String,
    badgeBg: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    testTag: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "clickScale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = androidx.compose.foundation.LocalIndication.current,
                onClick = onClick
            )
            .testTag(testTag),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = InspectorTheme.PanelBg),
        border = BorderStroke(1.dp, InspectorTheme.DividerBg)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Elegant leading neon icon container
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(badgeBg.copy(alpha = 0.12f))
                    .border(1.dp, badgeBg.copy(alpha = 0.25f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = badgeBg,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = InspectorTheme.TextWhite
                    )
                    
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(badgeBg.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = badge,
                            fontSize = 10.sp,
                            color = badgeBg,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = InspectorTheme.TextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(2.dp))
                
                Text(
                    text = infoText,
                    fontSize = 11.sp,
                    color = InspectorTheme.TextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            // Standard Right Chevron indicating drill-down navigation
            RightChevron(
                tint = InspectorTheme.TextMuted.copy(alpha = 0.45f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

// ================= CPU DETAIL SCREEN =================

@Composable
fun CpuDetailScreen(viewModel: DeviceViewModel) {
    val cpu by viewModel.cpuInfo.collectAsState()

    Scaffold(
        topBar = {
            SubScreenTopBar("Central Processor", onBack = { viewModel.navigateTo("dashboard") })
        },
        containerColor = InspectorTheme.DarkBg
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // Loading circular gauge
            item {
                StaggeredFadeInContainer(delayMillis = 40) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressGauge(
                            percentage = cpu.currentLoad,
                            label = String.format(Locale.getDefault(), "%.0f%%", cpu.currentLoad * 100),
                            subLabel = "Total Load",
                            gaugeColor = InspectorTheme.NeonCyan,
                            size = 180.dp
                        )
                    }
                }
            }

            // Specs Card
            item {
                StaggeredFadeInContainer(delayMillis = 120) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = InspectorTheme.PanelBg)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            DetailRow("Processor Model", cpu.model, highlight = true)
                            DetailRow("Architecture", cpu.architecture)
                            DetailRow("Cores Active", "${cpu.coreCount}")
                            DetailRow("Operating Temp", cpu.temperature, valueColor = InspectorTheme.WarmCoral)
                        }
                    }
                }
            }

            // Core list heading
            item {
                Text(
                    text = "HARDWARE CORE REGISTER",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = InspectorTheme.TextMuted,
                    letterSpacing = 1.sp
                )
            }

            // Core Chips list
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    cpu.coreFrequencies.forEachIndexed { idx, freq ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = InspectorTheme.PanelBg),
                            border = BorderStroke(1.dp, InspectorTheme.DividerBg)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(InspectorTheme.DarkBg),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "C$idx",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace,
                                            color = InspectorTheme.NeonTeal
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "CPU Core #$idx",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = InspectorTheme.TextWhite
                                    )
                                }
                                Text(
                                    text = freq,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (freq == "Sleeping") InspectorTheme.TextDark else InspectorTheme.TextWhite
                                )
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

// ================= RAM DETAIL SCREEN =================

@Composable
fun RamDetailScreen(viewModel: DeviceViewModel) {
    val ram by viewModel.ramInfo.collectAsState()
    val ramPercent = (ram.usedBytes.toDouble() / ram.totalBytes.toDouble()).toFloat()

    Scaffold(
        topBar = {
            SubScreenTopBar("System RAM Memory", onBack = { viewModel.navigateTo("dashboard") })
        },
        containerColor = InspectorTheme.DarkBg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            StaggeredFadeInContainer(delayMillis = 40) {
                CircularProgressGauge(
                    percentage = ramPercent,
                    label = String.format(Locale.getDefault(), "%.0f%%", ramPercent * 100),
                    subLabel = "Memory Allocation",
                    gaugeColor = InspectorTheme.NeonBlue,
                    size = 180.dp
                )
            }

            StaggeredFadeInContainer(delayMillis = 120) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = InspectorTheme.PanelBg),
                    border = BorderStroke(1.dp, InspectorTheme.DividerBg)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        DetailRow("Total RAM Capacity", formatBytes(ram.totalBytes), highlight = true)
                        DetailRow("Allocated Memory", formatBytes(ram.usedBytes), valueColor = InspectorTheme.NeonCyan)
                        DetailRow("Free Memory Capacity", formatBytes(ram.freeBytes), valueColor = InspectorTheme.NeonTeal)
                    }
                }
            }

            StaggeredFadeInContainer(delayMillis = 200) {
                // RAM performance tips
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .border(1.dp, InspectorTheme.DividerBg, RoundedCornerShape(10.dp))
                        .background(InspectorTheme.PanelBg)
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Tips",
                            tint = InspectorTheme.NeonCyan
                        )
                        Column {
                            Text(
                                text = "Diagnostics Tips",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = InspectorTheme.TextWhite
                            )
                            Text(
                                text = "Your device automatically manages RAM. High allocation percentage is normal for caching, allowing faster launch times of applications.",
                                fontSize = 12.sp,
                                color = InspectorTheme.TextMuted
                            )
                        }
                    }
                }
            }
        }
    }
}

// ================= BATTERY OVERVIEW SCREEN =================

@Composable
fun BatteryDetailScreen(viewModel: DeviceViewModel) {
    val battery by viewModel.batteryInfo.collectAsState()

    Scaffold(
        topBar = {
            SubScreenTopBar("Battery Management", onBack = { viewModel.navigateTo("dashboard") })
        },
        containerColor = InspectorTheme.DarkBg
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // Graphic Custom Battery Drawing
            item {
                BatteryVisualIndicator(
                    percentage = battery.percentage,
                    statusText = battery.chargingStatus
                )
            }

            // PRIMARY DYNAMIC BATTERY HEALTH & ALERT PANEL
            item {
                val isOverheat = battery.healthStatus.lowercase().contains("overheat")
                val isGood = battery.healthStatus.lowercase().contains("good")
                val alertColor = when {
                    isOverheat -> InspectorTheme.WarmCoral
                    isGood -> InspectorTheme.NeonTeal
                    else -> InspectorTheme.AmberGlow
                }
                val alertIcon = when {
                    isOverheat -> Icons.Default.Warning
                    isGood -> Icons.Default.CheckCircle
                    else -> Icons.Default.Info
                }
                val conditionLabel = when {
                    isOverheat -> "OVERHEATING WARNING"
                    isGood -> "HEALTHY INTEGRITY"
                    else -> battery.healthStatus.uppercase()
                }
                val conditionDesc = when {
                    isOverheat -> "The core temperature of your lithium cell is dangerously high. Slow down resource-heavy activities now."
                    isGood -> "Your battery has healthy capacity retention and optimal physical integrity."
                    else -> "Battery reporting secondary status. Ensure core environment conditions are normal."
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("battery_health_status_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = InspectorTheme.PanelBg),
                    border = BorderStroke(1.dp, alertColor.copy(alpha = 0.35f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(alertColor.copy(alpha = 0.12f))
                                    .border(1.dp, alertColor.copy(alpha = 0.25f), RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = alertIcon,
                                    contentDescription = null,
                                    tint = alertColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "BATTERY STATUS INTEGRITY",
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = InspectorTheme.TextMuted,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = conditionLabel,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = alertColor
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = conditionDesc,
                            fontSize = 12.sp,
                            color = InspectorTheme.TextMuted,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // DUAL-COLUMN HIGH-DENSITY METRICS ROW
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Temperature Card Group
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .testTag("battery_temp_card"),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = InspectorTheme.PanelBg),
                        border = BorderStroke(1.dp, InspectorTheme.DividerBg)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(InspectorTheme.WarmCoral.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = InspectorTheme.WarmCoral,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                Text(
                                    text = "THERMAL CORE",
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = InspectorTheme.TextMuted
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = String.format(Locale.getDefault(), "%.1f °C", battery.temperatureCelsius),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = InspectorTheme.TextWhite
                            )
                            val fahrenheit = (battery.temperatureCelsius * 9/5) + 32
                            Text(
                                text = String.format(Locale.getDefault(), "%.1f °F", fahrenheit),
                                fontSize = 11.sp,
                                color = InspectorTheme.TextMuted,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            // Thermal Visual bar representation
                            val progress = (battery.temperatureCelsius / 60f).coerceIn(0f, 1f)
                            val barColor = when {
                                battery.temperatureCelsius < 37f -> InspectorTheme.NeonTeal
                                battery.temperatureCelsius < 45f -> InspectorTheme.AmberGlow
                                else -> InspectorTheme.WarmCoral
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(InspectorTheme.DividerBg.copy(alpha = 0.4f))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(progress)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(barColor)
                                )
                            }
                        }
                    }

                    // Power & Health Retention Card Group
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .testTag("battery_health_capacity_card"),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = InspectorTheme.PanelBg),
                        border = BorderStroke(1.dp, InspectorTheme.DividerBg)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(InspectorTheme.NeonCyan.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = InspectorTheme.NeonCyan,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                Text(
                                    text = "RETAINED HEALTH",
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = InspectorTheme.TextMuted
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "${battery.healthEstimate}%",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = InspectorTheme.TextWhite
                            )
                            Text(
                                text = "LIFESPAN DEPTH",
                                fontSize = 11.sp,
                                color = InspectorTheme.TextMuted,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            // Health Retention Visual bar
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(InspectorTheme.DividerBg.copy(alpha = 0.4f))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(battery.healthEstimate / 100f)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(InspectorTheme.NeonCyan)
                                )
                            }
                        }
                    }
                }
            }

            // Specs
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = InspectorTheme.PanelBg),
                    border = BorderStroke(1.dp, InspectorTheme.DividerBg)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        DetailRow("State of Charge", "${battery.percentage}%", highlight = true)
                        DetailRow("Terminal Voltage", String.format(Locale.getDefault(), "%.3f V", battery.voltageVolts))
                        DetailRow("Core Temperature", String.format(Locale.getDefault(), "%.1f °C", battery.temperatureCelsius), valueColor = InspectorTheme.WarmCoral)
                        DetailRow("Plugged Source", battery.powerSource)
                        DetailRow("Device Integrity Status", battery.healthStatus, valueColor = InspectorTheme.NeonTeal)
                        DetailRow("Estimated Health State", "${battery.healthEstimate}%", valueColor = InspectorTheme.NeonCyan)
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
fun BatteryVisualIndicator(percentage: Int, statusText: String) {
    val animatedPercentage by animateFloatAsState(
        targetValue = percentage.toFloat(),
        animationSpec = tween(1200, easing = FastOutSlowInEasing),
        label = "battery"
    )
    val isCharging = statusText.lowercase().contains("charge") || statusText.lowercase().contains("plugged")

    val infiniteTransition = rememberInfiniteTransition(label = "chargingAnim")
    val flowOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "flowOffset"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Horizontal Battery Core Cylinder
            Box(
                modifier = Modifier
                    .width(180.dp)
                    .height(84.dp)
                    .border(3.dp, InspectorTheme.TextWhite, RoundedCornerShape(12.dp))
                    .padding(6.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                // Segmented 5-block fuel cell representation
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val segmentsCount = 5
                    val segmentThresholdPercent = 100f / segmentsCount
                    val barColor = when {
                        percentage < 20 -> InspectorTheme.WarmCoral
                        percentage < 45 -> InspectorTheme.AmberGlow
                        else -> InspectorTheme.NeonTeal
                    }
                    
                    for (i in 0 until segmentsCount) {
                        val segmentMinPercent = i * segmentThresholdPercent
                        val activeFraction = ((animatedPercentage - segmentMinPercent) / segmentThresholdPercent).coerceIn(0f, 1f)
                        
                        // If charging, make a traveling energy bubble wave
                        val waveGlowMultiplier = if (isCharging) {
                            val targetPos = flowOffset * segmentsCount
                            val distance = kotlin.math.abs(i.toFloat() - targetPos)
                            val factor = (1.5f - distance).coerceIn(0.4f, 1.5f)
                            factor
                        } else {
                            1f
                        }
                        
                        if (activeFraction > 0f) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(
                                                barColor.copy(alpha = (activeFraction * 0.4f * waveGlowMultiplier).coerceIn(0f, 1f)),
                                                barColor.copy(alpha = (activeFraction * waveGlowMultiplier).coerceIn(0f, 1f))
                                            )
                                        )
                                    )
                            )
                        } else {
                            // If charging, maybe dim cells also show small flowing ghost energy!
                            val ghostAlpha = if (isCharging) {
                                val targetPos = flowOffset * segmentsCount
                                val distance = kotlin.math.abs(i.toFloat() - targetPos)
                                val factor = (1.5f - distance).coerceIn(0f, 1.5f)
                                (0.15f * factor).coerceIn(0.05f, 0.25f)
                            } else {
                                0.25f
                            }
                            
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        if (isCharging) barColor.copy(alpha = ghostAlpha)
                                        else InspectorTheme.DividerBg.copy(alpha = 0.25f)
                                    )
                            )
                        }
                    }
                }

                // Centered text
                val amberColor = InspectorTheme.AmberGlow
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (isCharging) {
                            Canvas(modifier = Modifier.size(16.dp)) {
                                val w = size.width
                                val h = size.height
                                val path = androidx.compose.ui.graphics.Path().apply {
                                    moveTo(w * 0.55f, h * 0.05f)
                                    lineTo(w * 0.25f, h * 0.55f)
                                    lineTo(w * 0.55f, h * 0.55f)
                                    lineTo(w * 0.45f, h * 0.95f)
                                    lineTo(w * 0.75f, h * 0.45f)
                                    lineTo(w * 0.45f, h * 0.45f)
                                    close()
                                }
                                drawPath(path = path, color = amberColor)
                            }
                        }
                        Text(
                            text = "$percentage%",
                            fontWeight = FontWeight.Black,
                            fontSize = 22.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color.White
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(10.dp))
            
            Text(
                text = "STATUS: ${statusText.uppercase()}",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = InspectorTheme.TextMuted,
                letterSpacing = 1.sp
            )
        }
        
        // Positive terminal node drawn to the right of battery cylinder
        Box(
            modifier = Modifier
                .offset(x = 92.dp)
                .size(height = 24.dp, width = 8.dp)
                .clip(RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp))
                .background(InspectorTheme.TextWhite)
        )
    }
}

// ================= STORAGE SHIELD SCREEN =================

@Composable
fun StorageDetailScreen(viewModel: DeviceViewModel) {
    val storage by viewModel.storageInfo.collectAsState()
    val storagePercent = (storage.usedBytes.toDouble() / storage.totalBytes.toDouble()).toFloat()

    Scaffold(
        topBar = {
            SubScreenTopBar("Physical Storage", onBack = { viewModel.navigateTo("dashboard") })
        },
        containerColor = InspectorTheme.DarkBg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            CircularProgressGauge(
                percentage = storagePercent,
                label = String.format(Locale.getDefault(), "%.0f%%", storagePercent * 100),
                subLabel = "Storage Utilization",
                gaugeColor = InspectorTheme.AmberGlow,
                size = 190.dp
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = InspectorTheme.PanelBg)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    DetailRow("Aggregate Storage Capacity", formatBytes(storage.totalBytes), highlight = true)
                    DetailRow("Allocated/Used Space", formatBytes(storage.usedBytes), valueColor = InspectorTheme.WarmCoral)
                    DetailRow("Free Disposable Capacity", formatBytes(storage.freeBytes), valueColor = InspectorTheme.NeonTeal)
                }
            }

            // High depth visual progress bar
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "STORAGE GRID RATIO",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = InspectorTheme.TextMuted,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(18.dp)
                        .clip(CircleShape)
                        .background(InspectorTheme.PanelBg)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(storagePercent)
                            .clip(CircleShape)
                            .background(InspectorTheme.AmberGlow)
                    )
                }
            }
        }
    }
}

// ================= NETWORK TELEMETRY SCREEN =================

@Composable
fun NetworkDetailScreen(viewModel: DeviceViewModel) {
    val network by viewModel.networkInfo.collectAsState()

    Scaffold(
        topBar = {
            SubScreenTopBar("Network Interfaces", onBack = { viewModel.navigateTo("dashboard") })
        },
        containerColor = InspectorTheme.DarkBg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Radar scan animation graphic
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(InspectorTheme.PanelBg),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Signals",
                        tint = InspectorTheme.NeonBlue,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "IP RESOLVED: ${network.ipAddress}",
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = InspectorTheme.NeonTeal
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = InspectorTheme.PanelBg)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    DetailRow("Cellular link Type", network.networkType, highlight = true)
                    DetailRow("Gateway IPv4 Address", network.ipAddress, valueColor = InspectorTheme.NeonTeal)
                    DetailRow("WiFi Connection State", network.wifiStatus)
                    DetailRow("Mobile Cellular Connectivity", network.mobileDataStatus)
                    DetailRow("Local Link Signal Strength", network.signalStrength, valueColor = InspectorTheme.NeonBlue)
                }
            }
        }
    }
}

// ================= INTERACTIVE SENSORS SCREEN =================

@Composable
fun SensorsDetailScreen(viewModel: DeviceViewModel) {
    val sensors by viewModel.sensorList.collectAsState()
    val activeSensorId by viewModel.activeSensorId.collectAsState()
    val liveValues by viewModel.liveSensorValues.collectAsState()

    Scaffold(
        topBar = {
            SubScreenTopBar("Hardware Sensors", onBack = { viewModel.navigateTo("dashboard") })
        },
        containerColor = InspectorTheme.DarkBg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // Live display plot module if any sensor is active
            AnimatedVisibility(
                visible = activeSensorId != null,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                val activeSensorItem = sensors.find { it.id == activeSensorId }
                if (activeSensorItem != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = InspectorTheme.PanelBg),
                        border = BorderStroke(1.dp, InspectorTheme.NeonCyan.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "🔴 LIVE GRAPH: ${activeSensorItem.name.uppercase()}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = InspectorTheme.WarmCoral
                                )
                                TextButton(onClick = { viewModel.stopSensorLogging() }) {
                                    Text("PAUSE STREAM", color = InspectorTheme.NeonCyan, fontSize = 11.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Draw continuous coordinates
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                CoordinateValueText("X-Axis", liveValues.getOrElse(0) { 0f })
                                CoordinateValueText("Y-Axis", liveValues.getOrElse(1) { 0f })
                                CoordinateValueText("Z-Axis", liveValues.getOrElse(2) { 0f })
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Graph waves
                            LiveScopePlotWaves(values = liveValues)
                        }
                    }
                }
            }

            Text(
                text = "AVAILABLE SENSOR REGISTER",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = InspectorTheme.TextMuted,
                modifier = Modifier.padding(bottom = 10.dp, start = 4.dp)
            )

            // Scrollable list
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(sensors) { sensor ->
                    val isActive = sensor.id == activeSensorId
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.startSensorLogging(sensor.sensorType, sensor.id)
                            },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isActive) InspectorTheme.PanelBg else InspectorTheme.PanelBg
                        ),
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (isActive) InspectorTheme.NeonCyan else InspectorTheme.DividerBg
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = sensor.name,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = InspectorTheme.TextWhite
                                )
                                Text(
                                    text = "Vendor: ${sensor.vendor} • Power: ${String.format(Locale.getDefault(), "%.3f", sensor.power)}mA",
                                    fontSize = 11.sp,
                                    color = InspectorTheme.TextMuted
                                )
                            }
                            
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(InspectorTheme.DarkBg)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = sensor.typeString,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = InspectorTheme.NeonTeal
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CoordinateValueText(label: String, value: Float) {
    Column {
        Text(text = label, fontSize = 10.sp, color = InspectorTheme.TextMuted)
        Text(
            text = String.format(Locale.getDefault(), "%.4f", value),
            fontSize = 14.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = InspectorTheme.TextWhite
        )
    }
}

@Composable
fun LiveScopePlotWaves(values: List<Float>) {
    // Collect rolling history to plot nice waveform
    val rollingHistory = remember { mutableStateListOf<List<Float>>() }
    
    // Add current coordinates to history
    LaunchedEffect(values) {
        rollingHistory.add(values)
        if (rollingHistory.size > 24) {
            rollingHistory.removeAt(0)
        }
    }

    val cyanColor = InspectorTheme.NeonCyan
    val coralColor = InspectorTheme.WarmCoral

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .background(InspectorTheme.DarkBg)
    ) {
        val w = size.width
        val h = size.height
        val centerY = h / 2f
        
        // gridlines
        drawLine(Color(0xFF1E293B), Offset(0f, centerY), Offset(w, centerY), strokeWidth = 1f)
        
        if (rollingHistory.size > 1) {
            val stepX = w / 24f
            
            // X Wave (Cyan)
            for (i in 0 until rollingHistory.size - 1) {
                val val1 = rollingHistory[i].getOrElse(0) { 0f }
                val val2 = rollingHistory[i+1].getOrElse(0) { 0f }
                
                drawLine(
                    color = cyanColor,
                    start = Offset(i * stepX, centerY - (val1 * 3f).coerceIn(-centerY, centerY)),
                    end = Offset((i + 1) * stepX, centerY - (val2 * 3f).coerceIn(-centerY, centerY)),
                    strokeWidth = 2.dp.toPx()
                )
            }
            
            // Y Wave (Magenta)
            for (i in 0 until rollingHistory.size - 1) {
                val val1 = rollingHistory[i].getOrElse(1) { 0f }
                val val2 = rollingHistory[i+1].getOrElse(1) { 0f }
                
                drawLine(
                    color = coralColor,
                    start = Offset(i * stepX, centerY - (val1 * 3f).coerceIn(-centerY, centerY)),
                    end = Offset((i + 1) * stepX, centerY - (val2 * 3f).coerceIn(-centerY, centerY)),
                    strokeWidth = 2.dp.toPx()
                )
            }
        }
    }
}

// ================= DISPLAY SHIELD SCREEN =================

@Composable
fun DisplayDetailScreen(viewModel: DeviceViewModel) {
    val display by viewModel.displayInfo.collectAsState()

    Scaffold(
        topBar = {
            SubScreenTopBar("Screen & Display", onBack = { viewModel.navigateTo("dashboard") })
        },
        containerColor = InspectorTheme.DarkBg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Phone schematic
            PhoneVisualSchematic(display = display)

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = InspectorTheme.PanelBg)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    DetailRow("Physical Display Diagonal", String.format(Locale.getDefault(), "%.1f Inches", display.screenSizeInches), highlight = true)
                    DetailRow("Hardware Resolution", display.resolution)
                    DetailRow("Current Refresh Rate", "${display.refreshRate.toInt()} Hz", valueColor = InspectorTheme.NeonTeal)
                    DetailRow("Pixel Aspect Density", "${display.dpi} DPI")
                }
            }
        }
    }
}

@Composable
fun PhoneVisualSchematic(display: DisplayInfo) {
    val infiniteTransition = rememberInfiniteTransition(label = "scanLine")
    val scanY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "yOffset"
    )

    val tealColor = InspectorTheme.NeonTeal

    Box(
        modifier = Modifier
            .size(width = 150.dp, height = 240.dp)
            .border(4.dp, InspectorTheme.TextWhite, RoundedCornerShape(18.dp))
            .background(InspectorTheme.DarkBg)
            .clip(RoundedCornerShape(18.dp)),
        contentAlignment = Alignment.Center
    ) {
        // Continuous neon sweeps visualizing refresh rate scanning lines
        Canvas(modifier = Modifier.fillMaxSize()) {
            val currY = scanY * size.height
            
            // Draw horizontal scanning line line
            drawLine(
                color = tealColor.copy(alpha = 0.5f),
                start = Offset(0f, currY),
                end = Offset(size.width, currY),
                strokeWidth = 3.dp.toPx()
            )
            
            // Draw a trailing gradient
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        tealColor.copy(alpha = 0.15f),
                        Color.Transparent
                    ),
                    startY = (currY - 80f).coerceAtLeast(0f),
                    endY = currY
                ),
                topLeft = Offset(0f, (currY - 80f).coerceAtLeast(0f)),
                size = androidx.compose.ui.geometry.Size(size.width, (80f).coerceAtMost(currY))
            )
        }

        // Mock notch hole
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 10.dp)
                .size(10.dp)
                .clip(CircleShape)
                .background(InspectorTheme.TextWhite)
        )

        // Prints inside Mock screen
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "${display.resolution} Pixels",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = InspectorTheme.NeonCyan
            )
            Text(
                text = "${display.refreshRate.toInt()}Hz Screen",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = InspectorTheme.NeonTeal
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = String.format(Locale.getDefault(), "%.1f\"", display.screenSizeInches),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = InspectorTheme.TextWhite
            )
            Text(
                text = "DIAGONAL",
                fontSize = 9.sp,
                color = InspectorTheme.TextMuted,
                letterSpacing = 1.sp
            )
        }
    }
}

// ================= BENCHMARK SCREEN =================

@Composable
fun BenchmarkDetailScreen(viewModel: DeviceViewModel) {
    val benchmark by viewModel.benchmarkState.collectAsState()

    Scaffold(
        topBar = {
            SubScreenTopBar("Hardware Benchmark", onBack = { viewModel.navigateTo("dashboard") })
        },
        containerColor = InspectorTheme.DarkBg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            when (benchmark.status) {
                BenchmarkStatus.IDLE -> {
                    val idleColor = InspectorTheme.AmberGlow
                    val infiniteTransition = rememberInfiniteTransition(label = "benchmarkPulse")
                    val pulseScale by infiniteTransition.animateFloat(
                        initialValue = 1.0f,
                        targetValue = 1.15f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1500, easing = EaseInOutSine),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "starPulse"
                    )
                    val pulseAlpha by infiniteTransition.animateFloat(
                        initialValue = 0.12f,
                        targetValue = 0.35f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1500, easing = EaseInOutSine),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "glowPulse"
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(130.dp)
                                .clip(CircleShape)
                                .background(InspectorTheme.PanelBg)
                                .drawBehind {
                                    drawCircle(
                                        color = idleColor.copy(alpha = pulseAlpha),
                                        radius = (65 + (pulseScale - 1f) * 12f).dp.toPx()
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Benchmark IDLE",
                                tint = InspectorTheme.AmberGlow,
                                modifier = Modifier
                                    .size(54.dp)
                                    .graphicsLayer {
                                        scaleX = pulseScale
                                        scaleY = pulseScale
                                    }
                            )
                        }

                        Text(
                            text = "STRESS PERFORMANCE PROBE",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = InspectorTheme.TextWhite
                        )

                        Text(
                            text = "This utility performs multi-threaded mathematical calculations inside internal pipelines, copies memory blocks rapidly to evaluate RAM speed, and creates small files on disk to measure persistent sequential reading and writing speeds.",
                            fontSize = 13.sp,
                            color = InspectorTheme.TextMuted,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                    }

                    Button(
                        onClick = { viewModel.startBenchmarkTest() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("run_benchmark"),
                        colors = ButtonDefaults.buttonColors(containerColor = InspectorTheme.AmberGlow)
                    ) {
                        Text("LAUNCH BENCHMARK SUITE", color = InspectorTheme.DarkBg, fontWeight = FontWeight.Bold)
                    }
                }

                BenchmarkStatus.RUNNING -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        CircularProgressGauge(
                            percentage = benchmark.progress,
                            label = String.format(Locale.getDefault(), "%.0f%%", benchmark.progress * 100),
                            subLabel = benchmark.currentTestName,
                            gaugeColor = InspectorTheme.AmberGlow,
                            size = 170.dp
                        )

                        Text(
                            text = "DIAGNOSTIC PIPELINE CONSOLE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = InspectorTheme.AmberGlow,
                            modifier = Modifier.align(Alignment.Start)
                        )

                        // Terminal console display
                        DiagnosticConsoleLogger(logs = benchmark.logs)
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(InspectorTheme.PanelBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("PROBING... KEEP APPLICATION ACTIVE", color = InspectorTheme.TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                BenchmarkStatus.COMPLETED -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Overall TIER Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = InspectorTheme.PanelBg),
                            border = BorderStroke(2.dp, InspectorTheme.AmberGlow)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "🎖️ OVERALL PERFORMANCE TIER",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = InspectorTheme.TextMuted,
                                    letterSpacing = 1.sp
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = benchmark.overallTier.uppercase(),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = InspectorTheme.AmberGlow,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                Text(
                                    text = "${benchmark.overallScore}",
                                    fontSize = 48.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = InspectorTheme.TextWhite
                                )

                                Text(
                                    text = "INSPECTOR INDEX RATING",
                                    fontSize = 10.sp,
                                    color = InspectorTheme.TextMuted,
                                    letterSpacing = 1.5.sp
                                )
                            }
                        }

                        // Module indices
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = InspectorTheme.PanelBg)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "DIAGNOSTIC SCORES",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = InspectorTheme.TextWhite,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )

                                DetailRow("CPU Engine Test", "${benchmark.cpuScore} pts", valueColor = InspectorTheme.NeonTeal)
                                DetailRow("RAM Allocation", "${benchmark.ramScore} pts", valueColor = InspectorTheme.NeonCyan)
                                DetailRow("Storage Input/Output", "${benchmark.storageScore} pts", valueColor = InspectorTheme.AmberGlow)
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.resetBenchmarkTest() },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = InspectorTheme.TextWhite),
                            border = BorderStroke(1.dp, InspectorTheme.DividerBg)
                        ) {
                            Text("RESET TEST")
                        }

                        Button(
                            onClick = { viewModel.startBenchmarkTest() },
                            modifier = Modifier
                                .weight(1.2f)
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = InspectorTheme.AmberGlow)
                        ) {
                            Text("PROBE AGAIN", color = InspectorTheme.DarkBg, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun DiagnosticConsoleLogger(logs: List<String>) {
    val scrollState = rememberScrollState()
    
    // Automatically scrolls with logs
    LaunchedEffect(logs.size) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    val infiniteTransition = rememberInfiniteTransition(label = "terminalCursor")
    val cursorAlpha by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cursorAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF060A10))
            .border(1.2.dp, InspectorTheme.DividerBg, RoundedCornerShape(12.dp))
            .drawBehind {
                // Phosphor scanline filter overlay
                val scanLineSpacing = 4.dp.toPx()
                var yOffset = 0f
                while (yOffset < size.height) {
                    drawLine(
                        color = Color(0x1800E5FF),
                        start = Offset(0f, yOffset),
                        end = Offset(size.width, yOffset),
                        strokeWidth = 1.dp.toPx()
                    )
                    yOffset += scanLineSpacing
                }
            }
            .padding(14.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            logs.forEach { log ->
                Text(
                    text = log,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium,
                    color = if (log.contains("Score") || log.contains("Final") || log.contains("Complete")) {
                        InspectorTheme.AmberGlow
                    } else if (log.contains("⚡") || log.contains("✓") || log.contains("SUCCESS") || log.contains("PASSED")) {
                        InspectorTheme.NeonTeal
                    } else if (log.contains("ERROR") || log.contains("FAIL") || log.contains("✗") || log.contains("warning")) {
                        InspectorTheme.WarmCoral
                    } else {
                        Color(0xFF90A4AE)
                    }
                )
            }
            
            // Retro input caret prompt
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "sys@inspector:~# ",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = InspectorTheme.NeonCyan.copy(alpha = 0.5f),
                    fontWeight = FontWeight.Bold
                )
                Box(
                    modifier = Modifier
                        .size(width = 6.dp, height = 11.dp)
                        .background(InspectorTheme.NeonCyan.copy(alpha = if (cursorAlpha > 0.5f) 0.8f else 0f))
                )
            }
        }
    }
}

// ================= SYSTEM & SENSORS DIAGNOSTICS SCREEN =================

@Composable
fun SystemTestScreen(viewModel: DeviceViewModel) {
    val diagState by viewModel.diagnosticState.collectAsState()
    val progress by viewModel.diagnosticProgress.collectAsState()
    val currentItem by viewModel.currentTestingItem.collectAsState()
    val tests by viewModel.diagnosticTests.collectAsState()

    val context = LocalContext.current

    Scaffold(
        topBar = {
            SubScreenTopBar("System & Sensors", onBack = { viewModel.navigateTo("dashboard") })
        },
        containerColor = InspectorTheme.DarkBg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // Main Gauge Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = InspectorTheme.PanelBg),
                border = BorderStroke(1.dp, InspectorTheme.DividerBg)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Gauge visualization
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val activeColor = if (diagState == DiagnosticState.FINISHED) InspectorTheme.NeonTeal else InspectorTheme.AmberGlow
                        val trackColor = InspectorTheme.DividerBg.copy(alpha = 0.5f)
                        val animatedProgress by animateFloatAsState(
                            targetValue = progress,
                            animationSpec = tween(650, easing = LinearOutSlowInEasing),
                            label = "diagnosticProgress"
                        )

                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val strokeW = 10.dp.toPx()
                            
                            // Draw track circle
                            drawArc(
                                color = trackColor,
                                startAngle = -225f,
                                sweepAngle = 270f,
                                useCenter = false,
                                style = Stroke(width = strokeW, cap = StrokeCap.Round)
                            )
                            
                            // Glowing blooming under-glow arc
                            drawArc(
                                color = activeColor.copy(alpha = 0.2f),
                                startAngle = -225f,
                                sweepAngle = 270f * animatedProgress,
                                useCenter = false,
                                style = Stroke(width = strokeW * 1.8f, cap = StrokeCap.Round)
                            )
                            
                            // Draw active arc based on progress
                            drawArc(
                                color = activeColor,
                                startAngle = -225f,
                                sweepAngle = 270f * animatedProgress,
                                useCenter = false,
                                style = Stroke(width = strokeW, cap = StrokeCap.Round)
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            val scorePercentage = if (diagState == DiagnosticState.FINISHED) {
                                val passedCount = tests.count { it.status == "PASSED" }
                                val total = tests.size.coerceAtLeast(1)
                                ((passedCount.toFloat() / total.toFloat()) * 100).toInt()
                            } else {
                                (progress * 100).toInt()
                            }
                            
                            val labelText = when (diagState) {
                                DiagnosticState.IDLE -> "READY"
                                DiagnosticState.RUNNING -> "RUNNING"
                                DiagnosticState.FINISHED -> "HEALTH"
                            }

                            Text(
                                text = labelText,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = InspectorTheme.TextMuted
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "$scorePercentage%",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Black,
                                color = if (diagState == DiagnosticState.FINISHED) InspectorTheme.NeonTeal else InspectorTheme.TextWhite
                            )
                        }
                    }

                    // Display info text on status
                    val heading = when (diagState) {
                        DiagnosticState.IDLE -> "Ready for Diagnostic Probe"
                        DiagnosticState.RUNNING -> "Scanning Device Registers..."
                        DiagnosticState.FINISHED -> "Scan Completed Successfully!"
                    }
                    val body = when (diagState) {
                        DiagnosticState.IDLE -> "Initiates an automated diagnostic check across multi-core processors, memory speeds, cache blocks, local storage file trees, and native hardware sensor registers."
                        DiagnosticState.RUNNING -> "Evaluating module: ${currentItem ?: "System Components"}..."
                        DiagnosticState.FINISHED -> {
                            val passed = tests.count { it.status == "PASSED" }
                            val warnings = tests.count { it.status == "WARNING" }
                            "Scan complete. $passed components fully optimal. $warnings warnings or unpopulated registers detected."
                        }
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = heading,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = InspectorTheme.TextWhite,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = body,
                            fontSize = 12.sp,
                            color = InspectorTheme.TextMuted,
                            textAlign = TextAlign.Center,
                            lineHeight = 16.sp,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }

                    // CTAs
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (diagState == DiagnosticState.RUNNING) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .background(InspectorTheme.DividerBg, RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("DIAGNOSTICS IN PROGRESS...", color = InspectorTheme.TextMuted, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        } else {
                            if (diagState == DiagnosticState.FINISHED) {
                                Button(
                                    onClick = { viewModel.resetSystemAndSensorsTest() },
                                    colors = ButtonDefaults.buttonColors(containerColor = InspectorTheme.DividerBg),
                                    modifier = Modifier.weight(1f).height(48.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("RESET", color = InspectorTheme.TextWhite, fontWeight = FontWeight.Bold)
                                }
                            }
                            Button(
                                onClick = { viewModel.startSystemAndSensorsTest() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (diagState == DiagnosticState.FINISHED) InspectorTheme.NeonCyan else InspectorTheme.NeonTeal
                                ),
                                modifier = Modifier.weight(1.8f).height(48.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                val buttonText = if (diagState == DiagnosticState.FINISHED) "RE-TEST" else "START TEST"
                                Text(buttonText, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Results List Header
            if (tests.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "DIAGNOSTICS DETAIL RECORD",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = InspectorTheme.TextMuted
                    )
                    Text(
                        text = "${tests.count { it.status == "PASSED" || it.status == "WARNING" }} / ${tests.size} Passed",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = InspectorTheme.NeonTeal
                    )
                }
            }

            // Lazy list of diagnostic items
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(tests) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = InspectorTheme.PanelBg),
                        border = BorderStroke(1.dp, InspectorTheme.DividerBg)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(if (item.type == "System") InspectorTheme.NeonBlue.copy(alpha = 0.2f) else InspectorTheme.NeonCyan.copy(alpha = 0.2f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(item.type.uppercase(), fontSize = 8.sp, fontWeight = FontWeight.Black, color = if (item.type == "System") InspectorTheme.NeonBlue else InspectorTheme.NeonCyan)
                                    }
                                    Text(item.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = InspectorTheme.TextWhite)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (item.status == "PENDING" || item.status == "TESTING") item.description else item.detail,
                                    fontSize = 11.sp,
                                    color = InspectorTheme.TextMuted
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // Status Indicator Badge
                            when (item.status) {
                                "PENDING" -> {
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .border(1.5.dp, InspectorTheme.DividerBg, CircleShape)
                                    )
                                }
                                "TESTING" -> {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = InspectorTheme.AmberGlow
                                    )
                                }
                                "PASSED" -> {
                                    val passedColor = InspectorTheme.NeonTeal
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(passedColor.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Canvas(modifier = Modifier.size(10.dp)) {
                                            val path = androidx.compose.ui.graphics.Path().apply {
                                                moveTo(size.width * 0.18f, size.height * 0.5f)
                                                lineTo(size.width * 0.45f, size.height * 0.78f)
                                                lineTo(size.width * 0.85f, size.height * 0.22f)
                                            }
                                            drawPath(
                                                path = path,
                                                color = passedColor,
                                                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Companion.Round)
                                            )
                                        }
                                    }
                                }
                                "WARNING" -> {
                                    val warningColor = InspectorTheme.AmberGlow
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(warningColor.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("!", color = warningColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                "FAILED" -> {
                                    val failedColor = InspectorTheme.WarmCoral
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(failedColor.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("✗", color = failedColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
                
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

// ================= COMMON REUSABLE COMPOSABLES =================

@Composable
fun SubScreenTopBar(title: String, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(InspectorTheme.PanelBg)
            .statusBarsPadding()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val backInteractionSource = remember { MutableInteractionSource() }
            val backIsPressed by backInteractionSource.collectIsPressedAsState()
            val backOffset by animateDpAsState(
                targetValue = if (backIsPressed) (-4).dp else 0.dp,
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                label = "backPressOffset"
            )

            IconButton(
                onClick = onBack,
                interactionSource = backInteractionSource,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .testTag("back_button")
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack, // Standard default ArrowBack
                    contentDescription = "Return home",
                    tint = InspectorTheme.TextWhite,
                    modifier = Modifier.offset(x = backOffset)
                )
            }
            Text(
                text = title.uppercase(Locale.getDefault()),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = InspectorTheme.TextWhite,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

@Composable
fun DetailRow(label: String, value: String, highlight: Boolean = false, valueColor: Color = InspectorTheme.TextWhite) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = InspectorTheme.TextMuted
        )
        Text(
            text = value,
            fontSize = if (highlight) 15.sp else 13.sp,
            fontWeight = if (highlight) FontWeight.Bold else FontWeight.SemiBold,
            color = valueColor,
            textAlign = TextAlign.End,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun CircularProgressGauge(
    percentage: Float,
    label: String,
    subLabel: String,
    gaugeColor: Color,
    size: androidx.compose.ui.unit.Dp
) {
    val animatedPercentage by animateFloatAsState(
        targetValue = percentage,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "gauge"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "gaugeTracker")
    val orbitRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbitRotation"
    )

    Box(
        modifier = Modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val sweep = animatedPercentage * 360f
            val strokeW = 10.dp.toPx()
            val radius = (size.toPx() - 40f) / 2f
            
            // Background track circle
            drawCircle(
                color = gaugeColor.copy(alpha = 0.08f),
                radius = radius,
                style = Stroke(width = strokeW, cap = StrokeCap.Round)
            )
            
            // Soft neon bloom under-glow arc
            drawArc(
                color = gaugeColor.copy(alpha = 0.18f),
                startAngle = -90f,
                sweepAngle = sweep,
                useCenter = false,
                style = Stroke(width = strokeW * 1.8f, cap = StrokeCap.Round)
            )
            
            // Primary foreground progress arc
            drawArc(
                color = gaugeColor,
                startAngle = -90f,
                sweepAngle = sweep,
                useCenter = false,
                style = Stroke(width = strokeW, cap = StrokeCap.Round)
            )

            // Outer Orbit guideline (Sleek futuristic ring)
            val orbitRadius = radius + 15.dp.toPx()
            drawCircle(
                color = gaugeColor.copy(alpha = 0.1f),
                radius = orbitRadius,
                style = Stroke(width = 1.dp.toPx())
            )

            // Outer Orbit tracker satellite dot
            val angleRad = Math.toRadians((orbitRotation - 90).toDouble())
            val dotX = center.x + orbitRadius * kotlin.math.cos(angleRad).toFloat()
            val dotY = center.y + orbitRadius * kotlin.math.sin(angleRad).toFloat()
            drawCircle(
                color = gaugeColor,
                radius = 3.5.dp.toPx(),
                center = Offset(dotX, dotY)
            )
        }
        
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = label,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = InspectorTheme.TextWhite
            )
            
            Spacer(modifier = Modifier.height(2.dp))
            
            Text(
                text = subLabel.uppercase(Locale.getDefault()),
                fontSize = 10.sp,
                color = InspectorTheme.TextMuted,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

fun formatBytes(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
    return String.format(Locale.getDefault(), "%.1f %s", bytes / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
}
