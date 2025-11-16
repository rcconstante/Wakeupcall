package com.example.wakeupcallapp.sleepapp

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.wakeupcallapp.sleepapp.viewmodel.GoogleFitViewModel
import androidx.activity.ComponentActivity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.CornerRadius
import kotlinx.coroutines.launch
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

@Composable
fun DashboardScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel(viewModelStoreOwner = LocalContext.current as ComponentActivity),
    surveyViewModel: SurveyViewModel = viewModel(viewModelStoreOwner = LocalContext.current as ComponentActivity),
    googleFitViewModel: GoogleFitViewModel = viewModel(viewModelStoreOwner = LocalContext.current as ComponentActivity),
    onSignOut: () -> Unit = {}
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val authToken by authViewModel.authToken.collectAsState()
    val submissionResult by surveyViewModel.submissionResult.collectAsState()
    val isLoadingSurvey by surveyViewModel.isLoadingSurvey.collectAsState()
    val fitData by googleFitViewModel.fitData.collectAsState()
    val isFitConnected by googleFitViewModel.isConnected.collectAsState()
    var isDrawerOpen by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = isLoadingSurvey)
    
    val hasLoadedData by surveyViewModel.hasLoadedData.collectAsState()
    
    // Load survey data and Google Fit data when an auth token becomes available
    LaunchedEffect(authToken, hasLoadedData, currentUser) {
        android.util.Log.d("Dashboard", "🎯 Dashboard LaunchedEffect - token=${authToken?.take(20)}, hasLoaded=$hasLoadedData")
        val token = authToken ?: return@LaunchedEffect
        
        // Set userId for Google Fit
        currentUser?.let { user ->
            googleFitViewModel.setUserId(user.id.toString())
            android.util.Log.d("Dashboard", "📋 Set Google Fit userId: ${user.id}")
        }
        
        if (!hasLoadedData) {
            android.util.Log.d("Dashboard", "🔄 Fetching latest survey for dashboard")
            surveyViewModel.loadExistingSurvey(token)
        }
        googleFitViewModel.checkConnectionStatus()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Background Image
        Image(
            painter = painterResource(id = R.drawable.background),
            contentDescription = "Dashboard Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Pull-to-refresh wrapper
        SwipeRefresh(
            state = swipeRefreshState,
            onRefresh = {
                android.util.Log.d("Dashboard", "🔄 Swipe refresh triggered!")
                authToken?.let { token ->
                    android.util.Log.d("Dashboard", "🔄 Reloading survey data...")
                    surveyViewModel.loadExistingSurvey(token)
                }
                googleFitViewModel.fetchFitnessData()
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

            // Menu Icon
            Card(
                modifier = Modifier
                    .size(48.dp)
                    .clickable { 
                        isDrawerOpen = true 
                    },
                shape = CircleShape,
                colors = CardDefaults.cardColors(containerColor = Color(0x80FFFFFF)),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Menu",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // User Profile Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0x60FFFFFF))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        // Profile Avatar
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.logo),
                                contentDescription = "Profile",
                                modifier = Modifier.size(35.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column {
                            val userName = currentUser?.let { "${it.firstName} ${it.lastName}" } ?: "Loading..."
                            val age by surveyViewModel.age.collectAsState()
                            val heightCm by surveyViewModel.heightCm.collectAsState()
                            val weightKg by surveyViewModel.weightKg.collectAsState()
                            val heightM = heightCm / 100.0
                            val heightMSquared = heightM * heightM
                            val bmi = if (heightCm > 0) String.format("%.1f", weightKg / heightMSquared) else "--"
                            val sex by surveyViewModel.sex.collectAsState()
                            val hypertension by surveyViewModel.hypertension.collectAsState()
                            val diabetes by surveyViewModel.diabetes.collectAsState()
                            
                            Text(
                                text = userName,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Age: $age    BMI: $bmi    ${if (hypertension) "Hypertensive" else ""}",
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                            Text(
                                text = "Sex: ${sex.uppercase()}       ${if (bmi.toDoubleOrNull()?.let { it > 30 } == true) "Obese" else ""}         ${if (diabetes) "Diabetic" else ""}",
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }

                    Icon(
                        painter = painterResource(android.R.drawable.ic_menu_more),
                        contentDescription = "More",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0x60FFFFFF))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Text(
                        text = "Risk Assessment Result",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    if (submissionResult != null && submissionResult?.success == true) {
                        val prediction = submissionResult?.prediction
                        val probability = prediction?.osaProbability ?: 0.0
                        val riskLevel = prediction?.riskLevel ?: "Unknown"
                        val certainty = String.format("%.0f%%", probability * 100)
                        
                        val riskColor = when {
                            riskLevel.contains("High", ignoreCase = true) -> Color(0xFFE53935)
                            riskLevel.contains("Low", ignoreCase = true) -> Color(0xFF1E88E5)
                            riskLevel.contains("Moderate", ignoreCase = true) -> Color(0xFFFB8C00)
                            else -> Color(0xFF757575)
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = certainty,
                                    fontSize = 52.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Certainty",
                                    fontSize = 16.sp,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(130.dp)
                                    .clip(CircleShape)
                                    .background(riskColor.copy(alpha = 0.3f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = riskLevel,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = riskColor
                                )
                            }
                        }
                    } else if (isLoadingSurvey || !hasLoadedData) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator(color = Color.White)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Loading your assessment...",
                                    fontSize = 16.sp,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }
                        }
                    } else {
                        Text(
                            text = "Complete the questionnaire to see your risk assessment",
                            fontSize = 16.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Key Factors Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0x60FFFFFF))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Key Factors",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Icon(
                            painter = painterResource(android.R.drawable.ic_menu_more),
                            contentDescription = "View All",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    val topRiskFactors = submissionResult?.topRiskFactors
                    if (topRiskFactors != null && topRiskFactors.isNotEmpty()) {
                        topRiskFactors.forEachIndexed { index, factor ->
                            val icon = when {
                                factor.factor.contains("BMI", ignoreCase = true) -> "👤"
                                factor.factor.contains("snor", ignoreCase = true) -> "😴"
                                factor.factor.contains("sleep", ignoreCase = true) -> "🛏️"
                                factor.factor.contains("age", ignoreCase = true) -> "🎂"
                                factor.factor.contains("neck", ignoreCase = true) -> "📏"
                                else -> "⚠️"
                            }
                            KeyFactorCard(
                                icon = icon,
                                title = factor.factor,
                                description = "Impact: ${factor.impact}"
                            )
                            if (index < topRiskFactors.size - 1) {
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }
                    } else {
                        // Default factors when no data available
                        KeyFactorCard(
                            icon = "👤",
                            title = "BMI",
                            description = "Complete questionnaire to see factors"
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        KeyFactorCard(
                            icon = "😴",
                            title = "Snoring",
                            description = ""
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        KeyFactorCard(
                            icon = "🛏️",
                            title = "Sleep Duration",
                            description = ""
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sleep & Lifestyle Overview Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0x60FFFFFF))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Sleep & Lifestyle Overview",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        if (isFitConnected) {
                            Text(
                                text = "Google Fit",
                                fontSize = 12.sp,
                                color = Color(0xFF4CAF50),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (fitData != null) {
                        // Daily Steps Chart
                        Text(
                            text = "Daily Steps",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        DashboardStepsChart(
                            stepsData = fitData!!.weeklyStepsData,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Avg: ${fitData!!.averageDailySteps} steps/day",
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        // Sleep Duration Chart
                        Text(
                            text = "Sleep Duration",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        DashboardSleepChart(
                            sleepData = fitData!!.weeklySleepData,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Avg: ${String.format("%.1f", fitData!!.sleepDurationHours)}h/night",
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .background(
                                    Color(0x30FFFFFF),
                                    RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = if (isFitConnected) "Loading data..." else "Connect Google Fit",
                                    fontSize = 14.sp,
                                    color = Color.White.copy(alpha = 0.6f)
                                )
                                if (!isFitConnected) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Visit Profile to connect",
                                        fontSize = 12.sp,
                                        color = Color.White.copy(alpha = 0.5f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Retake Questionnaire Button
            Button(
                onClick = { 
                    // Don't reset - keep existing data and let user modify it
                    navController.navigate("demographics")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0x60FFFFFF)),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text(
                    text = "Retake Questionnaire",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(80.dp))
            }
        }

        // Drawer Overlay - rendered on top
        if (isDrawerOpen) {
            DashboardDrawer(
                navController = navController,
                onSignOut = {
                    isDrawerOpen = false
                    onSignOut()
                },
                onClose = { isDrawerOpen = false }
            )
        }
    }
}

@Composable
fun KeyFactorCard(icon: String, title: String, description: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0x40FFFFFF))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0x40FFFFFF)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = icon,
                    fontSize = 24.sp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                if (description.isNotEmpty()) {
                    Text(
                        text = description,
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewDashboard() {
    val navController = rememberNavController()
    DashboardScreen(navController)
}

/**
 * Navigation drawer for dashboard menu
 */
@Composable
fun DashboardDrawer(
    navController: NavController,
    onSignOut: () -> Unit = {},
    onClose: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.4f))
    ) {
        // Close area - clicking outside drawer closes it
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { onClose() }
        ) {
            // Drawer Content
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(320.dp)
                    .align(Alignment.CenterStart)
                    .background(
                        Color(0xFFFFFFFF),
                        RoundedCornerShape(topEnd = 0.dp, bottomEnd = 0.dp)
                    )
                    .clickable(enabled = false) { } // Prevent click-through
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        // Drawer Title
                        Text(
                            text = "Menu",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2C3E50),
                            modifier = Modifier.padding(start = 16.dp, top = 48.dp, bottom = 32.dp)
                        )

                        // Menu Items
                        DrawerMenuItem(
                            emoji = "🏠",
                            label = "Dashboard",
                            onClick = {
                                navController.navigate("dashboard") {
                                    popUpTo("dashboard") { inclusive = true }
                                }
                                onClose()
                            }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        DrawerMenuItem(
                            emoji = "👤",
                            label = "Profile",
                            onClick = {
                                navController.navigate("profile")
                                onClose()
                            }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        DrawerMenuItem(
                            emoji = "💡",
                            label = "Recommendations",
                            onClick = {
                                navController.navigate("recommendations")
                                onClose()
                            }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        DrawerMenuItem(
                            emoji = "📊",
                            label = "Key Factors",
                            onClick = {
                                navController.navigate("key_factors")
                                onClose()
                            }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        DrawerMenuItem(
                            emoji = "📱",
                            label = "Data Sources",
                            onClick = {
                                navController.navigate("data_sources")
                                onClose()
                            }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        DrawerMenuItem(
                            emoji = "⚙️",
                            label = "Settings",
                            onClick = {
                                navController.navigate("settings")
                                onClose()
                            }
                        )
                    }

                    // Sign Out Button at bottom
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onClose()
                                onSignOut()
                            }
                            .padding(horizontal = 16.dp, vertical = 24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🚪",
                            fontSize = 20.sp,
                            modifier = Modifier.padding(end = 16.dp)
                        )
                        Text(
                            text = "Sign Out",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFE74C3C)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DrawerMenuItem(
    emoji: String,
    label: String,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = emoji,
            fontSize = 20.sp,
            modifier = Modifier.padding(end = 16.dp)
        )
        Text(
            text = label,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF2C3E50)
        )
    }
}

@Composable
fun DashboardStepsChart(
    stepsData: Map<String, Int>,
    modifier: Modifier = Modifier
) {
    val maxSteps = 15000
    val sortedData = stepsData.entries.sortedBy { it.key }.takeLast(7)
    
    Canvas(modifier = modifier) {
        if (sortedData.isEmpty()) return@Canvas
        
        val barWidth = size.width / (sortedData.size * 2)
        val spacing = barWidth
        
        sortedData.forEachIndexed { index, entry ->
            val barHeight = (entry.value.toFloat() / maxSteps * size.height).coerceAtMost(size.height)
            val x = index * (barWidth + spacing) + spacing / 2
            val y = size.height - barHeight
            
            drawRoundRect(
                color = Color(0xFF4CAF50),
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(8f, 8f)
            )
            
            val text = if (entry.value >= 1000) "${entry.value / 1000}k" else "${entry.value}"
            val valuePaint = android.graphics.Paint().apply {
                color = android.graphics.Color.WHITE
                textSize = 20f
                textAlign = android.graphics.Paint.Align.CENTER
            }
            drawContext.canvas.nativeCanvas.drawText(text, x + barWidth / 2, y - 8f, valuePaint)
            
            val dayLabel = entry.key.substring(8, 10)
            val labelPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.WHITE
                textSize = 22f
                textAlign = android.graphics.Paint.Align.CENTER
                alpha = 150
            }
            drawContext.canvas.nativeCanvas.drawText(dayLabel, x + barWidth / 2, size.height + 25f, labelPaint)
        }
    }
}

@Composable
fun DashboardSleepChart(
    sleepData: Map<String, Double>,
    modifier: Modifier = Modifier
) {
    val maxSleep = 10.0
    val sortedData = sleepData.entries.sortedBy { it.key }.takeLast(7)
    
    Canvas(modifier = modifier) {
        if (sortedData.isEmpty()) return@Canvas
        
        val barWidth = size.width / (sortedData.size * 2)
        val spacing = barWidth
        
        sortedData.forEachIndexed { index, entry ->
            val barHeight = (entry.value / maxSleep * size.height).toFloat()
            val x = index * (barWidth + spacing) + spacing / 2
            val y = size.height - barHeight
            
            drawRoundRect(
                color = Color(0xFF6B8DD6),
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(8f, 8f)
            )
            
            val text = String.format("%.1fh", entry.value)
            val valuePaint = android.graphics.Paint().apply {
                color = android.graphics.Color.WHITE
                textSize = 20f
                textAlign = android.graphics.Paint.Align.CENTER
            }
            drawContext.canvas.nativeCanvas.drawText(text, x + barWidth / 2, y - 8f, valuePaint)
            
            val dayLabel = entry.key.substring(8, 10)
            val labelPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.WHITE
                textSize = 22f
                textAlign = android.graphics.Paint.Align.CENTER
                alpha = 150
            }
            drawContext.canvas.nativeCanvas.drawText(dayLabel, x + barWidth / 2, size.height + 25f, labelPaint)
        }
    }
}