package com.example.pebblenote

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pebblenote.ui.theme.PebbleNoteTheme

// Data class representing a PDF/Note item for selling
data class PDFItem(
    val id: Int,
    val title: String,
    val price: String,
    val views: Int,
    val downloads: Int,
    val likes: Int,
    val category: String = "Notes"
)

// Dummy data for the dashboard
private val dummyPDFs = listOf(
    PDFItem(1, "Ghumgham", "$3.00", 1, 1, 0),
    PDFItem(2, "fun", "$33.00", 9, 2, 1),
    PDFItem(3, "photo", "$2.00", 9, 2, 1),
    PDFItem(4, "Math Notes", "$5.50", 12, 5, 3),
)

class DashboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PebbleNoteTheme {
                DashboardScreen(dummyPDFs)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(pdfs: List<PDFItem>) {
    Scaffold(
        topBar = { DashboardTopBar() },
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
            item { StatsRow() }
            item { UploadButton() }
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
                PDFCard(pdf)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardTopBar() {
    TopAppBar(
        title = {
            Text(
                "PebbleNotes",
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
                    text = "to test one",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Text(
                    text = "Logout",
                    fontSize = 12.sp,
                    color = Color(0xFF1976D2),
                    modifier = Modifier.clickable { /* Handle logout */ }
                )
                Icon(
                    Icons.Default.AccountCircle,
                    contentDescription = "Profile",
                    modifier = Modifier.size(28.dp),
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

@Composable
fun StatsRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(Icons.Default.Image, "Total PDFs", "3", Color(0xFFE0F7FA))
        StatCard(Icons.Default.Visibility, "Total Views", "3", Color(0xFFE3F2FD))
        StatCard(Icons.Default.AttachMoney, "Total Earnings", "$0.00", Color(0xFFE8F5E9))
        StatCard(Icons.Default.Favorite, "Total Likes", "0", Color(0xFFFFEBEE))
        StatCard(Icons.Default.Download, "Total Downloads", "3", Color(0xFFF3E5F5))
    }
}

@Composable
fun StatCard(icon: ImageVector, title: String, value: String, backgroundColor: Color) {
    Card(
        modifier = Modifier
            .width(140.dp)
            .height(100.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = value,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        text = title,
                        fontSize = 10.sp,
                        color = Color.Gray,
                        lineHeight = 12.sp
                    )
                }
                Icon(
                    icon,
                    contentDescription = title,
                    tint = Color.White,
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(backgroundColor)
                        .padding(4.dp)
                )
            }
        }
    }
}

@Composable
fun UploadButton() {
    Button(
        onClick = { /* Handle upload */ },
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(12.dp)
    ) {
        Icon(
            Icons.Default.Add,
            contentDescription = "Upload",
            modifier = Modifier.size(20.dp),
            tint = Color.White
        )
        Spacer(Modifier.width(8.dp))
        Text(
            "Upload New PDF",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White
        )
    }
}

@Composable
fun PDFCard(pdf: PDFItem) {
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
                Icon(
                    Icons.Default.Description,
                    contentDescription = "PDF",
                    modifier = Modifier.size(48.dp),
                    tint = Color.Gray
                )
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

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { /* Handle Edit */ },
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit",
                        modifier = Modifier.size(16.dp),
                        tint = Color.White
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("Edit", fontSize = 14.sp, color = Color.White)
                }
                Button(
                    onClick = { /* Handle Delete */ },
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336)),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        modifier = Modifier.size(16.dp),
                        tint = Color.White
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("Delete", fontSize = 14.sp, color = Color.White)
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

@Preview(showBackground = true, backgroundColor = 0xFFF5F5F5)
@Composable
fun DashboardScreenPreview() {
    PebbleNoteTheme {
        DashboardScreen(dummyPDFs)
    }
}
