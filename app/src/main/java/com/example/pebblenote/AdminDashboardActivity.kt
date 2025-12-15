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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.example.pebblenote.ui.theme.PebbleNoteTheme
import com.google.firebase.database.FirebaseDatabase
import android.content.Intent
import androidx.compose.ui.tooling.preview.Preview

// Demo mode: use local persistence only (no Firebase writes required)
private const val DEMO_MODE = true

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
    val notes = remember { mutableStateListOf<AdminNote>() }
    val ctxState = rememberUpdatedState(LocalContext.current)

    if (DEMO_MODE) {
        LaunchedEffect(Unit) {
            val initial = LocalNotesStore.load(ctxState.value)
            notes.clear(); notes.addAll(initial.sortedBy { it.id })
            // Mirror local notes to Firebase so Realtime Database shows details
            val dbRef = FirebaseDatabase.getInstance().reference.child("notes")
            initial.forEach { n ->
                val data = mapOf(
                    "id" to n.id,
                    "title" to n.title,
                    "price" to n.price,
                    "pdfUri" to (n.pdfUri?.toString() ?: ""),
                    "previewImageUris" to n.previewImageUris.map { it.toString() },
                    "category" to n.category,
                    "description" to n.description,
                    "enabled" to n.enabled
                )
                dbRef.child(n.id.toString()).setValue(data)
            }
        }
    } else {
        // Properly manage Firebase listeners and support both "notes" and "Notes" paths
        DisposableEffect(Unit) {
            val db = FirebaseDatabase.getInstance().reference
            val lowerRef = db.child("notes")
            val upperRef = db.child("Notes")
            lowerRef.keepSynced(true)
            upperRef.keepSynced(true)

            fun snapshotToNotes(snapshot: com.google.firebase.database.DataSnapshot): List<AdminNote> {
                val list = mutableListOf<AdminNote>()
                for (child in snapshot.children) {
                    try {
                        val id = child.child("id").getValue(Number::class.java)?.toInt()
                            ?: (child.key?.toIntOrNull() ?: 0)
                        val title = child.child("title").getValue(String::class.java) ?: "Untitled"
                        val price = child.child("price").getValue(Number::class.java)?.toDouble() ?: 0.0
                        val pdfUri = child.child("pdfUri").getValue(String::class.java)
                            ?.takeIf { it.isNotBlank() }?.let(Uri::parse)
                        val previews = buildList {
                            child.child("previewImageUris").children.forEach { p ->
                                p.getValue(String::class.java)?.takeIf { it.isNotBlank() }?.let { add(Uri.parse(it)) }
                            }
                        }
                        val category = child.child("category").getValue(String::class.java) ?: "General"
                        val description = child.child("description").getValue(String::class.java) ?: ""
                        val enabled = child.child("enabled").getValue(Boolean::class.java) ?: true
                        list.add(AdminNote(id, title, price, pdfUri, previews, category, description, enabled))
                    } catch (_: Exception) { /* skip malformed */ }
                }
                return list
            }

            fun mergeAndSet(a: List<AdminNote>, b: List<AdminNote>) {
                val map = LinkedHashMap<Int, AdminNote>()
                a.forEach { map[it.id] = it }
                b.forEach { map[it.id] = it }
                notes.clear()
                notes.addAll(map.values.sortedBy { it.id })
            }

            var lowerList: List<AdminNote> = emptyList()
            var upperList: List<AdminNote> = emptyList()

            val lowerListener = object : com.google.firebase.database.ValueEventListener {
                override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                    lowerList = snapshotToNotes(snapshot)
                    mergeAndSet(lowerList, upperList)
                }
                override fun onCancelled(error: com.google.firebase.database.DatabaseError) { /* no-op */ }
            }

            val upperListener = object : com.google.firebase.database.ValueEventListener {
                override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                    upperList = snapshotToNotes(snapshot)
                    mergeAndSet(lowerList, upperList)
                }
                override fun onCancelled(error: com.google.firebase.database.DatabaseError) { /* no-op */ }
            }

            lowerRef.addValueEventListener(lowerListener)
            upperRef.addValueEventListener(upperListener)

            onDispose {
                lowerRef.removeEventListener(lowerListener)
                upperRef.removeEventListener(upperListener)
            }
        }
    }

    var editingNote by remember { mutableStateOf<AdminNote?>(null) }
    var showUploadDialog by remember { mutableStateOf(false) }
    var noteToDelete by remember { mutableStateOf<AdminNote?>(null) }

    if (!true) {
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Admin",
                            color = Color(0xFF4CAF50),
                            modifier = Modifier.padding(end = 12.dp)
                        )
                        Text(
                            text = "Logout",
                            color = Color(0xFF1976D2),
                            modifier = Modifier
                                .padding(end = 12.dp)
                                .clickable {
                                    val ctx = ctxState.value
                                    // Clear any session/prefs you use
                                    ctx.getSharedPreferences("pebble_prefs", android.content.Context.MODE_PRIVATE)
                                        .edit().clear().apply()

                                    // Finish current screen so listeners dispose
                                    (ctx as? ComponentActivity)?.finish()

                                    // Go to login with a fresh task
                                    ctx.startActivity(Intent(ctx, LoginActivity::class.java).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                    })
                                }
                        )
                    }
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
            // Insights cards
            item {
                val sales = remember { mutableStateOf(0) }
                val revenue = remember { mutableStateOf(0.0) }
                LaunchedEffect(Unit) {
                    val purchasesRef = FirebaseDatabase.getInstance().reference.child("purchases")
                    purchasesRef.addListenerForSingleValueEvent(object : com.google.firebase.database.ValueEventListener {
                        override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                            var count = 0
                            var total = 0.0
                            snapshot.children.forEach { userNode ->
                                userNode.children.forEach { p ->
                                    count += 1
                                    val priceStr = p.child("price").getValue(String::class.java) ?: "Rs 0.00"
                                    // parse Rs price
                                    val numeric = priceStr.replace("Rs", "").trim().replace(",", "")
                                    total += numeric.toDoubleOrNull() ?: 0.0
                                }
                            }
                            sales.value = count
                            revenue.value = total
                        }

                        override fun onCancelled(error: com.google.firebase.database.DatabaseError) {}
                    })
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    InsightCard(Icons.Default.Description, "Total Notes", notes.size.toString(), Color(0xFFE0F7FA), modifier = Modifier.weight(1f))
                    InsightCard(Icons.Default.ShoppingCart, "Total Sales", sales.value.toString(), Color(0xFFE3F2FD), modifier = Modifier.weight(1f))
                }
            }
            item {
                val revenue = remember { mutableStateOf(0.0) }
                LaunchedEffect(Unit) {
                    val purchasesRef = FirebaseDatabase.getInstance().reference.child("purchases")
                    purchasesRef.addListenerForSingleValueEvent(object : com.google.firebase.database.ValueEventListener {
                        override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                            var total = 0.0
                            snapshot.children.forEach { userNode ->
                                userNode.children.forEach { p ->
                                    val priceStr = p.child("price").getValue(String::class.java) ?: "Rs 0.00"
                                    // parse Rs price
                                    val numeric = priceStr.replace("Rs", "").trim().replace(",", "")
                                    total += numeric.toDoubleOrNull() ?: 0.0
                                }
                            }
                            revenue.value = total
                        }

                        override fun onCancelled(error: com.google.firebase.database.DatabaseError) {}
                    })
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    InsightCard(Icons.Default.AttachMoney, "Revenue", "Rs ${String.format("%.2f", revenue.value)}", Color(0xFFE8F5E9), modifier = Modifier.weight(1f))
                    InsightCard(Icons.Default.Group, "Active Users", "-", Color(0xFFFFF3E0), modifier = Modifier.weight(1f))
                }
            }
            if (notes.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(12.dp), contentAlignment = Alignment.Center) {
                        Text("No notes yet", color = Color.Gray)
                    }
                }
            }
            items(notes, key = { it.id }) { note ->
                AdminNoteCard(
                    note = note,
                    onEdit = { editingNote = it.copy() },
                    onDelete = { noteToDelete = it },
                    onToggleEnabled = {
                        it.enabled = !it.enabled
                        if (DEMO_MODE) {
                            LocalNotesStore.save(ctxState.value, notes)
                            // Also mirror to Firebase for visibility
                            val ref = FirebaseDatabase.getInstance().reference.child("notes").child(it.id.toString())
                            ref.child("enabled").setValue(it.enabled)
                        } else {
                            // Persist toggle to Firebase
                            val ref = FirebaseDatabase.getInstance().reference.child("notes").child(it.id.toString())
                            ref.child("enabled").setValue(it.enabled)
                        }
                    }
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
                if (DEMO_MODE) {
                    LocalNotesStore.save(ctxState.value, notes)
                    // Mirror edit to Firebase for visibility
                    val ref = FirebaseDatabase.getInstance().reference.child("notes").child(updated.id.toString())
                    val data = mapOf(
                        "id" to updated.id,
                        "title" to updated.title,
                        "price" to updated.price,
                        "pdfUri" to (updated.pdfUri?.toString() ?: ""),
                        "previewImageUris" to updated.previewImageUris.map { it.toString() },
                        "category" to updated.category,
                        "description" to updated.description,
                        "enabled" to updated.enabled
                    )
                    ref.setValue(data)
                } else {
                    // Persist edit to Firebase
                    val ref = FirebaseDatabase.getInstance().reference.child("notes").child(updated.id.toString())
                    val data = mapOf(
                        "id" to updated.id,
                        "title" to updated.title,
                        "price" to updated.price,
                        "pdfUri" to (updated.pdfUri?.toString() ?: ""),
                        "previewImageUris" to updated.previewImageUris.map { it.toString() },
                        "category" to updated.category,
                        "description" to updated.description,
                        "enabled" to updated.enabled
                    )
                    ref.setValue(data)
                }
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
                val created = newNote.copy(id = nextId)
                if (DEMO_MODE) {
                    notes.add(created)
                    LocalNotesStore.save(ctxState.value, notes)
                    // Mirror create to Firebase for visibility
                    val ref = FirebaseDatabase.getInstance().reference.child("notes").child(nextId.toString())
                    val data = mapOf(
                        "id" to created.id,
                        "title" to created.title,
                        "price" to created.price,
                        "pdfUri" to (created.pdfUri?.toString() ?: ""),
                        "previewImageUris" to created.previewImageUris.map { it.toString() },
                        "category" to created.category,
                        "description" to created.description,
                        "enabled" to created.enabled
                    )
                    ref.setValue(data)
                    showUploadDialog = false
                    android.widget.Toast.makeText(ctxState.value, "Note uploaded", android.widget.Toast.LENGTH_SHORT).show()
                } else {
                    // Persist create to Firebase then update local on success
                    val ref = FirebaseDatabase.getInstance().reference.child("notes").child(nextId.toString())
                    val data = mapOf(
                        "id" to created.id,
                        "title" to created.title,
                        "price" to created.price,
                        "pdfUri" to (created.pdfUri?.toString() ?: ""),
                        "previewImageUris" to created.previewImageUris.map { it.toString() },
                        "category" to created.category,
                        "description" to created.description,
                        "enabled" to created.enabled
                    )
                    ref.setValue(data).addOnCompleteListener {
                        if (it.isSuccessful) {
                            notes.add(created)
                            showUploadDialog = false
                            android.widget.Toast.makeText(ctxState.value, "Note uploaded", android.widget.Toast.LENGTH_SHORT).show()
                        } else {
                            android.widget.Toast.makeText(ctxState.value, it.exception?.localizedMessage ?: "Upload failed", android.widget.Toast.LENGTH_LONG).show()
                        }
                    }
                }
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
                    if (DEMO_MODE) {
                        notes.removeAll { it.id == toDelete.id }
                        LocalNotesStore.save(ctxState.value, notes)
                        // Mirror delete to Firebase
                        val ref = FirebaseDatabase.getInstance().reference.child("notes").child(toDelete.id.toString())
                        ref.removeValue()
                        android.widget.Toast.makeText(ctxState.value, "Note deleted", android.widget.Toast.LENGTH_SHORT).show()
                        noteToDelete = null
                    } else {
                        // Persist delete to Firebase
                        val ref = FirebaseDatabase.getInstance().reference.child("notes").child(toDelete.id.toString())
                        ref.removeValue().addOnCompleteListener {
                            if (it.isSuccessful) {
                                notes.removeAll { it.id == toDelete.id }
                                android.widget.Toast.makeText(ctxState.value, "Note deleted", android.widget.Toast.LENGTH_SHORT).show()
                            } else {
                                android.widget.Toast.makeText(ctxState.value, it.exception?.localizedMessage ?: "Delete failed", android.widget.Toast.LENGTH_LONG).show()
                            }
                            noteToDelete = null
                        }
                    }
                }) { Text("Delete", color = Color(0xFFF44336)) }
            },
            dismissButton = {
                TextButton(onClick = { noteToDelete = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun InsightCard(icon: ImageVector, title: String, value: String, bg: Color, modifier: Modifier = Modifier) {
    Card(colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp), shape = RoundedCornerShape(12.dp), modifier = modifier) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text(value, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(title, fontSize = 12.sp, color = Color.Gray)
            }
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(bg)
                .padding(4.dp))
        }
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
                    Text(note.title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Text("Rs ${String.format("%.2f", note.price)}", color = Color(0xFF1976D2), fontSize = 13.sp)
                    Text(if (note.enabled) "Enabled" else "Disabled", color = if (note.enabled) Color(0xFF4CAF50) else Color.Gray, fontSize = 12.sp)
                }
                Icon(
                    Icons.Default.Description,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFFFFFFF))
                        .padding(8.dp)
                )
            }

            Spacer(Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { onEdit(note) }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Edit", fontSize = 14.sp)
                }
                OutlinedButton(onClick = { onToggleEnabled(note) }, modifier = Modifier.weight(1f)) {
                    Icon(if (note.enabled) Icons.Default.Block else Icons.Default.Check, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text(if (note.enabled) "Disable" else "Enable", fontSize = 14.sp)
                }
                OutlinedButton(onClick = { onDelete(note) }, modifier = Modifier.weight(1f), colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFF44336))) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Delete", fontSize = 14.sp)
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

    val context = LocalContext.current
    val pickPdf = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (_: Exception) {}
            pdfUri = uri
        }
    }
    val pickImages = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        if (uris != null) {
            images = uris
            uris.forEach {
                try {
                    context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                } catch (_: Exception) {}
            }
        }
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

    val context = LocalContext.current
    val pickPdf = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (_: Exception) {}
            pdfUri = uri
        }
    }
    val pickImages = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        if (uris != null) {
            images = uris
            uris.forEach {
                try {
                    context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                } catch (_: Exception) {}
            }
        }
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

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun AdminDashboardActivityPreview() {
    PebbleNoteTheme {
        AdminDashboardScreen()
    }
}