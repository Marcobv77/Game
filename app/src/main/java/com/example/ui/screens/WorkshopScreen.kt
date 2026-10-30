package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ShipModule
import com.example.ui.theme.*
import com.example.viewmodel.ActiveScreen
import com.example.viewmodel.EclipseGameViewModel

@Composable
fun WorkshopScreen(viewModel: EclipseGameViewModel) {
    BackHandler {
        viewModel.navigateTo(ActiveScreen.COCKPIT)
    }

    val inventory by viewModel.inventory.collectAsState()
    val modules by viewModel.modules.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VoidBlack)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(16.dp)
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.navigateTo(ActiveScreen.COCKPIT) },
                modifier = Modifier.testTag("workshop_back_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Volver al Puente",
                    tint = TelemetryCyan
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "TALLER MODULAR DE LA NAVE",
                    color = TerminalText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Cápsula Eclipse-07 // Protocolo de Reconstrucción",
                    color = TerminalTextMuted,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Inventory Stockpile Grid
        Text(
            text = "INVENTARIO DE MATERIALES RECUPERADOS",
            color = CoronaAmber,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ResourceBox("Titanio", "${inventory.titaniumScrap}", HullSilver, Modifier.weight(1f))
            ResourceBox("Circuitos", "${inventory.quantumCircuits}", TelemetryCyan, Modifier.weight(1f))
            ResourceBox("Litio", "${inventory.lithiumCells}", CautionYellow, Modifier.weight(1f))
            ResourceBox("Plomo", "${inventory.leadPlates}", RadiationToxic, Modifier.weight(1f))
            ResourceBox("Hipernúcleos", "${inventory.hyperCores}", CoronaAmber, Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Modules Blueprint List
        Text(
            text = "PLANOS DE MÓDULOS TECNOLÓGICOS",
            color = TelemetryCyan,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(modules, key = { it.id }) { mod ->
                ModuleCraftCard(
                    module = mod,
                    canAfford = inventory.titaniumScrap >= mod.costTitanium &&
                            inventory.quantumCircuits >= mod.costCircuits &&
                            inventory.lithiumCells >= mod.costLithium &&
                            inventory.hyperCores >= mod.costSpecial,
                    onCraft = { viewModel.craftModule(mod.id) }
                )
            }
        }
    }
}

@Composable
fun ResourceBox(
    name: String,
    amount: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = CockpitSurfaceVariant,
        shape = RoundedCornerShape(6.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = name.uppercase(),
                color = color,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = amount,
                color = TerminalText,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
fun ModuleCraftCard(
    module: ShipModule,
    canAfford: Boolean,
    onCraft: () -> Unit
) {
    val borderColor = when {
        module.installed -> TerminalGreen
        canAfford -> CoronaAmber
        else -> CardBorderGlow
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = if (module.installed) TerminalGreen.copy(alpha = 0.2f) else TelemetryCyanDim.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "TIER ${module.tier}",
                                color = if (module.installed) TerminalGreen else TelemetryCyan,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = module.name,
                            color = TerminalText,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = module.description,
                        color = TerminalTextMuted,
                        fontSize = 11.sp
                    )
                }

                if (module.installed) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Instalado",
                        tint = TerminalGreen,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Cost Requirements & Craft Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CostTag("Ti", module.costTitanium, HullSilver)
                    CostTag("Circ", module.costCircuits, TelemetryCyan)
                    CostTag("Li", module.costLithium, CautionYellow)
                    if (module.costSpecial > 0) {
                        CostTag("Núcleo", module.costSpecial, CoronaAmber)
                    }
                }

                if (!module.installed) {
                    Button(
                        onClick = onCraft,
                        enabled = canAfford,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CoronaAmber,
                            disabledContainerColor = CockpitSurfaceVariant
                        ),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.testTag("craft_btn_${module.id}")
                    ) {
                        Text(
                            text = if (canAfford) "ENSAMBLAR" else "FALTAN RECURSOS",
                            color = if (canAfford) VoidBlack else TerminalTextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                } else {
                    Text(
                        text = "OPERATIVO",
                        color = TerminalGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
fun CostTag(label: String, cost: Int, color: Color) {
    Surface(
        color = CockpitSurfaceVariant,
        shape = RoundedCornerShape(4.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Text(
            text = "$label: $cost",
            color = color,
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}
