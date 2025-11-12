package com.example.tallerfinal.ui.screens.map



import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tallerfinal.data.User
import com.example.tallerfinal.data.UserWithId
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MapViewModel : ViewModel() {

    private val auth: FirebaseAuth = Firebase.auth
    private val database: FirebaseDatabase = Firebase.database("https://tallerfinal-ac2d4-default-rtdb.firebaseio.com/") // <-- IMPORTANTE: Pon la URL

    private val currentUserId: String?
        get() = auth.currentUser?.uid

    // Flujo para exponer la lista de *otros* usuarios en línea
    private val _otherUsers = MutableStateFlow<List<UserWithId>>(emptyList())
    val otherUsers: StateFlow<List<UserWithId>> = _otherUsers

    private val usersRef = database.getReference("users")
    private var usersListener: ValueEventListener? = null

    init {
        startListeningToOtherUsers()
    }

    private fun startListeningToOtherUsers() {
        // Asegurarnos de que el ID de usuario no sea nulo
        val uid = currentUserId ?: return

        usersListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val usersList = mutableListOf<UserWithId>()
                for (userSnapshot in snapshot.children) {
                    val userId = userSnapshot.key
                    val user = userSnapshot.getValue(User::class.java)

                    // Añadir solo si:
                    // 1. No es el usuario actual
                    // 2. El usuario no es nulo
                    // 3. El usuario está en línea (isOnline = true)
                    if (userId != null && user != null && userId != uid && user.isOnline) {
                        usersList.add(UserWithId(id = userId, user = user))
                    }
                }
                _otherUsers.value = usersList
            }

            override fun onCancelled(error: DatabaseError) {
                // Manejar error
            }
        }
        // Escuchamos el nodo "users" completo
        usersRef.addValueEventListener(usersListener!!)
    }

    // Limpiar el listener cuando el ViewModel es destruido
    override fun onCleared() {
        super.onCleared()
        usersListener?.let {
            usersRef.removeEventListener(it)
        }
    }
}