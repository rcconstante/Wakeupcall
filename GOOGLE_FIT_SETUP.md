# Google Fit Integration Setup Guide

## Overview
This guide will help you set up Google Fit integration for the WakeUp Call app to access user's step count, sleep data, and activity information.

---

## Prerequisites
- Google Cloud Console account
- Android Studio installed
- Physical Android device or emulator with Google Play Services

---

## Part 1: Google Cloud Console Setup

### Step 1: Create Google Cloud Project

1. **Go to Google Cloud Console**
   - Visit: https://console.cloud.google.com/
   - Sign in with your Google account

2. **Create New Project**
   - Click "Select a project" dropdown at the top
   - Click "NEW PROJECT"
   - Enter project name: `WakeUpCallApp` (or your preferred name)
   - Click "CREATE"

### Step 2: Enable Google Fit API

1. **Navigate to APIs & Services**
   - From the left menu, go to: `APIs & Services` → `Library`

2. **Search and Enable Fitness API**
   - Search for: "Fitness API"
   - Click on "Fitness API"
   - Click "ENABLE"

3. **Search and Enable Google Fit API**
   - Search for: "Google Fit API"  
   - Click on "Google Fit API"
   - Click "ENABLE"

### Step 3: Configure OAuth Consent Screen

1. **Go to OAuth Consent Screen**
   - From left menu: `APIs & Services` → `OAuth consent screen`

2. **Select User Type**
   - Choose: **External** (for testing with any Google account)
   - Click "CREATE"

3. **Fill App Information**
   ```
   App name: WakeUp Call
   User support email: your-email@gmail.com
   App logo: (Optional) Upload your app icon
   
   App domain (Optional for testing):
   - Application home page: (leave blank for testing)
   - Application privacy policy link: (leave blank for testing)
   - Application terms of service link: (leave blank for testing)
   
   Authorized domains: (leave blank for testing)
   
   Developer contact information: your-email@gmail.com
   ```

4. **Click "SAVE AND CONTINUE"**

5. **Scopes Section**
   - Click "ADD OR REMOVE SCOPES"
   - Search and add these scopes:
     - `fitness.activity.read`
     - `fitness.sleep.read`
     - `fitness.body.read`
   - Click "UPDATE"
   - Click "SAVE AND CONTINUE"

6. **Test Users** (for external app during development)
   - Click "ADD USERS"
   - Enter your test Google account email(s)
   - Click "ADD"
   - Click "SAVE AND CONTINUE"

7. **Summary**
   - Review and click "BACK TO DASHBOARD"

### Step 4: Create OAuth 2.0 Client ID

1. **Go to Credentials**
   - From left menu: `APIs & Services` → `Credentials`

2. **Create OAuth Client ID**
   - Click "+ CREATE CREDENTIALS"
   - Select "OAuth client ID"

3. **Application Type**
   - Select: **Android**

4. **Configure Android App**
   ```
   Name: WakeUp Call Android
   Package name: com.example.wakeupcallapp.sleepapp
   SHA-1 certificate fingerprint: [See below how to get this]
   ```

### Step 5: Get SHA-1 Certificate Fingerprint

#### For Debug Build (Development):

**Windows PowerShell:**
```powershell
cd $env:USERPROFILE\.android
keytool -list -v -keystore debug.keystore -alias androiddebugkey -storepass android -keypass android
```

**macOS/Linux Terminal:**
```bash
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
```

**Copy the SHA-1 value** (looks like: `A1:B2:C3:D4:E5:F6:...`)

#### For Release Build (Production):

```powershell
keytool -list -v -keystore your-release-key.keystore -alias your-key-alias
```
(Enter your keystore password when prompted)

5. **Paste SHA-1 into Google Cloud Console**
   - Paste the SHA-1 fingerprint
   - Click "CREATE"

6. **Done!**
   - You'll see your Client ID created
   - **Save the Client ID** (you don't need to add it to your app, Google Play Services handles it automatically)

---

## Part 2: Android App Configuration

### Step 1: Verify build.gradle.kts Dependencies

Open `app/build.gradle.kts` and verify these dependencies exist:

```kotlin
dependencies {
    // Google Fit + Auth
    implementation("com.google.android.gms:play-services-fitness:21.1.0")
    implementation("com.google.android.gms:play-services-auth:21.1.0")
    
    // Coroutines (for async Google Fit calls)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.1")
}
```

✅ **Already configured in your project!**

### Step 2: Verify AndroidManifest.xml Permissions

Open `app/src/main/AndroidManifest.xml` and verify:

```xml
<!-- Required permissions -->
<uses-permission android:name="android.permission.ACTIVITY_RECOGNITION" />
<uses-permission android:name="android.permission.INTERNET" />
```

✅ **Already configured in your project!**

### Step 3: Sync and Build

1. **Sync Gradle**
   - In Android Studio, click "Sync Now" if prompted
   - Or go to: `File` → `Sync Project with Gradle Files`

2. **Build the App**
   - Click `Build` → `Rebuild Project`

---

## Part 3: Testing Google Fit Integration

### Step 1: Install on Physical Device (Recommended)

**Why Physical Device?**
- Emulators may not have Google Fit or accurate sensors
- Real-world testing provides actual step/sleep data

**Installation Steps:**
1. Enable Developer Mode on your Android phone:
   - Go to `Settings` → `About Phone`
   - Tap "Build Number" 7 times
   
2. Enable USB Debugging:
   - Go to `Settings` → `Developer Options`
   - Enable "USB Debugging"

3. Connect phone to computer via USB

4. In Android Studio:
   - Select your device from the device dropdown
   - Click "Run" (green play button)

### Step 2: Test Permission Flow

1. **Sign Up / Login**
   - Create a new account or login

2. **Data Consent Screen**
   - You'll see "Connect to Google Fit" button
   - Click "Grant Permission"

3. **Google Sign-In Dialog**
   - Google account picker will appear
   - **Select the test account you added in Google Cloud Console**
   - If you see "App isn't verified" warning:
     - Click "Advanced"
     - Click "Go to WakeUp Call (unsafe)" - This is normal for apps in development

4. **Permission Dialog**
   - Google Fit will ask for permissions:
     - Read your stored physical activity data
     - Read info about your physical activity
     - Read sleep data
   - Click "ALLOW"

5. **Success!**
   - App will proceed to Demographics screen
   - Google Fit is now connected

### Step 3: Verify Data Display

1. **Navigate to Profile**
   - After completing survey, go to Profile screen
   - You should see:
     - ✅ "Weekly Sleep Pattern" card with bar chart
     - ✅ "Daily Steps" card with bar chart
     - ✅ Real data from Google Fit (if available)

2. **Navigate to Data Sources**
   - From Dashboard sidebar → "Data Sources"
   - Should show "Google Fit" as "Connected"
   - Shows today's step count
   - Can click "Disconnect" to revoke permissions

3. **Check Dashboard**
   - Survey data should now include real Google Fit metrics
   - More accurate OSA predictions based on actual activity/sleep

---

## Part 4: API Key Configuration (Optional - For Advanced Features)

### If You Need API Key for Server-Side Calls:

1. **Go to Credentials in Google Cloud Console**
   - `APIs & Services` → `Credentials`

2. **Create API Key**
   - Click "+ CREATE CREDENTIALS"
   - Select "API Key"
   - Copy the generated key

3. **Restrict the API Key** (Recommended)
   - Click on the API key name
   - Under "API restrictions":
     - Select "Restrict key"
     - Check "Fitness API" and "Google Fit API"
   - Under "Application restrictions":
     - Select "Android apps"
     - Add your package name and SHA-1
   - Click "SAVE"

4. **Add to Your Backend** (if needed for server-side access)
   ```python
   # backend/.env
   GOOGLE_FIT_API_KEY=your-api-key-here
   ```

   **Note:** For client-side Android app using OAuth, you don't need an API key - Google Play Services handles authentication automatically.

---

## Part 5: Troubleshooting

### Issue: "Sign in failed" or "Permission denied"

**Solution 1: Check OAuth Consent Screen**
- Ensure your test Google account is added to "Test users" in OAuth consent screen

**Solution 2: Verify SHA-1**
- Make sure the SHA-1 certificate fingerprint matches your debug keystore
- Re-run the keytool command to verify

**Solution 3: Wait for Propagation**
- After making changes in Google Cloud Console, wait 5-10 minutes
- Clear app data: `Settings` → `Apps` → `WakeUp Call` → `Storage` → `Clear Data`
- Reinstall the app

### Issue: "App not verified" warning

**Solution:**
- This is normal for apps in development
- Click "Advanced" → "Go to WakeUp Call (unsafe)"
- For production release, submit for OAuth verification (takes days)

### Issue: No data showing in charts

**Solution 1: Check Google Fit App**
- Install Google Fit app on your device
- Ensure it has permissions
- Walk around to generate step data
- Sleep tracking requires overnight data

**Solution 2: Sync Data**
- Open Google Fit app
- Tap your profile
- Tap "Settings" → "Sync now"
- Return to WakeUp Call app
- Navigate away and back to Profile to refresh

### Issue: "API not enabled" error

**Solution:**
- Go to Google Cloud Console
- Navigate to `APIs & Services` → `Library`
- Search and enable both:
  - "Fitness API"
  - "Google Fit API"

---



### Data Types Available:

- **Steps:** Daily step count and historical data
- **Sleep:** Sleep duration and segments (light, deep, REM)
- **Activity:** Walking, running, cycling, etc.
- **Distance:** Distance traveled
- **Calories:** Calories burned
- **Heart Rate:** (if device has sensor)

---

