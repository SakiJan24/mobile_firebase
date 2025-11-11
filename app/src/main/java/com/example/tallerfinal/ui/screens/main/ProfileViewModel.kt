package com.example.tallerfinal.ui.screens.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tallerfinal.data.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProfileViewModel : ViewModel() {

    private val auth: FirebaseAuth = Firebase.auth
    private val database: FirebaseDatabase = Firebase.database("URL_DE_TU_DATABASE_AQUI") // <-- IMPORTANTE: Pon la URL de tu Realtime Database

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    private val _updateStatus = MutableStateFlow<UpdateStatus>(UpdateStatus.Idle)
    val updateStatus: StateFlow<UpdateStatus> = _updateStatus

    private val userId: String?
        get() = auth.currentUser?.uid

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            userId?.let { uid ->
                try {
                    val snapshot = database.getReference("users").child(uid).get().await()
                    val user = snapshot.getValue<User>()
                    _user.value = user
                } catch (e: Exception) {
                    // Manejar error de carga
                }
            }
        }
    }

    fun updateProfile(name: String, phone: String) {
        viewModelScope.launch {
            _updateStatus.value = UpdateStatus.Loading
            userId?.let { uid ->
                try {
                    val updates = mapOf(
                        "name" to name,
                        "phone" to phone
                    )
                    database.getReference("users").child(uid).updateChildren(updates).await()
                    _updateStatus.value = UpdateStatus.Success("Perfil actualizado")
                    // Recargar datos
                    loadUserProfile()
                } catch (e: Exception) {
                    _updateStatus.value = UpdateStatus.Error(e.message ?: "Error al actualizar")
                }
            }
        }
    }

    fun updatePassword(newPass: String) {
        viewModelScope.launch {
            _updateStatus.value = UpdateStatus.Loading
            try {
                auth.currentUser?.updatePassword(newPass)?.await()
                _updateStatus.value = UpdateStatus.Success("Contraseña actualizada")
            } catch (e: Exception) {
                _updateStatus.value = UpdateStatus.Error(e.message ?: "Error al cambiar contraseña")
            }
        }
    }

    fun resetUpdateStatus() {
        _updateStatus.value = UpdateStatus.Idle
    }
}

sealed class UpdateStatus {
    object Idle : UpdateStatus()
    object Loading : UpdateStatus()
    data class Success(val message: String) : UpdateStatus()
    data class Error(val message: String) : UpdateStatus()
}