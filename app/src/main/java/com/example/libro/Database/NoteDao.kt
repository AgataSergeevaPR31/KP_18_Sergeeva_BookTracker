package com.example.libro.Database

import androidx.room.*

@Dao
interface NoteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note): Long

    @Update
    suspend fun updateNote(note: Note)

    @Delete
    suspend fun deleteNote(note: Note)

    @Query("SELECT * FROM notes WHERE book_id = :bookId ORDER BY creation_date DESC")
    suspend fun getNotesByBookId(bookId: Long): List<Note>

    @Query("SELECT * FROM notes WHERE is_favorite = 1")
    suspend fun getFavoriteNotes(): List<Note>

    @Query("SELECT COUNT(*) FROM notes WHERE book_id = :bookId")
    suspend fun getNotesCountByBookId(bookId: Long): Int

    @Query("SELECT * FROM notes ORDER BY creation_date DESC")
    suspend fun getAllNotes(): List<Note>
}