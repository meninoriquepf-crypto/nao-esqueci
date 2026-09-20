package com.naoesqueci.app

import android.app.Application
import com.naoesqueci.app.data.local.database.AppDatabase

class NaoEsqueciApplication : Application() {

    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }

    override fun onCreate() {
        super.onCreate()
        INSTANCE = this
    }

    companion object {
        @Volatile
        private var INSTANCE: NaoEsqueciApplication? = null

        fun getInstance(): NaoEsqueciApplication = INSTANCE!!
    }
}