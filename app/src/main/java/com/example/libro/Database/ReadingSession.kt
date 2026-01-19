package com.example.libro.Database

import androidx.room.*
import java.util.*

@Entity(
    tableName = "reading_sessions",
    foreignKeys = [
        ForeignKey(
            entity = Book::class,
            parentColumns = ["book_id"],
            childColumns = ["book_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ReadingSession(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "session_id")
    val sessionId: Long = 0,

    @ColumnInfo(name = "book_id")
    val bookId: Long,

    @ColumnInfo(name = "start_time")
    val startTime: Date,

    @ColumnInfo(name = "end_time")
    val endTime: Date? = null,

    @ColumnInfo(name = "start_page")
    val startPage: Int,

    @ColumnInfo(name = "end_page")
    val endPage: Int? = null,

    @ColumnInfo(name = "duration")
    val duration: Long = 0,

    @ColumnInfo(name = "reading_speed")
    val readingSpeed: Double? = null
)