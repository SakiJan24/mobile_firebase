package com.example.tallerfinal.ui.screens.main

import android.Manifest
import android.location.Location
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.tallerfinal.R
import com.example.tallerfinal.services.LocationHandler
import com.example.tallerfinal.ui.screens.auth.AuthViewModel
import com.example.tallerfinal.ui.screens.map.MapViewModel
import com.example.tallerfinal.ui.utils.bitmapDescriptorFromVector
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import androidx.compose.material.icons.filled.ExitToApp


@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel(),
    mapViewModel: MapViewModel = viewModel()
) {
    // --- Estado del Menú ---
    var menuExpanded by remember { mutableStateOf(false) }

    // --- Estado del Mapa ---
    val bogota = LatLng(4.60971, -74.08175) // Ubicación inicial (Bogotá)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(bogota, 10f)
    }

    // --- Estado de Permisos ---
    val locationPermissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    // --- Estado del Switch y Localización ---
    val context = LocalContext.current
    var isTrackingEnabled by remember { mutableStateOf(false) }
    val locationHandler = remember(context) {
        LocationHandler(context, "https://tallerfinal-ac2d4-default-rtdb.firebaseio.com/") // <-- IMPORTANTE: Pon la URL
    }
    val currentLocation by locationHandler.locationFlow.collectAsState()

    // --- Estado de Polilíneas ---
    // Polilínea para el usuario actual
    val myPath = remember { mutableStateListOf<LatLng>() }
    // Mapa para almacenar las polilíneas de otros usuarios (ID de usuario -> Lista de puntos)
    val otherUsersPaths = remember { mutableStateMapOf<String, MutableList<LatLng>>() }

    // --- Estado de Otros Usuarios ---
    val otherUsers by mapViewModel.otherUsers.collectAsState()

    // --- Efectos (Lógica) ---

    // 1. Efecto para manejar el Switch de rastreo
    LaunchedEffect(isTrackingEnabled, locationPermissionsState.allPermissionsGranted) {
        if (isTrackingEnabled) {
            if (locationPermissionsState.allPermissionsGranted) {
                // Iniciar rastreo
                locationHandler.startLocationUpdates()
            } else {
                // Si no hay permisos, pedirlos.
                isTrackingEnabled = false // Revertir el switch
                locationPermissionsState.launchMultiplePermissionRequest()
            }
        } else {
            // Detener rastreo
            locationHandler.stopLocationUpdates()
            myPath.clear() // Borrar la ruta al apagar
        }
    }

    // 2. Efecto para reaccionar a la ubicación del *propio* usuario
    LaunchedEffect(currentLocation) {
        currentLocation?.let { loc ->
            val newLatLng = LatLng(loc.latitude, loc.longitude)
            myPath.add(newLatLng) // Añadir punto a la ruta

            // Mover la cámara al usuario (opcional, pero útil)
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(newLatLng, 16f),
                1000
            )
        }
    }

    // 3. Efecto para actualizar las polilíneas de *otros* usuarios
    LaunchedEffect(otherUsers) {
        val currentOnlineIds = otherUsers.map { it.id }.toSet()

        // Actualizar o añadir puntos a las rutas de usuarios en línea
        otherUsers.forEach { userWithId ->
            val path = otherUsersPaths.getOrPut(userWithId.id) { mutableListOf() }
            val newPos = LatLng(userWithId.user.latitude, userWithId.user.longitude)

            // Añadir solo si es un punto nuevo
            if (path.isEmpty() || path.last() != newPos) {
                path.add(newPos)
            }
        }

        // Limpiar rutas de usuarios que se desconectaron
        val offlineUserIds = otherUsersPaths.keys.filterNot { it in currentOnlineIds }
        offlineUserIds.forEach {
            otherUsersPaths.remove(it)
        }
    }

    // --- Marcadores Personalizados ---
    val userMarkerIcon = remember(context) {
        bitmapDescriptorFromVector(context, R.drawable.ic_user_location)
    }
    val otherMarkerIcon = remember(context) {
        bitmapDescriptorFromVector(context, R.drawable.ic_custom_marker)
    }

    // --- UI (Compose) ---
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mapa en Tiempo Real") },
                actions = {
                    // Menú de opciones
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
                                    if (isTrackingEnabled) {
                                        locationHandler.stopLocationUpdates() // Asegura marcar offline
                                    }
                                    authViewModel.logout()
                                    // La navegación se encargará de redirigir al login
                                },
                                leadingIcon = {
                                    Icon(Icons.Filled.ExitToApp, contentDescription = "Cerrar sesión")
                                }
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // El Mapa
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                // Habilitar controles de UI (zoom, etc.)
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = true,
                    mapToolbarEnabled = false
                )
            ) {
                // 1. Marcador y Polilínea del USUARIO ACTUAL
                currentLocation?.let { loc ->
                    val currentLatLng = LatLng(loc.latitude, loc.longitude)
                    Marker(
                        state = MarkerState(position = currentLatLng),
                        title = "Mi Ubicación",
                        icon = userMarkerIcon
                    )
                    // Dibujar la polilínea del usuario actual
                    if (myPath.size > 1) {
                        Polyline(
                            points = myPath,
                            color = Color.Blue,
                            width = 10f
                        )
                    }
                }

                // 2. Marcadores y Polilíneas de OTROS USUARIOS
                otherUsers.forEach { userWithId ->
                    val userPos = LatLng(userWithId.user.latitude, userWithId.user.longitude)
                    Marker(
                        state = MarkerState(position = userPos),
                        title = userWithId.user.name ?: "Usuario",
                        icon = otherMarkerIcon
                    )

                    // Dibujar la polilínea del otro usuario
                    otherUsersPaths[userWithId.id]?.let { path ->
                        if (path.size > 1) {
                            Polyline(
                                points = path,
                                color = Color.Red,
                                width = 10f
                            )
                        }
                    }
                }
            }

            // El Switch (superpuesto en la esquina superior derecha)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Top
            ) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    shadowElevation = 4.dp, // Corrected property
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isTrackingEnabled) "En línea" else "Offline",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Switch(
                            checked = isTrackingEnabled,
                            onCheckedChange = {
                                isTrackingEnabled = it
                            }
                        )
                    }
                }
            }
        }
    }
}
