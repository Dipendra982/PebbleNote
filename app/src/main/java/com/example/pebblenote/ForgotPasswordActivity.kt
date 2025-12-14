package com.example.pebblenote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pebblenote.ui.theme.PebbleNoteTheme
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PebbleNoteTheme {
                ForgotPasswordScreen(onFinished = {
                    finish()
                })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(onFinished: () -> Unit = {}) {
    var email by remember { mutableStateOf("") }
    var otpSent by remember { mutableStateOf(false) }
    var otpCode by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var infoText by remember { mutableStateOf<String?>(null) }

    val startColor = Color(0xFFF8C1D9)
    val endColor = Color(0xFFCDB4F6)
    val gradientBrush = Brush.horizontalGradient(colors = listOf(startColor, endColor))

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Reset Password", fontWeight = FontWeight.Bold) })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.White)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(gradientBrush, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("Forgot your password?", color = Color.Black, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            }

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    if (email.isBlank()) {
                        infoText = "Please enter your email."
                    } else {
                        FirebaseAuth.getInstance().sendPasswordResetEmail(email.trim())
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    otpSent = true
                                    infoText = "Password reset email sent. Check your inbox."
                                } else {
                                    infoText = task.exception?.localizedMessage ?: "Failed to send reset email."
                                }
                            }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
            ) { Text(if (otpSent) "Resend OTP" else "Send OTP", color = Color.White) }

            if (otpSent) {
                OutlinedTextField(
                    value = otpCode,
                    onValueChange = { otpCode = it },
                    label = { Text("Enter OTP") },
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("New Password") },
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm Password") },
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        if (newPassword.isBlank() || confirmPassword.isBlank()) {
                            infoText = "Please fill all fields."
                        } else if (newPassword != confirmPassword) {
                            infoText = "Passwords do not match."
                        } else {
                            // In email reset flow, user resets via email link; here we just close.
                            infoText = "If you received the email, follow the link to reset."
                            onFinished()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) { Text("Reset Password", color = Color.White) }
            }

            if (infoText != null) {
                Text(infoText!!, color = Color.DarkGray)
            }
        }
    }
}
@Preview(showBackground = true, device = "spec:width=411dp,height=891dp",
    backgroundColor = 0xFF501616
)
@Composable
fun ForgotPasswordScreenPreview() {
    PebbleNoteTheme {
        ForgotPasswordScreen()
    }
}
