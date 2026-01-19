package com.example.libro.Database

import androidx.room.*

@Entity(tableName = "genres")
data class Genre(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "genre_id")
    val genreId: Long = 0,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "description")
    val description: String? = null
)