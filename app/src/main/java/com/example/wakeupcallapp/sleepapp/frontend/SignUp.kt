package com.example.wakeupcallapp.sleepapp

import android.os.Bundle
import com.example.wakeupcallapp.sleepapp.R
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

class SignUp : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SignUpScreenContent()
        }
    }
}

@Composable
fun SignUpScreenContent(
    onSignUpSuccess: (Boolean, Boolean) -> Unit = { _, _ -> },
    onBack: () -> Unit = {},
    authViewModel: com.example.wakeupcallapp.sleepapp.viewmodel.AuthViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        viewModelStoreOwner = LocalContext.current as ComponentActivity
    ),
    surveyViewModel: com.example.wakeupcallapp.sleepapp.viewmodel.SurveyViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        viewModelStoreOwner = LocalContext.current as ComponentActivity
    )
) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
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

        // Scrollable column
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Robot Icon
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Robot Icon",
                modifier = Modifier
                    .size(100.dp)
                    .padding(bottom = 12.dp),
                contentScale = ContentScale.Fit
            )

            // Card
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
                    // Title
                    Text(
                        text = "Create an Account",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // First Name
                    TextFieldLabel("First Name")
                    GlassTextField(value = firstName, onValueChange = { firstName = it })
                    Spacer(modifier = Modifier.height(16.dp))

                    // Last Name
                    TextFieldLabel("Last Name")
                    GlassTextField(value = lastName, onValueChange = { lastName = it })
                    Spacer(modifier = Modifier.height(16.dp))

                    // Email
                    TextFieldLabel("Email")
                    GlassTextField(value = email, onValueChange = { email = it })
                    Spacer(modifier = Modifier.height(16.dp))

                    // Password
                    TextFieldLabel("Password")
                    GlassTextField(
                        value = password,
                        onValueChange = { password = it },
                        isPassword = true
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Confirm Password
                    TextFieldLabel("Re-enter Password")
                    GlassTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        isPassword = true
                    )

                    Spacer(modifier = Modifier.height(28.dp))

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

                    // Sign Up Button
                    Button(
                        onClick = {
                            when {
                                firstName.isBlank() || lastName.isBlank() || email.isBlank() || password.isBlank() -> {
                                    errorMessage = "Please fill in all fields"
                                    showError = true
                                }
                                password != confirmPassword -> {
                                    errorMessage = "Passwords do not match"
                                    showError = true
                                }
                                password.length < 6 -> {
                                    errorMessage = "Password must be at least 6 characters"
                                    showError = true
                                }
                                !email.contains("@") -> {
                                    errorMessage = "Please enter a valid email"
                                    showError = true
                                }
                                else -> {
                                    authViewModel.signUp(
                                        email = email,
                                        password = password,
                                        firstName = firstName,
                                        lastName = lastName,
                                        onSuccess = {
                                            surveyViewModel.resetSurveyStateForNewSession()
                                            android.util.Log.d("SignUpScreen", "✅ Signup successful!")
                                            android.util.Log.d("SignUpScreen", "🆕 New user - navigating to onboarding flow")
                                            // New users NEVER have a survey - always go to onboarding
                                            onSignUpSuccess(false, false)
                                        },
                                        onError = { error ->
                                            errorMessage = error
                                            showError = true
                                        }
                                    )
                                }
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
                                text = "Sign Up",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TextFieldLabel(text: String) {
    Text(
        text = text,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        color = Color.White,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun GlassTextField(value: String, onValueChange: (String) -> Unit, isPassword: Boolean = false) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
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
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewSignUpScreen() {
    SignUpScreenContent()
}
