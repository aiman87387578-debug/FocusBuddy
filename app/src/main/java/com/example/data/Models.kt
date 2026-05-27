package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

@Entity(tableName = "long_tasks")
data class LongTask(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val type: String, // "Fixed" or "Infinite"
    val targetDays: Int, // target_days
    val dailyGoalMins: Int, // daily_goal_mins
    val status: String, // "Pending", "Active", "Completed"
    val startDate: Long, // timestamp, start_date (0 if not started)
    val daysCompleted: Int, // days_completed
    val bonusDaysAdded: Int = 0 // bonus_days_added
)

@Entity(tableName = "daily_routines")
data class DailyRoutine(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val startTime: String, // start_time (e.g. "08:30" or "14:15")
    val repeatDays: List<String>, // repeat_days (List of Strings, e.g., ["Monday", "Friday"])
    val isDoneToday: Boolean = false, // is_done_today
    val lastDoneDate: String = "" // "yyyy-MM-dd" to support daily resets
)

@Entity(tableName = "completion_logs")
data class CompletionLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val parentTaskId: Int, // parent_task_ref stored as Task ID
    val completionDate: Long // completion_date (Timestamp)
)

class Converters {
    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        return value?.joinToString(",") ?: ""
    }

    @TypeConverter
    fun toStringList(value: String?): List<String> {
        if (value.isNullOrEmpty()) return emptyList()
        return value.split(",")
    }
}
