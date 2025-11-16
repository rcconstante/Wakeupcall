package com.example.wakeupcallapp.sleepapp

import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.wakeupcallapp.sleepapp.R
import com.example.wakeupcallapp.sleepapp.viewmodel.GoogleFitViewModel
import com.example.wakeupcallapp.sleepapp.viewmodel.AuthViewModel

@Composable
fun DataSourcesScreen(
    navController: NavController,
    googleFitViewModel: GoogleFitViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current
    val isGoogleFitConnected by googleFitViewModel.isConnected.collectAsState()
    val fitData by googleFitViewModel.fitData.collectAsState()
    val isLoading by googleFitViewModel.isLoading.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
    
    // Check connection status when screen loads
    LaunchedEffect(currentUser) {
        // Set userId for Google Fit
        currentUser?.let { user ->
            googleFitViewModel.setUserId(user.id.toString())
        }
        
        googleFitViewModel.checkConnectionStatus()
        
        // Set up callback for permission result
        (context as? MainActivity)?.let { activity ->
            MainActivity.onGoogleFitPermissionResult = { granted ->
                googleFitViewModel.onPermissionResult(granted)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Background Image
        Image(
            painter = painterResource(id = R.drawable.background),
            contentDescription = "Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Header with back button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { navController.navigateUp() },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = "Data Sources",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Google Fit Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            // Google Fit Icon
                            Card(
                                modifier = Modifier.size(50.dp),
                                shape = CircleShape,
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "❤️",
                                        fontSize = 28.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column {
                                Text(
                                    text = "Google Fit",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = if (isGoogleFitConnected) {
                                        fitData?.let { 
                                            "${it.dailySteps} steps today"
                                        } ?: "Connected"
                                    } else {
                                        "Steps, Sleep, Activity"
                                    },
                                    fontSize = 14.sp,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }

                        // Disconnect/Connect Button
                        Button(
                            onClick = {
                                if (isGoogleFitConnected) {
                                    googleFitViewModel.disconnect()
                                } else {
                                    // Request permissions
                                    (context as? MainActivity)?.let { activity ->
                                        googleFitViewModel.getManager().requestPermissions(activity)
                                    }
                                }
                            },
                            enabled = !isLoading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isGoogleFitConnected)
                                    Color.White.copy(alpha = 0.9f)
                                else
                                    Color(0xFF6B8DD6)
                            ),
                            shape = RoundedCornerShape(20.dp),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = if (isGoogleFitConnected) Color(0xFF6B8DD6) else Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = if (isGoogleFitConnected) "Disconnect" else "Connect",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isGoogleFitConnected)
                                        Color(0xFF6B8DD6)
                                    else
                                        Color.White
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Description
                    Text(
                        text = "Connect app with your data sources for more accurate sleep tracking and personalized insights.",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.9f),
                        lineHeight = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewDataSourcesScreen() {
    val navController = rememberNavController()
    DataSourcesScreen(navController)
}