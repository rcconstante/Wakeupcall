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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wakeupcallapp.sleepapp.viewmodel.SurveyViewModel
import androidx.compose.ui.platform.LocalContext

class HealthHistory2 : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HealthHistory2ScreenContent()
        }
    }
}

@Composable
fun HealthHistory2ScreenContent(
    surveyViewModel: SurveyViewModel = viewModel(viewModelStoreOwner = LocalContext.current as ComponentActivity),
    googleFitViewModel: com.example.wakeupcallapp.sleepapp.viewmodel.GoogleFitViewModel = viewModel(viewModelStoreOwner = LocalContext.current as ComponentActivity),
    onNext: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    // Get Google Fit data
    val fitData by googleFitViewModel.fitData.collectAsState()
    val currentDailySteps by surveyViewModel.dailySteps.collectAsState()
    
    // Initialize with Google Fit data or default, then allow user override
    var stepCount by remember(fitData, currentDailySteps) { 
        mutableStateOf(
            when {
                fitData != null && fitData!!.dailySteps > 0 -> fitData!!.dailySteps.toString()
                currentDailySteps > 0 -> currentDailySteps.toString()
                else -> ""
            }
        )
    }
    var activityMinutes by remember { mutableStateOf("") }
    var activityType1 by remember { mutableStateOf("") }
    var activityType2 by remember { mutableStateOf("") }
    var selectedTime by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        // Background Image
        Image(
            painter = painterResource(id = R.drawable.background),
            contentDescription = "Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Scrollable Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp)) // slightly higher start for balance

            // Card Container
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0x50FFFFFF))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    // Title
                    Text(
                        text = "Health & Lifestyle",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Step Count Field
                    Text(
                        text = "Typical daily step-count:",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    HealthTextField(
                        value = stepCount,
                        onValueChange = { stepCount = it },
                        placeholder = "",
                        keyboardType = KeyboardType.Number
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Activity Minutes Field
                    Text(
                        text = "Daily physical activity (minutes):",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    HealthTextField(
                        value = activityMinutes,
                        onValueChange = { activityMinutes = it },
                        placeholder = "",
                        keyboardType = KeyboardType.Number
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Types of Physical Activity
                    Text(
                        text = "Types of physical activity:",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    HealthTextField(
                        value = activityType1,
                        onValueChange = { activityType1 = it },
                        placeholder = "",
                        keyboardType = KeyboardType.Text
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    HealthTextField(
                        value = activityType2,
                        onValueChange = { activityType2 = it },
                        placeholder = "",
                        keyboardType = KeyboardType.Text
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Time of Day Question
                    Text(
                        text = "At what time of day do you usually perform your physical activities?",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White,
                        lineHeight = 22.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    TimeRadioButtonOption("Morning (6 AM - 10 AM)", selectedTime == "Morning") { selectedTime = "Morning" }
                    Spacer(modifier = Modifier.height(8.dp))
                    TimeRadioButtonOption("Afternoon (11 AM - 4 PM)", selectedTime == "Afternoon") { selectedTime = "Afternoon" }
                    Spacer(modifier = Modifier.height(8.dp))
                    TimeRadioButtonOption("Evening (5 PM - 9 PM)", selectedTime == "Evening") { selectedTime = "Evening" }
                    Spacer(modifier = Modifier.height(8.dp))
                    TimeRadioButtonOption("Late Night (10 PM - 12 AM)", selectedTime == "Late Night") { selectedTime = "Late Night" }
                    Spacer(modifier = Modifier.height(8.dp))
                    TimeRadioButtonOption("I don't have a fixed time", selectedTime == "No fixed time") { selectedTime = "No fixed time" }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Error message
                    if (showError) {
                        Text(
                            text = "Please fill in all fields before continuing",
                            fontSize = 14.sp,
                            color = Color(0xFFFF6B6B),
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Navigation Buttons (inside card, properly aligned)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp), // adds breathing room from bottom
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Back Button
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    color = Color(0x40FFFFFF),
                                    shape = CircleShape
                                )
                        ) {
                            Icon(
                                painter = painterResource(android.R.drawable.ic_media_play),
                                contentDescription = "Back",
                                tint = Color.White,
                                modifier = Modifier
                                    .size(20.dp)
                                    .graphicsLayer(rotationZ = 180f)
                            )
                        }

                        // Next Button
                        IconButton(
                            onClick = {
                                // Validate all fields
                                if (stepCount.isEmpty() || activityMinutes.isEmpty() || 
                                    activityType1.isEmpty() || selectedTime.isEmpty()) {
                                    showError = true
                                } else {
                                    showError = false
                                    // Save to ViewModel
                                    val steps = stepCount.toIntOrNull() ?: 0
                                    surveyViewModel.updateDailySteps(steps)
                                    // Save physical activity time
                                    surveyViewModel.updatePhysicalActivityTime(selectedTime)
                                    onNext()
                                }
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    color = Color(0x40FFFFFF),
                                    shape = CircleShape
                                )
                        ) {
                            Icon(
                                painter = painterResource(android.R.drawable.ic_media_play),
                                contentDescription = "Next",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(60.dp)) // balanced bottom space
        }
    }
}

@Composable
fun HealthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        placeholder = {
            Text(text = placeholder, color = Color(0x80FFFFFF))
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color(0x40FFFFFF),
            unfocusedContainerColor = Color(0x40FFFFFF),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            cursorColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true
    )
}

@Composable
fun TimeRadioButtonOption(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
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
fun PreviewHealthHistory2Screen() {
    HealthHistory2ScreenContent()
}
