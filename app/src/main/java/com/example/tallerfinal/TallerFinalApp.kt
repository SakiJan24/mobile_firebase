package com.example.tallerfinal

import android.app.Application
import com.google.firebase.FirebaseApp

class TallerFinalApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}
