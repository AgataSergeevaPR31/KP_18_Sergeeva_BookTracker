package com.example.libro.Database

import androidx.room.*

@Entity(
    tableName = "book_genres",
    primaryKeys = ["book_id", "genre_id"],
    foreignKeys = [
        ForeignKey(
            entity = Book::class,
            parentColumns = ["book_id"],
            childColumns = ["book_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Genre::class,
            parentColumns = ["genre_id"],
            childColumns = ["genre_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class BookGenre(
    @ColumnInfo(name = "book_id")
    val bookId: Long,

    @ColumnInfo(name = "genre_id")
    val genreId: Long
)