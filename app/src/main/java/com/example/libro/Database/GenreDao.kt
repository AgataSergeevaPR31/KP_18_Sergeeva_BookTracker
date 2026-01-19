package com.example.libro.Database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GenreDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGenre(genre: Genre): Long

    @Query("SELECT * FROM genres")
    suspend fun getAllGenres(): List<Genre>

    @Query("SELECT * FROM genres WHERE genre_id = :genreId")
    suspend fun getGenreById(genreId: Long): Genre?

    @Query("SELECT * FROM genres WHERE name = :name")
    suspend fun getGenreByName(name: String): Genre?
}

@Dao
interface BookGenreDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookGenre(bookGenre: BookGenre)

    @Query("DELETE FROM book_genres WHERE book_id = :bookId AND genre_id = :genreId")
    suspend fun removeGenreFromBook(bookId: Long, genreId: Long)

    @Query("SELECT * FROM book_genres")
    suspend fun getAllBookGenres(): List<BookGenre>

    @Query("SELECT * FROM book_genres WHERE book_id = :bookId")
    suspend fun getGenresForBook(bookId: Long): List<BookGenre>

    @Query("DELETE FROM book_genres WHERE book_id = :bookId")
    suspend fun deleteGenresForBook(bookId: Long)
}


@Dao
interface AchievementDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievement(achievement: Achievement): Long

    @Query("SELECT * FROM achievements ORDER BY display_order")
    suspend fun getAllAchievements(): List<Achievement>

    @Query("SELECT * FROM achievements ORDER BY display_order ASC")
    fun getAllAchievementsFlow(): Flow<List<Achievement>>
}

@Dao
interface UserAchievementDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateUserAchievement(userAchievement: UserAchievement)

    @Query("SELECT * FROM user_achievements")
    suspend fun getAllUserAchievements(): List<UserAchievement>

    @Query("SELECT * FROM user_achievements WHERE achievement_id = :achievementId")
    suspend fun getUserAchievement(achievementId: Long): UserAchievement?

    @Query("SELECT * FROM user_achievements WHERE is_achieved = 1")
    suspend fun getAchievedUserAchievements(): List<UserAchievement>

    @Query("SELECT COUNT(*) FROM user_achievements WHERE is_achieved = 1")
    suspend fun getAchievedAchievementsCount(): Int

    @Query("SELECT * FROM user_achievements")
    fun getAllUserAchievementsFlow(): Flow<List<UserAchievement>>
}