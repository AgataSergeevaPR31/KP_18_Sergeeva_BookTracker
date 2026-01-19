package com.example.libro.Database
import androidx.room.*

@Dao
interface BookDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: Book): Long

    @Delete
    suspend fun deleteBook(book: Book)

    @Query("SELECT * FROM books ORDER BY title")
    suspend fun getAllBooks(): List<Book>

    @Query("SELECT * FROM books WHERE is_read = 1")
    suspend fun getReadBooks(): List<Book>

    @Query("SELECT * FROM books WHERE is_read = 0")
    suspend fun getUnreadBooks(): List<Book>

    @Query("UPDATE books SET current_page = :currentPage WHERE book_id = :bookId")
    suspend fun updateCurrentPage(bookId: Long, currentPage: Int)

    @Query("UPDATE books SET is_read = :isRead WHERE book_id = :bookId")
    suspend fun updateReadStatus(bookId: Long, isRead: Boolean)

    @Query("SELECT * FROM books WHERE book_id = :bookId")
    suspend fun getBookById(bookId: Long): Book?

    @Update
    suspend fun updateBook(book: Book)

    @Query("SELECT COUNT(*) FROM books WHERE is_read = 1")
    suspend fun getBooksReadCount(): Int

    @Query("SELECT * FROM books")
    suspend fun getBooks(): List<Book>

}