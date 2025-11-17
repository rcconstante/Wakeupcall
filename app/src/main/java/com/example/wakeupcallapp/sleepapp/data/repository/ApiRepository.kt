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
    
    // ============ PDF REPORT GENERATION ============
    
    suspend fun downloadReport(token: String): Result<java.io.File> = withContext(Dispatchers.IO) {
        try {
            if (token.isBlank()) {
                return@withContext Result.failure(Exception("Not authenticated"))
            }
            
            android.util.Log.d("ApiRepository", "📥 Requesting PDF report from backend...")
            val response = apiService.downloadReport("Bearer $token")
            
            android.util.Log.d("ApiRepository", "📡 Response code: ${response.code()}")
            android.util.Log.d("ApiRepository", "📡 Response successful: ${response.isSuccessful}")
            android.util.Log.d("ApiRepository", "📡 Response body null: ${response.body() == null}")
            
            if (response.isSuccessful && response.body() != null) {
                val responseBody = response.body()!!
                
                // Determine file extension from Content-Type header
                val contentType = response.headers()["Content-Type"] ?: "application/pdf"
                val contentLength = response.headers()["Content-Length"]?.toLongOrNull() ?: 0L
                val fileExtension = when {
                    contentType.contains("pdf") -> ".pdf"
                    contentType.contains("wordprocessingml") -> ".docx"
                    else -> ".pdf"
                }
                
                android.util.Log.d("ApiRepository", "📄 Content-Type: $contentType, Extension: $fileExtension")
                android.util.Log.d("ApiRepository", "📏 Content-Length: $contentLength bytes")
                
                // Save to temporary file with buffered writing
                val tempFile = java.io.File.createTempFile("WakeUpCall_Report_", fileExtension)
                android.util.Log.d("ApiRepository", "💾 Saving to: ${tempFile.absolutePath}")
                
                try {
                    var bytesWritten = 0L
                    val buffer = ByteArray(8192) // 8KB buffer
                    
                    tempFile.outputStream().buffered().use { output ->
                        responseBody.byteStream().buffered().use { input ->
                            var bytesRead: Int
                            while (input.read(buffer).also { bytesRead = it } != -1) {
                                output.write(buffer, 0, bytesRead)
                                bytesWritten += bytesRead
                                
                                // Log progress every 50KB
                                if (bytesWritten % 50000 == 0L) {
                                    val progress = if (contentLength > 0) {
                                        (bytesWritten * 100 / contentLength)
                                    } else {
                                        0
                                    }
                                    android.util.Log.d("ApiRepository", "⬇️ Downloaded: $bytesWritten bytes ($progress%)")
                                }
                            }
                            output.flush()
                        }
                    }
                    
                    android.util.Log.d("ApiRepository", "✅ Report saved: ${tempFile.absolutePath}")
                    android.util.Log.d("ApiRepository", "📊 Downloaded: $bytesWritten bytes, On disk: ${tempFile.length()} bytes")
                    android.util.Log.d("ApiRepository", "📂 File exists: ${tempFile.exists()}")
                    
                    if (tempFile.length() == 0L) {
                        android.util.Log.e("ApiRepository", "❌ File is empty!")
                        Result.failure(Exception("Downloaded file is empty"))
                    } else if (contentLength > 0 && tempFile.length() != contentLength) {
                        android.util.Log.e("ApiRepository", "⚠️ Size mismatch! Expected: $contentLength, Got: ${tempFile.length()}")
                        Result.failure(Exception("Incomplete download: expected $contentLength bytes, got ${tempFile.length()} bytes"))
                    } else {
                        Result.success(tempFile)
                    }
                } catch (e: Exception) {
                    android.util.Log.e("ApiRepository", "❌ Error writing file: ${e.message}", e)
                    if (tempFile.exists()) {
                        tempFile.delete()
                    }
                    throw e
                }
            } else {
                val errorBody = response.errorBody()?.string()
                android.util.Log.e("ApiRepository", "❌ Failed to download report: ${response.code()}")
                android.util.Log.e("ApiRepository", "❌ Error message: ${response.message()}")
                android.util.Log.e("ApiRepository", "❌ Error body: $errorBody")
                Result.failure(Exception("Failed to download report: ${response.message()}"))
            }
        } catch (e: Exception) {
            android.util.Log.e("ApiRepository", "❌ Exception downloading report: ${e.message}", e)
            Result.failure(e)
        }
    }
}
