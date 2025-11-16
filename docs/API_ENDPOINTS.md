# Flask API Endpoints Reference

## Base URL Configuration

```kotlin
// For Android Emulator (localhost)
http://10.0.2.2:5000/

// For Physical Device (your computer's IP)
http://192.168.1.XXX:5000/
```

---

## Authentication Endpoints

### 1. Sign Up

**Endpoint:** `POST /auth/signup`

**Request Body:**
```json
{
    "first_name": "John",
    "last_name": "Doe",
    "email": "john@example.com",
    "password": "password123"
}
```

**Success Response (201):**
```json
{
    "success": true,
    "message": "Account created successfully",
    "user": {
        "id": 1,
        "first_name": "John",
        "last_name": "Doe",
        "email": "john@example.com"
    },
    "auth_token": "eyJhbGciOiJIUzI1...",
    "expires_at": "2025-12-16T10:30:00"
}
```

**Error Response (400/409):**
```json
{
    "success": false,
    "error": "Email already registered"
}
```

**Kotlin Usage:**
```kotlin
authViewModel.signUp(
    email = "john@example.com",
    password = "password123",
    firstName = "John",
    lastName = "Doe",
    onSuccess = { /* Navigate to next screen */ },
    onError = { error -> /* Show error message */ }
)
```

---

### 2. Login

**Endpoint:** `POST /auth/login`

**Request Body:**
```json
{
    "email": "john@example.com",
    "password": "password123"
}
```

**Success Response (200):**
```json
{
    "success": true,
    "message": "Login successful",
    "user": {
        "id": 1,
        "first_name": "John",
        "last_name": "Doe",
        "email": "john@example.com"
    },
    "auth_token": "eyJhbGciOiJIUzI1...",
    "expires_at": "2025-12-16T10:30:00"
}
```

**Error Response (401):**
```json
{
    "success": false,
    "error": "Invalid email or password"
}
```

**Kotlin Usage:**
```kotlin
authViewModel.signIn(
    email = "john@example.com",
    password = "password123",
    onSuccess = { /* Navigate to dashboard */ },
    onError = { error -> /* Show error */ }
)
```

---

### 3. Logout

**Endpoint:** `POST /auth/logout`

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1...
```

**Success Response (200):**
```json
{
    "success": true,
    "message": "Logged out successfully"
}
```

**Kotlin Usage:**
```kotlin
authViewModel.signOut()
```

---

### 4. Verify Token

**Endpoint:** `GET /auth/verify`

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1...
```

**Success Response (200):**
```json
{
    "success": true,
    "user": {
        "id": 1,
        "email": "john@example.com",
        "first_name": "John",
        "last_name": "Doe"
    }
}
```

**Error Response (401):**
```json
{
    "success": false,
    "error": "Invalid or expired token"
}
```

---

## Survey Endpoints

### 5. Submit Survey

**Endpoint:** `POST /survey/submit`

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1...
Content-Type: application/json
```

**Request Body:**
```json
{
    "demographics": {
        "age": 45,
        "sex": "male",
        "height_cm": 175.0,
        "weight_kg": 85.0,
        "neck_circumference_cm": 42.0
    },
    "medical_history": {
        "hypertension": true,
        "diabetes": false,
        "smokes": false,
        "alcohol": true
    },
    "survey_responses": {
        "ess_responses": [2, 1, 2, 1, 2, 1, 2, 3],
        "berlin_responses": {
            "category1": {
                "item2": true,
                "item3": false,
                "item4": true,
                "item5": true,
                "item6": false
            },
            "category2": {
                "item7": true,
                "item8": false,
                "item9": false
            },
            "category3_sleepy": true
        },
        "stopbang_responses": {
            "snoring": true,
            "tired": true,
            "observed_apnea": false,
            "hypertension": true
        }
    },
    "google_fit": {
        "daily_steps": 8000,
        "sleep_duration_hours": 6.5
    }
}
```

**Success Response (201):**
```json
{
    "success": true,
    "message": "Survey submitted successfully",
    "survey_id": 1,
    "scores": {
        "ess": {
            "score": 14,
            "category": "Moderate excessive daytime sleepiness"
        },
        "berlin": {
            "score": 2,
            "category": "High Risk"
        },
        "stopbang": {
            "score": 6,
            "category": "High Risk"
        }
    },
    "prediction": {
        "osa_probability": 0.78,
        "risk_level": "High Risk",
        "recommendation": "🚨 HIGH RISK: Immediate sleep specialist consultation recommended | Consider overnight sleep study (polysomnography) | 🎯 Weight management: BMI 27.8 - aim for 5-10% weight loss | 📐 Large neck circumference increases OSA risk - focus on overall weight reduction"
    },
    "top_risk_factors": [
        {
            "factor": "Large Neck",
            "detail": "42cm",
            "impact": "20%",
            "priority": "High"
        },
        {
            "factor": "Severe Sleepiness",
            "detail": "ESS 14",
            "impact": "18%",
            "priority": "High"
        },
        {
            "factor": "Overweight",
            "detail": "BMI 27.8",
            "impact": "15%",
            "priority": "Medium"
        }
    ],
    "calculated_metrics": {
        "bmi": 27.8,
        "estimated_activity_level": 3,
        "estimated_sleep_quality": 5
    }
}
```

**Error Response (400/401):**
```json
{
    "success": false,
    "error": "Invalid or expired token"
}
```

**Kotlin Usage:**
```kotlin
surveyViewModel.submitSurvey(authToken)

// Observe results
val result by surveyViewModel.submissionResult.collectAsState()
result?.let { response ->
    println("Risk Level: ${response.prediction?.riskLevel}")
    println("OSA Probability: ${response.prediction?.osaProbability}")
}
```

---

### 6. Get Latest Survey

**Endpoint:** `GET /survey/get-latest`

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1...
```

**Success Response (200):**
```json
{
    "success": true,
    "message": "Survey data retrieved successfully",
    "data": {
        "survey_id": 1,
        "age": 45,
        "sex": "male",
        "height_cm": 175.0,
        "weight_kg": 85.0,
        "neck_circumference_cm": 42.0,
        "bmi": 27.8,
        "hypertension": true,
        "diabetes": false,
        "smokes": false,
        "alcohol": true,
        "ess_score": 14,
        "berlin_score": 2,
        "stopbang_score": 6,
        "osa_probability": 0.78,
        "risk_level": "High Risk",
        "completed_at": "2025-11-16T10:30:00"
    }
}
```

**Error Response (404):**
```json
{
    "success": false,
    "message": "No survey data found"
}
```

---

## Health Check

### 7. Health Check

**Endpoint:** `GET /`

**Success Response (200):**
```json
{
    "status": "running",
    "message": "WakeUp Call OSA Prediction API",
    "model_loaded": true,
    "timestamp": "2025-11-16T10:30:00"
}
```

**Kotlin Usage:**
```kotlin
// Test if backend is reachable
lifecycleScope.launch {
    try {
        val response = RetrofitClient.apiService.healthCheck()
        if (response.isSuccessful) {
            println("✅ Backend is running")
        }
    } catch (e: Exception) {
        println("❌ Cannot connect to backend")
    }
}
```

---

## Error Codes Summary

| Status Code | Meaning | Common Causes |
|-------------|---------|---------------|
| 200 | OK | Successful GET request |
| 201 | Created | Successful POST (signup, survey) |
| 400 | Bad Request | Missing/invalid fields |
| 401 | Unauthorized | Invalid/expired token |
| 404 | Not Found | Resource doesn't exist |
| 409 | Conflict | Email already registered |
| 500 | Server Error | Backend/ML model error |

---

## Testing with PowerShell

### Test Health Check
```powershell
Invoke-RestMethod -Uri "http://localhost:5000/" -Method Get
```

### Test Signup
```powershell
$body = @{
    first_name = "John"
    last_name = "Doe"
    email = "john@test.com"
    password = "password123"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:5000/auth/signup" -Method Post -Body $body -ContentType "application/json"
```

### Test Login
```powershell
$body = @{
    email = "john@test.com"
    password = "password123"
} | ConvertTo-Json

$response = Invoke-RestMethod -Uri "http://localhost:5000/auth/login" -Method Post -Body $body -ContentType "application/json"
$token = $response.auth_token
```

### Test Survey Submission
```powershell
$headers = @{
    "Authorization" = "Bearer $token"
}

$body = @{
    demographics = @{
        age = 45
        sex = "male"
        height_cm = 175
        weight_kg = 85
        neck_circumference_cm = 42
    }
    medical_history = @{
        hypertension = $true
        diabetes = $false
        smokes = $false
        alcohol = $true
    }
    survey_responses = @{
        ess_responses = @(2, 1, 2, 1, 2, 1, 2, 3)
        berlin_responses = @{
            category1 = @{
                item2 = $true
                item3 = $false
            }
            category2 = @{}
            category3_sleepy = $true
        }
        stopbang_responses = @{
            snoring = $true
            tired = $true
            observed_apnea = $false
            hypertension = $true
        }
    }
    google_fit = @{
        daily_steps = 8000
        sleep_duration_hours = 6.5
    }
} | ConvertTo-Json -Depth 10

Invoke-RestMethod -Uri "http://localhost:5000/survey/submit" -Method Post -Headers $headers -Body $body -ContentType "application/json"
```

---

## Token Management

### Token Storage (Recommended)

Store auth token securely in Android:

```kotlin
// Using DataStore (recommended)
class TokenManager(private val context: Context) {
    private val Context.dataStore by preferencesDataStore("auth")
    
    suspend fun saveToken(token: String) {
        context.dataStore.edit { prefs ->
            prefs[stringPreferencesKey("auth_token")] = token
        }
    }
    
    fun getToken(): Flow<String?> {
        return context.dataStore.data.map { prefs ->
            prefs[stringPreferencesKey("auth_token")]
        }
    }
    
    suspend fun clearToken() {
        context.dataStore.edit { it.clear() }
    }
}
```

### Token Expiration

Tokens expire after **30 days**. Handle expired tokens:

```kotlin
suspend fun makeAuthenticatedRequest() {
    try {
        val response = apiService.getLatestSurvey("Bearer $token")
        if (response.code() == 401) {
            // Token expired, re-login
            authViewModel.signOut()
            navigateToLogin()
        }
    } catch (e: Exception) {
        // Handle error
    }
}
```

---

## Network Configuration

### Add Internet Permission

`AndroidManifest.xml`:
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

### Allow Cleartext Traffic (Development Only)

`AndroidManifest.xml`:
```xml
<application
    android:usesCleartextTraffic="true"
    ...>
```

Or create `network_security_config.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <domain-config cleartextTrafficPermitted="true">
        <domain includeSubdomains="true">10.0.2.2</domain>
        <domain includeSubdomains="true">192.168.1.100</domain>
    </domain-config>
</network-security-config>
```

---

## Troubleshooting

### Connection Refused
- ✅ Flask backend is running
- ✅ Correct BASE_URL in RetrofitClient
- ✅ Same network (for physical device)
- ✅ Firewall allows port 5000

### 401 Unauthorized
- ✅ Token included in Authorization header
- ✅ Token not expired
- ✅ Token format: `Bearer <token>`

### 400 Bad Request
- ✅ All required fields present
- ✅ Correct data types
- ✅ Valid email format
- ✅ Password ≥ 6 characters

### Timeout
- ✅ Backend responding (test with browser)
- ✅ Network connectivity
- ✅ Increase OkHttp timeout if needed

---

**Need more help?** Check Flask backend logs and Android Logcat for detailed error messages!
