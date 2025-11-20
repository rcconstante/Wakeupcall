package com.example.wakeupcallapp.sleepapp.viewmodel

import android.app.Application
import android.util.Log
import androidx.activity.result.contract.ActivityResultContract
import androidx.health.connect.client.PermissionController
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.wakeupcallapp.sleepapp.healthconnect.HealthConnectData
import com.example.wakeupcallapp.sleepapp.healthconnect.HealthConnectManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel to manage Health Connect integration state
 * Replaces GoogleFitViewModel with Health Connect API
 */
class HealthConnectViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "HealthConnectViewModel"
    }

    private val healthConnectManager = HealthConnectManager(application)
    private val prefs = application.getSharedPreferences("auth_prefs", Application.MODE_PRIVATE)

    private val _isAvailable = MutableStateFlow(false)
    val isAvailable: StateFlow<Boolean> = _isAvailable.asStateFlow()

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _healthData = MutableStateFlow<HealthConnectData?>(null)
    val healthData: StateFlow<HealthConnectData?> = _healthData.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    private var currentUserId: String? = null

    init {
        checkAvailability()
    }
    
    /**
     * Check if Health Connect is available on this device
     */
    private fun checkAvailability() {
        viewModelScope.launch {
            try {
                val available = healthConnectManager.isAvailable()
                _isAvailable.value = available
                
                if (available) {
                    Log.d(TAG, "✅ Health Connect is available on this device")
                    checkConnectionStatus()
                } else {
                    Log.w(TAG, "⚠️ Health Connect is not available on this device")
                    _errorMessage.value = "Health Connect is not available. Please install it from Play Store."
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error checking Health Connect availability: ${e.message}", e)
                _isAvailable.value = false
                _errorMessage.value = "Failed to check Health Connect availability"
            }
        }
    }
    
    /**
     * Set the current user ID for user-specific connection state
     */
    fun setUserId(userId: String?) {
        Log.d(TAG, "Setting userId: $userId")
        currentUserId = userId
        checkConnectionStatus()
    }

    /**
     * Check if Health Connect is currently connected for the current user
     */
    fun checkConnectionStatus() {
        viewModelScope.launch {
            try {
                // Get userId from SharedPreferences if not set
                if (currentUserId == null) {
                    currentUserId = prefs.getString("userId", null)
                }
                
                val hasPerms = healthConnectManager.hasAllPermissions(currentUserId)
                Log.d(TAG, "Checking Health Connect connection status for user $currentUserId: $hasPerms")
                _isConnected.value = hasPerms
                
                if (hasPerms) {
                    Log.d(TAG, "Connected to Health Connect, fetching data...")
                    fetchHealthData()
                } else {
                    Log.w(TAG, "Not connected to Health Connect - permissions not granted or user not connected")
                    _healthData.value = null
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error checking connection status: ${e.message}", e)
                _isConnected.value = false
            }
        }
    }

    /**
     * Get the Health Connect permission contract for requesting permissions
     */
    fun getPermissionContract(): ActivityResultContract<Set<String>, Set<String>> {
        return PermissionController.createRequestPermissionResultContract()
    }
    
    /**
     * Get the set of permissions to request
     */
    fun getPermissions(): Set<String> {
        return healthConnectManager.permissions
    }
    
    /**
     * Get the set of permissions to request (alias for getPermissions)
     */
    fun getPermissionsToRequest(): Set<String> {
        return healthConnectManager.permissions
    }

    /**
     * Handle permission result from Health Connect
     */
    fun onPermissionResult(grantedPermissions: Set<String>) {
        viewModelScope.launch {
            Log.d(TAG, "Permission result received: ${grantedPermissions.size}/${healthConnectManager.permissions.size} granted")
            
            val allGranted = healthConnectManager.permissions.all { it in grantedPermissions }
            
            if (allGranted) {
                // Save user-specific connection state
                if (currentUserId == null) {
                    currentUserId = prefs.getString("userId", null)
                }
                currentUserId?.let { userId ->
                    healthConnectManager.setUserConnected(userId, true)
                    Log.d(TAG, "✅ Saved connection state for user: $userId")
                }
                _isConnected.value = true
                
                // Fetch data immediately
                fetchHealthData()
            } else {
                _isConnected.value = false
                val deniedCount = healthConnectManager.permissions.size - grantedPermissions.size
                _errorMessage.value = "Health Connect permissions denied ($deniedCount required permissions not granted)"
                Log.w(TAG, "⚠️ Not all permissions granted. Denied: ${healthConnectManager.permissions - grantedPermissions}")
            }
        }
    }

    /**
     * Fetch health data from Health Connect
     */
    fun fetchHealthData() {
        Log.d(TAG, "Starting health data fetch...")
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            try {
                Log.d(TAG, "Calling HealthConnectManager.getHealthData()...")
                val data = healthConnectManager.getHealthData()
                Log.d(TAG, "✅ Successfully fetched health data:")
                Log.d(TAG, "   - Daily Steps: ${data.dailySteps}")
                Log.d(TAG, "   - Average Steps: ${data.averageDailySteps}")
                Log.d(TAG, "   - Sleep Hours: ${data.sleepDurationHours}")
                Log.d(TAG, "   - Weekly Steps Data Points: ${data.weeklyStepsData.size}")
                Log.d(TAG, "   - Weekly Sleep Data Points: ${data.weeklySleepData.size}")
                _healthData.value = data
                
                if (data.weeklyStepsData.isEmpty() && data.weeklySleepData.isEmpty()) {
                    Log.w(TAG, "⚠️ Warning: No data returned from Health Connect. User may not have any activity/sleep data.")
                    _errorMessage.value = "No data available. Make sure you have health tracking apps that write to Health Connect."
                }
            } catch (e: Exception) {
                val errorMsg = "Failed to fetch health data: ${e.message}"
                Log.e(TAG, "❌ $errorMsg", e)
                _errorMessage.value = errorMsg
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Disconnect from Health Connect
     */
    fun disconnect() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Get userId if not set
                if (currentUserId == null) {
                    currentUserId = prefs.getString("userId", null)
                }
                
                // Clear user-specific connection state
                currentUserId?.let { userId ->
                    healthConnectManager.setUserConnected(userId, false)
                    Log.d(TAG, "✅ Successfully disconnected from Health Connect for user: $userId")
                }
                
                _isConnected.value = false
                _healthData.value = null
                
            } catch (e: Exception) {
                _errorMessage.value = "Error disconnecting: ${e.message}"
                Log.e(TAG, "❌ Error disconnecting", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _errorMessage.value = null
    }
}
