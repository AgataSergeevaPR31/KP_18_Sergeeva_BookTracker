package com.example.libro.Database

import androidx.room.*
import java.io.Serializable

@Entity(tableName = "achievements")
data class Achievement(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "achievement_id")
    val achievementId: Long = 0,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "description")
    val description: String,

    @ColumnInfo(name = "type")
    val type: AchievementType = AchievementType.PAGES_READ,

    @ColumnInfo(name = "required_value")
    val requiredValue: Double,

    @ColumnInfo(name = "display_order")
    val displayOrder: Int
) : Serializable {
    enum class AchievementType {
        PAGES_READ,
        BOOKS_READ,
        READING_STREAK,
        NIGHT_READING,
        LONG_BOOK,
        READING_TIME,
        PAGES_PER_SESSION,
        FIRST_STEPS
    }

    companion object {
        private const val serialVersionUID = 1L
    }
}