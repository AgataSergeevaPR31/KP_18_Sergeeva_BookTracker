package com.example.libro.Database

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

object DatabaseInitializer {

    fun initializeDatabase(context: Context) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val database = AppDatabase.getDatabase(context)
                initializeAchievements(database)
                initializeGenres(database)
                initializeCategories(database)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private suspend fun initializeAchievements(database: AppDatabase) {
        val achievementDao = database.achievementDao()
        val existingAchievements = achievementDao.getAllAchievements()
        if (existingAchievements.isNotEmpty()) {
            return
        }

        val achievements = listOf(
            Achievement(
                name = "Первый шаг",
                description = "Прочитать первую страницу",
                requiredValue = 1.0,
                displayOrder = 1
            ),
            Achievement(
                name = "Втягиваюсь",
                description = "Прочитать в сумме 100 страниц",
                requiredValue = 100.0,
                displayOrder = 2
            ),
            Achievement(
                name = "На старте",
                description = "Завершить чтение первой книги",
                requiredValue = 1.0,
                displayOrder = 3
            ),
            Achievement(
                name = "Непрерывность",
                description = "Читать 3 дня подряд",
                requiredValue = 3.0,
                displayOrder = 4
            ),
            Achievement(
                name = "Погружение",
                description = "Прочитать более 50 страниц за один сеанс",
                requiredValue = 50.0,
                displayOrder = 5
            ),
            Achievement(
                name = "Книгочей-новичок",
                description = "Прочитать в сумме 1000 страниц",
                requiredValue = 1000.0,
                displayOrder = 6
            ),
            Achievement(
                name = "Мастер страниц",
                description = "Прочитать в сумме 5000 страниц",
                requiredValue = 5000.0,
                displayOrder = 7
            ),
            Achievement(
                name = "Литературный гигант",
                description = "Прочитать в сумму 10000 страниц",
                requiredValue = 10000.0,
                displayOrder = 8
            ),
            Achievement(
                name = "Библиофил",
                description = "Завершить чтение 5 книг",
                requiredValue = 5.0,
                displayOrder = 9
            ),
            Achievement(
                name = "Книжный червь",
                description = "Завершить чтение 10 книг",
                requiredValue = 10.0,
                displayOrder = 10
            ),
            Achievement(
                name = "Легенда библиотеки",
                description = "Завершить чтение 50 книг",
                requiredValue = 50.0,
                displayOrder = 11
            ),
            Achievement(
                name = "Ночная сова",
                description = "Первое чтение после 22:00",
                requiredValue = 1.0,
                displayOrder = 12
            ),
            Achievement(
                name = "Глубокие мысли",
                description = "Прочитать 10 раз после 22:00",
                requiredValue = 10.0,
                displayOrder = 13
            ),
            Achievement(
                name = "Полуночник",
                description = "Прочитать после 00:00",
                requiredValue = 1.0,
                displayOrder = 14
            ),
            Achievement(
                name = "Ранняя пташка",
                description = "Первое чтение между 4:00 и 8:00 утра",
                requiredValue = 1.0,
                displayOrder = 15
            ),
            Achievement(
                name = "Утренний ритуал",
                description = "Прочитать 10 раз между 4:00 и 8:00 утра",
                requiredValue = 10.0,
                displayOrder = 16
            ),
            Achievement(
                name = "Дневной марафон",
                description = "Читать в три разных времени суток (утро, день, вечер) за одни календарные сутки",
                requiredValue = 3.0,
                displayOrder = 17
            ),
            Achievement(
                name = "Марафонец",
                description = "Читать 7 дней подряд без перерыва",
                requiredValue = 7.0,
                displayOrder = 18
            ),
            Achievement(
                name = "Железная воля",
                description = "Читать 30 дней подряд",
                requiredValue = 30.0,
                displayOrder = 19
            ),
            Achievement(
                name = "Спринтер",
                description = "Прочитать книгу менее чем за 24 часа с момента начала",
                requiredValue = 1.0,
                displayOrder = 20
            ),
            Achievement(
                name = "Стайер",
                description = "Прочитать книгу объёмом более 500 страниц",
                requiredValue = 500.0,
                displayOrder = 21
            ),
            Achievement(
                name = "Исполин",
                description = "Прочитать книгу объёмом более 1000 страниц",
                requiredValue = 1000.0,
                displayOrder = 22
            ),
            Achievement(
                name = "Юбилейный",
                description = "Прочитать ровно 100-ю книгу",
                requiredValue = 100.0,
                displayOrder = 23
            ),
            Achievement(
                name = "Символично",
                description = "Прочитать 2222 страницы в сумме",
                requiredValue = 2222.0,
                displayOrder = 24
            ),
            Achievement(
                name = "Круглая дата",
                description = "Прочитать ровно 100-ю книгу в приложении",
                requiredValue = 100.0,
                displayOrder = 25
            ),
            Achievement(
                name = "Золотой фонд",
                description = "Провести за чтением в сумме 100 часов",
                requiredValue = 100.0,
                displayOrder = 26
            ),
            Achievement(
                name = "Время — книги",
                description = "Провести за чтением в сумме 500 часов",
                requiredValue = 500.0,
                displayOrder = 27
            ),
            Achievement(
                name = "Собственный ритм",
                description = "Установить личный рекорд по страницам, прочитанным за день",
                requiredValue = 1.0,
                displayOrder = 28
            ),
            Achievement(
                name = "Неудержимый",
                description = "Побить свой личный рекорд по страницам за день",
                requiredValue = 1.0,
                displayOrder = 29
            )
        )

        achievements.forEach { achievement ->
            achievementDao.insertAchievement(achievement)
        }
    }

    private suspend fun initializeGenres(database: AppDatabase) {
        val genreDao = database.genreDao()
        val existingGenres = genreDao.getAllGenres()
        if (existingGenres.isNotEmpty()) {
            return
        }

        val genres = listOf(
            Genre(name = "Роман", description = "Художественная проза"),
            Genre(name = "Фантастика", description = "Научная фантастика и фэнтези"),
            Genre(name = "Детектив", description = "Детективные произведения"),
            Genre(name = "Научная литература", description = "Научные и образовательные книги"),
            Genre(name = "Биография", description = "Биографии известных людей"),
            Genre(name = "Поэзия", description = "Стихи и поэмы"),
            Genre(name = "Приключения", description = "Приключенческая литература"),
            Genre(name = "Исторический", description = "Исторические произведения"),
            Genre(name = "Психология", description = "Книги по психологии"),
            Genre(name = "Саморазвитие", description = "Книги по саморазвитию")
        )

        genres.forEach { genre ->
            genreDao.insertGenre(genre)
        }
    }

    private suspend fun initializeCategories(database: AppDatabase) {
        val categoryDao = database.categoryDao()
        val existingCategories = categoryDao.getAllCategories()
        if (existingCategories.isNotEmpty()) {
            return
        }

        val categories = listOf(
            Category(
                name = "Прочитано",
                categoryType = Category.CategoryType.SYSTEM,
                color = "#4CAF50",
                displayOrder = 1,
                creationDate = Date(),
                canEdit = false,
                canDelete = false
            ),
            Category(
                name = "Читаю сейчас",
                categoryType = Category.CategoryType.SYSTEM,
                color = "#2196F3",
                displayOrder = 2,
                creationDate = Date(),
                canEdit = false,
                canDelete = false
            ),
            Category(
                name = "Хочу прочитать",
                categoryType = Category.CategoryType.SYSTEM,
                color = "#FF9800",
                displayOrder = 3,
                creationDate = Date(),
                canEdit = false,
                canDelete = false
            )
        )

        categories.forEach { category ->
            categoryDao.insertCategory(category)
        }
    }
}