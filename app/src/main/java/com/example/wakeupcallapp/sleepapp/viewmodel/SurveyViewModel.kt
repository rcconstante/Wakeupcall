package com.example.wakeupcallapp.sleepapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.wakeupcallapp.sleepapp.data.models.*
import com.example.wakeupcallapp.sleepapp.data.repository.ApiRepository
import com.example.wakeupcallapp.sleepapp.utils.SurveyCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel to manage survey data collection across multiple screens
 * Stores all survey responses and handles submission to backend
 */
class SurveyViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository = ApiRepository()
    
    // ============ DEMOGRAPHICS ============
    private val _age = MutableStateFlow(0)
    val age: StateFlow<Int> = _age.asStateFlow()
    
    private val _sex = MutableStateFlow("")
    val sex: StateFlow<String> = _sex.asStateFlow()
    
    private val _heightCm = MutableStateFlow(0.0)
    val heightCm: StateFlow<Double> = _heightCm.asStateFlow()
    
    private val _weightKg = MutableStateFlow(0.0)
    val weightKg: StateFlow<Double> = _weightKg.asStateFlow()
    
    private val _neckCircumferenceCm = MutableStateFlow(0.0)
    val neckCircumferenceCm: StateFlow<Double> = _neckCircumferenceCm.asStateFlow()
    
    // ============ MEDICAL HISTORY ============
    private val _hypertension = MutableStateFlow(false)
    val hypertension: StateFlow<Boolean> = _hypertension.asStateFlow()
    
    private val _diabetes = MutableStateFlow(false)
    val diabetes: StateFlow<Boolean> = _diabetes.asStateFlow()
    
    private val _depression = MutableStateFlow(false)
    val depression: StateFlow<Boolean> = _depression.asStateFlow()
    
    private val _smokes = MutableStateFlow(false)
    val smokes: StateFlow<Boolean> = _smokes.asStateFlow()
    
    private val _alcohol = MutableStateFlow(false)
    val alcohol: StateFlow<Boolean> = _alcohol.asStateFlow()
    
    // ============ ESS (EPWORTH SLEEPINESS SCALE) - 8 questions ============
    // Each response is 0-3: 0=Never, 1=Slight, 2=Moderate, 3=High
    private val _essResponses = MutableStateFlow(MutableList(8) { 0 })
    val essResponses: StateFlow<List<Int>> = _essResponses.asStateFlow()
    
    // ============ BERLIN QUESTIONNAIRE ============
    private val _berlinCategory1 = MutableStateFlow(mutableMapOf<String, Boolean>())
    val berlinCategory1: StateFlow<Map<String, Boolean>> = _berlinCategory1.asStateFlow()
    
    private val _berlinCategory2 = MutableStateFlow(mutableMapOf<String, Boolean>())
    val berlinCategory2: StateFlow<Map<String, Boolean>> = _berlinCategory2.asStateFlow()
    
    private val _berlinCategory3Sleepy = MutableStateFlow(false)
    val berlinCategory3Sleepy: StateFlow<Boolean> = _berlinCategory3Sleepy.asStateFlow()
    
    // ============ SLEEP HABITS ============
    private val _sleepHours = MutableStateFlow(0.0)
    val sleepHours: StateFlow<Double> = _sleepHours.asStateFlow()
    
    private val _snores = MutableStateFlow(false)
    val snores: StateFlow<Boolean> = _snores.asStateFlow()
    
    private val _snoringLevel = MutableStateFlow("")
    val snoringLevel: StateFlow<String> = _snoringLevel.asStateFlow()
    
    private val _observedApnea = MutableStateFlow(false)
    val observedApnea: StateFlow<Boolean> = _observedApnea.asStateFlow()
    
    // ============ FATIGUE/SLEEPINESS ============
    private val _tiredDuringDay = MutableStateFlow("")
    val tiredDuringDay: StateFlow<String> = _tiredDuringDay.asStateFlow()
    
    private val _tiredAfterSleep = MutableStateFlow("")
    val tiredAfterSleep: StateFlow<String> = _tiredAfterSleep.asStateFlow()
    
    private val _feelsSleepy = MutableStateFlow(false)
    val feelsSleepy: StateFlow<Boolean> = _feelsSleepy.asStateFlow()
    
    private val _noddedOffDriving = MutableStateFlow("")
    val noddedOffDriving: StateFlow<String> = _noddedOffDriving.asStateFlow()
    
    // ============ SNORING DETAILS ============
    private val _snoringFrequency = MutableStateFlow("")
    val snoringFrequency: StateFlow<String> = _snoringFrequency.asStateFlow()
    
    private val _snoringBothersOthers = MutableStateFlow(false)
    val snoringBothersOthers: StateFlow<Boolean> = _snoringBothersOthers.asStateFlow()
    
    // ============ PHYSICAL ACTIVITY ============
    private val _physicalActivityTime = MutableStateFlow("")
    val physicalActivityTime: StateFlow<String> = _physicalActivityTime.asStateFlow()
    
    // ============ GOOGLE FIT DATA ============
    private val _dailySteps = MutableStateFlow(0)
    val dailySteps: StateFlow<Int> = _dailySteps.asStateFlow()
    
    // ============ SUBMISSION STATE ============
    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()
    
    private val _isLoadingSurvey = MutableStateFlow(false)
    val isLoadingSurvey: StateFlow<Boolean> = _isLoadingSurvey.asStateFlow()
    
    private val _hasLoadedData = MutableStateFlow(false)
    val hasLoadedData: StateFlow<Boolean> = _hasLoadedData.asStateFlow()
    
    private val _submissionResult = MutableStateFlow<SurveySubmissionResponse?>(null)
    val submissionResult: StateFlow<SurveySubmissionResponse?> = _submissionResult.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    // ============ UPDATE FUNCTIONS ============

    fun resetSurveyStateForNewSession() {
        android.util.Log.d("SurveyViewModel", "♻️ Resetting survey state for new session")
        _hasLoadedData.value = false
        _isLoadingSurvey.value = false
        _submissionResult.value = null
        _errorMessage.value = null
    }

    fun prepareForNewSurvey() {
        android.util.Log.d("SurveyViewModel", "♻️ Preparing for new survey (retake)")
        _isSubmitting.value = false
        _submissionResult.value = null
        _errorMessage.value = null
    }
    
    /**
     * Pre-fill survey data from Health Connect
     * Updates relevant fields if Health Connect data is available
     */
    fun prefillFromHealthConnect(healthData: com.example.wakeupcallapp.sleepapp.healthconnect.HealthConnectData?) {
        if (healthData == null) {
            android.util.Log.d("SurveyViewModel", "⚠️ No Health Connect data to prefill")
            return
        }
        
        android.util.Log.d("SurveyViewModel", "🏥 Pre-filling survey from Health Connect data...")
        
        // Pre-fill weight if available
        if (healthData.weightKg > 0) {
            _weightKg.value = healthData.weightKg
            android.util.Log.d("SurveyViewModel", "✅ Pre-filled weight: ${healthData.weightKg} kg")
        }
        
        // Pre-fill height if available
        if (healthData.heightCm > 0) {
            _heightCm.value = healthData.heightCm
            android.util.Log.d("SurveyViewModel", "✅ Pre-filled height: ${healthData.heightCm} cm")
        }
        
        // Pre-fill sleep hours if available
        if (healthData.sleepDurationHours > 0) {
            _sleepHours.value = healthData.sleepDurationHours
            android.util.Log.d("SurveyViewModel", "✅ Pre-filled sleep hours: ${healthData.sleepDurationHours}h")
        }
        
        // Pre-fill daily steps if available
        if (healthData.averageDailySteps > 0) {
            _dailySteps.value = healthData.averageDailySteps
            android.util.Log.d("SurveyViewModel", "✅ Pre-filled daily steps: ${healthData.averageDailySteps}")
            
            // Also update physical activity time based on steps
            _physicalActivityTime.value = when {
                healthData.averageDailySteps >= 10000 -> "60+ minutes"
                healthData.averageDailySteps >= 7500 -> "30-60 minutes"
                healthData.averageDailySteps >= 5000 -> "15-30 minutes"
                else -> "Less than 15 minutes"
            }
            android.util.Log.d("SurveyViewModel", "✅ Pre-filled activity time: ${_physicalActivityTime.value}")
        }
        
        android.util.Log.d("SurveyViewModel", "✅ Health Connect pre-fill completed")
    }
    
    fun updateDemographics(age: Int, sex: String, heightCm: Double, weightKg: Double, neckCm: Double) {
        android.util.Log.d("SurveyViewModel", "🔵 updateDemographics called with: age=$age, sex=$sex, height=$heightCm, weight=$weightKg, neck=$neckCm")
        android.util.Log.d("SurveyViewModel", "🔵 Before update: _age=${_age.value}, _height=${_heightCm.value}, _weight=${_weightKg.value}")
        
        _age.value = age
        _sex.value = sex
        _heightCm.value = heightCm
        _weightKg.value = weightKg
        _neckCircumferenceCm.value = neckCm
        
        android.util.Log.d("SurveyViewModel", "🔵 After update: _age=${_age.value}, _height=${_heightCm.value}, _weight=${_weightKg.value}")
    }
    
    fun updateMedicalHistory(hypertension: Boolean, diabetes: Boolean, depression: Boolean, smokes: Boolean, alcohol: Boolean) {
        _hypertension.value = hypertension
        _diabetes.value = diabetes
        _depression.value = depression
        _smokes.value = smokes
        _alcohol.value = alcohol
    }
    
    fun updateESSResponse(index: Int, value: Int) {
        require(index in 0..7) { "ESS index must be 0-7" }
        require(value in 0..3) { "ESS value must be 0-3" }
        val current = _essResponses.value.toMutableList()
        current[index] = value
        _essResponses.value = current
    }
    
    fun updateBerlinCategory1(item: String, value: Boolean) {
        val current = _berlinCategory1.value.toMutableMap()
        current[item] = value
        _berlinCategory1.value = current
    }
    
    fun updateBerlinCategory2(item: String, value: Boolean) {
        val current = _berlinCategory2.value.toMutableMap()
        current[item] = value
        _berlinCategory2.value = current
    }
    
    fun updateBerlinCategory3Sleepy(value: Boolean) {
        _berlinCategory3Sleepy.value = value
    }
    
    fun updateSleepHabits(hours: Double, snores: Boolean, snoringLevel: String, observedApnea: Boolean) {
        _sleepHours.value = hours
        _snores.value = snores
        _snoringLevel.value = snoringLevel
        _observedApnea.value = observedApnea
    }
    
    fun updateFatigueSleepiness(tiredDuringDay: String, tiredAfterSleep: String, feelsSleepy: Boolean) {
        _tiredDuringDay.value = tiredDuringDay
        _tiredAfterSleep.value = tiredAfterSleep
        _feelsSleepy.value = feelsSleepy
    }
    
    fun updateNoddedOffDriving(value: String) {
        _noddedOffDriving.value = value
    }
    
    fun updateSnoringFrequency(value: String) {
        _snoringFrequency.value = value
    }
    
    fun updateSnoringBothersOthers(value: Boolean) {
        _snoringBothersOthers.value = value
    }
    
    fun updatePhysicalActivityTime(value: String) {
        _physicalActivityTime.value = value
    }
    
    fun updateDailySteps(steps: Int) {
        _dailySteps.value = steps
    }
    
    // ============ RESET SURVEY DATA ============
    
    fun resetSurveyData() {
        // Reset demographics to defaults
        _age.value = 0
        _sex.value = ""
        _heightCm.value = 0.0
        _weightKg.value = 0.0
        _neckCircumferenceCm.value = 0.0
        
        // Reset medical history
        _hypertension.value = false
        _diabetes.value = false
        _depression.value = false
        _smokes.value = false
        _alcohol.value = false
        
        // Reset ESS responses
        _essResponses.value = MutableList(8) { 0 }
        
        // Reset Berlin responses
        _berlinCategory1.value = mutableMapOf()
        _berlinCategory2.value = mutableMapOf()
        _berlinCategory3Sleepy.value = false
        
        // Reset sleep habits
        _sleepHours.value = 0.0
        _snores.value = false
        _snoringLevel.value = ""
        _observedApnea.value = false
        
        // Reset fatigue/sleepiness
        _tiredDuringDay.value = ""
        _tiredAfterSleep.value = ""
        _feelsSleepy.value = false
        _noddedOffDriving.value = ""
        
        // Reset snoring details
        _snoringFrequency.value = ""
        _snoringBothersOthers.value = false
        
        // Reset physical activity
        _physicalActivityTime.value = ""
        
        // Reset Google Fit data
        _dailySteps.value = 0
        
        // Clear submission result
        _submissionResult.value = null
        _errorMessage.value = null
    }
    
    // ============ SUBMIT SURVEY ============
    
    fun submitSurvey(authToken: String, healthConnectViewModel: com.example.wakeupcallapp.sleepapp.viewmodel.HealthConnectViewModel? = null) {
        viewModelScope.launch {
            _isSubmitting.value = true
            _errorMessage.value = null
            
            try {
                // Log current ViewModel state before submission
                android.util.Log.d("SurveyViewModel", "=== SUBMITTING SURVEY DATA ===")
                android.util.Log.d("SurveyViewModel", "Demographics: age=${_age.value}, sex=${_sex.value}, height=${_heightCm.value}, weight=${_weightKg.value}, neck=${_neckCircumferenceCm.value}")
                android.util.Log.d("SurveyViewModel", "Medical: hypertension=${_hypertension.value}, diabetes=${_diabetes.value}, depression=${_depression.value}, smokes=${_smokes.value}, alcohol=${_alcohol.value}")
                android.util.Log.d("SurveyViewModel", "ESS: ${_essResponses.value}")
                android.util.Log.d("SurveyViewModel", "Sleep: hours=${_sleepHours.value}, snores=${_snores.value}, observedApnea=${_observedApnea.value}")
                android.util.Log.d("SurveyViewModel", "Daily Steps: ${_dailySteps.value}")
                
                // Build demographics
                val demographics = Demographics(
                    age = _age.value,
                    sex = _sex.value,
                    heightCm = _heightCm.value,
                    weightKg = _weightKg.value,
                    neckCircumferenceCm = _neckCircumferenceCm.value
                )
                
                // Build medical history
                val medicalHistory = MedicalHistory(
                    hypertension = _hypertension.value,
                    diabetes = _diabetes.value,
                    depression = _depression.value,
                    smokes = _smokes.value,
                    alcohol = _alcohol.value
                )
                
                // Build Berlin responses
                val berlinResponses = BerlinResponses(
                    category1 = _berlinCategory1.value,
                    category2 = _berlinCategory2.value,
                    category3Sleepy = _berlinCategory3Sleepy.value
                )
                
                // Build STOP-BANG responses
                val stopbangResponses = StopBangResponses(
                    snoring = _snores.value,
                    tired = _feelsSleepy.value,
                    observedApnea = _observedApnea.value,
                    hypertension = _hypertension.value
                )
                
                // Build survey responses with all additional fields
                val surveyResponses = SurveyResponses(
                    essResponses = _essResponses.value,
                    berlinResponses = berlinResponses,
                    stopbangResponses = stopbangResponses,
                    snoringLevel = _snoringLevel.value.takeIf { it.isNotEmpty() },
                    snoringFrequency = _snoringFrequency.value.takeIf { it.isNotEmpty() },
                    snoringBothersOthers = _snoringBothersOthers.value,
                    tiredDuringDay = _tiredDuringDay.value.takeIf { it.isNotEmpty() },
                    tiredAfterSleep = _tiredAfterSleep.value.takeIf { it.isNotEmpty() },
                    feelsSleepyDaytime = _feelsSleepy.value,
                    noddedOffDriving = _noddedOffDriving.value.isNotEmpty() && _noddedOffDriving.value != "Never or nearly never",
                    physicalActivityTime = _physicalActivityTime.value.takeIf { it.isNotEmpty() }
                )
                
                // Build health data - include data from HealthConnectViewModel if available
                // Convert HealthConnectData to GoogleFitData for API compatibility
                val googleFit = if (healthConnectViewModel != null) {
                    val healthData = healthConnectViewModel.healthData.value
                    android.util.Log.d("SurveyViewModel", "📊 Health Connect data available: dailySteps=${healthData?.dailySteps}, sleepHours=${healthData?.sleepDurationHours}")
                    
                    GoogleFitData(
                        dailySteps = _dailySteps.value,  // User input has priority
                        averageDailySteps = healthData?.averageDailySteps ?: _dailySteps.value,
                        sleepDurationHours = _sleepHours.value,
                        weeklyStepsData = healthData?.weeklyStepsData ?: emptyMap(),
                        weeklySleepData = healthData?.weeklySleepData ?: emptyMap(),
                        lastSyncTime = healthData?.lastSyncTime ?: System.currentTimeMillis()
                    )
                } else {
                    android.util.Log.d("SurveyViewModel", "⚠️ Health Connect ViewModel not provided, using minimal data")
                    GoogleFitData(
                        dailySteps = _dailySteps.value,
                        sleepDurationHours = _sleepHours.value
                    )
                }
                
                android.util.Log.d("SurveyViewModel", "📤 Submitting survey with complete health data...")
                
                // Submit to backend
                val result = repository.submitSurvey(
                    token = authToken,
                    demographics = demographics,
                    medicalHistory = medicalHistory,
                    surveyResponses = surveyResponses,
                    googleFit = googleFit
                )
                
                result.onSuccess { response ->
                    _submissionResult.value = response
                    
                    // Update SharedPreferences to mark survey as completed
                    val prefs = getApplication<Application>().getSharedPreferences("auth_prefs", Application.MODE_PRIVATE)
                    prefs.edit().putBoolean("has_survey", true).apply()
                }.onFailure { error ->
                    _errorMessage.value = error.message ?: "Submission failed"
                }
                
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Unknown error occurred"
            } finally {
                _isSubmitting.value = false
            }
        }
    }
    
    // ============ LOCAL SCORE CALCULATION ============
    
    fun calculateLocalScores(): Triple<Pair<Int, String>, Pair<Int, String>, Pair<Int, String>> {
        val bmi = SurveyCalculator.calculateBMI(_heightCm.value, _weightKg.value)
        
        val essScore = SurveyCalculator.calculateESSScore(_essResponses.value)
        
        val berlinScore = SurveyCalculator.calculateBerlinScore(
            _berlinCategory1.value,
            _berlinCategory2.value,
            _berlinCategory3Sleepy.value,
            bmi
        )
        
        val stopbangScore = SurveyCalculator.calculateStopBangScore(
            snoring = _snores.value,
            tired = _feelsSleepy.value,
            observedApnea = _observedApnea.value,
            hypertension = _hypertension.value,
            bmi = bmi,
            age = _age.value,
            neckCircumference = _neckCircumferenceCm.value,
            isMale = _sex.value.lowercase() == "male"
        )
        
        return Triple(essScore, berlinScore, stopbangScore)
    }
    
    fun clearError() {
        _errorMessage.value = null
    }
    
    fun resetSurvey() {
        _age.value = 0
        _sex.value = ""
        _heightCm.value = 0.0
        _weightKg.value = 0.0
        _neckCircumferenceCm.value = 0.0
        _hypertension.value = false
        _diabetes.value = false
        _depression.value = false
        _smokes.value = false
        _alcohol.value = false
        _essResponses.value = MutableList(8) { 0 }
        _berlinCategory1.value = mutableMapOf()
        _berlinCategory2.value = mutableMapOf()
        _berlinCategory3Sleepy.value = false
        _sleepHours.value = 0.0
        _snores.value = false
        _snoringLevel.value = ""
        _observedApnea.value = false
        _tiredDuringDay.value = ""
        _tiredAfterSleep.value = ""
        _feelsSleepy.value = false
        _dailySteps.value = 0
        _submissionResult.value = null
        _errorMessage.value = null
    }
    
    /**
     * Load existing survey data from backend and populate all fields
     */
    fun loadExistingSurvey(authToken: String) {
        android.util.Log.d("SurveyViewModel", "=".repeat(60))
        android.util.Log.d("SurveyViewModel", "🔵 loadExistingSurvey CALLED with token: ${authToken.take(20)}...")
        android.util.Log.d("SurveyViewModel", "🔵 Current values BEFORE load: age=${_age.value}, height=${_heightCm.value}, weight=${_weightKg.value}")
        
        viewModelScope.launch {
            _isLoadingSurvey.value = true
            _hasLoadedData.value = false  // Reset so UI shows loading
            _errorMessage.value = null
            
            try {
                android.util.Log.d("SurveyViewModel", "📡 Making API call to GET /survey/get-latest...")
                val result = repository.getLatestSurvey(authToken)
                
                result.onSuccess { response ->
                    android.util.Log.d("SurveyViewModel", "✅ API SUCCESS! Received survey data")
                    android.util.Log.d("SurveyViewModel", "📊 Response: success=${response.success}, surveyId=${response.surveyId}")
                    
                    // Update submission result with prediction data for Dashboard display
                    _submissionResult.value = response
                    android.util.Log.d("SurveyViewModel", "✅ Updated _submissionResult")
                    
                    // Mark that we've successfully loaded data
                    _hasLoadedData.value = true
                    android.util.Log.d("SurveyViewModel", "✅ Set hasLoadedData = true")
                    
                    // Populate demographic fields from the response
                    if (response.demographics != null) {
                        val demo = response.demographics
                        android.util.Log.d("SurveyViewModel", "🟢 Demographics found: age=${demo.age}, sex=${demo.sex}, height=${demo.heightCm}, weight=${demo.weightKg}, neck=${demo.neckCircumferenceCm}")
                        _age.value = demo.age
                        _sex.value = demo.sex
                        _heightCm.value = demo.heightCm
                        _weightKg.value = demo.weightKg
                        _neckCircumferenceCm.value = demo.neckCircumferenceCm
                        android.util.Log.d("SurveyViewModel", "✅ Updated all demographic fields")
                    } else {
                        android.util.Log.e("SurveyViewModel", "❌ Demographics is NULL in response!")
                    }
                    
                    // Populate medical history
                    if (response.medicalHistory != null) {
                        val medical = response.medicalHistory
                        android.util.Log.d("SurveyViewModel", "🟢 Medical history found: hypertension=${medical.hypertension}, diabetes=${medical.diabetes}, depression=${medical.depression}, smokes=${medical.smokes}, alcohol=${medical.alcohol}")
                        _hypertension.value = medical.hypertension
                        _diabetes.value = medical.diabetes
                        _depression.value = medical.depression
                        _smokes.value = medical.smokes
                        _alcohol.value = medical.alcohol
                        android.util.Log.d("SurveyViewModel", "✅ Updated all medical history fields")
                    } else {
                        android.util.Log.e("SurveyViewModel", "❌ Medical history is NULL in response!")
                    }
                    
                    android.util.Log.d("SurveyViewModel", "🔵 Final values AFTER load: age=${_age.value}, height=${_heightCm.value}, weight=${_weightKg.value}")
                    android.util.Log.d("SurveyViewModel", "=".repeat(60))
                }.onFailure { error ->
                    val errorMsg = error.message ?: "Failed to load survey data"
                    android.util.Log.e("SurveyViewModel", "❌ API FAILURE: $errorMsg")
                    android.util.Log.e("SurveyViewModel", "❌ Error details:", error)
                    _errorMessage.value = errorMsg
                }
            } catch (e: Exception) {
                val errorMsg = e.message ?: "Unknown error occurred"
                android.util.Log.e("SurveyViewModel", "❌ EXCEPTION in loadExistingSurvey: $errorMsg")
                android.util.Log.e("SurveyViewModel", "❌ Exception details:", e)
                _errorMessage.value = errorMsg
            } finally {
                _isLoadingSurvey.value = false
                android.util.Log.d("SurveyViewModel", "🔵 loadExistingSurvey FINISHED")
            }
        }
    }
    
    /**
     * Download PDF report from Flask backend
     */
    fun downloadReport(authToken: String, onComplete: (java.io.File?) -> Unit) {
        viewModelScope.launch {
            try {
                android.util.Log.d("SurveyViewModel", "📥 Downloading report from backend...")
                
                repository.downloadReport(authToken).onSuccess { file ->
                    android.util.Log.d("SurveyViewModel", "✅ Report downloaded successfully: ${file.absolutePath}")
                    onComplete(file)
                }.onFailure { error ->
                    android.util.Log.e("SurveyViewModel", "❌ Failed to download report: ${error.message}")
                    onComplete(null)
                }
            } catch (e: Exception) {
                android.util.Log.e("SurveyViewModel", "❌ Exception downloading report: ${e.message}")
                onComplete(null)
            }
        }
    }
}
