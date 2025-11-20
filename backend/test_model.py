"""
Test script to verify WakeUpCall_3Class5Fold_Pipeline.pkl works with 13 features
Expected output: Probability and Class prediction
"""

import pickle
import pandas as pd
import os
import sys

# Try joblib first (more compatible), fall back to pickle
try:
    import joblib
    USE_JOBLIB = True
except ImportError:
    USE_JOBLIB = False
    print("⚠️ joblib not found, using pickle (may have compatibility issues)")

# Sample input from user (13 features)
sample_input = {
    'Age': 52,
    'Sex': 1,  # Male
    'BMI': 36,
    'Neck_Circumference': 43,
    'Hypertension': 1,
    'Diabetes': 0,
    'Smokes': 0,
    'Alcohol': 1,
    'Snoring': 1,
    'Sleepiness': 1,
    'Epworth_Score': 16,
    'Berlin_Score': 1,
    'STOPBANG_Total': 6
}

# Feature order (must match training) - 13 features
FEATURES = [
    'Age', 'Sex', 'BMI', 'Neck_Circumference',
    'Hypertension', 'Diabetes', 'Smokes', 'Alcohol',
    'Snoring', 'Sleepiness', 'Epworth_Score', 'Berlin_Score',
    'STOPBANG_Total'
]

# Load model
model_path = os.path.join(os.path.dirname(__file__), 'WakeUpCall_3Class5Fold_Pipeline.pkl')
if not os.path.exists(model_path):
    model_path = os.path.join(os.path.dirname(__file__), '..', 'model', 'WakeUpCall_3Class5Fold_Pipeline.pkl')

print(f"Loading model from: {model_path}")
print(f"Model exists: {os.path.exists(model_path)}")
print(f"Python version: {sys.version}")

if os.path.exists(model_path):
    try:
        if USE_JOBLIB:
            print("Using joblib to load model...")
            model = joblib.load(model_path)
        else:
            print("Using pickle to load model...")
            with open(model_path, 'rb') as f:
                model = pickle.load(f)
        print(f"✅ Model loaded successfully!")
        print(f"Model type: {type(model)}")
        
        # Create DataFrame
        X_input = pd.DataFrame([{f: sample_input[f] for f in FEATURES}])
        print(f"\nInput shape: {X_input.shape}")
        print(f"Input features:\n{X_input}")
        
        # Predict
        y_prob = model.predict_proba(X_input)[:, 1][0]
        y_pred = int(y_prob >= 0.5)
        
        print(f"{'='*50}")
        print(f"PREDICTION RESULTS")
        print(f"{'='*50}")
        print(f"Predicted Probability of OSA: {y_prob:.2f}")
        print(f"Predicted Class: {y_pred} ({'High Risk' if y_pred == 1 else 'Low Risk'})")
        print(f"{'='*50}")
        
        # Show result
        if y_prob >= 0.5:
            print("✅ High risk detected for this patient profile")
        else:
            print("✅ Low/Moderate risk for this patient profile")
    
    except Exception as e:
        print(f"❌ Error loading or running model: {e}")
        import traceback
        traceback.print_exc()
else:
    print(f"❌ Model file not found at: {model_path}")
    print("Please ensure final_lgbm_pipeline.pkl is in the backend or model folder.")
