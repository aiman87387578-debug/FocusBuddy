package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.DailyRoutine
import com.example.ui.MainViewModel
import com.example.ui.theme.*

@Composable
fun WeeklyPlannerScreen(
    viewModel: MainViewModel
) {
    val allRoutines by viewModel.plannerRoutines.collectAsStateWithLifecycle()
    val selectedDay = viewModel.selectedPlannerDay
    
    val filteredRoutines = remember(allRoutines, selectedDay) {
        allRoutines.filter { it.repeatDays.contains(selectedDay) }
            .sortedWith(compareBy { it.startTime })
    }

    var showCreateDialog by remember { mutableStateOf(false) }
    var routineToEdit by remember { mutableStateOf<DailyRoutine?>(null) }

    val daysOfWeek = remember {
        listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
    }

    Scaffold(
        modifier = Modifier.fillMaxSize().testTag("planner_scaffold"),
        containerColor = ObsidianBg,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = NeonPurple,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_routine_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Routine")
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
                text = "ROUTINE HUB",
                style = MaterialTheme.typography.labelSmall,
                color = NeonPurple,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            Text(
                text = "Weekly Planner",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
            Text(
                text = "Structure recurring systems to bypass executive dysfunction.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(20.dp))

            // --- 7-DAY SELECTOR ---
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(daysOfWeek) { day ->
                    val isSelected = day == selectedDay
                    val isTodayReal = day == viewModel.todayName
                    
                    Box(
                        modifier = Modifier
                            .width(54.dp)
                            .height(68.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) NeonPurple else CharcoalCard)
                            .border(
                                1.dp,
                                if (isSelected) NeonPurpleLight else if (isTodayReal) NeonPurple.copy(alpha = 0.5f) else CharcoalCardElevated,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { viewModel.selectPlannerDay(day) }
                            .testTag("day_selector_$day"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = day.take(3).uppercase(),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Black,
                                color = if (isSelected) Color.White else TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            // Dot representing active status
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(
                                        if (isSelected) Color.White 
                                        else if (isTodayReal) NeonPurpleLight 
                                        else Color.Transparent
                                    )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- ROUTINE LISTS FOR SELECTED DAY ---
            Text(
                text = "$selectedDay Synchronized Routines (${filteredRoutines.size})",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = NeonPurpleLight
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (filteredRoutines.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(bottom = 60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bedtime,
                            contentDescription = "Rest",
                            tint = TextSecondary,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "No routines for $selectedDay",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Routines act as track-guides to bypass decision fatigue.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { showCreateDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentViolet),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("planner_create_first_button")
                        ) {
                            Text("+ Create a Routine")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredRoutines, key = { "planner_routine_${it.id}" }) { routine ->
                        PlannerRoutineCard(
                            routine = routine,
                            onEditClick = { routineToEdit = routine },
                            onDeleteClick = { viewModel.deleteRoutine(routine.id) }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }

    // --- CREATE DIALOG ---
    if (showCreateDialog) {
        RoutineFormDialog(
            initialRoutine = null,
            defaultSuggestedDay = selectedDay,
            onDismiss = { showCreateDialog = false },
            onSave = { title, time, days ->
                viewModel.createRoutine(title, time, days)
                showCreateDialog = false
            }
        )
    }

    // --- EDIT DIALOG ---
    if (routineToEdit != null) {
        RoutineFormDialog(
            initialRoutine = routineToEdit,
            defaultSuggestedDay = selectedDay,
            onDismiss = { routineToEdit = null },
            onSave = { title, time, days ->
                routineToEdit?.let {
                    viewModel.updateRoutine(it.copy(title = title, startTime = time, repeatDays = days))
                }
                routineToEdit = null
            }
        )
    }
}

@Composable
fun PlannerRoutineCard(
    routine: DailyRoutine,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("planner_routine_card_${routine.id}")
            .border(1.dp, CharcoalCardElevated, RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = CharcoalCard),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = routine.title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = "Time",
                            tint = NeonPurpleLight,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = routine.startTime,
                            style = MaterialTheme.typography.bodySmall,
                            color = NeonPurpleLight,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Repeat,
                            contentDescription = "Repeat days count",
                            tint = TextSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = if (routine.repeatDays.size == 7) "Everyday" else "${routine.repeatDays.size} days",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(CharcoalCardElevated)
                        .testTag("edit_routine_btn_${routine.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Routine",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }

                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0x15FF1744))
                        .testTag("delete_routine_btn_${routine.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Routine",
                        tint = DangerRed,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun RoutineFormDialog(
    initialRoutine: DailyRoutine?,
    defaultSuggestedDay: String,
    onDismiss: () -> Unit,
    onSave: (title: String, startTime: String, repeatDays: List<String>) -> Unit
) {
    var title by remember { mutableStateOf(initialRoutine?.title ?: "") }
    var hour by remember { mutableStateOf(initialRoutine?.startTime?.split(":")?.getOrNull(0) ?: "09") }
    var min by remember { mutableStateOf(initialRoutine?.startTime?.split(":")?.getOrNull(1) ?: "00") }
    
    // Choose repeat days
    val allWeekDays = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
    var selectedDays by remember {
        mutableStateOf(initialRoutine?.repeatDays ?: listOf(defaultSuggestedDay))
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialRoutine == null) "Initiate Routine Entry" else "Update Core Routine",
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
                    .testTag("routine_form_dialog_content"),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // --- TITLE INPUT ---
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Routine Title (e.g. Visual Sweep)") },
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
                    modifier = Modifier.fillMaxWidth().testTag("routine_title_input")
                )

                // --- TIME INPUT (DIGITAL CLOCK INPUT FOR SPEED/NO FRICTION) ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = hour,
                        onValueChange = { if (it.length <= 2 && it.toIntOrNull() in 0..23) hour = it },
                        label = { Text("Hour") },
                        placeholder = { Text("09") },
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonPurple,
                            unfocusedBorderColor = CharcoalCardElevated,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = ObsidianBg,
                            unfocusedContainerColor = ObsidianBg
                        ),
                        modifier = Modifier.weight(1f).testTag("routine_hour_input")
                    )
                    Text(":", color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
                    OutlinedTextField(
                        value = min,
                        onValueChange = { if (it.length <= 2 && it.toIntOrNull() in 0..59) min = it },
                        label = { Text("Min") },
                        placeholder = { Text("00") },
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonPurple,
                            unfocusedBorderColor = CharcoalCardElevated,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = ObsidianBg,
                            unfocusedContainerColor = ObsidianBg
                        ),
                        modifier = Modifier.weight(1f).testTag("routine_min_input")
                    )
                }

                // --- DAY REPEATER MULTI-SELECT ---
                Text(
                    text = "Weekly Resonance Mapping",
                    style = MaterialTheme.typography.labelSmall,
                    color = NeonPurpleLight,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    allWeekDays.forEach { day ->
                        val active = selectedDays.contains(day)
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (active) NeonPurple else CharcoalCardElevated)
                                .clickable {
                                    selectedDays = if (active) {
                                        selectedDays.filter { it != day }
                                    } else {
                                        selectedDays + day
                                    }
                                }
                                .testTag("form_day_toggle_$day"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = day.take(1),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = if (active) Color.White else TextSecondary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val formattedHour = hour.padStart(2, '0')
                        val formattedMin = min.padStart(2, '0')
                        onSave(title, "$formattedHour:$formattedMin", selectedDays)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("routine_save_button")
            ) {
                Text("Lock In")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonColors(Color.Transparent, TextSecondary, Color.Transparent, TextSecondary)
            ) {
                Text("Scrap")
            }
        }
    )
}
