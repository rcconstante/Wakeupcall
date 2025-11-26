package com.example.wakeupcallapp.sleepapp.data.models

import com.google.gson.annotations.SerializedName

// ============ AUTHENTICATION MODELS ============

data class SignUpRequest(
    @SerializedName("first_name") val firstName: String,
    @SerializedName("last_name") val lastName: String,
    val email: String,
    val password: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class AuthResponse(
    val success: Boolean,
    val message: String? = null,
    val user: User? = null,
    @SerializedName("auth_token") val authToken: String? = null,
    @SerializedName("expires_at") val expiresAt: String? = null,
    @SerializedName("has_survey") val hasSurvey: Boolean? = null,
    val error: String? = null
)

data class User(
    val id: Int,
    @SerializedName("first_name") val firstName: String,
    @SerializedName("last_name") val lastName: String,
    val email: String
)

// ============ SURVEY MODELS ============

data class SurveySubmissionRequest(
    val demographics: Demographics,
    @SerializedName("medical_history") val medicalHistory: MedicalHistory,
    @SerializedName("survey_responses") val surveyResponses: SurveyResponses,
    @SerializedName("google_fit") val googleFit: GoogleFitData? = null
)

data class Demographics(
    val age: Int,
    val sex: String, // "male" or "female"
    @SerializedName("height_cm") val heightCm: Double,
    @SerializedName("weight_kg") val weightKg: Double,
    @SerializedName("neck_circumference_cm") val neckCircumferenceCm: Double
)

data class MedicalHistory(
    val hypertension: Boolean,
    val diabetes: Boolean,
    val depression: Boolean = false,
    val smokes: Boolean,
    val alcohol: Boolean
)

data class SurveyResponses(
    @SerializedName("ess_responses") val essResponses: List<Int>, // 8 values, 0-3 each
    @SerializedName("berlin_responses") val berlinResponses: BerlinResponses,
    @SerializedName("stopbang_responses") val stopbangResponses: StopBangResponses,
    // Additional questionnaire fields
    @SerializedName("snoring_level") val snoringLevel: String? = null,
    @SerializedName("snoring_frequency") val snoringFrequency: String? = null,
    @SerializedName("snoring_bothers_others") val snoringBothersOthers: Boolean? = null,
    @SerializedName("tired_during_day") val tiredDuringDay: String? = null,
    @SerializedName("tired_after_sleep") val tiredAfterSleep: String? = null,
    @SerializedName("feels_sleepy_daytime") val feelsSleepyDaytime: Boolean? = null,
    @SerializedName("nodded_off_driving") val noddedOffDriving: Boolean? = null,
    @SerializedName("physical_activity_time") val physicalActivityTime: String? = null
)

data class BerlinResponses(
    val category1: Map<String, Boolean>,
    val category2: Map<String, Boolean>,
    @SerializedName("category3_sleepy") val category3Sleepy: Boolean
)

data class StopBangResponses(
    val snoring: Boolean,
    val tired: Boolean,
    @SerializedName("observed_apnea") val observedApnea: Boolean,
    val hypertension: Boolean // This can match the medical history
)

// Using same name GoogleFitData for backend API compatibility
// Data now comes from Health Connect instead of Google Fit
data class GoogleFitData(
    @SerializedName("daily_steps") val dailySteps: Int,
    @SerializedName("average_daily_steps") val averageDailySteps: Int = dailySteps,
    @SerializedName("sleep_duration_hours") val sleepDurationHours: Double,
    @SerializedName("weekly_steps_data") val weeklyStepsData: Map<String, Int> = emptyMap(),
    @SerializedName("weekly_sleep_data") val weeklySleepData: Map<String, Double> = emptyMap(),
    @SerializedName("last_sync_time") val lastSyncTime: Long = System.currentTimeMillis()
)

// Helper function to convert HealthConnectData to GoogleFitData for API
fun com.example.wakeupcallapp.sleepapp.healthconnect.HealthConnectData.toGoogleFitData(): GoogleFitData {
    return GoogleFitData(
        dailySteps = this.dailySteps,
        averageDailySteps = this.averageDailySteps,
        sleepDurationHours = this.sleepDurationHours,
        weeklyStepsData = this.weeklyStepsData,
        weeklySleepData = this.weeklySleepData,
        lastSyncTime = this.lastSyncTime
    )
}

// ============ SURVEY RESPONSE MODELS ============

data class SurveySubmissionResponse(
    val success: Boolean,
    val message: String,
    @SerializedName("survey_id") val surveyId: Int? = null,
    val demographics: Demographics? = null,
    @SerializedName("medical_history") val medicalHistory: MedicalHistory? = null,
    val scores: SurveyScores? = null,
    val prediction: OsaPrediction? = null,
    @SerializedName("top_risk_factors") val topRiskFactors: List<RiskFactor>? = null,
    @SerializedName("calculated_metrics") val calculatedMetrics: CalculatedMetrics? = null,
    val error: String? = null
)

data class SurveyScores(
    val ess: ScoreDetail,
    val berlin: ScoreDetail,
    val stopbang: ScoreDetail
)

data class ScoreDetail(
    val score: Int,
    val category: String
)

data class OsaPrediction(
    @SerializedName("osa_probability") val osaProbability: Double,
    @SerializedName("risk_level") val riskLevel: String,
    val recommendation: String
)

data class RiskFactor(
    val factor: String,
    val detail: String,
    val impact: String,
    val priority: String
)

data class CalculatedMetrics(
    val bmi: Double,
    @SerializedName("estimated_activity_level") val estimatedActivityLevel: Int? = null,
    @SerializedName("estimated_sleep_quality") val estimatedSleepQuality: Int? = null
)

// ============ GENERIC RESPONSE ============

data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val message: String? = null,
    val error: String? = null
)
