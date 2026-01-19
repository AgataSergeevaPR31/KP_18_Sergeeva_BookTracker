package com.example.libro.Database

import androidx.room.*
import java.io.Serializable
import java.util.*

@Entity(
    tableName = "books",
    indices = [Index(value = ["title", "author"])]
)
data class Book(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "book_id")
    val bookId: Long = 0,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "author")
    val author: String? = null,

    @ColumnInfo(name = "cover_image")
    val coverImage: String? = null,

    @ColumnInfo(name = "page_count")
    val pageCount: Int,

    @ColumnInfo(name = "is_read")
    var isRead: Boolean = false,

    @ColumnInfo(name = "start_date")
    var startDate: Date? = null,

    @ColumnInfo(name = "end_date")
    var endDate: Date? = null,

    @ColumnInfo(name = "current_page")
    var currentPage: Int = 0,

    @ColumnInfo(name = "rating")
    var rating: Float? = null,

    @ColumnInfo(name = "total_reading_time")
    var totalReadingTime: Long = 0,

    @ColumnInfo(name = "publisher")
    val publisher: String? = null,

    @ColumnInfo(name = "publication_year")
    val publicationYear: Int? = null,

    @ColumnInfo(name = "publication_month")
    val publicationMonth: Int? = null,

    @ColumnInfo(name = "translator")
    val translator: String? = null,

    @ColumnInfo(name = "is_purchased")
    val isPurchased: Boolean = true,

    @ColumnInfo(name = "is_lent")
    var isLent: Boolean = false,

    @ColumnInfo(name = "book_type")
    val bookType: BookType = BookType.PRINTED
) : Serializable {
    enum class BookType {
        PRINTED, ELECTRONIC, AUDIO
    }
}