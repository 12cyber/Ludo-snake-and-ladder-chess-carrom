package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/**
 * Procedural real-time audio synthesizer and haptic feedback engine for PLAYVERSE.
 * Generates zero-latency acoustic tones for dice rolls, token hops, snake slides,
 * ladder climbs, carrom wooden collisions, chess piece moves, and victory fanfares.
 */
class SoundManager(private val context: Context) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var musicJob: Job? = null

    var soundEnabled: Boolean = true
    var musicEnabled: Boolean = false
    var vibrationEnabled: Boolean = true

    fun updateSettings(sound: Boolean, music: Boolean, vibration: Boolean) {
        soundEnabled = sound
        vibrationEnabled = vibration
        if (music != musicEnabled) {
            musicEnabled = music
            if (musicEnabled) startBackgroundMusic() else stopBackgroundMusic()
        }
    }

    fun playClick() {
        vibrate(12)
        if (!soundEnabled) return
        playSynthesizedSequence(listOf(ToneStep(880.0, 35, 0.25)))
    }

    fun playDiceRoll() {
        vibrate(35)
        if (!soundEnabled) return
        playSynthesizedSequence(
            listOf(
                ToneStep(320.0, 40, 0.3),
                ToneStep(460.0, 40, 0.3),
                ToneStep(580.0, 45, 0.35),
                ToneStep(740.0, 60, 0.4)
            )
        )
    }

    fun playTokenMove() {
        vibrate(18)
        if (!soundEnabled) return
        playSynthesizedSequence(
            listOf(
                ToneStep(523.25, 45, 0.35),
                ToneStep(659.25, 55, 0.35)
            )
        )
    }

    fun playLadderClimb() {
        vibrate(50)
        if (!soundEnabled) return
        playSynthesizedSequence(
            listOf(
                ToneStep(440.0, 70, 0.4),
                ToneStep(554.37, 70, 0.4),
                ToneStep(659.25, 70, 0.45),
                ToneStep(880.0, 140, 0.5)
            )
        )
    }

    fun playSnakeSlide() {
        vibrate(70)
        if (!soundEnabled) return
        playSynthesizedSequence(
            listOf(
                ToneStep(660.0, 75, 0.4),
                ToneStep(520.0, 75, 0.4),
                ToneStep(390.0, 85, 0.4),
                ToneStep(260.0, 150, 0.45)
            )
        )
    }

    fun playCapture() {
        vibrate(65)
        if (!soundEnabled) return
        playSynthesizedSequence(
            listOf(
                ToneStep(300.0, 50, 0.45),
                ToneStep(600.0, 60, 0.5),
                ToneStep(900.0, 110, 0.5)
            )
        )
    }

    fun playCarromStrike(powerRatio: Float) {
        val ms = (20 + (powerRatio * 45)).toLong()
        vibrate(ms)
        if (!soundEnabled) return
        val freq = 220.0 + (powerRatio * 180.0)
        playSynthesizedSequence(
            listOf(
                ToneStep(freq, 45, (0.3 + powerRatio * 0.35).coerceAtMost(0.7)),
                ToneStep(freq * 1.4, 30, 0.25)
            )
        )
    }

    fun playCarromCollision(intensity: Float = 0.5f) {
        if (intensity > 0.35f) vibrate(10)
        if (!soundEnabled) return
        val amp = (0.15 + intensity * 0.35).coerceIn(0.1, 0.55)
        playSynthesizedSequence(
            listOf(ToneStep(980.0 + intensity * 240.0, 22, amp))
        )
    }

    fun playCarromPocket() {
        vibrate(55)
        if (!soundEnabled) return
        playSynthesizedSequence(
            listOf(
                ToneStep(587.33, 65, 0.45),
                ToneStep(880.0, 120, 0.5)
            )
        )
    }

    fun playChessMove(isCapture: Boolean = false, isCheck: Boolean = false) {
        vibrate(if (isCheck) 60 else if (isCapture) 35 else 18)
        if (!soundEnabled) return
        when {
            isCheck -> playSynthesizedSequence(
                listOf(
                    ToneStep(783.99, 70, 0.45),
                    ToneStep(1046.50, 140, 0.5)
                )
            )
            isCapture -> playSynthesizedSequence(
                listOf(
                    ToneStep(280.0, 45, 0.45),
                    ToneStep(420.0, 65, 0.4)
                )
            )
            else -> playSynthesizedSequence(
                listOf(ToneStep(360.0, 45, 0.35))
            )
        }
    }

    fun playVictory() {
        vibrate(120)
        if (!soundEnabled) return
        playSynthesizedSequence(
            listOf(
                ToneStep(523.25, 100, 0.45),
                ToneStep(659.25, 100, 0.45),
                ToneStep(783.99, 100, 0.45),
                ToneStep(1046.50, 260, 0.55)
            )
        )
    }

    private fun startBackgroundMusic() {
        stopBackgroundMusic()
        musicJob = scope.launch {
            val chordNotes = listOf(261.63, 329.63, 392.00, 493.88, 392.00, 329.63)
            var idx = 0
            while (isActive && musicEnabled) {
                val note = chordNotes[idx % chordNotes.size]
                playSynthesizedSequence(listOf(ToneStep(note, 380, 0.08)))
                idx++
                delay(520)
            }
        }
    }

    fun stopBackgroundMusic() {
        musicJob?.cancel()
        musicJob = null
    }

    private fun vibrate(durationMs: Long) {
        if (!vibrationEnabled) return
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
            if (vibrator?.hasVibrator() == true) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(
                        VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE)
                    )
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(durationMs)
                }
            }
        } catch (_: Exception) {
            // Ignore in test environments without vibrator hardware
        }
    }

    private data class ToneStep(val freqHz: Double, val durationMs: Int, val amplitude: Double)

    private fun playSynthesizedSequence(steps: List<ToneStep>) {
        scope.launch {
            try {
                val sampleRate = 22050
                val totalSamples = steps.sumOf { (sampleRate * it.durationMs) / 1000 }
                if (totalSamples <= 0) return@launch
                val pcm = ShortArray(totalSamples)
                var offset = 0
                for (step in steps) {
                    val count = (sampleRate * step.durationMs) / 1000
                    for (i in 0 until count) {
                        val t = i.toDouble() / sampleRate
                        val envelope = exp(-3.5 * i.toDouble() / count.coerceAtLeast(1))
                        val wave = sin(2.0 * PI * step.freqHz * t) +
                            0.25 * sin(4.0 * PI * step.freqHz * t)
                        val sample = (wave * step.amplitude * envelope * Short.MAX_VALUE * 0.7)
                            .toInt()
                            .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                        if (offset + i < pcm.size) {
                            pcm[offset + i] = sample.toShort()
                        }
                    }
                    offset += count
                }

                val audioTrack = AudioTrack.Builder()
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
                    .setBufferSizeInBytes(pcm.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                audioTrack.write(pcm, 0, pcm.size)
                audioTrack.play()
                val totalMs = steps.sumOf { it.durationMs }.toLong()
                delay(totalMs + 60L)
                audioTrack.stop()
                audioTrack.release()
            } catch (_: Exception) {
                // Safe fallback when running on JVM/headless environment
            }
        }
    }
}
