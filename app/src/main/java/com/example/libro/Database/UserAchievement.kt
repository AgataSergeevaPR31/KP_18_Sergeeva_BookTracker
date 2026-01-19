package com.example.libro.Database

import androidx.room.*
import java.io.Serializable
import java.util.*

@Entity(
    tableName = "user_achievements",
    foreignKeys = [
        ForeignKey(
            entity = Achievement::class,
            parentColumns = ["achievement_id"],
            childColumns = ["achievement_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class UserAchievement(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "user_achievement_id")
    val userAchievementId: Long = 0,

    @ColumnInfo(name = "achievement_id")
    val achievementId: Long,

    @ColumnInfo(name = "achievement_date")
    val achievementDate: Date? = null,

    @ColumnInfo(name = "current_progress")
    val currentProgress: Double = 0.0,

    @ColumnInfo(name = "is_achieved")
    val isAchieved: Boolean = false
) : Serializable {
    companion object {
        private const val serialVersionUID = 1L
    }
}