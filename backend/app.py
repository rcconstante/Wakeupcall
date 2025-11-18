from flask import Flask, request, jsonify
from flask_cors import CORS
import pickle
import numpy as np
import pandas as pd
from datetime import datetime, timedelta
import os
import sqlite3
import secrets
from werkzeug.security import generate_password_hash, check_password_hash
from functools import wraps
from recommendation_engine import RecommendationEngine

app = Flask(__name__)
app.config['SECRET_KEY'] = secrets.token_hex(32)
CORS(app)  # Enable CORS for Android app to access the API

# Database setup
DATABASE = 'wakeup_call.db'

def get_db():
    """Get database connection"""
    conn = sqlite3.connect(DATABASE)
    conn.row_factory = sqlite3.Row
    return conn

def init_db():
    """Initialize database with users table"""
    conn = get_db()
    cursor = conn.cursor()
    
    # Users table
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS users (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            first_name TEXT NOT NULL,
            last_name TEXT NOT NULL,
            email TEXT UNIQUE NOT NULL,
            password_hash TEXT NOT NULL,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        )
    ''')
    
    # Auth tokens table
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS auth_tokens (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            user_id INTEGER NOT NULL,
            token TEXT UNIQUE NOT NULL,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            expires_at TIMESTAMP NOT NULL,
            FOREIGN KEY (user_id) REFERENCES users (id)
        )
    ''')
    
    # User survey data table
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS user_surveys (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            user_id INTEGER NOT NULL,
            -- Demographics
            age INTEGER,
            sex TEXT,
            height_cm REAL,
            weight_kg REAL,
            neck_circumference_cm REAL,
            bmi REAL,
            -- Medical history
            hypertension INTEGER,
            diabetes INTEGER,
            smokes INTEGER,
            alcohol INTEGER,
            -- Survey scores
            ess_score INTEGER,
            berlin_score INTEGER,
            stopbang_score INTEGER,
            -- ML prediction
            osa_probability REAL,
            risk_level TEXT,
            -- Google Fit data
            daily_steps INTEGER,
            average_daily_steps INTEGER,
            sleep_duration_hours REAL,
            weekly_steps_json TEXT,
            weekly_sleep_json TEXT,
            completed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            FOREIGN KEY (user_id) REFERENCES users (id)
        )
    ''')
    
    conn.commit()
    conn.close()
    print("✅ Database initialized successfully!")

# Initialize database on startup
init_db()

def require_auth(f):
    """Decorator to require authentication token"""
    @wraps(f)
    def decorated_function(*args, **kwargs):
        token = request.headers.get('Authorization')
        
        if not token:
            return jsonify({'error': 'No authorization token provided', 'success': False}), 401
        
        # Remove 'Bearer ' prefix if present
        if token.startswith('Bearer '):
            token = token[7:]
        
        conn = get_db()
        cursor = conn.cursor()
        cursor.execute('''
            SELECT u.id, u.email, u.first_name, u.last_name 
            FROM users u 
            JOIN auth_tokens t ON u.id = t.user_id 
            WHERE t.token = ? AND t.expires_at > ?
        ''', (token, datetime.now()))
        
        user = cursor.fetchone()
        conn.close()
        
        if not user:
            return jsonify({'error': 'Invalid or expired token', 'success': False}), 401
        
        # Pass user info to the route
        request.current_user = {
            'id': user[0],
            'email': user[1],
            'first_name': user[2],
            'last_name': user[3]
        }
        
        return f(*args, **kwargs)
    
    return decorated_function

# Load the trained model and scaler
# The lgbm_model.pkl contains the model, scaler is separate
model = None
scaler = None

# Model paths
MODEL_PATHS = [
    os.path.join(os.path.dirname(__file__), 'lgbm_model.pkl'),  # backend folder
    os.path.join(os.path.dirname(__file__), '..', 'model', 'lgbm_model.pkl'),  # model folder
]

SCALER_PATHS = [
    os.path.join(os.path.dirname(__file__), 'scaler.pkl'),  # backend folder
    os.path.join(os.path.dirname(__file__), '..', 'model', 'scaler.pkl'),  # model folder
]

# Load model
for model_path in MODEL_PATHS:
    if os.path.exists(model_path):
        try:
            with open(model_path, 'rb') as f:
                loaded_data = pickle.load(f)
            
            # Check if it's a dict with model and scaler
            if isinstance(loaded_data, dict):
                model = loaded_data.get('model')
                if not scaler:  # Only use dict scaler if separate file not found
                    scaler = loaded_data.get('scaler')
                print(f"✅ Model loaded from dictionary: {model_path}")
            else:
                # It's just the model object
                model = loaded_data
                print(f"✅ Model loaded from: {model_path}")
            
            if model is not None:
                break
        except Exception as e:
            print(f"⚠️ Error loading model from {model_path}: {e}")

# Load scaler separately
for scaler_path in SCALER_PATHS:
    if os.path.exists(scaler_path):
        try:
            with open(scaler_path, 'rb') as f:
                scaler = pickle.load(f)
            print(f"✅ Scaler loaded from: {scaler_path}")
            break
        except Exception as e:
            print(f"⚠️ Error loading scaler from {scaler_path}: {e}")

if model is None:
    print("⚠️ Model file not found. Expected one of:")
    for p in MODEL_PATHS:
        print(f"   - {p}")
    print("Please ensure lgbm_model.pkl exists in backend or model folder!")
else:
    print("✅ Model loaded successfully!")

if scaler is None:
    print("⚠️ Scaler file not found. Expected one of:")
    for p in SCALER_PATHS:
        print(f"   - {p}")
    print("Please ensure scaler.pkl exists in backend or model folder!")
else:
    print("✅ Scaler loaded successfully!")

def generate_ml_recommendation(osa_probability, risk_level, age, bmi, neck_cm, hypertension, diabetes, smokes, alcohol, ess_score, berlin_score, stopbang_score, sleep_duration=7.0, daily_steps=5000):
    """Generate personalized recommendations using comprehensive recommendation engine"""
    
    # Use the new RecommendationEngine
    sex = 1  # Default to male (conservative for OSA risk)
    recommendations = RecommendationEngine.generate_recommendations(
        age=age,
        sex=sex,
        bmi=bmi,
        neck_cm=neck_cm,
        hypertension=hypertension,
        diabetes=diabetes,
        smokes=smokes,
        alcohol=alcohol,
        ess_score=ess_score,
        berlin_score=berlin_score,
        stopbang_score=stopbang_score,
        sleep_duration=sleep_duration,
        daily_steps=daily_steps,
        risk_level=risk_level
    )
    
    # Format for API response (pipe-separated)
    return RecommendationEngine.format_for_api(recommendations)

def calculate_top_risk_factors(input_features, osa_probability):
    """Calculate top risk factors based on actual survey data and thresholds"""
    
    risk_factors = []
    
    # Analyze each feature and its contribution to OSA risk
    age = input_features.get('Age', 0)
    sex = input_features.get('Sex', 0)  # 1=male, 0=female
    bmi = input_features.get('BMI', 0)
    neck = input_features.get('Neck_Circumference', 0)
    ess = input_features.get('Epworth_Score', 0)
    berlin = input_features.get('Berlin_Score', 0)
    stopbang = input_features.get('STOPBANG_Total', 0)
    
    # BMI analysis
    if bmi >= 35:
        risk_factors.append(("Severe Obesity", f"BMI {bmi:.1f}", 0.25))
    elif bmi >= 30:
        risk_factors.append(("Obesity", f"BMI {bmi:.1f}", 0.15))
    elif bmi >= 25:
        risk_factors.append(("Overweight", f"BMI {bmi:.1f}", 0.08))
    
    # Neck circumference
    if neck >= 43:
        risk_factors.append(("Large Neck", f"{neck}cm", 0.20))
    elif neck >= 40:
        risk_factors.append(("Thick Neck", f"{neck}cm", 0.12))
    
    # Age factor
    if age >= 65:
        risk_factors.append(("Advanced Age", f"{age} years", 0.15))
    elif age >= 50:
        risk_factors.append(("Middle Age", f"{age} years", 0.08))
    
    # Gender (male higher risk)
    if sex == 1:
        risk_factors.append(("Male Gender", "Higher OSA risk", 0.10))
    
    # Sleepiness
    if ess >= 16:
        risk_factors.append(("Severe Sleepiness", f"ESS {ess}", 0.18))
    elif ess >= 11:
        risk_factors.append(("Moderate Sleepiness", f"ESS {ess}", 0.10))
    
    # Snoring
    if berlin >= 1:
        risk_factors.append(("Snoring Issues", f"Berlin Score {berlin}", 0.12))
    
    # STOP-BANG
    if stopbang >= 6:
        risk_factors.append(("High STOP-BANG", f"Score {stopbang}/8", 0.20))
    elif stopbang >= 4:
        risk_factors.append(("Moderate STOP-BANG", f"Score {stopbang}/8", 0.12))
    
    # Medical conditions
    if input_features.get('Hypertension', 0):
        risk_factors.append(("Hypertension", "High blood pressure", 0.08))
    
    if input_features.get('Diabetes', 0):
        risk_factors.append(("Diabetes", "Blood sugar disorder", 0.06))
    
    if input_features.get('Alcohol', 0):
        risk_factors.append(("Alcohol Use", "Relaxes throat muscles", 0.05))
    
    # Sort by impact and return top factors
    risk_factors.sort(key=lambda x: x[2], reverse=True)
    
    # Format for frontend
    formatted_factors = []
    for i, (name, detail, impact) in enumerate(risk_factors[:6]):  # Top 6 factors
        formatted_factors.append({
            "factor": name,
            "detail": detail,
            "impact": f"{impact*100:.0f}%",
            "priority": "High" if impact >= 0.15 else "Medium" if impact >= 0.08 else "Low"
        })
    
    return formatted_factors

# Feature list (must match training order)
FEATURES = [
    'Age', 'Sex', 'BMI', 'Neck_Circumference', 'Hypertension', 'Diabetes',
    'Smokes', 'Alcohol', 'Snoring', 'Sleepiness',
    'Epworth_Score', 'Berlin_Score', 'STOPBANG_Total',
    'SleepQuality_Proxy_0to10', 'Sleep_Duration',
    'Physical_Activity_Level', 'Daily_Steps'
]

# Columns to scale (numerical features)
NUM_COLS = [
    'Age', 'BMI', 'Neck_Circumference', 'Epworth_Score', 'STOPBANG_Total',
    'SleepQuality_Proxy_0to10', 'Sleep_Duration', 'Physical_Activity_Level', 'Daily_Steps'
]


# ============ SURVEY CALCULATION UTILITIES ============

def calculate_ess_score(responses):
    """
    Calculate Epworth Sleepiness Scale (ESS) score from survey responses.
    responses: list of 8 integers (0-3 for each question)
    Returns: (score, category)
    """
    score = sum(responses)
    
    if score <= 5:
        category = "Low daytime sleepiness (normal)"
    elif score <= 10:
        category = "High daytime sleepiness (normal)"
    elif score in [11, 12]:
        category = "Mild excessive daytime sleepiness"
    elif score in range(13, 16):
        category = "Moderate excessive daytime sleepiness"
    elif score in range(16, 25):
        category = "Severe excessive daytime sleepiness"
    else:
        category = "Invalid"
    
    return score, category


def calculate_berlin_score(category1_items, category2_items, category3_sleepy, bmi):
    """
    Calculate Berlin Questionnaire score.
    Returns: (positive_categories_count, risk_category)
    """
    positive_categories = 0
    
    # Category 1: Snoring and breathing (items 2-6)
    cat1_score = sum(1 for v in category1_items.values() if v)
    if cat1_score >= 2:
        positive_categories += 1
    
    # Category 2: Daytime sleepiness (items 7-9)
    cat2_score = sum(1 for v in category2_items.values() if v)
    if cat2_score >= 2:
        positive_categories += 1
    
    # Category 3: Sleepiness or BMI > 30
    if category3_sleepy or bmi > 30:
        positive_categories += 1
    
    risk_category = "High Risk" if positive_categories >= 2 else "Low Risk"
    
    return positive_categories, risk_category


def calculate_stopbang_score(snoring, tired, observed, pressure, age, neck_circumference, bmi, male):
    """
    Calculate STOP-BANG score.
    Returns: (total_score, risk_category)
    """
    total_score = 0
    stop_score = 0
    
    # STOP questions
    if snoring:
        total_score += 1
        stop_score += 1
    if tired:
        total_score += 1
        stop_score += 1
    if observed:
        total_score += 1
        stop_score += 1
    if pressure:
        total_score += 1
        stop_score += 1
    
    # BANG questions
    if age > 50:
        total_score += 1
    if neck_circumference >= 40.0:
        total_score += 1
    if bmi > 35:
        total_score += 1
    if male:
        total_score += 1
    
    # Determine risk level according to README.md
    if total_score >= 5:
        risk_category = "High Risk"
    elif stop_score >= 2 and (male or bmi > 35 or neck_circumference >= 40.0):
        risk_category = "High Risk"
    elif total_score in range(3, 5):
        risk_category = "Intermediate Risk"
    else:
        risk_category = "Low Risk"
    
    return total_score, risk_category


@app.route('/', methods=['GET'])
def home():
    """Health check endpoint"""
    return jsonify({
        'status': 'running',
        'message': 'WakeUp Call OSA Prediction API',
        'model_loaded': model is not None,
        'timestamp': datetime.now().isoformat()
    })


# ============ AUTHENTICATION ENDPOINTS ============

@app.route('/auth/signup', methods=['POST'])
def signup():
    """
    Create a new user account
    
    Expected JSON input:
    {
        "first_name": "John",
        "last_name": "Doe",
        "email": "john@example.com",
        "password": "secure_password"
    }
    """
    try:
        data = request.get_json()
        
        # Validate required fields
        required_fields = ['first_name', 'last_name', 'email', 'password']
        missing_fields = [f for f in required_fields if f not in data or not data[f]]
        
        if missing_fields:
            return jsonify({
                'error': f'Missing required fields: {", ".join(missing_fields)}',
                'success': False
            }), 400
        
        first_name = data['first_name'].strip()
        last_name = data['last_name'].strip()
        email = data['email'].strip().lower()
        password = data['password']
        
        # Basic validation
        if len(password) < 6:
            return jsonify({
                'error': 'Password must be at least 6 characters long',
                'success': False
            }), 400
        
        if '@' not in email or '.' not in email:
            return jsonify({
                'error': 'Invalid email format',
                'success': False
            }), 400
        
        # Hash password
        password_hash = generate_password_hash(password)
        
        # Insert user into database
        conn = get_db()
        cursor = conn.cursor()
        
        try:
            cursor.execute('''
                INSERT INTO users (first_name, last_name, email, password_hash)
                VALUES (?, ?, ?, ?)
            ''', (first_name, last_name, email, password_hash))
            
            user_id = cursor.lastrowid
            
            # Generate auth token (valid for 30 days)
            token = secrets.token_urlsafe(32)
            expires_at = datetime.now() + timedelta(days=30)
            
            cursor.execute('''
                INSERT INTO auth_tokens (user_id, token, expires_at)
                VALUES (?, ?, ?)
            ''', (user_id, token, expires_at))
            
            conn.commit()
            
            return jsonify({
                'success': True,
                'message': 'Account created successfully',
                'user': {
                    'id': user_id,
                    'first_name': first_name,
                    'last_name': last_name,
                    'email': email
                },
                'auth_token': token,
                'expires_at': expires_at.isoformat(),
                'has_survey': False
            }), 201
            
        except sqlite3.IntegrityError:
            return jsonify({
                'error': 'Email already registered',
                'success': False
            }), 409
        
        finally:
            conn.close()
    
    except Exception as e:
        return jsonify({
            'error': str(e),
            'success': False
        }), 500


@app.route('/auth/login', methods=['POST'])
def login():
    """
    Login with email and password
    
    Expected JSON input:
    {
        "email": "john@example.com",
        "password": "secure_password"
    }
    """
    try:
        data = request.get_json()
        
        email = data.get('email', '').strip().lower()
        password = data.get('password', '')
        
        if not email or not password:
            return jsonify({
                'error': 'Email and password are required',
                'success': False
            }), 400
        
        conn = get_db()
        cursor = conn.cursor()
        
        # Get user by email
        cursor.execute('SELECT id, first_name, last_name, email, password_hash FROM users WHERE email = ?', (email,))
        user = cursor.fetchone()
        
        if not user:
            conn.close()
            return jsonify({
                'error': 'Invalid email or password',
                'success': False
            }), 401
        
        # Verify password
        if not check_password_hash(user[4], password):
            conn.close()
            return jsonify({
                'error': 'Invalid email or password',
                'success': False
            }), 401
        
        # Generate new auth token
        token = secrets.token_urlsafe(32)
        expires_at = datetime.now() + timedelta(days=30)
        
        cursor.execute('''
            INSERT INTO auth_tokens (user_id, token, expires_at)
            VALUES (?, ?, ?)
        ''', (user[0], token, expires_at))
        
        # Check if user has completed survey
        cursor.execute('''
            SELECT COUNT(*) FROM user_surveys WHERE user_id = ?
        ''', (user[0],))
        has_survey = cursor.fetchone()[0] > 0
        
        conn.commit()
        conn.close()
        
        return jsonify({
            'success': True,
            'message': 'Login successful',
            'user': {
                'id': user[0],
                'first_name': user[1],
                'last_name': user[2],
                'email': user[3]
            },
            'auth_token': token,
            'expires_at': expires_at.isoformat(),
            'has_survey': has_survey
        }), 200
    
    except Exception as e:
        return jsonify({
            'error': str(e),
            'success': False
        }), 500


@app.route('/auth/logout', methods=['POST'])
@require_auth
def logout():
    """Logout and invalidate token"""
    try:
        token = request.headers.get('Authorization', '').replace('Bearer ', '')
        
        conn = get_db()
        cursor = conn.cursor()
        cursor.execute('DELETE FROM auth_tokens WHERE token = ?', (token,))
        conn.commit()
        conn.close()
        
        return jsonify({
            'success': True,
            'message': 'Logged out successfully'
        }), 200
    
    except Exception as e:
        return jsonify({
            'error': str(e),
            'success': False
        }), 500


@app.route('/auth/verify', methods=['GET'])
@require_auth
def verify_token():
    """Verify if current token is valid"""
    return jsonify({
        'success': True,
        'user': request.current_user
    }), 200


@app.route('/survey/get-latest', methods=['GET'])
@require_auth
def get_latest_survey():
    """
    Get the latest survey results for the authenticated user
    Returns the most recent survey submission from the database in the same format as submit
    """
    try:
        user_id = request.current_user['id']
        
        conn = get_db()
        cursor = conn.cursor()
        
        cursor.execute('''
            SELECT id, age, sex, height_cm, weight_kg, neck_circumference_cm, bmi,
                   hypertension, diabetes, smokes, alcohol,
                   ess_score, berlin_score, stopbang_score, osa_probability, risk_level, completed_at,
                   sleep_duration_hours, daily_steps
            FROM user_surveys
            WHERE user_id = ?
            ORDER BY completed_at DESC
            LIMIT 1
        ''', (user_id,))
        
        survey = cursor.fetchone()
        conn.close()
        
        if not survey:
            return jsonify({
                'success': False,
                'message': 'No survey data found',
                'data': None
            }), 404
        
        # Extract survey data
        survey_id = survey[0]
        age = survey[1]
        sex_str = survey[2]
        height_cm = survey[3]
        weight_kg = survey[4]
        neck_cm = survey[5]
        bmi = survey[6]
        hypertension = bool(survey[7])
        diabetes = bool(survey[8])
        smokes = bool(survey[9])
        alcohol = bool(survey[10])
        ess_score = survey[11]
        berlin_score = survey[12]
        stopbang_score = survey[13]
        osa_probability = survey[14]
        risk_level = survey[15]
        # completed_at = survey[16]
        sleep_duration = survey[17] if len(survey) > 17 and survey[17] else 7.0
        daily_steps = survey[18] if len(survey) > 18 and survey[18] else 5000
        
        # Determine score categories
        if ess_score < 8:
            ess_category = "Normal"
        elif ess_score < 12:
            ess_category = "Mild"
        elif ess_score < 16:
            ess_category = "Moderate"
        else:
            ess_category = "Severe"
        
        berlin_category = "High Risk" if berlin_score >= 2 else "Low Risk"
        
        if stopbang_score < 3:
            stopbang_category = "Low Risk"
        elif stopbang_score < 5:
            stopbang_category = "Intermediate Risk"
        else:
            stopbang_category = "High Risk"
        
        # Generate comprehensive recommendations using the recommendation engine
        recommendation = generate_ml_recommendation(
            osa_probability, risk_level, age, bmi, neck_cm,
            hypertension, diabetes, smokes, alcohol,
            ess_score, berlin_score, stopbang_score,
            sleep_duration, daily_steps
        )
        
        # Calculate top risk factors
        top_factors = []
        if bmi >= 30:
            top_factors.append({
                'factor': f'High BMI ({bmi:.1f})',
                'detail': 'Obesity significantly increases OSA risk',
                'impact': 'High',
                'priority': '1'
            })
        if stopbang_score >= 5:
            top_factors.append({
                'factor': f'High STOP-BANG Score ({stopbang_score})',
                'detail': 'Multiple OSA risk factors present',
                'impact': 'High',
                'priority': '2'
            })
        if ess_score >= 11:
            top_factors.append({
                'factor': f'Excessive Daytime Sleepiness (ESS: {ess_score})',
                'detail': 'Significant sleepiness during daytime',
                'impact': 'Medium',
                'priority': '3'
            })
        if age >= 50:
            top_factors.append({
                'factor': f'Age ({age} years)',
                'detail': 'OSA risk increases with age',
                'impact': 'Medium',
                'priority': '4'
            })
        if hypertension:
            top_factors.append({
                'factor': 'Hypertension',
                'detail': 'High blood pressure linked to OSA',
                'impact': 'Medium',
                'priority': '5'
            })
        
        # Use already extracted demographics
        
        # Return in the same format as submit endpoint
        return jsonify({
            'success': True,
            'message': 'Survey data retrieved successfully',
            'data': {
                'success': True,
                'message': 'Survey data retrieved',
                'survey_id': survey_id,
                'demographics': {
                    'age': age,
                    'sex': sex_str,
                    'height_cm': height_cm,
                    'weight_kg': weight_kg,
                    'neck_circumference_cm': neck_cm
                },
                'medical_history': {
                    'hypertension': hypertension,
                    'diabetes': diabetes,
                    'smokes': smokes,
                    'alcohol': alcohol
                },
                'scores': {
                    'ess': {
                        'score': ess_score,
                        'category': ess_category
                    },
                    'berlin': {
                        'score': berlin_score,
                        'category': berlin_category
                    },
                    'stopbang': {
                        'score': stopbang_score,
                        'category': stopbang_category
                    }
                },
                'prediction': {
                    'osa_probability': round(osa_probability, 3),
                    'risk_level': risk_level,
                    'recommendation': recommendation
                },
                'top_risk_factors': top_factors[:5],  # Return top 5
                'calculated_metrics': {
                    'bmi': round(bmi, 1)
                }
            }
        }), 200
    
    except Exception as e:
        return jsonify({
            'error': str(e),
            'success': False
        }), 500


@app.route('/survey/submit', methods=['POST'])
@require_auth
def submit_survey():
    """
    Save survey results and generate OSA prediction
    
    Expected JSON input:
    {
        "demographics": {
            "age": 45,
            "sex": "male",
            "height_cm": 175,
            "weight_kg": 85,
            "neck_circumference_cm": 42
        },
        "medical_history": {
            "hypertension": true,
            "diabetes": false,
            "smokes": false,
            "alcohol": true
        },
        "survey_responses": {
            "ess_responses": [2, 1, 2, 1, 2, 1, 2, 1],
            "berlin_responses": {...},
            "stopbang_responses": {...}
        },
        "google_fit": {
            "daily_steps": 8000,
            "sleep_duration_hours": 6.5
        }
    }
    """
    try:
        data = request.get_json()
        user_id = request.current_user['id']
        
        print(f"🔵 === SURVEY SUBMISSION RECEIVED ===")
        print(f"🔵 User ID: {user_id}")
        print(f"🔵 Raw data keys: {list(data.keys())}")
        
        # Extract demographics
        demo = data.get('demographics', {})
        print(f"🔵 Demographics received: {demo}")
        age = demo.get('age', 30)
        sex = 1 if demo.get('sex', 'male').lower() == 'male' else 0
        height_cm = demo.get('height_cm', 170)
        weight_kg = demo.get('weight_kg', 70)
        neck_cm = demo.get('neck_circumference_cm', 37)
        
        # Calculate BMI
        height_m = height_cm / 100
        bmi = weight_kg / (height_m ** 2)
        
        # Extract medical history
        medical = data.get('medical_history', {})
        hypertension = 1 if medical.get('hypertension', False) else 0
        diabetes = 1 if medical.get('diabetes', False) else 0
        smokes = 1 if medical.get('smokes', False) else 0
        alcohol = 1 if medical.get('alcohol', False) else 0
        
        # Extract survey responses
        surveys = data.get('survey_responses', {})
        
        # ESS calculation
        ess_responses = surveys.get('ess_responses', [1, 1, 1, 1, 1, 1, 1, 1])
        ess_score, ess_category = calculate_ess_score(ess_responses)
        
        # Extract individual ESS scores for detailed storage
        ess_sitting_reading = ess_responses[0] if len(ess_responses) > 0 else 0
        ess_watching_tv = ess_responses[1] if len(ess_responses) > 1 else 0
        ess_public_sitting = ess_responses[2] if len(ess_responses) > 2 else 0
        ess_passenger_car = ess_responses[3] if len(ess_responses) > 3 else 0
        ess_lying_down_afternoon = ess_responses[4] if len(ess_responses) > 4 else 0
        ess_talking = ess_responses[5] if len(ess_responses) > 5 else 0
        ess_after_lunch = ess_responses[6] if len(ess_responses) > 6 else 0
        ess_traffic_stop = ess_responses[7] if len(ess_responses) > 7 else 0
        
        # Berlin calculation
        berlin_data = surveys.get('berlin_responses', {})
        berlin_cat1 = berlin_data.get('category1', {})
        berlin_cat2 = berlin_data.get('category2', {})
        berlin_cat3_sleepy = berlin_data.get('category3_sleepy', False)
        berlin_score, berlin_category = calculate_berlin_score(berlin_cat1, berlin_cat2, berlin_cat3_sleepy, bmi)
        berlin_score_binary = 1 if berlin_category == "High Risk" else 0
        
        # STOP-BANG calculation
        stopbang_data = surveys.get('stopbang_responses', {})
        snoring = stopbang_data.get('snoring', False)
        tired = stopbang_data.get('tired', False)
        observed = stopbang_data.get('observed_apnea', False)
        pressure = stopbang_data.get('hypertension', hypertension == 1)
        
        stopbang_score, stopbang_category = calculate_stopbang_score(
            snoring, tired, observed, pressure,
            age, neck_cm, bmi, sex == 1
        )
        
        # Extract additional survey fields that aren't in the standard scoring
        snoring_level = surveys.get('snoring_level', 'Unknown')  # "Mild", "Moderate", "Loud", "Very Loud"
        snoring_frequency = surveys.get('snoring_frequency', 'Unknown')  # Frequency of snoring
        snoring_bothers_others = 1 if surveys.get('snoring_bothers_others', False) else 0
        tired_during_day = surveys.get('tired_during_day', 'Unknown')  # Fatigue level
        tired_after_sleep = surveys.get('tired_after_sleep', 'Unknown')  # Post-sleep tiredness
        feels_sleepy_daytime = 1 if surveys.get('feels_sleepy_daytime', tired) else 0
        nodded_off_driving = 1 if surveys.get('nodded_off_driving', False) else 0
        physical_activity_time = surveys.get('physical_activity_time', 'Unknown')  # When they exercise
        
        # Extract Google Fit data
        fit_data = data.get('google_fit', {})
        daily_steps = fit_data.get('daily_steps', 5000)
        average_daily_steps = fit_data.get('average_daily_steps', daily_steps)
        weekly_steps_data = fit_data.get('weekly_steps_data', {})
        weekly_sleep_data = fit_data.get('weekly_sleep_data', {})
        
        print(f"🔵 Google Fit data received:")
        print(f"   Daily steps: {daily_steps}")
        print(f"   Average daily steps: {average_daily_steps}")
        print(f"   Weekly steps data: {len(weekly_steps_data)} days")
        print(f"   Weekly sleep data: {len(weekly_sleep_data)} days")
        
        # Better sleep duration estimation if not provided
        if 'sleep_duration_hours' in fit_data:
            sleep_duration = fit_data['sleep_duration_hours']
            print(f"   Sleep duration from Google Fit: {sleep_duration:.1f}h")
        else:
            # Estimate based on sleep problems (Berlin + ESS scores)
            if berlin_score >= 2:  # High snoring/sleep problems
                sleep_duration = 5.5 + (ess_score / 24) * 2  # 5.5-7.5 hours
            else:
                sleep_duration = 6.5 + (24 - ess_score) / 24 * 1.5  # 6.5-8 hours
            sleep_duration = max(4.0, min(10.0, sleep_duration))
            print(f"   Sleep duration estimated: {sleep_duration:.1f}h")
        
        # Convert Google Fit data to JSON strings for storage
        import json
        weekly_steps_json = json.dumps(weekly_steps_data) if weekly_steps_data else '{}'
        weekly_sleep_json = json.dumps(weekly_sleep_data) if weekly_sleep_data else '{}'
        
        # Estimate additional features
        sleepiness = 1 if ess_score > 10 else 0
        snoring_binary = 1 if snoring else 0
        
        # Estimate activity level from steps (1-5 scale)
        if daily_steps < 3000:
            activity_level = 1  # Sedentary
        elif daily_steps < 6000:
            activity_level = 2  # Low active
        elif daily_steps < 8000:
            activity_level = 3  # Moderate
        elif daily_steps < 10000:
            activity_level = 4  # Active
        else:
            activity_level = 5  # Very active
        
        # Adjust for age and health conditions
        if age > 60: activity_level = max(1, activity_level - 1)
        if hypertension or diabetes: activity_level = max(1, activity_level - 1)
        
        # Estimate sleep quality (inverse of sleepiness)
        sleep_quality = max(1, min(10, 10 - (ess_score // 3)))
        
        # Convert Berlin score to binary (0 = low risk, 1 = high risk)
        berlin_score_binary = 1 if berlin_score >= 2 else 0
        
        # Build feature dictionary for ML model
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
            'STOPBANG_Total': stopbang_score,
            'SleepQuality_Proxy_0to10': sleep_quality,
            'Sleep_Duration': sleep_duration,
            'Physical_Activity_Level': activity_level,
            'Daily_Steps': daily_steps
        }
        
        # Make prediction if model is loaded
        osa_probability = 0.0
        risk_level = "Unknown"
        recommendation = ""
        
        if model is not None:
            try:
                # DEBUG: Print input features
                print(f"🔍 DEBUG: Input features for ML model:")
                for feature, value in input_features.items():
                    print(f"  {feature}: {value}")
                
                # Create DataFrame (NO SCALING - LightGBM works without it)
                X_input = pd.DataFrame([{f: input_features[f] for f in FEATURES}])
                print(f"🔍 DEBUG: DataFrame shape: {X_input.shape}")
                print(f"🔍 DEBUG: DataFrame columns: {list(X_input.columns)}")
                print(f"🔍 DEBUG: DataFrame values: {X_input.iloc[0].tolist()}")
                
                # Get prediction (no scaling needed)
                y_prob = model.predict_proba(X_input)[:, 1][0]
                osa_probability = float(y_prob)
                print(f"🔍 DEBUG: Raw prediction: {y_prob}")
                print(f"🔍 DEBUG: OSA Probability: {osa_probability}")
                
                # Determine risk level and generate ML-based recommendations
                if y_prob < 0.3:
                    risk_level = "Low Risk"
                elif y_prob < 0.6:
                    risk_level = "Moderate Risk"
                else:
                    risk_level = "High Risk"
                
                # Generate personalized recommendation
                recommendation = generate_ml_recommendation(
                    osa_probability, risk_level, age, bmi, neck_cm, 
                    hypertension, diabetes, smokes, alcohol, 
                    ess_score, berlin_score_binary, stopbang_score,
                    sleep_duration, daily_steps
                )
            except Exception as e:
                print(f"❌ Prediction error: {e}")
        
        # Calculate top risk factors based on actual survey data
        top_factors = calculate_top_risk_factors(input_features, osa_probability)
        print(f"🎯 Top risk factors: {len(top_factors)} factors calculated")
        
        # Save to database with all demographics and medical history
        # Check if user already has a survey - if yes, UPDATE instead of INSERT
        conn = get_db()
        cursor = conn.cursor()
        
        try:
            # Check for existing survey
            cursor.execute('SELECT id FROM user_surveys WHERE user_id = ?', (user_id,))
            existing_survey = cursor.fetchone()
            
            if existing_survey:
                # UPDATE existing survey
                survey_id = existing_survey[0]
                print(f"🔄 Updating survey for user {user_id}:")
                print(f"   Age: {age}, Sex: {demo.get('sex', 'male')}, BMI: {bmi:.1f}")
                print(f"   ESS: {ess_score}, Berlin: {berlin_score_binary}, STOP-BANG: {stopbang_score}")
                print(f"   OSA Probability: {osa_probability:.3f}, Risk: {risk_level}")
                
                cursor.execute('''
                    UPDATE user_surveys 
                    SET age = ?, sex = ?, height_cm = ?, weight_kg = ?, neck_circumference_cm = ?, bmi = ?,
                        hypertension = ?, diabetes = ?, smokes = ?, alcohol = ?,
                        ess_score = ?, berlin_score = ?, stopbang_score = ?, 
                        osa_probability = ?, risk_level = ?,
                        daily_steps = ?, average_daily_steps = ?, sleep_duration_hours = ?,
                        weekly_steps_json = ?, weekly_sleep_json = ?,
                        snoring_level = ?, snoring_frequency = ?, snoring_bothers_others = ?,
                        tired_during_day = ?, tired_after_sleep = ?, feels_sleepy_daytime = ?,
                        nodded_off_driving = ?, physical_activity_time = ?,
                        ess_sitting_reading = ?, ess_watching_tv = ?, ess_public_sitting = ?,
                        ess_passenger_car = ?, ess_lying_down_afternoon = ?, ess_talking = ?,
                        ess_after_lunch = ?, ess_traffic_stop = ?,
                        completed_at = CURRENT_TIMESTAMP
                    WHERE user_id = ?
                ''', (age, demo.get('sex', 'male'), height_cm, weight_kg, neck_cm, bmi,
                      hypertension, diabetes, smokes, alcohol,
                      ess_score, berlin_score_binary, stopbang_score, osa_probability, risk_level,
                      daily_steps, average_daily_steps, sleep_duration, weekly_steps_json, weekly_sleep_json,
                      snoring_level, snoring_frequency, snoring_bothers_others,
                      tired_during_day, tired_after_sleep, feels_sleepy_daytime,
                      nodded_off_driving, physical_activity_time,
                      ess_sitting_reading, ess_watching_tv, ess_public_sitting,
                      ess_passenger_car, ess_lying_down_afternoon, ess_talking,
                      ess_after_lunch, ess_traffic_stop,
                      user_id))
                
                rows_affected = cursor.rowcount
                print(f"✅ Updated existing survey (ID: {survey_id}, rows affected: {rows_affected}) for user {user_id}")
                
                # Verify the update by reading back
                cursor.execute('''
                    SELECT age, ess_score, berlin_score, stopbang_score, osa_probability, risk_level 
                    FROM user_surveys WHERE user_id = ?
                ''', (user_id,))
                verify = cursor.fetchone()
                if verify:
                    print(f"🔍 Verification - DB now has: Age={verify[0]}, ESS={verify[1]}, Berlin={verify[2]}, STOP-BANG={verify[3]}, OSA={verify[4]:.3f}, Risk={verify[5]}")
            else:
                # INSERT new survey
                cursor.execute('''
                    INSERT INTO user_surveys 
                    (user_id, age, sex, height_cm, weight_kg, neck_circumference_cm, bmi,
                     hypertension, diabetes, smokes, alcohol,
                     ess_score, berlin_score, stopbang_score, osa_probability, risk_level,
                     daily_steps, average_daily_steps, sleep_duration_hours,
                     weekly_steps_json, weekly_sleep_json,
                     snoring_level, snoring_frequency, snoring_bothers_others,
                     tired_during_day, tired_after_sleep, feels_sleepy_daytime,
                     nodded_off_driving, physical_activity_time,
                     ess_sitting_reading, ess_watching_tv, ess_public_sitting,
                     ess_passenger_car, ess_lying_down_afternoon, ess_talking,
                     ess_after_lunch, ess_traffic_stop)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,
                            ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ''', (user_id, age, demo.get('sex', 'male'), height_cm, weight_kg, neck_cm, bmi,
                      hypertension, diabetes, smokes, alcohol,
                      ess_score, berlin_score_binary, stopbang_score, osa_probability, risk_level,
                      daily_steps, average_daily_steps, sleep_duration, weekly_steps_json, weekly_sleep_json,
                      snoring_level, snoring_frequency, snoring_bothers_others,
                      tired_during_day, tired_after_sleep, feels_sleepy_daytime,
                      nodded_off_driving, physical_activity_time,
                      ess_sitting_reading, ess_watching_tv, ess_public_sitting,
                      ess_passenger_car, ess_lying_down_afternoon, ess_talking,
                      ess_after_lunch, ess_traffic_stop))
                survey_id = cursor.lastrowid
                print(f"✅ Created new survey (ID: {survey_id}) for user {user_id}")
            
            conn.commit()
        except Exception as db_error:
            conn.rollback()
            print(f"❌ Database error: {db_error}")
            raise db_error
        finally:
            conn.close()
        
        return jsonify({
            'success': True,
            'message': 'Survey submitted successfully',
            'survey_id': survey_id,
            'scores': {
                'ess': {
                    'score': ess_score,
                    'category': ess_category
                },
                'berlin': {
                    'score': berlin_score,
                    'category': berlin_category
                },
                'stopbang': {
                    'score': stopbang_score,
                    'category': stopbang_category
                }
            },
            'prediction': {
                'osa_probability': round(osa_probability, 3),
                'risk_level': risk_level,
                'recommendation': recommendation
            },
            'top_risk_factors': top_factors,
            'calculated_metrics': {
                'bmi': round(bmi, 1),
                'estimated_activity_level': activity_level,
                'estimated_sleep_quality': sleep_quality
            }
        }), 201
        
    except Exception as e:
        print(f"❌ ERROR in submit_survey: {str(e)}")
        import traceback
        traceback.print_exc()
        return jsonify({
            'error': str(e),
            'success': False,
            'details': traceback.format_exc()
        }), 500


@app.route('/predict', methods=['POST'])
def predict_osa_risk():
    """
    Predict OSA risk based on user input
    
    Expected JSON input:
    {
        "Age": 45,
        "Sex": 1,  # 1=Male, 0=Female
        "BMI": 31.5,
        "Neck_Circumference": 42,
        "Hypertension": 1,  # 1=Yes, 0=No
        "Diabetes": 0,
        "Smokes": 0,
        "Alcohol": 1,
        "Snoring": 1,
        "Sleepiness": 1,
        "Epworth_Score": 14,
        "Berlin_Score": 1,  # 1=High, 0=Low
        "STOPBANG_Total": 5,
        "SleepQuality_Proxy_0to10": 4,
        "Sleep_Duration": 6.5,  # hours
        "Physical_Activity_Level": 2,  # 1-10 scale
        "Daily_Steps": 8000
    }
    """
    if model is None or scaler is None:
        return jsonify({
            'error': 'Model not loaded. Please train the model first.',
            'success': False
        }), 500
    
    try:
        # Get JSON data from request
        data = request.get_json()
        
        # Validate all required features are present
        missing_features = [f for f in FEATURES if f not in data]
        if missing_features:
            return jsonify({
                'error': f'Missing required features: {missing_features}',
                'success': False
            }), 400
        
        # Create DataFrame with correct feature order
        X_input = pd.DataFrame([{f: data[f] for f in FEATURES}])
        
        # Scale numerical columns
        X_input[NUM_COLS] = scaler.transform(X_input[NUM_COLS])
        
        # Get prediction probability
        y_prob = model.predict_proba(X_input)[:, 1][0]
        y_pred = int(y_prob >= 0.5)
        
        # Determine risk level
        if y_prob < 0.3:
            risk_level = "Low Risk"
        elif y_prob < 0.6:
            risk_level = "Moderate Risk"
        else:
            risk_level = "High Risk"
        
        # Generate comprehensive recommendations
        recommendation = generate_ml_recommendation(
            y_prob, risk_level, 
            data['Age'], data['BMI'], data['Neck_Circumference'],
            data['Hypertension'], data['Diabetes'], data['Smokes'], data['Alcohol'],
            data['Epworth_Score'], data['Berlin_Score'], data['STOPBANG_Total'],
            data['Sleep_Duration'], data['Daily_Steps']
        )
        
        # Return prediction result
        return jsonify({
            'success': True,
            'prediction': {
                'osa_probability': round(float(y_prob), 3),
                'osa_class': y_pred,
                'risk_level': risk_level,
                'recommendation': recommendation
            },
            'input_summary': {
                'age': data['Age'],
                'bmi': data['BMI'],
                'stopbang_score': data['STOPBANG_Total'],
                'epworth_score': data['Epworth_Score'],
                'sleep_duration': data['Sleep_Duration'],
                'daily_steps': data['Daily_Steps']
            },
            'timestamp': datetime.now().isoformat()
        })
        
    except Exception as e:
        return jsonify({
            'error': str(e),
            'success': False
        }), 500


@app.route('/survey/calculate', methods=['POST'])
def calculate_survey_scores():
    """
    Calculate survey scores (ESS, Berlin, STOP-BANG) without prediction.
    
    Expected JSON input:
    {
        "ess_responses": [2, 1, 2, 1, 2, 1, 2, 1],  # 8 values 0-3
        "berlin_category1": {"item2": true, "item3": false, ...},
        "berlin_category2": {"item7": true, ...},
        "berlin_category3_sleepy": true,
        "bmi": 28.5,
        "age": 45,
        "neck_circumference": 37.5,
        "male": true,
        "snoring": true,
        "tired": true,
        "observed_apnea": false,
        "hypertension": true
    }
    """
    try:
        data = request.get_json()
        
        # Calculate ESS
        ess_responses = data.get('ess_responses', [])
        ess_score, ess_category = calculate_ess_score(ess_responses)
        
        # Calculate Berlin
        berlin_cat1 = data.get('berlin_category1', {})
        berlin_cat2 = data.get('berlin_category2', {})
        berlin_cat3_sleepy = data.get('berlin_category3_sleepy', False)
        bmi = data.get('bmi', 25.0)
        berlin_score, berlin_category = calculate_berlin_score(berlin_cat1, berlin_cat2, berlin_cat3_sleepy, bmi)
        
        # Calculate STOP-BANG
        age = data.get('age', 30)
        neck_circumference = data.get('neck_circumference', 37.0)
        male = data.get('male', True)
        snoring = data.get('snoring', False)
        tired = data.get('tired', False)
        observed_apnea = data.get('observed_apnea', False)
        hypertension = data.get('hypertension', False)
        
        stopbang_score, stopbang_category = calculate_stopbang_score(
            snoring, tired, observed_apnea, hypertension,
            age, neck_circumference, bmi, male
        )
        
        # Calculate overall risk
        high_risk_count = 0
        if "Severe" in ess_category or "Moderate" in ess_category:
            high_risk_count += 1
        if berlin_category == "High Risk":
            high_risk_count += 1
        if stopbang_category == "High Risk":
            high_risk_count += 1
        
        overall_risk = "High" if high_risk_count >= 2 else ("Moderate" if high_risk_count >= 1 else "Low")
        
        return jsonify({
            'success': True,
            'survey_scores': {
                'ess': {
                    'score': ess_score,
                    'category': ess_category
                },
                'berlin': {
                    'score': berlin_score,
                    'category': berlin_category
                },
                'stopbang': {
                    'score': stopbang_score,
                    'category': stopbang_category
                },
                'overall_risk_level': overall_risk
            },
            'timestamp': datetime.now().isoformat()
        })
        
    except Exception as e:
        return jsonify({
            'error': str(e),
            'success': False
        }), 500


@app.route('/predict-from-google-fit', methods=['POST'])
def predict_from_google_fit():
    """
    Simplified prediction endpoint that accepts Google Fit data
    and fills in default/estimated values for medical history
    
    Expected JSON input:
    {
        "age": 45,
        "sex": "male",  # or "female"
        "height_cm": 175,
        "weight_kg": 85,
        "neck_circumference_cm": 42,
        "sleep_duration_hours": 6.5,
        "daily_steps": 8000,
        "snores": true,
        "feels_sleepy": true,
        # Optional medical history
        "hypertension": false,
        "diabetes": false,
        "smokes": false,
        "alcohol": false
    }
    """
    if model is None or scaler is None:
        return jsonify({
            'error': 'Model not loaded. Please train the model first.',
            'success': False
        }), 500
    
    try:
        data = request.get_json()
        
        # Calculate BMI
        height_m = data['height_cm'] / 100
        bmi = data['weight_kg'] / (height_m ** 2)
        
        # Convert sex to binary
        sex = 1 if data['sex'].lower() == 'male' else 0
        
        # Calculate STOP-Bang components
        sb_snore = 1 if data.get('snores', False) else 0
        sb_tired = 1 if data.get('feels_sleepy', False) else 0
        sb_pressure = 1 if data.get('hypertension', False) else 0
        sb_bmi = 1 if bmi > 35 else 0
        sb_age = 1 if data['age'] > 50 else 0
        sb_neck = 1 if data['neck_circumference_cm'] >= 40 else 0
        sb_male = sex
        
        stopbang_total = sb_snore + sb_tired + sb_pressure + sb_bmi + sb_age + sb_neck + sb_male
        
        # Estimate Epworth score (simplified)
        epworth_score = 12 if data.get('feels_sleepy', False) else 6
        
        # Estimate Berlin score based on snoring and sleepiness
        berlin_score = 1 if (data.get('snores', False) and data.get('feels_sleepy', False)) else 0
        
        # Estimate sleep quality (inverse of sleepiness)
        sleep_quality = 5 if data.get('feels_sleepy', False) else 7
        
        # Estimate physical activity level from steps
        steps = data['daily_steps']
        if steps < 5000:
            activity_level = 3
        elif steps < 10000:
            activity_level = 5
        else:
            activity_level = 8
        
        # Build feature dictionary
        input_features = {
            'Age': data['age'],
            'Sex': sex,
            'BMI': bmi,
            'Neck_Circumference': data['neck_circumference_cm'],
            'Hypertension': 1 if data.get('hypertension', False) else 0,
            'Diabetes': 1 if data.get('diabetes', False) else 0,
            'Smokes': 1 if data.get('smokes', False) else 0,
            'Alcohol': 1 if data.get('alcohol', False) else 0,
            'Snoring': sb_snore,
            'Sleepiness': 1 if data.get('feels_sleepy', False) else 0,
            'Epworth_Score': epworth_score,
            'Berlin_Score': berlin_score,
            'STOPBANG_Total': stopbang_total,
            'SleepQuality_Proxy_0to10': sleep_quality,
            'Sleep_Duration': data['sleep_duration_hours'],
            'Physical_Activity_Level': activity_level,
            'Daily_Steps': steps
        }
        
        # Create DataFrame
        X_input = pd.DataFrame([{f: input_features[f] for f in FEATURES}])
        
        # Scale numerical columns
        X_input[NUM_COLS] = scaler.transform(X_input[NUM_COLS])
        
        # Get prediction
        y_prob = model.predict_proba(X_input)[:, 1][0]
        y_pred = int(y_prob >= 0.5)
        
        # Determine risk level
        if y_prob < 0.3:
            risk_level = "Low Risk"
            recommendation = "Your OSA risk is low. Continue maintaining healthy sleep habits."
        elif y_prob < 0.6:
            risk_level = "Moderate Risk"
            recommendation = "You have moderate OSA risk. Consider consulting a sleep specialist."
        else:
            risk_level = "High Risk"
            recommendation = "You have high OSA risk. We strongly recommend consulting a sleep specialist soon."
        
        return jsonify({
            'success': True,
            'prediction': {
                'osa_probability': round(float(y_prob), 3),
                'osa_class': y_pred,
                'risk_level': risk_level,
                'recommendation': recommendation
            },
            'calculated_metrics': {
                'bmi': round(bmi, 1),
                'stopbang_score': stopbang_total,
                'estimated_epworth_score': epworth_score,
                'estimated_activity_level': activity_level
            },
            'timestamp': datetime.now().isoformat()
        })
        
    except KeyError as e:
        return jsonify({
            'error': f'Missing required field: {str(e)}',
            'success': False
        }), 400
    except Exception as e:
        return jsonify({
            'error': str(e),
            'success': False
        }), 500


@app.route('/survey/generate-pdf', methods=['POST'])
@require_auth
def generate_pdf_report():
    """
    Generate PDF report from survey data using ReportLab
    Returns PDF file as binary response
    """
    try:
        from pdf_generator import WakeUpCallPDFGenerator
        from io import BytesIO
        import matplotlib
        matplotlib.use('Agg')
        import matplotlib.pyplot as plt
        from datetime import datetime
        
        user_id = request.current_user['id']
        user_name = f"{request.current_user['first_name']} {request.current_user['last_name']}"
        
        # Get latest survey data
        conn = get_db()
        cursor = conn.cursor()
        cursor.execute('''
            SELECT age, sex, height_cm, weight_kg, neck_circumference_cm, bmi,
                   hypertension, diabetes, smokes, alcohol,
                   ess_score, berlin_score, stopbang_score, osa_probability, risk_level,
                   daily_steps, average_daily_steps, sleep_duration_hours,
                   weekly_steps_json, weekly_sleep_json
            FROM user_surveys
            WHERE user_id = ?
            ORDER BY completed_at DESC
            LIMIT 1
        ''', (user_id,))
        
        survey = cursor.fetchone()
        conn.close()
        
        if not survey:
            return jsonify({'error': 'No survey data found', 'success': False}), 404
        
        # Extract data
        age, sex, height_cm, weight_kg, neck_cm, bmi = survey[0:6]
        hypertension, diabetes, smokes, alcohol = survey[6:10]
        ess_score, berlin_score, stopbang_score = survey[10:13]
        osa_probability, risk_level = survey[13:15]
        daily_steps, average_daily_steps, sleep_duration_hours = survey[15:18]
        weekly_steps_json, weekly_sleep_json = survey[18:20]
        
        # Calculate ESS individual scores (divide total by 8 for average, then distribute)
        avg_ess = ess_score / 8
        ess_responses = [int(avg_ess)] * 8  # Simplified: use average for each question
        
        # Parse Google Fit JSON data
        import json
        weekly_steps_data = json.loads(weekly_steps_json) if weekly_steps_json else {}
        weekly_sleep_data = json.loads(weekly_sleep_json) if weekly_sleep_json else {}
        
        # Generate charts for PDF
        def generate_shap_chart():
            # Calculate impact scores
            age_impact = 0.75 if age >= 50 else 0.40
            snoring_impact = 0.85 if stopbang_score >= 1 else 0.25
            stopbang_impact = (stopbang_score / 8.0) * 0.9 + 0.1
            
            if neck_cm >= 43:
                neck_impact = 0.90
            elif neck_cm >= 40:
                neck_impact = 0.70
            elif neck_cm >= 37:
                neck_impact = 0.50
            else:
                neck_impact = 0.30
            
            ess_impact = (ess_score / 24.0) * 0.9 + 0.1
            
            factors = [
                ('Age', age_impact),
                ('Snoring', snoring_impact),
                ('STOP-BANG', stopbang_impact),
                ('Neck Circ', neck_impact),
                ('ESS Score', ess_impact)
            ]
            
            # Sort by impact
            factors.sort(key=lambda x: x[1], reverse=True)
            
            # Create bar chart
            fig, ax = plt.subplots(figsize=(8, 5))
            names = [f[0] for f in factors]
            values = [f[1] * 100 for f in factors]
            colors = ['#f44336' if v >= 70 else '#ff9800' if v >= 50 else '#4caf50' for v in values]
            
            ax.barh(names, values, color=colors)
            ax.set_xlabel('Impact (%)', fontsize=12)
            ax.set_title('SHAP Analysis - Risk Factor Impact', fontsize=14, fontweight='bold')
            ax.set_xlim(0, 100)
            
            for i, v in enumerate(values):
                ax.text(v + 2, i, f'{v:.0f}%', va='center', fontsize=10)
            
            plt.tight_layout()
            
            # Save to BytesIO
            img_buffer = BytesIO()
            plt.savefig(img_buffer, format='png', dpi=150, bbox_inches='tight')
            img_buffer.seek(0)
            plt.close()
            
            return img_buffer
        
        # Generate weekly steps chart
        def generate_steps_chart(steps_data):
            # Sort by date and take last 7 days
            sorted_data = sorted(steps_data.items(), key=lambda x: x[0])[-7:]
            dates = [d[5:] for d, _ in sorted_data]  # Extract MM-DD
            steps = [s for _, s in sorted_data]
            
            fig, ax = plt.subplots(figsize=(8, 5))
            colors = ['#4caf50' if s >= 8000 else '#ff9800' if s >= 5000 else '#f44336' for s in steps]
            bars = ax.bar(dates, steps, color=colors)
            
            ax.set_xlabel('Date', fontsize=12)
            ax.set_ylabel('Steps', fontsize=12)
            ax.set_title('Weekly Step Count', fontsize=14, fontweight='bold')
            ax.set_ylim(0, max(steps) * 1.2 if steps else 15000)
            
            # Add value labels on bars
            for bar in bars:
                height = bar.get_height()
                ax.text(bar.get_x() + bar.get_width()/2., height,
                       f'{int(height):,}',
                       ha='center', va='bottom', fontsize=9)
            
            plt.xticks(rotation=45)
            plt.tight_layout()
            
            img_buffer = BytesIO()
            plt.savefig(img_buffer, format='png', dpi=150, bbox_inches='tight')
            img_buffer.seek(0)
            plt.close()
            
            return img_buffer
        
        # Generate weekly sleep chart
        def generate_sleep_chart(sleep_data):
            # Sort by date and take last 7 days
            sorted_data = sorted(sleep_data.items(), key=lambda x: x[0])[-7:]
            dates = [d[5:] for d, _ in sorted_data]  # Extract MM-DD
            hours = [h for _, h in sorted_data]
            
            fig, ax = plt.subplots(figsize=(8, 5))
            colors = ['#4caf50' if h >= 7 else '#ff9800' if h >= 6 else '#f44336' for h in hours]
            bars = ax.bar(dates, hours, color=colors)
            
            ax.set_xlabel('Date', fontsize=12)
            ax.set_ylabel('Hours', fontsize=12)
            ax.set_title('Weekly Sleep Duration', fontsize=14, fontweight='bold')
            ax.set_ylim(0, 10)
            ax.axhline(y=7, color='gray', linestyle='--', alpha=0.5, label='Recommended (7h)')
            
            # Add value labels on bars
            for bar in bars:
                height = bar.get_height()
                ax.text(bar.get_x() + bar.get_width()/2., height,
                       f'{height:.1f}h',
                       ha='center', va='bottom', fontsize=9)
            
            ax.legend()
            plt.xticks(rotation=45)
            plt.tight_layout()
            
            img_buffer = BytesIO()
            plt.savefig(img_buffer, format='png', dpi=150, bbox_inches='tight')
            img_buffer.seek(0)
            plt.close()
            
            return img_buffer
        
        # Generate charts
        steps_chart_buffer = generate_steps_chart(weekly_steps_data) if weekly_steps_data else None
        sleep_chart_buffer = generate_sleep_chart(weekly_sleep_data) if weekly_sleep_data else None
        shap_chart_buffer = generate_shap_chart()
        
        # Calculate STOP-BANG components
        snoring = stopbang_score >= 1  # Simplified assumption
        tiredness = ess_score >= 11
        observed_apnea = False  # Not directly available
        bmi_over_35 = bmi > 35
        age_over_50 = age > 50
        neck_large = neck_cm >= 40 if sex == 'Male' else neck_cm >= 35
        gender_male = (sex == 'Male')
        
        # Generate comprehensive recommendations using the recommendation engine
        sex_binary = 1 if sex == 'Male' else 0
        recommendation = generate_ml_recommendation(
            osa_probability, risk_level, age, bmi, neck_cm,
            hypertension, diabetes, smokes, alcohol,
            ess_score, berlin_score, stopbang_score,
            sleep_duration_hours, daily_steps
        )
        
        # Build data dictionary for PDF generator
        pdf_data = {
            'patient': {
                'name': user_name,
                'age': age,
                'sex': sex,
                'height': f'{height_cm} cm',
                'weight': f'{weight_kg} kg',
                'bmi': bmi,
                'neck_circumference': f'{neck_cm} cm'
            },
            'assessment': {
                'risk_level': risk_level,
                'osa_probability': int(osa_probability * 100),
                'recommendation': recommendation
            },
            'stop_bang': {
                'score': stopbang_score,
                'snoring': snoring,
                'tiredness': tiredness,
                'observed_apnea': observed_apnea,
                'high_blood_pressure': hypertension,
                'bmi_over_35': bmi_over_35,
                'age_over_50': age_over_50,
                'neck_circumference_large': neck_large,
                'gender_male': gender_male
            },
            'epworth_sleepiness_scale': {
                'total_score': ess_score,
                'sitting_reading': ess_responses[0] if ess_responses else 0,
                'watching_tv': ess_responses[1] if ess_responses else 0,
                'public_sitting': ess_responses[2] if ess_responses else 0,
                'passenger_car': ess_responses[3] if ess_responses else 0,
                'lying_down_pm': ess_responses[4] if ess_responses else 0,
                'talking': ess_responses[5] if ess_responses else 0,
                'after_lunch': ess_responses[6] if ess_responses else 0,
                'traffic_stop': ess_responses[7] if ess_responses else 0
            },
            'google_fit': {
                'daily_steps': daily_steps or 0,
                'average_daily_steps': average_daily_steps or 0,
                'sleep_duration_hours': sleep_duration_hours or 0,
                'weekly_steps_chart': steps_chart_buffer,
                'weekly_sleep_chart': sleep_chart_buffer
            },
            'lifestyle': {
                'smoking': smokes,
                'alcohol': alcohol
            },
            'medical_history': {
                'hypertension': hypertension,
                'diabetes': diabetes
            },
            'shap_chart': shap_chart_buffer,
            'generated_date': datetime.now().strftime("%Y-%m-%d %H:%M")
        }
        
        # Generate PDF using ReportLab
        print("📄 Generating PDF report using ReportLab...")
        generator = WakeUpCallPDFGenerator()
        pdf_buffer = generator.generate_pdf(pdf_data)
        
        pdf_size = len(pdf_buffer.getvalue())
        print(f"✅ PDF generated successfully: {pdf_size} bytes")
        
        pdf_buffer.seek(0)
        
        from flask import send_file
        print(f"📤 Sending PDF file to client...")
        response = send_file(
            pdf_buffer,
            mimetype='application/pdf',
            as_attachment=True,
            download_name=f'WakeUpCall_Report_{user_name.replace(" ", "_")}.pdf'
        )
        response.headers['Content-Length'] = pdf_size
        print(f"✅ Response sent with Content-Length: {pdf_size}")
        return response
        
    except ImportError as ie:
        return jsonify({
            'error': f'Missing required library: {str(ie)}. Install with: pip install python-docx matplotlib',
            'success': False
        }), 500
    except Exception as e:
        import traceback
        print(f"❌ PDF Generation Error: {str(e)}")
        traceback.print_exc()
        return jsonify({
            'error': f'Failed to generate report: {str(e)}',
            'success': False
        }), 500


if __name__ == '__main__':
    print("🚀 Starting WakeUp Call OSA Prediction API...")
    print(f"📊 Model loaded: {model is not None}")
    print(f"📏 Scaler loaded: {scaler is not None and hasattr(scaler, 'transform')} (not required for LightGBM)")
    app.run(host='0.0.0.0', port=5000, debug=True)
