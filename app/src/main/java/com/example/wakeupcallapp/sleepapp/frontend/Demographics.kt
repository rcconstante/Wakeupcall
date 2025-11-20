package com.example.wakeupcallapp.sleepapp

import android.os.Bundle
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wakeupcallapp.sleepapp.R

class Demographics : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DemographicsScreenContent()
        }
    }
}

@Composable
fun DemographicsScreenContent(
    onNext: () -> Unit = {},
    onBack: () -> Unit = {},
    surveyViewModel: com.example.wakeupcallapp.sleepapp.viewmodel.SurveyViewModel = androidx.lifecycle.viewmodel.compose.viewModel(viewModelStoreOwner = androidx.compose.ui.platform.LocalContext.current as androidx.activity.ComponentActivity),
    healthConnectViewModel: com.example.wakeupcallapp.sleepapp.viewmodel.HealthConnectViewModel = androidx.lifecycle.viewmodel.compose.viewModel(viewModelStoreOwner = androidx.compose.ui.platform.LocalContext.current as androidx.activity.ComponentActivity)
) {
    // Get Health Connect data
    val healthData by healthConnectViewModel.healthData.collectAsState()
    val isHealthConnected by healthConnectViewModel.isConnected.collectAsState()
    
    // Pre-fill survey data from Health Connect when data becomes available
    LaunchedEffect(healthData, isHealthConnected) {
        if (isHealthConnected && healthData != null) {
            surveyViewModel.prefillFromHealthConnect(healthData)
        }
    }
    
    // Collect current ViewModel values
    val currentAge by surveyViewModel.age.collectAsState()
    val currentSex by surveyViewModel.sex.collectAsState()
    val currentHeight by surveyViewModel.heightCm.collectAsState()
    val currentWeight by surveyViewModel.weightKg.collectAsState()
    val currentNeck by surveyViewModel.neckCircumferenceCm.collectAsState()
    
    // Initialize form fields with current values, but allow user input to override
    var age by remember(currentAge) { mutableStateOf(currentAge.toString()) }
    var selectedSex by remember(currentSex) { mutableStateOf(if (currentSex == "male") "Male" else "Female") }
    var height by remember(currentHeight) { mutableStateOf(currentHeight.toString()) }
    var weight by remember(currentWeight) { mutableStateOf(currentWeight.toString()) }
    var neckCircumference by remember(currentNeck) { mutableStateOf(currentNeck.toString()) }

    // Calculate BMI
    val bmi = remember(height, weight) {
        try {
            val h = height.toDoubleOrNull() ?: 0.0
            val w = weight.toDoubleOrNull() ?: 0.0
            if (h > 0 && w > 0) {
                val heightInMeters = h / 100
                String.format("%.1f", w / (heightInMeters * heightInMeters))
            } else {
                "--"
            }
        } catch (e: Exception) {
            "--"
        }
    }

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
                        text = "Demographics",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Age field
                    Text(
                        text = "What is your age?",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    CustomTextField(
                        value = age,
                        onValueChange = { age = it },
                        placeholder = "",
                        keyboardType = KeyboardType.Number
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Sex selection
                    Text(
                        text = "What is your sex?",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    RadioButtonOption(
                        text = "Female",
                        selected = selectedSex == "Female",
                        onClick = { selectedSex = "Female" }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    RadioButtonOption(
                        text = "Male",
                        selected = selectedSex == "Male",
                        onClick = { selectedSex = "Male" }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Height field
                    Text(
                        text = "What is your height (cm)?",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    CustomTextField(
                        value = height,
                        onValueChange = { height = it },
                        placeholder = "",
                        keyboardType = KeyboardType.Number
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Weight field
                    Text(
                        text = "What is your weight (kg)?",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    CustomTextField(
                        value = weight,
                        onValueChange = { weight = it },
                        placeholder = "",
                        keyboardType = KeyboardType.Number
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // BMI Display
                    Text(
                        text = "Your BMI:   $bmi",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Neck circumference field
                    Text(
                        text = "What is your neck-circumference (cm)?",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    CustomTextField(
                        value = neckCircumference,
                        onValueChange = { neckCircumference = it },
                        placeholder = "",
                        keyboardType = KeyboardType.Number
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Next button inside card (bottom right)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(
                            onClick = {
                                // Log form values BEFORE parsing
                                android.util.Log.d("Demographics", "=== DEMOGRAPHICS FORM SUBMISSION ===")
                                android.util.Log.d("Demographics", "Raw form values: age='$age', sex='$selectedSex', height='$height', weight='$weight', neck='$neckCircumference'")
                                
                                // Parse values - NO DEFAULTS, use what's in the form
                                val ageInt = age.toIntOrNull() ?: currentAge
                                val heightDouble = height.toDoubleOrNull() ?: currentHeight
                                val weightDouble = weight.toDoubleOrNull() ?: currentWeight
                                val neckDouble = neckCircumference.toDoubleOrNull() ?: currentNeck
                                val sexString = if (selectedSex == "Male") "male" else "female"
                                
                                android.util.Log.d("Demographics", "Parsed values: age=$ageInt, sex=$sexString, height=$heightDouble, weight=$weightDouble, neck=$neckDouble")
                                android.util.Log.d("Demographics", "Current ViewModel before update: age=${currentAge}, sex=${currentSex}, height=${currentHeight}, weight=${currentWeight}, neck=${currentNeck}")
                                
                                surveyViewModel.updateDemographics(
                                    age = ageInt,
                                    sex = sexString,
                                    heightCm = heightDouble,
                                    weightKg = weightDouble,
                                    neckCm = neckDouble
                                )
                                
                                android.util.Log.d("Demographics", "✅ Updated ViewModel successfully")
                                onNext()
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
fun CustomTextField(
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
fun RadioButtonOption(
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
fun PreviewDemographicsScreen() {
    DemographicsScreenContent()
}
