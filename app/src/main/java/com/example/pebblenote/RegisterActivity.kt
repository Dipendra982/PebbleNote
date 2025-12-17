package com.example.pebblenote

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.example.pebblenote.ui.theme.PebbleNoteTheme

// NOTE: This file relies on the existence of CustomAuthTextField in your TextFields.kt
// and the presence of R.drawable.eye and R.drawable.eyeclose.

class RegistrationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PebbleNoteTheme {
                RegistrationScreen(onRegistered = {
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                })
            }
        }
    }
}

@Composable
fun RegistrationScreen(onRegistered: () -> Unit = {}) {
    // State variables for registration fields
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }
    var submitting by remember { mutableStateOf(false) }
    val ctx: Context = LocalContext.current

    // Colors matching the Login page gradient
    val startColor = Color(0xFFF8C1D9) // Light Pink
    val endColor = Color(0xFFCDB4F6)   // Light Purple
    val gradientBrush = Brush.horizontalGradient(
        colors = listOf(startColor, endColor)
    )

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, start = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
//                    val ctx = LocalContext.current
                    (ctx as? Activity)?.finish()
                }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.Black
                    )
                }
            }
        },
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f) // Fills available space
                    .clip(
                        RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp)
                    )
                    .background(Color.White)
            ) {
                // Background gradient for the rounded top part (Frame and Color from Login)
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            brush = gradientBrush,
                            shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp)
                        )
                        .clip(RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp))
                        .fillMaxHeight(0.40f) // Matches the height adjustment from Login
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp)
                        .padding(top = 40.dp), // Matches the vertical positioning from Login
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // --- App Logo ---
                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = "App Logo",
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(16.dp))
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // --- Header Text ---
                    Text(
                        text = "Create account",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // "Already have an account?" with clickable "sign in"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Already have an account? ",
                            fontSize = 14.sp,
                            color = Color.DarkGray
                        )
                        Text(
                            text = "sign in",
                            fontSize = 14.sp,
                            color = Color(0xFF7C3AED),
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clickable {
                                val intent = Intent(ctx, LoginActivity::class.java)
                                ctx.startActivity(intent)
                                (ctx as? Activity)?.finish()
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // --- Form Fields ---

                    // Full Name TextField
                    CustomAuthTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = "Enter full name"
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Email TextField
                    CustomAuthTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = "Enter email",
                        keyboardType = KeyboardType.Email
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Password TextField
                    CustomAuthTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = "Enter password",
                        keyboardType = KeyboardType.Password,
                        isPassword = true,
                        passwordVisible = passwordVisible,
                        onPasswordToggle = { passwordVisible = !passwordVisible }
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Spacer(modifier = Modifier.height(24.dp))

                    // Inline error message
                    if (errorText != null) {
                        Text(text = errorText!!, color = Color.Red, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // "Sign up" Button (Firebase Auth; then route to Login)
                    Button(
                        onClick = {
                            if (submitting) return@Button
                            // Basic validation
                            if (fullName.isBlank()) {
                                errorText = "Please enter your full name."
                                return@Button
                            }
                            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                                errorText = "Enter a valid email address."
                                return@Button
                            }
                            if (password.length < 6) {
                                errorText = "Password must be at least 6 characters."
                                return@Button
                            }
                            submitting = true
                            val auth = FirebaseAuth.getInstance()
                            auth.createUserWithEmailAndPassword(email.trim(), password)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        val uid = task.result?.user?.uid
                                        if (uid != null) {
                                            val userMap = mapOf(
                                                "name" to fullName,
                                                "email" to email.trim()
                                            )
                                            FirebaseDatabase.getInstance().reference
                                                .child("users")
                                                .child(uid)
                                                .setValue(userMap)
                                        }
                                        errorText = null
                                        submitting = false
                                        onRegistered()
                                    } else {
                                        submitting = false
                                        errorText = task.exception?.localizedMessage ?: "Registration failed."
                                    }
                                }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF7C3AED),
                            contentColor = Color.White
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                        enabled = !submitting
                    ) {
                        Text(
                            text = if (submitting) "Creating..." else "Sign up",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}


@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun RegistrationScreenPreview() {
    PebbleNoteTheme {
        RegistrationScreen()
    }
}