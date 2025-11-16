package com.example.wakeupcallapp.sleepapp

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.wakeupcallapp.sleepapp.navigation.AppNavGraph
import com.example.wakeupcallapp.sleepapp.viewmodel.AuthViewModel

/**
 * Main app composable that sets up navigation and authentication
 *
 * This composable:
 * - Creates and manages the NavController
 * - Manages authentication state via AuthViewModel
 * - Sets up the navigation graph
 * - Handles sign out logic
 */
@Composable
fun WakeUpCallApp() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val isAuthenticated by authViewModel.isAuthenticated.collectAsState()

    Surface(color = MaterialTheme.colorScheme.background) {
        AppNavGraph(
            navController = navController,
            isAuthenticated = isAuthenticated,
            onSignOut = {
                authViewModel.signOut()
                // Navigate back to auth screen and clear entire back stack
                navController.navigate("auth") {
                    popUpTo(0) { inclusive = true }
                }
            }
        )
    }
}
