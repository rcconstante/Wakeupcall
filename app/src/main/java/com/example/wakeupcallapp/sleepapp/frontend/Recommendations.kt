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
import com.example.wakeupcallapp.sleepapp.viewmodel.AuthViewModel
import androidx.activity.ComponentActivity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import android.widget.Toast
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.wakeupcallapp.sleepapp.utils.PDFReportGenerator
import android.util.Log

@Composable
fun RecommendationsScreen(
    navController: NavController,
    surveyViewModel: SurveyViewModel = viewModel(viewModelStoreOwner = LocalContext.current as ComponentActivity),
    authViewModel: AuthViewModel = viewModel(viewModelStoreOwner = LocalContext.current as ComponentActivity)
) {
    val submissionResult by surveyViewModel.submissionResult.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
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

            // Main Card
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
                    // Back button and Title
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
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Insights & Recommendations",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Personalized insights based on your health profile.",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    val recommendation = submissionResult?.prediction?.recommendation
                    if (recommendation != null && recommendation.isNotEmpty()) {
                        val recommendations = recommendation.split(" | ")
                        recommendations.forEachIndexed { index, rec ->
                            // Parse recommendation format: "Title: Description [Source]"
                            val titleEnd = rec.indexOf(":")
                            val sourceStart = rec.lastIndexOf("[")
                            val sourceEnd = rec.lastIndexOf("]")
                            
                            val title = if (titleEnd > 0) rec.substring(0, titleEnd).trim() else rec.take(50)
                            val description = if (titleEnd > 0 && sourceStart > titleEnd) {
                                rec.substring(titleEnd + 1, sourceStart).trim()
                            } else if (titleEnd > 0) {
                                rec.substring(titleEnd + 1).trim()
                            } else {
                                ""
                            }
                            val source = if (sourceStart > 0 && sourceEnd > sourceStart) {
                                rec.substring(sourceStart + 1, sourceEnd).trim()
                            } else {
                                ""
                            }
                            
                            // Determine icon based on title/content
                            val icon = when {
                                title.contains("HIGH RISK", ignoreCase = true) || title.contains("High Risk", ignoreCase = true) -> "🚨"
                                title.contains("INTERMEDIATE RISK", ignoreCase = true) || title.contains("Intermediate Risk", ignoreCase = true) || title.contains("MODERATE RISK", ignoreCase = true) || title.contains("Moderate Risk", ignoreCase = true) -> "⚠️"
                                title.contains("LOW RISK", ignoreCase = true) || title.contains("Low Risk", ignoreCase = true) -> "✅"
                                title.contains("Weight", ignoreCase = true) || title.contains("BMI", ignoreCase = true) -> "🎯"
                                title.contains("Alcohol", ignoreCase = true) -> "🍷"
                                title.contains("Smok", ignoreCase = true) -> "🚭"
                                title.contains("Sleep", ignoreCase = true) || title.contains("Bed", ignoreCase = true) -> "😴"
                                title.contains("Pressure", ignoreCase = true) || title.contains("Hypertension", ignoreCase = true) -> "🩺"
                                title.contains("Diabetes", ignoreCase = true) -> "💊"
                                title.contains("Age", ignoreCase = true) -> "👴"
                                title.contains("Neck", ignoreCase = true) -> "📐"
                                title.contains("Snor", ignoreCase = true) -> "💤"
                                title.contains("Activity", ignoreCase = true) || title.contains("Exercise", ignoreCase = true) -> "🏃"
                                title.contains("Cardiovascular", ignoreCase = true) || title.contains("Heart", ignoreCase = true) -> "❤️"
                                title.contains("STOP-BANG", ignoreCase = true) || title.contains("STOPBANG", ignoreCase = true) -> "📊"
                                title.contains("Sleepiness", ignoreCase = true) || title.contains("Daytime", ignoreCase = true) -> "😪"
                                title.contains("Breathing", ignoreCase = true) || title.contains("Airway", ignoreCase = true) -> "🫁"
                                else -> "💡"
                            }
                            
                            RecommendationItem(
                                icon = icon,
                                title = title,
                                description = description,
                                source = source
                            )
                            if (index < recommendations.size - 1) {
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }
                    } else {
                        Text(
                            text = "Complete the questionnaire to see personalized recommendations",
                            fontSize = 16.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Download Summary Button
            Button(
                onClick = {
                    coroutineScope.launch {
                        try {
                            val user = currentUser
                            val result = submissionResult
                            
                            if (user == null) {
                                Toast.makeText(context, "Please log in to download report", Toast.LENGTH_SHORT).show()
                                return@launch
                            }
                            
                            if (result == null) {
                                Toast.makeText(context, "No survey data available", Toast.LENGTH_SHORT).show()
                                return@launch
                            }
                            
                            Log.d("RecommendationsScreen", "📄 Downloading report from server...")
                            Toast.makeText(context, "Generating report...", Toast.LENGTH_SHORT).show()
                            
                            val authToken = authViewModel.authToken.value
                            if (authToken == null) {
                                Toast.makeText(context, "Authentication required", Toast.LENGTH_SHORT).show()
                                return@launch
                            }
                            
                            // Call backend API to generate report
                            surveyViewModel.downloadReport(authToken) { file ->
                                if (file != null) {
                                    Log.d("RecommendationsScreen", "✅ Report downloaded: ${file.absolutePath}")
                                    
                                    // Share the file
                                    val uri = FileProvider.getUriForFile(
                                        context,
                                        "${context.packageName}.fileprovider",
                                        file
                                    )
                                    
                                    // Determine MIME type based on file extension
                                    val mimeType = when {
                                        file.name.endsWith(".pdf", ignoreCase = true) -> "application/pdf"
                                        file.name.endsWith(".docx", ignoreCase = true) -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                                        else -> "*/*"
                                    }
                                    
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = mimeType
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        putExtra(Intent.EXTRA_SUBJECT, "WakeUpCall Sleep Apnea Report")
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    
                                    context.startActivity(Intent.createChooser(shareIntent, "Share Report"))
                                    Toast.makeText(context, "Report generated successfully!", Toast.LENGTH_LONG).show()
                                } else {
                                    Toast.makeText(context, "Failed to generate report", Toast.LENGTH_LONG).show()
                                }
                            }
                            
                        } catch (e: Exception) {
                            Log.e("RecommendationsScreen", "❌ Error: ${e.message}", e)
                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0x60FFFFFF)),
                shape = RoundedCornerShape(30.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0x40FFFFFF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "⬇",
                            fontSize = 20.sp,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = "Download Summary",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun RecommendationItem(
    icon: String,
    title: String,
    description: String,
    source: String = ""
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0x40FFFFFF))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
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
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    lineHeight = 18.sp
                )
                if (source.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Source: $source",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.6f),
                        lineHeight = 14.sp,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewRecommendationsScreen() {
    RecommendationsScreen(navController = rememberNavController())
}