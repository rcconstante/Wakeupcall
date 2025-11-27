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
import com.example.wakeupcallapp.sleepapp.utils.ESSMapper
import androidx.compose.ui.platform.LocalContext

class FatigueSleepiness4 : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FatigueSleepiness4ScreenContent()
        }
    }
}

@Composable
fun FatigueSleepiness4ScreenContent(
    surveyViewModel: SurveyViewModel = viewModel(viewModelStoreOwner = LocalContext.current as ComponentActivity),
    onNext: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    // Get saved ESS values from ViewModel to persist when navigating back
    val essResponses by surveyViewModel.essResponses.collectAsState()
    
    // ESS Q5, Q6, Q7 are at indices 4, 5, 6
    var carStop by remember(essResponses) { 
        mutableStateOf(
            if (essResponses.any { it > 0 }) ESSMapper.scoreToChoice(essResponses[4]) else ""
        )
    }
    var readingSitting by remember(essResponses) { 
        mutableStateOf(
            if (essResponses.any { it > 0 }) ESSMapper.scoreToChoice(essResponses[5]) else ""
        )
    }
    var afterDinner by remember(essResponses) { 
        mutableStateOf(
            if (essResponses.any { it > 0 }) ESSMapper.scoreToChoice(essResponses[6]) else ""
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

        // Scrollable content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(36.dp)) // slightly upward spacing

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
                        text = "Fatigue & Sleepiness",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Question 1
                    Text(
                        text = "In a car, while stopped for a few minutes in traffic *",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Fatigue4RadioOption("No chance of dozing", carStop) { carStop = it }
                    Spacer(modifier = Modifier.height(8.dp))
                    Fatigue4RadioOption("Slight chance of dozing", carStop) { carStop = it }
                    Spacer(modifier = Modifier.height(8.dp))
                    Fatigue4RadioOption("Moderate chance of dozing", carStop) { carStop = it }
                    Spacer(modifier = Modifier.height(8.dp))
                    Fatigue4RadioOption("High chance of dozing", carStop) { carStop = it }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Question 2
                    Text(
                        text = "Reading while sitting quietly *",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Fatigue4RadioOption("No chance of dozing", readingSitting) { readingSitting = it }
                    Spacer(modifier = Modifier.height(8.dp))
                    Fatigue4RadioOption("Slight chance of dozing", readingSitting) { readingSitting = it }
                    Spacer(modifier = Modifier.height(8.dp))
                    Fatigue4RadioOption("Moderate chance of dozing", readingSitting) { readingSitting = it }
                    Spacer(modifier = Modifier.height(8.dp))
                    Fatigue4RadioOption("High chance of dozing", readingSitting) { readingSitting = it }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Question 3
                    Text(
                        text = "Sitting quietly after dinner (without alcohol) *",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Fatigue4RadioOption("No chance of dozing", afterDinner) { afterDinner = it }
                    Spacer(modifier = Modifier.height(8.dp))
                    Fatigue4RadioOption("Slight chance of dozing", afterDinner) { afterDinner = it }
                    Spacer(modifier = Modifier.height(8.dp))
                    Fatigue4RadioOption("Moderate chance of dozing", afterDinner) { afterDinner = it }
                    Spacer(modifier = Modifier.height(8.dp))
                    Fatigue4RadioOption("High chance of dozing", afterDinner) { afterDinner = it }

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

                    // Navigation buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
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
                            Text("Back")
                        }

                        // Next button
                        TextButton(
                            onClick = {
                                if (carStop.isEmpty() || readingSitting.isEmpty() || afterDinner.isEmpty()) {
                                    showError = true
                                } else {
                                    showError = false
                                    // Save ESS Q5, Q6, Q7
                                    surveyViewModel.updateESSResponse(4, ESSMapper.mapESSDozing(carStop))
                                    surveyViewModel.updateESSResponse(5, ESSMapper.mapESSDozing(readingSitting))
                                    surveyViewModel.updateESSResponse(6, ESSMapper.mapESSDozing(afterDinner))
                                    onNext()
                                }
                            },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = Color.White
                            )
                        ) {
                            Text("Next")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}

@Composable
fun Fatigue4RadioOption(
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
fun PreviewFatigueSleepiness4Screen() {
    FatigueSleepiness4ScreenContent()
}
