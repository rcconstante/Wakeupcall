package com.example.wakeupcallapp.sleepapp.data.network

import com.example.wakeupcallapp.sleepapp.data.models.*
import retrofit2.Response
import retrofit2.http.*

/**
 * Retrofit API interface for WakeUp Call Flask backend
 */
interface ApiService {
    
    // ============ AUTHENTICATION ENDPOINTS ============
    
    @POST("auth/signup")
    suspend fun signUp(@Body request: SignUpRequest): Response<AuthResponse>
    
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>
    
    @POST("auth/logout")
    suspend fun logout(@Header("Authorization") token: String): Response<ApiResponse<Nothing>>
    
    @GET("auth/verify")
    suspend fun verifyToken(@Header("Authorization") token: String): Response<ApiResponse<User>>
    
    // ============ SURVEY ENDPOINTS ============
    
    @POST("survey/submit")
    suspend fun submitSurvey(
        @Header("Authorization") token: String,
        @Body request: SurveySubmissionRequest
    ): Response<SurveySubmissionResponse>
    
    @GET("survey/get-latest")
    suspend fun getLatestSurvey(
        @Header("Authorization") token: String
    ): Response<ApiResponse<SurveySubmissionResponse>>
    
    // ============ HEALTH CHECK ============
    
    @GET("/")
    suspend fun healthCheck(): Response<Map<String, Any>>
}
