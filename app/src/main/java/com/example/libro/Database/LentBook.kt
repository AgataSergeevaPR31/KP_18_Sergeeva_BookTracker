package com.example.libro.Database

import androidx.room.*
import java.util.*

@Entity(
    tableName = "lent_books",
    foreignKeys = [
        ForeignKey(
            entity = Book::class,
            parentColumns = ["book_id"],
            childColumns = ["book_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class LentBook(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "lent_id")
    val lentId: Long = 0,

    @ColumnInfo(name = "book_id")
    val bookId: Long,

    @ColumnInfo(name = "borrowed_by")
    val borrowedBy: String,

    @ColumnInfo(name = "borrow_date")
    val borrowDate: Date,

    @ColumnInfo(name = "expected_return_date")
    val expectedReturnDate: Date? = null,

    @ColumnInfo(name = "actual_return_date")
    val actualReturnDate: Date? = null
)
