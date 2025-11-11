package com.example.tallerfinal.ui.screens.main

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    profileViewModel: ProfileViewModel = viewModel()
) {
    val userState by profileViewModel.user.collectAsState()
    val status by profileViewModel.updateStatus.collectAsState()

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Fill fields when user loads
    LaunchedEffect(userState) {
        userState?.let {
            name = it.name ?: ""
            phone = it.phone ?: ""
        }
    }

    // Observe update status (success/error)
    LaunchedEffect(status) {
        when (val s = status) {
            is UpdateStatus.Success -> {
                snackbarHostState.showSnackbar(s.message)
                profileViewModel.resetUpdateStatus()
                newPassword = "" // clear password field
            }
            is UpdateStatus.Error -> {
                snackbarHostState.showSnackbar(s.message)
                profileViewModel.resetUpdateStatus()
            }
            else -> Unit
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Mi Perfil") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        if (userState == null || status == UpdateStatus.Loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Email (not editable)
                OutlinedTextField(
                    value = userState?.email ?: "Cargando...",
                    onValueChange = {},
                    label = { Text("Email (No editable)") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false
                )
                Spacer(modifier = Modifier.height(16.dp))

                // --- Update RTDB Data ---
                Text("Actualizar Datos", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre Completo") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Teléfono") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { profileViewModel.updateProfile(name, phone) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Actualizar Perfil")
                }

                Spacer(modifier = Modifier.height(24.dp))
                Divider()
                Spacer(modifier = Modifier.height(24.dp))

                // --- Update Password (Auth) ---
                Text("Actualizar Contraseña", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("Nueva Contraseña") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        if (newPassword.length >= 6) {
                            profileViewModel.updatePassword(newPassword)
                        } else {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("La contraseña debe tener al menos 6 caracteres")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cambiar Contraseña")
                }
            }
        }
    }
}
