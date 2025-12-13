package com.example.pebblenote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
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
import com.example.pebblenote.ui.theme.PebbleNoteTheme

class PurchaseActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val title = intent.getStringExtra("title") ?: "Note"
        val price = intent.getStringExtra("price") ?: "$0.00"
        setContent {
            PebbleNoteTheme {
                PurchaseScreen(title, price)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchaseScreen(title: String, price: String) {
    var selectedMethod by remember { mutableStateOf("Khalti") }

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
                onClick = { /* TODO: Integrate SDK/UI flow */ },
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
}
