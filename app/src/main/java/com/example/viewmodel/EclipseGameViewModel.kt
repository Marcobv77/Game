package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.CosmicAudioEngine
import com.example.data.GameRepository
import com.example.model.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.*

enum class ActiveScreen {
    MAIN_MENU, COCKPIT, WORKSHOP, STAR_MAP, CODEX_LOGS, EVA_SALVAGE, SETTINGS, ENDING_SCREEN
}

enum class EndingType {
    VICTORY_ARK_ESCAPE, DEATH_OXYGEN, DEATH_HULL_BREACH, DEATH_RADIATION, DEATH_VOID_SANITY
}

class EclipseGameViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = GameRepository(application)
    val audioEngine = CosmicAudioEngine(application)

    private val _currentScreen = MutableStateFlow(ActiveScreen.MAIN_MENU)
    val currentScreen: StateFlow<ActiveScreen> = _currentScreen.asStateFlow()

    private val _difficulty = MutableStateFlow(GameDifficulty.SUPERVIVIENTE)
    val difficulty: StateFlow<GameDifficulty> = _difficulty.asStateFlow()

    private val _gamePhase = MutableStateFlow(GamePhase.FASE_1_CAPSULA)
    val gamePhase: StateFlow<GamePhase> = _gamePhase.asStateFlow()

    private val _vitals = MutableStateFlow(VitalStats())
    val vitals: StateFlow<VitalStats> = _vitals.asStateFlow()

    private val _inventory = MutableStateFlow(Inventory())
    val inventory: StateFlow<Inventory> = _inventory.asStateFlow()

    // Smooth frame tick for Compose Canvas rendering synchronization
    private val _frameTick = MutableStateFlow(0L)
    val frameTick: StateFlow<Long> = _frameTick.asStateFlow()

    // Ship kinematic state (primitive floats for zero-allocation physics)
    @Volatile var posX = 260f
    @Volatile var posY = -120f
    @Volatile var velX = 0f
    @Volatile var velY = 0f
    @Volatile var angleDeg = 180f
    @Volatile var angularVel = 0f

    // Input thrust flags
    @Volatile var isThrustingForward = false
    @Volatile var isThrustingRetro = false
    @Volatile var isRotatingLeft = false
    @Volatile var isRotatingRight = false
    @Volatile var isRcsBraking = false

    // Entities and Modules
    private val _entities = MutableStateFlow(repository.getDefaultEntities())
    val entities: StateFlow<List<CelestialEntity>> = _entities.asStateFlow()

    private val _modules = MutableStateFlow(repository.getDefaultModules())
    val modules: StateFlow<List<ShipModule>> = _modules.asStateFlow()

    private val _logs = MutableStateFlow(repository.getDefaultLogs())
    val logs: StateFlow<List<SurvivorLog>> = _logs.asStateFlow()

    private val _sectors = MutableStateFlow(repository.getDefaultSectors())
    val sectors: StateFlow<List<Sector>> = _sectors.asStateFlow()

    // Interactive target within docking/EVA range
    private val _nearbyEntity = MutableStateFlow<CelestialEntity?>(null)
    val nearbyEntity: StateFlow<CelestialEntity?> = _nearbyEntity.asStateFlow()

    // EVA Minigame State
    private val _evaTarget = MutableStateFlow<CelestialEntity?>(null)
    val evaTarget: StateFlow<CelestialEntity?> = _evaTarget.asStateFlow()
    val evaCuttingProgress = MutableStateFlow(0f)
    val evaFrequencyTuned = MutableStateFlow(false)
    val evaTargetFrequency = MutableStateFlow(440)
    val evaCurrentFrequency = MutableStateFlow(320)

    // Ending info
    private val _endingType = MutableStateFlow<EndingType?>(null)
    val endingType: StateFlow<EndingType?> = _endingType.asStateFlow()

    // Active cosmic alert
    private val _activeAlert = MutableStateFlow<String?>(null)
    val activeAlert: StateFlow<String?> = _activeAlert.asStateFlow()

    private var simulationJob: Job? = null
    private var isDirty = false

    init {
        audioEngine.start()
        if (repository.hasSaveGame()) {
            loadGame()
        }
    }

    fun startNewGame(difficultyChoice: GameDifficulty) {
        _difficulty.value = difficultyChoice
        _gamePhase.value = GamePhase.FASE_1_CAPSULA
        _vitals.value = VitalStats(
            oxygen = 100f,
            power = 95f,
            hullIntegrity = 85f,
            temperature = 20.0f,
            radiation = 0.3f,
            sanity = 95f,
            fuel = 75f
        )
        _inventory.value = Inventory(
            titaniumScrap = 15,
            quantumCircuits = 4,
            lithiumCells = 3,
            leadPlates = 1,
            foodRations = 8,
            waterRations = 10,
            hyperCores = 0
        )
        posX = 260f
        posY = -120f
        velX = -0.1f
        velY = 0.2f
        angleDeg = 145f
        angularVel = 0f
        _entities.value = repository.getDefaultEntities()
        _modules.value = repository.getDefaultModules()
        _logs.value = repository.getDefaultLogs()
        _sectors.value = repository.getDefaultSectors()
        _endingType.value = null
        _activeAlert.value = "SISTEMA INICIALIZADO: Cápsula Eclipse-07 en órbita. Estabiliza el soporte vital."

        saveGame()
        navigateTo(ActiveScreen.COCKPIT)
        startSimulation()
    }

    fun continueGame() {
        loadGame()
        navigateTo(ActiveScreen.COCKPIT)
        startSimulation()
    }

    fun navigateTo(screen: ActiveScreen) {
        audioEngine.triggerUiBlip()
        _currentScreen.value = screen
        if (screen == ActiveScreen.COCKPIT && simulationJob?.isActive != true) {
            startSimulation()
        } else if (screen != ActiveScreen.COCKPIT && screen != ActiveScreen.EVA_SALVAGE) {
            // Save on opening menus
            if (isDirty) {
                saveGame()
                isDirty = false
            }
        }
    }

    private fun startSimulation() {
        simulationJob?.cancel()
        simulationJob = viewModelScope.launch {
            var tickCounter = 0L
            val dt = 0.033f // ~30 fps fixed timestep for predictable physics & low battery usage

            while (isActive) {
                delay(33)
                tickCounter++

                updatePhysics(dt)
                _frameTick.value = tickCounter

                if (tickCounter % 30L == 0L) { // Once per second
                    updateVitalsAndEnvironment()
                    checkPhaseProgression()
                    checkGameOverConditions()
                }

                if (tickCounter % 450L == 0L && isDirty) { // Save every ~15s if dirty
                    saveGame()
                    isDirty = false
                }
            }
        }
    }

    private fun updatePhysics(dt: Float) {
        val hasIonThruster = _modules.value.any { it.id == "mod_ion_thruster" && it.installed }

        // Steering / Attitude
        val rotSpeed = 90f * dt
        if (isRotatingLeft) {
            angleDeg -= rotSpeed
        }
        if (isRotatingRight) {
            angleDeg += rotSpeed
        }
        if (angleDeg < 0) angleDeg += 360f
        if (angleDeg >= 360f) angleDeg -= 360f

        // Thrusters
        var currentFuel = _vitals.value.fuel
        val fuelDrainRate = if (hasIonThruster) 0.04f else 0.08f
        val thrustPower = if (hasIonThruster) 18.0f else 12.0f

        val rad = Math.toRadians(angleDeg.toDouble())
        val dirX = cos(rad).toFloat()
        val dirY = sin(rad).toFloat()

        var isBurning = false
        if (isThrustingForward && currentFuel > 0.5f) {
            velX += dirX * thrustPower * dt
            velY += dirY * thrustPower * dt
            currentFuel -= fuelDrainRate
            isBurning = true
        }

        if (isThrustingRetro && currentFuel > 0.5f) {
            velX -= dirX * (thrustPower * 0.6f) * dt
            velY -= dirY * (thrustPower * 0.6f) * dt
            currentFuel -= fuelDrainRate * 0.6f
            isBurning = true
        }

        if (isRcsBraking && currentFuel > 0.2f) {
            velX *= (1f - 0.9f * dt)
            velY *= (1f - 0.9f * dt)
            currentFuel -= fuelDrainRate * 0.3f
            isBurning = true
        }

        audioEngine.isThrusterActive = isBurning

        // Gravity pull toward Helios-Omega (Dying Star at 0, 0)
        val px = posX
        val py = posY
        val distToCenter = sqrt(px * px + py * py).coerceAtLeast(60f)
        val gravForce = (4800f / (distToCenter * distToCenter)).coerceIn(0f, 3.5f)
        val gravDirX = -px / distToCenter
        val gravDirY = -py / distToCenter

        velX += gravDirX * gravForce * dt
        velY += gravDirY * gravForce * dt

        // Position integration
        posX = px + velX * dt
        posY = py + velY * dt

        if (isBurning) {
            _vitals.value = _vitals.value.copy(fuel = currentFuel.coerceAtLeast(0f))
            isDirty = true
        }

        // Proximity detection for salvage and docking
        val entitiesList = _entities.value
        val curX = posX
        val curY = posY
        var foundNear: CelestialEntity? = null
        for (i in entitiesList.indices) {
            val entity = entitiesList[i]
            val dx = entity.x - curX
            val dy = entity.y - curY
            if (dx * dx + dy * dy < (entity.radius + 65f) * (entity.radius + 65f)) {
                foundNear = entity
                break
            }
        }
        if (_nearbyEntity.value?.id != foundNear?.id) {
            _nearbyEntity.value = foundNear
        }
    }

    private fun updateVitalsAndEnvironment() {
        val curVitals = _vitals.value
        val diffRate = _difficulty.value.drainRate
        val modulesList = _modules.value

        var hasAirRecycler = false
        var hasSolar = false
        var hasLeadShield = false
        var hasThermalCoil = false
        var hasFusionCore = false

        for (i in modulesList.indices) {
            val m = modulesList[i]
            if (m.installed) {
                when (m.id) {
                    "mod_air_recycler" -> hasAirRecycler = true
                    "mod_solar_panel" -> hasSolar = true
                    "mod_lead_shield" -> hasLeadShield = true
                    "mod_thermal_coil" -> hasThermalCoil = true
                    "mod_fusion_core" -> hasFusionCore = true
                }
            }
        }

        // 1. Oxygen
        val baseO2Drain = if (hasAirRecycler) 0.12f else 0.28f
        val newO2 = (curVitals.oxygen - (baseO2Drain * diffRate)).coerceIn(0f, 100f)

        // 2. Power
        var powerDelta = -0.22f * diffRate
        if (hasFusionCore) {
            powerDelta = 0.5f
        } else if (hasSolar) {
            val toStarAngle = Math.toDegrees(atan2(-posY.toDouble(), -posX.toDouble())).let {
                if (it < 0) it + 360.0 else it
            }
            val angleDiff = abs(angleDeg - toStarAngle.toFloat()).let { if (it > 180) 360 - it else it }
            if (angleDiff < 55f) {
                powerDelta += 0.45f
            }
        }
        val newPower = (curVitals.power + powerDelta).coerceIn(0f, 100f)

        // 3. Radiation & Temperature based on distance to dying star
        val distToStar = sqrt(posX * posX + posY * posY)
        var radVal = (650f / (distToStar + 80f)).coerceIn(0.1f, 18.0f)
        if (hasLeadShield) radVal *= 0.45f

        val tempTarget = when {
            hasThermalCoil -> 21.0f
            distToStar < 180f -> 20f + (180f - distToStar) * 0.7f
            distToStar > 500f -> 20f - (distToStar - 500f) * 0.08f
            else -> 21.0f
        }
        val newTemp = curVitals.temperature + (tempTarget - curVitals.temperature) * 0.15f

        // 4. Sanity
        var sanityDelta = -0.06f * diffRate
        if (newO2 < 30f || curVitals.hullIntegrity < 40f || radVal > 4f) {
            sanityDelta -= 0.35f * diffRate
        }
        val newSanity = (curVitals.sanity + sanityDelta).coerceIn(0f, 100f)

        // Update procedural audio engine states
        audioEngine.radiationLevel = radVal
        audioEngine.isAlarmActive = (newO2 < 25f || curVitals.hullIntegrity < 30f || radVal > 5.5f)
        audioEngine.isHeartbeatActive = (newO2 < 20f || newSanity < 25f)

        _vitals.value = curVitals.copy(
            oxygen = newO2,
            power = newPower,
            temperature = newTemp,
            radiation = radVal,
            sanity = newSanity
        )
        isDirty = true
    }

    private fun checkPhaseProgression() {
        val installedMap = _modules.value.associateBy { it.id }
        val titanium = _inventory.value.titaniumScrap

        when (_gamePhase.value) {
            GamePhase.FASE_1_CAPSULA -> {
                if (installedMap["mod_air_recycler"]?.installed == true || titanium >= 30) {
                    _gamePhase.value = GamePhase.FASE_2_EXPLORACION
                    _activeAlert.value = "¡OBJETIVO CUMPLIDO! Avanzando a Fase II: Reconstrucción Modular."
                    audioEngine.triggerUiBlip()
                }
            }
            GamePhase.FASE_2_EXPLORACION -> {
                if (installedMap["mod_solar_panel"]?.installed == true && installedMap["mod_lead_shield"]?.installed == true) {
                    _gamePhase.value = GamePhase.FASE_3_TRIANGULACION
                    _activeAlert.value = "¡SUBSISTEMAS ACTIVOS! Localizando el Arca Éxodo-9."
                    audioEngine.triggerUiBlip()
                }
            }
            GamePhase.FASE_3_TRIANGULACION -> {
                if (installedMap["mod_warp_drive"]?.installed == true) {
                    _gamePhase.value = GamePhase.FASE_4_EL_ECLIPSE
                    _activeAlert.value = "¡MOTOR DE DISTORSIÓN OPERATIVO! El portal del Arca está abierto."
                    audioEngine.triggerUiBlip()
                }
            }
            GamePhase.FASE_4_EL_ECLIPSE -> {}
        }
    }

    private fun checkGameOverConditions() {
        val vit = _vitals.value
        when {
            vit.oxygen <= 0f -> triggerGameOver(EndingType.DEATH_OXYGEN)
            vit.hullIntegrity <= 0f -> triggerGameOver(EndingType.DEATH_HULL_BREACH)
            vit.radiation >= 15f -> triggerGameOver(EndingType.DEATH_RADIATION)
            vit.sanity <= 0f -> triggerGameOver(EndingType.DEATH_VOID_SANITY)
        }
    }

    private fun triggerGameOver(type: EndingType) {
        simulationJob?.cancel()
        audioEngine.isAlarmActive = false
        audioEngine.isThrusterActive = false
        audioEngine.isHeartbeatActive = false
        _endingType.value = type
        navigateTo(ActiveScreen.ENDING_SCREEN)
    }

    fun craftModule(moduleId: String) {
        val targetMod = _modules.value.find { it.id == moduleId } ?: return
        if (targetMod.installed) return

        val inv = _inventory.value
        if (inv.titaniumScrap >= targetMod.costTitanium &&
            inv.quantumCircuits >= targetMod.costCircuits &&
            inv.lithiumCells >= targetMod.costLithium &&
            inv.hyperCores >= targetMod.costSpecial
        ) {
            _inventory.value = inv.copy(
                titaniumScrap = inv.titaniumScrap - targetMod.costTitanium,
                quantumCircuits = inv.quantumCircuits - targetMod.costCircuits,
                lithiumCells = inv.lithiumCells - targetMod.costLithium,
                hyperCores = inv.hyperCores - targetMod.costSpecial
            )
            _modules.value = _modules.value.map {
                if (it.id == moduleId) it.copy(installed = true) else it
            }
            audioEngine.triggerClank()
            _activeAlert.value = "MÓDULO ENSAMBLADO: ${targetMod.name}"
            checkPhaseProgression()
            saveGame()
        }
    }

    fun startEva(entity: CelestialEntity) {
        if (entity.type == CelestialType.DYING_STAR) {
            _activeAlert.value = "ADVERTENCIA: Imposible realizar EVA en la fotosfera estelar."
            return
        }
        if (entity.type == CelestialType.EXODUS_ARK && _modules.value.find { it.id == "mod_warp_drive" }?.installed == true) {
            simulationJob?.cancel()
            _endingType.value = EndingType.VICTORY_ARK_ESCAPE
            navigateTo(ActiveScreen.ENDING_SCREEN)
            return
        }

        _evaTarget.value = entity
        evaCuttingProgress.value = 0f
        evaFrequencyTuned.value = false
        evaTargetFrequency.value = (400..600).random()
        evaCurrentFrequency.value = (300..380).random()
        navigateTo(ActiveScreen.EVA_SALVAGE)
    }

    fun cutLaserTick() {
        val progress = (evaCuttingProgress.value + 0.12f).coerceAtMost(1f)
        evaCuttingProgress.value = progress
        audioEngine.vibrate(30, 80)
        if (progress >= 1f) {
            audioEngine.triggerClank()
        }
    }

    fun tuneFrequency(sliderVal: Int) {
        evaCurrentFrequency.value = sliderVal
        val diff = abs(sliderVal - evaTargetFrequency.value)
        if (diff <= 15) {
            if (!evaFrequencyTuned.value) {
                evaFrequencyTuned.value = true
                audioEngine.triggerUiBlip()
                audioEngine.vibrate(50, 150)
            }
        }
    }

    fun completeEvaSalvage() {
        val target = _evaTarget.value ?: return
        val inv = _inventory.value

        _inventory.value = inv.copy(
            titaniumScrap = inv.titaniumScrap + target.rewardScrap,
            quantumCircuits = inv.quantumCircuits + target.rewardCircuits,
            lithiumCells = inv.lithiumCells + target.rewardLithium,
            hyperCores = inv.hyperCores + target.rewardHyperCore
        )

        _entities.value = _entities.value.map {
            if (it.id == target.id) it.copy(salvaged = true) else it
        }

        target.logId?.let { logId ->
            _logs.value = _logs.value.map {
                if (it.id == logId) it.copy(unlocked = true) else it
            }
        }

        _vitals.value = _vitals.value.copy(
            sanity = (_vitals.value.sanity + 15f).coerceAtMost(100f),
            fuel = (_vitals.value.fuel + 20f).coerceAtMost(100f)
        )

        audioEngine.triggerClank()
        _activeAlert.value = "SALVAMENTO: +${target.rewardScrap} Titanio, +${target.rewardCircuits} Circuitos."
        saveGame()
        navigateTo(ActiveScreen.COCKPIT)
    }

    fun resolveMoralChoice(logId: String, choiceName: String) {
        _logs.value = _logs.value.map {
            if (it.id == logId) {
                it.copy(resolved = true, chosenOption = choiceName)
            } else it
        }
        audioEngine.triggerUiBlip()
        saveGame()
    }

    fun repairHullWithScrap() {
        val inv = _inventory.value
        val vit = _vitals.value
        if (inv.titaniumScrap >= 5 && vit.hullIntegrity < 100f) {
            _inventory.value = inv.copy(titaniumScrap = inv.titaniumScrap - 5)
            _vitals.value = vit.copy(hullIntegrity = (vit.hullIntegrity + 20f).coerceAtMost(100f))
            audioEngine.triggerClank()
            _activeAlert.value = "CASCO REPARADO: +20% Integridad Estructural (-5 Titanio)."
            isDirty = true
        }
    }

    fun consumeRations() {
        val inv = _inventory.value
        val vit = _vitals.value
        if (inv.foodRations > 0 && inv.waterRations > 0) {
            _inventory.value = inv.copy(
                foodRations = inv.foodRations - 1,
                waterRations = inv.waterRations - 1
            )
            _vitals.value = vit.copy(
                sanity = (vit.sanity + 18f).coerceAtMost(100f),
                oxygen = (vit.oxygen + 10f).coerceAtMost(100f)
            )
            audioEngine.triggerUiBlip()
            _activeAlert.value = "RACIONES: Metabolismo y cordura estabilizados."
            isDirty = true
        }
    }

    fun jumpSector(sectorId: String) {
        val sec = _sectors.value.find { it.id == sectorId } ?: return
        val installedMap = _modules.value.associateBy { it.id }
        val hasWarp = installedMap["mod_warp_drive"]?.installed == true

        if (sec.requiredDriveTier > 0 && !hasWarp) {
            _activeAlert.value = "ERROR: Salto interestelar requiere el Motor de Distorsión."
            return
        }

        _sectors.value = _sectors.value.map {
            it.copy(isCurrent = (it.id == sectorId))
        }
        audioEngine.triggerClank()
        _activeAlert.value = "DISTORSIÓN: Ingresando a ${sec.name}."
        navigateTo(ActiveScreen.COCKPIT)
    }

    private fun saveGame() {
        val installedIds = _modules.value.filter { it.installed }.map { it.id }.toSet()
        val salvagedIds = _entities.value.filter { it.salvaged }.map { it.id }.toSet()
        val unlockedLogIds = _logs.value.filter { it.unlocked }.map { it.id }.toSet()
        val resolvedMap = _logs.value.filter { it.resolved && it.chosenOption != null }
            .associate { it.id to (it.chosenOption ?: "") }

        repository.saveGame(
            vitals = _vitals.value,
            inventory = _inventory.value,
            difficulty = _difficulty.value,
            phase = _gamePhase.value,
            posX = posX,
            posY = posY,
            installedModuleIds = installedIds,
            salvagedEntityIds = salvagedIds,
            unlockedLogIds = unlockedLogIds,
            resolvedLogIds = resolvedMap
        )
    }

    private fun loadGame() {
        _vitals.value = repository.loadVitals()
        _inventory.value = repository.loadInventory()
        _difficulty.value = repository.loadDifficulty()
        _gamePhase.value = repository.loadPhase()

        val (savedX, savedY) = repository.loadPosition()
        posX = savedX
        posY = savedY

        val installedSet = repository.loadInstalledModules()
        _modules.value = repository.getDefaultModules().map {
            it.copy(installed = installedSet.contains(it.id))
        }

        val salvagedSet = repository.loadSalvagedEntities()
        _entities.value = repository.getDefaultEntities().map {
            it.copy(salvaged = salvagedSet.contains(it.id))
        }

        val unlockedLogSet = repository.loadUnlockedLogs()
        val resolvedMap = repository.loadResolvedLogs()
        _logs.value = repository.getDefaultLogs().map {
            it.copy(
                unlocked = unlockedLogSet.contains(it.id),
                resolved = resolvedMap.containsKey(it.id),
                chosenOption = resolvedMap[it.id]
            )
        }
    }

    fun hasSavedGame(): Boolean = repository.hasSaveGame()

    fun dismissAlert() {
        _activeAlert.value = null
    }

    override fun onCleared() {
        super.onCleared()
        simulationJob?.cancel()
        audioEngine.stop()
    }
}
