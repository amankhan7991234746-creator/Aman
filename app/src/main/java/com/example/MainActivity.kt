package com.example

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas as ComposeCanvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke as DrawScopeStroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.data.Stroke
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.StudyViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: StudyViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = Color.Transparent
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        StudyWorkspaceScreen(viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun StudyWorkspaceScreen(viewModel: StudyViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val isCollab by viewModel.isCollaborative.collectAsState()
    val streak by viewModel.streakCount.collectAsState()
    val activeInviteLink by viewModel.sessionInviteLink.collectAsState()
    val activeCollaborators by viewModel.activeCollaborators.collectAsState()

    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                drawRect(Color(0xFF090414))
                drawCircle(
                    color = Color(0xFF673AB7).copy(alpha = 0.24f),
                    radius = size.minDimension * 0.44f,
                    center = Offset(size.width * 0.15f, size.height * 0.25f)
                )
                drawCircle(
                    color = Color(0xFF00E5FF).copy(alpha = 0.18f),
                    radius = size.minDimension * 0.38f,
                    center = Offset(size.width * 0.85f, size.height * 0.75f)
                )
                drawCircle(
                    color = Color(0xFFE91E63).copy(alpha = 0.12f),
                    radius = size.minDimension * 0.32f,
                    center = Offset(size.width * 0.50f, size.height * 0.45f)
                )
            }
    ) {
        // Left Column (Main App & Screen Workspace)
        Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
            // Header
            HeaderBar(
                onProfileClick = {
                    Toast.makeText(context, "Aman's Luxury Productivity Profile", Toast.LENGTH_SHORT).show()
                },
                streakCount = streak,
                isCollabActive = isCollab,
                onCollabToggle = { viewModel.toggleCollaborativeSession() }
            )

            // Dynamic Invitation Ribbon
            if (isCollab && activeInviteLink.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp).border(borderLayout(), RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF4F378B).copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Collab", tint = Color(0xFFD0BCFF), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Link: $activeInviteLink",
                            fontSize = 11.sp,
                            color = Color(0xFFD0BCFF),
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Classmates: ${activeCollaborators.joinToString(", ")}",
                            fontSize = 10.sp,
                            color = Color(0xFFCAC4D0)
                        )
                    }
                }
            }

            // Screen Area
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when (currentScreen) {
                    "DASHBOARD" -> DashboardScreen(viewModel)
                    "CANVAS" -> CanvasStudio(viewModel)
                    "PDF_READER" -> SplitBookWorkspace(viewModel)
                    "FLASHCARDS" -> FlashcardsDeckView(viewModel)
                    "SEARCH" -> AcademicSearchView(viewModel)
                    "AMAN_EYE" -> AmanEyeScreen(viewModel)
                }
            }

            // Bottom Navigation system
            BottomNav(currentScreen, onSelect = { viewModel.navigateTo(it) })
        }

        VerticalDivider(color = Color.White.copy(alpha = 0.08f), modifier = Modifier.fillMaxHeight().width(1.dp))

        // Right side-pane: AI study assistant
        Column(
            modifier = Modifier.width(360.dp).fillMaxHeight()
                .background(Color.White.copy(alpha = 0.02f))
                .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)))
        ) {
            AITutorSidebar(viewModel)
        }
    }
}

@Composable
fun HeaderBar(
    onProfileClick: () -> Unit,
    streakCount: Int,
    isCollabActive: Boolean,
    onCollabToggle: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.clickable { onProfileClick() },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFFD0BCFF)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "AM", color = Color(0xFF381E72), fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Aman's OS", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFE6E1E5))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "PRO", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD0BCFF), modifier = Modifier.background(Color(0xFF381E72), RoundedCornerShape(4.dp)).padding(horizontal = 4.dp, vertical = 2.dp))
                }
                Text(text = "PREMIUM STUDY WORKSTATION", fontSize = 9.sp, color = Color(0xFFCAC4D0))
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // Hot Streak Indicator
            Row(
                modifier = Modifier.background(Color(0xFF2B2B2B), RoundedCornerShape(16.dp)).padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "🔥", fontSize = 12.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "$streakCount Days", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            // Real-Time Sync trigger
            Button(
                onClick = onCollabToggle,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isCollabActive) Color(0xFF81C784) else Color(0xFF2B2B2B)
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.height(34.dp).testTag("collab_toggle_button")
            ) {
                Icon(
                    imageVector = if (isCollabActive) Icons.Default.CloudQueue else Icons.Default.CloudOff,
                    contentDescription = "Sync",
                    modifier = Modifier.size(16.dp),
                    tint = if (isCollabActive) Color(0xFF1E1E1E) else Color(0xFFD0BCFF)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isCollabActive) "COLLAB ACTIVE" else "SESSION LINK",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isCollabActive) Color(0xFF1E1E1E) else Color(0xFFCAC4D0)
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(viewModel: StudyViewModel) {
    val folders by viewModel.folders.collectAsState()
    val currentFolder by viewModel.selectedFolder.collectAsState()
    val allTasks by viewModel.tasksForFolder.collectAsState()
    val textbooks by viewModel.textbooksForFolder.collectAsState()
    val activityLogs by viewModel.amanActivityLogs.collectAsState()

    val pomodoroTime by viewModel.pomodoroTimeLeft.collectAsState()
    val pomodoroRunning by viewModel.isPomodoroRunning.collectAsState()

    var showNewFolderDialog by remember { mutableStateOf(false) }
    var folderInputName by remember { mutableStateOf("") }
    var folderInputEmoji by remember { mutableStateOf("📚") }

    var newTaskDialog by remember { mutableStateOf(false) }
    var newTaskTitle by remember { mutableStateOf("") }
    var newTaskPriority by remember { mutableStateOf("MEDIUM") }

    var newBookDialog by remember { mutableStateOf(false) }
    var newBookTitle by remember { mutableStateOf("") }
    var newBookContent by remember { mutableStateOf("") }

    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome and Streak Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth().border(borderLayout(), RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                shape = RoundedCornerShape(24.dp)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Welcome back, Aman! ✨", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(text = "Ready for high-performance S-Pen handwriting studies today?", fontSize = 12.sp, color = Color(0xFFCAC4D0), modifier = Modifier.padding(top = 2.dp))
                    }
                    Button(
                        onClick = {
                            newTaskDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD0BCFF))
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add", tint = Color(0xFF381E72))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Assign Target Task", color = Color(0xFF381E72), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Secure Biometric Vault Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.navigateTo("AMAN_EYE") }
                    .border(
                        BorderStroke(
                            1.5.dp,
                            androidx.compose.ui.graphics.Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFFD0BCFF).copy(0.4f),
                                    Color(0xFFE91E63).copy(0.1f)
                                )
                            )
                        ),
                        RoundedCornerShape(20.dp)
                    ),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF381E72).copy(alpha = 0.15f)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(44.dp).clip(CircleShape).background(Color(0xFF381E72)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("👁️", fontSize = 20.sp)
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "AMAN'S EYE PRIVATE VAULT", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD0BCFF))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "SECURE", fontSize = 8.sp, color = Color(0xFF81C784), fontWeight = FontWeight.Bold, modifier = Modifier.border(1.dp, Color(0xFF81C784).copy(0.4f), RoundedCornerShape(4.dp)).padding(horizontal = 4.dp, vertical = 1.dp))
                            }
                            Text(text = "Unfolds Spaced Repetition systems, STEM simulators, concept maps & audio podcasts.", fontSize = 11.sp, color = Color(0xFFCAC4D0), modifier = Modifier.padding(top = 1.dp))
                        }
                    }
                    Box(
                        modifier = Modifier.background(Color(0xFF381E72).copy(0.4f), RoundedCornerShape(12.dp)).padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(text = "UNLOCK VAULT ➔", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD0BCFF))
                    }
                }
            }
        }

        // Folder & Book Library Hub
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "📚 DIGITAL BOOK LIBRARY & FOLDER MANAGEMENT", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD0BCFF))
                    IconButton(onClick = { showNewFolderDialog = true }) {
                        Icon(Icons.Default.CreateNewFolder, contentDescription = "Create", tint = Color(0xFFD0BCFF))
                    }
                }

                // Horizontal list of folders
                FlowRow(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    folders.forEach { f ->
                        val isSelected = currentFolder?.id == f.id
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) Color(0xFF381E72) else Color(0xFF1E1E1E))
                                .border(1.dp, if (isSelected) Color(0xFFD0BCFF) else Color.White.copy(0.08f), RoundedCornerShape(12.dp))
                                .clickable {
                                    viewModel.selectFolder(f)
                                }
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = f.icon, fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = f.name, fontSize = 12.sp, color = if (isSelected) Color.White else Color(0xFFCAC4D0), fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }

        // Middle Section: Textbooks in selected folder & Pomodoro Focus Timer
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                // Textbook Library List
                Card(
                    modifier = Modifier.weight(1.2f).height(240.dp).border(borderLayout(), RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.04f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "Folder textbooks / PDFs", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD0BCFF))
                            Button(
                                onClick = { newBookDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2B2B2B)),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text("+ Import", fontSize = 10.sp, color = Color(0xFFD0BCFF))
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        if (textbooks.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(text = "No PDFs in this subject yet. Upload or Import above!", color = Color.Gray, fontSize = 11.sp)
                            }
                        } else {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(textbooks) { book ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFF2B2B2B))
                                            .clickable { viewModel.selectTextbook(book) }
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Book, contentDescription = "Book", tint = Color(0xFFD0BCFF), modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(text = book.title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            Text(text = "By ${book.author} • Page ${book.currentPage}/${book.totalPages}", fontSize = 9.sp, color = Color.LightGray)
                                        }
                                        Icon(Icons.Default.ArrowForwardIos, contentDescription = "Go", tint = Color.Gray, modifier = Modifier.size(10.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                // Pomodoro Focus Timer
                Card(
                    modifier = Modifier.weight(0.8f).height(240.dp).border(borderLayout(), RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.04f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp).fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "🍅 Focus Pomodoro Sprint", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD0BCFF))

                        // Custom animated radial progress representation
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(110.dp)) {
                            CircularProgressIndicator(
                                progress = pomodoroTime / 1500f,
                                modifier = Modifier.fillMaxSize(),
                                color = Color(0xFFF44336),
                                strokeWidth = 8.dp,
                                trackColor = Color.White.copy(0.08f)
                            )
                            val minutes = pomodoroTime / 60
                            val seconds = pomodoroTime % 60
                            Text(
                                text = String.format("%02d:%02d", minutes, seconds),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { viewModel.togglePomodoro() },
                                colors = ButtonDefaults.buttonColors(containerColor = if (pomodoroRunning) Color(0xFF9C27B0) else Color(0x33D0BCFF)),
                                modifier = Modifier.weight(1f).height(32.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(text = if (pomodoroRunning) "PAUSE" else "START", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { viewModel.resetPomodoro() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                border = BorderStroke(1.dp, Color.White.copy(0.12f)),
                                modifier = Modifier.weight(1f).height(32.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(text = "RESET", fontSize = 10.sp, color = Color(0xFFCAC4D0))
                            }
                        }
                    }
                }
            }
        }

        // Bottom Segment: Study Planner Checklist & Share/Import logging activity feed
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                // Checklist
                Card(
                    modifier = Modifier.weight(1f).height(240.dp).border(borderLayout(), RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.04f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "📋 Revision Planner Checklist", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD0BCFF))
                            Text(text = "${allTasks.count { it.isCompleted }}/${allTasks.size} Done", fontSize = 10.sp, color = Color.LightGray)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        if (allTasks.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(text = "No study tasks registered. Use + above to plan!", color = Color.Gray, fontSize = 11.sp)
                            }
                        } else {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(allTasks) { t ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Color(0xFF2B2B2B)).padding(horizontal = 8.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = t.isCompleted,
                                            onCheckedChange = { viewModel.toggleTask(t.id, it) },
                                            colors = CheckboxDefaults.colors(checkedColor = Color(0xFFD0BCFF))
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = t.title,
                                            fontSize = 11.sp,
                                            color = if (t.isCompleted) Color.Gray else Color.White,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.weight(1f),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Box(
                                            modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(
                                                when (t.priority) {
                                                    "HIGH" -> Color(0xFFD32F2F).copy(0.2f)
                                                    "MEDIUM" -> Color(0xFFFF9800).copy(0.2f)
                                                    else -> Color(0xFF4CAF50).copy(0.2f)
                                                }
                                            ).padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = t.priority,
                                                color = when (t.priority) {
                                                    "HIGH" -> Color(0xFFEF5350)
                                                    "MEDIUM" -> Color(0xFFFFB74D)
                                                    else -> Color(0xFF81C784)
                                                },
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        IconButton(onClick = { viewModel.deleteTask(t.id) }, modifier = Modifier.size(24.dp)) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray, modifier = Modifier.size(14.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Share & Imports activity monitor
                Card(
                    modifier = Modifier.weight(1f).height(240.dp).border(borderLayout(), RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.04f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "📤 Aman's Shares & Import activity center", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD0BCFF))
                        Spacer(modifier = Modifier.height(10.dp))
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(activityLogs) { log ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().border(1.dp, Color.White.copy(0.04f), RoundedCornerShape(6.dp)).padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = "Log", tint = Color(0xFFCCC2DC), modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = log, fontSize = 10.sp, color = Color(0xFFE6E1E5))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal dialog for adding foldered subject categories
    if (showNewFolderDialog) {
        AlertDialog(
            onDismissRequest = { showNewFolderDialog = false },
            containerColor = Color(0xFF2B2B2B),
            title = { Text("Create Foldered Subject Category", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = folderInputName,
                        onValueChange = { folderInputName = it },
                        label = { Text("Subject Name (e.g. Astrophysics)") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                    OutlinedTextField(
                        value = folderInputEmoji,
                        onValueChange = { folderInputEmoji = it },
                        label = { Text("Emoji Representation") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (folderInputName.isNotBlank()) {
                        viewModel.createNewFolder(folderInputName, folderInputEmoji)
                        folderInputName = ""
                        showNewFolderDialog = false
                    }
                }) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewFolderDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Modal dialogue for checklist assignments
    if (newTaskDialog) {
        AlertDialog(
            onDismissRequest = { newTaskDialog = false },
            containerColor = Color(0xFF2B2B2B),
            title = { Text("Assign New Study Checklist Task", color = Color.White, fontSize = 14.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newTaskTitle,
                        onValueChange = { newTaskTitle = it },
                        label = { Text("Task description") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("Priority:", fontSize = 11.sp, color = Color.LightGray)
                        listOf("HIGH", "MEDIUM", "LOW").forEach { p ->
                            Button(
                                onClick = { newTaskPriority = p },
                                colors = ButtonDefaults.buttonColors(containerColor = if (newTaskPriority == p) Color(0xFFD0BCFF) else Color(0x33FFFFFF)),
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                Text(p, fontSize = 9.sp, color = if (newTaskPriority == p) Color.Black else Color.White)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (newTaskTitle.isNotBlank()) {
                        viewModel.addNewTask(newTaskTitle, newTaskPriority)
                        newTaskTitle = ""
                        newTaskDialog = false
                    }
                }) {
                    Text("Add")
                }
            }
        )
    }

    // Modal dialog to import textbook notes
    if (newBookDialog) {
        AlertDialog(
            onDismissRequest = { newBookDialog = false },
            containerColor = Color(0xFF2B2B2B),
            title = { Text("Aman's Study Book/PDF Import Center", color = Color.White, fontSize = 14.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newBookTitle,
                        onValueChange = { newBookTitle = it },
                        label = { Text("Book Chapter or Document Title") }
                    )
                    OutlinedTextField(
                        value = newBookContent,
                        onValueChange = { newBookContent = it },
                        label = { Text("Insert academic textual content or formulas") },
                        maxLines = 8,
                        modifier = Modifier.height(140.dp)
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (newBookTitle.isNotBlank() && newBookContent.isNotBlank()) {
                        viewModel.importAmanBook(newBookTitle, newBookContent)
                        newBookTitle = ""
                        newBookContent = ""
                        newBookDialog = false
                    }
                }) {
                    Text("Upload PDF Content")
                }
            }
        )
    }
}

@Composable
fun CanvasStudio(viewModel: StudyViewModel) {
    val strokes by viewModel.drawingStrokes.collectAsState()
    val activeTool by viewModel.currentTool.collectAsState()
    val brushColor by viewModel.canvasColor.collectAsState()
    val brushWidth by viewModel.canvasBrushWidth.collectAsState()
    val bgType by viewModel.canvasBgType.collectAsState()
    val isCollab by viewModel.isCollaborative.collectAsState()
    val curCollabCursor by viewModel.collaboratorCursor.collectAsState()

    var activePoints = remember { mutableStateListOf<StrokePoint>() }
    val currentDrawingName = viewModel.selectedDrawing.collectAsState().value?.name ?: "Scratchpad"

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Workspace Toolbar
        Row(
            modifier = Modifier.fillMaxWidth().background(Color(0xFF1E1E1E), RoundedCornerShape(16.dp)).padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "✏️ Canvas: $currentDrawingName", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text(text = if (isCollab) "🟠 Collaboration Sync Activated" else "Local session mode", fontSize = 9.sp, color = Color.Gray)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                // Brush Color buttons
                val palette = listOf(0xFFD0BCFF.toInt(), 0xFF81C784.toInt(), 0xFFE57373.toInt(), 0xFFFFF176.toInt(), 0xFFFFFFFF.toInt())
                palette.forEach { cInt ->
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(Color(cInt))
                            .border(1.dp, if (brushColor == cInt) Color.White else Color.Transparent, CircleShape)
                            .clickable { viewModel.setCanvasColor(cInt) }
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Brush width options
                listOf(3f, 6f, 12f, 24f).forEach { size ->
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(if (brushWidth == size) Color(0xFF381E72) else Color.Transparent)
                            .clickable { viewModel.setCanvasBrushWidth(size) },
                        contentAlignment = Alignment.Center
                    ) {
                        Box(modifier = Modifier.size(size.coerceIn(2f, 10f).dp).clip(CircleShape).background(Color.White))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Tool Control Bar (Eraser, Pen, Highlighter, Background, Undo)
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left block
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Button(
                    onClick = { viewModel.setCanvasTool("PEN") },
                    colors = ButtonDefaults.buttonColors(containerColor = if (activeTool == "PEN") Color(0xFFD0BCFF) else Color(0xFF2B2B2B)),
                    modifier = Modifier.height(28.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Text("Pen", fontSize = 10.sp, color = if (activeTool == "PEN") Color.Black else Color.White)
                }

                Button(
                    onClick = { viewModel.setCanvasTool("HIGHLIGHTER") },
                    colors = ButtonDefaults.buttonColors(containerColor = if (activeTool == "HIGHLIGHTER") Color(0xFF81C784) else Color(0xFF2B2B2B)),
                    modifier = Modifier.height(28.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Text("Highlight", fontSize = 10.sp, color = if (activeTool == "HIGHLIGHTER") Color.Black else Color.White)
                }

                Button(
                    onClick = { viewModel.setCanvasTool("ERASER") },
                    colors = ButtonDefaults.buttonColors(containerColor = if (activeTool == "ERASER") Color(0xFFE57373) else Color(0xFF2B2B2B)),
                    modifier = Modifier.height(28.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Text("Eraser", fontSize = 10.sp, color = if (activeTool == "ERASER") Color.Black else Color.White)
                }
            }

            // Middle block: Background switches
            Row(
                modifier = Modifier.background(Color(0xFF1E1E1E), RoundedCornerShape(12.dp)).padding(2.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                listOf("BLANK", "LINED", "GRID").forEach { bg ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (bgType == bg) Color.White.copy(0.12f) else Color.Transparent)
                            .clickable { viewModel.updateDrawingBackground(bg) }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(bg, fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Right block: Actions
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = { viewModel.performUndo() }, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Undo, contentDescription = "Undo", tint = Color.White, modifier = Modifier.size(16.dp))
                }
                IconButton(onClick = { viewModel.performRedo() }, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Redo, contentDescription = "Redo", tint = Color.White, modifier = Modifier.size(16.dp))
                }
                IconButton(onClick = { viewModel.clearCanvas() }, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Clear", tint = Color(0xFFE57373), modifier = Modifier.size(16.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Actual interactive drawing area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF1C1C1C))
                .border(borderLayout(), RoundedCornerShape(24.dp))
                .pointerInput(activeTool) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            activePoints.clear()
                            activePoints.add(StrokePoint(offset.x, offset.y))
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            activePoints.add(StrokePoint(change.position.x, change.position.y))
                        },
                        onDragEnd = {
                            if (activePoints.isNotEmpty()) {
                                if (activeTool == "ERASER") {
                                    // Eraser mode: filter out strokes close to the drawing path
                                    // For simplicity, we just filter out drawing strokes matching close indices
                                    val eraserPathPoints = activePoints.toList()
                                    val remaining = strokes.filterNot { s ->
                                        s.points.any { pt ->
                                            eraserPathPoints.any { erPt ->
                                                Math.abs(pt.x - erPt.x) < 25f && Math.abs(pt.y - erPt.y) < 25f
                                            }
                                        }
                                    }
                                    // Re-save set
                                    viewModel.clearCanvas()
                                    for (st in remaining) {
                                        viewModel.addStrokeToCanvas(st)
                                    }
                                } else {
                                    val newStroke = Stroke(
                                        points = activePoints.toList(),
                                        color = if (activeTool == "HIGHLIGHTER") Color(brushColor).copy(0.4f).value.toInt() else brushColor,
                                        width = if (activeTool == "HIGHLIGHTER") brushWidth * 2.5f else brushWidth,
                                        isHighlighter = (activeTool == "HIGHLIGHTER")
                                    )
                                    viewModel.addStrokeToCanvas(newStroke)
                                }
                                activePoints.clear()
                            }
                        }
                    )
                }
                .testTag("drawing_canvas_workspace")
        ) {
            // Draw background grid lines based on type
            ComposeCanvas(modifier = Modifier.fillMaxSize()) {
                val canvasWidth = size.width
                val canvasHeight = size.height

                // Draw background pattern
                if (bgType == "LINED") {
                    var y = 40f
                    while (y < canvasHeight) {
                        drawLine(
                            color = Color(0x16FFFFFF),
                            start = Offset(0f, y),
                            end = Offset(canvasWidth, y),
                            strokeWidth = 1.dp.toPx()
                        )
                        y += 40f
                    }
                } else if (bgType == "GRID") {
                    var x = 40f
                    while (x < canvasWidth) {
                        drawLine(
                            color = Color(0x10FFFFFF),
                            start = Offset(x, 0f),
                            end = Offset(x, canvasHeight),
                            strokeWidth = 0.8.dp.toPx()
                        )
                        x += 40f
                    }
                    var y = 40f
                    while (y < canvasHeight) {
                        drawLine(
                            color = Color(0x10FFFFFF),
                            start = Offset(0f, y),
                            end = Offset(canvasWidth, y),
                            strokeWidth = 0.8.dp.toPx()
                        )
                        y += 40f
                    }
                }

                // Draw standard persistent strokes
                strokes.forEach { stroke ->
                    if (stroke.points.size > 1) {
                        val path = Path()
                        val start = stroke.points.first()
                        path.moveTo(start.x, start.y)
                        for (i in 1..stroke.points.lastIndex) {
                            val pt = stroke.points[i]
                            path.lineTo(pt.x, pt.y)
                        }
                        drawPath(
                            path = path,
                            color = Color(stroke.color),
                            style = DrawScopeStroke(
                                width = stroke.width,
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )
                    }
                }

                // Draw current active ongoing drag stroke
                if (activePoints.size > 1 && activeTool != "ERASER") {
                    val path = Path()
                    val start = activePoints.first()
                    path.moveTo(start.x, start.y)
                    for (i in 1..activePoints.lastIndex) {
                        val pt = activePoints[i]
                        path.lineTo(pt.x, pt.y)
                    }
                    drawPath(
                        path = path,
                        color = if (activeTool == "HIGHLIGHTER") Color(brushColor).copy(alpha = 0.4f) else Color(brushColor),
                        style = DrawScopeStroke(
                            width = if (activeTool == "HIGHLIGHTER") brushWidth * 2.5f else brushWidth,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }
            }

            // Real-Time Collaborator Hover cursor (Green point matching Sarah)
            curCollabCursor?.let { cursor ->
                Box(
                    modifier = Modifier
                        .offset(x = cursor.first.dp, y = cursor.second.dp)
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF81C784))
                )
                Box(
                    modifier = Modifier
                        .offset(x = (cursor.first + 14).dp, y = cursor.second.dp)
                        .background(Color(0xFF81C784), RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text("Sarah Stone is Annotation-Syncing...", fontSize = 7.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }

            // Little tips helper overlay UI
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
                    .background(Color.Black.copy(0.6f), RoundedCornerShape(8.dp))
                    .padding(6.dp)
            ) {
                Text(
                    text = "Aman's Stylus engine fully supports pointer dynamics & annotation layers.",
                    fontSize = 8.sp,
                    color = Color.LightGray
                )
            }
        }
    }
}

@Composable
fun SplitBookWorkspace(viewModel: StudyViewModel) {
    val selectedBook by viewModel.selectedTextbook.collectAsState()
    val strokes by viewModel.drawingStrokes.collectAsState()
    val activeTool by viewModel.currentTool.collectAsState()
    val brushColor by viewModel.canvasColor.collectAsState()
    val bgType by viewModel.canvasBgType.collectAsState()

    var activePoints = remember { mutableStateListOf<StrokePoint>() }

    if (selectedBook == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Please select a textbook or import a book from Aman's Dashboard to begin side-by-side reading.", color = Color.Gray, fontSize = 12.sp)
        }
        return
    }

    val book = selectedBook!!

    Row(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        // Left side: textbook reading page
        Card(
            modifier = Modifier.weight(1f).fillMaxHeight().border(borderLayout(), RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1C)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Book Title Header
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = book.title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(text = "By ${book.author}", fontSize = 10.sp, color = Color.Gray)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { if (book.currentPage > 1) viewModel.updateBookReadingProgress(book, book.currentPage - 1) },
                            enabled = book.currentPage > 1
                        ) {
                            Icon(Icons.Default.NavigateBefore, contentDescription = "Prev", tint = if (book.currentPage > 1) Color.White else Color.DarkGray)
                        }
                        Text(text = "${book.currentPage} / ${book.totalPages}", fontSize = 11.sp, color = Color.White)
                        IconButton(
                            onClick = { if (book.currentPage < book.totalPages) viewModel.updateBookReadingProgress(book, book.currentPage + 1) },
                            enabled = book.currentPage < book.totalPages
                        ) {
                            Icon(Icons.Default.NavigateNext, contentDescription = "Next", tint = if (book.currentPage < book.totalPages) Color.White else Color.DarkGray)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Page Reading Area
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color.Black.copy(alpha = 0.40f)).border(borderLayout(), RoundedCornerShape(12.dp)).padding(14.dp)
                ) {
                    ScrollViewText(text = book.content, page = book.currentPage)
                }

                Spacer(modifier = Modifier.height(10.dp))

                // OCR explanation query button
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { viewModel.askAITutor("Focusing on the textbook chapter titled '${book.title}', please summarize key terms and explain formulas in this section.") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF381E72)),
                        modifier = Modifier.weight(1.2f).height(32.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("AI Smart Query Summary", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { viewModel.generateTextbookFlashcards() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2B2B2B)),
                        modifier = Modifier.weight(1f).height(32.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("💡 Generate Flashcards", fontSize = 10.sp, color = Color(0xFFD0BCFF))
                    }
                }
            }
        }

        // Right side: interactive annotation / stylus workspace notepad (split screen)
        Card(
            modifier = Modifier.weight(1f).fillMaxHeight().border(borderLayout(), RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🖋️ Split Workspace Notebook", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD0BCFF))
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(onClick = { viewModel.performUndo() }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Undo, contentDescription = "Undo", tint = Color.LightGray, modifier = Modifier.size(14.dp))
                        }
                        IconButton(onClick = { viewModel.clearCanvas() }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Delete, contentDescription = "Clear", tint = Color(0xFFE57373), modifier = Modifier.size(14.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Canvas drawing arena
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black.copy(alpha = 0.40f))
                        .border(borderLayout(), RoundedCornerShape(12.dp))
                        .pointerInput(activeTool) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    activePoints.clear()
                                    activePoints.add(StrokePoint(offset.x, offset.y))
                                },
                                onDrag = { change, _ ->
                                    change.consume()
                                    activePoints.add(StrokePoint(change.position.x, change.position.y))
                                },
                                onDragEnd = {
                                    if (activePoints.isNotEmpty()) {
                                        val newStr = Stroke(
                                            points = activePoints.toList(),
                                            color = brushColor,
                                            width = 5f
                                        )
                                        viewModel.addStrokeToCanvas(newStr)
                                        activePoints.clear()
                                    }
                                }
                            )
                        }
                ) {
                    ComposeCanvas(modifier = Modifier.fillMaxSize()) {
                        // Drawing grid background dynamically
                        if (bgType == "GRID") {
                            var x = 30f
                            while (x < size.width) {
                                drawLine(Color(0x0EFFFFFF), Offset(x, 0f), Offset(x, size.height), 0.5.dp.toPx())
                                x += 30f
                            }
                            var y = 30f
                            while (y < size.height) {
                                drawLine(Color(0x0EFFFFFF), Offset(0f, y), Offset(size.width, y), 0.5.dp.toPx())
                                y += 30f
                            }
                        } else {
                            // Default to nice lined notebooks
                            var y = 30f
                            while (y < size.height) {
                                drawLine(Color(0x12FFFFFF), Offset(0f, y), Offset(size.width, y), 0.8.dp.toPx())
                                y += 30f
                            }
                        }

                        // Drawing persistent annotations/sketches
                        strokes.forEach { s ->
                            if (s.points.size > 1) {
                                val path = Path()
                                path.moveTo(s.points[0].x, s.points[0].y)
                                for (p in s.points) {
                                    path.lineTo(p.x, p.y)
                                }
                                drawPath(
                                    path = path,
                                    color = Color(s.color),
                                    style = DrawScopeStroke(width = s.width, cap = StrokeCap.Round)
                                )
                            }
                        }

                        // Drawing live drawing gesture
                        if (activePoints.size > 1) {
                            val path = Path()
                            path.moveTo(activePoints[0].x, activePoints[0].y)
                            for (p in activePoints) {
                                path.lineTo(p.x, p.y)
                            }
                            drawPath(path = path, color = Color(brushColor), style = DrawScopeStroke(width = 5f, cap = StrokeCap.Round))
                        }
                    }

                    Text("Active Annotations Page Sync", fontSize = 8.sp, color = Color.Gray, modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp))
                }
            }
        }
    }
}

@Composable
fun ScrollViewText(text: String, page: Int) {
    // Basic text divider to separate text chapters based on current pages
    val paragraphs = text.split("\n\n")
    val displayIndex = (page - 1).coerceIn(0, paragraphs.lastIndex)
    val displayText = paragraphs.getOrNull(displayIndex) ?: text

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Text(
                text = displayText,
                color = Color(0xFFE6E1E5),
                fontSize = 12.sp,
                lineHeight = 18.sp,
                fontFamily = FontFamily.Serif
            )
        }
    }
}

@Composable
fun FlashcardsDeckView(viewModel: StudyViewModel) {
    val cards by viewModel.flashcardsForFolder.collectAsState()
    var currentCardIndex by remember { mutableIntStateOf(0) }
    var isFlipped by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(text = "🎓 ACTIVE-RECALL QUIZ FLASHCARDS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD0BCFF))

        if (cards.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(text = "No study cards created for this folder yet.\nSelect textbook next door and press '+ Flashcards'!", color = Color.LightGray, fontSize = 11.sp, textAlign = TextAlign.Center)
            }
        } else {
            val safeIndex = currentCardIndex.coerceIn(0, cards.lastIndex)
            val currentCard = cards[safeIndex]

            Card(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(0.85f)
                    .clip(RoundedCornerShape(32.dp))
                    .clickable { isFlipped = !isFlipped }
                    .border(2.dp, Color(0xFFD0BCFF).copy(0.4f), RoundedCornerShape(32.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
            ) {
                Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        Text(
                            text = if (isFlipped) "🔑 EXPLANATION" else "❓ ACTIVE RECALL QUESTION",
                            color = if (isFlipped) Color(0xFF81C784) else Color(0xFFFFF176),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            modifier = Modifier.background(Color.Black.copy(0.4f), RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = if (isFlipped) currentCard.back else currentCard.front,
                            fontSize = 15.sp,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = 22.sp
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Tap Card element to flip & verify",
                            fontSize = 9.sp,
                            color = Color.Gray,
                            fontStyle = FontStyle.Italic
                        )
                    }
                }
            }

            // Controls
            Row(
                modifier = Modifier.fillMaxWidth(0.9f),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.deleteCard(currentCard.id) }) {
                    Icon(Icons.Default.DeleteForever, contentDescription = "Delete", tint = Color(0xFFE57373))
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = {
                            isFlipped = false
                            if (currentCardIndex > 0) currentCardIndex-- else currentCardIndex = cards.lastIndex
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2B2B2B))
                    ) {
                        Text("Previous")
                    }

                    Button(
                        onClick = {
                            isFlipped = false
                            if (currentCardIndex < cards.lastIndex) currentCardIndex++ else currentCardIndex = 0
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF381E72))
                    ) {
                        Text("Next Card")
                    }
                }

                Text(text = "${safeIndex + 1} / ${cards.size}", fontSize = 11.sp, color = Color.White)
            }
        }
    }
}

@Composable
fun AcademicSearchView(viewModel: StudyViewModel) {
    val searchResults by viewModel.searchResults.collectAsState()
    val searchIsLoading by viewModel.searchIsLoading.collectAsState()
    var queryInput by remember { mutableStateOf("") }

    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(text = "🌐 INBUILT WEB & GOOGLE SCHOLAR INDEX SEARCH", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD0BCFF))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = queryInput,
                onValueChange = { queryInput = it },
                label = { Text("Query formula, theory or research papers...") },
                modifier = Modifier.weight(1f).testTag("academic_search_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedLabelColor = Color(0xFFD0BCFF),
                    unfocusedLabelColor = Color.Gray,
                    focusedBorderColor = Color(0xFFD0BCFF)
                )
            )

            Button(
                onClick = { viewModel.executeInbuiltGoogleSearch(queryInput) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD0BCFF)),
                modifier = Modifier.height(56.dp).testTag("academic_search_button")
            ) {
                Icon(Icons.Default.Search, contentDescription = "Query", tint = Color(0xFF381E72))
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        if (searchIsLoading) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFFD0BCFF))
            }
        } else {
            if (searchResults.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("Search a scientific concept or equation and tap the search query button above.", color = Color.Gray, fontSize = 12.sp)
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(searchResults) { r ->
                        Card(
                            modifier = Modifier.fillMaxWidth().border(1.dp, Color.White.copy(0.06f), RoundedCornerShape(16.dp)),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1C))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = r.title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD0BCFF), maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                                    Box(modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(Color(0xFF381E72)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                                        Text(text = r.source, fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(text = r.snippet, fontSize = 10.sp, color = Color(0xFFE6E1E5), lineHeight = 15.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(text = "Verified Study Reference Paper", fontSize = 8.sp, color = Color.Gray, fontStyle = FontStyle.Italic)
                                    Text(
                                        text = "Open Reference URL",
                                        fontSize = 10.sp,
                                        color = Color(0xFF81C784),
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.clickable {
                                            Toast.makeText(context, "Redirecting Aman to scholarly literature database...", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AITutorSidebar(viewModel: StudyViewModel) {
    val reply by viewModel.aiTutorReply.collectAsState()
    val isAiLoading by viewModel.aiIsLoading.collectAsState()
    val currentFolder by viewModel.selectedFolder.collectAsState()
    val strokes by viewModel.drawingStrokes.collectAsState()

    var chatTextInput by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Sidebar header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF81C784)))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = "✨ OPULENT AI STUDY TUTOR", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD0BCFF), letterSpacing = 0.5.sp)
        }

        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Color.White.copy(0.04f)).padding(8.dp)
        ) {
            Text(
                text = "Academic Context: ${currentFolder?.name ?: "General Learning"}",
                fontSize = 9.sp,
                color = Color.LightGray
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Chat response viewport (Gemini Academic Responses)
        Card(
            modifier = Modifier.weight(1f).fillMaxWidth().border(BorderStroke(1.dp, Color.White.copy(0.04f)), RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF141414)),
            shape = RoundedCornerShape(12.dp)
        ) {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                item {
                    if (isAiLoading) {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(color = Color(0xFFD0BCFF), modifier = Modifier.size(30.dp))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("Consulting Gemini models & textbooks...", fontSize = 10.sp, color = Color.Gray)
                        }
                    } else {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "🤖 Academic Guide AI", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD0BCFF))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "• Now active", fontSize = 8.sp, color = Color(0xFF81C784))
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = reply,
                                fontSize = 11.sp,
                                color = Color(0xFFFFFBFE),
                                lineHeight = 16.sp,
                                fontFamily = FontFamily.Serif
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Instant tools: OCR handwriting snapshot discuss trigger
        Button(
            onClick = {
                // Generate a quick mock bitmap of the canvas strokes for explaining
                val mockBitmap = Bitmap.createBitmap(800, 800, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(mockBitmap)
                val paint = Paint().apply {
                    color = android.graphics.Color.WHITE
                    strokeWidth = 5f
                    color = android.graphics.Color.BLUE
                }
                // Simulate drawing something
                canvas.drawLine(100f, 100f, 700f, 700f, paint)
                viewModel.doOCRAnalysisOnCurrentNotepad(mockBitmap)
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2B2B2B)),
            modifier = Modifier.fillMaxWidth().height(32.dp).testTag("ocr_analysis_trigger"),
            contentPadding = PaddingValues(0.dp)
        ) {
            Icon(Icons.Default.CameraAlt, contentDescription = "OCR", tint = Color(0xFFD0BCFF), modifier = Modifier.size(12.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Discuss Handwriting OCR Snapshot", fontSize = 9.sp, color = Color(0xFFD0BCFF))
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Chat text Input box
        Row(
            modifier = Modifier.fillMaxWidth().background(Color(0xFF2B2B2B), RoundedCornerShape(16.dp)).padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = chatTextInput,
                onValueChange = { chatTextInput = it },
                placeholder = { Text("Consult tutor...", fontSize = 11.sp, color = Color.Gray) },
                modifier = Modifier.weight(1f).testTag("ai_chat_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                ),
                maxLines = 2
            )

            IconButton(
                onClick = {
                    if (chatTextInput.isNotBlank()) {
                        viewModel.askAITutor(chatTextInput)
                        chatTextInput = ""
                    }
                }
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send", tint = Color(0xFFD0BCFF), modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun BottomNav(currentScreen: String, onSelect: (String) -> Unit) {
    NavigationBar(
        containerColor = Color.White.copy(alpha = 0.03f),
        modifier = Modifier
            .height(60.dp)
            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))),
        tonalElevation = 0.dp
    ) {
        NavigationBarItem(
            selected = currentScreen == "DASHBOARD",
            onClick = { onSelect("DASHBOARD") },
            icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard", modifier = Modifier.size(20.dp)) },
            label = { Text("Aman's Home", fontSize = 9.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFFD0BCFF),
                selectedTextColor = Color(0xFFD0BCFF),
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray,
                indicatorColor = Color(0xFF381E72)
            ),
            modifier = Modifier.testTag("nav_dashboard")
        )

        NavigationBarItem(
            selected = currentScreen == "CANVAS",
            onClick = { onSelect("CANVAS") },
            icon = { Icon(Icons.Outlined.Edit, contentDescription = "Canvas", modifier = Modifier.size(20.dp)) },
            label = { Text("S-Pen Notepad", fontSize = 9.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFFD0BCFF),
                selectedTextColor = Color(0xFFD0BCFF),
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray,
                indicatorColor = Color(0xFF381E72)
            ),
            modifier = Modifier.testTag("nav_notepad")
        )

        NavigationBarItem(
            selected = currentScreen == "PDF_READER",
            onClick = { onSelect("PDF_READER") },
            icon = { Icon(Icons.Default.LocalLibrary, contentDescription = "Library", modifier = Modifier.size(20.dp)) },
            label = { Text("Split eBooks", fontSize = 9.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFFD0BCFF),
                selectedTextColor = Color(0xFFD0BCFF),
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray,
                indicatorColor = Color(0xFF381E72)
            ),
            modifier = Modifier.testTag("nav_split_reader")
        )

        NavigationBarItem(
            selected = currentScreen == "FLASHCARDS",
            onClick = { onSelect("FLASHCARDS") },
            icon = { Icon(Icons.Default.Style, contentDescription = "Cards", modifier = Modifier.size(20.dp)) },
            label = { Text("Active Recall", fontSize = 9.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFFD0BCFF),
                selectedTextColor = Color(0xFFD0BCFF),
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray,
                indicatorColor = Color(0xFF381E72)
            ),
            modifier = Modifier.testTag("nav_flashcards")
        )

        NavigationBarItem(
            selected = currentScreen == "SEARCH",
            onClick = { onSelect("SEARCH") },
            icon = { Icon(Icons.Default.Language, contentDescription = "Search", modifier = Modifier.size(20.dp)) },
            label = { Text("Search", fontSize = 8.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFFD0BCFF),
                selectedTextColor = Color(0xFFD0BCFF),
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray,
                indicatorColor = Color(0xFF381E72)
            ),
            modifier = Modifier.testTag("nav_search")
        )

        NavigationBarItem(
            selected = currentScreen == "AMAN_EYE",
            onClick = { onSelect("AMAN_EYE") },
            icon = { Icon(Icons.Default.Visibility, contentDescription = "Aman's Eye", modifier = Modifier.size(20.dp)) },
            label = { Text("Aman's Eye 👁️", fontSize = 8.sp, maxLines = 1) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFFD0BCFF),
                selectedTextColor = Color(0xFFD0BCFF),
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray,
                indicatorColor = Color(0xFF381E72)
            ),
            modifier = Modifier.testTag("nav_aman_eye")
        )
    }
}

fun borderLayout(): BorderStroke {
    return BorderStroke(
        1.dp,
        androidx.compose.ui.graphics.Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.18f),
                Color.White.copy(alpha = 0.02f),
                Color.White.copy(alpha = 0.08f)
            )
        )
    )
}

@Composable
fun AmanEyeScreen(viewModel: com.example.viewmodel.StudyViewModel) {
    val isFaceByPass by viewModel.isFaceScanned.collectAsState()
    val isFingerByPass by viewModel.isFingerprintScanned.collectAsState()
    val isVaultUnlocked by viewModel.isVaultUnlocked.collectAsState()
    val passcodeBuffer by viewModel.passcodeBuffer.collectAsState()
    val vaultMessage by viewModel.vaultErrorMessage.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .padding(16.dp)
    ) {
        // Vault Secure Header Status Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isVaultUnlocked) Icons.Default.LockOpen else Icons.Default.Lock,
                    contentDescription = "Lock",
                    tint = if (isVaultUnlocked) Color(0xFF81C784) else Color(0xFFE57373),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "👁️ SECURE AMAN'S EYE INTERIOR",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = if (isVaultUnlocked) "MILITARY-GRADE COGNITIVE HUB DECRYPTED" else "CRYPTOGRAPHIC LOCK ENGAGED • ENHANCED SECURITY",
                        color = if (isVaultUnlocked) Color(0xFF81C784) else Color.Gray,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            if (isVaultUnlocked) {
                Button(
                    onClick = { viewModel.lockEyeVault() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828).copy(alpha = 0.2f)),
                    border = BorderStroke(1.dp, Color(0xFFEF5350).copy(alpha = 0.4f)),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Text("Secure & Lock", color = Color(0xFFEF5350), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Divider(color = Color.White.copy(0.08f), thickness = 1.dp)
        Spacer(modifier = Modifier.height(12.dp))

        if (!isVaultUnlocked) {
            // Locked screen diagnostic biometrics view
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Left Column: Biometric touch fingerprint and face Structured camera scanner simulation
                Column(
                    modifier = Modifier
                        .weight(1.1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Fingerprint Touch pad element
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E142F).copy(alpha = 0.5f)),
                        border = BorderStroke(1.dp, if (isFingerByPass) Color(0xFF81C784).copy(0.4f) else Color(0xFFD0BCFF).copy(0.12f)),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "BIOMETRIC VECTOR 1: FINGERPRINT SCAN",
                                color = Color(0xFFD0BCFF),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.4.sp
                            )
                            Spacer(modifier = Modifier.height(14.dp))

                            Box(
                                modifier = Modifier
                                    .size(90.dp)
                                    .clip(CircleShape)
                                    .background(if (isFingerByPass) Color(0xFF81C784).copy(0.15f) else Color(0xFF131118))
                                    .border(
                                        BorderStroke(2.dp, if (isFingerByPass) Color(0xFF81C784) else Color(0xFFD0BCFF).copy(0.4f)),
                                        CircleShape
                                    )
                                    .clickable { viewModel.startFingerprintScan() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Fingerprint,
                                    contentDescription = "Fingerprint",
                                    tint = if (isFingerByPass) Color(0xFF81C784) else Color(0xFFD0BCFF),
                                    modifier = Modifier.size(44.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = if (isFingerByPass) "FINGERPRINT MATCHED [OK]" else "Touch sensor pad to align ridges",
                                color = if (isFingerByPass) Color(0xFF81C784) else Color.LightGray,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Face Scanner reticle card
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E142F).copy(alpha = 0.5f)),
                        border = BorderStroke(1.dp, if (isFaceByPass) Color(0xFF81C784).copy(0.4f) else Color(0xFFD0BCFF).copy(0.12f)),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "BIOMETRIC VECTOR 2: 3D FACE IRIS MATRIX",
                                color = Color(0xFFD0BCFF),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.4.sp
                            )
                            Spacer(modifier = Modifier.height(14.dp))

                            Box(
                                modifier = Modifier
                                    .size(90.dp)
                                    .background(if (isFaceByPass) Color(0xFF81C784).copy(0.1f) else Color(0xFF131118), RoundedCornerShape(16.dp))
                                    .border(
                                        BorderStroke(1.dp, if (isFaceByPass) Color(0xFF81C784) else Color(0xFFD0BCFF).copy(alpha = 0.3f)),
                                        RoundedCornerShape(16.dp)
                                    )
                                    .clickable { viewModel.startFaceScan() },
                                contentAlignment = Alignment.Center
                            ) {
                                // Design structured reticle corners inside Canvas
                                ComposeCanvas(modifier = Modifier.fillMaxSize()) {
                                    val w = size.width
                                    val h = size.height
                                    val lineLen = 16f
                                    val strokeW = 3f
                                    // Top Left
                                    drawLine(Color(0xFFD0BCFF), Offset(10f, 10f), Offset(10f + lineLen, 10f), strokeW)
                                    drawLine(Color(0xFFD0BCFF), Offset(10f, 10f), Offset(10f, 10f + lineLen), strokeW)
                                    // Top Right
                                    drawLine(Color(0xFFD0BCFF), Offset(w - 10f, 10f), Offset(w - 10f - lineLen, 10f), strokeW)
                                    drawLine(Color(0xFFD0BCFF), Offset(w - 10f, 10f), Offset(w - 10f, 10f + lineLen), strokeW)
                                    // Bottom Left
                                    drawLine(Color(0xFFD0BCFF), Offset(10f, h - 10f), Offset(10f + lineLen, h - 10f), strokeW)
                                    drawLine(Color(0xFFD0BCFF), Offset(10f, h - 10f), Offset(10f, h - 10f - lineLen), strokeW)
                                    // Bottom Right
                                    drawLine(Color(0xFFD0BCFF), Offset(w - 10f, h - 10f), Offset(w - 10f - lineLen, h - 10f), strokeW)
                                    drawLine(Color(0xFFD0BCFF), Offset(w - 10f, h - 10f), Offset(w - 10f, h - 10f - lineLen), strokeW)
                                }
                                Icon(
                                    imageVector = Icons.Default.Face,
                                    contentDescription = "Face Scan",
                                    tint = if (isFaceByPass) Color(0xFF81C784) else Color(0xFFD0BCFF),
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = if (isFaceByPass) "FACE GEOMETRY VERIFIED [OK]" else "Tap to trigger structured camera depth scan",
                                color = if (isFaceByPass) Color(0xFF81C784) else Color.LightGray,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Right Column: Professional Numeric passcode pad entry layout
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF120E21)),
                    border = BorderStroke(1.dp, Color(0xFFD0BCFF).copy(0.12f)),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "LAYER 3: COGNITIVE PASSCODE PAD",
                            color = Color(0xFFD0BCFF),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )

                        // Secure Dot representations
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.padding(vertical = 12.dp)
                        ) {
                            for (i in 1..4) {
                                val filled = passcodeBuffer.length >= i
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clip(CircleShape)
                                        .background(if (filled) Color(0xFFD0BCFF) else Color(0xFF221936))
                                        .border(BorderStroke(1.dp, Color(0xFFD0BCFF).copy(0.5f)), CircleShape)
                                )
                            }
                        }

                        // Passcode Dialer Grid (3x4 Layout)
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            val keys = listOf(
                                listOf("1", "2", "3"),
                                listOf("4", "5", "6"),
                                listOf("7", "8", "9"),
                                listOf("C", "0", "➔")
                            )
                            keys.forEach { row ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    row.forEach { num ->
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .aspectRatio(1.6f)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(if (num == "➔") Color(0xFF381E72) else Color(0xFF1E1632))
                                                .clickable {
                                                    if (num == "C") {
                                                        viewModel.clearPasscode()
                                                    } else if (num == "➔") {
                                                        // Auto checks when full anyway
                                                    } else {
                                                        viewModel.enterPasscodeDigit(num)
                                                    }
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = num,
                                                color = if (num == "➔") Color(0xFFD0BCFF) else Color.White,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Help tip sequence bypass
                        Text(
                            text = "PASSCODE PHYSICS CONSTANT SEQUENCE IS: 1 3 7 9",
                            fontSize = 8.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            // Diagnostic status log banner at bottom
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.Black.copy(0.4f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Cached, contentDescription = "Tick", tint = Color(0xFFD0BCFF), modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = vaultMessage ?: "Status: Decrypted key hashes requested. Complete all three verification vectors.",
                        color = if (vaultMessage?.contains("❌") == true) Color(0xFFEF5350) else Color(0xFFD0BCFF),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        } else {
            // Unlocked multi-pillar control workspace
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Column 1: Spaced Repetition Center Workspace
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF140D22)),
                    border = BorderStroke(1.dp, Color.White.copy(0.05f))
                ) {
                    val srsList by viewModel.srsItems.collectAsState()
                    val selectedSRSItem by viewModel.selectedSRSItem.collectAsState()

                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "🗂️ PILLAR 2: AP BIOPHYSICS SPACED REPETITION (SM-2)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD0BCFF)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Spaced Repetition items horizontal/vertical mini grid
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.weight(0.9f)
                        ) {
                            items(srsList) { srs ->
                                val isChosen = selectedSRSItem?.id == srs.id
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isChosen) Color(0xFF381E72) else Color(0xFF1E172F))
                                        .border(BorderStroke(1.dp, if (isChosen) Color(0xFFD0BCFF) else Color.Transparent), RoundedCornerShape(8.dp))
                                        .clickable { viewModel.selectSRSItem(srs) }
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = srs.prompt, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 2.dp)) {
                                            Text(text = "EF: ${srs.easeFactor}", fontSize = 8.sp, color = Color.LightGray)
                                            Text(text = "Reps: ${srs.repetitions}", fontSize = 8.sp, color = Color.LightGray)
                                            Text(text = "Interval: ${srs.intervalDays}d", fontSize = 8.sp, color = Color.LightGray)
                                        }
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color.Black.copy(0.3f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(text = srs.nextReview, color = Color(0xFF81C784), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // Selected review dialog card
                        Spacer(modifier = Modifier.height(8.dp))
                        if (selectedSRSItem != null) {
                            val activeItm = selectedSRSItem!!
                            Card(
                                modifier = Modifier.fillMaxWidth().weight(1.1f),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF150A26)),
                                border = BorderStroke(1.dp, Color(0xFFD0BCFF).copy(0.3f))
                            ) {
                                Column(modifier = Modifier.padding(10.dp).fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                                    Column {
                                        Text(text = "ACTIVE RECALL QUESTION & DEEP EXPLANATION", fontSize = 9.sp, color = Color(0xFFFFF176), fontWeight = FontWeight.Bold)
                                        Text(text = activeItm.prompt, fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 2.dp))
                                        Text(text = activeItm.expansion, fontSize = 10.sp, color = Color.LightGray, modifier = Modifier.padding(top = 4.dp))
                                    }

                                    // SM-2 Scoring quality feedback (q=0,3,4,5)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        // Quality feedback rating buttons
                                        Button(
                                            onClick = { viewModel.processSRSFeedback(activeItm, 0) },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                                            contentPadding = PaddingValues(0.dp),
                                            modifier = Modifier.weight(1f).height(24.dp)
                                        ) {
                                            Text("Again (0d)", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                        Button(
                                            onClick = { viewModel.processSRSFeedback(activeItm, 3) },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF57C00)),
                                            contentPadding = PaddingValues(0.dp),
                                            modifier = Modifier.weight(1f).height(24.dp)
                                        ) {
                                            Text("Hard (3d)", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                        Button(
                                            onClick = { viewModel.processSRSFeedback(activeItm, 4) },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C)),
                                            contentPadding = PaddingValues(0.dp),
                                            modifier = Modifier.weight(1f).height(24.dp)
                                        ) {
                                            Text("Good (4d)", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                        Button(
                                            onClick = { viewModel.processSRSFeedback(activeItm, 5) },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00796B)),
                                            contentPadding = PaddingValues(0.dp),
                                            modifier = Modifier.weight(1f).height(24.dp)
                                        ) {
                                            Text("Easy (5d)", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier.fillMaxWidth().weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Select an active microcard from schedule to review.", color = Color.Gray, fontSize = 10.sp)
                            }
                        }
                    }
                }

                // Column 2: Multimodal Concept Mapping Canvas
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF140D22)),
                    border = BorderStroke(1.dp, Color.White.copy(0.05f))
                ) {
                    val nodes by viewModel.conceptNodes.collectAsState()
                    val selectedConf by viewModel.selectedConcept.collectAsState()

                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "📐 PILLAR 1: MULTIMODAL SUBJECT CONCEPT MAPPING",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD0BCFF)
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        // Render actual concept vertices in a Canvas with clicking!
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF0C0715))
                                .pointerInput(nodes) {
                                    detectDragGestures(
                                        onDragStart = { offset ->
                                            // Detect click matching closest node coordinate radius
                                            val clicked = nodes.firstOrNull { node ->
                                                val dx = node.x - offset.x
                                                val dy = node.y - offset.y
                                                (dx * dx + dy * dy) < 1200f // 35dp radius approx
                                            }
                                            viewModel.selectConcept(clicked)
                                        },
                                        onDrag = { _, _ -> },
                                        onDragEnd = {}
                                    )
                                }
                        ) {
                            ComposeCanvas(modifier = Modifier.fillMaxSize()) {
                                // 1. Draw connecting relational lines (Edges)
                                nodes.forEach { parentNode ->
                                    parentNode.connections.forEach { childId ->
                                        val childNode = nodes.firstOrNull { it.id == childId }
                                        if (childNode != null) {
                                            drawLine(
                                                color = Color(0xFFD0BCFF).copy(0.2f),
                                                start = Offset(parentNode.x, parentNode.y),
                                                end = Offset(childNode.x, childNode.y),
                                                strokeWidth = 3f
                                            )
                                        }
                                    }
                                }

                                // 2. Draw Concept Nodes
                                nodes.forEach { nd ->
                                    val isSel = selectedConf?.id == nd.id
                                    // Halo glow
                                    if (isSel) {
                                        drawCircle(
                                            color = Color.White.copy(0.15f),
                                            radius = 28f,
                                            center = Offset(nd.x, nd.y)
                                        )
                                        drawCircle(
                                            color = Color(nd.colorCode),
                                            radius = 16f,
                                            center = Offset(nd.x, nd.y),
                                            style = DrawScopeStroke(width = 4f)
                                        )
                                    } else {
                                        drawCircle(
                                            color = Color(nd.colorCode),
                                            radius = 12f,
                                            center = Offset(nd.x, nd.y)
                                        )
                                    }
                                }
                            }

                            // Text overlays over Canvas
                            nodes.forEach { nd ->
                                Box(
                                    modifier = Modifier
                                        .offset(
                                            x = (nd.x / 1.5f).toInt().dp,
                                            y = (nd.y / 1.5f - 24f).toInt().dp
                                        )
                                        .background(Color.Black.copy(0.6f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text(text = nd.label, color = Color.White, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Node anchor detail box
                        Spacer(modifier = Modifier.height(10.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth().height(80.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.Black.copy(0.4f))
                        ) {
                            if (selectedConf != null) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(text = "HIGHLIGHTED CONCEPT CONNECTION INFO", fontSize = 8.sp, color = Color(0xFF81C784), fontWeight = FontWeight.Bold)
                                    Text(text = "Subject node: ${selectedConf!!.label}", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                                    Text(text = "📚 PDF Notebook Anchoring: '${selectedConf!!.anchorTextbook}'", fontSize = 10.sp, color = Color.LightGray)
                                }
                            } else {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text("Tap on any node circle on concept map coordinates to trace connections.", color = Color.Gray, fontSize = 10.sp, textAlign = TextAlign.Center)
                                }
                            }
                        }
                    }
                }

                // Column 3: Immersive Formats: Podcast Player & Gravitation orbits Simulator
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF140D22)),
                    border = BorderStroke(1.dp, Color.White.copy(0.05f))
                ) {
                    val podcastPlaying by viewModel.podcastIsPlaying.collectAsState()
                    val podcastProg by viewModel.podcastProgress.collectAsState()
                    val elapsedVal by viewModel.podcastElapsed.collectAsState()
                    val speedVal by viewModel.podcastSpeed.collectAsState()

                    val simPlaying by viewModel.simIsPlaying.collectAsState()
                    val simVel by viewModel.orbitalVelocity.collectAsState()
                    val centralMass by viewModel.centralMass.collectAsState()
                    val partAngle by viewModel.particleAngle.collectAsState()

                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "🎧 PILLAR 3: SYNTHESIZED PODCASTS & SIMULATION",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD0BCFF)
                        )

                        // 1. Synthesized Podcast player
                        Card(
                            modifier = Modifier.fillMaxWidth().border(BorderStroke(1.dp, Color.White.copy(0.04f)), RoundedCornerShape(12.dp)),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF221639))
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                    Column {
                                        Text("Daily Audio Brief Podcast Summary", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        Text("Voice Synthesis: Prof. Alan Vance", fontSize = 8.sp, color = Color.LightGray)
                                    }
                                    IconButton(onClick = { viewModel.stepPodcastSpeed() }, modifier = Modifier.size(26.dp)) {
                                        Text(text = speedVal, color = Color(0xFFD0BCFF), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                // Interactive animated audio waveform
                                Spacer(modifier = Modifier.height(4.dp))
                                ComposeCanvas(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(18.dp)
                                ) {
                                    val count = 28
                                    val gap = 6f
                                    val barW = (size.width - (count - 1) * gap) / count
                                    for (i in 0 until count) {
                                        // Random bar height multiplier based on isPlaying ticks
                                        val heightMultiplier = if (podcastPlaying) {
                                            ((Math.sin(partAngle.toDouble() * 3.0 + i) + 1.2) / 2.2).toFloat()
                                        } else {
                                            0.15f
                                        }
                                        val barH = size.height * heightMultiplier
                                        drawRect(
                                            color = Color(0xFFD0BCFF),
                                            topLeft = Offset(i * (barW + gap), size.height / 2f - barH / 2f),
                                            size = androidx.compose.ui.geometry.Size(barW, barH.coerceAtLeast(3f))
                                        )
                                    }
                                }

                                Slider(
                                    value = podcastProg,
                                    onValueChange = {},
                                    colors = SliderDefaults.colors(activeTrackColor = Color(0xFFD0BCFF), thumbColor = Color(0xFFD0BCFF)),
                                    modifier = Modifier.height(16.dp)
                                )

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text(elapsedVal, fontSize = 9.sp, color = Color.LightGray)

                                    IconButton(
                                        onClick = { viewModel.togglePodcastPlaying() },
                                        modifier = Modifier.size(24.dp).background(Color(0xFF381E72), CircleShape)
                                    ) {
                                        Icon(
                                            imageVector = if (podcastPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                            contentDescription = "Playback",
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }

                                    Text("12:00", fontSize = 9.sp, color = Color.LightGray)
                                }
                            }
                        }

                        // 2. STEM Gravitational field orbital planet Keplerian simulator
                        Card(
                            modifier = Modifier.fillMaxWidth().weight(1f).border(BorderStroke(1.dp, Color.White.copy(0.04f)), RoundedCornerShape(12.dp)),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F0819))
                        ) {
                            Column(modifier = Modifier.padding(8.dp).fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text("Keplerian orbital gravity simulator", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    IconButton(onClick = { viewModel.toggleSTEMSim() }, modifier = Modifier.size(20.dp)) {
                                        Icon(
                                            imageVector = if (simPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                            contentDescription = "SimPlay",
                                            tint = Color(0xFF81C784),
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }

                                // Orbital Canvas
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth()
                                        .background(Color.Black.copy(0.3f), RoundedCornerShape(8.dp))
                                ) {
                                    ComposeCanvas(modifier = Modifier.fillMaxSize()) {
                                        val cx = size.width / 2f
                                        val cy = size.height / 2f

                                        // Central Sun mass radius relative to mass value
                                        val sunRadius = 14f + (centralMass / 500f) * 6f
                                        drawCircle(Color(0xFFFFB74D), sunRadius, Offset(cx, cy))

                                        // Orbit trace circle path diameter
                                        val traceRadius = (cx * 0.7f)
                                        drawCircle(Color.White.copy(0.08f), traceRadius, Offset(cx, cy), style = DrawScopeStroke(1f))

                                        // Rotating particle position
                                        val px = cx + traceRadius * Math.cos(partAngle.toDouble()).toFloat()
                                        val py = cy + traceRadius * Math.sin(partAngle.toDouble()).toFloat()
                                        drawCircle(Color(0xFF00E5FF), 8f, Offset(px, py))

                                        // Force vectors line representing Newtonian gravity
                                        drawLine(Color(0xFFE57373).copy(alpha = 0.5f), Offset(px, py), Offset(cx, cy), 2f)
                                    }
                                }

                                // Controls
                                Column {
                                    Text("Tangential velocity constant: ${String.format("%.2f", simVel)}", fontSize = 8.sp, color = Color.LightGray)
                                    Slider(
                                        value = simVel,
                                        onValueChange = { viewModel.updateSTEMVelocity(it) },
                                        valueRange = 1f..10f,
                                        modifier = Modifier.height(14.dp)
                                    )

                                    Text("Central Gravity Mass parameter: ${centralMass.toInt()}", fontSize = 8.sp, color = Color.LightGray)
                                    Slider(
                                        value = centralMass,
                                        onValueChange = { viewModel.updateSTEMMass(it) },
                                        valueRange = 100f..1000f,
                                        modifier = Modifier.height(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
