package com.example.tallerfinal.services

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class LocationHandler(
    context: Context,
    private val databaseUrl: String // Pasamos la URL de la BD
) {
    private val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
    private val auth = Firebase.auth
    private val database = Firebase.database(databaseUrl)

    // --- Flujo para emitir la ubicación a la UI ---
    private val _locationFlow = MutableStateFlow<Location?>(null)
    val locationFlow: StateFlow<Location?> = _locationFlow

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.lastLocation?.let { location ->
                // 1. Emitir la ubicación a la UI (HomeScreen)
                _locationFlow.value = location

                // 2. Actualizar la localización en Firebase en tiempo real
                updateLocationInDatabase(location.latitude, location.longitude, true)
            }
        }
    }

    @SuppressLint("MissingPermission") // El permiso se verifica en la UI (HomeScreen)
    fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, 10000 // Intervalo de 10 segundos
        ).build()

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
        // 1. Limpiar la ubicación en la UI
        _locationFlow.value = null
        // 2. Actualizar el estado a "offline" en Firebase
        updateLocationInDatabase(0.0, 0.0, false)
    }

    private fun updateLocationInDatabase(lat: Double, lon: Double, isOnline: Boolean) {
        auth.currentUser?.uid?.let { uid ->
            val updates = mapOf(
                "latitude" to lat,
                "longitude" to lon,
                "isOnline" to isOnline
            )
            database.getReference("users").child(uid).updateChildren(updates)
        }
    }
}