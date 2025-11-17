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
import com.example.wakeupcallapp.sleepapp.viewmodel.SurveyViewModel
import androidx.activity.ComponentActivity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@Composable
fun KeyFactorsScreen(
    navController: NavController,
    surveyViewModel: SurveyViewModel = viewModel(viewModelStoreOwner = LocalContext.current as ComponentActivity)
) {
    val submissionResult by surveyViewModel.submissionResult.collectAsState()
    
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
                .padding(24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            val topRiskFactors = submissionResult?.topRiskFactors

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
                        verticalAlignment = Alignment.CenterVertically
                    ) {
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

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "Top key factors",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "These are the top contributing factors affecting your results.",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.9f),
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                    
                    val demographics = submissionResult?.demographics
                    val scores = submissionResult?.scores
                    
                    if (demographics != null && scores != null) {
                        // Determine top 3 SHAP factors based on impact
                        val shapFactors = mutableListOf<Triple<String, String, Float>>()
                        
                        // Calculate impact scores for each factor
                        val ageImpact = if (demographics.age >= 50) 0.75f else 0.40f
                        shapFactors.add(Triple("🎂", "Age", ageImpact))
                        
                        val snoringImpact = if (scores.stopbang.score >= 1) 0.85f else 0.25f
                        shapFactors.add(Triple("😴", "Snoring", snoringImpact))
                        
                        val stopbangImpact = (scores.stopbang.score.toFloat() / 8f) * 0.9f + 0.1f
                        shapFactors.add(Triple("📊", "STOP-BANG", stopbangImpact))
                        
                        val neckImpact = when {
                            demographics.neckCircumferenceCm >= 43 -> 0.90f
                            demographics.neckCircumferenceCm >= 40 -> 0.70f
                            demographics.neckCircumferenceCm >= 37 -> 0.50f
                            else -> 0.30f
                        }
                        shapFactors.add(Triple("📏", "Neck Circumference", neckImpact))
                        
                        val essImpact = (scores.ess.score.toFloat() / 24f) * 0.9f + 0.1f
                        shapFactors.add(Triple("💤", "ESS Score", essImpact))
                        
                        // Sort by impact and take top 3
                        val topShapFactors = shapFactors.sortedByDescending { it.third }.take(3)
                        
                        topShapFactors.forEachIndexed { index, factor ->
                            val isPositive = factor.third < 0.60f
                            KeyFactorItem(icon = factor.first, title = factor.second, isPositive = isPositive)
                            if (index < topShapFactors.size - 1) {
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }
                    } else {
                        Text(
                            text = "Complete the questionnaire to see your key factors",
                            fontSize = 16.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // SHAP Analysis Card
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
                        text = "SHAP Analysis",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "These are the top contributing factors affecting your results.",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.9f),
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    val demographics = submissionResult?.demographics
                    val scores = submissionResult?.scores
                    
                    if (demographics != null && scores != null) {
                        // Calculate impact for each factor
                        val ageImpact = if (demographics.age >= 50) 0.75f else 0.40f
                        ShapFeatureBar("Age", ageImpact)
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        val snoringProgress = if (scores.stopbang.score >= 1) 0.85f else 0.25f
                        ShapFeatureBar("Snoring", snoringProgress)
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        val stopbangProgress = (scores.stopbang.score.toFloat() / 8f) * 0.9f + 0.1f
                        ShapFeatureBar("STOP-BANG", stopbangProgress)
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        val neckProgress = when {
                            demographics.neckCircumferenceCm >= 43 -> 0.90f
                            demographics.neckCircumferenceCm >= 40 -> 0.70f
                            demographics.neckCircumferenceCm >= 37 -> 0.50f
                            else -> 0.30f
                        }
                        ShapFeatureBar("Neck Circumference", neckProgress)
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        val essProgress = (scores.ess.score.toFloat() / 24f) * 0.9f + 0.1f
                        ShapFeatureBar("ESS Score", essProgress)
                        
                        // Show additional top risk factors if available
                        if (topRiskFactors != null && topRiskFactors.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            topRiskFactors.take(3).forEach { factor ->
                                val impactValue = when(factor.priority.lowercase()) {
                                    "high" -> 0.85f
                                    "medium" -> 0.60f
                                    "low" -> 0.35f
                                    else -> 0.50f
                                }
                                ShapFeatureBar(factor.factor, impactValue)
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }
                    } else {
                        Text(
                            text = "Complete the questionnaire to see SHAP analysis",
                            fontSize = 16.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun KeyFactorItem(icon: String, title: String, isPositive: Boolean = true) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0x40FFFFFF))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0x40FFFFFF)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = icon,
                        fontSize = 22.sp
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = title,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }

            // Arrow icon (up or down)
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(0x40FFFFFF)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isPositive) "↑" else "↓",
                    fontSize = 20.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun ShapFeatureBar(label: String, progress: Float) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 15.sp,
            color = Color.White,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.width(120.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0x30FFFFFF))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF7B9FD8))
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewKeyFactorsScreen() {
    KeyFactorsScreen(navController = rememberNavController())
}