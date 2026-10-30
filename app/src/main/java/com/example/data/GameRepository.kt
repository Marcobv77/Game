package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.*
import org.json.JSONArray
import org.json.JSONObject

class GameRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("eclipse_drift_save_v1", Context.MODE_PRIVATE)

    fun getDefaultModules(): List<ShipModule> = listOf(
        ShipModule(
            id = "mod_air_recycler",
            type = ModuleType.AIR_RECYCLER,
            name = "Filtro de Aire Reciclador C-12",
            description = "Filtro catalítico para depurar CO2. Reduce el consumo de O2 en un 55%.",
            tier = 1,
            costTitanium = 20,
            costCircuits = 4,
            costLithium = 2
        ),
        ShipModule(
            id = "mod_solar_panel",
            type = ModuleType.SOLAR_PANEL,
            name = "Paneles Fotovoltaicos Desplegables",
            description = "Células solares de arseniuro de galio. Recarga energía eléctrica al apuntar a la estrella.",
            tier = 1,
            costTitanium = 25,
            costCircuits = 6,
            costLithium = 5
        ),
        ShipModule(
            id = "mod_lead_shield",
            type = ModuleType.LEAD_SHIELD,
            name = "Blindaje Radiológico Grafeno-Plomo",
            description = "Recubrimiento pesado contra llamaradas coronales y rayos gamma del colapso estelar.",
            tier = 2,
            costTitanium = 35,
            costCircuits = 5,
            costLithium = 4
        ),
        ShipModule(
            id = "mod_ion_thruster",
            type = ModuleType.ION_THRUSTER,
            name = "Propulsor Iónico Vectorial de Gas Xenón",
            description = "Empuje de alta eficiencia. Aumenta la velocidad y reduce el consumo de combustible a la mitad.",
            tier = 2,
            costTitanium = 40,
            costCircuits = 8,
            costLithium = 8
        ),
        ShipModule(
            id = "mod_thermal_coil",
            type = ModuleType.THERMAL_COIL,
            name = "Bobina Térmica Regenerativa",
            description = "Mantiene la cabina a 21°C impidiendo la congelación en la penumbra o el sobrecalentamiento.",
            tier = 2,
            costTitanium = 30,
            costCircuits = 6,
            costLithium = 6
        ),
        ShipModule(
            id = "mod_fusion_core",
            type = ModuleType.FUSION_CORE,
            name = "Micro-Reactor de Fusión Heliet-3",
            description = "Fuente autónoma de energía ininterrumpida. Elimina la dependencia de baterías químicas.",
            tier = 3,
            costTitanium = 60,
            costCircuits = 14,
            costLithium = 12,
            costSpecial = 1
        ),
        ShipModule(
            id = "mod_warp_drive",
            type = ModuleType.WARP_DRIVE,
            name = "Motor de Distorsión Gravitacional 'Eclipse Drift'",
            description = "El último invento de la colonia antes de perecer. Permite saltos interestelares hacia el Arca.",
            tier = 3,
            costTitanium = 75,
            costCircuits = 20,
            costLithium = 18,
            costSpecial = 2
        )
    )

    fun getDefaultLogs(): List<SurvivorLog> = listOf(
        SurvivorLog(
            id = "log_01",
            author = "Capitán Vance",
            role = "Comandante de Aethelgard-4",
            starDate = "3194.212",
            title = "Evacuación de Emergencia - Protocolo Omega",
            audioTranscript = "A toda la tripulación: el núcleo de Helios-Omega ha entrado en fase de contracción anómala. No es una supernova normal... algo está drenando el espectro lumínico de la estrella. Todas las cápsulas deben dispersarse. Si sobreviven, diríjanse al Arca Éxodo-9 en el borde del sistema. Que el vacío se apiade de nosotros.",
            moralPrompt = "¿Deseas emitir un pulso de eco de radar para buscar supervivientes cercanos (consume 15% de energía pero puede revelar restos)?",
            choiceA = "Emitir pulso de eco (Riesgo de energía)",
            choiceB = "Mantener silencio electromagnético (Ahorro estricto)",
            unlocked = true
        ),
        SurvivorLog(
            id = "log_02",
            author = "Dra. Aris Thorne",
            role = "Astrofísica Jefe",
            starDate = "3194.218",
            title = "La Sombra no es Materia",
            audioTranscript = "La masa que bloquea la fotosfera de la estrella... no es un cuerpo celeste. Tiene geometría viva. Nuestras lecturas de radiación de Hawking se disparan exponencialmente cuando la 'sombra' pasa frente al sensor. No nos destruyó una tormenta solar. Fuimos consumidos.",
            moralPrompt = "Descifrar los registros prohibidos de la Dra. Thorne aumentará tu conocimiento pero afectará tu cordura.",
            choiceA = "Descifrar registros (+Datos / -10 Cordura)",
            choiceB = "Cifrar y archivar en cuarentena (+Paz mental)",
            unlocked = false
        ),
        SurvivorLog(
            id = "log_03",
            author = "Ingeniero Kaelen",
            role = "Soporte Vital y Presurización",
            starDate = "3194.225",
            title = "La Válvula 4C y el Sacrificio",
            audioTranscript = "Teníamos tres cápsulas conectadas en cadena. La válvula de amoniaco reventó y el sistema de soporte vital colapsó. Para que una sobreviviera, tuve que desconectar manualmente las abrazaderas de las otras dos. Aún escucho los golpes en el casco... el metal resonando en el silencio absoluto...",
            moralPrompt = "¿Realizar un minuto de silencio y calibrar el regulador de oxígeno para evitar fallos mecánicos?",
            choiceA = "Calibrar meticulosamente (+Recursos O2)",
            choiceB = "Ignorar y continuar marcha",
            unlocked = false
        ),
        SurvivorLog(
            id = "log_04",
            author = "Transmisión del Arca Éxodo-9",
            role = "Baliza de Emergencia Automatizada",
            starDate = "3194.240",
            title = "Coordenadas del Umbral Interestelar",
            audioTranscript = "Aquí el Arca Éxodo-9. Motores sublumínicos al 40%. La Sombra del Eclipse ha alcanzado la órbita de Caronte. A cualquier superviviente de la estación Aethelgard: nos replegamos hacia el hiperportal en el sector exterior. No intenten el salto sin un motor de distorsión calibrado.",
            moralPrompt = "La baliza contiene un núcleo cuántico sobrecargado. ¿Desmontarlo o reactivar la señal para otros?",
            choiceA = "Extraer núcleo cuántico (+1 Núcleo)",
            choiceB = "Potenciar señal (+15 Cordura / Esperanza)",
            unlocked = false
        ),
        SurvivorLog(
            id = "log_05",
            author = "Registro Desconocido",
            role = "Eco del Vacío Profundo",
            starDate = "???",
            title = "En el Frío Eterno",
            audioTranscript = "No mires demasiado tiempo a la corona apagada. Cuando la luz se apaga, no hay noche... hay una presencia. Tu calor es lo único que te separa de la nada. Cuida tu fuego.",
            unlocked = false
        )
    )

    fun getDefaultEntities(): List<CelestialEntity> = listOf(
        CelestialEntity(
            id = "entity_star",
            name = "Helios-Omega (Estrella Moribunda)",
            type = CelestialType.DYING_STAR,
            x = 0f,
            y = 0f,
            radius = 160f,
            dangerLevel = "EXTREMO (Radiación y Calor)",
            radiationBonus = 8.5f
        ),
        CelestialEntity(
            id = "entity_station_ruins",
            name = "Restos de la Colonia Aethelgard-4",
            type = CelestialType.DERELICT_STATION,
            x = 320f,
            y = -180f,
            radius = 55f,
            dangerLevel = "Moderado",
            radiationBonus = 0.5f,
            logId = "log_01",
            rewardScrap = 24,
            rewardCircuits = 5,
            rewardLithium = 3
        ),
        CelestialEntity(
            id = "entity_pod_twin",
            name = "Cápsula de Escape Derribada 'Eclipse-04'",
            type = CelestialType.GHOST_FREIGHTER,
            x = -260f,
            y = 210f,
            radius = 35f,
            dangerLevel = "Bajo",
            radiationBonus = 0.2f,
            logId = "log_02",
            rewardScrap = 18,
            rewardCircuits = 4,
            rewardLithium = 2
        ),
        CelestialEntity(
            id = "entity_asteroid_belt",
            name = "Cinturón de Asteroides de Caronte",
            type = CelestialType.ASTEROID_FIELD,
            x = 520f,
            y = 380f,
            radius = 80f,
            dangerLevel = "Alto (Impactos de Meteorito)",
            radiationBonus = 1.2f,
            rewardScrap = 35,
            rewardLithium = 6
        ),
        CelestialEntity(
            id = "entity_lab_prometheus",
            name = "Estación de Investigación 'Prometheus'",
            type = CelestialType.RESEARCH_LAB,
            x = -480f,
            y = -390f,
            radius = 60f,
            dangerLevel = "Severo (Fugas Químicas)",
            radiationBonus = 2.8f,
            logId = "log_03",
            rewardScrap = 30,
            rewardCircuits = 8,
            rewardLithium = 5,
            rewardHyperCore = 1
        ),
        CelestialEntity(
            id = "entity_beacon",
            name = "Baliza de Relé Subespacial 73-K",
            type = CelestialType.QUANTUM_BEACON,
            x = 110f,
            y = 620f,
            radius = 30f,
            dangerLevel = "Bajo",
            radiationBonus = 0.3f,
            logId = "log_04",
            rewardCircuits = 6,
            rewardLithium = 4
        ),
        CelestialEntity(
            id = "entity_ark",
            name = "El Arca Éxodo-9 (Nave Colonia Primaria)",
            type = CelestialType.EXODUS_ARK,
            x = -750f,
            y = 700f,
            radius = 95f,
            dangerLevel = "Desconocido (Anomalía Gravitacional)",
            radiationBonus = 4.0f,
            logId = "log_05",
            rewardScrap = 50,
            rewardCircuits = 15,
            rewardLithium = 15,
            rewardHyperCore = 1
        )
    )

    fun getDefaultSectors(): List<Sector> = listOf(
        Sector(
            id = "sec_1",
            name = "Sector 01: Órbita Muerta de Aethelgard",
            description = "El perímetro inmediato del cataclismo. Escombros de la estación madre flotan en caída libre.",
            threatLevel = "Moderado",
            starCondition = "Eclipse Parcial (Corona Visible)",
            radiationMultiplier = 1.0f,
            isCurrent = true,
            requiredDriveTier = 0
        ),
        Sector(
            id = "sec_2",
            name = "Sector 02: Cinturón de Desechos de Caronte",
            description = "Nube densa de asteroides y naves cargueras fracturadas. Rico en titanio y litio.",
            threatLevel = "Alto",
            starCondition = "Oscurecimiento Progresivo",
            radiationMultiplier = 1.6f,
            requiredDriveTier = 1
        ),
        Sector(
            id = "sec_3",
            name = "Sector 03: La Corona de la Sombra",
            description = "La atmósfera de la estrella agonizante. Ondas gravitacionales extremas y radiación pura.",
            threatLevel = "Peligro Crítico",
            starCondition = "Fase de Colapso",
            radiationMultiplier = 2.8f,
            requiredDriveTier = 2
        ),
        Sector(
            id = "sec_4",
            name = "Sector 04: El Abismo y El Arca Éxodo",
            description = "El confín del sistema estelar. Aquí aguarda el Arca y el destino final de la humanidad.",
            threatLevel = "Hostil Cósmico",
            starCondition = "Apagón Estelar Total",
            radiationMultiplier = 3.5f,
            requiredDriveTier = 3
        )
    )

    fun hasSaveGame(): Boolean = prefs.getBoolean("has_saved_game", false)

    fun saveGame(
        vitals: VitalStats,
        inventory: Inventory,
        difficulty: GameDifficulty,
        phase: GamePhase,
        posX: Float,
        posY: Float,
        installedModuleIds: Set<String>,
        salvagedEntityIds: Set<String>,
        unlockedLogIds: Set<String>,
        resolvedLogIds: Map<String, String>
    ) {
        val editor = prefs.edit()
        editor.putBoolean("has_saved_game", true)
        editor.putString("difficulty", difficulty.name)
        editor.putString("phase", phase.name)
        editor.putFloat("vitals_o2", vitals.oxygen)
        editor.putFloat("vitals_power", vitals.power)
        editor.putFloat("vitals_hull", vitals.hullIntegrity)
        editor.putFloat("vitals_temp", vitals.temperature)
        editor.putFloat("vitals_rads", vitals.radiation)
        editor.putFloat("vitals_sanity", vitals.sanity)
        editor.putFloat("vitals_fuel", vitals.fuel)

        editor.putInt("inv_titanium", inventory.titaniumScrap)
        editor.putInt("inv_circuits", inventory.quantumCircuits)
        editor.putInt("inv_lithium", inventory.lithiumCells)
        editor.putInt("inv_lead", inventory.leadPlates)
        editor.putInt("inv_food", inventory.foodRations)
        editor.putInt("inv_water", inventory.waterRations)
        editor.putInt("inv_hypercore", inventory.hyperCores)

        editor.putFloat("pos_x", posX)
        editor.putFloat("pos_y", posY)

        editor.putStringSet("installed_modules", installedModuleIds)
        editor.putStringSet("salvaged_entities", salvagedEntityIds)
        editor.putStringSet("unlocked_logs", unlockedLogIds)

        val resolvedJson = JSONObject()
        resolvedLogIds.forEach { (k, v) -> resolvedJson.put(k, v) }
        editor.putString("resolved_logs_json", resolvedJson.toString())

        editor.apply()
    }

    fun clearSave() {
        prefs.edit().clear().apply()
    }

    fun loadVitals(): VitalStats = VitalStats(
        oxygen = prefs.getFloat("vitals_o2", 95f),
        power = prefs.getFloat("vitals_power", 88f),
        hullIntegrity = prefs.getFloat("vitals_hull", 75f),
        temperature = prefs.getFloat("vitals_temp", 19.5f),
        radiation = prefs.getFloat("vitals_rads", 0.4f),
        sanity = prefs.getFloat("vitals_sanity", 90f),
        fuel = prefs.getFloat("vitals_fuel", 60f)
    )

    fun loadInventory(): Inventory = Inventory(
        titaniumScrap = prefs.getInt("inv_titanium", 14),
        quantumCircuits = prefs.getInt("inv_circuits", 3),
        lithiumCells = prefs.getInt("inv_lithium", 2),
        leadPlates = prefs.getInt("inv_lead", 1),
        foodRations = prefs.getInt("inv_food", 6),
        waterRations = prefs.getInt("inv_water", 8),
        hyperCores = prefs.getInt("inv_hypercore", 0)
    )

    fun loadDifficulty(): GameDifficulty {
        val raw = prefs.getString("difficulty", GameDifficulty.SUPERVIVIENTE.name)
        return try {
            GameDifficulty.valueOf(raw ?: GameDifficulty.SUPERVIVIENTE.name)
        } catch (_: Exception) {
            GameDifficulty.SUPERVIVIENTE
        }
    }

    fun loadPhase(): GamePhase {
        val raw = prefs.getString("phase", GamePhase.FASE_1_CAPSULA.name)
        return try {
            GamePhase.valueOf(raw ?: GamePhase.FASE_1_CAPSULA.name)
        } catch (_: Exception) {
            GamePhase.FASE_1_CAPSULA
        }
    }

    fun loadPosition(): Pair<Float, Float> =
        Pair(prefs.getFloat("pos_x", 120f), prefs.getFloat("pos_y", -140f))

    fun loadInstalledModules(): Set<String> =
        prefs.getStringSet("installed_modules", emptySet()) ?: emptySet()

    fun loadSalvagedEntities(): Set<String> =
        prefs.getStringSet("salvaged_entities", emptySet()) ?: emptySet()

    fun loadUnlockedLogs(): Set<String> =
        prefs.getStringSet("unlocked_logs", setOf("log_01")) ?: setOf("log_01")

    fun loadResolvedLogs(): Map<String, String> {
        val res = mutableMapOf<String, String>()
        val raw = prefs.getString("resolved_logs_json", null) ?: return res
        try {
            val json = JSONObject(raw)
            json.keys().forEach { key ->
                res[key] = json.optString(key)
            }
        } catch (_: Exception) {}
        return res
    }
}
