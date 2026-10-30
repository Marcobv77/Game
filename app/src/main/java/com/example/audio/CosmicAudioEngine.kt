package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread
import kotlin.math.PI
import kotlin.math.sin

/**
 * Highly optimized procedural real-time synthesizer for Eclipse Drift.
 * Uses a precomputed sine lookup table and fast integer math to minimize CPU and battery usage.
 */
class CosmicAudioEngine(private val context: Context) {

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    private val isRunning = AtomicBoolean(false)
    private var audioTrack: AudioTrack? = null
    private var audioThread: Thread? = null

    // Audio controls
    @Volatile var soundEnabled = true
    @Volatile var hapticsEnabled = true

    // Dynamic synthesis states
    @Volatile var ambientDroneVolume = 0.35f
    @Volatile var isThrusterActive = false
    @Volatile var isAlarmActive = false
    @Volatile var radiationLevel = 0.0f
    @Volatile var isHeartbeatActive = false

    private val sampleRate = 22050
    private val bufferSize = AudioTrack.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_OUT_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    ).coerceAtLeast(2048)

    // Trigger one-shots
    @Volatile private var clankSamplesRemaining = 0
    @Volatile private var uiBlipSamplesRemaining = 0

    // Fast Sine Table (1024 entries for high-speed table lookup)
    companion object {
        private const val SINE_TABLE_SIZE = 1024
        private const val SINE_MASK = SINE_TABLE_SIZE - 1
        private val SINE_TABLE = FloatArray(SINE_TABLE_SIZE) { i ->
            sin(2.0 * PI * i / SINE_TABLE_SIZE).toFloat()
        }

        private inline fun fastSin(phaseNorm: Double): Float {
            val index = (phaseNorm * SINE_TABLE_SIZE).toInt() and SINE_MASK
            return SINE_TABLE[index]
        }
    }

    // Fast XORShift random for low-cost noise generation
    private var xorSeed = 88172645463325252L
    private fun fastNoise(): Float {
        xorSeed = xorSeed xor (xorSeed shl 13)
        xorSeed = xorSeed xor (xorSeed shr 7)
        xorSeed = xorSeed xor (xorSeed shl 17)
        return ((xorSeed and 0xFFFF).toFloat() / 32768.0f) - 1.0f
    }

    fun start() {
        if (isRunning.getAndSet(true)) return

        audioThread = thread(name = "EclipseAudioThread", priority = Thread.NORM_PRIORITY) {
            try {
                audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(bufferSize)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

                audioTrack?.play()

                val buffer = ShortArray(1024)
                var phaseNormDrone1 = 0.0
                var phaseNormDrone2 = 0.0
                var phaseNormAlarm = 0.0
                var phaseNormHeartbeat = 0.0
                var sampleCounter = 0L

                val invSampleRate = 1.0 / sampleRate

                while (isRunning.get()) {
                    if (!soundEnabled) {
                        buffer.fill(0)
                        audioTrack?.write(buffer, 0, buffer.size)
                        Thread.sleep(40)
                        continue
                    }

                    // Check if completely idle
                    val droneVol = ambientDroneVolume
                    val thruster = isThrusterActive
                    val alarm = isAlarmActive
                    val rad = radiationLevel
                    val heartbeat = isHeartbeatActive
                    val hasClank = clankSamplesRemaining > 0
                    val hasBlip = uiBlipSamplesRemaining > 0

                    if (droneVol <= 0.01f && !thruster && !alarm && rad <= 0.4f && !heartbeat && !hasClank && !hasBlip) {
                        buffer.fill(0)
                        audioTrack?.write(buffer, 0, buffer.size)
                        Thread.sleep(30)
                        continue
                    }

                    for (i in buffer.indices) {
                        sampleCounter++
                        var sample = 0f

                        // 1. Cosmic Drone (55Hz and 36.7Hz sub-harmonics via table lookup)
                        if (droneVol > 0.01f) {
                            phaseNormDrone1 += 55.0 * invSampleRate
                            phaseNormDrone2 += 36.7 * invSampleRate
                            if (phaseNormDrone1 >= 1.0) phaseNormDrone1 -= 1.0
                            if (phaseNormDrone2 >= 1.0) phaseNormDrone2 -= 1.0

                            val drone = (fastSin(phaseNormDrone1) * 0.6f + fastSin(phaseNormDrone2) * 0.4f) * droneVol
                            sample += drone * 0.32f
                        }

                        // 2. Thruster RCS Hiss (Fast noise)
                        if (thruster) {
                            sample += fastNoise() * 0.40f
                        }

                        // 3. Alarm Siren
                        if (alarm) {
                            val hz = if ((sampleCounter / 2200) % 2L == 0L) 880.0 else 1174.0
                            phaseNormAlarm += hz * invSampleRate
                            if (phaseNormAlarm >= 1.0) phaseNormAlarm -= 1.0
                            sample += if (fastSin(phaseNormAlarm) > 0f) 0.25f else -0.25f
                        }

                        // 4. Geiger Radiation Clicks
                        if (rad > 0.5f) {
                            val threshold = (rad * 0.0018f).coerceIn(0.0005f, 0.035f)
                            if ((xorSeed and 0x7FFF).toFloat() / 32768.0f < threshold) {
                                sample += if (fastNoise() > 0f) 0.7f else -0.7f
                            }
                        }

                        // 5. Heartbeat / Low O2 Pulse
                        if (heartbeat) {
                            val beatCycle = sampleCounter % 18000
                            if (beatCycle < 1800) {
                                phaseNormHeartbeat += 65.0 * invSampleRate
                                if (phaseNormHeartbeat >= 1.0) phaseNormHeartbeat -= 1.0
                                val env = 1.0f - (beatCycle / 1800.0f)
                                sample += fastSin(phaseNormHeartbeat) * env * 0.45f
                            }
                        }

                        // 6. Impact Clank
                        if (clankSamplesRemaining > 0) {
                            val progress = clankSamplesRemaining.toFloat() / 4410.0f
                            val clank = (fastSin((clankSamplesRemaining * 0.045)) * 0.6f + fastNoise() * 0.4f) * progress
                            sample += clank * 0.6f
                            clankSamplesRemaining--
                        }

                        // 7. UI Blip
                        if (uiBlipSamplesRemaining > 0) {
                            val env = uiBlipSamplesRemaining.toFloat() / 880.0f
                            val blip = fastSin(uiBlipSamplesRemaining * 0.08) * env
                            sample += blip * 0.22f
                            uiBlipSamplesRemaining--
                        }

                        // Fast clamp to Short range
                        val clamped = sample.coerceIn(-0.95f, 0.95f)
                        buffer[i] = (clamped * 32767f).toInt().toShort()
                    }

                    audioTrack?.write(buffer, 0, buffer.size)
                }
            } catch (_: Exception) {
            } finally {
                try {
                    audioTrack?.stop()
                    audioTrack?.release()
                } catch (_: Exception) {}
                audioTrack = null
            }
        }
    }

    fun triggerClank() {
        clankSamplesRemaining = 4410
        vibrate(80, 200)
    }

    fun triggerUiBlip() {
        uiBlipSamplesRemaining = 880
    }

    fun vibrate(durationMs: Long, amplitude: Int = 128) {
        if (!hapticsEnabled || vibrator == null || !vibrator.hasVibrator()) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val clampedAmp = amplitude.coerceIn(1, 255)
                vibrator.vibrate(VibrationEffect.createOneShot(durationMs, clampedAmp))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(durationMs)
            }
        } catch (_: Exception) {}
    }

    fun stop() {
        isRunning.set(false)
        try {
            audioThread?.interrupt()
        } catch (_: Exception) {}
        audioThread = null
    }
}
