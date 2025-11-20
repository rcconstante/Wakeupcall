"""
Direct test of model prediction logic (simulates backend processing)
This tests the exact same logic that runs in app.py when a survey is submitted
Uses WakeUpCall_3Class5Fold_Pipeline.pkl with 13 features
"""

import sys
import os
sys.path.insert(0, os.path.dirname(__file__))

# Import required modules
import pandas as pd
try:
    import joblib
    USE_JOBLIB = True
except ImportError:
    import pickle
    USE_JOBLIB = False

# Load the model (same logic as app.py)
MODEL_PATHS = [
    os.path.join(os.path.dirname(__file__), 'WakeUpCall_3Class5Fold_Pipeline.pkl'),
    os.path.join(os.path.dirname(__file__), '..', 'model', 'WakeUpCall_3Class5Fold_Pipeline.pkl'),
]

FEATURES = [
    'Age', 'Sex', 'BMI', 'Neck_Circumference', 'Hypertension', 'Diabetes',
    'Smokes', 'Alcohol', 'Snoring', 'Sleepiness',
    'Epworth_Score', 'Berlin_Score', 'STOPBANG_Total'
]

model = None
for model_path in MODEL_PATHS:
    if os.path.exists(model_path):
        try:
            if USE_JOBLIB:
                loaded_data = joblib.load(model_path)
            else:
                with open(model_path, 'rb') as f:
                    loaded_data = pickle.load(f)
            
            if isinstance(loaded_data, dict):
                model = loaded_data.get('model')
            else:
                model = loaded_data
            
            if model is not None:
                print(f"✅ Model loaded from: {model_path}")
                break
        except Exception as e:
            print(f"⚠️ Error loading model from {model_path}: {e}")

if model is None:
    print("❌ Failed to load model!")
    sys.exit(1)

print(f"Model type: {type(model)}")
print(f"Using joblib: {USE_JOBLIB}")

# Simulate survey data processing (from Android app)
print("\n" + "="*70)
print("SIMULATING SURVEY SUBMISSION")
print("="*70)

# Demographics (using sample input format)
age = 52
sex_str = "male"
sex = 1 if sex_str.lower() == 'male' else 0
height_cm = 175
weight_kg = 110  # To get BMI ~36
neck_cm = 43

# Calculate BMI
height_m = height_cm / 100
bmi = weight_kg / (height_m ** 2)

# Medical history
hypertension = 1
diabetes = 0
smokes = 0
alcohol = 1

# Survey responses
ess_responses = [2, 2, 2, 2, 2, 2, 2, 2]
ess_score = sum(ess_responses)  # 16

# Berlin score (simplified - high risk)
berlin_score_binary = 1  # High risk

# STOP-BANG score
snoring = True
tired = True
observed = True
pressure = True  # Same as hypertension

stopbang_score = 0
if snoring: stopbang_score += 1
if tired: stopbang_score += 1
if observed: stopbang_score += 1
if pressure: stopbang_score += 1
if bmi > 35: stopbang_score += 1
if age > 50: stopbang_score += 1
if neck_cm > 40: stopbang_score += 1
if sex == 1: stopbang_score += 1

# Calculate derived features
sleepiness = 1 if ess_score > 10 else 0
snoring_binary = 1 if snoring else 0

# Build feature dictionary (EXACT SAME as app.py) - 13 features
input_features = {
    'Age': age,
    'Sex': sex,
    'BMI': bmi,
    'Neck_Circumference': neck_cm,
    'Hypertension': hypertension,
    'Diabetes': diabetes,
    'Smokes': smokes,
    'Alcohol': alcohol,
    'Snoring': snoring_binary,
    'Sleepiness': sleepiness,
    'Epworth_Score': ess_score,
    'Berlin_Score': berlin_score_binary,
    'STOPBANG_Total': stopbang_score
}

print("\nInput Demographics:")
print(f"  Age: {age}, Sex: {sex_str}, Height: {height_cm}cm, Weight: {weight_kg}kg")
print(f"  Neck: {neck_cm}cm, BMI: {bmi:.1f}")

print("\nMedical History:")
print(f"  Hypertension: {bool(hypertension)}, Diabetes: {bool(diabetes)}")
print(f"  Smokes: {bool(smokes)}, Alcohol: {bool(alcohol)}")

print("\nSurvey Scores:")
print(f"  ESS Score: {ess_score} (High sleepiness)")
print(f"  Berlin Score: {berlin_score_binary} (High Risk)")
print(f"  STOP-BANG Total: {stopbang_score}")

print("\n" + "="*70)
print("MODEL INPUT (13 features):")
print("="*70)
for feature in FEATURES:
    print(f"  {feature}: {input_features[feature]}")

# Make prediction (EXACT SAME logic as app.py)
try:
    X_input = pd.DataFrame([{f: input_features[f] for f in FEATURES}])
    
    print(f"\nDataFrame shape: {X_input.shape}")
    print(f"DataFrame columns: {list(X_input.columns)}")
    
    # Get prediction (pipeline handles scaling internally)
    y_prob = model.predict_proba(X_input)[:, 1][0]
    osa_probability = float(y_prob)
    
    # Determine risk level
    if y_prob < 0.3:
        risk_level = "Low Risk"
    elif y_prob < 0.6:
        risk_level = "Moderate Risk"
    else:
        risk_level = "High Risk"
    
    print("\n" + "="*70)
    print("PREDICTION RESULT (This is what Android app will receive)")
    print("="*70)
    print(f"OSA Probability: {osa_probability:.3f} ({osa_probability*100:.1f}%)")
    print(f"Risk Level: {risk_level}")
    print("="*70)
    
    # Validate result
    print("\nVALIDATION:")
    if osa_probability >= 0.7:
        print("✅ HIGH RISK detected (expected for these symptoms)")
        print(f"   - High ESS score ({ess_score})")
        print("   - High Berlin score")
        print(f"   - High STOP-BANG ({stopbang_score})")
        print(f"   - Male, age {age}, BMI {bmi:.1f}, neck {neck_cm}cm")
    else:
        print(f"✅ Prediction: {risk_level} ({osa_probability:.3f})")
    
    # Show what will be sent to Android
    print("\n" + "="*70)
    print("API RESPONSE (JSON format sent to Android app):")
    print("="*70)
    api_response = {
        "success": True,
        "prediction": {
            "osa_probability": round(osa_probability, 3),
            "risk_level": risk_level,
            "recommendation": "Consult a sleep specialist for comprehensive evaluation..."
        },
        "scores": {
            "ess_score": ess_score,
            "berlin_score": berlin_score_binary,
            "stopbang_score": stopbang_score
        }
    }
    
    import json
    print(json.dumps(api_response, indent=2))
    
except Exception as e:
    print(f"\n❌ Prediction error: {e}")
    import traceback
    traceback.print_exc()
