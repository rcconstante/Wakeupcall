"""
End-to-end test for the survey submission API endpoint
Tests that the backend correctly processes survey data and returns predictions using final_lgbm_pipeline.pkl
"""

import requests
import json

# Test data matching the Android app format
test_survey_data = {
    "demographics": {
        "age": 45,
        "sex": "male",
        "height_cm": 175,
        "weight_kg": 85,
        "neck_circumference_cm": 42
    },
    "medical_history": {
        "hypertension": True,
        "diabetes": False,
        "smokes": False,
        "alcohol": True
    },
    "survey_responses": {
        "ess_responses": [2, 2, 2, 2, 2, 1, 2, 1],  # ESS = 14 (high sleepiness)
        "berlin_responses": {
            "category1": {
                "snoring_loudly": True,
                "snoring_often": True,
                "breathing_pauses": True,
                "choking_gasping": True
            },
            "category2": {
                "morning_tiredness": True,
                "daytime_fatigue": True,
                "sleep_while_driving": False
            },
            "category3_sleepy": True
        },
        "stopbang_responses": {
            "snoring": True,
            "tired": True,
            "observed_apnea": True,
            "hypertension": True
        },
        "feels_sleepy_daytime": True,
        "snoring_level": "Very Loud",
        "snoring_frequency": "Every night",
        "snoring_bothers_others": True,
        "tired_during_day": "Very tired",
        "tired_after_sleep": "Still tired",
        "nodded_off_driving": False,
        "physical_activity_time": "Morning"
    },
    "google_fit": {
        "daily_steps": 5000,
        "average_daily_steps": 4800,
        "sleep_duration_hours": 6.0,
        "weekly_steps_data": {},
        "weekly_sleep_data": {}
    }
}

# Expected model input (14 features)
print("="*60)
print("EXPECTED MODEL INPUT (14 features):")
print("="*60)
expected_features = {
    'Age': 45,
    'Sex': 1,  # male
    'BMI': 27.8,  # 85kg / (1.75m)^2
    'Neck_Circumference': 42,
    'Hypertension': 1,
    'Diabetes': 0,
    'Smokes': 0,
    'Alcohol': 1,
    'Snoring': 1,
    'Sleepiness': 1,  # ESS > 10
    'Epworth_Score': 14,
    'Berlin_Score': 1,  # High risk
    'STOPBANG_Total': 5,  # Should be 5+ (age 45, male, BMI 27.8, neck 42, hypertension, snoring, tired, observed)
    'SleepQuality': 6  # Estimated from ESS
}

for feature, value in expected_features.items():
    print(f"  {feature}: {value}")

print("\n" + "="*60)
print("TESTING API ENDPOINT")
print("="*60)

# Note: This test assumes the Flask app is running
# You would need to start the Flask app first with: python app.py

BASE_URL = "http://localhost:5000"

print(f"\nTest URL: {BASE_URL}/api/survey/submit")
print("\nNOTE: This test requires:")
print("  1. Flask app running (python app.py)")
print("  2. Valid authentication token")
print("  3. Database initialized")
print("\nTo run this test:")
print("  1. Start Flask: cd backend && python app.py")
print("  2. Create a test user and get auth token")
print("  3. Update the test with the token")
print("  4. Run: python test_api_endpoint.py")

print("\n" + "="*60)
print("EXPECTED OUTPUT:")
print("="*60)
print("  - OSA Probability: ~0.90-1.00 (High Risk)")
print("  - Risk Level: High Risk")
print("  - Recommendations: Personalized based on risk factors")
print("  - Success: True")
print("="*60)

# Uncomment below to actually test (requires running server + auth token)
"""
# You would need a valid auth token from registration/login
AUTH_TOKEN = "your_auth_token_here"

headers = {
    "Authorization": f"Bearer {AUTH_TOKEN}",
    "Content-Type": "application/json"
}

try:
    response = requests.post(
        f"{BASE_URL}/api/survey/submit",
        headers=headers,
        json=test_survey_data,
        timeout=10
    )
    
    print(f"\nStatus Code: {response.status_code}")
    print(f"Response:")
    print(json.dumps(response.json(), indent=2))
    
    if response.status_code == 200:
        result = response.json()
        if result.get('success'):
            print("\n✅ TEST PASSED!")
            print(f"OSA Probability: {result.get('prediction', {}).get('osa_probability', 'N/A')}")
            print(f"Risk Level: {result.get('prediction', {}).get('risk_level', 'N/A')}")
        else:
            print("\n❌ TEST FAILED: API returned success=False")
    else:
        print(f"\n❌ TEST FAILED: HTTP {response.status_code}")
        
except requests.exceptions.ConnectionError:
    print("\n❌ Connection Error: Flask app not running")
    print("Start the Flask app with: cd backend && python app.py")
except Exception as e:
    print(f"\n❌ Error: {e}")
"""
