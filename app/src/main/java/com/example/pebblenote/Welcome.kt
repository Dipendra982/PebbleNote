package com.example.pebblenote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pebblenote.ui.theme.PebbleNoteTheme

class WelcomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PebbleNoteTheme {
                WelcomeScreen(
                    onCreateAccount = {
                        startActivity(android.content.Intent(this, RegistrationActivity::class.java))
                    },
                    onLogin = {
                        startActivity(android.content.Intent(this, LoginActivity::class.java))
                    }
                )
            }
        }
    }
}

@Composable
fun WelcomeScreen(
    onCreateAccount: () -> Unit = {},
    onLogin: () -> Unit = {}
) {

    val gradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFF8C1D9),
            Color(0xFFCDB4F6)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
            .padding(54.dp)
    ) {

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(48.dp))

            // Title
            Text(
                text = "Welcome =)",
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Subtitle
            Text(
                text = "Hi there!\nUnlock new skills and knowledge with PebbleNote.\nLog in or create an account to get started.",
                fontSize = 14.sp,
                color = Color.DarkGray,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )



            Spacer(modifier = Modifier.weight(1f))

            // Illustration placeholder
            Image(
                painter = painterResource(id = R.drawable.landing),
                contentDescription = "Welcome Illustration",
                modifier = Modifier
                    .height(350.dp)
                    .fillMaxWidth()
            )


            Spacer(modifier = Modifier.weight(1f))

            // Create Account Button
            Button(
                onClick = onCreateAccount,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                )
            ) {
                Text(
                    text = "Create Account",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Login Button (Outline style)
            OutlinedButton(
                onClick = onLogin,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = Brush.horizontalGradient(
                        listOf(Color.White, Color.White)
                    )
                )
            ) {
                Text(
                    text = "Log In",
                    fontSize = 16.sp,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/* ================= PREVIEW ================= */

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun WelcomeScreenPreview() {
    PebbleNoteTheme {
        WelcomeScreen()
    }
}
