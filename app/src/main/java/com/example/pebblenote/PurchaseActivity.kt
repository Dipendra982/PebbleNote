package com.example.pebblenote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.example.pebblenote.ui.theme.PebbleNoteTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import android.widget.Toast
import android.content.Intent

class PurchaseActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val noteId = intent.getIntExtra("noteId", 0)
        val title = intent.getStringExtra("title") ?: "Note"
        val price = intent.getStringExtra("price") ?: "Rs 0.00"
        setContent {
            PebbleNoteTheme {
                PurchaseScreen(noteId, title, price)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchaseScreen(noteId: Int, title: String, price: String) {
    var selectedMethod by remember { mutableStateOf("Khalti") }
    var showSuccess by remember { mutableStateOf(false) }
    var downloadInProgress by remember { mutableStateOf(false) }
    val ctx = LocalContext.current

    Scaffold(
        topBar = { TopAppBar(title = { Text("Purchase", fontWeight = FontWeight.Bold) }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(shape = RoundedCornerShape(12.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Text(title, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    Text(price, fontSize = 16.sp, color = Color(0xFF1976D2))
                    Text("Preview available", fontSize = 12.sp, color = Color.Gray)
                }
            }

            Text("Choose Payment Method", fontWeight = FontWeight.Medium)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FilterChip(selected = selectedMethod == "Khalti", onClick = { selectedMethod = "Khalti" }, label = { Text("Khalti") })
                FilterChip(selected = selectedMethod == "eSewa", onClick = { selectedMethod = "eSewa" }, label = { Text("eSewa") })
            }

            Button(
                onClick = {
                    val uid = FirebaseAuth.getInstance().currentUser?.uid
                    if (uid != null) {
                        val ts = System.currentTimeMillis().toString()
                        val ref = FirebaseDatabase.getInstance().reference.child("purchases").child(uid).child(ts)
                        val data = mapOf(
                            "noteId" to noteId,
                            "title" to title,
                            "price" to price,
                            "method" to selectedMethod,
                            "status" to "success",
                            "timestamp" to ts
                        )
                        ref.setValue(data).addOnCompleteListener {
                            if (!it.isSuccessful) {
                                Toast.makeText(ctx, it.exception?.localizedMessage ?: "Failed to record purchase", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    showSuccess = true
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Pay with $selectedMethod", color = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }

    if (showSuccess) {
        AlertDialog(
            onDismissRequest = { showSuccess = false },
            title = { Text("Payment Successful") },
            text = { Text("Your purchase was completed via $selectedMethod.\n\nYou can now download your PDF!") },
            confirmButton = {
                Button(
                    onClick = {
                        downloadInProgress = true
                        // Simulate download process
                        Thread {
                            Thread.sleep(1000)
                            Toast.makeText(ctx, "Demo downloaded successfully", Toast.LENGTH_SHORT).show()
                            downloadInProgress = false
                            // Redirect to Dashboard
                            showSuccess = false
                            ctx.startActivity(Intent(ctx, DashboardActivity::class.java).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            })
                        }.start()
                    },
                    enabled = !downloadInProgress,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
                ) {
                    Icon(Icons.Default.FileDownload, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(if (downloadInProgress) "Downloading..." else "Download PDF")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showSuccess = false
                    ctx.startActivity(Intent(ctx, DashboardActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    })
                }) { Text("Skip") }
            }
        )
    }
}
