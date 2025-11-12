package com.example.tallerfinal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.tallerfinal.navigation.AppNavigation
import com.example.tallerfinal.ui.theme.TallerFinalTheme
import com.google.firebase.FirebaseApp
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.common.api.ApiException

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Google Maps
        try {
            MapsInitializer.initialize(this) // This ensures that the Maps SDK is properly initialized
        } catch (e: ApiException) {
            e.printStackTrace()
        }

        setContent {
            TallerFinalTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}
