package com.example.libro.Database

import androidx.room.*
import java.util.*

@Dao
interface PurchasedBookDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchasedBook(purchasedBook: PurchasedBook): Long

    @Update
    suspend fun updatePurchasedBook(purchasedBook: PurchasedBook)

    @Delete
    suspend fun deletePurchasedBook(purchasedBook: PurchasedBook)

    @Query("SELECT * FROM purchased_books WHERE purchase_id = :purchaseId")
    suspend fun getPurchasedBookById(purchaseId: Long): PurchasedBook?

    @Query("SELECT SUM(price) FROM purchased_books")
    suspend fun getTotalSpent(): Double?

    @Query("SELECT SUM(price) FROM purchased_books WHERE purchase_date BETWEEN :startDate AND :endDate")
    suspend fun getTotalSpentBetweenDates(startDate: Date, endDate: Date): Double?

    @Query("SELECT AVG(price) FROM purchased_books")
    suspend fun getAverageBookPrice(): Double?

    @Query("SELECT * FROM purchased_books ORDER BY price DESC LIMIT 1")
    suspend fun getMostExpensivePurchase(): PurchasedBook?

    @Query("SELECT * FROM purchased_books WHERE price > 0 ORDER BY price ASC LIMIT 1")
    suspend fun getCheapestPurchase(): PurchasedBook?

    @Query("SELECT COUNT(*) FROM purchased_books")
    suspend fun getPurchaseCount(): Int

    @Query("SELECT DISTINCT purchase_location FROM purchased_books WHERE purchase_location IS NOT NULL AND purchase_location != ''")
    suspend fun getUniquePurchaseLocations(): List<String>

    @Query("DELETE FROM purchased_books WHERE book_id = :bookId")
    suspend fun deletePurchasesForBook(bookId: Long)

    @Query("DELETE FROM purchased_books")
    suspend fun deleteAllPurchases()

    @Query("SELECT EXISTS(SELECT 1 FROM purchased_books WHERE book_id = :bookId)")
    suspend fun isBookPurchased(bookId: Long): Boolean

    @Query("SELECT * FROM purchased_books WHERE book_id = :bookId ORDER BY purchase_date DESC LIMIT 1")
    suspend fun getLastPurchaseForBook(bookId: Long): PurchasedBook?

    @Query("SELECT * FROM purchased_books WHERE book_id = :bookId")
    suspend fun getPurchasedBookByBookId(bookId: Long): PurchasedBook?

    @Query("DELETE FROM purchased_books WHERE book_id = :bookId")
    suspend fun deletePurchasedBookByBookId(bookId: Long)
}