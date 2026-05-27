package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.data.LongTask
import com.example.ui.MainViewModel
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LongTaskViewScreen(
    navController: NavController,
    viewModel: MainViewModel,
    taskId: Int
) {
    // Find the current task
    val allTasks by viewModel.allTasks.collectAsStateWithLifecycle(initialValue = emptyList())
    val task = remember(allTasks, taskId) {
        allTasks.find { it.id == taskId }
    }

    val taskLogs by viewModel.allLogs.collectAsStateWithLifecycle()
    val filteredLogs = remember(taskLogs, taskId) {
        taskLogs.filter { it.parentTaskId == taskId }
    }

    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (task == null) {
        Scaffold(
            modifier = Modifier.fillMaxSize().testTag("task_view_error_scaffold"),
            containerColor = ObsidianBg
        ) { innerPadding ->
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Quest Not Found", color = Color.White, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = { navController.popBackStack() }) {
                        Text("Return to Safety")
                    }
                }
            }
        }
        return
    }

    // --- MATHEMATICAL OVERDUE CHECKS ---
    val isOverdue = remember(task) {
        if (task.status == "Active" && task.startDate > 0L) {
            val totalAllowedDays = task.targetDays + task.bonusDaysAdded
            val deadlineMillis = task.startDate + totalAllowedDays * 86400000L
            System.currentTimeMillis() > deadlineMillis
        } else {
            false
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize().testTag("task_view_scaffold"),
        containerColor = ObsidianBg,
        topBar = {
            TopAppBar(
                title = { Text("Quest Intelligence", fontWeight = FontWeight.Black, color = Color.White) },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.testTag("task_view_back")
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showEditDialog = true },
                        modifier = Modifier.testTag("task_view_edit")
                    ) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Goal", tint = NeonPurpleLight)
                    }
                    IconButton(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier.testTag("task_view_delete")
                    ) {
                        Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = "Discard Goal", tint = DangerRed)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ObsidianBg)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // --- HEADER METRICS ---
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, PurpleBorder, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = CharcoalCard),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "QUEST TITLE",
                        style = MaterialTheme.typography.labelSmall,
                        color = NeonPurple,
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("DAILY DOSE", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            Text("${task.dailyGoalMins} Mins", style = MaterialTheme.typography.bodyLarge, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text("TOPOLOGY", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            Text(task.type, style = MaterialTheme.typography.bodyLarge, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text("STATUS", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            Text(
                                text = task.status.uppercase(),
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (task.status == "Completed") DoneGreen else if (task.status == "Active") NeonPurpleLight else PendingYellow,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // --- BONUS DAY ALERT MODULE ---
            if (isOverdue) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, PendingYellow.copy(alpha = 0.5f), RoundedCornerShape(14.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0x1FDD2C00)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Warning, contentDescription = "Overdue Limit", tint = PendingYellow)
                            Text(
                                "Quest Grace Window Expired!", 
                                fontWeight = FontWeight.Bold, 
                                color = Color.White,
                                fontSize = 16.sp
                            )
                        }
                        Text(
                            text = "Life happens. ADHD system allows claiming a Bonus Day to extend the timeline and clear cognitive pressure.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                        Button(
                            onClick = { viewModel.claimBonusDay(task) },
                            colors = ButtonDefaults.buttonColors(containerColor = PendingYellow),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("claim_bonus_day_btn")
                        ) {
                            Text("Claim Bonus Day (+1 Day)", color = Color.Black, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }

            // --- PROGRESS STATS ---
            val totalGoalDays = task.targetDays + task.bonusDaysAdded
            val progress = if (totalGoalDays > 0) {
                (task.daysCompleted.toFloat() / totalGoalDays).coerceIn(0f, 1f)
            } else {
                0f
            }
            val progressPercent = (progress * 100).toInt()

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, PurpleBorder, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = CharcoalCard),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Progress Convergence", fontWeight = FontWeight.Bold, color = Color.White)
                        Text(
                            text = "$progressPercent%", 
                            fontWeight = FontWeight.Black, 
                            color = NeonPurpleLight,
                            fontSize = 20.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = NeonPurple,
                        trackColor = ObsidianBg
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Sprinting days", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            Text("${task.daysCompleted} Completed", style = MaterialTheme.typography.bodyMedium, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Total Target Space", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            Text("$totalGoalDays Days", style = MaterialTheme.typography.bodyMedium, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        if (task.bonusDaysAdded > 0) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Emergency Extensions", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                Text("+${task.bonusDaysAdded} Bonus", style = MaterialTheme.typography.bodyMedium, color = PendingYellow, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // --- COMPILATION LOG CALENDAR WIDGET ---
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, PurpleBorder, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = CharcoalCard),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    val currentMonthCalendar = remember { Calendar.getInstance() }
                    val monthName = remember {
                        SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date())
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "RESONANCE CALENDAR",
                            style = MaterialTheme.typography.labelSmall,
                            color = NeonPurple,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = monthName,
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Days of week short headers
                    val weekAbbrev = listOf("M", "T", "W", "T", "F", "S", "S")
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        weekAbbrev.forEach {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(32.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Calendar Month Grid Generation
                    val logDatesSet = remember(filteredLogs) {
                        filteredLogs.map { log ->
                            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(log.completionDate))
                        }.toSet()
                    }

                    val firstOfMonthCal = remember {
                        Calendar.getInstance().apply {
                            set(Calendar.DAY_OF_MONTH, 1)
                        }
                    }
                    val totalMonthDays = firstOfMonthCal.getActualMaximum(Calendar.DAY_OF_MONTH)
                    val rawFirstDayOfWeek = firstOfMonthCal.get(Calendar.DAY_OF_WEEK)
                    // standard compensation for Monday starting offsets (Sunday=7, Monday=1, etc. -> map Sunday=6, Mon=0...)
                    val adjustedOffset = when (rawFirstDayOfWeek) {
                        Calendar.MONDAY -> 0
                        Calendar.TUESDAY -> 1
                        Calendar.WEDNESDAY -> 2
                        Calendar.THURSDAY -> 3
                        Calendar.FRIDAY -> 4
                        Calendar.SATURDAY -> 5
                        Calendar.SUNDAY -> 6
                        else -> 0
                    }

                    val daysGrid = remember {
                        (1..adjustedOffset).map { "" } + (1..totalMonthDays).map { it.toString() }
                    }
                    val gridChunks = remember { daysGrid.chunked(7) }

                    val monthIndex = currentMonthCalendar.get(Calendar.MONTH)
                    val yearValue = currentMonthCalendar.get(Calendar.YEAR)

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        gridChunks.forEach { rowDays ->
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                rowDays.forEach { dayStr ->
                                    if (dayStr.isEmpty()) {
                                        Box(modifier = Modifier.size(32.dp))
                                    } else {
                                        val dayNum = dayStr.toInt()
                                        val elementDateStr = remember(dayNum) {
                                            val dayCal = Calendar.getInstance().apply {
                                                set(Calendar.MONTH, monthIndex)
                                                set(Calendar.YEAR, yearValue)
                                                set(Calendar.DAY_OF_MONTH, dayNum)
                                            }
                                            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(dayCal.time)
                                        }
                                        
                                        val wasLogged = logDatesSet.contains(elementDateStr)

                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(CircleShape)
                                                .background(if (wasLogged) NeonPurple else Color.Transparent)
                                                .border(if (wasLogged) 0.dp else 1.dp, if (wasLogged) Color.Transparent else CharcoalCardElevated, CircleShape)
                                                .testTag("cal_day_$dayStr"),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = dayStr,
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = if (wasLogged) FontWeight.Black else FontWeight.Normal,
                                                color = if (wasLogged) Color.White else TextPrimary
                                            )
                                        }
                                    }
                                }
                                // Fill missing items to avoid stretching uneven lines
                                if (rowDays.size < 7) {
                                    (1..(7 - rowDays.size)).forEach { _ ->
                                        Box(modifier = Modifier.size(32.dp))
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(NeonPurple))
                        Text("Log Date Active", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // --- FORM DIALOGUE TO EDIT QUEST ---
    if (showEditDialog) {
        QuestFormDialog(
            initialQuest = task,
            onDismiss = { showEditDialog = false },
            onSave = { updatedTask ->
                viewModel.editQuest(updatedTask)
                showEditDialog = false
            }
        )
    }

    // --- COMMENCE TRASH DIALOG --
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Purge Quest?", fontWeight = FontWeight.Black, color = Color.White) },
            containerColor = CharcoalCard,
            text = { Text("Are you sure you want to completely erase '${task.title}'? This will completely clear all history logs.", color = TextSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteQuest(task.id)
                        showDeleteConfirm = false
                        navController.popBackStack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("confirm_delete")
                ) {
                    Text("Deconstruct")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Abort", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
fun QuestFormDialog(
    initialQuest: LongTask,
    onDismiss: () -> Unit,
    onSave: (LongTask) -> Unit
) {
    var title by remember { mutableStateOf(initialQuest.title) }
    var type by remember { mutableStateOf(initialQuest.type) }
    var targetDays by remember { mutableStateOf(initialQuest.targetDays.toString()) }
    var dailyGoalMins by remember { mutableStateOf(initialQuest.dailyGoalMins.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Recalibrate Quest",
                color = Color.White,
                fontWeight = FontWeight.Black
            )
        },
        containerColor = CharcoalCard,
        tonalElevation = 8.dp,
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("quest_recalibrate_content"),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Quest Title") },
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonPurple,
                        unfocusedBorderColor = CharcoalCardElevated,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = ObsidianBg,
                        unfocusedContainerColor = ObsidianBg
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("edit_quest_title")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = targetDays,
                        onValueChange = { if (it.isEmpty() || it.toIntOrNull() != null) targetDays = it },
                        label = { Text("Target Days") },
                        enabled = type == "Fixed",
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonPurple,
                            unfocusedBorderColor = CharcoalCardElevated,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = ObsidianBg,
                            unfocusedContainerColor = ObsidianBg
                        ),
                        modifier = Modifier.weight(1f).testTag("edit_quest_days")
                    )

                    OutlinedTextField(
                        value = dailyGoalMins,
                        onValueChange = { if (it.isEmpty() || it.toIntOrNull() != null) dailyGoalMins = it },
                        label = { Text("Mins/Day") },
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonPurple,
                            unfocusedBorderColor = CharcoalCardElevated,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = ObsidianBg,
                            unfocusedContainerColor = ObsidianBg
                        ),
                        modifier = Modifier.weight(1f).testTag("edit_quest_mins")
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val finalDays = if (type == "Fixed") targetDays.toIntOrNull() ?: 30 else 365
                        val finalMins = dailyGoalMins.toIntOrNull() ?: 15
                        onSave(initialQuest.copy(title = title, targetDays = finalDays, dailyGoalMins = finalMins))
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("save_quest_edit")
            ) {
                Text("Lock Recalibrations")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonColors(Color.Transparent, TextSecondary, Color.Transparent, TextSecondary)
            ) {
                Text("Cancel")
            }
        }
    )
}
