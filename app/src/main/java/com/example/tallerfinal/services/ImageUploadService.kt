package com.example.tallerfinal.services

import android.net.Uri
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.tasks.await

/*
 * Servicio de carga de imágenes
 * Maneja el upload y eliminación de fotos de perfil en Firebase Storage
 */
class ImageUploadService {

    private val storage = Firebase.storage
    private val auth = Firebase.auth

    suspend fun uploadProfileImage(imageUri: Uri): String {
        val userId = auth.currentUser?.uid ?: throw Exception("Usuario no autenticado")

        val imageRef = storage.reference
            .child("profile_images")
            .child(userId)
            .child("profile.jpg")

        imageRef.putFile(imageUri).await()
        val downloadUrl = imageRef.downloadUrl.await()

        return downloadUrl.toString()
    }

    suspend fun deleteProfileImage() {
        val userId = auth.currentUser?.uid ?: throw Exception("Usuario no autenticado")

        val imageRef = storage.reference
            .child("profile_images")
            .child(userId)
            .child("profile.jpg")

        try {
            imageRef.delete().await()
        } catch (e: Exception) {
        }
    }
}
