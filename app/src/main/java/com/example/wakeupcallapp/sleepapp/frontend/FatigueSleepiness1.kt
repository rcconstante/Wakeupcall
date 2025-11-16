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

class FatigueSleepiness1 : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FatigueSleepiness1ScreenContent()
        }
    }
}

@Composable
fun FatigueSleepiness1ScreenContent(
    surveyViewModel: SurveyViewModel = viewModel(viewModelStoreOwner = LocalContext.current as ComponentActivity),
    onNext: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    var tiredDuringDay by remember { mutableStateOf("") }
    var tiredAfterSleep by remember { mutableStateOf("") }
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

            // Card container (consistent size)
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
                        text = "During your wake time, do you feel tired, fatigued, or not up to par?",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White,
                        lineHeight = 22.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    FatigueRadioButtonOption(
                        text = "Nearly every day",
                        selected = tiredDuringDay == "Nearly every day",
                        onClick = { tiredDuringDay = "Nearly every day" }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FatigueRadioButtonOption(
                        text = "3-4 times a week",
                        selected = tiredDuringDay == "3-4 times a week",
                        onClick = { tiredDuringDay = "3-4 times a week" }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FatigueRadioButtonOption(
                        text = "1-2 times a week",
                        selected = tiredDuringDay == "1-2 times a week",
                        onClick = { tiredDuringDay = "1-2 times a week" }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FatigueRadioButtonOption(
                        text = "1-2 times a month",
                        selected = tiredDuringDay == "1-2 times a month",
                        onClick = { tiredDuringDay = "1-2 times a month" }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FatigueRadioButtonOption(
                        text = "Never or nearly never",
                        selected = tiredDuringDay == "Never or nearly never",
                        onClick = { tiredDuringDay = "Never or nearly never" }
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Question 2
                    Text(
                        text = "How often do you feel tired or fatigued after your sleep?",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White,
                        lineHeight = 22.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    FatigueRadioButtonOption(
                        text = "Nearly every day",
                        selected = tiredAfterSleep == "Nearly every day",
                        onClick = { tiredAfterSleep = "Nearly every day" }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FatigueRadioButtonOption(
                        text = "3-4 times a week",
                        selected = tiredAfterSleep == "3-4 times a week",
                        onClick = { tiredAfterSleep = "3-4 times a week" }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FatigueRadioButtonOption(
                        text = "1-2 times a week",
                        selected = tiredAfterSleep == "1-2 times a week",
                        onClick = { tiredAfterSleep = "1-2 times a week" }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FatigueRadioButtonOption(
                        text = "1-2 times a month",
                        selected = tiredAfterSleep == "1-2 times a month",
                        onClick = { tiredAfterSleep = "1-2 times a month" }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FatigueRadioButtonOption(
                        text = "Never or nearly never",
                        selected = tiredAfterSleep == "Never or nearly never",
                        onClick = { tiredAfterSleep = "Never or nearly never" }
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
                                if (tiredDuringDay.isEmpty() || tiredAfterSleep.isEmpty()) {
                                    showError = true
                                } else {
                                    showError = false
                                    // Save fatigue/sleepiness data
                                    surveyViewModel.updateFatigueSleepiness(
                                        tiredDuringDay = tiredDuringDay,
                                        tiredAfterSleep = tiredAfterSleep,
                                        feelsSleepy = tiredDuringDay != "Never or nearly never"
                                    )
                                    // Map to Berlin Category 2 (item 7 - tired after sleep)
                                    val frequentlyTired = tiredAfterSleep == "Nearly every day" || tiredAfterSleep == "3-4 times a week"
                                    surveyViewModel.updateBerlinCategory2("item7", frequentlyTired)
                                    // Map to Berlin Category 3 (sleepy during day)
                                    surveyViewModel.updateBerlinCategory3Sleepy(tiredDuringDay != "Never or nearly never")
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
fun FatigueRadioButtonOption(
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
fun PreviewFatigueSleepiness1Screen() {
    FatigueSleepiness1ScreenContent()
}
