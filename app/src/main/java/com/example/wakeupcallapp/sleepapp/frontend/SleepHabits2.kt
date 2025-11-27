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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wakeupcallapp.sleepapp.viewmodel.SurveyViewModel
import androidx.compose.ui.platform.LocalContext

class SleepHabits2 : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SleepHabits2ScreenContent()
        }
    }
}

@Composable
fun SleepHabits2ScreenContent(
    surveyViewModel: SurveyViewModel = viewModel(viewModelStoreOwner = LocalContext.current as ComponentActivity),
    onNext: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    // Get snoring state from ViewModel to conditionally show/hide snoring questions
    val snores by surveyViewModel.snores.collectAsState()
    val currentSnoringFrequency by surveyViewModel.snoringFrequency.collectAsState()
    val currentSnoringBothersOthers by surveyViewModel.snoringBothersOthers.collectAsState()
    val currentObservedApnea by surveyViewModel.observedApnea.collectAsState()
    
    // Initialize local state from ViewModel values (persist when going back)
    var snoreFrequency by remember(currentSnoringFrequency) { 
        mutableStateOf(currentSnoringFrequency.ifEmpty { "" }) 
    }
    var botheredOthers by remember(currentSnoringBothersOthers) { 
        mutableStateOf(
            when {
                currentSnoringBothersOthers -> "Yes"
                currentSnoringFrequency.isNotEmpty() -> "No"
                else -> ""
            }
        ) 
    }
    var breathingQuit by remember(currentObservedApnea) { 
        mutableStateOf(
            when {
                currentObservedApnea -> "Nearly every day"
                currentSnoringFrequency.isNotEmpty() -> "Never or nearly never"
                else -> ""
            }
        ) 
    }
    var showError by remember { mutableStateOf(false) }
    val dailySteps by surveyViewModel.dailySteps.collectAsState()

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Background image
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
                        text = "Sleep Habits & Behavior",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Only show snoring-related questions if user said "Yes" to snoring
                    if (snores) {
                        // Question 1 - Snoring frequency
                        Text(
                            text = "How often do you snore?*",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        listOf(
                            "Nearly every day",
                            "3–4 times a week",
                            "1–2 times a week",
                            "1–2 times a month",
                            "Never or nearly never"
                        ).forEach { option ->
                            Snore2RadioButtonOption(
                                text = option,
                                selected = snoreFrequency == option,
                                onClick = { snoreFrequency = option }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        Spacer(modifier = Modifier.height(28.dp))

                        // Question 2 - Snoring bothers others
                        Text(
                            text = "Has your snoring ever bothered other people?*",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White,
                            lineHeight = 22.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        listOf("Yes", "No").forEach { option ->
                            Snore2RadioButtonOption(
                                text = option,
                                selected = botheredOthers == option,
                                onClick = { botheredOthers = option }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        Spacer(modifier = Modifier.height(28.dp))
                    }

                    // Question 3
                    Text(
                        text = "Has anyone observed that you quit breathing during your sleep?*",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White,
                        lineHeight = 22.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    listOf(
                        "Nearly every day",
                        "3–4 times a week",
                        "1–2 times a week",
                        "1–2 times a month",
                        "Never or nearly never"
                    ).forEach { option ->
                        Snore2RadioButtonOption(
                            text = option,
                            selected = breathingQuit == option,
                            onClick = { breathingQuit = option }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
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

                    // Navigation buttons (inside card, slightly upward)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Back button
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

                        // Next button
                        TextButton(
                            onClick = {
                                // If user doesn't snore, only validate breathing quit question
                                // If user snores, validate all questions
                                val isValid = if (snores) {
                                    snoreFrequency.isNotEmpty() && botheredOthers.isNotEmpty() && breathingQuit.isNotEmpty()
                                } else {
                                    breathingQuit.isNotEmpty()
                                }
                                
                                if (!isValid) {
                                    showError = true
                                } else {
                                    showError = false
                                    
                                    // Only update snoring-related fields if user snores
                                    if (snores) {
                                        surveyViewModel.updateBerlinCategory1("item4", botheredOthers == "Yes")
                                        surveyViewModel.updateSnoringFrequency(snoreFrequency)
                                        surveyViewModel.updateSnoringBothersOthers(botheredOthers == "Yes")
                                    } else {
                                        // Set default values for non-snorers
                                        surveyViewModel.updateSnoringFrequency("Never or nearly never")
                                        surveyViewModel.updateSnoringBothersOthers(false)
                                    }
                                    
                                    val hasFrequentApnea = breathingQuit == "Nearly every day" || breathingQuit == "3–4 times a week"
                                    surveyViewModel.updateSleepHabits(
                                        hours = surveyViewModel.sleepHours.value,
                                        snores = surveyViewModel.snores.value,
                                        snoringLevel = surveyViewModel.snoringLevel.value,
                                        observedApnea = hasFrequentApnea
                                    )
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

            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}

@Composable
fun Snore2RadioButtonOption(
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
fun PreviewSleepHabits2Screen() {
    SleepHabits2ScreenContent()
}
