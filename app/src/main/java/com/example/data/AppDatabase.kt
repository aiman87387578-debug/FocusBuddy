package com.example.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface LongTaskDao {
    @Query("SELECT * FROM long_tasks ORDER BY id DESC")
    fun getAllTasks(): Flow<List<LongTask>>

    @Query("SELECT * FROM long_tasks WHERE status = :status ORDER BY id DESC")
    fun getTasksByStatus(status: String): Flow<List<LongTask>>

    @Query("SELECT * FROM long_tasks WHERE id = :id")
    fun getTaskById(id: Int): Flow<LongTask?>

    @Query("SELECT * FROM long_tasks WHERE id = :id")
    suspend fun getTaskByIdSuspend(id: Int): LongTask?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: LongTask): Long

    @Update
    suspend fun updateTask(task: LongTask)

    @Delete
    suspend fun deleteTask(task: LongTask)

    @Query("DELETE FROM long_tasks WHERE id = :id")
    suspend fun deleteTaskById(id: Int)
}

@Dao
interface DailyRoutineDao {
    @Query("SELECT * FROM daily_routines ORDER BY startTime ASC")
    fun getAllRoutines(): Flow<List<DailyRoutine>>

    @Query("SELECT * FROM daily_routines ORDER BY startTime ASC")
    suspend fun getAllRoutinesSuspend(): List<DailyRoutine>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutine(routine: DailyRoutine): Long

    @Update
    suspend fun updateRoutine(routine: DailyRoutine)

    @Delete
    suspend fun deleteRoutine(routine: DailyRoutine)

    @Query("DELETE FROM daily_routines WHERE id = :id")
    suspend fun deleteRoutineById(id: Int)
}

@Dao
interface CompletionLogDao {
    @Query("SELECT * FROM completion_logs WHERE parentTaskId = :taskId ORDER BY completionDate DESC")
    fun getLogsForTask(taskId: Int): Flow<List<CompletionLog>>

    @Query("SELECT * FROM completion_logs ORDER BY completionDate DESC")
    fun getAllLogs(): Flow<List<CompletionLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: CompletionLog): Long

    @Query("DELETE FROM completion_logs WHERE parentTaskId = :taskId AND date(completionDate/1000, 'unixepoch') = date(:dateMillis/1000, 'unixepoch')")
    suspend fun deleteLogForDate(taskId: Int, dateMillis: Long)

    @Query("DELETE FROM completion_logs WHERE parentTaskId = :taskId")
    suspend fun deleteLogsForTask(taskId: Int)
}

@Database(entities = [LongTask::class, DailyRoutine::class, CompletionLog::class], version = 2, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun longTaskDao(): LongTaskDao
    abstract fun dailyRoutineDao(): DailyRoutineDao
    abstract fun completionLogDao(): CompletionLogDao
}
