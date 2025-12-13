package com.example.pebblenote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.example.pebblenote.ui.theme.PebbleNoteTheme

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Auto-login if remembered
        val prefs = getSharedPreferences("pebble_prefs", MODE_PRIVATE)
        val remembered = prefs.getBoolean("remember_me", false)
        if (remembered) {
            val isAdmin = prefs.getBoolean("is_admin", false)
            val dest = if (isAdmin) AdminDashboardActivity::class.java else DashboardActivity::class.java
            startActivity(android.content.Intent(this, dest))
            finish()
            return
        }
        setContent {
            PebbleNoteTheme {
                LoginScreen(
                    onLoginResult = { isAdmin ->
                        val dest = if (isAdmin) AdminDashboardActivity::class.java else DashboardActivity::class.java
                        startActivity(android.content.Intent(this, dest))
                        finish()
                    }
                )
            }
        }
    }
}
@Composable
fun LoginScreen(onLoginResult: (isAdmin: Boolean) -> Unit = {}) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var rememberMeChecked by remember { mutableStateOf(false) }
    var isAdminSelected by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }
    val ctx = LocalContext.current

    val startColor = Color(0xFFF8C1D9)
    val endColor = Color(0xFFCDB4F6)
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
                IconButton(onClick = { /* Handle back press */ }) {
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
                    .weight(1f)
                    .clip(
                        RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp)
                    )
                    .background(Color.White)
            ) {
                // Background gradient for the rounded top part
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            brush = gradientBrush,
                            shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp)
                        )
                        .clip(RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp))
                        .fillMaxHeight(0.40f)
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp)
                        .padding(top = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Welcome Back",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Ready to continue your learning journey?",
                        fontSize = 14.sp,
                        color = Color.DarkGray
                    )
                    Text(
                        text = "Your path is right here.",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(48.dp))

                    CustomAuthTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = "Enter email",
                        keyboardType = KeyboardType.Email
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    CustomAuthTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = "Password",
                        keyboardType = KeyboardType.Password,
                        isPassword = true,
                        passwordVisible = passwordVisible,
                        onPasswordToggle = { passwordVisible = !passwordVisible }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { rememberMeChecked = !rememberMeChecked }
                        ) {
                            Checkbox(
                                checked = rememberMeChecked,
                                onCheckedChange = { rememberMeChecked = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = endColor,
                                    uncheckedColor = Color.Gray
                                )
                            )
                            Text(
                                text = "Remember me",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                        }

                        Text(
                            text = "Forgot password?",
                            color = endColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Role selector (Admin / User)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilterChip(
                            selected = isAdminSelected,
                            onClick = { isAdminSelected = true },
                            label = { Text("Admin") },
                            leadingIcon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = null) }
                        )
                        FilterChip(
                            selected = !isAdminSelected,
                            onClick = { isAdminSelected = false },
                            label = { Text("User") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Error text if credentials invalid
                    if (errorText != null) {
                        Text(
                            text = errorText!!,
                            color = Color.Red,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // START OF BUTTON FIX
                    Button(
                        onClick = {
                            // Dummy credentials
                            val adminUser = "admin@example.com"
                            val adminPass = "Admin@123"
                            val normalUser = "user@example.com"
                            val normalPass = "User@123"

                            val ok = if (isAdminSelected) {
                                email.equals(adminUser, ignoreCase = true) && password == adminPass
                            } else {
                                email.equals(normalUser, ignoreCase = true) && password == normalPass
                            }

                            if (ok) {
                                // Persist remember me
                                if (rememberMeChecked) {
                                    val prefs = ctx.getSharedPreferences("pebble_prefs", android.content.Context.MODE_PRIVATE)
                                    prefs.edit()
                                        .putBoolean("remember_me", true)
                                        .putBoolean("is_admin", isAdminSelected)
                                        .putString("email", email)
                                        .apply()
                                } else {
                                    val prefs = ctx.getSharedPreferences("pebble_prefs", android.content.Context.MODE_PRIVATE)
                                    prefs.edit().clear().apply()
                                }
                                errorText = null
                                onLoginResult(isAdminSelected)
                            } else {
                                errorText = "Invalid credentials for ${if (isAdminSelected) "Admin" else "User"}."
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(
                            // Set background to White
                            containerColor = Color.White,
                            // Set text color to Black
                            contentColor = Color.Black
                        ),
                        // Added a subtle shadow to make the white button stand out against the white background (if any)
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        Text(
                            text = "Log In",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    // END OF BUTTON FIX
                }
            }
        }
    }
}

// NOTE: The CustomAuthTextField and Activity/Preview components remain unchanged.

@Composable
fun CustomAuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onPasswordToggle: () -> Unit = {}
) {
    val visualTransformation = when {
        isPassword && !passwordVisible -> PasswordVisualTransformation()
        else -> VisualTransformation.None
    }

    val trailingIcon: @Composable (() -> Unit)? = if (isPassword) {
        {
            // Using custom drawables from the R.drawable folder
            val imagePainter = if (passwordVisible)
                painterResource(id = R.drawable.eyeclose)
            else
                painterResource(id = R.drawable.eye)

            val description = if (passwordVisible) "Hide password" else "Show password"

            IconButton(onClick = onPasswordToggle) {
                Icon(
                    painter = imagePainter,
                    contentDescription = description,
                    tint = Color.Gray
                )
            }
        }
    } else null

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        label = { Text(label) },
        shape = RoundedCornerShape(16.dp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = visualTransformation,
        trailingIcon = trailingIcon,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedBorderColor = Color.LightGray.copy(alpha = 0.5f),
            unfocusedBorderColor = Color.LightGray.copy(alpha = 0.3f),
            focusedLabelColor = Color.DarkGray,
            unfocusedLabelColor = Color.Gray,
        )
    )
}


@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun LoginScreenPreview() {
    PebbleNoteTheme {
        LoginScreen()
    }
}