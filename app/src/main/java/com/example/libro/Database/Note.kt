package com.example.libro.Database

import androidx.room.*
import java.util.*

@Entity(
    tableName = "notes",
    foreignKeys = [
        ForeignKey(
            entity = Book::class,
            parentColumns = ["book_id"],
            childColumns = ["book_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Note(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "note_id")
    val noteId: Long = 0,

    @ColumnInfo(name = "book_id")
    val bookId: Long,

    @ColumnInfo(name = "note_text")
    val noteText: String,

    @ColumnInfo(name = "creation_date")
    val creationDate: Date,

    @ColumnInfo(name = "page_number")
    val pageNumber: Int? = null,

    @ColumnInfo(name = "is_favorite")
    val isFavorite: Boolean = false,

    @ColumnInfo(name = "image_path")
    val imagePath: String? = null,

    @ColumnInfo(name = "note_category")
    val category: String? = null
)