"""Quick test to check which model loads in app.py"""
import sys
import os

# Set path
sys.path.insert(0, os.path.dirname(__file__))

print("Testing model loading...")
print("="*60)

# Import the model loading section
try:
    import joblib
    USE_JOBLIB = True
except ImportError:
    import pickle
    USE_JOBLIB = False

MODEL_PATHS = [
    os.path.join(os.path.dirname(__file__), 'WakeUpCall_3Class5Fold_Pipeline.pkl'),
    os.path.join(os.path.dirname(__file__), '..', 'model', 'WakeUpCall_3Class5Fold_Pipeline.pkl'),
    os.path.join(os.path.dirname(__file__), 'final_lgbm_pipeline.pkl'),
    os.path.join(os.path.dirname(__file__), '..', 'model', 'final_lgbm_pipeline.pkl'),
]

model = None
loaded_from = None

for model_path in MODEL_PATHS:
    if os.path.exists(model_path):
        print(f"\nTrying: {os.path.basename(model_path)}")
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
                loaded_from = model_path
                print(f"✅ SUCCESS!")
                break
        except Exception as e:
            print(f"❌ Failed: {str(e)[:100]}")
            
            # Try compatibility mode for WakeUpCall model
            if 'WakeUpCall_3Class5Fold' in model_path:
                try:
                    print(f"   Trying compatibility mode...")
                    import pickle
                    with open(model_path, 'rb') as f:
                        loaded_data = pickle.load(f, encoding='latin1')
                    model = loaded_data
                    loaded_from = model_path
                    print(f"✅ SUCCESS with compatibility mode!")
                    break
                except Exception as e2:
                    print(f"❌ Compatibility mode failed: {str(e2)[:100]}")

print("\n" + "="*60)
if model is not None:
    print(f"✅ FINAL RESULT: Model loaded successfully")
    print(f"   From: {os.path.basename(loaded_from)}")
    print(f"   Type: {type(model)}")
else:
    print("❌ FINAL RESULT: No model could be loaded")
    print("\n   Available models:")
    for p in MODEL_PATHS:
        exists = "✓" if os.path.exists(p) else "✗"
        print(f"   {exists} {os.path.basename(p)}")
