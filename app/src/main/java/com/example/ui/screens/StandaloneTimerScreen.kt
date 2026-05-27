package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.theme.*

@Composable
fun StandaloneTimerScreen(
    viewModel: MainViewModel
) {
    val totalRemaining = viewModel.timerRemainingMillis
    val isRunning = viewModel.isTimerRunning
    val isCompleted = viewModel.isTimerCompleted
    val progress = viewModel.getTimerProgress()
    val formattedTime = viewModel.getFormattedTimer()

    Scaffold(
        modifier = Modifier.fillMaxSize().testTag("timer_scaffold"),
        containerColor = ObsidianBg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // --- HEADER ---
            Text(
                text = "NEURO-DRIFT SHIELD",
                style = MaterialTheme.typography.labelSmall,
                color = NeonPurple,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                modifier = Modifier.align(Alignment.Start)
            )
            Text(
                text = "Focus Shield",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black,
                color = Color.White,
                modifier = Modifier.align(Alignment.Start)
            )
            Text(
                text = "Establish a high-contrast boundary to shelter active processing.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.weight(0.4f))

            // --- GLOWING CIRCULAR COUNTDOWN TIMER ---
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(240.dp)
                    .testTag("timer_ring_container")
            ) {
                // Background shadow track
                CircularProgressIndicator(
                    progress = 1f,
                    modifier = Modifier.size(230.dp),
                    color = CharcoalCardElevated,
                    strokeWidth = 12.dp
                )

                // Neon active track (animated progress)
                CircularProgressIndicator(
                    progress = progress,
                    modifier = Modifier.size(230.dp),
                    color = if (isCompleted) DoneGreen else NeonPurple,
                    strokeWidth = 12.dp
                )

                // Centred Visual state (Count of time left)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    AnimatedVisibility(
                        visible = isCompleted,
                        enter = fadeIn() + scaleIn(),
                        exit = fadeOut() + scaleOut()
                    ) {
                        Text(
                            text = "CONVERGED",
                            style = MaterialTheme.typography.labelSmall,
                            color = DoneGreen,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = formattedTime,
                        fontSize = 54.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace, // Keep stable numbers layout to avoid shifting
                        color = if (isCompleted) DoneGreen else Color.White,
                        modifier = Modifier.testTag("timer_countdown_text")
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (isRunning) "SHIELD ACTIVE" else "DORMANT",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isRunning) NeonPurpleLight else TextSecondary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.weight(0.3f))

            // --- TACTICAL SPEED PRESETS ---
            Text(
                text = "TEMPORAL PRESETS",
                style = MaterialTheme.typography.labelSmall,
                color = NeonPurple,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val presets = listOf(
                    PresetOption("5m", 5, 0),
                    PresetOption("15m", 15, 0),
                    PresetOption("25m", 25, 0),
                    PresetOption("45m", 45, 0)
                )

                presets.forEach { preset ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(CharcoalCard)
                            .border(1.dp, PurpleBorder, RoundedCornerShape(10.dp))
                            .clickable {
                                if (!isRunning) {
                                    viewModel.setTimerTime(preset.mins, preset.secs)
                                }
                            }
                            .testTag("preset_${preset.label}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = preset.label,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- DEEP MICRO ADJUSTERS (+- buttons) ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(CharcoalCard)
                        .border(1.dp, PurpleBorder, RoundedCornerShape(12.dp)),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = {
                            if (!isRunning) {
                                val currentMin = viewModel.timerMinutesInput
                                if (currentMin > 0) {
                                    viewModel.setTimerTime(currentMin - 1, viewModel.timerSecondsInput)
                                }
                            }
                        },
                        modifier = Modifier.testTag("timer_minus_min")
                    ) {
                        Icon(imageVector = Icons.Default.Remove, contentDescription = "Minus Minute", tint = TextSecondary)
                    }
                    Text(
                        text = "${viewModel.timerMinutesInput}m",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = {
                            if (!isRunning) {
                                val currentMin = viewModel.timerMinutesInput
                                viewModel.setTimerTime(currentMin + 1, viewModel.timerSecondsInput)
                            }
                        },
                        modifier = Modifier.testTag("timer_plus_min")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add Minute", tint = Color.White)
                    }
                }

                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(CharcoalCard)
                        .border(1.dp, PurpleBorder, RoundedCornerShape(12.dp)),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = {
                            if (!isRunning) {
                                val currentSec = viewModel.timerSecondsInput
                                if (currentSec >= 10) {
                                    viewModel.setTimerTime(viewModel.timerMinutesInput, currentSec - 10)
                                } else if (viewModel.timerMinutesInput > 0) {
                                    viewModel.setTimerTime(viewModel.timerMinutesInput - 1, currentSec + 50)
                                }
                            }
                        },
                        modifier = Modifier.testTag("timer_minus_sec")
                    ) {
                        Icon(imageVector = Icons.Default.Remove, contentDescription = "Minus 10s", tint = TextSecondary)
                    }
                    Text(
                        text = "${viewModel.timerSecondsInput}s",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = {
                            if (!isRunning) {
                                val currentSec = viewModel.timerSecondsInput
                                if (currentSec >= 50) {
                                    viewModel.setTimerTime(viewModel.timerMinutesInput + 1, currentSec - 50)
                                } else {
                                    viewModel.setTimerTime(viewModel.timerMinutesInput, currentSec + 10)
                                }
                            }
                        },
                        modifier = Modifier.testTag("timer_plus_sec")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add 10s", tint = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.weight(0.4f))

            // --- TACTILE PLAYBACK RUNNERS (PLAY / PAUSE / REFRESH) ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // RESET
                Button(
                    onClick = { viewModel.resetTimer() },
                    colors = ButtonDefaults.buttonColors(containerColor = CharcoalCardElevated),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp)
                        .testTag("timer_reset")
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "Reset Frame", tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Reset", fontWeight = FontWeight.Bold, color = Color.White)
                }

                // PLAY/PAUSE TRIGGER
                Button(
                    onClick = {
                        if (isRunning) {
                            viewModel.pauseTimer()
                        } else {
                            viewModel.startTimer()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRunning) AccentViolet else NeonPurple
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1.5f)
                        .height(54.dp)
                        .testTag("timer_play_pause")
                ) {
                    Icon(
                        imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Trigger Cycle Play",
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isRunning) "Pause" else "Commence",
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

data class PresetOption(
    val label: String,
    val mins: Int,
    val secs: Int
)
