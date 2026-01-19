package com.example.libro.Database

import androidx.room.*

@Dao
interface BookCategoryDao {

    @Delete
    suspend fun deleteBookCategory(bookCategory: BookCategory)

    @Query("DELETE FROM book_categories WHERE book_id = :bookId AND category_id = :categoryId")
    suspend fun removeBookFromCategory(bookId: Long, categoryId: Long)

    @Query("SELECT c.* FROM categories c INNER JOIN book_categories bc ON c.category_id = bc.category_id WHERE bc.book_id = :bookId")
    suspend fun getCategoriesForBook(bookId: Long): List<Category>

    @Query("DELETE FROM book_categories WHERE book_id = :bookId")
    suspend fun deleteBookCategoriesForBook(bookId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookCategory(bookCategory: BookCategory): Long

    @Query("SELECT * FROM book_categories WHERE category_id = :categoryId")
    suspend fun getBookCategoriesByCategoryId(categoryId: Long): List<BookCategory>

}
