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

// Helper function to map frequency for Berlin item 9
private fun mapFrequencyToBerlin(frequency: String): Boolean {
    return when(frequency) {
        "Nearly every day", "3-4 times a week" -> true
        else -> false
    }
}

class FatigueSleepiness2 : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FatigueSleepiness2ScreenContent()
        }
    }
}

@Composable
fun FatigueSleepiness2ScreenContent(
    surveyViewModel: SurveyViewModel = viewModel(viewModelStoreOwner = LocalContext.current as ComponentActivity),
    onNext: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    var noddedOffDriving by remember { mutableStateOf("") }
    var dozingReading by remember { mutableStateOf("") }
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

            // Card container (consistent look)
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
                        text = "Fatigue & Sleepiness",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Question 1
                    Text(
                        text = "Have you ever nodded off or fallen asleep while driving a vehicle?",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White,
                        lineHeight = 22.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Fatigue2RadioButtonOption(
                        text = "Nearly every day",
                        selected = noddedOffDriving == "Nearly every day",
                        onClick = { noddedOffDriving = "Nearly every day" }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Fatigue2RadioButtonOption(
                        text = "3-4 times a week",
                        selected = noddedOffDriving == "3-4 times a week",
                        onClick = { noddedOffDriving = "3-4 times a week" }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Fatigue2RadioButtonOption(
                        text = "1-2 times a week",
                        selected = noddedOffDriving == "1-2 times a week",
                        onClick = { noddedOffDriving = "1-2 times a week" }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Fatigue2RadioButtonOption(
                        text = "1-2 times a month",
                        selected = noddedOffDriving == "1-2 times a month",
                        onClick = { noddedOffDriving = "1-2 times a month" }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Fatigue2RadioButtonOption(
                        text = "Never or nearly never",
                        selected = noddedOffDriving == "Never or nearly never",
                        onClick = { noddedOffDriving = "Never or nearly never" }
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Question 2
                    Text(
                        text = "Identify how likely you are to fall asleep during the following daytime activities:",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White,
                        lineHeight = 22.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Sitting and reading",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Fatigue2RadioButtonOption(
                        text = "No chance of dozing",
                        selected = dozingReading == "No chance of dozing",
                        onClick = { dozingReading = "No chance of dozing" }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Fatigue2RadioButtonOption(
                        text = "Slight chance of dozing",
                        selected = dozingReading == "Slight chance of dozing",
                        onClick = { dozingReading = "Slight chance of dozing" }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Fatigue2RadioButtonOption(
                        text = "Moderate chance of dozing",
                        selected = dozingReading == "Moderate chance of dozing",
                        onClick = { dozingReading = "Moderate chance of dozing" }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Fatigue2RadioButtonOption(
                        text = "High chance of dozing",
                        selected = dozingReading == "High chance of dozing",
                        onClick = { dozingReading = "High chance of dozing" }
                    )

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

                    // Navigation buttons (inside card)
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
                                if (noddedOffDriving.isEmpty() || dozingReading.isEmpty()) {
                                    showError = true
                                } else {
                                    showError = false
                                    // Save ESS Q1 (sitting and reading)
                                    surveyViewModel.updateESSResponse(0, ESSMapper.mapESSDozing(dozingReading))
                                    // Save Berlin item 9 (dozed while driving)
                                    surveyViewModel.updateBerlinCategory2("item9", mapFrequencyToBerlin(noddedOffDriving))
                                    // Save nodded off driving separately for database
                                    surveyViewModel.updateNoddedOffDriving(noddedOffDriving)
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

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun Fatigue2RadioButtonOption(
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
fun PreviewFatigueSleepiness2Screen() {
    FatigueSleepiness2ScreenContent()
}
