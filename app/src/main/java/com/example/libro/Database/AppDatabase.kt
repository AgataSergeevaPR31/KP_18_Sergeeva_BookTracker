package com.example.libro.Database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        Book::class,
        LentBook::class,
        PurchasedBook::class,
        Note::class,
        ReadingSession::class,
        Category::class,
        BookCategory::class,
        Genre::class,
        BookGenre::class,
        Achievement::class,
        UserAchievement::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao
    abstract fun lentBookDao(): LentBookDao
    abstract fun purchasedBookDao(): PurchasedBookDao
    abstract fun noteDao(): NoteDao
    abstract fun readingSessionDao(): ReadingSessionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun bookCategoryDao(): BookCategoryDao
    abstract fun genreDao(): GenreDao
    abstract fun bookGenreDao(): BookGenreDao
    abstract fun achievementDao(): AchievementDao
    abstract fun userAchievementDao(): UserAchievementDao


    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "reading_tracker_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}