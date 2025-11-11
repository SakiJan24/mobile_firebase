package com.example.tallerfinal.ui.screens.main


import android.Manifest
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.tallerfinal.services.LocationHandler
import com.example.tallerfinal.ui.screens.auth.AuthViewModel

import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel()
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var isOnline by remember { mutableStateOf(false) }

    val context = LocalContext.current
    // Instanciamos el LocationHandler. Asegúrate de poner tu URL de BD
    val locationHandler = remember {
        LocationHandler(context, "https://tallerfinal-ac2d4-default-rtdb.firebaseio.com/")
    }

    // --- Gestión de Permisos ---
    val locationPermissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    // Efecto para reaccionar a los cambios de 'isOnline'
    LaunchedEffect(isOnline, locationPermissionsState.allPermissionsGranted) {
        if (isOnline) {
            if (locationPermissionsState.allPermissionsGranted) {
                // Si hay permisos, inicia la localización
                locationHandler.startLocationUpdates()
            } else {
                // Si no hay permisos, pide permisos.
                // Si el usuario los concede, este Effect se re-ejecutará.
                isOnline = false // Revierte el switch
                locationPermissionsState.launchMultiplePermissionRequest()
            }
        } else {
            // Si el switch se apaga, detiene la localización
            locationHandler.stopLocationUpdates()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Home") },
                actions = {
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menú")
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Modificar Perfil") },
                                onClick = {
                                    navController.navigate("profile")
                                    menuExpanded = false
                                },
                                leadingIcon = { Icon(Icons.Default.Person, null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Cerrar Sesión") },
                                onClick = {
                                    if (isOnline) {
                                        locationHandler.stopLocationUpdates() // Asegura marcar offline
                                    }
                                    authViewModel.logout()
                                    // El AppNavigation se encargará de redirigir al login
                                },
                                leadingIcon = { Icon(Icons.Default.ExitToApp, null) }
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Bienvenido", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(32.dp))

            // Switch para "isOnline"
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                Text(
                    text = if (isOnline) "En línea (Enviando ubicación)" else "Fuera de línea",
                    style = MaterialTheme.typography.bodyLarge
                )
                Switch(
                    checked = isOnline,
                    onCheckedChange = {
                        isOnline = it
                        // El LaunchedEffect se encargará del resto (pedir permisos o iniciar/detener)
                    }
                )
            }

            if (!locationPermissionsState.allPermissionsGranted && isOnline) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Se requieren permisos de localización para estar 'En línea'.",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}