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
     * Subscribe to Google Fit data sources to enable background data collection
     * This should be called after permissions are granted
     */
    suspend fun subscribeToDataSources(): Boolean {
        val account = getGoogleAccount() ?: run {
            Log.e(TAG, "❌ No Google account for subscription")
            return false
        }
        
        if (!GoogleSignIn.hasPermissions(account, fitnessOptions)) {
            Log.e(TAG, "❌ No permissions for subscription")
            return false
        }
        
        return try {
            Log.d(TAG, "📡 Subscribing to Google Fit data sources...")
            
            // Subscribe to step count recording
            Fitness.getRecordingClient(context, account)
                .subscribe(DataType.TYPE_STEP_COUNT_DELTA)
                .await()
            Log.d(TAG, "✅ Subscribed to step count recording")
            
            // Subscribe to sleep recording
            try {
                Fitness.getRecordingClient(context, account)
                    .subscribe(DataType.TYPE_SLEEP_SEGMENT)
                    .await()
                Log.d(TAG, "✅ Subscribed to sleep segment recording")
            } catch (e: Exception) {
                Log.w(TAG, "⚠️ Could not subscribe to sleep: ${e.message}")
            }
            
            // Subscribe to activity recording
            try {
                Fitness.getRecordingClient(context, account)
                    .subscribe(DataType.TYPE_ACTIVITY_SEGMENT)
                    .await()
                Log.d(TAG, "✅ Subscribed to activity segment recording")
            } catch (e: Exception) {
                Log.w(TAG, "⚠️ Could not subscribe to activity: ${e.message}")
            }
            
            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to subscribe to data sources: ${e.message}", e)
            false
        }
    }
    
    /**
     * Check current subscriptions
     */
    suspend fun checkSubscriptions() {
        val account = getGoogleAccount() ?: return
        
        try {
            val subscriptions = Fitness.getRecordingClient(context, account)
                .listSubscriptions()
                .await()
            
            Log.d(TAG, "📋 Current subscriptions (${subscriptions.size}):")
            subscriptions.forEach { subscription ->
                Log.d(TAG, "  - ${subscription.dataType?.name ?: "unknown"}")
            }
            
            if (subscriptions.isEmpty()) {
                Log.w(TAG, "⚠️ No active subscriptions! Subscribing now...")
                subscribeToDataSources()
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to check subscriptions: ${e.message}", e)
        }
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
     * Fetch daily step count for the last N days using aggregated data
     * Uses multiple data sources to ensure we get data even if one source is empty
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
        
        Log.d(TAG, "✅ Account found: ${account.email} - Permissions verified for steps fetch")

        return try {
            Log.d(TAG, "📊 Fetching aggregated steps for last $days days...")
            
            // Set time range - start from beginning of N days ago to end of today
            val calendar = java.util.Calendar.getInstance()
            calendar.set(java.util.Calendar.HOUR_OF_DAY, 23)
            calendar.set(java.util.Calendar.MINUTE, 59)
            calendar.set(java.util.Calendar.SECOND, 59)
            val endTime = calendar.timeInMillis
            
            calendar.add(java.util.Calendar.DAY_OF_YEAR, -days)
            calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
            calendar.set(java.util.Calendar.MINUTE, 0)
            calendar.set(java.util.Calendar.SECOND, 0)
            val startTime = calendar.timeInMillis
            
            Log.d(TAG, "⏰ Time range: ${java.util.Date(startTime)} to ${java.util.Date(endTime)}")

            // FIRST ATTEMPT: Try with aggregated data (most reliable)
            val aggregateRequest = DataReadRequest.Builder()
                .aggregate(DataType.AGGREGATE_STEP_COUNT_DELTA)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build()

            Log.d(TAG, "📤 Attempt 1: Fetching AGGREGATED step data from all sources...")
            val aggregateResponse = Fitness.getHistoryClient(context, account)
                .readData(aggregateRequest)
                .await()

            val stepsMap = mutableMapOf<String, Int>()
            val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
            
            Log.d(TAG, "📥 Received ${aggregateResponse.buckets.size} daily buckets from Google Fit")
            
            if (aggregateResponse.buckets.isNotEmpty()) {
                aggregateResponse.buckets.forEachIndexed { index, bucket ->
                    val bucketStart = bucket.getStartTime(TimeUnit.MILLISECONDS)
                    val date = dateFormat.format(java.util.Date(bucketStart))
                    
                    var dailySteps = 0
                    
                    Log.d(TAG, "  Bucket $index: ${bucket.dataSets.size} data sets")
                    bucket.dataSets.forEach { dataSet ->
                        Log.d(TAG, "    DataSet: ${dataSet.dataSource.streamName} - ${dataSet.dataPoints.size} points")
                        dataSet.dataPoints.forEach { dataPoint ->
                            val steps = dataPoint.getValue(Field.FIELD_STEPS).asInt()
                            dailySteps += steps
                        }
                    }
                    
                    stepsMap[date] = dailySteps
                    Log.d(TAG, "📅 Day ${index + 1}: $date = $dailySteps steps")
                }
            }
            
            // SECOND ATTEMPT: If no aggregated data, try reading raw step deltas
            if (stepsMap.isEmpty() || stepsMap.values.sum() == 0) {
                Log.w(TAG, "⚠️ No aggregated data found. Trying RAW step deltas...")
                
                val rawRequest = DataReadRequest.Builder()
                    .read(DataType.TYPE_STEP_COUNT_DELTA)
                    .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                    .build()
                
                val rawResponse = Fitness.getHistoryClient(context, account)
                    .readData(rawRequest)
                    .await()
                
                Log.d(TAG, "📥 Raw data: ${rawResponse.dataSets.size} data sets")
                
                rawResponse.dataSets.forEach { dataSet ->
                    Log.d(TAG, "  DataSet: ${dataSet.dataSource.streamName} - ${dataSet.dataPoints.size} points")
                    dataSet.dataPoints.forEach { dataPoint ->
                        val pointStart = dataPoint.getStartTime(TimeUnit.MILLISECONDS)
                        val date = dateFormat.format(java.util.Date(pointStart))
                        val steps = dataPoint.getValue(Field.FIELD_STEPS).asInt()
                        
                        stepsMap[date] = (stepsMap[date] ?: 0) + steps
                    }
                }
                
                Log.d(TAG, "📊 After raw data: ${stepsMap.size} days with ${stepsMap.values.sum()} total steps")
            }
            
            // THIRD ATTEMPT: List available data sources
            if (stepsMap.isEmpty() || stepsMap.values.sum() == 0) {
                Log.w(TAG, "⚠️ Still no data. Checking available data sources...")
                try {
                    val dataSourcesResponse = Fitness.getSensorsClient(context, account)
                        .findDataSources(
                            com.google.android.gms.fitness.request.DataSourcesRequest.Builder()
                                .setDataTypes(DataType.TYPE_STEP_COUNT_DELTA)
                                .setDataSourceTypes(com.google.android.gms.fitness.data.DataSource.TYPE_DERIVED)
                                .build()
                        )
                        .await()
                    
                    Log.d(TAG, "📋 Available step data sources:")
                    dataSourcesResponse.forEach { dataSource ->
                        Log.d(TAG, "  - ${dataSource.streamIdentifier}")
                        Log.d(TAG, "    Name: ${dataSource.streamName}")
                        Log.d(TAG, "    App: ${dataSource.appPackageName}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to list data sources: ${e.message}")
                }
            }

            // Fill in missing days with 0 steps
            val currentCal = java.util.Calendar.getInstance()
            currentCal.timeInMillis = startTime
            while (currentCal.timeInMillis <= endTime) {
                val dateStr = dateFormat.format(currentCal.time)
                if (!stepsMap.containsKey(dateStr)) {
                    stepsMap[dateStr] = 0
                }
                currentCal.add(java.util.Calendar.DAY_OF_YEAR, 1)
            }

            val totalSteps = stepsMap.values.sum()
            Log.d(TAG, "✅ Successfully fetched steps for $days days: ${stepsMap.size} days total, $totalSteps total steps")
            
            if (totalSteps == 0) {
                Log.w(TAG, "⚠️ WARNING: Total steps is 0. User may not have any step data in Google Fit, or app needs to record some activity first.")
            }
            
            stepsMap
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to fetch steps: ${e.message}", e)
            e.printStackTrace()
            emptyMap()
        }
    }

    /**
     * Fetch total steps for today using real-time aggregated data
     * This gets the most up-to-date step count for the current day
     */
    suspend fun getTodaySteps(): Int {
        val account = getGoogleAccount() ?: run {
            Log.e(TAG, "❌ No Google account found for today's steps")
            return 0
        }
        
        if (!GoogleSignIn.hasPermissions(account, fitnessOptions)) {
            Log.e(TAG, "❌ No permissions for today's steps")
            return 0
        }

        return try {
            Log.d(TAG, "📊 Fetching today's steps in real-time...")
            
            // Get today's time range (from midnight to now)
            val calendar = java.util.Calendar.getInstance()
            val endTime = calendar.timeInMillis
            
            calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
            calendar.set(java.util.Calendar.MINUTE, 0)
            calendar.set(java.util.Calendar.SECOND, 0)
            calendar.set(java.util.Calendar.MILLISECOND, 0)
            val startTime = calendar.timeInMillis
            
            Log.d(TAG, "⏰ Today's range: ${java.util.Date(startTime)} to ${java.util.Date(endTime)}")

            val request = DataReadRequest.Builder()
                .aggregate(DataType.AGGREGATE_STEP_COUNT_DELTA)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .enableServerQueries()
                .build()

            val response = Fitness.getHistoryClient(context, account)
                .readData(request)
                .await()

            var todaySteps = 0
            response.buckets.forEach { bucket ->
                bucket.dataSets.forEach { dataSet ->
                    dataSet.dataPoints.forEach { dataPoint ->
                        todaySteps += dataPoint.getValue(Field.FIELD_STEPS).asInt()
                    }
                }
            }
            
            // If no buckets, check direct data points
            if (todaySteps == 0) {
                response.dataSets.forEach { dataSet ->
                    dataSet.dataPoints.forEach { dataPoint ->
                        todaySteps += dataPoint.getValue(Field.FIELD_STEPS).asInt()
                    }
                }
            }

            Log.d(TAG, "✅ Today's steps: $todaySteps")
            todaySteps
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to fetch today's steps: ${e.message}", e)
            e.printStackTrace()
            0
        }
    }

    /**
     * Fetch sleep duration for the last N days
     * Aggregates sleep segments to get total sleep per day
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
            Log.d(TAG, "😴 Fetching sleep data for last $days days...")
            
            // Set time range properly
            val calendar = java.util.Calendar.getInstance()
            calendar.set(java.util.Calendar.HOUR_OF_DAY, 23)
            calendar.set(java.util.Calendar.MINUTE, 59)
            calendar.set(java.util.Calendar.SECOND, 59)
            val endTime = calendar.timeInMillis
            
            calendar.add(java.util.Calendar.DAY_OF_YEAR, -days)
            calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
            calendar.set(java.util.Calendar.MINUTE, 0)
            calendar.set(java.util.Calendar.SECOND, 0)
            val startTime = calendar.timeInMillis
            
            Log.d(TAG, "⏰ Sleep time range: ${java.util.Date(startTime)} to ${java.util.Date(endTime)}")

            val request = DataReadRequest.Builder()
                .read(DataType.TYPE_SLEEP_SEGMENT)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .enableServerQueries()
                .build()

            val response = Fitness.getHistoryClient(context, account)
                .readData(request)
                .await()

            val sleepMap = mutableMapOf<String, Double>()
            val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
            
            Log.d(TAG, "📥 Received ${response.dataSets.size} sleep datasets from Google Fit")
            response.dataSets.forEach { dataSet ->
                Log.d(TAG, "Processing sleep dataset with ${dataSet.dataPoints.size} sleep segments")
                dataSet.dataPoints.forEach { dataPoint ->
                    // Sleep segments can span across midnight, so we attribute to the start date
                    val sleepStart = dataPoint.getStartTime(TimeUnit.MILLISECONDS)
                    val sleepEnd = dataPoint.getEndTime(TimeUnit.MILLISECONDS)
                    val date = dateFormat.format(java.util.Date(sleepStart))
                    
                    val durationMillis = sleepEnd - sleepStart
                    val hours = durationMillis / (1000.0 * 60 * 60)
                    
                    // Accumulate sleep for the day (multiple sleep segments possible)
                    sleepMap[date] = (sleepMap[date] ?: 0.0) + hours
                    
                    val sleepType = try {
                        dataPoint.getValue(Field.FIELD_SLEEP_SEGMENT_TYPE)?.asInt()?.let { type ->
                            when (type) {
                                1 -> "awake"
                                2 -> "sleep"
                                3 -> "out-of-bed"
                                4 -> "light sleep"
                                5 -> "deep sleep"
                                6 -> "REM sleep"
                                else -> "unknown"
                            }
                        } ?: "unspecified"
                    } catch (e: Exception) {
                        "unspecified"
                    }
                    
                    Log.d(TAG, "😴 $date: +${String.format("%.1f", hours)}h ($sleepType) - Total: ${String.format("%.1f", sleepMap[date])}h")
                }
            }

            // Fill in missing days with 0.0 hours
            val currentCal = java.util.Calendar.getInstance()
            currentCal.timeInMillis = startTime
            while (currentCal.timeInMillis <= endTime) {
                val dateStr = dateFormat.format(currentCal.time)
                if (!sleepMap.containsKey(dateStr)) {
                    sleepMap[dateStr] = 0.0
                }
                currentCal.add(java.util.Calendar.DAY_OF_YEAR, 1)
            }

            Log.d(TAG, "✅ Fetched sleep for $days days: ${sleepMap.size} days with data")
            Log.d(TAG, "😴 Average sleep: ${String.format("%.1f", sleepMap.values.average())}h per night")
            sleepMap
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to fetch sleep data: ${e.message}", e)
            e.printStackTrace()
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
     * Get comprehensive fitness data for survey submission and dashboard display
     * Fetches: today's steps, 7-day average, weekly patterns, and sleep data
     */
    suspend fun getFitnessData(): GoogleFitData {
        Log.d(TAG, "📊 Fetching comprehensive Google Fit data...")
        
        // First check subscriptions
        checkSubscriptions()
        
        // Fetch all data
        val todaySteps = getTodaySteps()
        val weeklySteps = getDailySteps(7)
        val dailySleep = getDailySleepHours(7)
        
        // Calculate averages only from days with actual data (non-zero values)
        val stepsWithData = weeklySteps.values.filter { it > 0 }
        val averageSteps = if (stepsWithData.isNotEmpty()) {
            stepsWithData.sum() / stepsWithData.size
        } else {
            todaySteps // Fallback to today's steps if no weekly data
        }
        
        val sleepWithData = dailySleep.values.filter { it > 0.0 }
        val averageSleep = if (sleepWithData.isNotEmpty()) {
            sleepWithData.average()
        } else {
            0.0
        }
        
        Log.d(TAG, "✅ Comprehensive fitness data summary:")
        Log.d(TAG, "   📱 Today's steps: $todaySteps")
        Log.d(TAG, "   📊 7-day average: $averageSteps steps/day (${stepsWithData.size} days with data)")
        Log.d(TAG, "   📅 Weekly steps pattern: ${weeklySteps.size} days")
        Log.d(TAG, "   😴 Average sleep: ${String.format("%.1f", averageSleep)}h (${sleepWithData.size} nights with data)")
        Log.d(TAG, "   🌙 Weekly sleep pattern: ${dailySleep.size} nights")
        
        // Log diagnostic information if no data found
        if (todaySteps == 0 && stepsWithData.isEmpty()) {
            Log.w(TAG, "⚠️ ============================================")
            Log.w(TAG, "⚠️ NO STEP DATA FOUND IN GOOGLE FIT")
            Log.w(TAG, "⚠️ Possible reasons:")
            Log.w(TAG, "⚠️ 1. User hasn't walked with their phone recently")
            Log.w(TAG, "⚠️ 2. Google Fit app needs to be opened at least once")
            Log.w(TAG, "⚠️ 3. Phone's activity recognition is disabled")
            Log.w(TAG, "⚠️ 4. Battery optimization is preventing background tracking")
            Log.w(TAG, "⚠️ Suggestion: Open Google Fit app and check if it shows step data")
            Log.w(TAG, "⚠️ ============================================")
        }

        return GoogleFitData(
            dailySteps = todaySteps,
            averageDailySteps = averageSteps,
            sleepDurationHours = averageSleep,
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
