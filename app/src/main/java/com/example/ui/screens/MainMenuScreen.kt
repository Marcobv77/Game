package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.GameDifficulty
import com.example.ui.theme.*
import com.example.viewmodel.ActiveScreen
import com.example.viewmodel.EclipseGameViewModel
import kotlin.random.Random

private data class CachedStar(val relX: Float, val relY: Float, val radius: Float, val alpha: Float)

@Composable
fun MainMenuScreen(viewModel: EclipseGameViewModel) {
    var showDifficultyDialog by remember { mutableStateOf(false) }
    val hasSave = remember { viewModel.hasSavedGame() }

    // Precomputed stars to completely eliminate per-frame GC allocations
    val cachedStars = remember {
        val rand = Random(42)
        List(60) {
            CachedStar(
                relX = rand.nextFloat(),
                relY = rand.nextFloat(),
                radius = rand.nextFloat() * 1.8f + 0.6f,
                alpha = rand.nextFloat() * 0.7f + 0.25f
            )
        }
    }

    // Coronal flare pulsation (smooth 60fps hardware accelerated)
    val infiniteTransition = rememberInfiniteTransition(label = "CoronaPulsing")
    val flareScale by infiniteTransition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "FlareAnim"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VoidBlack)
    ) {
        // Starfield & Eclipse Corona Background Canvas (Zero-allocation draw loop)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Cached Cosmic void dust
            for (i in cachedStars.indices) {
                val star = cachedStars[i]
                drawCircle(
                    color = Color.White.copy(alpha = star.alpha),
                    radius = star.radius,
                    center = Offset(star.relX * width, star.relY * height)
                )
            }

            // Central Eclipsed Star Corona Glow (positioned for vertical phone layout)
            val starCenter = Offset(width * 0.5f, height * 0.25f)
            val baseRadius = width * 0.28f * flareScale

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        CoronaAmber.copy(alpha = 0.45f),
                        CoronaAmberDim.copy(alpha = 0.2f),
                        TelemetryCyan.copy(alpha = 0.08f),
                        Color.Transparent
                    ),
                    center = starCenter,
                    radius = baseRadius * 1.8f
                ),
                radius = baseRadius * 1.8f,
                center = starCenter
            )

            // Dark Moon / Eclipse Body
            drawCircle(
                color = VoidBlack,
                radius = baseRadius * 0.85f,
                center = starCenter
            )

            // Coronal Rim
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        CoronaAmber.copy(alpha = 0.9f),
                        EclipseGold.copy(alpha = 0.6f),
                        Color.Transparent
                    ),
                    center = starCenter,
                    radius = baseRadius * 0.92f
                ),
                radius = baseRadius * 0.92f,
                center = starCenter
            )
        }

        // Hero Art Overlay at top with vertical gradient blending
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(290.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_eclipse_menu),
                contentDescription = "Eclipse Drift Hero View",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = 0.55f
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                VoidBlack.copy(alpha = 0.6f),
                                VoidBlack
                            )
                        )
                    )
            )
        }

        // Vertical Main Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 22.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Title & Lore Branding
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 28.dp)
            ) {
                Surface(
                    color = CockpitSurfaceVariant.copy(alpha = 0.85f),
                    shape = RoundedCornerShape(4.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, TelemetryCyan.copy(alpha = 0.6f))
                ) {
                    Text(
                        text = "SISTEMA HELIOS-OMEGA // SUPERVIVENCIA REAL",
                        color = TelemetryCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "ECLIPSE DRIFT",
                    color = TerminalText,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 3.sp,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "“En el vacío, la única luz es la que tú generas.”",
                    color = CoronaAmber,
                    fontSize = 13.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Vertical Navigation Action Cards
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (hasSave) {
                    MenuActionButton(
                        title = "CONTINUAR DERIVA",
                        subtitle = "Reanudar telemetría de cápsula Eclipse-07",
                        icon = Icons.Default.PlayArrow,
                        accentColor = CoronaAmber,
                        testTag = "menu_continue_btn",
                        onClick = { viewModel.continueGame() }
                    )
                }

                MenuActionButton(
                    title = "NUEVA EXPEDICIÓN",
                    subtitle = "Iniciar desde la cápsula de escape dañada",
                    icon = Icons.Default.RocketLaunch,
                    accentColor = TelemetryCyan,
                    testTag = "menu_new_game_btn",
                    onClick = { showDifficultyDialog = true }
                )

                MenuActionButton(
                    title = "REGISTROS Y CÓDICE",
                    subtitle = "Transmisiones interceptadas y diarios del Arca",
                    icon = Icons.Default.RecordVoiceOver,
                    accentColor = OxygenTeal,
                    testTag = "menu_codex_btn",
                    onClick = { viewModel.navigateTo(ActiveScreen.CODEX_LOGS) }
                )

                MenuActionButton(
                    title = "SISTEMAS Y OPCIONES",
                    subtitle = "Audio procesal, háptica y diagnóstico",
                    icon = Icons.Default.Tune,
                    accentColor = TerminalTextMuted,
                    testTag = "menu_settings_btn",
                    onClick = { viewModel.navigateTo(ActiveScreen.SETTINGS) }
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Footer telemetry
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "COLONIA AETHELGARD-4 // DESINTEGRADA",
                    color = HazardRed.copy(alpha = 0.8f),
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "PORTRAIT // OPTIMIZADO",
                    color = TerminalTextMuted,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }

    // Difficulty selection dialog
    if (showDifficultyDialog) {
        AlertDialog(
            onDismissRequest = { showDifficultyDialog = false },
            title = {
                Text(
                    text = "SELECCIÓN DE PROTOCOLO",
                    color = CoronaAmber,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Elige la dureza del vacío espacial:",
                        color = TerminalText,
                        fontSize = 13.sp
                    )

                    GameDifficulty.values().forEach { diff ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showDifficultyDialog = false
                                    viewModel.startNewGame(diff)
                                },
                            color = CockpitSurfaceVariant,
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderGlow)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = diff.label.uppercase(),
                                    color = if (diff == GameDifficulty.TERROR_COSMICO) HazardRed else TelemetryCyan,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = diff.description,
                                    color = TerminalTextMuted,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showDifficultyDialog = false }) {
                    Text("CANCELAR", color = TerminalTextMuted)
                }
            },
            containerColor = CockpitSurface
        )
    }
}

@Composable
fun MenuActionButton(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .testTag(testTag),
        color = CockpitSurface.copy(alpha = 0.95f),
        border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(accentColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                    .border(1.dp, accentColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = TerminalText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = subtitle,
                    color = TerminalTextMuted,
                    fontSize = 11.sp
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = accentColor.copy(alpha = 0.8f)
            )
        }
    }
}
