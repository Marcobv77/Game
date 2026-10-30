package com.example.model

enum class GameDifficulty(val label: String, val drainRate: Float, val description: String) {
    SUPERVIVIENTE("Superviviente", 1.0f, "Experiencia canónica hard sci-fi. Recursos limitados y amenazas constantes."),
    TERROR_COSMICO("Terror Cósmico", 1.6f, "Supervivencia implacable. El vacío devora tu cordura y el oxígeno no perdona."),
    EXPLORADOR("Explorador", 0.5f, "El vacío es hostil pero benevolente. Ideal para sumergirse en la narrativa y el lore.")
}

enum class GamePhase(val title: String, val objective: String) {
    FASE_1_CAPSULA(
        "Fase I: Deriva y Supervivencia",
        "Estabiliza la cápsula de escape Eclipse-07. Repara fugas y reúne 30 de Chatarra de Titanio."
    ),
    FASE_2_EXPLORACION(
        "Fase II: Reconstrucción Modular",
        "Construye Paneles Fotovoltaicos y el Escudo Radiológico para adentrarte en el cinturón de restos."
    ),
    FASE_3_TRIANGULACION(
        "Fase III: La Búsqueda del Arca",
        "Ensambla el Motor de Distorsión y localiza la señal del 'Arca Éxodo-9' en el sector profundo."
    ),
    FASE_4_EL_ECLIPSE(
        "Fase IV: El Abrazo del Eclipse",
        "La estrella se apaga. Llega al hiperportal del Arca o acepta tu destino en el vacío eterno."
    )
}

data class VitalStats(
    val oxygen: Float = 95f,       // 0..100 %
    val power: Float = 88f,        // 0..100 %
    val hullIntegrity: Float = 75f,// 0..100 %
    val temperature: Float = 19.5f,// Celsius
    val radiation: Float = 0.4f,   // mSv/h
    val sanity: Float = 90f,       // 0..100 %
    val fuel: Float = 60f          // Propelente RCS 0..100 %
)

data class Inventory(
    val titaniumScrap: Int = 12,
    val quantumCircuits: Int = 3,
    val lithiumCells: Int = 2,
    val leadPlates: Int = 1,
    val foodRations: Int = 6,
    val waterRations: Int = 8,
    val hyperCores: Int = 0
)

enum class ModuleType {
    AIR_RECYCLER, SOLAR_PANEL, LEAD_SHIELD, ION_THRUSTER, THERMAL_COIL, GRAVITY_STABILIZER, FUSION_CORE, WARP_DRIVE
}

data class ShipModule(
    val id: String,
    val type: ModuleType,
    val name: String,
    val description: String,
    val tier: Int,
    val installed: Boolean = false,
    val costTitanium: Int,
    val costCircuits: Int,
    val costLithium: Int,
    val costSpecial: Int = 0
)

enum class CelestialType {
    DYING_STAR, DERELICT_STATION, ASTEROID_FIELD, GHOST_FREIGHTER, RESEARCH_LAB, QUANTUM_BEACON, EXODUS_ARK
}

data class CelestialEntity(
    val id: String,
    val name: String,
    val type: CelestialType,
    val x: Float,
    val y: Float,
    val radius: Float,
    val dangerLevel: String,
    val radiationBonus: Float,
    val salvaged: Boolean = false,
    val logId: String? = null,
    val rewardScrap: Int = 0,
    val rewardCircuits: Int = 0,
    val rewardLithium: Int = 0,
    val rewardHyperCore: Int = 0
)

data class SurvivorLog(
    val id: String,
    val author: String,
    val role: String,
    val starDate: String,
    val title: String,
    val audioTranscript: String,
    val moralPrompt: String? = null,
    val choiceA: String? = null,
    val choiceB: String? = null,
    var unlocked: Boolean = false,
    var resolved: Boolean = false,
    var chosenOption: String? = null
)

data class Sector(
    val id: String,
    val name: String,
    val description: String,
    val threatLevel: String,
    val starCondition: String,
    val radiationMultiplier: Float,
    val isCurrent: Boolean = false,
    val requiredDriveTier: Int = 0
)
