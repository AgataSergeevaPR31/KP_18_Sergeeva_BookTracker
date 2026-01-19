package com.example.libro.Database

import androidx.room.TypeConverter
import java.util.*

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromBookType(value: String?): Book.BookType? {
        return value?.let { Book.BookType.valueOf(it) }
    }

    @TypeConverter
    fun bookTypeToString(bookType: Book.BookType?): String? {
        return bookType?.name
    }

    @TypeConverter
    fun fromCategoryType(value: String?): Category.CategoryType? {
        return value?.let { Category.CategoryType.valueOf(it) }
    }

    @TypeConverter
    fun categoryTypeToString(categoryType: Category.CategoryType?): String? {
        return categoryType?.name
    }
}