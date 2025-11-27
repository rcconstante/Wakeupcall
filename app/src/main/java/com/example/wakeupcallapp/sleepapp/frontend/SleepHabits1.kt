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

class SleepHabits1 : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SleepHabits1ScreenContent()
        }
    }
}

@Composable
fun SleepHabits1ScreenContent(
    surveyViewModel: SurveyViewModel = viewModel(viewModelStoreOwner = LocalContext.current as ComponentActivity),
    healthConnectViewModel: com.example.wakeupcallapp.sleepapp.viewmodel.HealthConnectViewModel = viewModel(viewModelStoreOwner = LocalContext.current as ComponentActivity),
    onNext: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    // Get Health Connect data
    val healthData by healthConnectViewModel.healthData.collectAsState()
    val currentSleepHours by surveyViewModel.sleepHours.collectAsState()
    val currentSnores by surveyViewModel.snores.collectAsState()
    val currentSnoringLevel by surveyViewModel.snoringLevel.collectAsState()
    
    // Initialize with Health Connect data if available, otherwise use ViewModel value
    // This persists state when user navigates back
    var sleepHours by remember(currentSleepHours, healthData) { 
        mutableStateOf(
            if (healthData != null && healthData!!.sleepDurationHours > 0) {
                String.format("%.1f", healthData!!.sleepDurationHours)
            } else if (currentSleepHours > 0) {
                String.format("%.1f", currentSleepHours)
            } else {
                ""
            }
        )
    }
    // Persist snoring answer from ViewModel
    var doesSnore by remember(currentSnores) { 
        mutableStateOf(
            when {
                currentSnores -> "Yes"
                currentSnoringLevel == "None" || currentSnoringLevel.isEmpty() -> ""
                else -> "No"
            }
        )
    }
    // Persist snoring level from ViewModel
    var snoringLevel by remember(currentSnoringLevel) { 
        mutableStateOf(
            when (currentSnoringLevel) {
                "Slightly louder" -> "Slightly louder"
                "As loud as talking" -> "As loud as talking"
                "Louder than talking" -> "Louder than talking"
                "Very loud" -> "Very loud"
                else -> ""
            }
        )
    }
    var showError by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
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
            Spacer(modifier = Modifier.height(40.dp))

            // Card container
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
                        text = "Sleep Habits and Behavior",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Sleep hours field
                    Text(
                        text = "How many hours of sleep do you get? *",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White,
                        lineHeight = 22.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    SleepTextField(
                        value = sleepHours,
                        onValueChange = { sleepHours = it },
                        placeholder = "",
                        keyboardType = KeyboardType.Number
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Do you snore question
                    Text(
                        text = "Do you snore? *",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    SleepRadioButtonOption(
                        text = "Yes",
                        selected = doesSnore == "Yes",
                        onClick = { doesSnore = "Yes" }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    SleepRadioButtonOption(
                        text = "No",
                        selected = doesSnore == "No",
                        onClick = { doesSnore = "No" }
                    )

                    // Only show snoring level if user answered Yes to snoring
                    if (doesSnore == "Yes") {
                        Spacer(modifier = Modifier.height(24.dp))

                        // Snoring level question
                        Text(
                            text = "Your snoring is... *",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        SleepRadioButtonOption(
                            text = "Slightly louder than breathing",
                            selected = snoringLevel == "Slightly louder",
                            onClick = { snoringLevel = "Slightly louder" }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        SleepRadioButtonOption(
                            text = "As loud as talking",
                            selected = snoringLevel == "As loud as talking",
                            onClick = { snoringLevel = "As loud as talking" }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        SleepRadioButtonOption(
                            text = "Louder than talking",
                            selected = snoringLevel == "Louder than talking",
                            onClick = { snoringLevel = "Louder than talking" }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        SleepRadioButtonOption(
                            text = "Very loud, can be heard in adjacent rooms",
                            selected = snoringLevel == "Very loud",
                            onClick = { snoringLevel = "Very loud" }
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Error message
                    if (showError) {
                        Text(
                            text = "Please answer all questions before continuing",
                            fontSize = 14.sp,
                            color = Color(0xFFFF6B6B),
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Navigation buttons (aligned inside card)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
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
                                // Validate: if snoring Yes, must select level; if No, skip level
                                val isValid = sleepHours.isNotEmpty() && doesSnore.isNotEmpty() &&
                                    (doesSnore == "No" || snoringLevel.isNotEmpty())
                                
                                if (!isValid) {
                                    showError = true
                                } else {
                                    showError = false
                                    // Save to ViewModel
                                    surveyViewModel.updateSleepHabits(
                                        hours = sleepHours.toDoubleOrNull() ?: 7.0,
                                        snores = doesSnore == "Yes",
                                        snoringLevel = if (doesSnore == "Yes") snoringLevel else "None",
                                        observedApnea = false
                                    )
                                    // Map to Berlin Category 1
                                    surveyViewModel.updateBerlinCategory1("item2", doesSnore == "Yes")
                                    surveyViewModel.updateBerlinCategory1("item3", snoringLevel == "Very loud")
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

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun SleepTextField(
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
            Text(
                text = placeholder,
                color = Color(0x80FFFFFF)
            )
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
fun SleepRadioButtonOption(
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
                .border(
                    width = 2.dp,
                    color = Color.White,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (selected) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(
                            color = Color(0xFF6B8DD6),
                            shape = CircleShape
                        )
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
fun PreviewSleepHabits1Screen() {
    SleepHabits1ScreenContent()
}
