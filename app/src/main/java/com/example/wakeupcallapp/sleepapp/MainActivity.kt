package com.example.wakeupcallapp.sleepapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType

class MainActivity : ComponentActivity() {
    
    // Fitness options for Google Fit permissions
    val fitnessOptions = FitnessOptions.builder()
        .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.TYPE_SLEEP_SEGMENT, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.TYPE_ACTIVITY_SEGMENT, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.TYPE_DISTANCE_DELTA, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.TYPE_CALORIES_EXPENDED, FitnessOptions.ACCESS_READ)
        .build()
    
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        Log.d(TAG, "📨 onActivityResult: requestCode=$requestCode, resultCode=$resultCode")
        
        if (requestCode == GOOGLE_FIT_PERMISSIONS_REQUEST_CODE) {
            Log.d(TAG, "🎯 Google Fit permission result received")
            
            // Check if permissions were granted
            val account = GoogleSignIn.getAccountForExtension(this, fitnessOptions)
            
            Log.d(TAG, "📋 Account after permission request: ${account?.email ?: "null"}")
            
            val permissionGranted = if (account != null) {
                val hasPerms = GoogleSignIn.hasPermissions(account, fitnessOptions)
                Log.d(TAG, "📋 GoogleSignIn.hasPermissions() = $hasPerms")
                hasPerms
            } else {
                Log.e(TAG, "❌ No account found after permission request!")
                false
            }
            
            Log.d(TAG, if (permissionGranted) "✅ Fitness permissions GRANTED" else "❌ Fitness permissions DENIED or CANCELLED")
            
            // Invoke callback
            val callback = onGoogleFitPermissionResult
            if (callback != null) {
                Log.d(TAG, "🔔 Invoking permission callback with result: $permissionGranted")
                callback.invoke(permissionGranted)
            } else {
                Log.e(TAG, "❌ No callback registered!")
            }
        } else {
            Log.d(TAG, "⚠️ Ignoring result for requestCode=$requestCode (not Google Fit)")
        }
    }
    
    companion object {
        private const val TAG = "MainActivity"
        private const val GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 1001
        // Callback to notify when Google Fit permissions are granted/denied
        var onGoogleFitPermissionResult: ((Boolean) -> Unit)? = null
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Surface(color = MaterialTheme.colorScheme.background) {
                WakeUpCallApp()
            }
        }
    }
    

}

