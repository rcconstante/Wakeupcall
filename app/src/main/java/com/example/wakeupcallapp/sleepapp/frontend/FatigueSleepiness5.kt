package com.example.wakeupcallapp.sleepapp

import android.os.Bundle
import com.example.wakeupcallapp.sleepapp.R
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wakeupcallapp.sleepapp.viewmodel.SurveyViewModel
import com.example.wakeupcallapp.sleepapp.viewmodel.AuthViewModel
import com.example.wakeupcallapp.sleepapp.utils.ESSMapper
import androidx.compose.ui.platform.LocalContext

class FatigueSleepiness5 : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FatigueSleepiness5ScreenContent()
        }
    }
}

@Composable
fun FatigueSleepiness5ScreenContent(
    surveyViewModel: SurveyViewModel = viewModel(viewModelStoreOwner = LocalContext.current as ComponentActivity),
    authViewModel: AuthViewModel = viewModel(viewModelStoreOwner = LocalContext.current as ComponentActivity),
    healthConnectViewModel: com.example.wakeupcallapp.sleepapp.viewmodel.HealthConnectViewModel = viewModel(viewModelStoreOwner = LocalContext.current as ComponentActivity),
    onNext: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    var carStoppedChoice by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var hasSubmittedFromThisScreen by remember { mutableStateOf(false) }
    
    val isSubmitting by surveyViewModel.isSubmitting.collectAsState()
    val submissionResult by surveyViewModel.submissionResult.collectAsState()
    val errorMessage by surveyViewModel.errorMessage.collectAsState()
    val authToken by authViewModel.authToken.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        // Background Image
        Image(
            painter = painterResource(id = R.drawable.background),
            contentDescription = "Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Scrollable content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(36.dp))

            // Card container
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0x50FFFFFF))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Title
                    Text(
                        text = "Fatigue & Sleepiness",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Question
                    Text(
                        text = "Sitting in a car, stopped for a few minutes due to traffic",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Radio options
                    Fatigue5RadioOption("No chance of dozing", carStoppedChoice) { carStoppedChoice = it }
                    Spacer(modifier = Modifier.height(8.dp))
                    Fatigue5RadioOption("Slight chance of dozing", carStoppedChoice) { carStoppedChoice = it }
                    Spacer(modifier = Modifier.height(8.dp))
                    Fatigue5RadioOption("Moderate chance of dozing", carStoppedChoice) { carStoppedChoice = it }
                    Spacer(modifier = Modifier.height(8.dp))
                    Fatigue5RadioOption("High chance of dozing", carStoppedChoice) { carStoppedChoice = it }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Error message
                    if (showError) {
                        Text(
                            text = "Please answer the question before submitting",
                            fontSize = 14.sp,
                            color = Color(0xFFFF6B6B),
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Error message from submission
                    errorMessage?.let { error ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFF6B6B).copy(alpha = 0.2f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "⚠️ Backend Connection Failed",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = error,
                                    fontSize = 14.sp,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "✅ Steps to fix:",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "1. Start backend: cd backend && python app.py",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                                Text(
                                    text = "2. Verify server: http://192.168.18.3:5000/",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                                Text(
                                    text = "3. Check your PC's firewall settings",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Success message - only navigate if we submitted from THIS screen
                    if (hasSubmittedFromThisScreen) {
                        submissionResult?.let { result ->
                            if (result.success) {
                                LaunchedEffect(result) {
                                    android.util.Log.d("FatigueSleepiness5", "✅ Survey submitted successfully! Navigating to dashboard...")
                                    kotlinx.coroutines.delay(1000)
                                    onNext()
                                }
                            }
                        }
                    }

                    // Submit Button
                    Button(
                        onClick = {
                            if (carStoppedChoice.isEmpty()) {
                                showError = true
                            } else {
                                showError = false
                                android.util.Log.d("FatigueSleepiness5", "🚀 Submit button clicked!")
                                android.util.Log.d("FatigueSleepiness5", "🚀 Answer: $carStoppedChoice = ${ESSMapper.mapESSDozing(carStoppedChoice)}")
                                
                                // Save ESS Q8 (final question)
                                surveyViewModel.updateESSResponse(7, ESSMapper.mapESSDozing(carStoppedChoice))
                                
                                // Submit survey
                                authToken?.let { token ->
                                    android.util.Log.d("FatigueSleepiness5", "🚀 Submitting survey with token: ${token.take(20)}...")
                                    hasSubmittedFromThisScreen = true
                                    surveyViewModel.submitSurvey(token, healthConnectViewModel)
                                } ?: android.util.Log.e("FatigueSleepiness5", "❌ No auth token available!")
                            }
                        },
                        enabled = !isSubmitting,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0x40FFFFFF)),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Text(
                                text = "Submit",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}

@Composable
fun Fatigue5RadioOption(
    text: String,
    selectedValue: String,
    onSelect: (String) -> Unit
) {
    val selected = selectedValue == text
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect(text) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(
                    color = if (selected) Color.White else Color.Transparent,
                    shape = CircleShape
                )
                .border(2.dp, Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (selected) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(Color(0xFF6B8DD6), CircleShape)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = text,
            fontSize = 16.sp,
            color = Color.White,
            fontWeight = FontWeight.Normal
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewFatigueSleepiness5Screen() {
    FatigueSleepiness5ScreenContent()
}
