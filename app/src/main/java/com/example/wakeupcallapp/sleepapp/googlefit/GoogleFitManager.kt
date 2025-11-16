package com.example.wakeupcallapp.sleepapp.googlefit

import android.app.Activity
import android.content.Context
import android.util.Log
import com.example.wakeupcallapp.sleepapp.MainActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.data.Field
import com.google.android.gms.fitness.request.DataReadRequest
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit

/**
 * Google Fit Manager to handle authentication and data fetching
 */
class GoogleFitManager(private val context: Context) {

    companion object {
        private const val TAG = "GoogleFitManager"
        const val GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 1001
        private const val PREFS_NAME = "google_fit_prefs"
        private const val KEY_USER_CONNECTED_PREFIX = "user_connected_"
    }
    
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // Define what data types we want to access
    private val fitnessOptions = FitnessOptions.builder()
        .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.TYPE_SLEEP_SEGMENT, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.TYPE_ACTIVITY_SEGMENT, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.TYPE_DISTANCE_DELTA, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.TYPE_CALORIES_EXPENDED, FitnessOptions.ACCESS_READ)
        .build()

    /**
     * Check if user has already granted Google Fit permissions
     * This checks both Android account permissions AND user-specific connection state
     */
    fun hasPermissions(userId: String? = null): Boolean {
        val account = getGoogleAccount()
        if (account == null) {
            Log.w(TAG, "⚠️ No Google account found - not signed in")
            return false
        }
        
        val hasPerms = GoogleSignIn.hasPermissions(account, fitnessOptions)
        Log.d(TAG, "Permission check: account=${account.email}, hasPermissions=$hasPerms")
        
        // Check user-specific connection state
        if (userId != null) {
            val userConnected = isUserConnected(userId)
            Log.d(TAG, "User-specific connection state for $userId: $userConnected")
            return hasPerms && userConnected
        }
        
        return hasPerms
    }
    
    /**
     * Check if a specific user has connected Google Fit
     */
    fun isUserConnected(userId: String): Boolean {
        return prefs.getBoolean("${KEY_USER_CONNECTED_PREFIX}$userId", false)
    }
    
    /**
     * Mark a user as connected to Google Fit
     */
    fun setUserConnected(userId: String, connected: Boolean) {
        Log.d(TAG, "Setting user $userId connected state: $connected")
        prefs.edit().putBoolean("${KEY_USER_CONNECTED_PREFIX}$userId", connected).apply()
    }
    
    /**
     * Clear all user connection states (for testing/cleanup)
     */
    fun clearAllUserConnections() {
        Log.d(TAG, "Clearing all user connection states")
        prefs.edit().clear().apply()
    }

    /**
     * Get the Google account if signed in
     */
    fun getGoogleAccount(): GoogleSignInAccount? {
        return GoogleSignIn.getAccountForExtension(context, fitnessOptions)
    }

    /**
     * Request Google Fit permissions from the user
     * Call this from MainActivity
     */
    fun requestPermissions(activity: MainActivity) {
        Log.d(TAG, "🔐 Requesting Google Fit permissions...")
        
        // Check if already have permissions
        val currentAccount = getGoogleAccount()
        if (currentAccount != null && GoogleSignIn.hasPermissions(currentAccount, fitnessOptions)) {
            Log.d(TAG, "✅ Permissions already granted for ${currentAccount.email}")
            MainActivity.onGoogleFitPermissionResult?.invoke(true)
            return
        }
        
        // Get account to use for permission request
        val account = currentAccount ?: GoogleSignIn.getLastSignedInAccount(context)
        
        Log.d(TAG, "📋 Account for permission request: ${account?.email ?: "null"}")
        Log.d(TAG, "📋 Requesting permissions via GoogleSignIn.requestPermissions()...")
        
        // Request Google Fit permissions directly
        // This will show the OAuth consent screen
        GoogleSignIn.requestPermissions(
            activity,
            GOOGLE_FIT_PERMISSIONS_REQUEST_CODE,
            account,
            fitnessOptions
        )
        
        Log.d(TAG, "🚀 Permission request sent to Google Play Services")
    }

    /**
     * Revoke Google Fit permissions (disconnect)
     * This disconnects at both the Android account level AND clears user-specific state
     */
    suspend fun revokePermissions(userId: String? = null): Boolean {
        return try {
            val account = getGoogleAccount()
            if (account != null) {
                Log.d(TAG, "🔓 Revoking Google Fit permissions for account: ${account.email}")
                
                // Disable Fitness API
                try {
                    Fitness.getConfigClient(context, account)
                        .disableFit()
                        .await()
                    Log.d(TAG, "✅ Successfully disabled Fitness API")
                } catch (e: Exception) {
                    Log.e(TAG, "⚠️ Failed to disable Fitness API: ${e.message}", e)
                }
                
                // Revoke access from Google account (this fully disconnects)
                try {
                    val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .build()
                    val googleSignInClient = GoogleSignIn.getClient(context, signInOptions)
                    googleSignInClient.revokeAccess().await()
                    Log.d(TAG, "✅ Successfully revoked Google account access")
                } catch (e: Exception) {
                    Log.e(TAG, "⚠️ Failed to revoke access: ${e.message}", e)
                }
            }
            
            // Clear user-specific connection state
            if (userId != null) {
                setUserConnected(userId, false)
                Log.d(TAG, "✅ Cleared connection state for user: $userId")
            }
            
            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to revoke permissions", e)
            false
        }
    }

    /**
     * Fetch daily step count for the last N days
     * @param days Number of days to look back (default 7)
     * @return Map of date string to step count
     */
    suspend fun getDailySteps(days: Int = 7): Map<String, Int> {
        val account = getGoogleAccount() ?: run {
            Log.e(TAG, "❌ No Google account found for steps fetch - permissions not granted")
            return emptyMap()
        }
        
        if (!GoogleSignIn.hasPermissions(account, fitnessOptions)) {
            Log.e(TAG, "❌ No permissions granted for steps fetch - OAuth consent required")
            return emptyMap()
        }
        
        Log.d(TAG, "✅ Account found and permissions verified for steps fetch")

        return try {
            Log.d(TAG, "Fetching steps for last $days days...")
            val endTime = System.currentTimeMillis()
            val startTime = endTime - TimeUnit.DAYS.toMillis(days.toLong())

            val request = DataReadRequest.Builder()
                .aggregate(DataType.AGGREGATE_STEP_COUNT_DELTA)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build()

            val response = Fitness.getHistoryClient(context, account)
                .readData(request)
                .await()

            val stepsMap = mutableMapOf<String, Int>()
            
            Log.d(TAG, "Received ${response.buckets.size} buckets from Google Fit")
            response.buckets.forEach { bucket ->
                val dataSets = bucket.dataSets
                Log.d(TAG, "Processing bucket with ${dataSets.size} datasets")
                dataSets.forEach { dataSet ->
                    Log.d(TAG, "Processing dataset with ${dataSet.dataPoints.size} data points")
                    dataSet.dataPoints.forEach { dataPoint ->
                        val date = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
                            .format(java.util.Date(dataPoint.getStartTime(TimeUnit.MILLISECONDS)))
                        val steps = dataPoint.getValue(Field.FIELD_STEPS).asInt()
                        stepsMap[date] = steps
                        Log.d(TAG, "$date: $steps steps")
                    }
                }
            }

            Log.d(TAG, "✅ Fetched steps for $days days: ${stepsMap.size} days with data")
            stepsMap
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to fetch steps: ${e.message}", e)
            emptyMap()
        }
    }

    /**
     * Fetch total steps for today
     */
    suspend fun getTodaySteps(): Int {
        val stepsMap = getDailySteps(1)
        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
            .format(java.util.Date())
        return stepsMap[today] ?: 0
    }

    /**
     * Fetch sleep duration for the last N days
     * @param days Number of days to look back (default 7)
     * @return Map of date string to sleep duration in hours
     */
    suspend fun getDailySleepHours(days: Int = 7): Map<String, Double> {
        val account = getGoogleAccount() ?: run {
            Log.e(TAG, "❌ No Google account found for sleep fetch - permissions not granted")
            return emptyMap()
        }
        
        if (!GoogleSignIn.hasPermissions(account, fitnessOptions)) {
            Log.e(TAG, "❌ No permissions granted for sleep fetch - OAuth consent required")
            return emptyMap()
        }

        return try {
            Log.d(TAG, "Fetching sleep data for last $days days...")
            val endTime = System.currentTimeMillis()
            val startTime = endTime - TimeUnit.DAYS.toMillis(days.toLong())

            val request = DataReadRequest.Builder()
                .read(DataType.TYPE_SLEEP_SEGMENT)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build()

            val response = Fitness.getHistoryClient(context, account)
                .readData(request)
                .await()

            val sleepMap = mutableMapOf<String, Double>()
            
            Log.d(TAG, "Received ${response.dataSets.size} datasets from Google Fit for sleep")
            response.dataSets.forEach { dataSet ->
                Log.d(TAG, "Processing sleep dataset with ${dataSet.dataPoints.size} data points")
                dataSet.dataPoints.forEach { dataPoint ->
                    val date = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
                        .format(java.util.Date(dataPoint.getStartTime(TimeUnit.MILLISECONDS)))
                    
                    val durationMillis = dataPoint.getEndTime(TimeUnit.MILLISECONDS) - 
                                        dataPoint.getStartTime(TimeUnit.MILLISECONDS)
                    val hours = durationMillis / (1000.0 * 60 * 60)
                    
                    sleepMap[date] = (sleepMap[date] ?: 0.0) + hours
                    Log.d(TAG, "$date: ${String.format("%.1f", hours)}h")
                }
            }

            Log.d(TAG, "✅ Fetched sleep for $days days: ${sleepMap.size} days with data")
            sleepMap
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to fetch sleep data: ${e.message}", e)
            emptyMap()
        }
    }

    /**
     * Fetch average sleep duration for the last N days
     */
    suspend fun getAverageSleepHours(days: Int = 7): Double {
        val sleepMap = getDailySleepHours(days)
        return if (sleepMap.isNotEmpty()) {
            sleepMap.values.sum() / sleepMap.size
        } else {
            0.0
        }
    }

    /**
     * Get comprehensive fitness data for survey submission
     */
    suspend fun getFitnessData(): GoogleFitData {
        val todaySteps = getTodaySteps()
        val weeklySteps = getDailySteps(7)
        val averageSteps = if (weeklySteps.isNotEmpty()) {
            weeklySteps.values.sum() / weeklySteps.size
        } else {
            0
        }
        
        val sleepHours = getAverageSleepHours(7)
        val dailySleep = getDailySleepHours(7)

        return GoogleFitData(
            dailySteps = todaySteps,
            averageDailySteps = averageSteps,
            sleepDurationHours = sleepHours,
            weeklyStepsData = weeklySteps,
            weeklySleepData = dailySleep,
            lastSyncTime = System.currentTimeMillis()
        )
    }
}

/**
 * Data class to hold Google Fit data
 */
data class GoogleFitData(
    val dailySteps: Int,
    val averageDailySteps: Int,
    val sleepDurationHours: Double,
    val weeklyStepsData: Map<String, Int>,
    val weeklySleepData: Map<String, Double>,
    val lastSyncTime: Long
)
