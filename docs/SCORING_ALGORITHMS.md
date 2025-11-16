# Survey Scoring Algorithms Reference

## Overview

This document provides detailed information about the three survey scoring systems used in the WakeUp Call app for OSA risk assessment.

---

## 1. Epworth Sleepiness Scale (ESS)

### Purpose
Measures daytime sleepiness by assessing the likelihood of dozing off in 8 different situations.

### Questions (8 total)

Each question asks: "How likely are you to doze off or fall asleep in the following situations?"

1. Sitting and reading
2. Watching TV
3. Sitting inactive in a public place (e.g., theater, meeting)
4. As a passenger in a car for an hour without a break
5. Lying down to rest in the afternoon
6. Sitting and talking to someone
7. Sitting quietly after lunch (without alcohol)
8. In a car while stopped in traffic

### Response Scale

- **0** = Would never doze
- **1** = Slight chance of dozing
- **2** = Moderate chance of dozing
- **3** = High chance of dozing

### Scoring

**Total Score Range:** 0-24 (sum of all 8 responses)

**Interpretation:**
- **0-5**: Low daytime sleepiness (normal)
- **6-10**: High daytime sleepiness (normal)
- **11-12**: Mild excessive daytime sleepiness
- **13-15**: Moderate excessive daytime sleepiness
- **16-24**: Severe excessive daytime sleepiness

### Implementation Notes

```kotlin
// ESS responses stored as List<Int> with 8 values (0-3 each)
val essResponses = listOf(2, 1, 0, 2, 3, 1, 0, 2)  // Example
val essScore = essResponses.sum()  // = 11
```

### Mapping UI to ESS Scores

Your Fatigue/Sleepiness screens use text responses. Map them to 0-3:

```kotlin
fun mapFrequencyToESSScore(frequency: String): Int {
    return when (frequency) {
        "Never or nearly never" -> 0
        "1-2 times a month" -> 1
        "1-2 times a week" -> 2
        "3-4 times a week" -> 2
        "Nearly every day" -> 3
        else -> 0
    }
}
```

---

## 2. Berlin Questionnaire

### Purpose
Identifies individuals at high risk for OSA based on snoring, daytime sleepiness, and medical history.

### Structure

The Berlin Questionnaire has **3 categories**. A person is at **high risk** if they score positive in **2 or more categories**.

### Category 1: Snoring and Breathing Pauses

**5 questions about snoring:**

| Item | Question | Scoring |
|------|----------|---------|
| 2 | Do you snore? | Yes = 1 point |
| 3 | Your snoring is... | "Louder than talking" or "Very loud" = 1 point |
| 4 | How often do you snore? | "Nearly every night" or "Every night" = 1 point |
| 5 | Has your snoring bothered others? | Yes = 1 point |
| 6 | Has anyone noticed you stop breathing? | Yes = 2 points |

**Category 1 is POSITIVE if total ≥ 2 points**

### Category 2: Daytime Sleepiness

**3 questions about tiredness:**

| Item | Question | Scoring |
|------|----------|---------|
| 7 | Do you feel tired after sleep? | "Nearly every day" or "3-4 times/week" = 1 point |
| 8 | Do you feel tired during day? | "Nearly every day" or "3-4 times/week" = 1 point |
| 9 | Have you dozed off while driving? | Yes = 1 point |

**Category 2 is POSITIVE if total ≥ 2 points**

### Category 3: Hypertension or BMI > 30

**Category 3 is POSITIVE if:**
- BMI > 30 kg/m² **OR**
- History of high blood pressure (hypertension)

### Final Risk Assessment

- **High Risk**: 2 or more positive categories
- **Low Risk**: 0-1 positive categories

### Implementation Example

```kotlin
// Category 1
val cat1Points = mapOf(
    "item2" to true,   // Snores
    "item3" to false,  // Not very loud
    "item4" to true,   // Snores every night
    "item5" to true,   // Bothers others
    "item6" to false   // No observed apnea
)
val cat1Score = cat1Points.values.count { it }  // = 3 points
val cat1Positive = cat1Score >= 2  // true

// Category 2
val cat2Points = mapOf(
    "item7" to true,   // Tired after sleep
    "item8" to false,  // Not tired during day
    "item9" to false   // Never dozed while driving
)
val cat2Score = cat2Points.values.count { it }  // = 1 point
val cat2Positive = cat2Score >= 2  // false

// Category 3
val bmi = 31.5
val hypertension = true
val cat3Positive = (bmi > 30) || hypertension  // true

// Final assessment
val positiveCategories = listOf(cat1Positive, cat2Positive, cat3Positive).count { it }
val riskLevel = if (positiveCategories >= 2) "High Risk" else "Low Risk"
```

### Mapping Your Screens to Berlin

**SleepHabits1.kt** → Berlin Category 1:
- "Do you snore?" → item2
- "Your snoring is..." → item3
- (Need frequency question) → item4
- (Need bothering others question) → item5
- (Observed apnea from SleepHabits2) → item6

**FatigueSleepiness1.kt** → Berlin Category 2:
- "tired after sleep" → item7
- "tired during day" → item8
- (Need driving question) → item9

**HealthHistory1.kt** → Berlin Category 3:
- "Hypertension" + BMI from Demographics

---

## 3. STOP-BANG Questionnaire

### Purpose
Simple 8-question screening tool specifically designed for OSA detection.

### Questions (8 Yes/No)

| Letter | Question | Points if YES |
|--------|----------|---------------|
| **S** | Do you **Snore** loudly? | 1 |
| **T** | Do you often feel **Tired** during the day? | 1 |
| **O** | Has anyone **Observed** you stop breathing? | 1 |
| **P** | Do you have high blood **Pressure**? | 1 |
| **B** | Is your **BMI** > 35? | 1 |
| **A** | Are you over 50 years of **Age**? | 1 |
| **N** | Is your **Neck** circumference ≥ 40 cm (16 inches)? | 1 |
| **G** | Are you Male (**Gender**)? | 1 |

### Scoring

**Total Score Range:** 0-8

### Risk Stratification

#### For General Population:

- **Low Risk**: 0-2 yes answers
- **Intermediate Risk**: 3-4 yes answers
- **High Risk**: 5-8 yes answers

#### Special High-Risk Conditions:

Even with a low score, patient is **High Risk** if:
- **2+ STOP questions YES** (S, T, O, or P) **AND**
  - Male **OR**
  - BMI > 35 **OR**
  - Neck circumference ≥ 40 cm

### Implementation Example

```kotlin
// Individual questions
val snoring = true
val tired = true
val observedApnea = false
val hypertension = true
val bmi = 32.0
val age = 52
val neckCircumference = 41.0
val isMale = true

// Calculate score
var score = 0
var stopScore = 0

if (snoring) { score++; stopScore++ }
if (tired) { score++; stopScore++ }
if (observedApnea) { score++; stopScore++ }
if (hypertension) { score++; stopScore++ }
if (bmi > 35) score++
if (age > 50) score++
if (neckCircumference >= 40.0) score++
if (isMale) score++

// Determine risk
val riskCategory = when {
    score >= 5 -> "High Risk"
    score in 3..4 -> "Intermediate Risk"
    score in 0..2 -> {
        // Check special conditions
        if (stopScore >= 2 && (isMale || bmi > 35 || neckCircumference >= 40.0)) {
            "High Risk"
        } else {
            "Low Risk"
        }
    }
    else -> "Low Risk"
}
```

### Mapping Your Screens to STOP-BANG

| Question | Source Screen | Variable |
|----------|---------------|----------|
| S - Snoring | SleepHabits1 | `doesSnore == "Yes"` |
| T - Tired | FatigueSleepiness1 | `feelsSleepy` or frequency |
| O - Observed | SleepHabits2 | Needs question added |
| P - Pressure | HealthHistory1 | `hypertension == "Yes"` |
| B - BMI > 35 | Demographics | Calculate from height/weight |
| A - Age > 50 | Demographics | `age > 50` |
| N - Neck ≥ 40cm | Demographics | `neckCircumference >= 40` |
| G - Gender Male | Demographics | `sex == "Male"` |

---

## Integration with Machine Learning

### Input Features Required for ML Model

The Flask backend expects these 17 features:

```python
{
    'Age': 45,
    'Sex': 1,  # 1=Male, 0=Female
    'BMI': 31.5,
    'Neck_Circumference': 42,
    'Hypertension': 1,  # 1=Yes, 0=No
    'Diabetes': 0,
    'Smokes': 0,
    'Alcohol': 1,
    'Snoring': 1,
    'Sleepiness': 1,
    'Epworth_Score': 14,  # ESS total (0-24)
    'Berlin_Score': 1,  # 1=High Risk, 0=Low Risk
    'STOPBANG_Total': 5,  # Total score (0-8)
    'SleepQuality_Proxy_0to10': 4,
    'Sleep_Duration': 6.5,  # hours
    'Physical_Activity_Level': 2,  # 1-10 scale
    'Daily_Steps': 8000
}
```

### Deriving ML Features from Surveys

```kotlin
// From calculated scores
val essScore = calculateESSScore(essResponses).first
val berlinRisk = calculateBerlinScore(...).second
val stopbangScore = calculateStopBangScore(...).first

// Convert for ML
val mlFeatures = mapOf(
    "Epworth_Score" to essScore,
    "Berlin_Score" to if (berlinRisk == "High Risk") 1 else 0,
    "STOPBANG_Total" to stopbangScore,
    "Sleepiness" to if (essScore > 10) 1 else 0,
    "Snoring" to if (snores) 1 else 0,
    // ... other features from demographics and medical history
)
```

---

## Complete Survey Data Collection Checklist

### Demographics Screen ✅
- [x] Age
- [x] Sex (Male/Female)
- [x] Height (cm)
- [x] Weight (kg)
- [x] Neck Circumference (cm)

### Health History Screens
- [x] Hypertension
- [x] Diabetes
- [x] Smoking
- [x] Alcohol consumption

### Sleep Habits Screens
- [x] Sleep duration (hours)
- [x] Snoring (Yes/No)
- [x] Snoring loudness
- [ ] Snoring frequency (for Berlin item4)
- [ ] Bothering others with snoring (for Berlin item5)
- [ ] Observed breathing pauses (for Berlin item6 & STOP-BANG O)

### Fatigue/Sleepiness Screens
- [ ] ESS 8 questions (need to map frequency responses to 0-3 scale)
- [x] Tired during day (Berlin item8)
- [x] Tired after sleep (Berlin item7)
- [ ] Dozed while driving (for Berlin item9)

### Google Fit Integration (Optional)
- [ ] Daily steps
- [ ] Sleep duration from fitness tracker

---

## Testing Your Implementation

### Test Case 1: Low Risk Patient

```kotlin
// Input
age = 30, sex = "female", bmi = 22, neck = 35
hypertension = false, diabetes = false
essScore = 4, berlinPositive = 0, stopbangScore = 1

// Expected
Risk Level: "Low Risk"
OSA Probability: < 0.3
```

### Test Case 2: High Risk Patient

```kotlin
// Input
age = 55, sex = "male", bmi = 35, neck = 43
hypertension = true, diabetes = true
essScore = 18, berlinPositive = 3, stopbangScore = 7

// Expected
Risk Level: "High Risk"
OSA Probability: > 0.7
Recommendation: "Immediate sleep specialist consultation"
```

---

## Summary

All three scoring systems work together to provide comprehensive OSA risk assessment:

1. **ESS** measures **sleepiness severity** (0-24 scale)
2. **Berlin** identifies **high-risk individuals** (High/Low)
3. **STOP-BANG** provides **quick screening** (0-8 scale)

These scores, combined with demographics and medical history, feed into the **LightGBM ML model** for final OSA probability prediction (0-1).

**Remember:** Always validate user input and handle edge cases (empty fields, invalid numbers, etc.) before calculating scores!
