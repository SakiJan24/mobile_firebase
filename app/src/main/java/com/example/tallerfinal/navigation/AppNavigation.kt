package com.example.tallerfinal.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.tallerfinal.ui.screens.auth.AuthViewModel
import com.example.tallerfinal.ui.screens.auth.LoginScreen
import com.example.tallerfinal.ui.screens.auth.RegisterScreen
import com.example.tallerfinal.ui.screens.main.HomeScreen
import com.example.tallerfinal.ui.screens.main.ProfileScreen

/*
 * Sistema de navegación de la aplicación
 * Maneja las rutas entre pantallas y el estado de autenticación
 */
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()

    val startDestination = if (authViewModel.currentUser != null) {
        "home"
    } else {
        "login"
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable("login") {
            LoginScreen(navController = navController, authViewModel = authViewModel)
        }
        composable("register") {
            RegisterScreen(navController = navController, authViewModel = authViewModel)
        }
        composable("home") {
            HomeScreen(navController = navController, authViewModel = authViewModel)
        }
        composable("profile") {
            ProfileScreen(navController = navController)
        }
    }
}