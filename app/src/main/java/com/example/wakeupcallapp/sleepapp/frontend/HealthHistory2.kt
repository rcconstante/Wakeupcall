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
    healthConnectViewModel: com.example.wakeupcallapp.sleepapp.viewmodel.HealthConnectViewModel = viewModel(viewModelStoreOwner = LocalContext.current as ComponentActivity),
    onNext: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    // Get Health Connect data
    val healthData by healthConnectViewModel.healthData.collectAsState()
    val currentDailySteps by surveyViewModel.dailySteps.collectAsState()
    val currentPhysicalActivityTime by surveyViewModel.physicalActivityTime.collectAsState()
    
    // Initialize with Health Connect data or default, then allow user override
    var stepCount by remember(healthData, currentDailySteps) { 
        mutableStateOf(
            when {
                healthData != null && healthData!!.averageDailySteps > 0 -> healthData!!.averageDailySteps.toString()
                currentDailySteps > 0 -> currentDailySteps.toString()
                else -> ""
            }
        )
    }
    var activityMinutes by remember { mutableStateOf("") }
    var selectedActivityType by remember { mutableStateOf("") }
    var selectedTime by remember(healthData, currentPhysicalActivityTime) { 
        mutableStateOf(
            if (currentPhysicalActivityTime.isNotEmpty()) {
                currentPhysicalActivityTime
            } else if (healthData != null && healthData!!.averageDailySteps > 0) {
                when {
                    healthData!!.averageDailySteps >= 10000 -> "60+ minutes"
                    healthData!!.averageDailySteps >= 7500 -> "30-60 minutes"
                    healthData!!.averageDailySteps >= 5000 -> "15-30 minutes"
                    else -> "Less than 15 minutes"
                }
            } else {
                ""
            }
        )
    }
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
                        text = "Typical daily step-count: *",
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

                    // Types of Physical Activity - Dropdown
                    Text(
                        text = "Type of physical activity you usually perform: *",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    var activityExpanded by remember { mutableStateOf(false) }
                    val activityTypes = listOf(
                        "Walking/Jogging",
                        "Running/Marathon",
                        "Gym/Weight Training",
                        "Swimming",
                        "Cycling",
                        "Yoga/Pilates",
                        "Sports (Basketball, Football, etc.)",
                        "Dance/Aerobics",
                        "Hiking/Outdoor Activities",
                        "Home Workouts",
                        "Sedentary/No Regular Exercise"
                    )
                    
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = selectedActivityType,
                            onValueChange = {},
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = true,
                            placeholder = { Text("Select activity type", color = Color(0x80FFFFFF)) },
                            trailingIcon = {
                                Icon(
                                    painter = painterResource(android.R.drawable.arrow_down_float),
                                    contentDescription = "Dropdown",
                                    tint = Color.White,
                                    modifier = Modifier.clickable { activityExpanded = !activityExpanded }
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0x40FFFFFF),
                                unfocusedContainerColor = Color(0x40FFFFFF),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                        DropdownMenu(
                            expanded = activityExpanded,
                            onDismissRequest = { activityExpanded = false },
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xE6FFFFFF))
                        ) {
                            activityTypes.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option, color = Color.Black) },
                                    onClick = {
                                        selectedActivityType = option
                                        activityExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Time of Day Question - now dropdown
                    Text(
                        text = "At what time of day do you usually perform your physical activities? *",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White,
                        lineHeight = 22.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    var expanded by remember { mutableStateOf(false) }
                    val timeOptions = listOf(
                        "Morning (6 AM - 10 AM)",
                        "Afternoon (11 AM - 4 PM)",
                        "Evening (5 PM - 9 PM)",
                        "Late Night (10 PM - 12 AM)",
                        "I don't have a fixed time"
                    )
                    
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = selectedTime,
                            onValueChange = {},
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = true,
                            trailingIcon = {
                                Icon(
                                    painter = painterResource(android.R.drawable.arrow_down_float),
                                    contentDescription = "Dropdown",
                                    tint = Color.White,
                                    modifier = Modifier.clickable { expanded = !expanded }
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0x40FFFFFF),
                                unfocusedContainerColor = Color(0x40FFFFFF),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xE6FFFFFF))
                        ) {
                            timeOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option, color = Color.Black) },
                                    onClick = {
                                        selectedTime = option
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

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
                        TextButton(
                            onClick = onBack,
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = Color.White
                            )
                        ) {
                            Text(
                                text = "Back",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // Next Button
                        TextButton(
                            onClick = {
                                // Validate all fields
                                if (stepCount.isEmpty() || activityMinutes.isEmpty() || 
                                    selectedActivityType.isEmpty() || selectedTime.isEmpty()) {
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
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = Color.White
                            )
                        ) {
                            Text(
                                text = "Next",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
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
