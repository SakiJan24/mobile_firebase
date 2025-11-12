package com.example.tallerfinal.data
import com.google.firebase.database.IgnoreExtraProperties

/*
 * Modelo de datos de usuario
 * Almacena información del usuario en Firebase Realtime Database
 * IgnoreExtraProperties evita que Firebase falle si hay campos adicionales en la BD
 */
@IgnoreExtraProperties
data class User(
    val name: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val isOnline: Boolean = false,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val profileImageUrl: String? = null
) {
    /* Constructor vacío requerido por Firebase Realtime Database */
    constructor() : this(null, null, null, false, 0.0, 0.0, null)
}