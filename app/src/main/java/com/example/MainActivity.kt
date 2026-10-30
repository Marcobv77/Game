package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.VoidBlack
import com.example.viewmodel.ActiveScreen
import com.example.viewmodel.EclipseGameViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val gameViewModel: EclipseGameViewModel = viewModel()
                val currentScreen by gameViewModel.currentScreen.collectAsState()

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = VoidBlack
                ) {
                    Crossfade(
                        targetState = currentScreen,
                        label = "ScreenTransition"
                    ) { screen ->
                        when (screen) {
                            ActiveScreen.MAIN_MENU -> MainMenuScreen(gameViewModel)
                            ActiveScreen.COCKPIT -> FlightCockpitScreen(gameViewModel)
                            ActiveScreen.WORKSHOP -> WorkshopScreen(gameViewModel)
                            ActiveScreen.STAR_MAP -> SpaceMapScreen(gameViewModel)
                            ActiveScreen.CODEX_LOGS -> CodexLogsScreen(gameViewModel)
                            ActiveScreen.EVA_SALVAGE -> EvaSalvageScreen(gameViewModel)
                            ActiveScreen.SETTINGS -> SettingsScreen(gameViewModel)
                            ActiveScreen.ENDING_SCREEN -> EndingScreen(gameViewModel)
                        }
                    }
                }
            }
        }
    }
}
