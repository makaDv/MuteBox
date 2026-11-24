package com.hushly.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import com.hushly.dnd.DndController
import com.hushly.schedule.ScheduleManager

data class PermState(
    val listener: Boolean,
    val dnd: Boolean,
    val postNotifications: Boolean,
    val exactAlarms: Boolean,
    val battery: Boolean,
) {
    /** Indispensabili per far funzionare Hushly. */
    val core: Boolean get() = listener && dnd
    val all: Boolean get() = core && postNotifications && exactAlarms && battery
}

object Permissions {
    fun read(context: Context) = PermState(
        listener = NotificationManagerCompat.getEnabledListenerPackages(context).contains(context.packageName),
        dnd = DndController.hasDndAccess(context),
        postNotifications = Build.VERSION.SDK_INT < 33 ||
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED,
        exactAlarms = ScheduleManager.canScheduleExact(context),
        battery = context.getSystemService(PowerManager::class.java).isIgnoringBatteryOptimizations(context.packageName),
    )

    fun listenerSettings() = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)

    fun dndSettings() = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)

    fun appDetails(context: Context) =
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + context.packageName))

    fun exactAlarmSettings(context: Context): Intent =
        if (Build.VERSION.SDK_INT >= 31) {
            Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:" + context.packageName))
        } else {
            appDetails(context)
        }

    @SuppressLint("BatteryLife")
    fun batterySettings(context: Context) =
        Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:" + context.packageName))
}
