package com.example.wakeupcallapp.sleepapp.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.wakeupcallapp.sleepapp.data.models.User
import com.example.wakeupcallapp.sleepapp.data.repository.ApiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel to manage authentication state throughout the app
 *
 * This ViewModel maintains the user's authentication status and provides
 * methods for sign in, sign up, and sign out operations.
 */
class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ApiRepository()
    private val prefs = application.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _authToken = MutableStateFlow<String?>(null)
    val authToken: StateFlow<String?> = _authToken.asStateFlow()

    private val _hasSurvey = MutableStateFlow(false)
    val hasSurvey: StateFlow<Boolean> = _hasSurvey.asStateFlow()
    
    private val _hasAcceptedInfoConsent = MutableStateFlow(false)
    val hasAcceptedInfoConsent: StateFlow<Boolean> = _hasAcceptedInfoConsent.asStateFlow()

    private val _isGuestMode = MutableStateFlow(false)
    val isGuestMode: StateFlow<Boolean> = _isGuestMode.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        // Restore session from SharedPreferences
        val savedToken = prefs.getString("auth_token", null)
        val savedUserId = prefs.getInt("user_id", -1)
        val savedFirstName = prefs.getString("first_name", null)
        val savedLastName = prefs.getString("last_name", null)
        val savedEmail = prefs.getString("email", null)
        val savedHasSurvey = prefs.getBoolean("has_survey", false)
        val savedHasAcceptedInfoConsent = prefs.getBoolean("has_accepted_info_consent", false)
        
        if (savedToken != null && savedUserId != -1 && savedFirstName != null && savedLastName != null && savedEmail != null) {
            _authToken.value = savedToken
            _currentUser.value = User(savedUserId, savedFirstName, savedLastName, savedEmail)
            _hasSurvey.value = savedHasSurvey
            _hasAcceptedInfoConsent.value = savedHasAcceptedInfoConsent
            _isAuthenticated.value = true
        }
    }

    /**
     * Sign in with email and password
     *
     * @param email the user's email
     * @param password the user's password
     * @param onSuccess callback when login succeeds
     * @param onError callback when login fails
     */
    fun signIn(
        email: String, 
        password: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val result = repository.login(email, password)
                
                result.onSuccess { response ->
                    if (response.success && response.authToken != null && response.user != null) {
                        _isAuthenticated.value = true
                        _currentUser.value = response.user
                        _authToken.value = response.authToken
                        _hasSurvey.value = response.hasSurvey ?: false
                        
                        // Save to SharedPreferences
                        prefs.edit().apply {
                            putString("auth_token", response.authToken)
                            putInt("user_id", response.user.id)
                            putString("first_name", response.user.firstName)
                            putString("last_name", response.user.lastName)
                            putString("email", response.user.email)
                            putBoolean("has_survey", response.hasSurvey ?: false)
                            // Keep existing hasAcceptedInfoConsent for returning users
                            apply()
                        }
                        
                        onSuccess()
                    } else {
                        val error = response.error ?: "Login failed"
                        _errorMessage.value = error
                        onError(error)
                    }
                }.onFailure { error ->
                    val errorMsg = error.message ?: "Login failed"
                    _errorMessage.value = errorMsg
                    onError(errorMsg)
                }
            } catch (e: Exception) {
                val errorMsg = e.message ?: "Unknown error occurred"
                _errorMessage.value = errorMsg
                onError(errorMsg)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Sign up with user details
     *
     * @param email the user's email
     * @param password the user's password
     * @param firstName the user's first name
     * @param lastName the user's last name
     * @param onSuccess callback when signup succeeds
     * @param onError callback when signup fails
     */
    fun signUp(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val result = repository.signUp(firstName, lastName, email, password)
                
                result.onSuccess { response ->
                    if (response.success && response.authToken != null && response.user != null) {
                        _isAuthenticated.value = true
                        _currentUser.value = response.user
                        _authToken.value = response.authToken
                        _hasSurvey.value = response.hasSurvey ?: false
                        _hasAcceptedInfoConsent.value = false // New users must accept info consent
                        
                        // Save to SharedPreferences
                        prefs.edit().apply {
                            putString("auth_token", response.authToken)
                            putInt("user_id", response.user.id)
                            putString("first_name", response.user.firstName)
                            putString("last_name", response.user.lastName)
                            putString("email", response.user.email)
                            putBoolean("has_survey", response.hasSurvey ?: false)
                            putBoolean("has_accepted_info_consent", false)
                            apply()
                        }
                        
                        onSuccess()
                    } else {
                        val error = response.error ?: "Sign up failed"
                        _errorMessage.value = error
                        onError(error)
                    }
                }.onFailure { error ->
                    val errorMsg = error.message ?: "Sign up failed"
                    _errorMessage.value = errorMsg
                    onError(errorMsg)
                }
            } catch (e: Exception) {
                val errorMsg = e.message ?: "Unknown error occurred"
                _errorMessage.value = errorMsg
                onError(errorMsg)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Sign out the current user
     */
    fun signOut() {
        viewModelScope.launch {
            try {
                _authToken.value?.let { token ->
                    repository.logout(token)
                }
            } catch (e: Exception) {
                // Ignore logout errors
            } finally {
                _isAuthenticated.value = false
                _currentUser.value = null
                _authToken.value = null
                _hasSurvey.value = false
                _hasAcceptedInfoConsent.value = false
                _errorMessage.value = null
                
                // Clear SharedPreferences
                prefs.edit().clear().apply()
            }
        }
    }
    
    /**
     * Mark that user has accepted the info consent
     */
    fun acceptInfoConsent() {
        _hasAcceptedInfoConsent.value = true
        prefs.edit().putBoolean("has_accepted_info_consent", true).apply()
    }

    /**
     * Verify current auth token
     */
    fun verifyToken() {
        viewModelScope.launch {
            try {
                _authToken.value?.let { token ->
                    val result = repository.verifyToken(token)
                    result.onSuccess { user ->
                        _currentUser.value = user
                        _isAuthenticated.value = true
                    }.onFailure {
                        // Token invalid, sign out
                        signOut()
                    }
                }
            } catch (e: Exception) {
                signOut()
            }
        }
    }

    /**
     * Clear any error messages
     */
    fun clearError() {
        _errorMessage.value = null
    }

    /**
     * Enter guest mode - creates a temporary guest user with full functionality
     */
    fun enterGuestMode() {
        _isGuestMode.value = true
        _isAuthenticated.value = true
        _currentUser.value = User(
            id = -1,
            firstName = "Guest",
            lastName = "",
            email = "guest@wakeupcall.app"
        )
        // Generate a guest token that allows limited backend functionality
        _authToken.value = "guest_token_${System.currentTimeMillis()}"
        _hasSurvey.value = false
        _hasAcceptedInfoConsent.value = true // Guest accepts by continuing
        
        // Don't save guest mode to SharedPreferences
    }

    /**
     * Exit guest mode and clear all data
     */
    fun exitGuestMode() {
        if (_isGuestMode.value) {
            _isGuestMode.value = false
            _isAuthenticated.value = false
            _currentUser.value = null
            _authToken.value = null
            _hasSurvey.value = false
            _hasAcceptedInfoConsent.value = false
            
            android.util.Log.d("AuthViewModel", "✅ Guest mode exited - all data cleared")
        }
    }
}
