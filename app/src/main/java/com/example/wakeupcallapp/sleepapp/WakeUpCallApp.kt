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
import com.example.wakeupcallapp.sleepapp.viewmodel.SurveyViewModel

/**
 * Main app composable that sets up navigation and authentication
 *
 * This composable:
 * - Creates and manages the NavController
 * - Manages authentication state via AuthViewModel
 * - Manages survey data via SurveyViewModel
 * - Sets up the navigation graph
 * - Handles sign out logic (clears all user data)
 */
@Composable
fun WakeUpCallApp() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val surveyViewModel: SurveyViewModel = viewModel()
    val isAuthenticated by authViewModel.isAuthenticated.collectAsState()

    Surface(color = MaterialTheme.colorScheme.background) {
        AppNavGraph(
            navController = navController,
            isAuthenticated = isAuthenticated,
            onSignOut = {
                // Clear ALL survey data - reset ViewModel completely
                android.util.Log.d("WakeUpCallApp", "🔄 Signing out - clearing all survey data")
                surveyViewModel.resetSurveyData() // Clear all form fields
                surveyViewModel.resetSurvey() // Clear submission results
                surveyViewModel.resetSurveyStateForNewSession() // Reset loading states
                
                // Then sign out (handles both guest and regular users)
                authViewModel.signOut()
                authViewModel.exitGuestMode() // Ensure guest mode is cleared
                
                // Navigate back to auth screen and clear entire back stack
                navController.navigate("auth") {
                    popUpTo(0) { inclusive = true }
                }
                
                android.util.Log.d("WakeUpCallApp", "✅ Sign out complete - all data cleared")
            }
        )
    }
}
