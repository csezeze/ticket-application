package com.turkcell.ticketapp

import android.app.Application
import com.turkcell.data.di.dataModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class TicketAppApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@TicketAppApplication)
            modules(
                dataModule
            )
        }
    }
}