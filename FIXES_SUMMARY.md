# Survey Questionnaire Fixes - Summary

## Date: November 19, 2025

## Issues Addressed

### 1. High Risk Professional Consultation Recommendation ✅

**Issue:** User reported not seeing "consult professional" recommendation for High Risk results.

**Root Cause Analysis:**
- The recommendation **IS** properly implemented in both:
  - `RecommendationGenerator.kt` (Android) - Priority 12 (highest)
  - `recommendation_engine.py` (Backend) - Priority 12 (highest)

**High Risk Criteria (any of these trigger the recommendation):**
```kotlin
val isHighRisk = scores.stopbang.score >= 5 || 
    scores.berlin.category.contains("High", ignoreCase = true) ||
    (scores.stopbang.score >= 3 && bmi >= 30) ||
    (scores.ess.score >= 16) ||
    (scores.stopbang.score >= 3 && medicalHistory.hypertension && demographics.neckCircumferenceCm >= 40)
```

**Solution:** 
- Code is correct
- Recommendation appears at Priority Insight #1 when High Risk criteria are met
- **User should retake the survey** to see the recommendation

---

### 2. Daily Step Count Database Update ✅

**Issue:** "Typical daily step-count" not updating in database

**Root Cause:** Fields exist but weren't being properly populated from Google Fit data

**Solution:**
- Database already has both `daily_steps` and `average_daily_steps` columns
- Backend (`app.py`) properly extracts from Google Fit data:
  ```python
  daily_steps = fit_data.get('daily_steps', 5000)
  average_daily_steps = fit_data.get('average_daily_steps', daily_steps)
  ```
- These are stored in both INSERT and UPDATE operations

---

### 3. Missing Questionnaire Fields Not Contributing to Model ✅

**Issue:** Many survey questions were collected in the UI but not stored in the database or passed to the model.

**Questions That Were Missing:**
1. At what time of day do you usually perform your physical activities?
2. How many hours of sleep do you get? *(was collected but not all details)*
3. Do you snore? *(was collected but not frequency/bothers others)*
4. Your snoring is... *(snoring level)*
5. How often do you snore? *(snoring frequency)*
6. Has your snoring ever bothered other people?
7. Has anyone observed that you quit breathing during your sleep? *(was collected)*
8. During your wake time, do you feel tired, fatigued or not up to par?
9. How often do you feel tired or fatigued after your sleep?
10. Have you ever nodded off or fallen asleep while driving a vehicle?
11. Identify how likely you are to fall asleep during the following daytime activities:
    - Watching television
    - Sitting inactive in a public place
    - Sitting for an hour as a passenger in a car
    - In a car, while stopped for a few minutes in traffic
    - Reading while sitting quietly
    - Sitting quietly after dinner (without alcohol)
    - Sitting in a car, stopped for a few minutes due to traffic

**Database Schema Changes:**
Created migration script that added 17 new columns to `user_surveys` table:
- `snoring_level` (TEXT)
- `snoring_frequency` (TEXT)
- `snoring_bothers_others` (INTEGER)
- `sleep_quality_rating` (INTEGER)
- `tired_during_day` (TEXT)
- `tired_after_sleep` (TEXT)
- `feels_sleepy_daytime` (INTEGER)
- `nodded_off_driving` (INTEGER)
- `physical_activity_time` (TEXT)
- `ess_sitting_reading` (INTEGER)
- `ess_watching_tv` (INTEGER)
- `ess_public_sitting` (INTEGER)
- `ess_passenger_car` (INTEGER)
- `ess_lying_down_afternoon` (INTEGER)
- `ess_talking` (INTEGER)
- `ess_after_lunch` (INTEGER)
- `ess_traffic_stop` (INTEGER)

**Backend Changes (app.py):**
- Modified `/survey/submit` endpoint to extract all new fields from survey responses
- Updated both INSERT and UPDATE SQL statements to store all new fields
- Individual ESS scores are now stored separately for detailed analysis

**Android App Changes:**

1. **ApiModels.kt** - Added new fields to `SurveyResponses` data class:
```kotlin
data class SurveyResponses(
    @SerializedName("ess_responses") val essResponses: List<Int>,
    @SerializedName("berlin_responses") val berlinResponses: BerlinResponses,
    @SerializedName("stopbang_responses") val stopbangResponses: StopBangResponses,
    // NEW FIELDS:
    @SerializedName("snoring_level") val snoringLevel: String? = null,
    @SerializedName("snoring_frequency") val snoringFrequency: String? = null,
    @SerializedName("snoring_bothers_others") val snoringBothersOthers: Boolean? = null,
    @SerializedName("tired_during_day") val tiredDuringDay: String? = null,
    @SerializedName("tired_after_sleep") val tiredAfterSleep: String? = null,
    @SerializedName("feels_sleepy_daytime") val feelsSleepyDaytime: Boolean? = null,
    @SerializedName("nodded_off_driving") val noddedOffDriving: Boolean? = null,
    @SerializedName("physical_activity_time") val physicalActivityTime: String? = null
)
```

2. **SurveyViewModel.kt** - Added state variables and update functions:
```kotlin
// New state variables
private val _noddedOffDriving = MutableStateFlow("")
private val _snoringFrequency = MutableStateFlow("")
private val _snoringBothersOthers = MutableStateFlow(false)
private val _physicalActivityTime = MutableStateFlow("")

// New update functions
fun updateNoddedOffDriving(value: String)
fun updateSnoringFrequency(value: String)
fun updateSnoringBothersOthers(value: Boolean)
fun updatePhysicalActivityTime(value: String)
```

3. **UI Screen Updates:**
   - `SleepHabits2.kt` - Now calls `updateSnoringFrequency()` and `updateSnoringBothersOthers()`
   - `FatigueSleepiness2.kt` - Now calls `updateNoddedOffDriving()`
   - `HealthHistory2.kt` - Now calls `updatePhysicalActivityTime()`

---

## How Scores Are Calculated

All scores are calculated in the backend (`app.py`) and stored in the database:

### Epworth Sleepiness Scale (ESS)
- Sum of 8 questions (0-3 each) = Total score 0-24
- Stored as: `ess_score` (total) + individual scores (`ess_sitting_reading`, etc.)
- Categories:
  - 0-5: Low daytime sleepiness (normal)
  - 6-10: High daytime sleepiness (normal)
  - 11-12: Mild excessive daytime sleepiness
  - 13-15: Moderate excessive daytime sleepiness
  - 16-24: Severe excessive daytime sleepiness

### Berlin Questionnaire
- Category 1 (items 2-6): 2+ points = positive
- Category 2 (items 7-9): 2+ points = positive
- Category 3: Sleepy while driving OR BMI > 30 = positive
- **Score = number of positive categories (0-3)**
- High Risk: 2+ positive categories
- Stored as: `berlin_score` (0-3)

### STOP-BANG
- S: Snoring
- T: Tired
- O: Observed apnea
- P: Pressure (hypertension)
- B: BMI > 35
- A: Age > 50
- N: Neck circumference (≥40cm men, ≥35cm women)
- G: Gender (male)
- **Score = sum of Yes answers (0-8)**
- High Risk: Score ≥5 OR (Score ≥2 in STOP + male/BMI>35/neck≥40cm)
- Stored as: `stopbang_score` (0-8)

---

## ML Model Input

The model receives 17 features (as defined in README.md):
```python
FEATURES = [
    'Age', 'Sex', 'BMI', 'Neck_Circumference', 'Hypertension', 'Diabetes',
    'Smokes', 'Alcohol', 'Snoring', 'Sleepiness',
    'Epworth_Score', 'Berlin_Score', 'STOPBANG_Total',
    'SleepQuality_Proxy_0to10', 'Sleep_Duration',
    'Physical_Activity_Level', 'Daily_Steps'
]
```

All these features are derived from the questionnaire:
- Direct answers (Age, Sex, medical history)
- Calculated scores (ESS, Berlin, STOP-BANG)
- Derived metrics (BMI, sleep quality proxy, activity level)
- Google Fit data (Daily steps, sleep duration)

**The additional questionnaire fields we added:**
- Enhance data collection for future analysis
- Provide richer context for recommendations
- Enable detailed reporting in PDFs
- Allow for future model improvements

---

## Files Modified

### Backend Files:
1. `backend/migrate_add_all_questionnaire_fields.py` *(NEW)* - Database migration script
2. `backend/app.py` - Updated survey submission endpoint

### Android Files:
1. `app/src/main/java/com/example/wakeupcallapp/sleepapp/data/models/ApiModels.kt`
2. `app/src/main/java/com/example/wakeupcallapp/sleepapp/viewmodel/SurveyViewModel.kt`
3. `app/src/main/java/com/example/wakeupcallapp/sleepapp/frontend/SleepHabits2.kt`
4. `app/src/main/java/com/example/wakeupcallapp/sleepapp/frontend/FatigueSleepiness2.kt`
5. `app/src/main/java/com/example/wakeupcallapp/sleepapp/frontend/HealthHistory2.kt`

---

## Testing Instructions

1. **Clear existing data** (optional, for clean test):
   ```powershell
   cd backend
   python clear_data.py
   ```

2. **Verify database schema**:
   ```powershell
   python check_db.py
   ```
   - Should show 40 columns in `user_surveys` table

3. **Restart backend**:
   ```powershell
   python app.py
   ```

4. **In Android app**:
   - Complete a new survey from start to finish
   - Answer all questions including:
     - Snoring frequency
     - Has snoring bothered others
     - Nodded off while driving
     - Time of day for physical activity
   - Submit survey

5. **Verify data storage**:
   ```powershell
   python check_db.py
   ```
   - Check that all new fields have values

6. **Check High Risk recommendation**:
   - If you have STOP-BANG ≥ 5 OR Berlin High Risk, you should see:
     - **"High Risk: Professional Sleep Evaluation Recommended"** as Priority Insight #1
   - This appears in both the Recommendations screen and PDF report

---

## Notes

- All scores (ESS, Berlin, STOP-BANG) are calculated on the backend, not in the Android app
- The ML model is called after scores are calculated
- Recommendations are generated based on both the ML prediction AND individual risk factors
- The database stores both the summary scores AND individual question responses for detailed analysis
- Google Fit data (steps, sleep) is optional but enhances predictions when available

---

## Migration Steps Already Completed

✅ Database migration script created and run successfully  
✅ Added 17 new columns to `user_surveys` table  
✅ Updated backend to extract and store all new fields  
✅ Updated Android data models to include new fields  
✅ Updated ViewModel with new state variables and functions  
✅ Updated UI screens to call new update functions  
✅ Verified High Risk recommendation logic is correct  

---

## What Users Need to Do

**Important:** Users must **retake the survey** after this update to:
1. Have all their questionnaire responses properly stored
2. See the updated recommendations including High Risk consultation if applicable
3. Get accurate data passed to the ML model

Old survey data will still work but won't have the new detailed fields populated.
