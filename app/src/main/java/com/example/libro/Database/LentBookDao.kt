package com.example.libro.Database

import androidx.room.*

@Dao
interface LentBookDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLentBook(lentBook: LentBook)

    @Update
    suspend fun updateLentBook(lentBook: LentBook)

    @Delete
    suspend fun deleteLentBook(lentBook: LentBook)

    @Query("SELECT * FROM lent_books WHERE book_id = :bookId")
    suspend fun getLentBookByBookId(bookId: Long): LentBook?

    @Query("DELETE FROM lent_books WHERE book_id = :bookId")
    suspend fun deleteLentBookByBookId(bookId: Long)

}