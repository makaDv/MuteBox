package io.github.makadv.mutebox

import android.app.Application
import android.content.Context
import io.github.makadv.mutebox.data.RulesRepository
import io.github.makadv.mutebox.data.SettingsStore
import io.github.makadv.mutebox.dnd.StatusNotification

class MuteBoxApp : Application() {
    lateinit var settings: SettingsStore
        private set
    lateinit var rules: RulesRepository
        private set

    override fun onCreate() {
        super.onCreate()
        settings = SettingsStore(this)
        rules = RulesRepository(this)
        StatusNotification.createChannel(this)
    }
}

val Context.mutebox: MuteBoxApp
    get() = applicationContext as MuteBoxApp
