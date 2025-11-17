# WakeUpCall - Local Development Setup Guide

This guide will help you set up and run the WakeUpCall Sleep Apnea Assessment application locally using VS Code and Android Studio.

## Prerequisites

- **Python 3.8+** - [Download](https://www.python.org/downloads/)
- **Android Studio** - [Download](https://developer.android.com/studio)
- **VS Code** - [Download](https://code.visualstudio.com/)
- **Git** - [Download](https://git-scm.com/)

---

## Part 1: Backend Setup (Flask)

### Step 1: Open Backend in VS Code

```bash
cd d:\Cursor Project\WakeUpCallApp\backend
code .
```

### Step 2: Create Virtual Environment

```bash
python -m venv venv
```

### Step 3: Activate Virtual Environment

**Windows (PowerShell):**
```powershell
.\venv\Scripts\Activate.ps1
```

**Windows (Command Prompt):**
```cmd
venv\Scripts\activate.bat
```

**macOS/Linux:**
```bash
source venv/bin/activate
```

### Step 4: Install Dependencies

```bash
pip install -r requirements.txt
```

### Step 5: Run Backend Server

```bash
python app.py
```

You should see output like:
```
 * Running on http://127.0.0.1:5000
 * Press CTRL+C to quit
```

**Keep this terminal running!** The backend will be available at `http://localhost:5000`

---

## Part 2: Android App Setup

### Step 1: Update Backend URL (If Using Physical Device)

The app is configured for Android Emulator by default (`http://10.0.2.2:5000/`).

**For physical device**, find your computer's IP address:

**Windows (PowerShell):**
```powershell
ipconfig
```
Look for "IPv4 Address" (e.g., `192.168.1.100`)

**macOS/Linux:**
```bash
ifconfig
```

### Step 2: Update URL in Code

Open `RetrofitClient.kt`:
```
app/src/main/java/com/example/wakeupcallapp/sleepapp/data/network/RetrofitClient.kt
```

Update the BASE_URL:
```kotlin
// For Emulator (default):
private const val BASE_URL = "http://10.0.2.2:5000/"

// For Physical Device (replace with your IP):
private const val BASE_URL = "http://192.168.1.100:5000/"
```

### Step 3: Open Project in Android Studio

1. Open Android Studio
2. Click **File → Open**
3. Navigate to `d:\Cursor Project\WakeUpCallApp`
4. Click **OK**
5. Wait for Gradle sync to complete

### Step 4: Build the App

```bash
# In Android Studio, press:
Ctrl + F9  (Windows/Linux)
Cmd + F9   (macOS)
```

Or use menu: **Build → Make Project**

### Step 5: Run the App

**Option A: Android Emulator**
1. Click **AVD Manager** (Android Virtual Device icon)
2. Select or create an emulator
3. Click the play button to start the emulator
4. Once emulator is ready, click **Run → Run 'app'** (or press Shift+F10)

**Option B: Physical Device**
1. Enable **Developer Mode** on your Android device:
   - Go to Settings → About Phone
   - Tap "Build Number" 7 times
   - Go back to Settings → Developer Options
   - Enable "USB Debugging"
2. Connect device via USB
3. Click **Run → Run 'app'** (or press Shift+F10)
4. Select your device from the list

---

## Troubleshooting

### Backend Issues

**Error: `ModuleNotFoundError`**
- Make sure virtual environment is activated
- Run: `pip install -r requirements.txt`

**Error: `Port 5000 already in use`**
- Change port in `app.py`:
  ```python
  if __name__ == '__main__':
      app.run(debug=True, port=5001)  # Change 5000 to 5001
  ```

**Error: `ReportLab not found`**
```bash
pip install reportlab
```

### Android App Issues

**Error: `Connection refused`**
- Make sure backend is running
- Check URL in `RetrofitClient.kt`
- For emulator: use `10.0.2.2:5000`
- For physical device: use your computer's IP

**Error: `Gradle sync failed`**
- Click **File → Sync Now**
- Or wait for automatic sync

**App crashes on startup**
- Check logcat for errors: **View → Tool Windows → Logcat**
- Ensure minimum SDK is 24 or higher

---

## Testing the Setup

### 1. Test Backend

Open browser and go to:
```
http://localhost:5000/health
```

Should return:
```json
{"status": "ok"}
```

### 2. Test Android App

1. Launch app on emulator/device
2. Sign up with test credentials
3. Complete the survey
4. Download the PDF report

---

## Project Structure

```
WakeUpCallApp/
├── backend/                    # Flask API
│   ├── app.py                 # Main Flask app
│   ├── pdf_generator.py       # PDF generation
│   ├── requirements.txt       # Python dependencies
│   └── venv/                  # Virtual environment
│
├── app/                        # Android app
│   ├── src/main/
│   │   ├── java/             # Kotlin source files
│   │   └── res/              # Android resources
│   └── build.gradle.kts      # Android build config
│
└── docs/                       # Documentation
```

---

## Key Technologies

**Backend:**
- Flask 3.1.2
- SQLite Database
- ReportLab (PDF generation)
- Matplotlib (Charts)
- scikit-learn (ML models)

**Android:**
- Kotlin
- Jetpack Compose
- Retrofit 2 (HTTP client)
- OkHttp 4 (HTTP logging)

---

## Environment Variables

Create `.env` file in backend directory (optional):
```
DEBUG=True
DATABASE_URL=sqlite:///wakeupcall.db
LOG_LEVEL=DEBUG
```

---

## Common Commands

### Backend
```bash
# Activate environment
.\venv\Scripts\Activate.ps1

# Install packages
pip install -r requirements.txt

# Run server
python app.py

# Run database migration
python migrate_db.py

# Deactivate environment
deactivate
```

### Android
```bash
# Sync gradle
./gradlew sync

# Build APK
./gradlew build

# Run tests
./gradlew test

# Build and run
./gradlew installDebug
```

---

## Need Help?

- Check backend logs in terminal where `python app.py` is running
- Check Android logs in Logcat (View → Tool Windows → Logcat)
- Verify network connectivity between app and backend
- Ensure all dependencies are installed: `pip list`

---

**Last Updated:** November 17, 2025
