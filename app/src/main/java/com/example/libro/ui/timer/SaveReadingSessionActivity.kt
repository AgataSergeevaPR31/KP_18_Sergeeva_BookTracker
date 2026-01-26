package com.example.libro.ui.timer

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.libro.Database.*
import com.example.libro.R
import com.example.libro.databinding.ActivitySaveReadingSessionBinding
import com.example.libro.ui.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.math.max

@AndroidEntryPoint
class SaveReadingSessionActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySaveReadingSessionBinding

    @Inject lateinit var bookDao: BookDao
    @Inject lateinit var readingSessionDao: ReadingSessionDao
    @Inject lateinit var categoryDao: CategoryDao
    @Inject lateinit var bookCategoryDao: BookCategoryDao
    @Inject lateinit var achievementDao: AchievementDao
    @Inject lateinit var userAchievementDao: UserAchievementDao

    private lateinit var currentBook: Book
    private var readingTimeMillis: Long = 0
    private var startPage: Int = 0
    private var endPage: Int = 0
    private var totalReadingTimeBefore: Long = 0
    private var dialogShown = false
    private var isDataLoaded = false
    private var isFirstReadingToday: Boolean = false
    private var currentStreak: Int = 0
    private var newAchievements: List<Achievement> = emptyList()

    companion object {
        const val EXTRA_BOOK_ID = "BOOK_ID"
        const val EXTRA_READING_TIME = "READING_TIME"
        const val EXTRA_IS_FIRST_READING_TODAY = "IS_FIRST_READING_TODAY"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySaveReadingSessionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        getIntentData()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = "Сохранение сеанса чтения"
    }

    private fun getIntentData() {
        val bookId = intent.getLongExtra(EXTRA_BOOK_ID, -1L)
        readingTimeMillis = intent.getLongExtra(EXTRA_READING_TIME, 0L)
        isFirstReadingToday = intent.getBooleanExtra(EXTRA_IS_FIRST_READING_TODAY, false)

        if (bookId == -1L) {
            finish()
            return
        }

        lifecycleScope.launch {
            try {
                currentBook = bookDao.getBookById(bookId) ?: run {
                    finish()
                    return@launch
                }

                totalReadingTimeBefore = readingSessionDao.getTotalReadingTimeForBook(bookId) ?: 0L
                startPage = currentBook.currentPage
                endPage = startPage

                currentStreak = calculateCurrentStreak()

                setupUI()
                updateUI()
                setupClickListeners()

                isDataLoaded = true

                if (!dialogShown && !isFinishing) {
                    dialogShown = true
                    binding.root.postDelayed({
                        if (!isFinishing && isDataLoaded) {
                            showCurrentPageDialog()
                        }
                    }, 300)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                finish()
            }
        }
    }

    private suspend fun calculateCurrentStreak(): Int {
        val readingSessions = readingSessionDao.getAllSessions()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val datesWithReading = mutableSetOf<String>()

        readingSessions.forEach { session ->
            session.startTime?.let {
                datesWithReading.add(dateFormat.format(it))
            }
        }

        if (datesWithReading.isEmpty()) {
            return if (isFirstReadingToday) 1 else 0
        }

        val sortedDates = datesWithReading.sorted()

        var streak = -1
        val calendar = Calendar.getInstance()

        calendar.add(Calendar.DAY_OF_YEAR, -1)
        var currentDateStr = dateFormat.format(calendar.time)

        while (true) {
            if (sortedDates.contains(currentDateStr)) {
                streak++
                calendar.add(Calendar.DAY_OF_YEAR, -1)
                currentDateStr = dateFormat.format(calendar.time)
            } else {
                break
            }
        }

        if (isFirstReadingToday) {
            val todayStr = dateFormat.format(Date())
            val yesterdayCalendar = Calendar.getInstance()
            yesterdayCalendar.add(Calendar.DAY_OF_YEAR, -1)
            val yesterdayStr = dateFormat.format(yesterdayCalendar.time)

            if (sortedDates.contains(yesterdayStr)) {
                streak += 1
            } else {
                streak = 1
            }
        }

        return streak
    }

    private fun setupUI() {
        val currentDate = Date()
        val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        binding.tvReadingDate.text = "Дата чтения: ${dateFormat.format(currentDate)}"
        updateReadingTimeDisplay()
    }

    @SuppressLint("StringFormatMatches")
    private fun updateUI() {
        if (!isDataLoaded) return
        binding.tvPagesInfo.text = getString(
            R.string.pages_info_format,
            currentBook.currentPage,
            currentBook.pageCount
        )
    }

    private fun showCurrentPageDialog() {
        if (!isDataLoaded || isFinishing) return

        val dialogView = layoutInflater.inflate(R.layout.dialog_current_page, null)
        val numberPicker = dialogView.findViewById<android.widget.NumberPicker>(R.id.numberPicker)
        val tvTotalPages = dialogView.findViewById<android.widget.TextView>(R.id.tvTotalPages)

        numberPicker.minValue = 0
        numberPicker.maxValue = currentBook.pageCount
        numberPicker.value = currentBook.currentPage
        tvTotalPages.text = "из ${currentBook.pageCount} страниц"

        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Текущая страница")
            .setMessage("На какой странице вы остановились?")
            .setView(dialogView)
            .setPositiveButton("OK") { dialogInterface, _ ->
                val selectedPage = numberPicker.value
                endPage = selectedPage
                currentBook = currentBook.copy(currentPage = selectedPage)

                binding.tvPagesInfo.text = getString(
                    R.string.pages_info_format,
                    selectedPage,
                    currentBook.pageCount
                )

                lifecycleScope.launch {
                    bookDao.updateBook(currentBook)
                }

                dialogInterface.dismiss()
            }
            .setNegativeButton("Отмена") { dialogInterface, _ ->
                endPage = currentBook.currentPage
                dialogInterface.dismiss()
            }
            .setCancelable(false)
            .create()

        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
    }

    private fun setupClickListeners() {
        binding.ivEditTime.setOnClickListener {
            if (isDataLoaded) showTimePickerDialog()
        }

        binding.cardPages.setOnClickListener {
            if (isDataLoaded) showCurrentPageDialog()
        }

        binding.ivEditPages.setOnClickListener {
            if (isDataLoaded) showCurrentPageDialog()
        }

        binding.btnSave.setOnClickListener {
            if (isDataLoaded) saveReadingSessionAndNavigate()
        }

        binding.toolbar.setNavigationOnClickListener {
            if (isDataLoaded) saveReadingSessionAndFinish()
            else finish()
        }
    }

    private fun showTimePickerDialog() {
        val hours = (readingTimeMillis / (1000 * 60 * 60)).toInt()
        val minutes = ((readingTimeMillis % (1000 * 60 * 60)) / (1000 * 60)).toInt()
        val seconds = ((readingTimeMillis % (1000 * 60)) / 1000).toInt()

        showSimpleTimePickerDialog(hours, minutes, seconds)
    }

    private fun showSimpleTimePickerDialog(initialHours: Int, initialMinutes: Int, initialSeconds: Int) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_time_picker, null)
        val hourPicker = dialogView.findViewById<android.widget.NumberPicker>(R.id.numberPickerHour)
        val minutePicker = dialogView.findViewById<android.widget.NumberPicker>(R.id.numberPickerMinute)
        val secondPicker = dialogView.findViewById<android.widget.NumberPicker>(R.id.numberPickerSecond)

        hourPicker.minValue = 0
        hourPicker.maxValue = 23
        hourPicker.value = initialHours

        minutePicker.minValue = 0
        minutePicker.maxValue = 59
        minutePicker.value = initialMinutes

        secondPicker.minValue = 0
        secondPicker.maxValue = 59
        secondPicker.value = initialSeconds

        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Редактировать время чтения")
            .setView(dialogView)
            .setPositiveButton("Сохранить") { dialogInterface, _ ->
                val h = hourPicker.value
                val m = minutePicker.value
                val s = secondPicker.value

                readingTimeMillis = (h * 3600L + m * 60L + s) * 1000L
                updateReadingTimeDisplay()
                dialogInterface.dismiss()
            }
            .setNegativeButton("Отмена") { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            .setCancelable(false)
            .create()

        dialog.show()
    }

    private fun updateReadingTimeDisplay() {
        val hours = readingTimeMillis / (1000 * 60 * 60)
        val minutes = (readingTimeMillis % (1000 * 60 * 60)) / (1000 * 60)
        val seconds = (readingTimeMillis % (1000 * 60)) / 1000

        binding.tvReadingTime.text = String.format("%02d ч %02d мин %02d сек", hours, minutes, seconds)
    }

    private fun saveReadingSessionAndNavigate() {
        if (!isDataLoaded) return

        lifecycleScope.launch {
            try {
                saveReadingSessionToDatabase()

                newAchievements = checkForNewAchievements()

                updateReadingPreferences()

                if (newAchievements.isNotEmpty()) {
                    showAchievementDialog(newAchievements)
                } else {
                    if (isFirstReadingToday) {
                        navigateToFireActivity()
                    } else {
                        navigateToHome()
                    }
                    finish()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@SaveReadingSessionActivity, "Ошибка сохранения", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showAchievementDialog(achievements: List<Achievement>) {
        if (achievements.isEmpty()) return

        val title = if (achievements.size == 1) {
            "🎉 Новое достижение!"
        } else {
            "🎉 Новые достижения!"
        }

        val message = if (achievements.size == 1) {
            "Поздравляем! Вы получили достижение:\n\"${achievements[0].name}\"\n\n${achievements[0].description}"
        } else {
            val achievementsList = achievements.joinToString("\n") { "• ${it.name}: ${it.description}" }
            "Поздравляем! Вы получили достижения:\n\n$achievementsList"
        }

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Круто!") { dialog, _ ->
                dialog.dismiss()
                lifecycleScope.launch {
                    if (isFirstReadingToday) {
                        navigateToFireActivity()
                    } else {
                        navigateToHome()
                    }
                    finish()
                }
            }
            .setCancelable(false)
            .show()
    }

    private fun navigateToFireActivity() {
        val intent = Intent(this, FireActivity::class.java).apply {
            putExtra(FireActivity.EXTRA_STREAK_COUNT, currentStreak + 1)
            putExtra(FireActivity.EXTRA_NEW_ACHIEVEMENTS, newAchievements.isNotEmpty())
        }
        startActivity(intent)
    }

    private fun navigateToHome() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("FRAGMENT_TO_LOAD", "home")

            if (newAchievements.isNotEmpty()) {
                val prefs = getSharedPreferences("achievements", MODE_PRIVATE)
                val editor = prefs.edit()
                editor.putBoolean("has_new_achievements", true)
                val achievementNames = newAchievements.map { it.name }.joinToString("|")
                editor.putString("achievement_names", achievementNames)
                editor.apply()
            }
        }
        startActivity(intent)
    }

    private fun updateReadingPreferences() {
        val prefs = getSharedPreferences("reading_prefs", MODE_PRIVATE)
        val editor = prefs.edit()

        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        editor.putString("last_reading_date", today)

        val sessionsToday = prefs.getInt("sessions_today", 0)
        editor.putInt("sessions_today", sessionsToday + 1)

        editor.apply()
    }

    private suspend fun saveReadingSessionToDatabase() {
        if (endPage == 0) {
            endPage = currentBook.currentPage
        }

        val session = ReadingSession(
            bookId = currentBook.bookId,
            startTime = Date(System.currentTimeMillis() - readingTimeMillis),
            endTime = Date(),
            startPage = startPage,
            endPage = endPage,
            duration = readingTimeMillis / 1000
        )

        readingSessionDao.insertReadingSession(session)

        val pagesRead = max(0, endPage - startPage)
        val newTotalReadingTime = totalReadingTimeBefore + (readingTimeMillis / (1000 * 60))

        val isBookFinished = endPage >= currentBook.pageCount && !currentBook.isRead

        val updatedBook = if (isBookFinished) {
            currentBook.copy(
                isRead = true,
                endDate = Date(),
                totalReadingTime = newTotalReadingTime,
                currentPage = endPage
            ).apply {
                if (startDate == null && pagesRead > 0) {
                    startDate = Date()
                }
            }
        } else {
            currentBook.copy(
                totalReadingTime = newTotalReadingTime,
                currentPage = endPage
            ).apply {
                if (startDate == null && pagesRead > 0) {
                    startDate = Date()
                }
            }
        }

        bookDao.updateBook(updatedBook)

        if (isBookFinished) {
            addBookToReadCategory()
        }
    }

    private suspend fun addBookToReadCategory() {
        try {
            val readCategory = categoryDao.getCategoryByName("Прочитано")

            val category = if (readCategory == null) {
                createReadCategory()
            } else {
                readCategory
            }

            val existingCategories = bookCategoryDao.getCategoriesForBook(currentBook.bookId)
            val alreadyInCategory = existingCategories.any { it.categoryId == category.categoryId }

            if (!alreadyInCategory) {
                val bookCategory = BookCategory(
                    bookId = currentBook.bookId,
                    categoryId = category.categoryId,
                    addedDate = Date(),
                    orderInCategory = 0
                )
                bookCategoryDao.insertBookCategory(bookCategory)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun createReadCategory(): Category {
        val category = Category(
            name = "Прочитано",
            categoryType = Category.CategoryType.SYSTEM,
            color = "#4CAF50",
            displayOrder = 1,
            creationDate = Date(),
            canEdit = false,
            canDelete = false
        )
        val categoryId = categoryDao.insertCategory(category)
        return category.copy(categoryId = categoryId)
    }

    private suspend fun checkForNewAchievements(): List<Achievement> {
        val newAchievements = mutableListOf<Achievement>()

        try {
            val allAchievements = achievementDao.getAllAchievements()
            val userAchievements = userAchievementDao.getAllUserAchievements()
            val achievedIds = userAchievements.map { it.achievementId }.toSet()

            val totalReadingTimeInSeconds = readingSessionDao.getTotalReadingTime() ?: 0L
            val totalReadingTimeInHours = totalReadingTimeInSeconds / 3600.0
            val totalBooksRead = bookDao.getBooks().count { it.isRead }
            val totalPagesRead = readingSessionDao.getTotalPagesRead() ?: 0
            val currentStreak = calculateCurrentStreak()
            val pagesReadInSession = max(0, endPage - startPage)
            val isReadingAtNight = isNightTimeReading()
            val bookCompleted = endPage >= currentBook.pageCount && endPage > startPage

            for (achievement in allAchievements) {
                val isAlreadyAchieved = achievedIds.contains(achievement.achievementId)

                if (!isAlreadyAchieved) {
                    var shouldAward = false
                    var progressValue = 0.0

                    when (achievement.type) {
                        Achievement.AchievementType.PAGES_READ -> {
                            progressValue = totalPagesRead.toDouble()
                            shouldAward = totalPagesRead >= achievement.requiredValue.toInt()
                        }

                        Achievement.AchievementType.BOOKS_READ -> {
                            progressValue = totalBooksRead.toDouble()
                            shouldAward = totalBooksRead >= achievement.requiredValue.toInt()
                        }

                        Achievement.AchievementType.READING_STREAK -> {
                            progressValue = currentStreak.toDouble()
                            shouldAward = currentStreak >= achievement.requiredValue.toInt()
                        }

                        Achievement.AchievementType.NIGHT_READING -> {
                            shouldAward = isReadingAtNight
                            progressValue = if (shouldAward) 1.0 else 0.0
                        }

                        Achievement.AchievementType.LONG_BOOK -> {
                            shouldAward = bookCompleted && currentBook.pageCount >= achievement.requiredValue.toInt()
                            progressValue = if (shouldAward) currentBook.pageCount.toDouble() else 0.0
                        }

                        Achievement.AchievementType.READING_TIME -> {
                            progressValue = totalReadingTimeInHours
                            shouldAward = totalReadingTimeInHours >= achievement.requiredValue
                        }

                        Achievement.AchievementType.PAGES_PER_SESSION -> {
                            progressValue = pagesReadInSession.toDouble()
                            shouldAward = pagesReadInSession >= achievement.requiredValue.toInt()
                        }

                        Achievement.AchievementType.FIRST_STEPS -> {
                            shouldAward = pagesReadInSession > 0 && totalPagesRead == pagesReadInSession
                            progressValue = if (shouldAward) 1.0 else 0.0
                        }
                    }

                    if (shouldAward) {
                        try {
                            val userAchievement = UserAchievement(
                                achievementId = achievement.achievementId,
                                achievementDate = Date(),
                                currentProgress = progressValue,
                                isAchieved = true
                            )

                            userAchievementDao.insertOrUpdateUserAchievement(userAchievement)
                            newAchievements.add(achievement)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return newAchievements
    }

    private fun isNightTimeReading(): Boolean {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        return hour >= 22 || hour < 6
    }

    private fun saveReadingSessionAndFinish() {
        if (!isDataLoaded) {
            finish()
            return
        }

        lifecycleScope.launch {
            try {
                saveReadingSessionToDatabase()
                updateReadingPreferences()
                finish()
            } catch (e: Exception) {
                e.printStackTrace()
                finish()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                if (isDataLoaded) saveReadingSessionAndFinish() else finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        if (isDataLoaded) {
            saveReadingSessionAndFinish()
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isDataLoaded = false
        dialogShown = false
    }
}