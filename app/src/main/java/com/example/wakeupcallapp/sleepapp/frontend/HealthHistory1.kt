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

class HealthHistory : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HealthHistory1ScreenContent()
        }
    }
}

@Composable
fun HealthHistory1ScreenContent(
    surveyViewModel: SurveyViewModel = viewModel(viewModelStoreOwner = LocalContext.current as ComponentActivity),
    onNext: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    // Collect current ViewModel values
    val currentHypertension by surveyViewModel.hypertension.collectAsState()
    val currentDiabetes by surveyViewModel.diabetes.collectAsState()
    val currentSmokes by surveyViewModel.smokes.collectAsState()
    val currentAlcohol by surveyViewModel.alcohol.collectAsState()
    
    // Initialize with current values - remember keys ensure re-initialization when values change
    var hypertension by remember(currentHypertension) { mutableStateOf(if (currentHypertension) "Yes" else "No") }
    var diabetes by remember(currentDiabetes) { mutableStateOf(if (currentDiabetes) "Yes" else "No") }
    var smoking by remember(currentSmokes) { mutableStateOf(if (currentSmokes) "Yes" else "No") }
    var alcohol by remember(currentAlcohol) { mutableStateOf(if (currentAlcohol) "Yes" else "No") }
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
            Spacer(modifier = Modifier.height(48.dp)) // Moved slightly upward (more top space)

            // Card container
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0x50FFFFFF)
                )
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

                    // Question 1: Hypertension
                    Text(
                        text = "Have you been diagnosed with high blood pressure (hypertension)?",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White,
                        lineHeight = 22.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    HealthRadioButtonOption("Yes", hypertension == "Yes") { hypertension = "Yes" }
                    Spacer(modifier = Modifier.height(8.dp))
                    HealthRadioButtonOption("No", hypertension == "No") { hypertension = "No" }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Question 2: Diabetes
                    Text(
                        text = "Have you been diagnosed with Diabetes?",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White,
                        lineHeight = 22.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    HealthRadioButtonOption("Yes", diabetes == "Yes") { diabetes = "Yes" }
                    Spacer(modifier = Modifier.height(8.dp))
                    HealthRadioButtonOption("No", diabetes == "No") { diabetes = "No" }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Question 3: Smoking
                    Text(
                        text = "Do you smoke?",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White,
                        lineHeight = 22.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    HealthRadioButtonOption("Yes", smoking == "Yes") { smoking = "Yes" }
                    Spacer(modifier = Modifier.height(8.dp))
                    HealthRadioButtonOption("No", smoking == "No") { smoking = "No" }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Question 4: Alcohol
                    Text(
                        text = "Do you drink alcohol?",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White,
                        lineHeight = 22.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    HealthRadioButtonOption("Yes", alcohol == "Yes") { alcohol = "Yes" }
                    Spacer(modifier = Modifier.height(8.dp))
                    HealthRadioButtonOption("No", alcohol == "No") { alcohol = "No" }

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

                    // Navigation buttons row (inside the card)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Back button
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

                        // Next button
                        IconButton(
                            onClick = {
                                android.util.Log.d("HealthHistory1", "=== HEALTH HISTORY FORM SUBMISSION ===")
                                android.util.Log.d("HealthHistory1", "Form values: hypertension='$hypertension', diabetes='$diabetes', smoking='$smoking', alcohol='$alcohol'")
                                android.util.Log.d("HealthHistory1", "Current ViewModel: hypertension=$currentHypertension, diabetes=$currentDiabetes, smokes=$currentSmokes, alcohol=$currentAlcohol")
                                
                                // Validate all fields are answered
                                if (hypertension.isEmpty() || diabetes.isEmpty() || 
                                    smoking.isEmpty() || alcohol.isEmpty()) {
                                    showError = true
                                    android.util.Log.d("HealthHistory1", "❌ Validation failed - empty fields")
                                } else {
                                    showError = false
                                    // Save to ViewModel
                                    surveyViewModel.updateMedicalHistory(
                                        hypertension = hypertension == "Yes",
                                        diabetes = diabetes == "Yes",
                                        smokes = smoking == "Yes",
                                        alcohol = alcohol == "Yes"
                                    )
                                    android.util.Log.d("HealthHistory1", "✅ Updated ViewModel successfully")
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

            Spacer(modifier = Modifier.height(40.dp)) // Reduced bottom space for balance
        }
    }
}

@Composable
fun HealthRadioButtonOption(
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
fun PreviewHealthHistory1Screen() {
    HealthHistory1ScreenContent()
}
