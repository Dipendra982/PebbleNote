package com.example.pebblenote

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pebblenote.ui.theme.PebbleNoteTheme

// Admin-facing data model
data class AdminNote(
    val id: Int,
    var title: String,
    var price: Double,
    var pdfUri: Uri? = null,
    var previewImageUris: List<Uri> = emptyList(),
    var category: String = "General",
    var description: String = "",
    var enabled: Boolean = true
)

class AdminDashboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PebbleNoteTheme {
                AdminDashboardScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen() {
    // Admin-only access gate (replace with real auth)
    val isAdmin = remember { true }

    // In-memory demo list; replace with ViewModel + Repository
    val notes = remember { mutableStateListOf(
        AdminNote(1, "Ghumgham", 3.0),
        AdminNote(2, "fun", 33.0),
        AdminNote(3, "photo", 2.0),
    ) }

    var editingNote by remember { mutableStateOf<AdminNote?>(null) }
    var showUploadDialog by remember { mutableStateOf(false) }
    var noteToDelete by remember { mutableStateOf<AdminNote?>(null) }

    if (!isAdmin) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Admins only", style = MaterialTheme.typography.titleMedium)
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Admin Dashboard", fontWeight = FontWeight.Bold)
                },
                actions = {
                    Text(
                        text = "Admin",
                        color = Color(0xFF4CAF50),
                        modifier = Modifier.padding(end = 12.dp)
                    )
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showUploadDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Upload")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(notes, key = { it.id }) { note ->
                AdminNoteCard(
                    note = note,
                    onEdit = { editingNote = it.copy() },
                    onDelete = { noteToDelete = it },
                    onToggleEnabled = { it.enabled = !it.enabled }
                )
            }
            item { Spacer(Modifier.height(64.dp)) }
        }
    }

    // Edit Dialog
    editingNote?.let { draft ->
        EditNoteDialog(
            draft = draft,
            onDismiss = { editingNote = null },
            onSave = { updated ->
                val index = notes.indexOfFirst { it.id == updated.id }
                if (index >= 0) notes[index] = updated
                editingNote = null
            }
        )
    }

    // Upload Dialog
    if (showUploadDialog) {
        UploadNoteDialog(
            onDismiss = { showUploadDialog = false },
            onSubmit = { newNote ->
                val nextId = (notes.maxOfOrNull { it.id } ?: 0) + 1
                notes.add(newNote.copy(id = nextId))
                showUploadDialog = false
            }
        )
    }

    // Delete Confirm
    noteToDelete?.let { toDelete ->
        AlertDialog(
            onDismissRequest = { noteToDelete = null },
            title = { Text("Delete note?") },
            text = { Text("This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    notes.removeAll { it.id == toDelete.id }
                    noteToDelete = null
                }) { Text("Delete", color = Color(0xFFF44336)) }
            },
            dismissButton = {
                TextButton(onClick = { noteToDelete = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun AdminNoteCard(
    note: AdminNote,
    onEdit: (AdminNote) -> Unit,
    onDelete: (AdminNote) -> Unit,
    onToggleEnabled: (AdminNote) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(Modifier.weight(1f)) {
                    Text(note.title, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    Text("$${String.format("%.2f", note.price)}", color = Color(0xFF1976D2))
                    Text(if (note.enabled) "Enabled" else "Disabled", color = if (note.enabled) Color(0xFF4CAF50) else Color.Gray, fontSize = 12.sp)
                }
                Icon(
                    Icons.Default.Description,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF0F0F0))
                        .padding(8.dp)
                )
            }

            Spacer(Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { onEdit(note) }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Edit")
                }
                OutlinedButton(onClick = { onToggleEnabled(note) }, modifier = Modifier.weight(1f)) {
                    Icon(if (note.enabled) Icons.Default.Block else Icons.Default.Check, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text(if (note.enabled) "Disable" else "Enable")
                }
                OutlinedButton(onClick = { onDelete(note) }, modifier = Modifier.weight(1f), colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFF44336))) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Delete")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditNoteDialog(
    draft: AdminNote,
    onDismiss: () -> Unit,
    onSave: (AdminNote) -> Unit
) {
    var title by remember(draft.id) { mutableStateOf(draft.title) }
    var priceText by remember(draft.id) { mutableStateOf(draft.price.toString()) }
    var pdfUri by remember(draft.id) { mutableStateOf(draft.pdfUri) }
    var images by remember(draft.id) { mutableStateOf(draft.previewImageUris) }
    var category by remember(draft.id) { mutableStateOf(draft.category) }
    var description by remember(draft.id) { mutableStateOf(draft.description) }

    val pickPdf = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) pdfUri = uri
    }
    val pickImages = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        if (uris != null) images = uris
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Note") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") })
                OutlinedTextField(value = priceText, onValueChange = { priceText = it }, label = { Text("Price") }, singleLine = true)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AssistChip(onClick = { pickPdf.launch(arrayOf("application/pdf")) }, label = { Text(if (pdfUri == null) "Pick PDF" else "Change PDF") }, leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) })
                    AssistChip(onClick = { pdfUri = null }, label = { Text("Remove PDF") })
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AssistChip(onClick = { pickImages.launch(arrayOf("image/*")) }, label = { Text("Pick Images") }, leadingIcon = { Icon(Icons.Default.Image, contentDescription = null) })
                    AssistChip(onClick = { images = emptyList() }, label = { Text("Clear Images") })
                }
                OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category / Subject") })
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Short Description") })
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val price = priceText.toDoubleOrNull()
                if (title.isBlank() || price == null || price < 0.0) return@TextButton
                onSave(draft.copy(title = title, price = price, pdfUri = pdfUri, previewImageUris = images, category = category, description = description))
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UploadNoteDialog(
    onDismiss: () -> Unit,
    onSubmit: (AdminNote) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var pdfUri by remember { mutableStateOf<Uri?>(null) }
    var images by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var category by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var uploading by remember { mutableStateOf(false) }

    val pickPdf = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) pdfUri = uri
    }
    val pickImages = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        if (uris != null) images = uris
    }

    AlertDialog(
        onDismissRequest = { if (!uploading) onDismiss() },
        title = { Text("Upload New Note") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (uploading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") })
                OutlinedTextField(value = priceText, onValueChange = { priceText = it }, label = { Text("Price") }, singleLine = true)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AssistChip(onClick = { pickPdf.launch(arrayOf("application/pdf")) }, label = { Text(if (pdfUri == null) "Select PDF" else "Change PDF") }, leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) })
                    if (pdfUri != null) AssistChip(onClick = { pdfUri = null }, label = { Text("Remove PDF") })
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AssistChip(onClick = { pickImages.launch(arrayOf("image/*")) }, label = { Text("Select Images") }, leadingIcon = { Icon(Icons.Default.Image, contentDescription = null) })
                    if (images.isNotEmpty()) AssistChip(onClick = { images = emptyList() }, label = { Text("Clear Images") })
                }
                OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category / Subject") })
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Short Description") })
            }
        },
        confirmButton = {
            TextButton(enabled = !uploading, onClick = {
                val price = priceText.toDoubleOrNull()
                if (title.isBlank() || price == null || price < 0.0 || pdfUri == null) return@TextButton
                uploading = true
                // Simulate a quick upload then submit
                onSubmit(AdminNote(id = 0, title = title, price = price, pdfUri = pdfUri, previewImageUris = images, category = category, description = description, enabled = true))
            }) { Text(if (uploading) "Uploading..." else "Upload") }
        },
        dismissButton = { TextButton(enabled = !uploading, onClick = onDismiss) { Text("Cancel") } }
    )
}
