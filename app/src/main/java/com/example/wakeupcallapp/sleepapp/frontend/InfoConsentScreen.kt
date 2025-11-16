package com.example.wakeupcallapp.sleepapp

import com.example.wakeupcallapp.sleepapp.R
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class InfoConsentScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            InfoConsentScreenContent()
        }
    }
}

@Composable
fun InfoConsentScreenContent(
    onNext: () -> Unit = {},
    authViewModel: com.example.wakeupcallapp.sleepapp.viewmodel.AuthViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        viewModelStoreOwner = androidx.compose.ui.platform.LocalContext.current as androidx.activity.ComponentActivity
    )
) {
    var termsChecked by remember { mutableStateOf(false) }
    var privacyChecked by remember { mutableStateOf(false) }

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
                .padding(horizontal = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(80.dp))

            // Logo with floating animation
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .offset(y = floatingOffset.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Logo",
                    modifier = Modifier.size(500.dp),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(35.dp))

            // Title
            Text(
                text = "Wake Up Call",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Description
            Text(
                text = "An AI-driven tool that analyzes your lifestyle and sleep patterns to assess sleep apnea risk and promote healthier sleep.",
                fontSize = 14.sp,
                color = Color(0xFFE8EFFF),
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Terms and Conditions Checkbox
            CheckboxItem(
                checked = termsChecked,
                onCheckedChange = { termsChecked = it },
                title = "Terms and Conditions",
                description = "This checkbox indicates that a user has read, understood, and agrees to app's legally binding rules."
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Privacy Policy Checkbox
            CheckboxItem(
                checked = privacyChecked,
                onCheckedChange = { privacyChecked = it },
                title = "Privacy policy",
                description = "This checkbox signifies a user's consent to the app's data collection and handling practices."
            )

            Spacer(modifier = Modifier.weight(1f))

            // Page indicators and next button at bottom
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Page indicators
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PageIndicator(isActive = false)
                    PageIndicator(isActive = true)
                    PageIndicator(isActive = false)
                }

                // Next button
                IconButton(
                    onClick = {
                        authViewModel.acceptInfoConsent()
                        android.util.Log.d("InfoConsentScreen", "✅ Info consent accepted")
                        onNext()
                    },
                    enabled = termsChecked && privacyChecked,
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            color = if (termsChecked && privacyChecked)
                                Color(0x60FFFFFF)
                            else
                                Color(0x30FFFFFF),
                            shape = CircleShape
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
}

@Composable
fun CheckboxItem(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) },
        verticalAlignment = Alignment.Top
    ) {
        // Custom Checkbox
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(
                    if (checked) Color.White else Color.Transparent
                )
                .border(
                    width = 2.dp,
                    color = if (checked) Color.White else Color(0x80FFFFFF),
                    shape = RoundedCornerShape(4.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (checked) {
                Icon(
                    painter = painterResource(android.R.drawable.ic_menu_save),
                    contentDescription = "Checked",
                    tint = Color(0xFF6B8DD6),
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Text content
        Column {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White

            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                fontSize = 12.sp,
                color = Color(0xB3FFFFFF),
                lineHeight = 16.sp
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewInfoConsentScreen() {
    InfoConsentScreenContent()
}