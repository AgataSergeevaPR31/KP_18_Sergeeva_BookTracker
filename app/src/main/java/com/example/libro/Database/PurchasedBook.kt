package com.example.libro.Database

import androidx.room.*
import java.util.*

@Entity(
    tableName = "purchased_books",
    foreignKeys = [
        ForeignKey(
            entity = Book::class,
            parentColumns = ["book_id"],
            childColumns = ["book_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class PurchasedBook(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "purchase_id")
    val purchaseId: Long = 0,

    @ColumnInfo(name = "book_id")
    val bookId: Long,

    @ColumnInfo(name = "purchase_date")
    val purchaseDate: Date,

    @ColumnInfo(name = "price")
    val price: Double,

    @ColumnInfo(name = "purchase_location")
    val purchaseLocation: String? = null,

    @ColumnInfo(name = "note")
    val note: String? = null
)