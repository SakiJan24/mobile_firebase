package com.example.tallerfinal.data


// Clase contenedora para asociar un ID de Firebase con su objeto User
data class UserWithId(
    val id: String,
    val user: User
)