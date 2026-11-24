package io.github.makadv.shushlist

import android.app.Application
import android.content.Context
import io.github.makadv.shushlist.data.RulesRepository
import io.github.makadv.shushlist.data.SettingsStore
import io.github.makadv.shushlist.dnd.StatusNotification

class ShushlistApp : Application() {
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

val Context.shushlist: ShushlistApp
    get() = applicationContext as ShushlistApp
