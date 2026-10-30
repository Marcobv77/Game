package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.ActiveScreen
import com.example.viewmodel.EclipseGameViewModel
import com.example.viewmodel.EndingType

@Composable
fun EndingScreen(viewModel: EclipseGameViewModel) {
    BackHandler {
        viewModel.navigateTo(ActiveScreen.MAIN_MENU)
    }

    val ending by viewModel.endingType.collectAsState()
    val isVictory = ending == EndingType.VICTORY_ARK_ESCAPE

    val title = when (ending) {
        EndingType.VICTORY_ARK_ESCAPE -> "EL ARCA ÉXODO: SALTO INTERESTELAR"
        EndingType.DEATH_OXYGEN -> "ASFIXIA EN EL VACÍO"
        EndingType.DEATH_HULL_BREACH -> "DESCOMPRESIÓN EXPLOSIVA"
        EndingType.DEATH_RADIATION -> "TORMENTA CORONAL LETAL"
        EndingType.DEATH_VOID_SANITY -> "EL ABRAZO DE LA SOMBRA"
        null -> "EXPEDICIÓN FINALIZADA"
    }

    val description = when (ending) {
        EndingType.VICTORY_ARK_ESCAPE ->
            "Lograste ensamblar el Motor de Distorsión Gravitacional 'Eclipse Drift' y acoplarte a los restos operativos del Arca Éxodo-9. Mientras Helios-Omega es devorado por la oscuridad viva, los hiperpropulsores rasgan el tejido del espacio. El último hijo de Aethelgard-4 sobrevive."
        EndingType.DEATH_OXYGEN ->
            "El medidor de O2 cae a cero absoluto. Tu respiración se vuelve pesada y el visor se empaña de escarcha por última vez. La cápsula continúa su deriva inerte a través del silencio cósmico."
        EndingType.DEATH_HULL_BREACH ->
            "Una fractura estructural desgarra las placas de titanio. La presión de la cabina colapsa en un instante; tus herramientas y memorias son absorbidas hacia el vacío insondable."
        EndingType.DEATH_RADIATION ->
            "Las llamaradas de la estrella moribunda atravesaron el casco. La dosis acumulada de radiación gamma inutilizó tus sistemas y tu cuerpo. Solo queda ceniza estelar."
        EndingType.DEATH_VOID_SANITY ->
            "El aislamiento y las voces en las frecuencias muertas quebraron tu cordura. Desactivaste los sellos de seguridad pensando que la luz de la estrella te llamaba. El vacío te dio la bienvenida."
        null -> "La cápsula ha perdido el contacto."
    }

    val accentColor = if (isVictory) TerminalGreen else HazardRed

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VoidBlack)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = CockpitSurface,
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(2.dp, accentColor)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isVictory) "MISIÓN LOGRADA" else "TELEMETRÍA PERDIDA",
                    color = accentColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = title,
                    color = TerminalText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = description,
                    color = TerminalTextMuted,
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        viewModel.startNewGame(viewModel.difficulty.value)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("ending_retry_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        tint = VoidBlack,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "INICIAR NUEVA DERIVA",
                        color = VoidBlack,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = {
                        viewModel.navigateTo(ActiveScreen.MAIN_MENU)
                    },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("ending_menu_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = null,
                        tint = TerminalText,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "MENÚ PRINCIPAL",
                        color = TerminalText,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}
