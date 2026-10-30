package com.example.ui.screens

import android.view.MotionEvent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CelestialType
import com.example.ui.theme.*
import com.example.viewmodel.ActiveScreen
import com.example.viewmodel.EclipseGameViewModel
import kotlin.math.*

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun FlightCockpitScreen(viewModel: EclipseGameViewModel) {
    BackHandler {
        viewModel.navigateTo(ActiveScreen.MAIN_MENU)
    }

    val vitals by viewModel.vitals.collectAsState()
    val phase by viewModel.gamePhase.collectAsState()
    val nearbyEntity by viewModel.nearbyEntity.collectAsState()
    val activeAlert by viewModel.activeAlert.collectAsState()
    val entities by viewModel.entities.collectAsState()
    val frameTick by viewModel.frameTick.collectAsState()

    var showObjectiveDetails by remember { mutableStateOf(false) }

    // Preallocated Path for ship rendering to avoid per-frame object allocation
    val shipPath = remember {
        Path().apply {
            moveTo(18f, 0f)
            lineTo(-14f, -14f)
            lineTo(-8f, 0f)
            lineTo(-14f, 14f)
            close()
        }
    }
    val flamePath = remember {
        Path().apply {
            moveTo(-10f, 0f)
            lineTo(-32f, -6f)
            lineTo(-46f, 0f)
            lineTo(-32f, 6f)
            close()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VoidBlack)
    ) {
        // High-Performance Deep Space Viewport & Tactical Radar
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Subscribe to frameTick to trigger redraw at stable 30fps
            @Suppress("UNUSED_VARIABLE")
            val tick = frameTick

            val canvasW = size.width
            val canvasH = size.height
            // Center the radar view in the upper vertical half so bottom controls never overlap
            val center = Offset(canvasW * 0.5f, canvasH * 0.40f)

            val zoom = 0.85f
            val curPosX = viewModel.posX
            val curPosY = viewModel.posY

            // Radar distance rings
            drawCircle(
                color = TelemetryCyan.copy(alpha = 0.07f),
                radius = 110f * zoom,
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )
            drawCircle(
                color = TelemetryCyan.copy(alpha = 0.05f),
                radius = 240f * zoom,
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )
            drawCircle(
                color = TelemetryCyan.copy(alpha = 0.03f),
                radius = 380f * zoom,
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )

            // Dying Star (Helios-Omega) at World (0, 0)
            val starRelX = (0f - curPosX) * zoom
            val starRelY = (0f - curPosY) * zoom
            val starScreen = Offset(center.x + starRelX, center.y + starRelY)
            val starRadius = 130f * zoom

            // Coronal gradient glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        CoronaAmber.copy(alpha = 0.45f),
                        CoronaAmberDim.copy(alpha = 0.22f),
                        HazardRed.copy(alpha = 0.08f),
                        Color.Transparent
                    ),
                    center = starScreen,
                    radius = starRadius * 2.0f
                ),
                radius = starRadius * 2.0f,
                center = starScreen
            )
            // Black disc of the eclipsed core
            drawCircle(
                color = Color.Black,
                radius = starRadius * 0.9f,
                center = starScreen
            )
            drawCircle(
                color = CoronaAmber,
                radius = starRadius * 0.92f,
                center = starScreen,
                style = Stroke(width = 2.5.dp.toPx())
            )

            // Celestial Bodies and Wrecks
            for (i in entities.indices) {
                val entity = entities[i]
                val ex = center.x + (entity.x - curPosX) * zoom
                val ey = center.y + (entity.y - curPosY) * zoom
                val isNearby = nearbyEntity?.id == entity.id

                val entityColor = when (entity.type) {
                    CelestialType.DYING_STAR -> CoronaAmber
                    CelestialType.DERELICT_STATION -> TelemetryCyan
                    CelestialType.GHOST_FREIGHTER -> CautionYellow
                    CelestialType.ASTEROID_FIELD -> HullSilver
                    CelestialType.RESEARCH_LAB -> OxygenTeal
                    CelestialType.QUANTUM_BEACON -> SanityPurple
                    CelestialType.EXODUS_ARK -> TerminalGreen
                }

                val baseRadius = (entity.radius * 0.5f * zoom).coerceAtLeast(13f)
                val entityPos = Offset(ex, ey)

                drawCircle(
                    color = if (entity.salvaged) entityColor.copy(alpha = 0.35f) else entityColor,
                    radius = baseRadius,
                    center = entityPos,
                    style = if (entity.salvaged) Stroke(width = 1.5.dp.toPx()) else androidx.compose.ui.graphics.drawscope.Fill
                )

                if (!entity.salvaged && entity.type != CelestialType.DYING_STAR) {
                    drawCircle(
                        color = entityColor.copy(alpha = if (isNearby) 0.85f else 0.35f),
                        radius = baseRadius + (if (isNearby) 14f else 8f),
                        center = entityPos,
                        style = Stroke(width = if (isNearby) 2.dp.toPx() else 1.dp.toPx())
                    )
                }

                // Offscreen directional indicator
                val isOffscreen = ex < 16f || ex > canvasW - 16f || ey < 120f || ey > canvasH - 240f
                if (isOffscreen && entity.type != CelestialType.DYING_STAR && !entity.salvaged) {
                    val angle = atan2(ey - center.y, ex - center.x)
                    val edgeDist = min(canvasW * 0.44f, canvasH * 0.28f)
                    val edgeX = center.x + cos(angle) * edgeDist
                    val edgeY = center.y + sin(angle) * edgeDist

                    drawCircle(
                        color = entityColor.copy(alpha = 0.75f),
                        radius = 4.5.dp.toPx(),
                        center = Offset(edgeX, edgeY)
                    )
                }
            }

            // Player Ship at Center with Attitude Rotation
            rotate(degrees = viewModel.angleDeg, pivot = center) {
                // Thruster plume
                if (viewModel.isThrustingForward) {
                    drawPath(
                        path = flamePath,
                        brush = Brush.horizontalGradient(
                            listOf(TelemetryCyan, CoronaAmber, Color.Transparent),
                            startX = -10f,
                            endX = -46f
                        )
                    )
                }
                if (viewModel.isThrustingRetro) {
                    drawCircle(
                        color = TelemetryCyan.copy(alpha = 0.7f),
                        radius = 5.dp.toPx(),
                        center = Offset(18f, 0f)
                    )
                }

                // Hull
                drawPath(path = shipPath, color = CockpitSurfaceVariant)
                drawPath(path = shipPath, color = TelemetryCyan, style = Stroke(width = 2.dp.toPx()))

                // Canopy
                drawCircle(color = CoronaAmber, radius = 3.5.dp.toPx(), center = Offset(6f, 0f))
            }

            // Velocity Vector line
            val vx = viewModel.velX
            val vy = viewModel.velY
            if (abs(vx) > 0.4f || abs(vy) > 0.4f) {
                drawLine(
                    color = TerminalGreen.copy(alpha = 0.7f),
                    start = center,
                    end = Offset(center.x + vx * 1.6f, center.y + vy * 1.6f),
                    strokeWidth = 2.dp.toPx()
                )
            }
        }

        // Top UI: Mission Phase and Telemetry Bar (Vertical Compact Fit)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = 8.dp, start = 12.dp, end = 12.dp)
        ) {
            // Objective Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showObjectiveDetails = !showObjectiveDetails },
                color = CockpitSurface.copy(alpha = 0.92f),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderGlow)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = phase.title.uppercase(),
                            color = CoronaAmber,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = phase.objective,
                            color = TerminalText,
                            fontSize = 11.sp,
                            maxLines = if (showObjectiveDetails) 3 else 1
                        )
                    }
                    Icon(
                        imageVector = if (showObjectiveDetails) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Expand Objective",
                        tint = CoronaAmber,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Vital Metrics Bar (Optimized for vertical screens)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                TelemetryPill(
                    label = "O2",
                    value = "${vitals.oxygen.toInt()}%",
                    accent = if (vitals.oxygen < 25f) HazardRed else OxygenTeal,
                    isCritical = vitals.oxygen < 25f,
                    modifier = Modifier.weight(1f)
                )
                TelemetryPill(
                    label = "PWR",
                    value = "${vitals.power.toInt()}%",
                    accent = if (vitals.power < 20f) HazardRed else CoronaAmber,
                    isCritical = vitals.power < 20f,
                    modifier = Modifier.weight(1f)
                )
                TelemetryPill(
                    label = "CASCO",
                    value = "${vitals.hullIntegrity.toInt()}%",
                    accent = if (vitals.hullIntegrity < 30f) HazardRed else HullSilver,
                    isCritical = vitals.hullIntegrity < 30f,
                    modifier = Modifier.weight(1f)
                )
                TelemetryPill(
                    label = "TEMP",
                    value = "${vitals.temperature.toInt()}°C",
                    accent = if (vitals.temperature < 5f || vitals.temperature > 40f) HazardRed else TelemetryCyan,
                    isCritical = vitals.temperature < 5f || vitals.temperature > 40f,
                    modifier = Modifier.weight(1f)
                )
                TelemetryPill(
                    label = "RAD",
                    value = String.format("%.1f", vitals.radiation),
                    accent = if (vitals.radiation > 4f) RadiationToxic else TerminalGreen,
                    isCritical = vitals.radiation > 4f,
                    modifier = Modifier.weight(1f)
                )
                TelemetryPill(
                    label = "SAN",
                    value = "${vitals.sanity.toInt()}%",
                    accent = if (vitals.sanity < 25f) HazardRed else SanityPurple,
                    isCritical = vitals.sanity < 25f,
                    modifier = Modifier.weight(1f)
                )
            }

            // Alert banner
            activeAlert?.let { alertMsg ->
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.dismissAlert() },
                    color = HazardRedDim.copy(alpha = 0.95f),
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, HazardRed)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Alert",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = alertMsg,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss",
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // EVA / Docking Banner overlay
        AnimatedVisibility(
            visible = nearbyEntity != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = 80.dp)
                .padding(horizontal = 16.dp)
        ) {
            nearbyEntity?.let { entity ->
                Surface(
                    color = CockpitSurface.copy(alpha = 0.96f),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(2.dp, TelemetryCyan)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.GpsFixed,
                                contentDescription = null,
                                tint = TelemetryCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "EN RANGO DE SALVAMENTO",
                                color = TelemetryCyan,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Text(
                            text = entity.name,
                            color = TerminalText,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(vertical = 3.dp)
                        )

                        Text(
                            text = "Peligro: ${entity.dangerLevel} // Recompensa: +${entity.rewardScrap} Ti, +${entity.rewardCircuits} Circ",
                            color = TerminalTextMuted,
                            fontSize = 10.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = { viewModel.startEva(entity) },
                            colors = ButtonDefaults.buttonColors(containerColor = CoronaAmber),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("cockpit_eva_action_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Construction,
                                contentDescription = null,
                                tint = VoidBlack,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (entity.type == CelestialType.EXODUS_ARK) "ACOPLARSE AL ARCA" else "INICIAR EXCURSIÓN EVA",
                                color = VoidBlack,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }

        // Bottom Flight Deck & Tactical Navigation Controls (Vertical ergonomic design)
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, CockpitSurface.copy(alpha = 0.95f), VoidBlack)
                    )
                )
                .navigationBarsPadding()
                .padding(bottom = 12.dp, start = 12.dp, end = 12.dp)
        ) {
            // Quick Navigation Hub Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                DeckMiniButton(
                    label = "TALLER",
                    icon = Icons.Default.Build,
                    color = CoronaAmber,
                    testTag = "cockpit_workshop_nav",
                    onClick = { viewModel.navigateTo(ActiveScreen.WORKSHOP) }
                )
                DeckMiniButton(
                    label = "MAPA",
                    icon = Icons.Default.Explore,
                    color = TelemetryCyan,
                    testTag = "cockpit_starmap_nav",
                    onClick = { viewModel.navigateTo(ActiveScreen.STAR_MAP) }
                )
                DeckMiniButton(
                    label = "CÓDICE",
                    icon = Icons.Default.LibraryBooks,
                    color = OxygenTeal,
                    testTag = "cockpit_codex_nav",
                    onClick = { viewModel.navigateTo(ActiveScreen.CODEX_LOGS) }
                )
                DeckMiniButton(
                    label = "PARCHEAR",
                    icon = Icons.Default.Shield,
                    color = HullSilver,
                    testTag = "cockpit_patch_hull",
                    onClick = { viewModel.repairHullWithScrap() }
                )
                DeckMiniButton(
                    label = "RACIÓN",
                    icon = Icons.Default.LocalHospital,
                    color = TerminalGreen,
                    testTag = "cockpit_ration_btn",
                    onClick = { viewModel.consumeRations() }
                )
                DeckMiniButton(
                    label = "MENÚ",
                    icon = Icons.Default.Home,
                    color = TerminalTextMuted,
                    testTag = "cockpit_menu_nav",
                    onClick = { viewModel.navigateTo(ActiveScreen.MAIN_MENU) }
                )
            }

            // Ergonomic Flight Controls for Vertical Form Factor
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Cluster: Yaw Steering and Inertial Brake
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FlightTouchPad(
                        icon = Icons.Default.RotateLeft,
                        label = "IZQ",
                        testTag = "control_turn_left",
                        onTouchChange = { pressed -> viewModel.isRotatingLeft = pressed }
                    )
                    FlightTouchPad(
                        icon = Icons.Default.RotateRight,
                        label = "DER",
                        testTag = "control_turn_right",
                        onTouchChange = { pressed -> viewModel.isRotatingRight = pressed }
                    )
                    FlightTouchPad(
                        icon = Icons.Default.PanTool,
                        label = "FRENO",
                        testTag = "control_rcs_brake",
                        accent = CautionYellow,
                        onTouchChange = { pressed -> viewModel.isRcsBraking = pressed }
                    )
                }

                // Fuel Gauge in Center
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Text(
                        text = "RCS",
                        color = TerminalTextMuted,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    LinearProgressIndicator(
                        progress = { vitals.fuel / 100f },
                        color = if (vitals.fuel < 20f) HazardRed else CoronaAmber,
                        trackColor = CockpitSurfaceVariant,
                        modifier = Modifier
                            .width(46.dp)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                    )
                    Text(
                        text = "${vitals.fuel.toInt()}%",
                        color = TerminalText,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Right Cluster: Retro Thruster and Main Forward Impulse
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FlightTouchPad(
                        icon = Icons.Default.ArrowDownward,
                        label = "RETRO",
                        testTag = "control_thrust_retro",
                        accent = TelemetryCyan,
                        onTouchChange = { pressed -> viewModel.isThrustingRetro = pressed }
                    )
                    FlightTouchPad(
                        icon = Icons.Default.ArrowUpward,
                        label = "IMPULSO",
                        testTag = "control_thrust_forward",
                        accent = CoronaAmber,
                        isLarge = true,
                        onTouchChange = { pressed -> viewModel.isThrustingForward = pressed }
                    )
                }
            }
        }
    }
}

@Composable
fun TelemetryPill(
    label: String,
    value: String,
    accent: Color,
    isCritical: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = if (isCritical) HazardRedDim.copy(alpha = 0.5f) else CockpitSurface.copy(alpha = 0.85f),
        shape = RoundedCornerShape(6.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isCritical) HazardRed else CardBorderGlow)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 4.dp, horizontal = 2.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                color = accent,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = value,
                color = TerminalText,
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
fun DeckMiniButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .testTag(testTag),
        color = CockpitSurfaceVariant.copy(alpha = 0.9f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                color = TerminalText,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun FlightTouchPad(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    testTag: String,
    accent: Color = TelemetryCyan,
    isLarge: Boolean = false,
    onTouchChange: (Boolean) -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .size(if (isLarge) 54.dp else 48.dp)
            .clip(CircleShape)
            .pointerInteropFilter { event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        isPressed = true
                        onTouchChange(true)
                        true
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        isPressed = false
                        onTouchChange(false)
                        true
                    }
                    else -> false
                }
            }
            .testTag(testTag),
        color = if (isPressed) accent.copy(alpha = 0.35f) else CockpitSurfaceVariant,
        border = androidx.compose.foundation.BorderStroke(
            width = if (isPressed) 2.dp else 1.dp,
            color = if (isPressed) accent else accent.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isPressed) Color.White else accent,
                modifier = Modifier.size(if (isLarge) 24.dp else 20.dp)
            )
            Text(
                text = label,
                color = if (isPressed) Color.White else TerminalTextMuted,
                fontSize = 7.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
