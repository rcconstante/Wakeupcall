# Backend Integration Guide - WakeUp Call App

## Overview

This document explains the Flask backend integration with the Android frontend, including authentication, survey data collection, and ML prediction.

## ✅ What Has Been Implemented

### 1. **Backend (Flask)**
- ✅ User authentication (signup/login/logout)
- ✅ SQLite database for users and surveys
- ✅ Survey submission endpoint with ML prediction
- ✅ ESS, Berlin, STOP-BANG score calculation
- ✅ ML model integration (LightGBM)
- ✅ Risk assessment and personalized recommendations

### 2. **Android Frontend - Network Layer**
- ✅ Retrofit API client (`RetrofitClient.kt`)
- ✅ API service interface (`ApiService.kt`)
- ✅ API repository (`ApiRepository.kt`)
- ✅ Data models for requests/responses (`ApiModels.kt`)
- ✅ Survey calculator utility (`SurveyCalculator.kt`)

### 3. **Android Frontend - ViewModels**
- ✅ `AuthViewModel` - handles login/signup with real API calls
- ✅ `SurveyViewModel` - collects survey data across multiple screens

### 4. **Android Frontend - UI Updates**
- ✅ `LogIn.kt` - connected to AuthViewModel with API integration
- ✅ `SignUp.kt` - connected to AuthViewModel with API integration  
- ✅ `Demographics.kt` - saves data to SurveyViewModel

## 🔧 Setup Instructions

### Step 1: Configure Backend URL

**File:** `app/src/main/java/.../data/network/RetrofitClient.kt`

```kotlin
// For Android Emulator
private const val BASE_URL = "http://10.0.2.2:5000/"

// For Physical Device - Update with your computer's IP
private const val BASE_URL = "http://192.168.1.100:5000/"
```

**To find your IP:**
```powershell
ipconfig
```
Look for **IPv4 Address** under your active WiFi/Ethernet adapter.

### Step 2: Start Flask Backend

```powershell
cd "d:\Cursor Project\WakeUpCallApp\backend"
python app.py
```

Expected output:
```
✅ Database initialized successfully!
✅ Model loaded successfully!
🚀 Starting WakeUp Call OSA Prediction API...
* Running on http://0.0.0.0:5000
```

### Step 3: Build Android App

The app will now connect to the Flask backend automatically.

## 📊 Survey Flow

### Current Implementation

1. **Splash Screen** → **Info Consent** → **Auth Screen**
2. User chooses **Login** or **SignUp**
3. After authentication → **Data Consent** → **Demographics**
4. **Health History 1** → **Health History 2**
5. **Sleep Habits 1** → **Sleep Habits 2**
6. **Fatigue/Sleepiness 1-5** (ESS questions)
7. Final submission → **Dashboard**

### Data Collection

Each screen saves data to `SurveyViewModel`:
- **Demographics**: age, sex, height, weight, neck circumference
- **Health History**: hypertension, diabetes, smoking, alcohol
- **Sleep Habits**: sleep hours, snoring, observed apnea
- **Fatigue/Sleepiness**: ESS 8-question responses (0-3 each)

## 🔄 What Needs to Be Done

### Remaining Survey Screens to Update

You need to update these screens to save data to `SurveyViewModel`:

#### 1. **HealthHistory1.kt & HealthHistory2.kt**
Add ViewModel parameter and save medical history:

```kotlin
@Composable
fun HealthHistory1ScreenContent(
    onNext: () -> Unit = {},
    onBack: () -> Unit = {},
    surveyViewModel: SurveyViewModel = viewModel()
) {
    // ... existing state variables ...
    
    // On next button:
    surveyViewModel.updateMedicalHistory(
        hypertension = hypertension == "Yes",
        diabetes = diabetes == "Yes",
        smokes = smoking == "Yes",
        alcohol = alcohol == "Yes"
    )
    onNext()
}
```

#### 2. **SleepHabits1.kt & SleepHabits2.kt**
Save sleep habits data:

```kotlin
@Composable
fun SleepHabits1ScreenContent(
    onNext: () -> Unit = {},
    onBack: () -> Unit = {},
    surveyViewModel: SurveyViewModel = viewModel()
) {
    // ... existing state ...
    
    // On next:
    surveyViewModel.updateSleepHabits(
        hours = sleepHours.toDoubleOrNull() ?: 7.0,
        snores = doesSnore == "Yes",
        snoringLevel = snoringLevel,
        observedApnea = false // From SleepHabits2
    )
    
    // Berlin Category 1 (snoring questions)
    surveyViewModel.updateBerlinCategory1("item2", doesSnore == "Yes")
    surveyViewModel.updateBerlinCategory1("item3", snoringLevel == "Very loud")
    // ... more items
}
```

#### 3. **FatigueSleepiness1-5.kt**
These screens collect ESS (Epworth Sleepiness Scale) responses:

```kotlin
@Composable
fun FatigueSleepiness2ScreenContent(
    onNext: () -> Unit = {},
    onBack: () -> Unit = {},
    surveyViewModel: SurveyViewModel = viewModel()
) {
    // ESS Question 1 (index 0)
    var response1 by remember { mutableStateOf(0) }
    
    // Map text responses to 0-3 scale
    val responseValue = when(response1) {
        "Never" -> 0
        "Slight" -> 1
        "Moderate" -> 2
        "High" -> 3
        else -> 0
    }
    
    // Save to ViewModel
    surveyViewModel.updateESSResponse(0, responseValue)
}
```

**ESS Scale (0-3):**
- 0 = Would never doze
- 1 = Slight chance of dozing
- 2 = Moderate chance of dozing
- 3 = High chance of dozing

#### 4. **Create Final Submission Screen**

After FatigueSleepiness5, create a **results preview screen** before dashboard:

```kotlin
@Composable
fun SurveySubmissionScreen(
    onComplete: () -> Unit = {},
    surveyViewModel: SurveyViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {
    val authToken by authViewModel.authToken.collectAsState()
    val isSubmitting by surveyViewModel.isSubmitting.collectAsState()
    val result by surveyViewModel.submissionResult.collectAsState()
    val error by surveyViewModel.errorMessage.collectAsState()
    
    // Calculate local scores for preview
    val (essScore, berlinScore, stopbangScore) = surveyViewModel.calculateLocalScores()
    
    Column {
        Text("Survey Preview")
        Text("ESS Score: ${essScore.first} - ${essScore.second}")
        Text("Berlin: ${berlinScore.second}")
        Text("STOP-BANG: ${stopbangScore.first} - ${stopbangScore.second}")
        
        Button(
            onClick = {
                authToken?.let { token ->
                    surveyViewModel.submitSurvey(token)
                }
            },
            enabled = !isSubmitting
        ) {
            if (isSubmitting) {
                CircularProgressIndicator()
            } else {
                Text("Submit Survey")
            }
        }
        
        // Show results after submission
        result?.let { response ->
            Text("Risk Level: ${response.prediction?.riskLevel}")
            Text("OSA Probability: ${response.prediction?.osaProbability}")
            Text("Recommendation: ${response.prediction?.recommendation}")
            
            Button(onClick = onComplete) {
                Text("Continue to Dashboard")
            }
        }
    }
}
```

## 📋 Survey Scoring Reference

### Epworth Sleepiness Scale (ESS)
- **8 questions**, each scored 0-3
- **Total range**: 0-24
- **Categories**:
  - 0-5: Low daytime sleepiness (normal)
  - 6-10: High daytime sleepiness (normal)
  - 11-12: Mild excessive daytime sleepiness
  - 13-15: Moderate excessive daytime sleepiness
  - 16-24: Severe excessive daytime sleepiness

### Berlin Questionnaire
- **Category 1** (Snoring): Items 2-6, positive if ≥2 points
- **Category 2** (Daytime sleepiness): Items 7-9, positive if ≥2 points
- **Category 3**: Positive if BMI > 30 or hypertension
- **Risk**: High if ≥2 categories positive

### STOP-BANG
- **8 yes/no questions**
- **Risk levels**:
  - Low: 0-2 yes
  - Intermediate: 3-4 yes
  - High: 5-8 yes

## 🔐 Authentication Flow

### Sign Up
```
User enters credentials → AuthViewModel.signUp()
→ ApiRepository.signUp() → Flask /auth/signup
→ Returns auth_token → Stored in ViewModel
→ Navigate to Data Consent
```

### Login
```
User enters credentials → AuthViewModel.signIn()
→ ApiRepository.login() → Flask /auth/login
→ Returns auth_token → Stored in ViewModel
→ Navigate to Data Consent
```

### Logout
```
User clicks logout → AuthViewModel.signOut()
→ ApiRepository.logout() → Flask /auth/logout
→ Clear token and user data
→ Navigate to Auth Screen
```

## 🚀 Testing the Integration

### 1. Test Backend Health
```powershell
Invoke-RestMethod -Uri "http://localhost:5000/" -Method Get
```

### 2. Test Signup
```powershell
$body = @{
    first_name = "John"
    last_name = "Doe"
    email = "john@example.com"
    password = "password123"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:5000/auth/signup" -Method Post -Body $body -ContentType "application/json"
```

### 3. Test Login
```powershell
$body = @{
    email = "john@example.com"
    password = "password123"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:5000/auth/login" -Method Post -Body $body -ContentType "application/json"
```

## 🐛 Troubleshooting

### Cannot connect to Flask
1. Check Flask is running: `http://localhost:5000/`
2. Verify BASE_URL in RetrofitClient.kt
3. For physical device, use computer's IP address
4. Ensure both devices on same WiFi network
5. Check Windows Firewall allows port 5000

### Authentication fails
1. Check backend logs for errors
2. Verify database file exists: `wakeup_call.db`
3. Check email format and password length (min 6 chars)

### Survey submission fails
1. Ensure user is authenticated (has auth_token)
2. Check all required fields are filled
3. Verify Flask model files exist: `lgbm_model.pkl`, `scaler.pkl`
4. Check backend logs for ML prediction errors

## 📱 Next Steps

1. **Update remaining survey screens** (HealthHistory, SleepHabits, FatigueSleepiness)
2. **Create submission/results screen** before dashboard
3. **Add loading states** to all API calls
4. **Add error handling** UI for failed requests
5. **Test complete flow** from signup to ML prediction
6. **Display results** on Dashboard screen

## 📚 File Structure

```
app/src/main/java/com/example/wakeupcallapp/sleepapp/
├── data/
│   ├── models/
│   │   └── ApiModels.kt          # All API request/response models
│   ├── network/
│   │   ├── ApiService.kt         # Retrofit API interface
│   │   └── RetrofitClient.kt     # Retrofit configuration
│   └── repository/
│       └── ApiRepository.kt      # API call handling
├── viewmodel/
│   ├── AuthViewModel.kt          # Authentication state
│   └── SurveyViewModel.kt        # Survey data collection
├── utils/
│   └── SurveyCalculator.kt       # ESS, Berlin, STOP-BANG calculation
└── frontend/
    ├── LogIn.kt                  # ✅ Updated
    ├── SignUp.kt                 # ✅ Updated
    ├── Demographics.kt           # ✅ Updated
    ├── HealthHistory1.kt         # 🔧 Needs update
    ├── HealthHistory2.kt         # 🔧 Needs update
    ├── SleepHabits1.kt           # 🔧 Needs update
    ├── SleepHabits2.kt           # 🔧 Needs update
    ├── FatigueSleepiness1-5.kt   # 🔧 Needs update
    └── (Create) SubmissionScreen.kt  # 🆕 Create new
```

## 🎯 Success Criteria

✅ User can sign up and receive auth token  
✅ User can login with credentials  
✅ Demographics data saves to ViewModel  
🔧 All survey screens save data to ViewModel  
🔧 Survey submits to Flask backend  
🔧 ML prediction returns OSA risk assessment  
🔧 Results display on dashboard

---

**Need Help?** Check the Flask backend logs and Android Logcat for detailed error messages.
