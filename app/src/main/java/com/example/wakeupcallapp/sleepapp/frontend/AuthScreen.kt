package com.example.wakeupcallapp.sleepapp

import android.os.Bundle
import com.example.wakeupcallapp.sleepapp.R
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

class AuthScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AuthScreenContent()
        }
    }
}

@Composable
fun AuthScreenContent(
    onLogIn: () -> Unit = {},
    onSignUp: () -> Unit = {},
    onGuest: () -> Unit = {}
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

        // Main content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp)
                .padding(top = 40.dp), // 👈 shifts everything slightly downward
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo with floating animation
            Box(
                modifier = Modifier
                    .size(300.dp)
                    .offset(y = floatingOffset.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Logo",
                    modifier = Modifier.size(250.dp),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(60.dp))

            // Log In Button
            Button(
                onClick = onLogIn,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0x40FFFFFF)
                ),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text(
                    text = "Log In",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Create an Account Button
            Button(
                onClick = onSignUp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0x40FFFFFF)
                ),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text(
                    text = "Create an Account",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // OR Divider
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(1.dp)
                        .background(Color(0x60FFFFFF))
                )
                Text(
                    text = "OR",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(1.dp)
                        .background(Color(0x60FFFFFF))
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Guest Button
            Button(
                onClick = onGuest,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0x30FFFFFF)
                ),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text(
                    text = "Guest",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.weight(1f))

        // Page indicators at bottom
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                PageIndicator(isActive = false)
                Spacer(modifier = Modifier.width(8.dp))
                PageIndicator(isActive = false)
                Spacer(modifier = Modifier.width(8.dp))
                PageIndicator(isActive = true)
            }
        }
    }
}

@Composable
fun PageIndicator(isActive: Boolean) {
    Box(
        modifier = Modifier
            .size(if (isActive) 10.dp else 8.dp)
            .background(
                color = if (isActive) Color.White else Color(0x60FFFFFF),
                shape = CircleShape
            )
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewAuthScreen() {
    AuthScreenContent()
}