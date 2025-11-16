package com.example.wakeupcallapp.sleepapp

import android.os.Bundle
import com.example.wakeupcallapp.sleepapp.R
import androidx.activity.ComponentActivity
import androidx.compose.runtime.LaunchedEffect
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wakeupcallapp.sleepapp.viewmodel.SurveyViewModel

class DataConsentScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DataConsentScreenContent()
        }
    }
}

@Composable
fun DataConsentScreenContent(
    onGrantPermission: () -> Unit = {},
    onDeny: () -> Unit = {},
    surveyViewModel: SurveyViewModel = viewModel(viewModelStoreOwner = LocalContext.current as ComponentActivity),
    googleFitViewModel: com.example.wakeupcallapp.sleepapp.viewmodel.GoogleFitViewModel = viewModel(viewModelStoreOwner = LocalContext.current as ComponentActivity),
    authViewModel: com.example.wakeupcallapp.sleepapp.viewmodel.AuthViewModel = viewModel(viewModelStoreOwner = LocalContext.current as ComponentActivity)
) {
    val context = LocalContext.current
    val infiniteTransition = rememberInfiniteTransition(label = "infinite")
    val currentUser by authViewModel.currentUser.collectAsState()

    LaunchedEffect(currentUser) {
        surveyViewModel.prepareForNewSurvey()
        
        // Set userId for Google Fit
        currentUser?.let { user ->
            googleFitViewModel.setUserId(user.id.toString())
        }
        
        // Set up callback for permission result
        (context as? MainActivity)?.let { activity ->
            MainActivity.onGoogleFitPermissionResult = { granted ->
                googleFitViewModel.onPermissionResult(granted)
                // Navigate to next screen after permission handling
                if (granted) {
                    onGrantPermission()
                }
            }
        }
    }

    // Floating animation for logo
    val floatingOffset by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floating"
    )

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

        // Main content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Card container
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0x50FFFFFF)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Logo with floating animation and circular background
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .offset(y = floatingOffset.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Circular background
                        Box(
                            modifier = Modifier
                                .size(140.dp)
                                .background(
                                    color = Color(0x40FFFFFF),
                                    shape = CircleShape
                                )
                        )

                        // Logo
                        Image(
                            painter = painterResource(id = R.drawable.logo),
                            contentDescription = "Logo",
                            modifier = Modifier.size(100.dp),
                            contentScale = ContentScale.Fit
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Title
                    Text(
                        text = "Connect to Google Fit",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Description
                    Text(
                        text = "Allow WakeUp Call to access your activity and health data from Google Fit.",
                        fontSize = 15.sp,
                        color = Color(0xFFE8EFFF),
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Grant Permission Button
                    Button(
                        onClick = {
                            // Request Google Fit permissions
                            (context as? MainActivity)?.let { activity ->
                                googleFitViewModel.getManager().requestPermissions(activity)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0x50FFFFFF)
                        ),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        Text(
                            text = "Grant Permission",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Deny Button
                    Button(
                        onClick = onDeny,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0x30FFFFFF)
                        ),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        Text(
                            text = "Deny",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Footer text
                    Text(
                        text = "Permission request sent.\nCheck your Google Fit popup.",
                        fontSize = 13.sp,
                        color = Color(0xCCFFFFFF),
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewDataConsentScreen() {
    DataConsentScreenContent()
}