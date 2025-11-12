package com.example.tallerfinal.services

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/*
 * Servicio de manejo de ubicación GPS
 * Rastrea la ubicación del usuario y actualiza Firebase en tiempo real
 * Mantiene el historial de la ruta del usuario mediante StateFlow
 */
class LocationHandler(
    context: Context,
    private val databaseUrl: String
) {
    private val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
    private val auth = Firebase.auth
    private val database = Firebase.database(databaseUrl)

    private val _currentLocation = MutableStateFlow<LatLng?>(null)
    val currentLocation: StateFlow<LatLng?> = _currentLocation

    private val _userPath = MutableStateFlow<List<LatLng>>(emptyList())
    val userPath: StateFlow<List<LatLng>> = _userPath

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.lastLocation?.let { location ->
                val latLng = LatLng(location.latitude, location.longitude)
                _currentLocation.value = latLng
                _userPath.value = _userPath.value + latLng
                updateLocationInDatabase(location.latitude, location.longitude, true)
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, 5000
        ).setMinUpdateIntervalMillis(3000).build()

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
        _userPath.value = emptyList()
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

    fun clearPath() {
        _userPath.value = emptyList()
    }
}