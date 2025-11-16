package com.example.wakeupcallapp.sleepapp

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.wakeupcallapp.sleepapp.R

@Composable
fun SettingsScreen(navController: NavController) {
    var metricSystemEnabled by remember { mutableStateOf(true) }

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
                    text = "Settings",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Settings Options Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0x60FFFFFF))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // App Design Settings
                    SettingsMenuItem(
                        icon = "🎨",
                        title = "App Design Settings",
                        onClick = { /* Handle navigation */ }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Data Sources
                    SettingsMenuItem(
                        icon = "🛡️",
                        title = "Data Sources",
                        onClick = { /* Handle navigation */ }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // App Design Settings (second one)
                    SettingsMenuItem(
                        icon = "🎯",
                        title = "App Design Settings",
                        onClick = { /* Handle navigation */ }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Metric System Toggle Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0x60FFFFFF))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Metric System",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )

                    Switch(
                        checked = metricSystemEnabled,
                        onCheckedChange = { metricSystemEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF6B8DD6),
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color(0x60FFFFFF)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun SettingsMenuItem(
    icon: String,
    title: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
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
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Icon
                Text(
                    text = icon,
                    fontSize = 24.sp,
                    modifier = Modifier.size(40.dp),
                )

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
            }

            Icon(
                painter = painterResource(android.R.drawable.ic_menu_more),
                contentDescription = "Navigate",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewSettingsScreen() {
    val navController = rememberNavController()
    SettingsScreen(navController)
}