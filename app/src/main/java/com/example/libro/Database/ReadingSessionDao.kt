package com.example.libro.Database

import androidx.room.*

@Dao
interface ReadingSessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReadingSession(session: ReadingSession): Long

    @Update
    suspend fun updateReadingSession(session: ReadingSession)

    @Query("SELECT * FROM reading_sessions")
    suspend fun getAllSessions(): List<ReadingSession>

    @Query("""
        SELECT COUNT(DISTINCT date(start_time/1000, 'unixepoch')) 
        FROM reading_sessions 
        WHERE date(start_time/1000, 'unixepoch') = date('now')
    """)
    suspend fun getReadingDaysToday(): Int

    @Query("SELECT SUM(duration) FROM reading_sessions")
    suspend fun getTotalReadingTime(): Long?

    @Query("SELECT SUM(end_page - start_page) FROM reading_sessions WHERE end_page IS NOT NULL")
    suspend fun getTotalPagesRead(): Int?

    @Query("SELECT SUM(duration) FROM reading_sessions WHERE book_id = :bookId")
    suspend fun getTotalReadingTimeForBook(bookId: Long): Long?

    @Query("SELECT * FROM reading_sessions WHERE book_id = :bookId")
    suspend fun getSessionsForBook(bookId: Long): List<ReadingSession>
}