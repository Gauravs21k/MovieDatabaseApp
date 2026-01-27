package com.gaurav.moviedatabaseapp

import android.app.Application
import com.gaurav.moviedatabaseapp.di.components.AppComponent
import com.gaurav.moviedatabaseapp.di.components.DaggerAppComponent
import com.gaurav.moviedatabaseapp.di.modules.AppModule

class MovieApplication : Application() {

    lateinit var appComponent: AppComponent

    override fun onCreate() {
        super.onCreate()
        appComponent = DaggerAppComponent.builder()
            .appModule(AppModule(this))
            .build()
    }
}
