package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class TaskRepository(
    private val longTaskDao: LongTaskDao,
    private val dailyRoutineDao: DailyRoutineDao,
    private val completionLogDao: CompletionLogDao
) {
    // TASKS
    fun getTasksByStatus(status: String): Flow<List<LongTask>> = 
        longTaskDao.getTasksByStatus(status)

    fun getAllTasks(): Flow<List<LongTask>> = 
        longTaskDao.getAllTasks()

    fun getTaskById(id: Int): Flow<LongTask?> = 
        longTaskDao.getTaskById(id)

    suspend fun getTaskByIdSuspend(id: Int): LongTask? =
        longTaskDao.getTaskByIdSuspend(id)

    suspend fun insertTask(task: LongTask): Long = 
        longTaskDao.insertTask(task)

    suspend fun updateTask(task: LongTask) = 
        longTaskDao.updateTask(task)

    suspend fun deleteTaskById(id: Int) {
        longTaskDao.deleteTaskById(id)
        completionLogDao.deleteLogsForTask(id)
    }

    suspend fun startQuest(task: LongTask) {
        val updated = task.copy(
            status = "Active",
            startDate = System.currentTimeMillis()
        )
        longTaskDao.updateTask(updated)
    }

    suspend fun markTaskComplete(task: LongTask) {
        val updated = task.copy(status = "Completed")
        longTaskDao.updateTask(updated)
    }

    suspend fun incrementTaskProgress(task: LongTask) {
        val now = System.currentTimeMillis()
        val updated = task.copy(daysCompleted = task.daysCompleted + 1)
        longTaskDao.updateTask(updated)
        completionLogDao.insertLog(CompletionLog(parentTaskId = task.id, completionDate = now))
    }

    suspend fun addBonusDay(task: LongTask) {
        val updated = task.copy(bonusDaysAdded = task.bonusDaysAdded + 1)
        longTaskDao.updateTask(updated)
    }

    // ROUTINES (With automatic daily reset logic)
    suspend fun performDailyResetIfNeeded(todayStr: String) {
        try {
            val list = dailyRoutineDao.getAllRoutinesSuspend()
            for (routine in list) {
                if (routine.lastDoneDate != todayStr) {
                    dailyRoutineDao.updateRoutine(
                        routine.copy(isDoneToday = false, lastDoneDate = todayStr)
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getRoutinesWithResetCheck(todayStr: String): Flow<List<DailyRoutine>> {
        return dailyRoutineDao.getAllRoutines()
    }

    suspend fun insertRoutine(routine: DailyRoutine): Long =
        dailyRoutineDao.insertRoutine(routine)

    suspend fun updateRoutine(routine: DailyRoutine) =
        dailyRoutineDao.updateRoutine(routine)

    suspend fun toggleRoutineCompletion(routine: DailyRoutine, todayStr: String) {
        val updated = routine.copy(
            isDoneToday = !routine.isDoneToday,
            lastDoneDate = todayStr
        )
        dailyRoutineDao.updateRoutine(updated)
    }

    suspend fun deleteRoutineById(id: Int) =
        dailyRoutineDao.deleteRoutineById(id)

    // COMPLETION LOGS
    fun getLogsForTask(taskId: Int): Flow<List<CompletionLog>> =
        completionLogDao.getLogsForTask(taskId)

    fun getAllLogs(): Flow<List<CompletionLog>> =
        completionLogDao.getAllLogs()

    suspend fun insertCompletionLog(taskId: Int, timestamp: Long) {
        completionLogDao.insertLog(CompletionLog(parentTaskId = taskId, completionDate = timestamp))
    }
}
