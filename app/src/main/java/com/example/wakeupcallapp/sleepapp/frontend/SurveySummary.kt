package com.example.wakeupcallapp.sleepapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.platform.LocalContext

class SurveySummary : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SurveySummaryScreenContent()
        }
    }
}

@Composable
fun SurveySummaryScreenContent(
    surveyViewModel: SurveyViewModel = viewModel(viewModelStoreOwner = LocalContext.current as ComponentActivity),
    authViewModel: com.example.wakeupcallapp.sleepapp.viewmodel.AuthViewModel = viewModel(viewModelStoreOwner = LocalContext.current as ComponentActivity),
    healthConnectViewModel: com.example.wakeupcallapp.sleepapp.viewmodel.HealthConnectViewModel = viewModel(viewModelStoreOwner = LocalContext.current as ComponentActivity),
    onSubmit: () -> Unit = {},
    onEdit: (String) -> Unit = {},
    onBack: () -> Unit = {}
) {
    // Get auth token for submission
    val token by authViewModel.authToken.collectAsState()
    
    // Collect all survey data
    val age by surveyViewModel.age.collectAsState()
    val sex by surveyViewModel.sex.collectAsState()
    val heightCm by surveyViewModel.heightCm.collectAsState()
    val weightKg by surveyViewModel.weightKg.collectAsState()
    val neckCircumferenceCm by surveyViewModel.neckCircumferenceCm.collectAsState()
    val hypertension by surveyViewModel.hypertension.collectAsState()
    val diabetes by surveyViewModel.diabetes.collectAsState()
    val smokes by surveyViewModel.smokes.collectAsState()
    val alcohol by surveyViewModel.alcohol.collectAsState()
    val sleepHours by surveyViewModel.sleepHours.collectAsState()
    val snores by surveyViewModel.snores.collectAsState()
    val snoringLevel by surveyViewModel.snoringLevel.collectAsState()
    val dailySteps by surveyViewModel.dailySteps.collectAsState()
    val physicalActivityTime by surveyViewModel.physicalActivityTime.collectAsState()
    
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
            Spacer(modifier = Modifier.height(40.dp))

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
                        text = "Review Your Answers",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    
                    Text(
                        text = "Review your responses before submitting. Tap Edit to make changes.",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.9f),
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Demographics Section
                    SummarySectionHeader("Demographics")
                    SummaryItem("Age", "$age years", onEditClick = { onEdit("demographics") })
                    SummaryItem("Sex", sex.capitalize(), onEditClick = { onEdit("demographics") })
                    SummaryItem("Height", String.format("%.1f cm", heightCm), onEditClick = { onEdit("demographics") })
                    SummaryItem("Weight", String.format("%.1f kg", weightKg), onEditClick = { onEdit("demographics") })
                    SummaryItem("Neck Circumference", String.format("%.1f cm", neckCircumferenceCm), onEditClick = { onEdit("demographics") })
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    // Medical History Section
                    SummarySectionHeader("Medical History")
                    SummaryItem("Hypertension", if (hypertension) "Yes" else "No", onEditClick = { onEdit("health_history_1") })
                    SummaryItem("Diabetes", if (diabetes) "Yes" else "No", onEditClick = { onEdit("health_history_1") })
                    SummaryItem("Smokes", if (smokes) "Yes" else "No", onEditClick = { onEdit("health_history_1") })
                    SummaryItem("Alcohol", if (alcohol) "Yes" else "No", onEditClick = { onEdit("health_history_1") })
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    // Activity & Lifestyle Section
                    SummarySectionHeader("Activity & Lifestyle")
                    SummaryItem("Daily Steps", dailySteps.toString(), onEditClick = { onEdit("health_history_2") })
                    SummaryItem("Physical Activity Time", physicalActivityTime, onEditClick = { onEdit("health_history_2") })
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    // Sleep Habits Section
                    SummarySectionHeader("Sleep Habits")
                    SummaryItem("Sleep Duration", String.format("%.1f hours", sleepHours), onEditClick = { onEdit("sleep_habits_1") })
                    SummaryItem("Snoring", if (snores) "Yes" else "No", onEditClick = { onEdit("sleep_habits_1") })
                    if (snores) {
                        SummaryItem("Snoring Level", snoringLevel, onEditClick = { onEdit("sleep_habits_1") })
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Navigation Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
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

                        Button(
                            onClick = {
                                android.util.Log.d("SurveySummary", "🚀 Submit Survey button clicked!")
                                
                                // Submit survey to backend first
                                token?.let { authToken ->
                                    android.util.Log.d("SurveySummary", "📤 Calling surveyViewModel.submitSurvey() with token")
                                    surveyViewModel.submitSurvey(authToken, healthConnectViewModel)
                                } ?: run {
                                    android.util.Log.e("SurveySummary", "❌ No auth token available!")
                                }
                                
                                // Then navigate via callback
                                onSubmit()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF6B8DD6),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Submit Survey",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
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
fun SummarySectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
fun SummaryItem(
    label: String,
    value: String,
    onEditClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.7f),
                fontWeight = FontWeight.Normal
            )
            Text(
                text = value,
                fontSize = 16.sp,
                color = Color.White,
                fontWeight = FontWeight.Medium
            )
        }
        
        TextButton(
            onClick = onEditClick,
            colors = ButtonDefaults.textButtonColors(
                contentColor = Color(0xFF6B8DD6)
            )
        ) {
            Text(
                text = "Edit",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewSurveySummaryScreen() {
    SurveySummaryScreenContent()
}
