package com.example.tallerfinal.ui.screens.main


import android.Manifest
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.tallerfinal.services.LocationHandler
import com.example.tallerfinal.ui.screens.auth.AuthViewModel
import com.example.tallerfinal.utils.MarkerUtils
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel()
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var isOnline by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val databaseUrl = "https://tallerfinal-ac2d4-default-rtdb.firebaseio.com/"
    val coroutineScope = rememberCoroutineScope()

    val locationHandler = remember { LocationHandler(context, databaseUrl) }
    val mapViewModel = remember { MapViewModel(databaseUrl) }

    // Observar la ubicación actual del usuario
    val currentLocation by locationHandler.currentLocation.collectAsState()
    val userPath by locationHandler.userPath.collectAsState()
    val onlineUsers by mapViewModel.onlineUsers.collectAsState()

    // Cargar foto de perfil del usuario actual
    var currentUserImageUrl by remember { mutableStateOf<String?>(null) }
    var currentUserMarker by remember { mutableStateOf<com.google.android.gms.maps.model.BitmapDescriptor?>(null) }

    // Mapa de marcadores personalizados para otros usuarios
    var userMarkers by remember { mutableStateOf<Map<String, com.google.android.gms.maps.model.BitmapDescriptor>>(emptyMap()) }

    // Estado para verificar si Google Maps está listo
    var isMapReady by remember { mutableStateOf(false) }

    // Cargar foto de perfil del usuario actual
    LaunchedEffect(Unit) {
        val auth = Firebase.auth
        val database = Firebase.database(databaseUrl)
        auth.currentUser?.uid?.let { uid ->
            val snapshot = database.getReference("users").child(uid).get().await()
            val user = snapshot.getValue<com.example.tallerfinal.data.User>()
            currentUserImageUrl = user?.profileImageUrl

            // Crear marcador personalizado para el usuario actual
            currentUserMarker = MarkerUtils.createCustomMarker(context, currentUserImageUrl, true)
        }
    }

    // Crear marcadores personalizados para otros usuarios
    LaunchedEffect(onlineUsers) {
        coroutineScope.launch {
            val newMarkers = mutableMapOf<String, com.google.android.gms.maps.model.BitmapDescriptor>()
            onlineUsers.forEach { (uid, onlineUser) ->
                val marker = MarkerUtils.createCustomMarker(
                    context,
                    onlineUser.user.profileImageUrl,
                    false
                )
                newMarkers[uid] = marker
            }
            userMarkers = newMarkers
        }
    }

    // Estado del mapa
    val defaultLocation = LatLng(4.60971, -74.08175) // Bogotá por defecto
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, 12f)
    }

    // Mover cámara cuando se detecte la ubicación del usuario
    LaunchedEffect(currentLocation) {
        currentLocation?.let {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(it, 15f)
        }
    }

    // Gestión de Permisos
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
                locationHandler.startLocationUpdates()
            } else {
                isOnline = false
                locationPermissionsState.launchMultiplePermissionRequest()
            }
        } else {
            locationHandler.stopLocationUpdates()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mapa de Ubicaciones") },
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
                                        locationHandler.stopLocationUpdates()
                                    }
                                    authViewModel.logout()
                                    navController.navigate("login") {
                                        popUpTo(navController.graph.startDestinationId) {
                                            inclusive = true
                                        }
                                    }
                                    menuExpanded = false
                                },
                                leadingIcon = { Icon(Icons.Default.ExitToApp, null) }
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Mapa de Google Maps
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    isMyLocationEnabled = false,
                    mapType = MapType.NORMAL
                ),
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = true,
                    myLocationButtonEnabled = false,
                    compassEnabled = true,
                    scrollGesturesEnabled = true,
                    zoomGesturesEnabled = true
                )
            ) {
                // Marcador del usuario actual - solo mostrar si el marcador está listo
                val location = currentLocation
                val marker = currentUserMarker
                if (location != null && marker != null) {
                    Marker(
                        state = MarkerState(position = location),
                        title = "Mi ubicación",
                        icon = marker
                    )

                    // Polyline de la ruta del usuario
                    if (userPath.isNotEmpty()) {
                        Polyline(
                            points = userPath,
                            color = Color.Blue,
                            width = 10f
                        )
                    }
                }

                // Marcadores de otros usuarios en línea
                onlineUsers.values.forEach { onlineUser ->
                    val userLocation = LatLng(onlineUser.user.latitude, onlineUser.user.longitude)
                    val markerIcon = userMarkers[onlineUser.uid]

                    // Solo mostrar marcador si existe
                    if (markerIcon != null) {
                        Marker(
                            state = MarkerState(position = userLocation),
                            title = onlineUser.user.name ?: "Usuario",
                            snippet = onlineUser.user.email,
                            icon = markerIcon
                        )
                    }

                    // Polyline de la ruta del otro usuario
                    if (onlineUser.path.isNotEmpty()) {
                        Polyline(
                            points = onlineUser.path,
                            color = Color.Red,
                            width = 8f
                        )
                    }
                }
            }

            // Botón flotante para centrar el mapa
            FloatingActionButton(
                onClick = {
                    val location = currentLocation
                    if (location != null) {
                        coroutineScope.launch {
                            cameraPositionState.animate(
                                com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(location, 15f),
                                500
                            )
                        }
                    }
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .padding(top = 64.dp),
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = "Centrar en mi ubicación",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            // Panel de control flotante
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isOnline) "En línea" else "Fuera de línea",
                                style = MaterialTheme.typography.titleMedium,
                                color = if (isOnline) Color(0xFF4CAF50) else Color.Gray
                            )
                            Text(
                                text = if (isOnline) "Compartiendo ubicación" else "Ubicación desactivada",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = isOnline,
                            onCheckedChange = { isOnline = it }
                        )
                    }

                    if (!locationPermissionsState.allPermissionsGranted && isOnline) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Se requieren permisos de localización",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    // Contador de usuarios en línea
                    Spacer(modifier = Modifier.height(8.dp))
                    val totalOnline = if (isOnline) onlineUsers.size + 1 else onlineUsers.size
                    Text(
                        text = "Usuarios en línea: ${totalOnline}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}