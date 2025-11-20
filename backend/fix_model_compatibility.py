"""
Fix compatibility issue with WakeUpCall_3Class5Fold_Pipeline.pkl
This script attempts to load the model with compatibility fixes
"""

import sys
import pickle
import joblib
import os

# Add compatibility fix for _RemainderColsList
try:
    from sklearn.compose._column_transformer import _RemainderColsList
except:
    # Create a dummy class if it doesn't exist
    import sklearn.compose._column_transformer as ct
    if not hasattr(ct, '_RemainderColsList'):
        class _RemainderColsList(list):
            pass
        ct._RemainderColsList = _RemainderColsList

model_path = 'WakeUpCall_3Class5Fold_Pipeline.pkl'

print(f"Attempting to load: {model_path}")
print(f"File exists: {os.path.exists(model_path)}")

if os.path.exists(model_path):
    try:
        # Try with encoding='latin1' which sometimes helps
        with open(model_path, 'rb') as f:
            model = pickle.load(f, encoding='latin1')
        print("✅ Model loaded successfully with pickle (latin1 encoding)!")
        print(f"Model type: {type(model)}")
        
        # Test prediction
        import pandas as pd
        sample = pd.DataFrame([{
            'Age': 52,
            'Sex': 1,
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
        }])
        
        prob = model.predict_proba(sample)[:, 1][0]
        pred = int(prob >= 0.5)
        print(f"\n✅ Test prediction successful!")
        print(f"Probability: {prob:.3f}")
        print(f"Class: {pred}")
        
    except Exception as e:
        print(f"❌ Error: {e}")
        import traceback
        traceback.print_exc()
        
        print("\n" + "="*60)
        print("SOLUTION: The model needs to be re-saved with compatible version")
        print("="*60)
        print("This model was saved with an older scikit-learn version.")
        print("You have two options:")
        print("1. Use final_lgbm_pipeline.pkl instead (already working)")
        print("2. Re-train and save the model with scikit-learn 1.7.2")
