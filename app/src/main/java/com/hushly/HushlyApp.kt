package com.hushly

import android.app.Application
import android.content.Context
import com.hushly.data.RulesRepository
import com.hushly.data.SettingsStore
import com.hushly.dnd.StatusNotification

class HushlyApp : Application() {
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

val Context.hushly: HushlyApp
    get() = applicationContext as HushlyApp
