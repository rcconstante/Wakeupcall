package com.example.wakeupcallapp.sleepapp.data.repository

import com.example.wakeupcallapp.sleepapp.data.models.*
import com.example.wakeupcallapp.sleepapp.data.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository to handle all API communication with Flask backend
 */
class ApiRepository {
    
    private val apiService = RetrofitClient.apiService
    
    // ============ AUTHENTICATION ============
    
    suspend fun signUp(
        firstName: String,
        lastName: String,
        email: String,
        password: String
    ): Result<AuthResponse> = withContext(Dispatchers.IO) {
        try {
            val request = SignUpRequest(firstName, lastName, email, password)
            val response = apiService.signUp(request)
            
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.message() ?: "Sign up failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun login(
        email: String,
        password: String
    ): Result<AuthResponse> = withContext(Dispatchers.IO) {
        try {
            val request = LoginRequest(email, password)
            val response = apiService.login(request)
            
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.message() ?: "Login failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun logout(token: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.logout("Bearer $token")
            
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(Exception(response.message() ?: "Logout failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun verifyToken(token: String): Result<User> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.verifyToken("Bearer $token")
            
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception(response.message() ?: "Token verification failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ============ SURVEY SUBMISSION ============
    
    suspend fun submitSurvey(
        token: String,
        demographics: Demographics,
        medicalHistory: MedicalHistory,
        surveyResponses: SurveyResponses,
        googleFit: GoogleFitData? = null
    ): Result<SurveySubmissionResponse> = withContext(Dispatchers.IO) {
        try {
            android.util.Log.d("ApiRepository", "🚀 === SUBMITTING SURVEY TO BACKEND ===")
            android.util.Log.d("ApiRepository", "🚀 Demographics: age=${demographics.age}, sex=${demographics.sex}, height=${demographics.heightCm}, weight=${demographics.weightKg}")
            android.util.Log.d("ApiRepository", "🚀 Medical: hypertension=${medicalHistory.hypertension}, diabetes=${medicalHistory.diabetes}")
            android.util.Log.d("ApiRepository", "🚀 ESS responses: ${surveyResponses.essResponses}")
            
            val request = SurveySubmissionRequest(
                demographics = demographics,
                medicalHistory = medicalHistory,
                surveyResponses = surveyResponses,
                googleFit = googleFit
            )
            
            val response = apiService.submitSurvey("Bearer $token", request)
            
            android.util.Log.d("ApiRepository", "📡 Submit response code: ${response.code()}")
            android.util.Log.d("ApiRepository", "📡 Submit response message: ${response.message()}")
            
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                android.util.Log.d("ApiRepository", "✅ Survey submitted successfully! Survey ID: ${body.surveyId}")
                Result.success(body)
            } else {
                android.util.Log.e("ApiRepository", "❌ Survey submission failed: ${response.code()} ${response.message()}")
                Result.failure(Exception(response.message() ?: "Survey submission failed"))
            }
        } catch (e: Exception) {
            android.util.Log.e("ApiRepository", "❌ Exception during survey submission: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun getLatestSurvey(token: String): Result<SurveySubmissionResponse> = 
        withContext(Dispatchers.IO) {
        try {
            android.util.Log.d("ApiRepository", "🌐 Making GET /survey/get-latest request with token: ${token.take(20)}...")
            val response = apiService.getLatestSurvey("Bearer $token")
            
            android.util.Log.d("ApiRepository", "📡 Response code: ${response.code()}")
            android.util.Log.d("ApiRepository", "📡 Response message: ${response.message()}")
            android.util.Log.d("ApiRepository", "📡 Response isSuccessful: ${response.isSuccessful}")
            
            if (response.isSuccessful) {
                val body = response.body()
                android.util.Log.d("ApiRepository", "📦 Response body: success=${body?.success}, data=${body?.data}, message=${body?.message}")
                
                if (body?.data != null) {
                    android.util.Log.d("ApiRepository", "✅ Survey data found! Demographics: ${body.data.demographics}")
                    Result.success(body.data)
                } else {
                    android.util.Log.e("ApiRepository", "❌ Response body or data is NULL!")
                    Result.failure(Exception(body?.message ?: "No survey data found"))
                }
            } else {
                android.util.Log.e("ApiRepository", "❌ Response not successful: ${response.code()} ${response.message()}")
                Result.failure(Exception(response.message() ?: "Failed to retrieve survey"))
            }
        } catch (e: Exception) {
            android.util.Log.e("ApiRepository", "❌ Exception in getLatestSurvey: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    // ============ HEALTH CHECK ============
    
    suspend fun healthCheck(): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.healthCheck()
            Result.success(response.isSuccessful)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
