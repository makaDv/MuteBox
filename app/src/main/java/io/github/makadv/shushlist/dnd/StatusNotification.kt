package io.github.makadv.shushlist.dnd

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import io.github.makadv.shushlist.R
import io.github.makadv.shushlist.schedule.HushReceiver
import io.github.makadv.shushlist.ui.MainActivity

/** Notifica fissa mostrata mentre Shushlist è attivo, con il pulsante "Disattiva". */
object StatusNotification {
    private const val CHANNEL = "status"
    private const val ID = 1

    fun createChannel(context: Context) {
        val channel = NotificationChannel(CHANNEL, "Stato di Shushlist", NotificationManager.IMPORTANCE_LOW).apply {
            description = "Mostra quando Shushlist è attivo"
            setShowBadge(false)
        }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    @SuppressLint("MissingPermission")
    fun show(context: Context) {
        if (Build.VERSION.SDK_INT >= 33 &&
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return

        val open = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_IMMUTABLE,
        )
        val stop = PendingIntent.getBroadcast(
            context, 1,
            Intent(context, HushReceiver::class.java).setAction(HushReceiver.ACTION_STOP),
            PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(context, CHANNEL)
            .setSmallIcon(R.drawable.ic_hush)
            .setContentTitle("Shushlist attivo")
            .setContentText("Suonano solo le notifiche che hai scelto")
            .setOngoing(true)
            .setSilent(true)
            .setContentIntent(open)
            .addAction(0, "Disattiva", stop)
            .build()
        NotificationManagerCompat.from(context).notify(ID, notification)
    }

    fun cancel(context: Context) {
        NotificationManagerCompat.from(context).cancel(ID)
    }
}
