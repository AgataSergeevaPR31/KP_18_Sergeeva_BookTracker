package com.example.libro.Database

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    fun provideBookDao(database: AppDatabase): BookDao {
        return database.bookDao()
    }

    @Provides
    fun provideCategoryDao(database: AppDatabase): CategoryDao {
        return database.categoryDao()
    }

    @Provides
    fun provideGenreDao(database: AppDatabase): GenreDao {
        return database.genreDao()
    }

    @Provides
    fun provideBookGenreDao(database: AppDatabase): BookGenreDao {
        return database.bookGenreDao()
    }

    @Provides
    fun provideBookCategoryDao(database: AppDatabase): BookCategoryDao {
        return database.bookCategoryDao()
    }

    @Provides
    fun provideAchievementDao(database: AppDatabase): AchievementDao {
        return database.achievementDao()
    }

    @Provides
    fun provideUserAchievementDao(database: AppDatabase): UserAchievementDao {
        return database.userAchievementDao()
    }

    @Provides
    fun provideLentBookDao(database: AppDatabase): LentBookDao {
        return database.lentBookDao()
    }

    @Provides
    fun providePurchasedBookDao(database: AppDatabase): PurchasedBookDao {
        return database.purchasedBookDao()
    }

    @Provides
    fun provideNoteDao(database: AppDatabase): NoteDao {
        return database.noteDao()
    }

    @Provides
    fun provideReadingSessionDao(database: AppDatabase): ReadingSessionDao {
        return database.readingSessionDao()
    }

}