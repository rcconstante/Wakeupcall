package com.example.wakeupcallapp.sleepapp

import com.example.wakeupcallapp.sleepapp.R
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

class SplashScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            WakeUpCallScreen(
                onNext = { navController.navigate("info_consent") },
                onSkip = { navController.navigate("info_consent") }
            )
        }
    }
}

@Composable
fun WakeUpCallScreen(
    onNext: () -> Unit = {},
    onSkip: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "infinite")

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

        // Main content - Centered logo with title and subtitle
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo with floating animation
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .offset(y = floatingOffset.dp),
                contentAlignment = Alignment.Center
            ) {
                // Logo image
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Logo",
                    modifier = Modifier.size(400.dp),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Title
            Text(
                text = "Wake Up Call",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Subtitle
            Text(
                text = "Empowering healthier sleep through\nintelligence",
                fontSize = 16.sp,
                color = Color(0xFFE8EFFF),
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )
        }

        // Page indicators and next button at bottom
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(32.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Page indicators
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PageIndicator(isActive = true)
                PageIndicator(isActive = false)
                PageIndicator(isActive = false)
            }

            // Next button
            IconButton(
                onClick = onNext,
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        Color(0x40FFFFFF),
                        CircleShape
                    )
            ) {
                Icon(
                    painter = painterResource(android.R.drawable.ic_media_play),
                    contentDescription = "Next",
                    tint = Color.White,
                    modifier = Modifier
                        .size(24.dp)
                        .graphicsLayer(rotationZ = 0f)
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewWakeUpCallScreen() {
    WakeUpCallScreen()
}