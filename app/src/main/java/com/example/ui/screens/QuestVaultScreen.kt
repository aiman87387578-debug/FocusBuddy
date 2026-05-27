package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.data.LongTask
import com.example.ui.MainViewModel
import com.example.ui.theme.*

@Composable
fun QuestVaultScreen(
    navController: NavController,
    viewModel: MainViewModel
) {
    val pendingQuests by viewModel.pendingTasks.collectAsStateWithLifecycle()
    val activeQuests by viewModel.activeTasks.collectAsStateWithLifecycle()
    val completedQuests by viewModel.completedTasks.collectAsStateWithLifecycle()

    var selectedTabIndex by remember { mutableStateOf(0) }
    var showCreateDialog by remember { mutableStateOf(false) }

    val tabs = listOf("Pending", "Active", "Completed")

    Scaffold(
        modifier = Modifier.fillMaxSize().testTag("vault_scaffold"),
        containerColor = ObsidianBg,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = NeonPurple,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_quest_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Scribe New Quest")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // --- HEADER ---
            Text(
                text = "NEURO-LOGIC",
                style = MaterialTheme.typography.labelSmall,
                color = NeonPurple,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            Text(
                text = "Quest Vault",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
            Text(
                text = "Break daunting objectives into hyper-focused gamified runs.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- TAB NAV SELECTOR ---
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.Transparent,
                contentColor = NeonPurple,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = NeonPurple
                    )
                },
                divider = {
                    HorizontalDivider(color = CharcoalCardElevated)
                },
                modifier = Modifier.fillMaxWidth().testTag("vault_tab_row")
            ) {
                tabs.forEachIndexed { index, title ->
                    val count = when (index) {
                        0 -> pendingQuests.size
                        1 -> activeQuests.size
                        else -> completedQuests.size
                    }
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = title, 
                                    fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 14.sp
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (selectedTabIndex == index) NeonPurple.copy(alpha = 0.2f) else CharcoalCardElevated)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = count.toString(),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (selectedTabIndex == index) NeonPurpleLight else TextSecondary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        },
                        modifier = Modifier.testTag("vault_tab_$index")
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- TAB ACTIONS PANEL ---
            Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                when (selectedTabIndex) {
                    0 -> PendingQuestsPanel(
                        quests = pendingQuests,
                        onStart = { viewModel.startQuest(it) },
                        onDelete = { viewModel.deleteQuest(it) }
                    )
                    1 -> ActiveQuestsPanel(
                        quests = activeQuests,
                        onCardClick = { navController.navigate("task_view/${it.id}") },
                        onCompleteClick = { viewModel.forceCompleteQuest(it) }
                    )
                    2 -> CompletedQuestsPanel(
                        quests = completedQuests,
                        onCardClick = { navController.navigate("task_view/${it.id}") },
                        onDeleteClick = { viewModel.deleteQuest(it) }
                    )
                }
            }
        }
    }

    // --- CREATE QUEST DIALOG ---
    if (showCreateDialog) {
        QuestFormDialog(
            onDismiss = { showCreateDialog = false },
            onSave = { title, type, days, mins ->
                viewModel.createQuest(title, type, days, mins)
                showCreateDialog = false
            }
        )
    }
}

@Composable
fun PendingQuestsPanel(
    quests: List<LongTask>,
    onStart: (LongTask) -> Unit,
    onDelete: (Int) -> Unit
) {
    if (quests.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Inventory,
                    contentDescription = "No archives",
                    tint = TextSecondary,
                    modifier = Modifier.size(48.dp)
                )
                Text(
                    text = "No Pending Quests",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "A blank quest registry bypasses choice paralysis.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(quests, key = { "pending_${it.id}" }) { quest ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pending_quest_${quest.id}")
                        .border(1.dp, PurpleBorder, RoundedCornerShape(14.dp)),
                    colors = CardDefaults.cardColors(containerColor = CharcoalCard)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = quest.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Duration: ${quest.targetDays} days  |  Goal: ${quest.dailyGoalMins} mins/day",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                            
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(AccentVioletGlow)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = quest.type.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = NeonPurpleLight,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { onStart(quest) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("start_quest_btn_${quest.id}"),
                                colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Commence Run",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Start Quest")
                            }

                            IconButton(
                                onClick = { onDelete(quest.id) },
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0x15FF1744))
                                    .testTag("delete_pending_btn_${quest.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Trash Quest",
                                    tint = DangerRed
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ActiveQuestsPanel(
    quests: List<LongTask>,
    onCardClick: (LongTask) -> Unit,
    onCompleteClick: (LongTask) -> Unit
) {
    if (quests.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CompassCalibration,
                    contentDescription = "No active",
                    tint = TextSecondary,
                    modifier = Modifier.size(48.dp)
                )
                Text(
                    text = "No Active Quests",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Launch a pending quest or create a new sync point above.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(quests, key = { "active_${it.id}" }) { quest ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("active_quest_${quest.id}")
                        .clickable { onCardClick(quest) }
                        .border(1.dp, PurpleBorder, RoundedCornerShape(14.dp)),
                    colors = CardDefaults.cardColors(containerColor = CharcoalCard)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = quest.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Dose: ${quest.dailyGoalMins} mins/day | Type: ${quest.type}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                            
                            IconButton(
                                onClick = { onCompleteClick(quest) },
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(DoneGreen.copy(alpha = 0.15f))
                                    .testTag("force_complete_btn_${quest.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Trigger Lock Complete",
                                    tint = DoneGreen
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Progress indicator bar inside card
                        val totalDays = quest.targetDays + quest.bonusDaysAdded
                        val progress = if (totalDays > 0) {
                            (quest.daysCompleted.toFloat() / totalDays).coerceIn(0f, 1f)
                        } else {
                            0f
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Daily Completions",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                            Text(
                                text = "${quest.daysCompleted}/$totalDays Days",
                                style = MaterialTheme.typography.bodySmall,
                                color = NeonPurpleLight,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = progress,
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                            color = NeonPurple,
                            trackColor = ObsidianBg
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CompletedQuestsPanel(
    quests: List<LongTask>,
    onCardClick: (LongTask) -> Unit,
    onDeleteClick: (Int) -> Unit
) {
    if (quests.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = "No trophies",
                    tint = TextSecondary,
                    modifier = Modifier.size(48.dp)
                )
                Text(
                    text = "No Completed Quests",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Trophy shelf empty. Complete active runners to display here.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(quests, key = { "completed_${it.id}" }) { quest ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("completed_quest_${quest.id}")
                        .clickable { onCardClick(quest) }
                        .border(1.dp, DoneGreen.copy(alpha = 0.2f), RoundedCornerShape(14.dp)),
                    colors = CardDefaults.cardColors(containerColor = CharcoalCard)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Done stamp",
                                    tint = DoneGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = quest.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextSecondary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Done in ${quest.daysCompleted} days! | Daily: ${quest.dailyGoalMins}m",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }

                        IconButton(
                            onClick = { onDeleteClick(quest.id) },
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0x15FF1744))
                                .testTag("delete_completed_btn_${quest.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Trashing Trophy",
                                tint = DangerRed
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuestFormDialog(
    onDismiss: () -> Unit,
    onSave: (title: String, type: String, targetDays: Int, dailyGoalMins: Int) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("Fixed") } // "Fixed" or "Infinite"
    var targetDays by remember { mutableStateOf("30") }
    var dailyGoalMins by remember { mutableStateOf("15") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Draft Neuro-Quest",
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
                    .testTag("quest_form_dialog_content"),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Quest Title (e.g. Kotlin Sprint)") },
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonPurple,
                        unfocusedBorderColor = CharcoalCardElevated,
                        focusedLabelColor = NeonPurple,
                        unfocusedLabelColor = TextSecondary,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = ObsidianBg,
                        unfocusedContainerColor = ObsidianBg
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("quest_title_input")
                )

                // --- QUEST TYPE (Fixed vs Infinite) ---
                Text(
                    text = "Goal Target Topology",
                    style = MaterialTheme.typography.labelSmall,
                    color = NeonPurpleLight,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectableGroup(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val types = listOf("Fixed", "Infinite")
                    types.forEach { t ->
                        val selected = type == t
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selected) NeonPurple else CharcoalCardElevated)
                                .border(
                                    1.dp,
                                    if (selected) NeonPurpleLight else Color.Transparent,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { type = t }
                                .testTag("form_type_toggle_$t"),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = t,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selected) Color.White else TextPrimary
                                )
                                if (t == "Infinite" && selected) {
                                    Text("Ongoing habit", fontSize = 9.sp, color = TextSecondary)
                                } else if (t == "Fixed" && selected) {
                                    Text("Defined target", fontSize = 9.sp, color = TextSecondary)
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = targetDays,
                        onValueChange = { if (it.isEmpty() || it.toIntOrNull() != null) targetDays = it },
                        label = { Text("Target (Days)") },
                        placeholder = { Text("30") },
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
                        modifier = Modifier.weight(1f).testTag("quest_days_input")
                    )

                    OutlinedTextField(
                        value = dailyGoalMins,
                        onValueChange = { if (it.isEmpty() || it.toIntOrNull() != null) dailyGoalMins = it },
                        label = { Text("Goal (Mins/Day)") },
                        placeholder = { Text("15") },
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonPurple,
                            unfocusedBorderColor = CharcoalCardElevated,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = ObsidianBg,
                            unfocusedContainerColor = ObsidianBg
                        ),
                        modifier = Modifier.weight(1f).testTag("quest_mins_input")
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val currentTargetDays = if (type == "Fixed") targetDays.toIntOrNull() ?: 30 else 365
                        val currentGoalMins = dailyGoalMins.toIntOrNull() ?: 15
                        onSave(title, type, currentTargetDays, currentGoalMins)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("quest_save_button")
            ) {
                Text("Engage Quest")
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
