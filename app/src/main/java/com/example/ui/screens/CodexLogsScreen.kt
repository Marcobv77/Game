package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SurvivorLog
import com.example.ui.theme.*
import com.example.viewmodel.ActiveScreen
import com.example.viewmodel.EclipseGameViewModel
import kotlin.math.sin

@Composable
fun CodexLogsScreen(viewModel: EclipseGameViewModel) {
    BackHandler {
        viewModel.navigateTo(ActiveScreen.COCKPIT)
    }

    val logs by viewModel.logs.collectAsState()
    var selectedLog by remember { mutableStateOf(logs.firstOrNull { it.unlocked } ?: logs.first()) }
    var isPlayingAudio by remember { mutableStateOf(false) }

    val infiniteAnim = rememberInfiniteTransition(label = "Waveform")
    val phaseAnim by infiniteAnim.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Phase"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VoidBlack)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.navigateTo(ActiveScreen.COCKPIT) },
                modifier = Modifier.testTag("codex_back_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Volver",
                    tint = OxygenTeal
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "CÓDICE DE TRANSMISIONES Y BITÁCORAS",
                    color = TerminalText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Ecos de los Caídos // Colonia Aethelgard-4",
                    color = TerminalTextMuted,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Selected Log Detailed Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.1f),
            color = CockpitSurface,
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderGlow)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp)
            ) {
                if (selectedLog.unlocked) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = selectedLog.title,
                                color = CoronaAmber,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "${selectedLog.author} // ${selectedLog.role}",
                                color = TelemetryCyan,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        IconButton(
                            onClick = {
                                isPlayingAudio = !isPlayingAudio
                                viewModel.audioEngine.triggerUiBlip()
                            },
                            modifier = Modifier.testTag("play_audio_btn")
                        ) {
                            Icon(
                                imageVector = if (isPlayingAudio) Icons.Default.StopCircle else Icons.Default.PlayCircleFilled,
                                contentDescription = "Reproducir Audio",
                                tint = CoronaAmber,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    // Audio Waveform Visualization
                    if (isPlayingAudio) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(36.dp)
                                .padding(vertical = 4.dp),
                            color = CockpitSurfaceVariant,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val midY = size.height * 0.5f
                                for (i in 0..40) {
                                    val x = size.width * (i / 40f)
                                    val amp = (sin(i * 0.45f + phaseAnim) * 10.dp.toPx()).coerceAtLeast(2f)
                                    drawLine(
                                        color = CoronaAmber,
                                        start = Offset(x, midY - amp),
                                        end = Offset(x, midY + amp),
                                        strokeWidth = 2.dp.toPx()
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = selectedLog.audioTranscript,
                        color = TerminalText,
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        modifier = Modifier.weight(1f)
                    )

                    // Moral decision if available
                    selectedLog.moralPrompt?.let { prompt ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            color = CockpitSurfaceVariant,
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, TelemetryCyan.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "DECISIÓN MORAL // PROTOCOLO DE AISLAMIENTO",
                                    color = TelemetryCyan,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = prompt,
                                    color = TerminalText,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )

                                if (!selectedLog.resolved) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        selectedLog.choiceA?.let { chA ->
                                            Button(
                                                onClick = { viewModel.resolveMoralChoice(selectedLog.id, chA) },
                                                colors = ButtonDefaults.buttonColors(containerColor = CoronaAmberDim),
                                                shape = RoundedCornerShape(6.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text(chA, fontSize = 9.sp, color = TerminalText, maxLines = 1)
                                            }
                                        }
                                        selectedLog.choiceB?.let { chB ->
                                            Button(
                                                onClick = { viewModel.resolveMoralChoice(selectedLog.id, chB) },
                                                colors = ButtonDefaults.buttonColors(containerColor = CockpitSurface),
                                                shape = RoundedCornerShape(6.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text(chB, fontSize = 9.sp, color = TerminalTextMuted, maxLines = 1)
                                            }
                                        }
                                    }
                                } else {
                                    Text(
                                        text = "DECISIÓN ADOPTADA: ${selectedLog.chosenOption}",
                                        color = TerminalGreen,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Bloqueado",
                                tint = TerminalTextMuted,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "FRECUENCIA CIFRADA // DATOS PERDIDOS",
                                color = TerminalTextMuted,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "Realiza excursiones EVA en naves y estaciones abandonadas para recuperar este registro.",
                                color = TerminalTextMuted.copy(alpha = 0.7f),
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Logs Selector List
        Text(
            text = "ARCHIVOS RECUPERADOS DEL SISTEMA",
            color = OxygenTeal,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.9f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(logs, key = { it.id }) { log ->
                val isSelected = selectedLog.id == log.id
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedLog = log },
                    color = if (isSelected) CockpitSurfaceVariant else CockpitSurface,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSelected) CoronaAmber else if (log.unlocked) OxygenTeal.copy(alpha = 0.4f) else CardBorderGlow
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (log.unlocked) Icons.Default.Mic else Icons.Default.Lock,
                                contentDescription = null,
                                tint = if (log.unlocked) CoronaAmber else TerminalTextMuted,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (log.unlocked) log.title else "FRECUENCIA BLOQUEADA [??]",
                                    color = if (log.unlocked) TerminalText else TerminalTextMuted,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = if (log.unlocked) "${log.author} // ${log.starDate}" else "Cifrado cuántico desconocido",
                                    color = TerminalTextMuted,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        if (log.unlocked && log.resolved) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Resuelto",
                                tint = TerminalGreen,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
