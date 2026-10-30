package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.ActiveScreen
import com.example.viewmodel.EclipseGameViewModel

@Composable
fun SettingsScreen(viewModel: EclipseGameViewModel) {
    BackHandler {
        viewModel.navigateTo(ActiveScreen.MAIN_MENU)
    }

    var soundOn by remember { mutableStateOf(viewModel.audioEngine.soundEnabled) }
    var hapticsOn by remember { mutableStateOf(viewModel.audioEngine.hapticsEnabled) }
    var droneVol by remember { mutableStateOf(viewModel.audioEngine.ambientDroneVolume) }
    var showResetDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VoidBlack)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.navigateTo(ActiveScreen.MAIN_MENU) },
                modifier = Modifier.testTag("settings_back_btn")
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
                    text = "CONFIGURACIÓN Y DIAGNÓSTICO",
                    color = TerminalText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Ajustes de Interfaz y Motores de Síntesis",
                    color = TerminalTextMuted,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Sound Toggle
        SettingToggleCard(
            title = "SONIDO Y SÍNTESIS PROCEDURAL",
            subtitle = "Drones cósmicos, motores RCS y alarmas sintetizadas",
            icon = Icons.Default.VolumeUp,
            isChecked = soundOn,
            onCheckedChange = {
                soundOn = it
                viewModel.audioEngine.soundEnabled = it
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Ambient Drone Volume Slider
        if (soundOn) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = CockpitSurface,
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderGlow)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "VOLUMEN DE DRONE DEL VACÍO: ${(droneVol * 100).toInt()}%",
                        color = CoronaAmber,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Slider(
                        value = droneVol,
                        onValueChange = {
                            droneVol = it
                            viewModel.audioEngine.ambientDroneVolume = it
                        },
                        valueRange = 0f..1f,
                        colors = SliderDefaults.colors(
                            thumbColor = CoronaAmber,
                            activeTrackColor = CoronaAmber
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Haptics Toggle
        SettingToggleCard(
            title = "RESPUESTA HÁPTICA",
            subtitle = "Vibración táctil en colisiones, alarmas y soplete EVA",
            icon = Icons.Default.Vibration,
            isChecked = hapticsOn,
            onCheckedChange = {
                hapticsOn = it
                viewModel.audioEngine.hapticsEnabled = it
            }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Manual de Supervivencia Hard Sci-Fi
        Text(
            text = "MANUAL DE SUPERVIVENCIA Y FÍSICA ESPACIAL",
            color = TelemetryCyan,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        SurvivalTipCard(
            title = "Inercia Newtoniana",
            content = "En el vacío no hay fricción aerodinámica. Un impulso de aceleración mantendrá tu velocidad constante hasta que quemes en sentido contrario o actives el Freno RCS."
        )

        Spacer(modifier = Modifier.height(8.dp))

        SurvivalTipCard(
            title = "Atracción Gravitacional de Helios-Omega",
            content = "La estrella agonizante en el centro (0,0) ejerce gravedad constante. Si te acercas demasiado, el calor quemará tu casco y la radiación destruirá tus células."
        )

        Spacer(modifier = Modifier.height(8.dp))

        SurvivalTipCard(
            title = "Reconstrucción Modular",
            content = "Los planos del Taller son tu única salvación. Ensambla primero el Filtro de Aire C-12 para conservar oxígeno y los Paneles Solares para no quedarte a oscuras."
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Danger Zone: Reset
        Button(
            onClick = { showResetDialog = true },
            colors = ButtonDefaults.buttonColors(containerColor = HazardRedDim),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("reset_save_btn")
        ) {
            Icon(
                imageVector = Icons.Default.DeleteForever,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "REINICIAR DATOS DE EXPEDICIÓN",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = {
                Text(
                    text = "¿ELIMINAR PROGRESO?",
                    color = HazardRed,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            },
            text = {
                Text(
                    text = "Esto borrará tu cápsula, módulos y registros recuperados. Comenzarás una expedición desde cero en el cataclismo de Aethelgard.",
                    color = TerminalText,
                    fontSize = 12.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showResetDialog = false
                        viewModel.startNewGame(viewModel.difficulty.value)
                        viewModel.navigateTo(ActiveScreen.MAIN_MENU)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HazardRed)
                ) {
                    Text("BORRAR DEFINITIVAMENTE", color = Color.White, fontSize = 10.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("CANCELAR", color = TerminalTextMuted)
                }
            },
            containerColor = CockpitSurface
        )
    }
}

@Composable
fun SettingToggleCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = CockpitSurface,
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderGlow)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = TelemetryCyan,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = title,
                        color = TerminalText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = subtitle,
                        color = TerminalTextMuted,
                        fontSize = 10.sp
                    )
                }
            }

            Switch(
                checked = isChecked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = CoronaAmber,
                    checkedTrackColor = CoronaAmberDim
                )
            )
        }
    }
}

@Composable
fun SurvivalTipCard(title: String, content: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = CockpitSurface,
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderGlow)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                color = CoronaAmber,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = content,
                color = TerminalText,
                fontSize = 11.sp,
                lineHeight = 16.sp
            )
        }
    }
}
