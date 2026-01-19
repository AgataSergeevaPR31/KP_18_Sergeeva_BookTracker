package com.example.libro.Database

import androidx.room.*

@Dao
interface CategoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category): Long

    @Update
    suspend fun updateCategory(category: Category)

    @Delete
    suspend fun deleteCategory(category: Category)

    @Query("SELECT * FROM categories WHERE category_type = :categoryType ORDER BY display_order")
    suspend fun getCategoriesByType(categoryType: Category.CategoryType): List<Category>

    @Query("SELECT COUNT(*) FROM categories WHERE name = :name")
    suspend fun countCategoriesByName(name: String): Int

    @Query("SELECT * FROM categories")
    suspend fun getAllCategories(): List<Category>

    @Query("SELECT * FROM categories WHERE name = :name")
    suspend fun getCategoryByName(name: String): Category?
}