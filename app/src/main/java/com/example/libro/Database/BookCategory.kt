package com.example.libro.Database

import androidx.room.*
import java.util.*

@Entity(
    tableName = "book_categories",
    primaryKeys = ["book_id", "category_id"],
    foreignKeys = [
        ForeignKey(
            entity = Book::class,
            parentColumns = ["book_id"],
            childColumns = ["book_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Category::class,
            parentColumns = ["category_id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class BookCategory(
    @ColumnInfo(name = "book_id")
    val bookId: Long,

    @ColumnInfo(name = "category_id")
    val categoryId: Long,

    @ColumnInfo(name = "added_date")
    val addedDate: Date,

    @ColumnInfo(name = "order_in_category")
    val orderInCategory: Int = 0,

    @ColumnInfo(name = "notes")
    val notes: String? = null
)