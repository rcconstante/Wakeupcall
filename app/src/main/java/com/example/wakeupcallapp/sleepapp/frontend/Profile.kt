package com.example.wakeupcallapp.sleepapp

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.wakeupcallapp.sleepapp.R
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wakeupcallapp.sleepapp.viewmodel.AuthViewModel
import com.example.wakeupcallapp.sleepapp.viewmodel.SurveyViewModel
import com.example.wakeupcallapp.sleepapp.viewmodel.HealthConnectViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.getValue
import androidx.activity.ComponentActivity
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.nativeCanvas
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

@Composable
fun ProfileScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel(viewModelStoreOwner = LocalContext.current as ComponentActivity),
    surveyViewModel: SurveyViewModel = viewModel(viewModelStoreOwner = LocalContext.current as ComponentActivity),
    healthConnectViewModel: HealthConnectViewModel = viewModel(viewModelStoreOwner = LocalContext.current as ComponentActivity)
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val authToken by authViewModel.authToken.collectAsState()
    val submissionResult by surveyViewModel.submissionResult.collectAsState()
    val isLoadingSurvey by surveyViewModel.isLoadingSurvey.collectAsState()
    val healthData by healthConnectViewModel.healthData.collectAsState()
    val isHealthConnected by healthConnectViewModel.isConnected.collectAsState()
    val hasLoadedData by surveyViewModel.hasLoadedData.collectAsState()
    
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = isLoadingSurvey)
    
    // Load survey data when an auth token becomes available
    LaunchedEffect(authToken, hasLoadedData) {
        android.util.Log.d("Profile", "🎯 Profile LaunchedEffect - token=${authToken?.take(20)}, hasLoaded=$hasLoadedData")
        val token = authToken ?: return@LaunchedEffect
        if (!hasLoadedData) {
            android.util.Log.d("Profile", "🔄 Fetching latest survey for profile")
            surveyViewModel.loadExistingSurvey(token)
        }
        healthConnectViewModel.checkConnectionStatus()
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Background Image
        Image(
            painter = painterResource(id = R.drawable.background),
            contentDescription = "Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Pull-to-refresh wrapper
        SwipeRefresh(
            state = swipeRefreshState,
            onRefresh = {
                android.util.Log.d("Profile", "🔄 Swipe refresh triggered!")
                authToken?.let { token ->
                    android.util.Log.d("Profile", "🔄 Reloading survey data...")
                    surveyViewModel.loadExistingSurvey(token)
                }
                healthConnectViewModel.checkConnectionStatus()
                healthConnectViewModel.fetchHealthData()
            }
        ) {
            // Scrollable Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
            Spacer(modifier = Modifier.height(16.dp))

            // User Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0x60FFFFFF))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    val userName = currentUser?.let { "${it.firstName} ${it.lastName}" } ?: "Loading..."
                    val age by surveyViewModel.age.collectAsState()
                    val heightCm by surveyViewModel.heightCm.collectAsState()
                    val weightKg by surveyViewModel.weightKg.collectAsState()
                    val sex by surveyViewModel.sex.collectAsState()
                    val bmi = if (heightCm > 0) weightKg / ((heightCm / 100.0) * (heightCm / 100.0)) else 0.0
                    val bmiStr = String.format("%.1f", bmi)
                    val bmiCategory = when {
                        bmi < 18.5 -> "Underweight"
                        bmi < 25 -> "Normal"
                        bmi < 30 -> "Overweight"
                        else -> "Obese"
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Back button
                        IconButton(
                            onClick = { navController.navigateUp() },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // Profile Avatar
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.logo),
                                contentDescription = "Profile",
                                modifier = Modifier.size(60.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Text(
                            text = userName,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // User Stats Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            ProfileInfoItem("Age", age.toString())
                            Spacer(modifier = Modifier.height(16.dp))
                            ProfileInfoItem("Sex", sex.replaceFirstChar { it.uppercase() })
                            Spacer(modifier = Modifier.height(16.dp))
                            ProfileInfoItem("Height", String.format("%.0f cm", heightCm))
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            ProfileInfoItem("Weight", String.format("%.1f kg", weightKg))
                            Spacer(modifier = Modifier.height(16.dp))
                            ProfileInfoItem("BMI", bmiStr)
                            Spacer(modifier = Modifier.height(16.dp))
                            ProfileInfoItem("BMI Category", bmiCategory)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Health & Lifestyle Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0x60FFFFFF))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Text(
                        text = "Health & Lifestyle",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    val hypertension by surveyViewModel.hypertension.collectAsState()
                    val diabetes by surveyViewModel.diabetes.collectAsState()
                    val smokes by surveyViewModel.smokes.collectAsState()
                    val alcohol by surveyViewModel.alcohol.collectAsState()
                    val dailySteps by surveyViewModel.dailySteps.collectAsState()
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            HealthInfoItem("Hypertensive:", if (hypertension) "Yes" else "No")
                            Spacer(modifier = Modifier.height(12.dp))
                            HealthInfoItem("Diabetic:", if (diabetes) "Yes" else "No")
                            Spacer(modifier = Modifier.height(12.dp))
                            HealthInfoItem("Smokes:", if (smokes) "Yes" else "No")
                            Spacer(modifier = Modifier.height(12.dp))
                            HealthInfoItem("Alcohol:", if (alcohol) "Yes" else "No")
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            HealthInfoItem("Step Count:", dailySteps.toString())
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sleep Metrics Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0x60FFFFFF))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Text(
                        text = "Sleep Metrics",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    val scores = submissionResult?.scores
                    
                    if (scores != null) {
                        SleepMetricItem(
                            title = "Berlin Score:",
                            value = "${scores.berlin.score} - ${scores.berlin.category}"
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        SleepMetricItem(
                            title = "STOP-BANG Score:",
                            value = "${scores.stopbang.score}/8 - ${scores.stopbang.category}"
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        SleepMetricItem(
                            title = "Epworth Sleepiness Scale (ESS):",
                            value = "${scores.ess.score}/24 - ${scores.ess.category}"
                        )
                    } else {
                        Text(
                            text = "Complete the questionnaire to see your sleep metrics",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Health Connect Profile Card
            if (isHealthConnected) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0x60FFFFFF))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        Text(
                            text = "Health Connect Profile",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                HealthInfoItem(
                                    "Active Calories:",
                                    healthData?.let { String.format("%.1f kcal", it.activeCaloriesBurned) } ?: "--"
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                HealthInfoItem(
                                    "Activity Intensity:",
                                    healthData?.activityIntensity ?: "--"
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                HealthInfoItem(
                                    "Weight:",
                                    healthData?.let { 
                                        if (it.weightKg > 0) String.format("%.1f kg", it.weightKg) else "--"
                                    } ?: "--"
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                HealthInfoItem(
                                    "Total Calories:",
                                    healthData?.let { String.format("%.1f kcal", it.totalCaloriesBurned) } ?: "--"
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                HealthInfoItem(
                                    "Height:",
                                    healthData?.let { 
                                        if (it.heightCm > 0) String.format("%.0f cm", it.heightCm) else "--"
                                    } ?: "--"
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                HealthInfoItem(
                                    "Distance:",
                                    healthData?.let { 
                                        if (it.distanceMeters > 0) String.format("%.2f km", it.distanceMeters / 1000.0) else "--"
                                    } ?: "--"
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Health Connect Data Status Message
            if (isHealthConnected && (healthData == null || (healthData!!.averageDailySteps == 0 && healthData!!.sleepDurationHours == 0.0))) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0x60FFFFFF))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "📊 Health Connect Data",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "No activity data found",
                            fontSize = 16.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "To see your sleep and step data:",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "1. Open a health tracking app",
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "2. Ensure it's writing data to Health Connect",
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "3. Wait a few hours for data to sync",
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Button(
                            onClick = { 
                                healthConnectViewModel.fetchHealthData()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4CAF50)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Text(
                                text = "🔄 Refresh Data",
                                fontSize = 16.sp,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Weekly Sleep Chart from Health Connect
            if (isHealthConnected && healthData != null && (healthData!!.averageDailySteps > 0 || healthData!!.sleepDurationHours > 0.0)) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0x60FFFFFF))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Weekly Sleep Pattern",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Sync",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Sleep hours bar chart
                        SleepBarChart(
                            sleepData = healthData!!.weeklySleepData,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Average sleep display
                        Text(
                            text = "Average: ${String.format("%.1f", healthData!!.sleepDurationHours)} hours/night",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.9f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Weekly Steps Chart from Health Connect
            if (isHealthConnected && healthData != null && (healthData!!.averageDailySteps > 0 || healthData!!.sleepDurationHours > 0.0)) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0x60FFFFFF))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Daily Steps",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Sync",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Steps bar chart
                        StepsBarChart(
                            stepsData = healthData!!.weeklyStepsData,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Average steps display
                        Text(
                            text = "Average: ${healthData!!.averageDailySteps} steps/day",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.9f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun SleepBarChart(
    sleepData: Map<String, Double>,
    modifier: Modifier = Modifier
) {
    val sortedData = sleepData.entries.sortedBy { it.key }.takeLast(7)
    // Dynamically calculate max sleep based on actual data with 20% padding
    val maxDataValue = sortedData.maxOfOrNull { it.value } ?: 10.0
    val maxSleep = kotlin.math.max(10.0, maxDataValue * 1.2) // At least 10 hours scale, or 120% of max data
    
    Canvas(modifier = modifier) {
        val barWidth = size.width / (sortedData.size * 2)
        val spacing = barWidth
        
        sortedData.forEachIndexed { index, entry ->
            val barHeight = ((entry.value / maxSleep * size.height * 0.9f).toFloat()).coerceAtMost(size.height * 0.9f)
            val x = (index * (barWidth + spacing) + spacing / 2).toFloat()
            val y = (size.height - barHeight).toFloat()
            
            // Draw bar
            drawRoundRect(
                color = Color(0xFF6B8DD6),
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(8f, 8f)
            )
            
            // Draw value text
            val text = String.format("%.1fh", entry.value)
            val valuePaint = android.graphics.Paint().apply {
                color = android.graphics.Color.WHITE
                textSize = 24f
                textAlign = android.graphics.Paint.Align.CENTER
            }
            drawContext.canvas.nativeCanvas.drawText(text, x + barWidth / 2, y - 10f, valuePaint)
            
            // Draw day label
            val dayLabel = entry.key.substring(8, 10) // Get day number
            val dayPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.WHITE
                textSize = 28f
                textAlign = android.graphics.Paint.Align.CENTER
                alpha = 180
            }
            drawContext.canvas.nativeCanvas.drawText(dayLabel, x + barWidth / 2, size.height + 30f, dayPaint)
        }
    }
}

@Composable
fun StepsBarChart(
    stepsData: Map<String, Int>,
    modifier: Modifier = Modifier
) {
    val sortedData = stepsData.entries.sortedBy { it.key }.takeLast(7)
    // Dynamically calculate max steps based on actual data with 20% padding
    val maxDataValue = sortedData.maxOfOrNull { it.value } ?: 10000
    val maxSteps = kotlin.math.max(10000, (maxDataValue * 1.2).toInt()) // At least 10k scale, or 120% of max data
    
    Canvas(modifier = modifier) {
        val barWidth = size.width / (sortedData.size * 2)
        val spacing = barWidth
        
        sortedData.forEachIndexed { index, entry ->
            val barHeight = ((entry.value.toFloat() / maxSteps * size.height * 0.9f).coerceAtMost(size.height * 0.9f))
            val x = (index * (barWidth + spacing) + spacing / 2).toFloat()
            val y = (size.height - barHeight).toFloat()
            
            // Draw bar
            drawRoundRect(
                color = Color(0xFF4CAF50),
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(8f, 8f)
            )
            
            // Draw value text
            val text = if (entry.value >= 1000) "${entry.value / 1000}k" else "${entry.value}"
            val valuePaint = android.graphics.Paint().apply {
                color = android.graphics.Color.WHITE
                textSize = 24f
                textAlign = android.graphics.Paint.Align.CENTER
            }
            drawContext.canvas.nativeCanvas.drawText(text, x + barWidth / 2, y - 10f, valuePaint)
            
            // Draw day label
            val dayLabel = entry.key.substring(8, 10) // Get day number
            val labelPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.WHITE
                textSize = 28f
                textAlign = android.graphics.Paint.Align.CENTER
                alpha = 180
            }
            drawContext.canvas.nativeCanvas.drawText(dayLabel, x + barWidth / 2, size.height + 30f, labelPaint)
        }
    }
}

@Composable
fun ProfileInfoItem(label: String, value: String) {
    Column {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color.White.copy(alpha = 0.8f)
        )
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
    }
}

@Composable
fun HealthInfoItem(label: String, value: String) {
    Column {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color.White.copy(alpha = 0.9f),
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
    }
}

@Composable
fun SleepMetricItem(title: String, value: String) {
    Column {
        Text(
            text = title,
            fontSize = 15.sp,
            color = Color.White.copy(alpha = 0.9f),
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            lineHeight = 22.sp
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewProfileScreen() {
    val navController = rememberNavController()
    ProfileScreen(navController)
}