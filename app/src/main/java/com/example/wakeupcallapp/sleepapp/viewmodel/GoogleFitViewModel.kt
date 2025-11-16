package com.example.wakeupcallapp.sleepapp.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.wakeupcallapp.sleepapp.googlefit.GoogleFitData
import com.example.wakeupcallapp.sleepapp.googlefit.GoogleFitManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel to manage Google Fit integration state
 */
class GoogleFitViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "GoogleFitViewModel"
    }

    private val googleFitManager = GoogleFitManager(application)
    private val prefs = application.getSharedPreferences("auth_prefs", Application.MODE_PRIVATE)

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _fitData = MutableStateFlow<GoogleFitData?>(null)
    val fitData: StateFlow<GoogleFitData?> = _fitData.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    private var currentUserId: String? = null

    init {
        checkConnectionStatus()
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
     * Check if Google Fit is currently connected for the current user
     */
    fun checkConnectionStatus() {
        // Get userId from SharedPreferences if not set
        if (currentUserId == null) {
            currentUserId = prefs.getString("userId", null)
        }
        
        val hasPerms = googleFitManager.hasPermissions(currentUserId)
        Log.d(TAG, "Checking Google Fit connection status for user $currentUserId: $hasPerms")
        _isConnected.value = hasPerms
        if (hasPerms) {
            Log.d(TAG, "Connected to Google Fit, fetching data...")
            fetchFitnessData()
        } else {
            Log.w(TAG, "Not connected to Google Fit - permissions not granted or user not connected")
            _fitData.value = null
        }
    }

    /**
     * Get the GoogleFitManager instance for requesting permissions
     */
    fun getManager(): GoogleFitManager = googleFitManager

    /**
     * Handle permission result from Google Fit
     */
    fun onPermissionResult(granted: Boolean) {
        Log.d(TAG, "Permission result received: $granted")
        if (granted) {
            // Save user-specific connection state
            if (currentUserId == null) {
                currentUserId = prefs.getString("userId", null)
            }
            currentUserId?.let { userId ->
                googleFitManager.setUserConnected(userId, true)
                Log.d(TAG, "✅ Saved connection state for user: $userId")
            }
            _isConnected.value = true
            fetchFitnessData()
        } else {
            _isConnected.value = false
            _errorMessage.value = "Google Fit permissions denied"
        }
    }

    /**
     * Fetch fitness data from Google Fit
     */
    fun fetchFitnessData() {
        Log.d(TAG, "Starting fitness data fetch...")
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            try {
                Log.d(TAG, "Calling GoogleFitManager.getFitnessData()...")
                val data = googleFitManager.getFitnessData()
                Log.d(TAG, "✅ Successfully fetched fitness data:")
                Log.d(TAG, "   - Daily Steps: ${data.dailySteps}")
                Log.d(TAG, "   - Average Steps: ${data.averageDailySteps}")
                Log.d(TAG, "   - Sleep Hours: ${data.sleepDurationHours}")
                Log.d(TAG, "   - Weekly Steps Data Points: ${data.weeklyStepsData.size}")
                Log.d(TAG, "   - Weekly Sleep Data Points: ${data.weeklySleepData.size}")
                _fitData.value = data
                if (data.weeklyStepsData.isEmpty() && data.weeklySleepData.isEmpty()) {
                    Log.w(TAG, "⚠️ Warning: No data returned from Google Fit. User may not have any activity/sleep data.")
                    _errorMessage.value = "No data available. Make sure you have Google Fit activity data."
                }
            } catch (e: Exception) {
                val errorMsg = "Failed to fetch fitness data: ${e.message}"
                Log.e(TAG, "❌ $errorMsg", e)
                _errorMessage.value = errorMsg
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Disconnect from Google Fit
     */
    fun disconnect() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Get userId if not set
                if (currentUserId == null) {
                    currentUserId = prefs.getString("userId", null)
                }
                
                val success = googleFitManager.revokePermissions(currentUserId)
                if (success) {
                    _isConnected.value = false
                    _fitData.value = null
                    Log.d(TAG, "✅ Successfully disconnected from Google Fit for user: $currentUserId")
                } else {
                    _errorMessage.value = "Failed to disconnect from Google Fit"
                }
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
