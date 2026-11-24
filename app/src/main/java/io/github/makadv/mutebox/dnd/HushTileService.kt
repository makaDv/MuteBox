package io.github.makadv.mutebox.dnd

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import io.github.makadv.mutebox.mutebox
import io.github.makadv.mutebox.ui.MainActivity

/** Riquadro nelle Impostazioni rapide per accendere e spegnere MuteBox. */
class HushTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        updateTile()
    }

    override fun onClick() {
        super.onClick()
        if (mutebox.settings.current.active) {
            DndController.deactivate(this)
        } else if (!DndController.activate(this)) {
            openApp()
        }
        updateTile()
    }

    private fun updateTile() {
        val tile = qsTile ?: return
        val active = mutebox.settings.current.active
        tile.state = if (active) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.label = "MuteBox"
        if (Build.VERSION.SDK_INT >= 29) tile.subtitle = if (active) "Attivo" else "Spento"
        tile.updateTile()
    }

    /** Mancano i permessi: apre l'app per concederli. */
    private fun openApp() {
        val intent = Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (Build.VERSION.SDK_INT >= 34) {
            startActivityAndCollapse(PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE))
        } else {
            @Suppress("DEPRECATION")
            startActivityAndCollapse(intent)
        }
    }
}
