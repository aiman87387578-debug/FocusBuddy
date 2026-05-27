package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.data.DailyRoutine
import com.example.data.LongTask
import com.example.ui.MainViewModel
import com.example.ui.theme.*

@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: MainViewModel
) {
    val activeTasks by viewModel.activeTasks.collectAsStateWithLifecycle()
    val routines by viewModel.dashboardRoutines.collectAsStateWithLifecycle()
    val allLogs by viewModel.allLogs.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.fillMaxSize().testTag("dashboard_scaffold"),
        containerColor = ObsidianBg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            // --- HEADER ---
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "CURRENT OPERATIONS",
                        style = MaterialTheme.typography.labelSmall,
                        color = NeonPurple,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                    val dateFormatted = remember {
                        java.text.SimpleDateFormat("EEEE, dd MMM", java.util.Locale.getDefault()).format(java.util.Date())
                    }
                    Text(
                        text = dateFormatted,
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-0.5).sp
                    )
                }
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(CharcoalCard)
                        .border(1.dp, NeonPurple, androidx.compose.foundation.shape.CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "DB",
                        color = NeonPurple,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // --- ACTIVE QUESTS ---
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ACTIVE QUESTS (${activeTasks.size})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = NeonPurpleLight,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Tap cards to view Intelligence",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }

                if (activeTasks.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(CharcoalCard)
                                .border(1.dp, CharcoalCardElevated, RoundedCornerShape(16.dp))
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "Empty quests",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(36.dp)
                                )
                                Text(
                                    text = "No Active Quests",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Go to Quest Vault to commence a focused journey.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary,
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                )
                                Button(
                                    onClick = { navController.navigate("vault") },
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("dashboard_goto_vault")
                                ) {
                                    Text("Open Quest Vault")
                                }
                            }
                        }
                    }
                } else {
                    items(activeTasks, key = { "task_${it.id}" }) { task ->
                        val isLoggedToday = remember(allLogs, task.id) {
                            val todayStr = viewModel.getTodayDateString()
                            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                            allLogs.any { log ->
                                log.parentTaskId == task.id && sdf.format(java.util.Date(log.completionDate)) == todayStr
                            }
                        }
                        ActiveQuestCard(
                            task = task,
                            isLoggedToday = isLoggedToday,
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { navController.navigate("task_view/${task.id}") },
                            onLogClick = { viewModel.incrementTaskProgress(task) }
                        )
                    }
                }

                // --- DAILY ROUTINES ---
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PRIORITY ROUTINES",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            letterSpacing = 2.sp
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        HorizontalDivider(
                            color = Zinc800,
                            thickness = 1.dp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                if (routines.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(CharcoalCard)
                                .border(1.dp, CharcoalCardElevated, RoundedCornerShape(16.dp))
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = "Empty routines",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(32.dp)
                                )
                                Text(
                                    text = "Zero Routines Scheduled Today",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Tap Planner below to configure routines for ${viewModel.todayName}.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary,
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                )
                            }
                        }
                    }
                } else {
                    items(routines, key = { "routine_${it.id}" }) { routine ->
                        RoutineListItem(
                            routine = routine,
                            onToggle = { viewModel.toggleRoutine(routine) }
                        )
                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
fun ActiveQuestCard(
    task: LongTask,
    isLoggedToday: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onLogClick: () -> Unit
) {
    val totalGoalDays = task.targetDays + task.bonusDaysAdded
    val progress = if (totalGoalDays > 0) {
        (task.daysCompleted.toFloat() / totalGoalDays).coerceIn(0f, 1f)
    } else {
        0f
    }
    val progressPercent = (progress * 100).toInt()

    Card(
        modifier = modifier
            .testTag("task_card_${task.id}")
            .clickable { onClick() }
            .border(1.dp, PurpleBorder, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = CharcoalCard),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Main progress and info section (left/middle)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Title
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "DAY ${task.daysCompleted} OF $totalGoalDays",
                        style = MaterialTheme.typography.labelSmall,
                        color = Zinc500,
                        fontWeight = FontWeight.Bold,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                    Text(
                        text = "$progressPercent%",
                        style = MaterialTheme.typography.labelSmall,
                        color = NeonPurple,
                        fontWeight = FontWeight.Bold,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                }

                // Small sleek progress bar
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = NeonPurple,
                    trackColor = ObsidianBg
                )
            }

            // CTAs section (right side)
            Button(
                onClick = onLogClick,
                enabled = !isLoggedToday,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isLoggedToday) Zinc800 else NeonPurple,
                    contentColor = if (isLoggedToday) Zinc500 else Color.Black,
                    disabledContainerColor = Zinc800,
                    disabledContentColor = Zinc500
                ),
                shape = RoundedCornerShape(6.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                modifier = Modifier
                    .height(36.dp)
                    .testTag("log_progress_button_${task.id}")
            ) {
                Text(
                    text = if (isLoggedToday) "✓ Logged" else "+ Log",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

@Composable
fun RoutineListItem(
    routine: DailyRoutine,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clip(RoundedCornerShape(12.dp))
            .background(CharcoalCard.copy(alpha = 0.5f))
            .border(
                1.dp, 
                if (routine.isDoneToday) DoneGreen.copy(alpha = 0.4f) else CharcoalCardElevated, 
                RoundedCornerShape(12.dp)
            )
            .clickable { onToggle() }
            .testTag("routine_item_${routine.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left accent bar
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(4.dp)
                .background(if (routine.isDoneToday) DoneGreen else NeonPurple)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = routine.startTime,
                style = MaterialTheme.typography.bodyMedium,
                color = if (routine.isDoneToday) Zinc500 else NeonPurple,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(56.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = routine.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (routine.isDoneToday) Zinc500 else Color.White,
                    fontWeight = FontWeight.Bold,
                    textDecoration = if (routine.isDoneToday) TextDecoration.LineThrough else TextDecoration.None
                )
                
                Text(
                    text = if (routine.isDoneToday) "Routine Executed" else "Critical Start Routine",
                    style = MaterialTheme.typography.labelSmall,
                    color = Zinc500
                )
            }

            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.Transparent)
                    .border(
                        2.dp, 
                        if (routine.isDoneToday) DoneGreen else Zinc700, 
                        RoundedCornerShape(4.dp)
                    )
                    .testTag("routine_checkbox_${routine.id}"),
                contentAlignment = Alignment.Center
            ) {
                if (routine.isDoneToday) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(DoneGreen)
                    )
                }
            }
        }
    }
}
