package com.example.tallerfinal.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tallerfinal.data.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = Firebase.auth
    private val database: FirebaseDatabase = Firebase.database("https://tallerfinal-ac2d4-default-rtdb.firebaseio.com/") // <-- IMPORTANTE: Pon la URL de tu Realtime Database

    // Estado para la UI (Loading, Success, Error)
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    // Estado para saber si el usuario está logueado (observa el estado real de Firebase)
    val currentUser = auth.currentUser

    // --- Funciones de Autenticación ---

    fun register(name: String, email: String, pass: String, phone: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                // 1. Crear usuario en Firebase Auth
                val authResult = auth.createUserWithEmailAndPassword(email, pass).await()
                val firebaseUser = authResult.user

                if (firebaseUser != null) {
                    // 2. Crear objeto de usuario para la base de datos
                    val user = User(
                        name = name,
                        email = email,
                        phone = phone,
                        isOnline = false, // Valor inicial
                        latitude = 0.0,
                        longitude = 0.0
                    )

                    // 3. Guardar datos adicionales en Realtime Database
                    database.getReference("users").child(firebaseUser.uid).setValue(user).await()
                    _authState.value = AuthState.Success
                } else {
                    _authState.value = AuthState.Error("No se pudo crear el usuario.")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun login(email: String, pass: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                auth.signInWithEmailAndPassword(email, pass).await()
                _authState.value = AuthState.Success
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Error de inicio de sesión")
            }
        }
    }

    fun logout() {
        auth.signOut()
        _authState.value = AuthState.Idle // Resetea el estado
    }

    // Resetea el estado (por ejemplo, después de mostrar un error)
    fun resetAuthState() {
        _authState.value = AuthState.Idle
    }
}

// Clase sellada para manejar los estados de la UI
sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}