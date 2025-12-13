package com.example.pebblenote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import android.net.Uri
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.Image
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pebblenote.ui.theme.PebbleNoteTheme

class ProfileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PebbleNoteTheme {
                ProfileScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen() {
    val ctx = LocalContext.current
    val prefs = remember { ctx.getSharedPreferences("pebble_prefs", android.content.Context.MODE_PRIVATE) }
    var name by remember { mutableStateOf(prefs.getString("profile_name", "test one") ?: "test one") }
    var email by remember { mutableStateOf(prefs.getString("profile_email", "user@example.com") ?: "user@example.com") }
    var bio by remember { mutableStateOf(prefs.getString("profile_bio", "") ?: "") }
    var saving by remember { mutableStateOf(false) }
    var avatarInitials by remember { mutableStateOf("TU") }
    var avatarUri by remember { mutableStateOf(prefs.getString("profile_avatar_uri", null)) }
    var avatarBitmap by remember(avatarUri) {
        mutableStateOf(
            avatarUri?.let {
                try {
                    val uri = Uri.parse(it)
                    ctx.contentResolver.openInputStream(uri)?.use { stream ->
                        BitmapFactory.decodeStream(stream)?.asImageBitmap()
                    }
                } catch (_: Exception) { null }
            }
        )
    }

    val pickImage = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            avatarUri = uri.toString()
            avatarBitmap = try {
                ctx.contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream)?.asImageBitmap()
                }
            } catch (_: Exception) { null }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (saving) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            Text("Edit your details", fontSize = 14.sp)
            // Profile picture: show picked image if available, else initials placeholder
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .align(Alignment.CenterHorizontally)
                    .clip(RoundedCornerShape(48.dp))
                    .padding(0.dp),
                contentAlignment = Alignment.Center
            ) {
                if (avatarBitmap != null) {
                    Image(bitmap = avatarBitmap!!, contentDescription = "Avatar")
                } else {
                    Surface(
                        color = Color(0xFFE0F2F1),
                        shape = RoundedCornerShape(48.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(avatarInitials, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00796B))
                        }
                    }
                }
            }
            OutlinedButton(
                onClick = { pickImage.launch("image/*") },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) { Text("Change Picture") }
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, singleLine = true)
            OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, singleLine = true)
            OutlinedTextField(value = bio, onValueChange = { bio = it }, label = { Text("Bio") })

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    saving = true
                    prefs.edit()
                        .putString("profile_name", name)
                        .putString("profile_email", email)
                        .putString("profile_bio", bio)
                        .apply()
                    // save avatar uri if available
                    if (avatarUri != null) {
                        prefs.edit().putString("profile_avatar_uri", avatarUri).apply()
                    }
                    saving = false
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Save Changes", color = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}
