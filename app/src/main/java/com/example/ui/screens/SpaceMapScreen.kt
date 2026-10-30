package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Sector
import com.example.ui.theme.*
import com.example.viewmodel.ActiveScreen
import com.example.viewmodel.EclipseGameViewModel

@Composable
fun SpaceMapScreen(viewModel: EclipseGameViewModel) {
    BackHandler {
        viewModel.navigateTo(ActiveScreen.COCKPIT)
    }

    val sectors by viewModel.sectors.collectAsState()
    val modules by viewModel.modules.collectAsState()
    val hasWarp = modules.find { it.id == "mod_warp_drive" }?.installed == true

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
                modifier = Modifier.testTag("starmap_back_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Volver",
                    tint = TelemetryCyan
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "MAPA TÁCTICO DEL SISTEMA HELIOS-OMEGA",
                    color = TerminalText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Telemetría Orbital y Proyección de Distorsión",
                    color = TerminalTextMuted,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Orbital Schematic Canvas
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            color = CockpitSurface,
            shape = RoundedCornerShape(10.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderGlow)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width * 0.5f, size.height * 0.5f)

                // Dying Star at Center
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(CoronaAmber, CoronaAmberDim, Color.Transparent),
                        center = center,
                        radius = 48.dp.toPx()
                    ),
                    radius = 48.dp.toPx(),
                    center = center
                )
                drawCircle(
                    color = VoidBlack,
                    radius = 16.dp.toPx(),
                    center = center
                )

                // Concentric orbital rings for sectors
                val r1 = 38.dp.toPx()
                val r2 = 58.dp.toPx()
                val r3 = 78.dp.toPx()
                val r4 = 98.dp.toPx()

                drawCircle(color = TelemetryCyan.copy(alpha = 0.35f), radius = r1, center = center, style = Stroke(1.dp.toPx()))
                drawCircle(color = CautionYellow.copy(alpha = 0.35f), radius = r2, center = center, style = Stroke(1.dp.toPx()))
                drawCircle(color = HazardRed.copy(alpha = 0.35f), radius = r3, center = center, style = Stroke(1.dp.toPx()))
                drawCircle(color = OxygenTeal.copy(alpha = 0.35f), radius = r4, center = center, style = Stroke(1.dp.toPx()))

                // Player ship dot
                drawCircle(
                    color = TerminalGreen,
                    radius = 4.dp.toPx(),
                    center = Offset(center.x + 32.dp.toPx(), center.y - 20.dp.toPx())
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Sector List
        Text(
            text = "SECTORES DE NAVEGACIÓN Y AMENAZA",
            color = CoronaAmber,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(sectors, key = { it.id }) { sector ->
                SectorCard(
                    sector = sector,
                    hasWarp = hasWarp,
                    onJump = { viewModel.jumpSector(sector.id) }
                )
            }
        }
    }
}

@Composable
fun SectorCard(
    sector: Sector,
    hasWarp: Boolean,
    onJump: () -> Unit
) {
    val isLocked = sector.requiredDriveTier > 0 && !hasWarp
    val borderColor = when {
        sector.isCurrent -> TerminalGreen
        isLocked -> CardBorderGlow
        else -> TelemetryCyan
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = CockpitSurface,
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = sector.name,
                        color = if (sector.isCurrent) TerminalGreen else TerminalText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = sector.description,
                        color = TerminalTextMuted,
                        fontSize = 11.sp
                    )
                }

                if (sector.isCurrent) {
                    Surface(
                        color = TerminalGreen.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "SECTOR ACTUAL",
                            color = TerminalGreen,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        color = CockpitSurfaceVariant,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "Amenaza: ${sector.threatLevel}",
                            color = HazardRed,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Surface(
                        color = CockpitSurfaceVariant,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "Rad x${sector.radiationMultiplier}",
                            color = RadiationToxic,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                if (!sector.isCurrent) {
                    Button(
                        onClick = onJump,
                        enabled = !isLocked,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TelemetryCyan,
                            disabledContainerColor = CockpitSurfaceVariant
                        ),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.testTag("jump_btn_${sector.id}")
                    ) {
                        Text(
                            text = if (isLocked) "DISTORSIÓN BLOQUEADA" else "SALTO VECTORIAL",
                            color = if (isLocked) TerminalTextMuted else VoidBlack,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}
