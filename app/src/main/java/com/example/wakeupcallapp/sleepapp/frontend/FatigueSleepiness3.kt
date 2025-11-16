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

class FatigueSleepiness3 : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FatigueSleepiness3ScreenContent()
        }
    }
}

@Composable
fun FatigueSleepiness3ScreenContent(
    surveyViewModel: SurveyViewModel = viewModel(viewModelStoreOwner = LocalContext.current as ComponentActivity),
    onNext: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    var watchingTv by remember { mutableStateOf("") }
    var sittingPublic by remember { mutableStateOf("") }
    var sittingPassenger by remember { mutableStateOf("") }
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
            Spacer(modifier = Modifier.height(40.dp))

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
                        text = "Fatigue & Sleepiness",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Question 1
                    Text(
                        text = "Watching television",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Fatigue3RadioOption("No chance of dozing", watchingTv) { watchingTv = it }
                    Spacer(modifier = Modifier.height(8.dp))
                    Fatigue3RadioOption("Slight chance of dozing", watchingTv) { watchingTv = it }
                    Spacer(modifier = Modifier.height(8.dp))
                    Fatigue3RadioOption("Moderate chance of dozing", watchingTv) { watchingTv = it }
                    Spacer(modifier = Modifier.height(8.dp))
                    Fatigue3RadioOption("High chance of dozing", watchingTv) { watchingTv = it }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Question 2
                    Text(
                        text = "Sitting inactive in a public place",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Fatigue3RadioOption("No chance of dozing", sittingPublic) { sittingPublic = it }
                    Spacer(modifier = Modifier.height(8.dp))
                    Fatigue3RadioOption("Slight chance of dozing", sittingPublic) { sittingPublic = it }
                    Spacer(modifier = Modifier.height(8.dp))
                    Fatigue3RadioOption("Moderate chance of dozing", sittingPublic) { sittingPublic = it }
                    Spacer(modifier = Modifier.height(8.dp))
                    Fatigue3RadioOption("High chance of dozing", sittingPublic) { sittingPublic = it }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Question 3
                    Text(
                        text = "Sitting for an hour as a passenger in a car",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Fatigue3RadioOption("No chance of dozing", sittingPassenger) { sittingPassenger = it }
                    Spacer(modifier = Modifier.height(8.dp))
                    Fatigue3RadioOption("Slight chance of dozing", sittingPassenger) { sittingPassenger = it }
                    Spacer(modifier = Modifier.height(8.dp))
                    Fatigue3RadioOption("Moderate chance of dozing", sittingPassenger) { sittingPassenger = it }
                    Spacer(modifier = Modifier.height(8.dp))
                    Fatigue3RadioOption("High chance of dozing", sittingPassenger) { sittingPassenger = it }

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
                                if (watchingTv.isEmpty() || sittingPublic.isEmpty() || sittingPassenger.isEmpty()) {
                                    showError = true
                                } else {
                                    showError = false
                                    // Save ESS Q2, Q3, Q4
                                    surveyViewModel.updateESSResponse(1, ESSMapper.mapESSDozing(watchingTv))
                                    surveyViewModel.updateESSResponse(2, ESSMapper.mapESSDozing(sittingPublic))
                                    surveyViewModel.updateESSResponse(3, ESSMapper.mapESSDozing(sittingPassenger))
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
                                modifier = Modifier
                                    .size(20.dp)
                                    .graphicsLayer(rotationZ = 0f)
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
fun Fatigue3RadioOption(
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
fun PreviewFatigueSleepiness3Screen() {
    FatigueSleepiness3ScreenContent()
}