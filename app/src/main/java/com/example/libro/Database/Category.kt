package com.example.libro.Database
import androidx.room.*
import java.util.*

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "category_id")
    val categoryId: Long = 0,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "category_type")
    val categoryType: CategoryType,

    @ColumnInfo(name = "icon")
    val icon: ByteArray? = null,

    @ColumnInfo(name = "color")
    val color: String,

    @ColumnInfo(name = "display_order")
    val displayOrder: Int,

    @ColumnInfo(name = "creation_date")
    val creationDate: Date,

    @ColumnInfo(name = "can_edit")
    val canEdit: Boolean = true,

    @ColumnInfo(name = "can_delete")
    val canDelete: Boolean = true
) {
    enum class CategoryType {
        SYSTEM, USER
    }
}