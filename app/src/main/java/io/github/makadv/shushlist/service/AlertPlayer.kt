package io.github.makadv.shushlist.service

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import io.github.makadv.shushlist.shushlist

/**
 * Suona sul canale "sveglia": in modalità Shushlist il telefono lascia passare solo le sveglie,
 * quindi questo suono si sente anche con "Non disturbare" attivo.
 */
object AlertPlayer {
    private const val TAG = "AlertPlayer"
    private const val DEBOUNCE_MS = 3_000L
    private const val MAX_DURATION_MS = 8_000L

    private val handler = Handler(Looper.getMainLooper())
    private val lastPlayed = HashMap<String, Long>()
    private var player: MediaPlayer? = null

    private val alarmAttributes: AudioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_ALARM)
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .build()

    /** [key] identifica la conversazione: due notifiche della stessa chat entro 3 secondi suonano una volta sola. */
    @Synchronized
    fun play(context: Context, key: String?) {
        val now = SystemClock.elapsedRealtime()
        if (key != null) {
            val last = lastPlayed[key]
            if (last != null && now - last < DEBOUNCE_MS) return
            if (lastPlayed.size > 200) lastPlayed.clear()
            lastPlayed[key] = now
        }
        val s = context.shushlist.settings.current
        stop()
        val uris = listOfNotNull(
            s.soundUri?.let(Uri::parse),
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM),
        )
        for (uri in uris) {
            if (start(context, uri, s.volume / 100f)) break
        }
        if (s.vibrate) vibrate(context)
    }

    private fun start(context: Context, uri: Uri, volume: Float): Boolean = try {
        val mp = MediaPlayer()
        mp.setAudioAttributes(alarmAttributes)
        mp.setDataSource(context.applicationContext, uri)
        mp.setVolume(volume, volume)
        mp.setOnCompletionListener { stop() }
        mp.prepare()
        mp.start()
        player = mp
        handler.postDelayed({ stop() }, MAX_DURATION_MS)
        true
    } catch (e: Exception) {
        Log.w(TAG, "Impossibile riprodurre $uri", e)
        false
    }

    @Synchronized
    fun stop() {
        handler.removeCallbacksAndMessages(null)
        player?.let {
            runCatching { it.stop() }
            it.release()
        }
        player = null
    }

    private fun vibrate(context: Context) {
        val vibrator = if (Build.VERSION.SDK_INT >= 31) {
            context.getSystemService(VibratorManager::class.java).defaultVibrator
        } else {
            context.getSystemService(Vibrator::class.java)
        } ?: return
        val effect = VibrationEffect.createWaveform(longArrayOf(0, 400, 200, 400), -1)
        if (Build.VERSION.SDK_INT >= 33) {
            vibrator.vibrate(effect, VibrationAttributes.createForUsage(VibrationAttributes.USAGE_ALARM))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(effect, alarmAttributes)
        }
    }
}
