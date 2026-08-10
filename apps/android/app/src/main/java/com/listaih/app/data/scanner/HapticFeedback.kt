package com.listaih.app.data.scanner

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.os.Build

/**
 * Haptic and audio feedback for barcode scans:
 * - success: 80ms vibration + short beep
 * - error: double vibration + alert beep
 */
object HapticFeedback {

    private const val SUCCESS_VIBRATION_MS = 80L
    private const val ERROR_VIBRATION_MS = 70L

    fun success(context: Context) {
        vibrate(context, VibrationEffect.createOneShot(SUCCESS_VIBRATION_MS, VibrationEffect.DEFAULT_AMPLITUDE))
        beep(context, ToneGenerator.TONE_PROP_BEEP)
    }

    fun error(context: Context) {
        vibrate(
            context,
            VibrationEffect.createWaveform(longArrayOf(0, ERROR_VIBRATION_MS, 50, ERROR_VIBRATION_MS), -1)
        )
        beep(context, ToneGenerator.TONE_CDMA_ABBR_ALERT)
    }

    private fun vibrate(context: Context, effect: VibrationEffect) {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                manager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
            vibrator?.vibrate(effect)
        } catch (e: Exception) {
            // Vibration is best-effort
        }
    }

    private fun beep(context: Context, toneType: Int) {
        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            if (audioManager?.isVolumeFixed == true) return
            val tone = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 80)
            tone.startTone(toneType, 120)
            // Shorter than the tone duration, releases the stream
            Thread.sleep(100)
            tone.release()
        } catch (e: Exception) {
            // Audio is best-effort
        }
    }
}