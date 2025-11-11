package com.example.tallerfinal.data
import com.google.firebase.database.IgnoreExtraProperties

// ignoreExtraProperties es útil para que Firebase no falle si hay campos extra en la BD
@IgnoreExtraProperties
data class User(
    val name: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val isOnline: Boolean = false,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
) {
    // Constructor vacío requerido por Firebase Realtime Database
    constructor() : this(null, null, null, false, 0.0, 0.0)
}