package com.example.pebblenote

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pebblenote.ui.theme.PebbleNoteTheme
// Firebase removed for demo persistence of notes

// Data class representing a PDF/Note item for selling
data class PDFItem(
    val id: Int,
    val title: String,
    val price: String,
    val views: Int,
    val downloads: Int,
    val likes: Int,
    val category: String = "Notes",
    val description: String = "",
    val previewImageUri: String? = null
)

// No dummy data; live data only from Firebase

class DashboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PebbleNoteTheme {
                var pdfs by remember { mutableStateOf(emptyList<PDFItem>()) }

                // Load from local store saved by Admin (demo persistence)
                LaunchedEffect(Unit) {
                    val adminNotes = LocalNotesStore.load(this@DashboardActivity)
                    val mapped = adminNotes.filter { it.enabled }.map { n ->
                        PDFItem(
                            id = n.id,
                            title = n.title,
                            price = "Rs ${String.format("%.2f", n.price)}",
                            views = 0,
                            downloads = 0,
                            likes = 0,
                            category = n.category.ifBlank { "Notes" },
                            description = n.description,
                            previewImageUri = n.previewImageUris.firstOrNull()?.toString()
                        )
                    }
                    pdfs = mapped
                }

                DashboardScreen(pdfs,
                    onLogout = {
                        // Clear remember me and session
                        val prefs = getSharedPreferences("pebble_prefs", MODE_PRIVATE)
                        prefs.edit().clear().apply()
                        // Navigate to Login and clear back stack
                        val intent = Intent(this, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    },
                    onProfile = {
                        startActivity(Intent(this, ProfileActivity::class.java))
                    },
                    onBuy = { item ->
                        val intent = Intent(this, PurchaseActivity::class.java)
                        intent.putExtra("noteId", item.id)
                        intent.putExtra("title", item.title)
                        intent.putExtra("price", item.price)
                        startActivity(intent)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    pdfs: List<PDFItem>,
    onLogout: () -> Unit,
    onProfile: () -> Unit,
    onBuy: (PDFItem) -> Unit
) {
    var detailItem by remember { mutableStateOf<PDFItem?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        topBar = { DashboardTopBar(onLogout = onLogout, onProfile = onProfile) },
        containerColor = Color(0xFFF5F5F5)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { DashboardWelcomeHeader() }
            item {
                Text(
                    text = "Your PDFs & Notes",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 8.dp),
                    color = Color.Black
                )
            }
            items(pdfs) { pdf ->
                PDFCard(pdf, onBuy = { onBuy(pdf) }, onViewDetails = { detailItem = pdf })
            }
        }
    }

    if (detailItem != null) {
        ModalBottomSheet(
            onDismissRequest = { detailItem = null },
            sheetState = sheetState
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(detailItem!!.title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(detailItem!!.description.ifBlank { "No description available." }, fontSize = 14.sp, color = Color.DarkGray)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AssistChip(onClick = { /* preview images placeholder */ }, label = { Text("Preview Images") }, leadingIcon = { Icon(Icons.Default.Image, contentDescription = null) })
                    AssistChip(onClick = { /* open sample PDF placeholder */ }, label = { Text("Open Sample") }, leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) })
                }
                Spacer(Modifier.height(8.dp))
                Button(onClick = { onBuy(detailItem!!); detailItem = null }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))) {
                    Text("Buy Now", color = Color.White)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardTopBar(onLogout: () -> Unit = {}, onProfile: () -> Unit = {}) {
    TopAppBar(
        title = {
            Text(
                "PebbleNote",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color.Black
            )
        },
        actions = {
            Row(
                modifier = Modifier.padding(end = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "test one",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Text(
                    text = "Logout",
                    fontSize = 12.sp,
                    color = Color(0xFF1976D2),
                    modifier = Modifier.clickable { onLogout() }
                )
                Icon(
                    Icons.Default.AccountCircle,
                    contentDescription = "Profile",
                    modifier = Modifier
                        .size(28.dp)
                        .clickable { onProfile() },
                    tint = Color(0xFF1976D2)
                )
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(Color(0xFF4CAF50), RoundedCornerShape(6.dp))
                )
                Text(
                    text = "Welcome back!",
                    fontSize = 11.sp,
                    color = Color(0xFF4CAF50),
                    fontWeight = FontWeight.Medium
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
        modifier = Modifier.height(64.dp)
    )
}

@Composable
fun DashboardWelcomeHeader() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Dashboard",
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.Black
        )
        Text(
            text = "Welcome back, test! Manage your PDFs and notes and track your performance.",
            fontSize = 14.sp,
            color = Color.DarkGray,
            lineHeight = 18.sp
        )
    }
}

// Stats removed for user dashboard to keep UI clean

// StatCard removed

// Upload button removed for end-user dashboard

@Composable
fun PDFCard(pdf: PDFItem, onBuy: () -> Unit, onViewDetails: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // PDF Thumbnail Placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF0F0F0)),
                contentAlignment = Alignment.Center
            ) {
                val ctx = androidx.compose.ui.platform.LocalContext.current
                val preview = pdf.previewImageUri
                if (preview != null) {
                    val bmp = try {
                        val uri = android.net.Uri.parse(preview)
                        ctx.contentResolver.openInputStream(uri)?.use { stream ->
                            android.graphics.BitmapFactory.decodeStream(stream)
                        }
                    } catch (_: Exception) {
                        null
                    }
                    if (bmp != null) {
                        androidx.compose.foundation.Image(
                            bitmap = bmp.asImageBitmap(),
                            contentDescription = "Preview",
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            Icons.Default.Image,
                            contentDescription = "Preview",
                            modifier = Modifier.size(48.dp),
                            tint = Color.Gray
                        )
                    }
                } else {
                    Icon(
                        Icons.Default.Description,
                        contentDescription = "PDF",
                        modifier = Modifier.size(48.dp),
                        tint = Color.Gray
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Title and Price
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        pdf.title,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = Color.Black
                    )
                    Text(
                        pdf.category,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
                Text(
                    pdf.price,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF1976D2)
                )
            }

            Spacer(Modifier.height(8.dp))

            // Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatIconText(Icons.Default.Visibility, pdf.views.toString(), Color.Gray)
                StatIconText(Icons.Default.FavoriteBorder, pdf.likes.toString(), Color.Gray)
                StatIconText(Icons.Default.Download, pdf.downloads.toString(), Color.Gray)
            }

            Spacer(Modifier.height(12.dp))

            // End-user actions: view details and purchase
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onViewDetails,
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("View Details", fontSize = 14.sp, color = Color.White)
                }
                Button(
                    onClick = onBuy,
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("Buy Now", fontSize = 14.sp, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun StatIconText(icon: ImageVector, text: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(16.dp)
        )
        Text(text, fontSize = 12.sp, color = color)
    }
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun DashboardScreenPreview() {
    PebbleNoteTheme {
        val samplePdfs = listOf(
            PDFItem(1, "Math Notes", "Rs 50.00", 100, 25, 10),
            PDFItem(2, "Physics Notes", "Rs 75.00", 150, 40, 20)
        )
        DashboardScreen(
            samplePdfs,
            onLogout = {},
            onProfile = {},
            onBuy = {}
        )
    }
}
