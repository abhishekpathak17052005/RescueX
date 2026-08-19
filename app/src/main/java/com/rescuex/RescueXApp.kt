package com.rescuex

import android.app.Application
import com.rescuex.di.DependencyContainer

class RescueXApp : Application() {
    lateinit var container: DependencyContainer

    override fun onCreate() {
        super.onCreate()
        container = DependencyContainer(this)
    }
}
