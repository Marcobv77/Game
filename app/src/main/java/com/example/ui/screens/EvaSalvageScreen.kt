package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.ActiveScreen
import com.example.viewmodel.EclipseGameViewModel
import kotlin.math.sin

@Composable
fun EvaSalvageScreen(viewModel: EclipseGameViewModel) {
    BackHandler {
        viewModel.navigateTo(ActiveScreen.COCKPIT)
    }

    val target by viewModel.evaTarget.collectAsState()
    val cuttingProgress by viewModel.evaCuttingProgress.collectAsState()
    val isTuned by viewModel.evaFrequencyTuned.collectAsState()
    val targetFreq by viewModel.evaTargetFrequency.collectAsState()
    val currentFreq by viewModel.evaCurrentFrequency.collectAsState()

    val infiniteAnim = rememberInfiniteTransition(label = "EvaWave")
    val waveOffset by infiniteAnim.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Wave"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VoidBlack)
    ) {
        // First-person Visor HUD Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Visor Curved Frame Vignette
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.Transparent,
                        VoidBlack.copy(alpha = 0.5f),
                        VoidBlack.copy(alpha = 0.95f)
                    ),
                    center = Offset(w * 0.5f, h * 0.5f),
                    radius = w * 0.75f
                )
            )

            // Helmet HUD Reticle Corner brackets
            val bracketSize = 24.dp.toPx()
            val inset = 36.dp.toPx()
            val strokePx = 2.dp.toPx()

            // Top-left
            drawLine(TelemetryCyan, Offset(inset, inset), Offset(inset + bracketSize, inset), strokePx)
            drawLine(TelemetryCyan, Offset(inset, inset), Offset(inset, inset + bracketSize), strokePx)

            // Top-right
            drawLine(TelemetryCyan, Offset(w - inset, inset), Offset(w - inset - bracketSize, inset), strokePx)
            drawLine(TelemetryCyan, Offset(w - inset, inset), Offset(w - inset, inset + bracketSize), strokePx)

            // Bottom-left
            drawLine(TelemetryCyan, Offset(inset, h - inset), Offset(inset + bracketSize, h - inset), strokePx)
            drawLine(TelemetryCyan, Offset(inset, h - inset), Offset(inset, h - inset - bracketSize), strokePx)

            // Bottom-right
            drawLine(TelemetryCyan, Offset(w - inset, h - inset), Offset(w - inset - bracketSize, h - inset), strokePx)
            drawLine(TelemetryCyan, Offset(w - inset, h - inset), Offset(w - inset, h - inset - bracketSize), strokePx)

            // Central Laser Target Reticle
            val center = Offset(w * 0.5f, h * 0.35f)
            drawCircle(
                color = if (cuttingProgress >= 1f) TerminalGreen else CoronaAmber,
                radius = 48.dp.toPx(),
                center = center,
                style = Stroke(2.dp.toPx())
            )
            drawCircle(
                color = if (cuttingProgress >= 1f) TerminalGreen.copy(alpha = 0.3f) else CoronaAmber.copy(alpha = 0.2f),
                radius = 48.dp.toPx() * cuttingProgress,
                center = center
            )
        }

        // Top Telemetry Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = CockpitSurfaceVariant,
                    shape = RoundedCornerShape(4.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, TelemetryCyan)
                ) {
                    Text(
                        text = "EXCURSIÓN EXTRAVEHICULAR (EVA) // TRAJE MK-IV",
                        color = TelemetryCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                IconButton(
                    onClick = { viewModel.navigateTo(ActiveScreen.COCKPIT) },
                    modifier = Modifier.testTag("eva_abort_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Abortar EVA",
                        tint = HazardRed
                    )
                }
            }

            Text(
                text = target?.name ?: "Estructura a la Deriva",
                color = TerminalText,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // Bottom Interactive Station
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(16.dp),
            color = CockpitSurface.copy(alpha = 0.95f),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderGlow)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Step 1: Laser Torch Cutting
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "1. CORTE TÉRMICO DE COMPUERTA",
                        color = if (cuttingProgress >= 1f) TerminalGreen else CoronaAmber,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "${(cuttingProgress * 100).toInt()}%",
                        color = if (cuttingProgress >= 1f) TerminalGreen else TerminalText,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                LinearProgressIndicator(
                    progress = { cuttingProgress },
                    color = if (cuttingProgress >= 1f) TerminalGreen else CoronaAmber,
                    trackColor = CockpitSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (cuttingProgress < 1f) {
                    Button(
                        onClick = { viewModel.cutLaserTick() },
                        colors = ButtonDefaults.buttonColors(containerColor = CoronaAmber),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("eva_laser_cut_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FlashOn,
                            contentDescription = null,
                            tint = VoidBlack,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ACTIVAR SOPLETE DE CORTE DE PLASMA",
                            color = VoidBlack,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                } else {
                    // Step 2: Signal / Frequency Calibration
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "2. SINTONIZAR FRECUENCIA DE NÚCLEO",
                            color = if (isTuned) TerminalGreen else TelemetryCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "${currentFreq} Hz / Meta: ~${targetFreq} Hz",
                            color = if (isTuned) TerminalGreen else CautionYellow,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Waveform visualizer
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp),
                        color = CockpitSurfaceVariant,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val midY = size.height * 0.5f
                            val points = 60
                            var prevX = 0f
                            var prevY = midY
                            for (i in 0..points) {
                                val x = size.width * (i.toFloat() / points)
                                val freqRatio = currentFreq / 400f
                                val y = midY + sin((i * 0.35f * freqRatio) + waveOffset) * 12.dp.toPx()
                                if (i > 0) {
                                    drawLine(
                                        color = if (isTuned) TerminalGreen else TelemetryCyan,
                                        start = Offset(prevX, prevY),
                                        end = Offset(x, y),
                                        strokeWidth = 2.dp.toPx()
                                    )
                                }
                                prevX = x
                                prevY = y
                            }
                        }
                    }

                    Slider(
                        value = currentFreq.toFloat(),
                        onValueChange = { viewModel.tuneFrequency(it.toInt()) },
                        valueRange = 250f..650f,
                        colors = SliderDefaults.colors(
                            thumbColor = if (isTuned) TerminalGreen else TelemetryCyan,
                            activeTrackColor = if (isTuned) TerminalGreen else TelemetryCyan
                        ),
                        modifier = Modifier.testTag("eva_frequency_slider")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { viewModel.completeEvaSalvage() },
                        enabled = isTuned,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TerminalGreen,
                            disabledContainerColor = CockpitSurfaceVariant
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("eva_collect_salvage_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Inventory2,
                            contentDescription = null,
                            tint = if (isTuned) VoidBlack else TerminalTextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isTuned) "EXTRAER RECURSOS Y REGISTROS" else "SINTONIZA LA FRECUENCIA",
                            color = if (isTuned) VoidBlack else TerminalTextMuted,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}
