package com.example.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.data.AppDatabase
import com.example.data.CompletionLog
import com.example.data.DailyRoutine
import com.example.data.LongTask
import com.example.data.TaskRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainViewModel(
    application: Application,
    private val repository: TaskRepository
) : AndroidViewModel(application) {

    // --- TIMING HELPERS ---
    // Memory cache to instantly disable and block multiple logs for the same task in a single session/day
    private val loggedTasksTodayCache = java.util.concurrent.ConcurrentHashMap<Int, String>()

    fun getTodayDayName(): String {
        val calendar = Calendar.getInstance()
        return when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> "Monday"
            Calendar.TUESDAY -> "Tuesday"
            Calendar.WEDNESDAY -> "Wednesday"
            Calendar.THURSDAY -> "Thursday"
            Calendar.FRIDAY -> "Friday"
            Calendar.SATURDAY -> "Saturday"
            Calendar.SUNDAY -> "Sunday"
            else -> "Monday"
        }
    }

    fun getTodayDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    val todayStr = getTodayDateString()
    val todayName = getTodayDayName()

    // --- QUESTS / LONG TASKS ---
    val activeTasks: StateFlow<List<LongTask>> = repository.getTasksByStatus("Active")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingTasks: StateFlow<List<LongTask>> = repository.getTasksByStatus("Pending")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val completedTasks: StateFlow<List<LongTask>> = repository.getTasksByStatus("Completed")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTasks: StateFlow<List<LongTask>> = repository.getAllTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All completion logs
    val allLogs: StateFlow<List<CompletionLog>> = repository.getAllLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- DAILY ROUTINES ---
    // Dashboard routines (Routines that repeat today, sorted by start_time ascending)
    val dashboardRoutines: StateFlow<List<DailyRoutine>> = repository.getRoutinesWithResetCheck(todayStr)
        .map { routines ->
            routines.filter { it.repeatDays.contains(todayName) }
                .sortedWith(compareBy { it.startTime })
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- WEEKLY PLANNER ROUTINES ---
    var selectedPlannerDay by mutableStateOf(getTodayDayName())
        private set

    fun selectPlannerDay(day: String) {
        selectedPlannerDay = day
    }

    // Weekly planner filtered routines
    val plannerRoutines: StateFlow<List<DailyRoutine>> = combine(
        repository.getRoutinesWithResetCheck(todayStr),
        MutableStateFlow("") // dummy to satisfy combine syntax if needed, or just observe selectedPlannerDay manually in combined block
    ) { routines, _ ->
        // We will filter based on selectedPlannerDay dynamically on recomposition or combine with a Flow of SelectedDay
        routines
    }.flatMapLatest { list ->
        // We can create a flow from selectedPlannerDay state, or just compose it in UI.
        // To be reactive and strictly flow-based, let's observe directly.
        flowOf(list)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- MAIN TRANSACTIONS ---
    fun createQuest(title: String, type: String, targetDays: Int, dailyGoalMins: Int) {
        viewModelScope.launch {
            repository.insertTask(
                LongTask(
                    title = title,
                    type = type,
                    targetDays = targetDays,
                    dailyGoalMins = dailyGoalMins,
                    status = "Pending",
                    startDate = 0L,
                    daysCompleted = 0
                )
            )
        }
    }

    fun startQuest(task: LongTask) {
        viewModelScope.launch {
            repository.startQuest(task)
        }
    }

    fun deleteQuest(taskId: Int) {
        viewModelScope.launch {
            repository.deleteTaskById(taskId)
        }
    }

    fun editQuest(task: LongTask) {
        viewModelScope.launch {
            repository.updateTask(task)
        }
    }

    fun incrementTaskProgress(task: LongTask) {
        val todayStr = getTodayDateString()
        if (loggedTasksTodayCache[task.id] == todayStr) {
            return
        }
        loggedTasksTodayCache[task.id] = todayStr

        viewModelScope.launch {
            // Guarantee progress can only be logged once per day
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val logs = repository.getAllLogs().first()
            val alreadyLogged = logs.any { log ->
                log.parentTaskId == task.id && sdf.format(Date(log.completionDate)) == todayStr
            }
            if (alreadyLogged) {
                return@launch
            }

            repository.incrementTaskProgress(task)
            // Auto complete if target days reached for fixed tasks
            val freshTask = repository.getTaskByIdSuspend(task.id) ?: return@launch
            if (freshTask.type == "Fixed" && freshTask.daysCompleted >= (freshTask.targetDays + freshTask.bonusDaysAdded)) {
                repository.markTaskComplete(freshTask)
            }
        }
    }

    fun claimBonusDay(task: LongTask) {
        viewModelScope.launch {
            repository.addBonusDay(task)
        }
    }

    fun forceCompleteQuest(task: LongTask) {
        viewModelScope.launch {
            repository.markTaskComplete(task)
        }
    }

    // --- ROUTINE ACTIONS ---
    fun createRoutine(title: String, startTime: String, repeatDays: List<String>) {
        viewModelScope.launch {
            repository.insertRoutine(
                DailyRoutine(
                    title = title,
                    startTime = startTime,
                    repeatDays = repeatDays,
                    isDoneToday = false,
                    lastDoneDate = todayStr
                )
            )
        }
    }

    fun updateRoutine(routine: DailyRoutine) {
        viewModelScope.launch {
            repository.updateRoutine(routine)
        }
    }

    fun deleteRoutine(routineId: Int) {
        viewModelScope.launch {
            repository.deleteRoutineById(routineId)
        }
    }

    fun toggleRoutine(routine: DailyRoutine) {
        viewModelScope.launch {
            repository.toggleRoutineCompletion(routine, todayStr)
        }
    }

    // --- STANDALONE TIMER STATE ---
    // Independent countdown clock values
    var timerMinutesInput by mutableStateOf(5)
    var timerSecondsInput by mutableStateOf(0)

    var timerTotalMins by mutableStateOf(5)
    var timerTotalSecs by mutableStateOf(0)

    var timerRemainingMillis by mutableStateOf(5 * 60 * 1000L)
    var isTimerRunning by mutableStateOf(false)
    var isTimerCompleted by mutableStateOf(false)

    private var timerJob: Job? = null

    fun setTimerTime(mins: Int, secs: Int) {
        timerMinutesInput = mins.coerceIn(0, 599)
        timerSecondsInput = secs.coerceIn(0, 59)
        if (!isTimerRunning) {
            timerTotalMins = timerMinutesInput
            timerTotalSecs = timerSecondsInput
            timerRemainingMillis = (timerMinutesInput * 60L + timerSecondsInput) * 1000L
            isTimerCompleted = false
        }
    }

    fun startTimer() {
        if (isTimerRunning) return
        if (timerRemainingMillis <= 0L) {
            // Reset to configured input if expired
            timerRemainingMillis = (timerMinutesInput * 60L + timerSecondsInput) * 1000L
        }
        
        isTimerRunning = true
        isTimerCompleted = false
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            var lastTick = System.currentTimeMillis()
            while (timerRemainingMillis > 0 && isTimerRunning) {
                delay(100)
                val now = System.currentTimeMillis()
                val delta = now - lastTick
                lastTick = now
                timerRemainingMillis = (timerRemainingMillis - delta).coerceAtLeast(0L)
                if (timerRemainingMillis <= 0L) {
                    isTimerRunning = false
                    isTimerCompleted = true
                    // Visual/vibrational feedback or sound played by Composable
                }
            }
        }
    }

    fun pauseTimer() {
        isTimerRunning = false
        timerJob?.cancel()
    }

    fun resetTimer() {
        isTimerRunning = false
        isTimerCompleted = false
        timerJob?.cancel()
        timerRemainingMillis = (timerMinutesInput * 60L + timerSecondsInput) * 1000L
    }

    fun getFormattedTimer(): String {
        val totalSecs = (timerRemainingMillis + 999) / 1000 // ceiling division to show input state
        val m = totalSecs / 60
        val s = totalSecs % 60
        return String.format(Locale.getDefault(), "%02d:%02d", m, s)
    }

    fun getTimerProgress(): Float {
        val totalMillis = (timerTotalMins * 60L + timerTotalSecs) * 1000L
        if (totalMillis == 0L) return 0f
        return (timerRemainingMillis.toFloat() / totalMillis).coerceIn(0f, 1f)
    }

    init {
        // Run initial seed logic if the database is completely empty so that the user doesn't face a blank sheet
        viewModelScope.launch {
            repository.performDailyResetIfNeeded(todayStr)
            val dbRoutines = repository.getRoutinesWithResetCheck(todayStr).first()
            if (dbRoutines.isEmpty()) {
                // Seed some useful ADHD routines!
                repository.insertRoutine(
                    DailyRoutine(
                        title = "Visual Declutter & Desk Sweep",
                        startTime = "09:00",
                        repeatDays = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"),
                        isDoneToday = false,
                        lastDoneDate = todayStr
                    )
                )
                repository.insertRoutine(
                    DailyRoutine(
                        title = "Hydration Rush & Deep Breath",
                        startTime = "14:00",
                        repeatDays = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"),
                        isDoneToday = false,
                        lastDoneDate = todayStr
                    )
                )
                repository.insertRoutine(
                    DailyRoutine(
                        title = "Inbox Zero Reset (Admin Cleansing)",
                        startTime = "17:00",
                        repeatDays = listOf("Monday", "Friday"),
                        isDoneToday = false,
                        lastDoneDate = todayStr
                    )
                )
            }
            // Seed a starter long task too
            val dbTasks = repository.getAllTasks().first()
            if (dbTasks.isEmpty()) {
                repository.insertTask(
                    LongTask(
                        title = "15-Min Focused Coding Sprint",
                        type = "Infinite",
                        targetDays = 30,
                        dailyGoalMins = 15,
                        status = "Active",
                        startDate = System.currentTimeMillis() - 86400000L * 3, // started 3 days ago
                        daysCompleted = 3
                    )
                )
                repository.insertTask(
                    LongTask(
                        title = "Deep Morning Reading Challenge",
                        type = "Fixed",
                        targetDays = 14,
                        dailyGoalMins = 20,
                        status = "Pending",
                        startDate = 0L,
                        daysCompleted = 0
                    )
                )
                // Log completions for seeded active task
                val activeTask = repository.getAllTasks().first().firstOrNull { it.status == "Active" }
                if (activeTask != null) {
                    repository.insertCompletionLog(activeTask.id, System.currentTimeMillis() - 86400000L * 3)
                    repository.insertCompletionLog(activeTask.id, System.currentTimeMillis() - 86400000L * 2)
                    repository.insertCompletionLog(activeTask.id, System.currentTimeMillis() - 86400000L)
                }
            }
        }
    }
}

class MainViewModelFactory(
    private val application: Application,
    private val repository: TaskRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
