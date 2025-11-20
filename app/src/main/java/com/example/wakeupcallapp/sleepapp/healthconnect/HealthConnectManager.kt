package com.example.wakeupcallapp.sleepapp.healthconnect

import android.content.Context
import android.util.Log
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.records.WeightRecord
import androidx.health.connect.client.records.HeightRecord
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * Health Connect Manager to handle permissions and data fetching
 * Replaces GoogleFitManager with modern Health Connect API
 */
class HealthConnectManager(private val context: Context) {

    companion object {
        private const val TAG = "HealthConnectManager"
        private const val PREFS_NAME = "health_connect_prefs"
        private const val KEY_USER_CONNECTED_PREFIX = "user_connected_"
    }
    
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    // Health Connect client
    private val healthConnectClient by lazy {
        HealthConnectClient.getOrCreate(context)
    }
    
    // Define the permissions we need
    val permissions = setOf(
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getWritePermission(StepsRecord::class),
        HealthPermission.getReadPermission(SleepSessionRecord::class),
        HealthPermission.getWritePermission(SleepSessionRecord::class),
        HealthPermission.getReadPermission(ActiveCaloriesBurnedRecord::class),
        HealthPermission.getWritePermission(ActiveCaloriesBurnedRecord::class),
        HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class),
        HealthPermission.getWritePermission(TotalCaloriesBurnedRecord::class),
        HealthPermission.getReadPermission(WeightRecord::class),
        HealthPermission.getWritePermission(WeightRecord::class),
        HealthPermission.getReadPermission(HeightRecord::class),
        HealthPermission.getWritePermission(HeightRecord::class),
        HealthPermission.getReadPermission(DistanceRecord::class),
        HealthPermission.getWritePermission(DistanceRecord::class),
        HealthPermission.getReadPermission(ExerciseSessionRecord::class),
        HealthPermission.getWritePermission(ExerciseSessionRecord::class)
    )

    /**
     * Check if Health Connect is available on this device
     */
    suspend fun isAvailable(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE
            } catch (e: Exception) {
                Log.e(TAG, "Error checking Health Connect availability: ${e.message}", e)
                false
            }
        }
    }

    /**
     * Check if all required permissions are granted
     */
    suspend fun hasAllPermissions(userId: String? = null): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val granted = healthConnectClient.permissionController.getGrantedPermissions()
                val hasPerms = permissions.all { it in granted }
                
                Log.d(TAG, "Permission check: granted=${granted.size}/${permissions.size}, hasAll=$hasPerms")
                
                // Check user-specific connection state
                if (userId != null) {
                    val userConnected = isUserConnected(userId)
                    Log.d(TAG, "User-specific connection state for $userId: $userConnected")
                    return@withContext hasPerms && userConnected
                }
                
                hasPerms
            } catch (e: Exception) {
                Log.e(TAG, "Error checking permissions: ${e.message}", e)
                false
            }
        }
    }
    
    /**
     * Check if a specific user has connected Health Connect
     */
    fun isUserConnected(userId: String): Boolean {
        return prefs.getBoolean("${KEY_USER_CONNECTED_PREFIX}$userId", false)
    }
    
    /**
     * Mark a user as connected to Health Connect
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
     * Fetch daily step counts for the last N days
     * @param days Number of days to look back (default 7)
     * @return Map of date string to step count
     */
    suspend fun getDailySteps(days: Int = 7): Map<String, Int> {
        return withContext(Dispatchers.IO) {
            try {
                if (!hasAllPermissions()) {
                    Log.e(TAG, "❌ No permissions granted for steps fetch")
                    return@withContext emptyMap()
                }
                
                Log.d(TAG, "📊 Fetching steps for last $days days from Health Connect...")
                
                // Calculate time range - from start of N days ago to end of today
                val endTime = ZonedDateTime.now()
                val startTime = endTime.minusDays(days.toLong()).toLocalDate().atStartOfDay(ZoneId.systemDefault())
                
                Log.d(TAG, "⏰ Time range: $startTime to $endTime")
                
                // Read steps records
                val request = ReadRecordsRequest(
                    recordType = StepsRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(
                        startTime.toInstant(),
                        endTime.toInstant()
                    )
                )
                
                val response = healthConnectClient.readRecords(request)
                Log.d(TAG, "📥 Received ${response.records.size} step records")
                
                // Group by date and sum steps
                val stepsMap = mutableMapOf<String, Int>()
                val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                
                response.records.forEach { record ->
                    val date = record.startTime
                        .atZone(record.startZoneOffset ?: ZoneId.systemDefault())
                        .toLocalDate()
                        .format(dateFormatter)
                    
                    stepsMap[date] = (stepsMap[date] ?: 0) + record.count.toInt()
                }
                
                // Fill in missing days with 0 steps
                var currentDate = startTime.toLocalDate()
                while (!currentDate.isAfter(endTime.toLocalDate())) {
                    val dateStr = currentDate.format(dateFormatter)
                    if (!stepsMap.containsKey(dateStr)) {
                        stepsMap[dateStr] = 0
                    }
                    currentDate = currentDate.plusDays(1)
                }
                
                val totalSteps = stepsMap.values.sum()
                Log.d(TAG, "✅ Fetched steps for $days days: ${stepsMap.size} days, $totalSteps total steps")
                
                if (totalSteps == 0) {
                    Log.w(TAG, "⚠️ WARNING: Total steps is 0. User may not have recorded any activity yet.")
                }
                
                stepsMap
            } catch (e: Exception) {
                Log.e(TAG, "❌ Failed to fetch steps: ${e.message}", e)
                emptyMap()
            }
        }
    }

    /**
     * Fetch total steps for today
     */
    suspend fun getTodaySteps(): Int {
        return withContext(Dispatchers.IO) {
            try {
                if (!hasAllPermissions()) {
                    Log.e(TAG, "❌ No permissions for today's steps")
                    return@withContext 0
                }
                
                Log.d(TAG, "📊 Fetching today's steps from Health Connect...")
                
                // Get today's time range (from midnight to now)
                val now = ZonedDateTime.now()
                val startOfDay = now.toLocalDate().atStartOfDay(ZoneId.systemDefault())
                
                Log.d(TAG, "⏰ Today's range: $startOfDay to $now")
                
                val request = ReadRecordsRequest(
                    recordType = StepsRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(
                        startOfDay.toInstant(),
                        now.toInstant()
                    )
                )
                
                val response = healthConnectClient.readRecords(request)
                val todaySteps = response.records.sumOf { it.count.toInt() }
                
                Log.d(TAG, "✅ Today's steps: $todaySteps (from ${response.records.size} records)")
                todaySteps
            } catch (e: Exception) {
                Log.e(TAG, "❌ Failed to fetch today's steps: ${e.message}", e)
                0
            }
        }
    }

    /**
     * Fetch daily sleep duration for the last N days
     * @param days Number of days to look back (default 7)
     * @return Map of date string to sleep duration in hours
     */
    suspend fun getDailySleepHours(days: Int = 7): Map<String, Double> {
        return withContext(Dispatchers.IO) {
            try {
                if (!hasAllPermissions()) {
                    Log.e(TAG, "❌ No permissions granted for sleep fetch")
                    return@withContext emptyMap()
                }
                
                Log.d(TAG, "😴 Fetching sleep data for last $days days from Health Connect...")
                
                // Calculate time range
                val endTime = ZonedDateTime.now()
                val startTime = endTime.minusDays(days.toLong()).toLocalDate().atStartOfDay(ZoneId.systemDefault())
                
                Log.d(TAG, "⏰ Sleep time range: $startTime to $endTime")
                
                // Read sleep session records
                val request = ReadRecordsRequest(
                    recordType = SleepSessionRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(
                        startTime.toInstant(),
                        endTime.toInstant()
                    )
                )
                
                val response = healthConnectClient.readRecords(request)
                Log.d(TAG, "📥 Received ${response.records.size} sleep sessions")
                
                // Group by date (using start date) and calculate total sleep hours
                val sleepMap = mutableMapOf<String, Double>()
                val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                
                response.records.forEach { session ->
                    val sessionStart = session.startTime
                        .atZone(session.startZoneOffset ?: ZoneId.systemDefault())
                    val sessionEnd = session.endTime
                        .atZone(session.endZoneOffset ?: ZoneId.systemDefault())
                    
                    // Attribute sleep to the start date
                    val date = sessionStart.toLocalDate().format(dateFormatter)
                    
                    // Calculate duration in hours
                    val durationMinutes = ChronoUnit.MINUTES.between(sessionStart, sessionEnd)
                    val hours = durationMinutes / 60.0
                    
                    // Accumulate sleep for the day (multiple sessions possible)
                    sleepMap[date] = (sleepMap[date] ?: 0.0) + hours
                    
                    Log.d(TAG, "😴 $date: +${String.format("%.1f", hours)}h - Total: ${String.format("%.1f", sleepMap[date])}h")
                }
                
                // Fill in missing days with 0.0 hours
                var currentDate = startTime.toLocalDate()
                while (!currentDate.isAfter(endTime.toLocalDate())) {
                    val dateStr = currentDate.format(dateFormatter)
                    if (!sleepMap.containsKey(dateStr)) {
                        sleepMap[dateStr] = 0.0
                    }
                    currentDate = currentDate.plusDays(1)
                }
                
                val avgSleep = sleepMap.values.filter { it > 0.0 }.average()
                Log.d(TAG, "✅ Fetched sleep for $days days: ${sleepMap.size} days")
                Log.d(TAG, "😴 Average sleep: ${String.format("%.1f", if (avgSleep.isNaN()) 0.0 else avgSleep)}h per night")
                
                sleepMap
            } catch (e: Exception) {
                Log.e(TAG, "❌ Failed to fetch sleep data: ${e.message}", e)
                emptyMap()
            }
        }
    }

    /**
     * Fetch active calories burned for today
     */
    suspend fun getActiveCalories(days: Int = 7): Double {
        return withContext(Dispatchers.IO) {
            try {
                if (!hasAllPermissions()) {
                    Log.e(TAG, "❌ No permissions for active calories")
                    return@withContext 0.0
                }
                
                Log.d(TAG, "🔥 Fetching active calories from Health Connect...")
                
                val endTime = ZonedDateTime.now()
                val startTime = endTime.minusDays(days.toLong()).toLocalDate().atStartOfDay(ZoneId.systemDefault())
                
                val request = ReadRecordsRequest(
                    recordType = ActiveCaloriesBurnedRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(
                        startTime.toInstant(),
                        endTime.toInstant()
                    )
                )
                
                val response = healthConnectClient.readRecords(request)
                val totalCalories = response.records.sumOf { it.energy.inKilocalories }
                
                Log.d(TAG, "✅ Active calories: $totalCalories kcal (from ${response.records.size} records)")
                totalCalories
            } catch (e: Exception) {
                Log.e(TAG, "❌ Failed to fetch active calories: ${e.message}", e)
                0.0
            }
        }
    }

    /**
     * Fetch total calories burned for today
     */
    suspend fun getTotalCalories(days: Int = 7): Double {
        return withContext(Dispatchers.IO) {
            try {
                if (!hasAllPermissions()) {
                    Log.e(TAG, "❌ No permissions for total calories")
                    return@withContext 0.0
                }
                
                Log.d(TAG, "🔥 Fetching total calories from Health Connect...")
                
                val endTime = ZonedDateTime.now()
                val startTime = endTime.minusDays(days.toLong()).toLocalDate().atStartOfDay(ZoneId.systemDefault())
                
                val request = ReadRecordsRequest(
                    recordType = TotalCaloriesBurnedRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(
                        startTime.toInstant(),
                        endTime.toInstant()
                    )
                )
                
                val response = healthConnectClient.readRecords(request)
                val totalCalories = response.records.sumOf { it.energy.inKilocalories }
                
                Log.d(TAG, "✅ Total calories: $totalCalories kcal (from ${response.records.size} records)")
                totalCalories
            } catch (e: Exception) {
                Log.e(TAG, "❌ Failed to fetch total calories: ${e.message}", e)
                0.0
            }
        }
    }

    /**
     * Fetch latest weight measurement
     */
    suspend fun getLatestWeight(): Double {
        return withContext(Dispatchers.IO) {
            try {
                if (!hasAllPermissions()) {
                    Log.e(TAG, "❌ No permissions for weight")
                    return@withContext 0.0
                }
                
                Log.d(TAG, "⚖️ Fetching weight from Health Connect...")
                
                val endTime = ZonedDateTime.now()
                val startTime = endTime.minusDays(90).toLocalDate().atStartOfDay(ZoneId.systemDefault())
                
                val request = ReadRecordsRequest(
                    recordType = WeightRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(
                        startTime.toInstant(),
                        endTime.toInstant()
                    )
                )
                
                val response = healthConnectClient.readRecords(request)
                val latestWeight = response.records.maxByOrNull { it.time }?.weight?.inKilograms ?: 0.0
                
                Log.d(TAG, "✅ Latest weight: $latestWeight kg (from ${response.records.size} records)")
                latestWeight
            } catch (e: Exception) {
                Log.e(TAG, "❌ Failed to fetch weight: ${e.message}", e)
                0.0
            }
        }
    }

    /**
     * Fetch latest height measurement
     */
    suspend fun getLatestHeight(): Double {
        return withContext(Dispatchers.IO) {
            try {
                if (!hasAllPermissions()) {
                    Log.e(TAG, "❌ No permissions for height")
                    return@withContext 0.0
                }
                
                Log.d(TAG, "📏 Fetching height from Health Connect...")
                
                val endTime = ZonedDateTime.now()
                val startTime = endTime.minusDays(365).toLocalDate().atStartOfDay(ZoneId.systemDefault())
                
                val request = ReadRecordsRequest(
                    recordType = HeightRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(
                        startTime.toInstant(),
                        endTime.toInstant()
                    )
                )
                
                val response = healthConnectClient.readRecords(request)
                val latestHeight = response.records.maxByOrNull { it.time }?.height?.inMeters ?: 0.0
                val heightCm = latestHeight * 100.0
                
                Log.d(TAG, "✅ Latest height: $heightCm cm (from ${response.records.size} records)")
                heightCm
            } catch (e: Exception) {
                Log.e(TAG, "❌ Failed to fetch height: ${e.message}", e)
                0.0
            }
        }
    }

    /**
     * Fetch total distance for the last N days
     */
    suspend fun getTotalDistance(days: Int = 7): Double {
        return withContext(Dispatchers.IO) {
            try {
                if (!hasAllPermissions()) {
                    Log.e(TAG, "❌ No permissions for distance")
                    return@withContext 0.0
                }
                
                Log.d(TAG, "🏃 Fetching distance from Health Connect...")
                
                val endTime = ZonedDateTime.now()
                val startTime = endTime.minusDays(days.toLong()).toLocalDate().atStartOfDay(ZoneId.systemDefault())
                
                val request = ReadRecordsRequest(
                    recordType = DistanceRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(
                        startTime.toInstant(),
                        endTime.toInstant()
                    )
                )
                
                val response = healthConnectClient.readRecords(request)
                val totalDistance = response.records.sumOf { it.distance.inMeters }
                
                Log.d(TAG, "✅ Total distance: $totalDistance m (from ${response.records.size} records)")
                totalDistance
            } catch (e: Exception) {
                Log.e(TAG, "❌ Failed to fetch distance: ${e.message}", e)
                0.0
            }
        }
    }

    /**
     * Get activity intensity from recent exercise sessions
     */
    suspend fun getActivityIntensity(): String {
        return withContext(Dispatchers.IO) {
            try {
                if (!hasAllPermissions()) {
                    Log.e(TAG, "❌ No permissions for exercise sessions")
                    return@withContext "No Data"
                }
                
                Log.d(TAG, "💪 Fetching activity intensity from Health Connect...")
                
                val endTime = ZonedDateTime.now()
                val startTime = endTime.minusDays(7).toLocalDate().atStartOfDay(ZoneId.systemDefault())
                
                val request = ReadRecordsRequest(
                    recordType = ExerciseSessionRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(
                        startTime.toInstant(),
                        endTime.toInstant()
                    )
                )
                
                val response = healthConnectClient.readRecords(request)
                
                if (response.records.isEmpty()) {
                    Log.d(TAG, "ℹ️ No exercise sessions found")
                    return@withContext "No Data"
                }
                
                // Calculate average intensity based on session count and types
                val sessionCount = response.records.size
                val intensity = when {
                    sessionCount >= 5 -> "High"
                    sessionCount >= 3 -> "Moderate"
                    sessionCount >= 1 -> "Low"
                    else -> "No Data"
                }
                
                Log.d(TAG, "✅ Activity intensity: $intensity ($sessionCount sessions in last 7 days)")
                intensity
            } catch (e: Exception) {
                Log.e(TAG, "❌ Failed to fetch activity intensity: ${e.message}", e)
                "No Data"
            }
        }
    }

    /**
     * Get comprehensive health data for survey submission and dashboard display
     * Fetches: today's steps, 7-day average, weekly patterns, and sleep data
     */
    suspend fun getHealthData(): HealthConnectData {
        return withContext(Dispatchers.IO) {
            Log.d(TAG, "📊 Fetching comprehensive Health Connect data...")
            
            // Fetch all data
            val todaySteps = getTodaySteps()
            val weeklySteps = getDailySteps(7)
            val dailySleep = getDailySleepHours(7)
            val activeCalories = getActiveCalories(7)
            val totalCalories = getTotalCalories(7)
            val weight = getLatestWeight()
            val height = getLatestHeight()
            val distance = getTotalDistance(7)
            val intensity = getActivityIntensity()
            
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
            
            Log.d(TAG, "✅ Comprehensive health data summary:")
            Log.d(TAG, "   📱 Today's steps: $todaySteps")
            Log.d(TAG, "   📊 7-day average: $averageSteps steps/day (${stepsWithData.size} days with data)")
            Log.d(TAG, "   📅 Weekly steps pattern: ${weeklySteps.size} days")
            Log.d(TAG, "   😴 Average sleep: ${String.format("%.1f", averageSleep)}h (${sleepWithData.size} nights with data)")
            Log.d(TAG, "   🌙 Weekly sleep pattern: ${dailySleep.size} nights")
            Log.d(TAG, "   🔥 Active calories: ${String.format("%.1f", activeCalories)} kcal")
            Log.d(TAG, "   🔥 Total calories: ${String.format("%.1f", totalCalories)} kcal")
            Log.d(TAG, "   ⚖️ Weight: ${String.format("%.1f", weight)} kg")
            Log.d(TAG, "   📏 Height: ${String.format("%.1f", height)} cm")
            Log.d(TAG, "   🏃 Distance: ${String.format("%.1f", distance)} m")
            Log.d(TAG, "   💪 Activity intensity: $intensity")
            
            // Log diagnostic information if no data found
            if (todaySteps == 0 && stepsWithData.isEmpty()) {
                Log.w(TAG, "⚠️ ============================================")
                Log.w(TAG, "⚠️ NO STEP DATA FOUND IN HEALTH CONNECT")
                Log.w(TAG, "⚠️ Possible reasons:")
                Log.w(TAG, "⚠️ 1. User hasn't recorded any activity yet")
                Log.w(TAG, "⚠️ 2. No health apps are writing step data to Health Connect")
                Log.w(TAG, "⚠️ 3. Phone's activity tracking is disabled")
                Log.w(TAG, "⚠️ Suggestion: Install a fitness tracker app that writes to Health Connect")
                Log.w(TAG, "⚠️ ============================================")
            }
            
            HealthConnectData(
                dailySteps = todaySteps,
                averageDailySteps = averageSteps,
                sleepDurationHours = averageSleep,
                weeklyStepsData = weeklySteps,
                weeklySleepData = dailySleep,
                lastSyncTime = System.currentTimeMillis(),
                activeCaloriesBurned = activeCalories,
                totalCaloriesBurned = totalCalories,
                weightKg = weight,
                heightCm = height,
                distanceMeters = distance,
                activityIntensity = intensity
            )
        }
    }
}

/**
 * Data class to hold Health Connect data
 * Maintains same structure as GoogleFitData for backend compatibility
 */
data class HealthConnectData(
    val dailySteps: Int,
    val averageDailySteps: Int,
    val sleepDurationHours: Double,
    val weeklyStepsData: Map<String, Int>,
    val weeklySleepData: Map<String, Double>,
    val lastSyncTime: Long,
    val activeCaloriesBurned: Double = 0.0,
    val totalCaloriesBurned: Double = 0.0,
    val weightKg: Double = 0.0,
    val heightCm: Double = 0.0,
    val distanceMeters: Double = 0.0,
    val activityIntensity: String = "--"
)
