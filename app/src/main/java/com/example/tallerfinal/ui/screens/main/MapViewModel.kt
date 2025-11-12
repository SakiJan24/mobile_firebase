package com.example.tallerfinal.ui.screens.main

import androidx.lifecycle.ViewModel
import com.example.tallerfinal.data.User
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class OnlineUser(
    val uid: String,
    val user: User,
    val path: List<LatLng> = emptyList()
)

/*
 * ViewModel del mapa
 * Gestiona los usuarios en línea y sus rutas en tiempo real
 * Escucha cambios en Firebase Realtime Database usando ChildEventListener
 */
class MapViewModel(databaseUrl: String) : ViewModel() {

    private val auth = Firebase.auth
    private val database = Firebase.database(databaseUrl)

    private val _onlineUsers = MutableStateFlow<Map<String, OnlineUser>>(emptyMap())
    val onlineUsers: StateFlow<Map<String, OnlineUser>> = _onlineUsers

    private val usersRef = database.getReference("users")
    private val userPathsMap = mutableMapOf<String, MutableList<LatLng>>()

    private val usersListener = object : ChildEventListener {
        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
            updateUserFromSnapshot(snapshot)
        }

        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
            updateUserFromSnapshot(snapshot)
        }

        override fun onChildRemoved(snapshot: DataSnapshot) {
            val uid = snapshot.key ?: return
            val currentMap = _onlineUsers.value.toMutableMap()
            currentMap.remove(uid)
            _onlineUsers.value = currentMap
            userPathsMap.remove(uid)
        }

        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
        override fun onCancelled(error: DatabaseError) {}
    }

    init {
        usersRef.addChildEventListener(usersListener)
    }

    private fun updateUserFromSnapshot(snapshot: DataSnapshot) {
        val uid = snapshot.key ?: return
        val currentUserId = auth.currentUser?.uid

        if (uid == currentUserId) return

        val user = snapshot.getValue<User>() ?: return

        if (user.isOnline && user.latitude != 0.0 && user.longitude != 0.0) {
            val latLng = LatLng(user.latitude, user.longitude)

            if (!userPathsMap.containsKey(uid)) {
                userPathsMap[uid] = mutableListOf()
            }
            userPathsMap[uid]?.add(latLng)

            val onlineUser = OnlineUser(
                uid = uid,
                user = user,
                path = userPathsMap[uid] ?: emptyList()
            )

            val currentMap = _onlineUsers.value.toMutableMap()
            currentMap[uid] = onlineUser
            _onlineUsers.value = currentMap
        } else {
            val currentMap = _onlineUsers.value.toMutableMap()
            currentMap.remove(uid)
            _onlineUsers.value = currentMap
            userPathsMap.remove(uid)
        }
    }

    override fun onCleared() {
        super.onCleared()
        usersRef.removeEventListener(usersListener)
    }
}

