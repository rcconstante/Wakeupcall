package com.example.wakeupcallapp.sleepapp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.wakeupcallapp.sleepapp.AuthScreenContent
import com.example.wakeupcallapp.sleepapp.DataConsentScreenContent
import com.example.wakeupcallapp.sleepapp.DashboardScreen
import com.example.wakeupcallapp.sleepapp.DemographicsScreenContent
import com.example.wakeupcallapp.sleepapp.FatigueSleepiness1ScreenContent
import com.example.wakeupcallapp.sleepapp.FatigueSleepiness2ScreenContent
import com.example.wakeupcallapp.sleepapp.FatigueSleepiness3ScreenContent
import com.example.wakeupcallapp.sleepapp.FatigueSleepiness4ScreenContent
import com.example.wakeupcallapp.sleepapp.FatigueSleepiness5ScreenContent
import com.example.wakeupcallapp.sleepapp.InfoConsentScreenContent
import com.example.wakeupcallapp.sleepapp.SleepHabits1ScreenContent
import com.example.wakeupcallapp.sleepapp.SleepHabits2ScreenContent
import com.example.wakeupcallapp.sleepapp.WakeUpCallScreen
import com.example.wakeupcallapp.sleepapp.LogInScreenContent
import com.example.wakeupcallapp.sleepapp.SignUpScreenContent
import com.example.wakeupcallapp.sleepapp.ProfileScreen
import com.example.wakeupcallapp.sleepapp.RecommendationsScreen
import com.example.wakeupcallapp.sleepapp.KeyFactorsScreen
import com.example.wakeupcallapp.sleepapp.DataSourcesScreen
import com.example.wakeupcallapp.sleepapp.SettingsScreen
import com.example.wakeupcallapp.sleepapp.HealthHistory1ScreenContent
import com.example.wakeupcallapp.sleepapp.HealthHistory2ScreenContent

/**
 * Navigation routes for the app
 */
object NavRoutes {
    // Unauthenticated flow
    const val SPLASH = "splash"
    const val INFO_CONSENT = "info_consent"
    const val AUTH = "auth"
    const val LOGIN = "login"
    const val SIGNUP = "signup"

    // Post-authentication onboarding flow
    const val DATA_CONSENT = "data_consent"
    const val DEMOGRAPHICS = "demographics"
    const val SURVEY_COORDINATOR = "survey_coordinator"
    const val FATIGUE_SLEEPINESS_1 = "fatigue_sleepiness_1"
    const val FATIGUE_SLEEPINESS_2 = "fatigue_sleepiness_2"
    const val FATIGUE_SLEEPINESS_3 = "fatigue_sleepiness_3"
    const val FATIGUE_SLEEPINESS_4 = "fatigue_sleepiness_4"
    const val FATIGUE_SLEEPINESS_5 = "fatigue_sleepiness_5"
    const val SLEEP_HABITS_1 = "sleep_habits_1"
    const val SLEEP_HABITS_2 = "sleep_habits_2"
    const val HEALTH_HISTORY_1 = "health_history_1"
    const val HEALTH_HISTORY_2 = "health_history_2"

    // Authenticated main app flow
    const val DASHBOARD = "dashboard"
    const val PROFILE = "profile"
    const val RECOMMENDATIONS = "recommendations"
    const val KEY_FACTORS = "key_factors"
    const val DATA_SOURCES = "data_sources"
    const val SETTINGS = "settings"
}

/**
 * Sets up the navigation graph for the entire app
 *
 * @param navController the navigation controller managing the navigation stack
 * @param isAuthenticated whether the user is authenticated
 * @param onSignOut callback to trigger sign out logic
 */
@Composable
fun AppNavGraph(
    navController: NavHostController,
    isAuthenticated: Boolean,
    onSignOut: () -> Unit
) {
    // Start destination is determined ONCE when the app first loads
    // Use remember to keep it stable across recompositions
    val startDestination = androidx.compose.runtime.remember {
        val destination = if (isAuthenticated) NavRoutes.DASHBOARD else NavRoutes.SPLASH
        android.util.Log.d("NavGraph", "🎯 Initial startDestination: $destination (isAuthenticated=$isAuthenticated)")
        destination
    }
    
    // For returning authenticated users at app start, navigate to dashboard
    LaunchedEffect(Unit) {
        if (isAuthenticated && startDestination == NavRoutes.DASHBOARD) {
            android.util.Log.d("NavGraph", "✅ Returning authenticated user - staying on dashboard")
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // ========================
        // UNAUTHENTICATED FLOW
        // ========================

        composable(NavRoutes.SPLASH) {
            WakeUpCallScreen(
                onNext = { navController.navigate(NavRoutes.INFO_CONSENT) },
                onSkip = { navController.navigate(NavRoutes.INFO_CONSENT) }
            )
        }

        composable(NavRoutes.INFO_CONSENT) {
            val authViewModel: com.example.wakeupcallapp.sleepapp.viewmodel.AuthViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                viewModelStoreOwner = androidx.compose.ui.platform.LocalContext.current as androidx.activity.ComponentActivity
            )
            val isAuthenticated by authViewModel.isAuthenticated.collectAsState()
            
            InfoConsentScreenContent(
                onNext = { 
                    android.util.Log.d("NavGraph", "📍 Info consent accepted - isAuthenticated=$isAuthenticated")
                    if (isAuthenticated) {
                        // User is logged in/signed up, go to onboarding
                        android.util.Log.d("NavGraph", "➡️ Navigating to DATA_CONSENT")
                        navController.navigate(NavRoutes.DATA_CONSENT) {
                            popUpTo(NavRoutes.INFO_CONSENT) { inclusive = true }
                        }
                    } else {
                        // First time app launch, go to auth
                        android.util.Log.d("NavGraph", "➡️ Navigating to AUTH")
                        navController.navigate(NavRoutes.AUTH)
                    }
                }
            )
        }

        composable(NavRoutes.AUTH) {
            AuthScreenContent(
                onLogIn = { navController.navigate(NavRoutes.LOGIN) },
                onSignUp = { navController.navigate(NavRoutes.SIGNUP) },
                onGuest = { navController.navigate(NavRoutes.DASHBOARD) }
            )
        }

        composable(NavRoutes.LOGIN) {
            LogInScreenContent(
                onSignInSuccess = { hasSurvey, _ ->
                    android.util.Log.d("NavGraph", "📍 Login Success - hasSurvey=$hasSurvey")
                    
                    if (hasSurvey) {
                        // User has survey data - go straight to dashboard
                        android.util.Log.d("NavGraph", "➡️ Navigating to DASHBOARD (has survey data)")
                        navController.navigate(NavRoutes.DASHBOARD) {
                            popUpTo(NavRoutes.AUTH) { inclusive = true }
                        }
                    } else {
                        // No survey yet - go to DATA_CONSENT (Google Fit), then survey
                        android.util.Log.d("NavGraph", "➡️ Navigating to DATA_CONSENT (no survey)")
                        navController.navigate(NavRoutes.DATA_CONSENT) {
                            popUpTo(NavRoutes.AUTH) { inclusive = true }
                        }
                    }
                },
                onBack = { navController.navigateUp() }
            )
        }

        composable(NavRoutes.SIGNUP) {
            SignUpScreenContent(
                onSignUpSuccess = { hasSurvey, hasAcceptedInfoConsent ->
                    android.util.Log.d("NavGraph", "📍 SignUp Success - hasSurvey=$hasSurvey, hasAcceptedInfoConsent=$hasAcceptedInfoConsent")
                    
                    // New users go directly to DATA_CONSENT (they already saw InfoConsent at app launch)
                    android.util.Log.d("NavGraph", "➡️ Navigating to DATA_CONSENT (new user)")
                    navController.navigate(NavRoutes.DATA_CONSENT) {
                        popUpTo(NavRoutes.AUTH) { inclusive = true }
                    }
                },
                onBack = { navController.navigateUp() }
            )
        }

        // ========================
        // ONBOARDING FLOW
        // (After Login/SignUp)
        // ========================

        composable(NavRoutes.DATA_CONSENT) {
            DataConsentScreenContent(
                onGrantPermission = { navController.navigate(NavRoutes.DEMOGRAPHICS) },
                onDeny = { navController.navigate(NavRoutes.DEMOGRAPHICS) }
            )
        }

        composable(NavRoutes.DEMOGRAPHICS) {
            DemographicsScreenContent(
                onNext = { navController.navigate(NavRoutes.HEALTH_HISTORY_1) },
                onBack = { navController.navigateUp() }
            )
        }

        composable(NavRoutes.HEALTH_HISTORY_1) {
            HealthHistory1ScreenContent(
                onNext = { navController.navigate(NavRoutes.HEALTH_HISTORY_2) },
                onBack = { navController.navigateUp() }
            )
        }

        composable(NavRoutes.HEALTH_HISTORY_2) {
            HealthHistory2ScreenContent(
                onNext = { navController.navigate(NavRoutes.SLEEP_HABITS_1) },
                onBack = { navController.navigateUp() }
            )
        }

        composable(NavRoutes.SLEEP_HABITS_1) {
            SleepHabits1ScreenContent(
                onNext = { navController.navigate(NavRoutes.SLEEP_HABITS_2) },
                onBack = { navController.navigateUp() }
            )
        }

        composable(NavRoutes.SLEEP_HABITS_2) {
            SleepHabits2ScreenContent(
                onNext = { navController.navigate(NavRoutes.FATIGUE_SLEEPINESS_1) },
                onBack = { navController.navigateUp() }
            )
        }

        composable(NavRoutes.FATIGUE_SLEEPINESS_1) {
            FatigueSleepiness1ScreenContent(
                onNext = { navController.navigate(NavRoutes.FATIGUE_SLEEPINESS_2) },
                onBack = { navController.navigateUp() }
            )
        }

        composable(NavRoutes.FATIGUE_SLEEPINESS_2) {
            FatigueSleepiness2ScreenContent(
                onNext = { navController.navigate(NavRoutes.FATIGUE_SLEEPINESS_3) },
                onBack = { navController.navigateUp() }
            )
        }

        composable(NavRoutes.FATIGUE_SLEEPINESS_3) {
            FatigueSleepiness3ScreenContent(
                onNext = { navController.navigate(NavRoutes.FATIGUE_SLEEPINESS_4) },
                onBack = { navController.navigateUp() }
            )
        }

        composable(NavRoutes.FATIGUE_SLEEPINESS_4) {
            FatigueSleepiness4ScreenContent(
                onNext = { navController.navigate(NavRoutes.FATIGUE_SLEEPINESS_5) },
                onBack = { navController.navigateUp() }
            )
        }

        composable(NavRoutes.FATIGUE_SLEEPINESS_5) {
            FatigueSleepiness5ScreenContent(
                onNext = {
                    // Navigate to dashboard and clear all back stack
                    navController.navigate(NavRoutes.DASHBOARD) {
                        popUpTo(NavRoutes.SPLASH) { inclusive = true }
                    }
                },
                onBack = { navController.navigateUp() }
            )
        }

        // ========================
        // AUTHENTICATED MAIN APP
        // ========================

        composable(NavRoutes.DASHBOARD) {
            DashboardScreen(
                navController = navController,
                onSignOut = onSignOut
            )
        }

        composable(NavRoutes.PROFILE) {
            ProfileScreen(navController = navController)
        }

        composable(NavRoutes.RECOMMENDATIONS) {
            RecommendationsScreen(navController = navController)
        }

        composable(NavRoutes.KEY_FACTORS) {
            KeyFactorsScreen(navController = navController)
        }

        composable(NavRoutes.DATA_SOURCES) {
            DataSourcesScreen(navController = navController)
        }

        composable(NavRoutes.SETTINGS) {
            SettingsScreen(navController = navController)
        }
    }
}
