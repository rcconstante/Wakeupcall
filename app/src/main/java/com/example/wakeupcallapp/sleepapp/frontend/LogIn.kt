package com.example.wakeupcallapp.sleepapp

import android.os.Bundle
import com.example.wakeupcallapp.sleepapp.R
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

class LogIn : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LogInScreenContent()
        }
    }
}

@Composable
fun LogInScreenContent(
    onSignInSuccess: (Boolean, Boolean) -> Unit = { _, _ -> },
    onBack: () -> Unit = {},
    authViewModel: com.example.wakeupcallapp.sleepapp.viewmodel.AuthViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        viewModelStoreOwner = LocalContext.current as ComponentActivity
    ),
    surveyViewModel: com.example.wakeupcallapp.sleepapp.viewmodel.SurveyViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        viewModelStoreOwner = LocalContext.current as ComponentActivity
    )
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    
    val isLoading by authViewModel.isLoading.collectAsState()
    val error by authViewModel.errorMessage.collectAsState()
    val authToken by authViewModel.authToken.collectAsState()
    
    // Show error if present
    LaunchedEffect(error) {
        if (error != null) {
            errorMessage = error!!
            showError = true
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

        // Main content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Robot Illustration
            Box(
                modifier = Modifier
                    .size(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Sleeping Robot",
                    modifier = Modifier.size(180.dp),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Card container
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0x50FFFFFF))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Email Field
                    Text(
                        text = "Email",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0x40FFFFFF),
                            unfocusedContainerColor = Color(0x40FFFFFF),
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Color.White
                        ),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Password Field
                    Text(
                        text = "Password",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        visualTransformation = PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0x40FFFFFF),
                            unfocusedContainerColor = Color(0x40FFFFFF),
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Color.White
                        ),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Error message
                    if (showError) {
                        Text(
                            text = errorMessage,
                            color = Color(0xFFFF6B6B),
                            fontSize = 14.sp,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Sign In Button
                    Button(
                        onClick = {
                            if (email.isNotBlank() && password.isNotBlank()) {
                                authViewModel.signIn(
                                    email = email,
                                    password = password,
                                    onSuccess = {
                                        surveyViewModel.resetSurveyStateForNewSession()
                                        android.util.Log.d("LogInScreen", "✅ Login successful!")
                                        android.util.Log.d("LogInScreen", "🔄 Navigating to Dashboard - it will load survey data")
                                        // DON'T load survey here - let Dashboard handle it on mount
                                        // Pass hasSurvey flag to navigation
                                        onSignInSuccess(authViewModel.hasSurvey.value, authViewModel.hasAcceptedInfoConsent.value)
                                    },
                                    onError = { error ->
                                        errorMessage = error
                                        showError = true
                                    }
                                )
                            } else {
                                errorMessage = "Please fill in all fields"
                                showError = true
                            }
                        },
                        enabled = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0x40FFFFFF)),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Text(
                                text = "Sign In",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Forgot Password
                    Text(
                        text = "Forgot Password?",
                        fontSize = 14.sp,
                        color = Color.White,
                        modifier = Modifier.clickable { /* Handle password reset */ }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewLogInScreen() {
    LogInScreenContent()
}
