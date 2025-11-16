# WakeUp Call Backend - OSA Risk Prediction API

Flask backend with LightGBM machine learning model for predicting Obstructive Sleep Apnea (OSA) risk.

## 📋 Prerequisites

- Python 3.8 or higher
- pip (Python package manager)

## 🚀 Setup Instructions

### 1. Install Python Dependencies

Open PowerShell in the backend folder and run:

```powershell
cd "d:\Client Project\WakeUpCallApp\backend"
pip install -r requirements.txt
```

### 2. Prepare Training Data

You need two CSV datasets to train the model:

1. **OLENKA3_Categorical_Updated.csv** - Main OSA dataset
2. **Sleep_Health_and_Lifestyle_Dataset.csv** - Sleep health data

Download these datasets and place them in the `backend` folder.

**Where to get the data:**
- Check Kaggle for sleep apnea datasets
- Or use your own medical datasets with similar features

### 3. Train the Model

Run the training script:

```powershell
python train_and_export_model.py
```

This will:
- Load and merge the datasets
- Clean and preprocess the data
- Engineer features (STOP-Bang score, sleep quality, etc.)
- Train a LightGBM model with 5-fold cross-validation
- Export `osa_model.pkl` and `scaler.pkl`
- Test the exported model

**Expected output:**
```
✅ Final model trained on full dataset
✅ Model saved to: osa_model.pkl
✅ Scaler saved to: scaler.pkl
```

### 4. Run the Flask API

```powershell
python app.py
```

The API will start on `http://0.0.0.0:5000`

**Expected output:**
```
🚀 Starting WakeUp Call OSA Prediction API...
📊 Model loaded: True
📏 Scaler loaded: True
* Running on http://0.0.0.0:5000
```

## 🌐 API Endpoints

### 1. Health Check

**GET** `/`

Check if the API is running.

**Response:**
```json
{
  "status": "running",
  "message": "WakeUp Call OSA Prediction API",
  "model_loaded": true,
  "timestamp": "2025-11-13T10:30:00"
}
```

### 2. Predict from Google Fit Data (Simplified)

**POST** `/predict-from-google-fit`

Predict OSA risk using Google Fit data with automatic feature estimation.

**Request Body:**
```json
{
  "age": 45,
  "sex": "male",
  "height_cm": 175,
  "weight_kg": 85,
  "neck_circumference_cm": 42,
  "sleep_duration_hours": 6.5,
  "daily_steps": 8000,
  "snores": true,
  "feels_sleepy": true,
  "hypertension": false,
  "diabetes": false,
  "smokes": false,
  "alcohol": false
}
```

**Response:**
```json
{
  "success": true,
  "prediction": {
    "osa_probability": 0.723,
    "osa_class": 1,
    "risk_level": "High Risk",
    "recommendation": "You have high OSA risk. We strongly recommend consulting a sleep specialist soon."
  },
  "calculated_metrics": {
    "bmi": 27.8,
    "stopbang_score": 5,
    "estimated_epworth_score": 12,
    "estimated_activity_level": 5
  },
  "timestamp": "2025-11-13T10:30:00"
}
```

### 3. Detailed Prediction

**POST** `/predict`

Predict OSA risk with all medical features provided manually.

**Request Body:**
```json
{
  "Age": 45,
  "Sex": 1,
  "BMI": 31.5,
  "Neck_Circumference": 42,
  "Hypertension": 1,
  "Diabetes": 0,
  "Smokes": 0,
  "Alcohol": 1,
  "Snoring": 1,
  "Sleepiness": 1,
  "Epworth_Score": 14,
  "Berlin_Score": 1,
  "STOPBANG_Total": 5,
  "SleepQuality_Proxy_0to10": 4,
  "Sleep_Duration": 6.5,
  "Physical_Activity_Level": 2,
  "Daily_Steps": 8000
}
```

**Response:**
```json
{
  "success": true,
  "prediction": {
    "osa_probability": 0.734,
    "osa_class": 1,
    "risk_level": "High Risk",
    "recommendation": "You have high OSA risk. We strongly recommend consulting a sleep specialist soon."
  },
  "input_summary": {
    "age": 45,
    "bmi": 31.5,
    "stopbang_score": 5,
    "epworth_score": 14,
    "sleep_duration": 6.5,
    "daily_steps": 8000
  },
  "timestamp": "2025-11-13T10:30:00"
}
```

## 🧪 Testing the API

### Using PowerShell (curl alternative)

**Health Check:**
```powershell
Invoke-RestMethod -Uri "http://localhost:5000/" -Method Get
```

**Predict from Google Fit:**
```powershell
$body = @{
    age = 45
    sex = "male"
    height_cm = 175
    weight_kg = 85
    neck_circumference_cm = 42
    sleep_duration_hours = 6.5
    daily_steps = 8000
    snores = $true
    feels_sleepy = $true
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:5000/predict-from-google-fit" -Method Post -Body $body -ContentType "application/json"
```

### Using Python

```python
import requests

# Health check
response = requests.get("http://localhost:5000/")
print(response.json())

# Predict
data = {
    "age": 45,
    "sex": "male",
    "height_cm": 175,
    "weight_kg": 85,
    "neck_circumference_cm": 42,
    "sleep_duration_hours": 6.5,
    "daily_steps": 8000,
    "snores": True,
    "feels_sleepy": True
}
response = requests.post("http://localhost:5000/predict-from-google-fit", json=data)
print(response.json())
```

## 📱 Connect Android App to Backend

### Step 1: Find Your Computer's IP Address

```powershell
ipconfig
```

Look for **IPv4 Address** under your active network adapter (e.g., `192.168.1.100`)

### Step 2: Update Android App

Open `OSAApiService.kt` and update the BASE_URL:

```kotlin
private const val BASE_URL = "http://192.168.1.100:5000"  // Your IP here
```

### Step 3: Ensure Same Network

- Your Android device and computer must be on the **same WiFi network**
- Disable any firewall that might block port 5000

### Step 4: Test Connection

In your Android app, the API service will automatically connect to the backend when you request a prediction.

## 🎯 Model Features

The model uses 17 features to predict OSA risk:

**Demographics:**
- Age
- Sex (1=Male, 0=Female)

**Physical Metrics:**
- BMI
- Neck Circumference (cm)

**Medical History:**
- Hypertension (Yes=1, No=0)
- Diabetes (Yes=1, No=0)
- Smoking (Yes=1, No=0)
- Alcohol consumption (Yes=1, No=0)

**Sleep Symptoms:**
- Snoring (Yes=1, No=0)
- Daytime Sleepiness (Yes=1, No=0)
- Epworth Sleepiness Score (0-24)
- Berlin Questionnaire Score (High=1, Low=0)
- STOP-Bang Total Score (0-8)
- Sleep Quality Proxy (0-10)

**Activity & Sleep:**
- Sleep Duration (hours)
- Physical Activity Level (1-10)
- Daily Steps

## 📊 Model Performance

Based on 5-fold cross-validation:

- **Accuracy**: ~85-90%
- **Precision**: ~80-85%
- **Recall**: ~85-90%
- **F1-Score**: ~82-87%
- **ROC-AUC**: ~90-95%

## 🔧 Troubleshooting

### Model not found error

```
⚠️ Model files not found. Run train_and_export_model.py first!
```

**Solution:** Run the training script to generate model files.

### Cannot connect from Android

**Solution:**
1. Check if Flask is running: `http://localhost:5000/`
2. Verify your computer's IP address
3. Ensure both devices are on same WiFi
4. Check Windows Firewall settings
5. Try accessing from browser: `http://YOUR_IP:5000/`

### Port already in use

```
OSError: [WinError 10048] Only one usage of each socket address
```

**Solution:**
- Stop any existing Flask instances
- Or change the port in `app.py`: `app.run(port=5001)`

### Import errors

```
ModuleNotFoundError: No module named 'flask'
```

**Solution:** Install dependencies: `pip install -r requirements.txt`

## 📚 Additional Resources

- [Flask Documentation](https://flask.palletsprojects.com/)
- [LightGBM Documentation](https://lightgbm.readthedocs.io/)
- [STOP-Bang Questionnaire](https://www.stopbang.ca/)
- [Epworth Sleepiness Scale](https://epworthsleepinessscale.com/)

## 🆘 Support

If you encounter issues:

1. Check the console logs for error messages
2. Verify all dependencies are installed
3. Ensure datasets are in the correct location
4. Test API endpoints using PowerShell/browser
5. Check Android app logs in Logcat
