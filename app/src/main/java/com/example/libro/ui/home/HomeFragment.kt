package com.example.libro.ui.home

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.libro.ui.note.AddNotesActivity
import com.example.libro.Database.AppDatabase
import com.example.libro.Database.Book
import com.example.libro.Database.ReadingSession
import com.example.libro.R
import com.example.libro.ui.Adapters.StatisticsAdapter
import com.example.libro.ui.Adapters.StatisticItem
import com.example.libro.Decoration.SpacingItemDecoration
import com.example.libro.ui.library.AddBookActivity
import com.example.libro.ui.timer.TimerActivity
import com.example.libro.ui.Views.ReadingCalendarView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment() {

    @Inject
    lateinit var database: AppDatabase

    private lateinit var statisticsRecyclerView: RecyclerView
    private lateinit var fireStreakTextView: TextView
    private lateinit var fireIcon: ImageView
    private lateinit var booksHorizontalScrollView: HorizontalScrollView
    private lateinit var booksContainer: LinearLayout
    private lateinit var readingCalendarView: ReadingCalendarView
    private lateinit var purchasedCountTextView: TextView
    private lateinit var lentCountTextView: TextView
    private lateinit var purchasedCard: CardView
    private lateinit var lentCard: CardView

    private lateinit var statisticsAdapter: StatisticsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        statisticsRecyclerView = view.findViewById(R.id.statisticsRecyclerView)
        fireStreakTextView = view.findViewById(R.id.fireStreakTextView)
        fireIcon = view.findViewById(R.id.fireIcon)
        booksHorizontalScrollView = view.findViewById(R.id.booksHorizontalScrollView)
        booksContainer = view.findViewById(R.id.booksContainer)
        readingCalendarView = view.findViewById(R.id.readingCalendarView)
        purchasedCountTextView = view.findViewById(R.id.purchasedCountTextView)
        lentCountTextView = view.findViewById(R.id.lentCountTextView)
        purchasedCard = view.findViewById(R.id.purchasedCard)
        lentCard = view.findViewById(R.id.lentCard)

        setupAdapters()
        loadData()
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_ADD_NOTE && resultCode == Activity.RESULT_OK) {
            loadData()
        }

        if (requestCode == REQUEST_EDIT_BOOK && resultCode == Activity.RESULT_OK) {
            loadData()
        }

        if (requestCode == REQUEST_READING_TIMER && resultCode == Activity.RESULT_OK) {
            loadData()
        }

        if (requestCode == REQUEST_SAVE_SESSION && resultCode == Activity.RESULT_OK) {
            loadData()
        }
    }

    private fun setupAdapters() {
        statisticsAdapter = StatisticsAdapter(emptyList())
        statisticsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = statisticsAdapter
            addItemDecoration(SpacingItemDecoration(16, requireContext()))
        }
    }

    private fun loadData() {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val currentlyReadingBooks = database.bookDao().getAllBooks().filter {
                    !it.isRead && it.currentPage > 0
                }

                val readingSessions = database.readingSessionDao().getAllSessions()

                val totalBooks = database.bookDao().getAllBooks()
                val purchasedBooks = totalBooks.count { it.isPurchased }
                val lentBooks = totalBooks.count { it.isLent }

                val fireStreak = calculateFireStreak(readingSessions)

                val statistics = calculateStatistics(readingSessions)

                withContext(Dispatchers.Main) {
                    updateFireStreakUI(fireStreak)
                    updateStatisticsUI(statistics)
                    updateBookCountsUI(purchasedBooks, lentBooks)

                    readingCalendarView.setReadingDates(getReadingDates(readingSessions))

                    updateBooksScrollView(currentlyReadingBooks)
                }
            }
        }
    }

    private fun updateStatisticsUI(statistics: List<StatisticItem>) {
        statisticsAdapter.updateStatistics(statistics)
    }

    private fun updateBookCountsUI(purchased: Int, lent: Int) {
        purchasedCountTextView.text = "$purchased книг"
        lentCountTextView.text = "$lent книг"
    }

    private fun updateBooksScrollView(books: List<Book>) {
        booksContainer.removeAllViews()

        if (books.isEmpty()) {
            val emptyView = TextView(requireContext()).apply {
                text = "Нет книг, которые читаются сейчас"
                textSize = 16f
                setTextColor(resources.getColor(R.color.text_secondary))
                setPadding(0, 32, 0, 32)
            }
            booksContainer.addView(emptyView)
            return
        }

        books.forEach { book ->
            val bookCard = layoutInflater.inflate(R.layout.item_currently_reading_book, null)

            val titleTextView: TextView = bookCard.findViewById(R.id.bookTitleTextView)
            val authorTextView: TextView = bookCard.findViewById(R.id.bookAuthorTextView)
            val startDateTextView: TextView = bookCard.findViewById(R.id.startDateTextView)
            val notesCountTextView: TextView = bookCard.findViewById(R.id.notesCountTextView)
            val progressPercentageTextView: TextView =
                bookCard.findViewById(R.id.progressPercentageTextView)
            val pagesReadTextView: TextView = bookCard.findViewById(R.id.pagesReadTextView)
            val readingTimeTextView: TextView = bookCard.findViewById(R.id.readingTimeTextView)
            val timeLeftTextView: TextView = bookCard.findViewById(R.id.timeLeftTextView)
            val progressBar: ProgressBar = bookCard.findViewById(R.id.readingProgressBar)
            val noteIcon: ImageView = bookCard.findViewById(R.id.noteIcon)
            val timerIcon: ImageView = bookCard.findViewById(R.id.timerIcon)
            val coverImageView: ImageView? = bookCard.findViewById(R.id.bookCoverImageView)

            titleTextView.text = book.title
            authorTextView.text = book.author ?: "Автор не указан"

            if (coverImageView != null) {
                if (!book.coverImage.isNullOrEmpty()) {
                    Glide.with(requireContext())
                        .load(book.coverImage)
                        .placeholder(R.drawable.plug)
                        .error(R.drawable.plug)
                        .into(coverImageView)
                } else {
                    coverImageView.setImageResource(R.drawable.plug)
                }
            }

            val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            startDateTextView.text =
                "Начало: ${book.startDate?.let { dateFormat.format(it) } ?: "Не указано"}"

            lifecycleScope.launch(Dispatchers.IO) {
                val notesCount = database.noteDao().getNotesByBookId(book.bookId).size
                withContext(Dispatchers.Main) {
                    notesCountTextView.text = "Заметки: $notesCount"
                }
            }

            val progressPercentage = if (book.pageCount > 0) {
                (book.currentPage.toFloat() / book.pageCount * 100).toInt()
            } else {
                0
            }

            progressPercentageTextView.text = "$progressPercentage%"
            pagesReadTextView.text = "${book.currentPage}/${book.pageCount} стр."

            progressBar.max = book.pageCount
            progressBar.progress = book.currentPage

            lifecycleScope.launch(Dispatchers.IO) {
                val totalReadingTimeForBook = database.readingSessionDao()
                    .getTotalReadingTimeForBook(book.bookId) ?: 0L

                withContext(Dispatchers.Main) {
                    val formattedTime = if (totalReadingTimeForBook > 0) {
                        formatReadingTime(totalReadingTimeForBook * 1000)
                    } else {
                        "0ч 0м"
                    }
                    readingTimeTextView.text = "Чтения: $formattedTime"
                }
            }

            val estimatedTimeLeft = calculateEstimatedTimeLeft(book)
            timeLeftTextView.text = "Осталось: $estimatedTimeLeft"

            noteIcon.setOnClickListener {
                navigateToBookNotes(book.bookId)
            }

            timerIcon.setOnClickListener {
                navigateToReadingTimer(book.bookId)
            }

            bookCard.setOnClickListener {
                navigateToBookDetails(book.bookId)
            }

            val layoutParams = LinearLayout.LayoutParams(
                resources.getDimensionPixelSize(R.dimen.book_card_width),
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.marginEnd = resources.getDimensionPixelSize(R.dimen.card_margin)
            bookCard.layoutParams = layoutParams

            booksContainer.addView(bookCard)
        }
    }

    private fun calculateFireStreak(readingSessions: List<ReadingSession>): Int {
        val calendar = Calendar.getInstance()
        val datesWithReading = mutableSetOf<String>()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        readingSessions.forEach { session ->
            session.startTime?.let {
                datesWithReading.add(dateFormat.format(it))
            }
        }

        var streak = 0
        calendar.time = Date()

        while (true) {
            val dateStr = dateFormat.format(calendar.time)
            if (datesWithReading.contains(dateStr)) {
                streak++
                calendar.add(Calendar.DAY_OF_YEAR, -1)
            } else {
                break
            }
        }

        return streak
    }

    private fun calculateStatistics(readingSessions: List<ReadingSession>): List<StatisticItem> {
        val statistics = mutableListOf<StatisticItem>()

        if (readingSessions.isEmpty()) {
            statistics.add(StatisticItem("Общее время", "0 ч", "За всё время"))
            statistics.add(StatisticItem("Сегодня", "0 мин", "Время чтения"))
            statistics.add(StatisticItem("Среднее", "0 мин/день", "За 7 дней"))
            return statistics
        }

        val totalTimeSeconds = readingSessions.sumOf { it.duration }
        val totalHours = totalTimeSeconds / 3600
        val remainingMinutes = (totalTimeSeconds % 3600) / 60
        statistics.add(StatisticItem("Общее время", "${totalHours}ч ${remainingMinutes}м", "За всё время"))

        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val todaySessions = readingSessions.filter { session ->
            session.startTime?.after(today.time) ?: false
        }

        val todayTimeSeconds = todaySessions.sumOf { it.duration }
        val todayMinutes = todayTimeSeconds / 60
        val todayHours = todayTimeSeconds / 3600
        val todayRemainingMinutes = (todayTimeSeconds % 3600) / 60

        val todayDisplayTime = if (todayHours > 0) {
            "${todayHours}ч ${todayRemainingMinutes}м"
        } else {
            "${todayMinutes}м"
        }

        statistics.add(StatisticItem("Сегодня", todayDisplayTime, "Время чтения"))

        val weekAgo = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -7)
        }

        val weekSessions = readingSessions.filter { session ->
            session.startTime?.after(weekAgo.time) ?: false
        }

        val weekTimeSeconds = weekSessions.sumOf { it.duration }
        val averagePerDaySeconds = if (weekSessions.isNotEmpty()) weekTimeSeconds / 7 else 0L

        val averageMinutes = averagePerDaySeconds / 60
        val averageHours = averagePerDaySeconds / 3600
        val averageRemainingMinutes = (averagePerDaySeconds % 3600) / 60

        val averageDisplayTime = if (averageHours > 0) {
            "${averageHours}ч ${averageRemainingMinutes}м"
        } else {
            "${averageMinutes}м"
        }

        statistics.add(StatisticItem("Среднее", averageDisplayTime, "За 7 дней"))

        return statistics
    }

    private fun updateFireStreakUI(streak: Int) {
        if (streak > 0) {
            fireIcon.setImageResource(R.drawable.fire)
            fireStreakTextView.text = "$streak дней подряд!"
            fireStreakTextView.setTextColor(requireContext().getColor(R.color.success))
        } else {
            fireIcon.setImageResource(R.drawable.fireempty)
            fireStreakTextView.text = "Начните читать сегодня!"
            fireStreakTextView.setTextColor(requireContext().getColor(R.color.warning))
        }
    }

    private fun getReadingDates(readingSessions: List<ReadingSession>): List<Date> {
        val dateSet = mutableSetOf<Date>()
        val calendar = Calendar.getInstance()

        readingSessions.forEach { session ->
            session.startTime?.let {
                calendar.time = it
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                dateSet.add(calendar.time)
            }
        }

        return dateSet.toList()
    }

    private fun formatReadingTime(milliseconds: Long): String {
        if (milliseconds <= 0) return "0ч 0м"

        val totalSeconds = milliseconds / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60

        return if (hours > 0) {
            "${hours}ч ${minutes}м"
        } else {
            "${minutes}м"
        }
    }

    private fun calculateEstimatedTimeLeft(book: Book): String {
        val remainingPages = book.pageCount - book.currentPage
        if (remainingPages <= 0) return "Завершено"

        val averageSpeed = 1.0
        val minutesLeft = remainingPages / averageSpeed

        return if (minutesLeft > 60) {
            "${(minutesLeft / 60).toInt()}ч ${(minutesLeft % 60).toInt()}м"
        } else {
            "${minutesLeft.toInt()}м"
        }
    }

    private fun navigateToBookDetails(bookId: Long) {
        val intent = Intent(requireContext(), AddBookActivity::class.java).apply {
            putExtra("book_id", bookId)
        }
        startActivityForResult(intent, REQUEST_EDIT_BOOK)
    }

    private fun navigateToBookNotes(bookId: Long) {
        lifecycleScope.launch(Dispatchers.IO) {
            val book = database.bookDao().getBookById(bookId)
            withContext(Dispatchers.Main) {
                val intent = Intent(requireContext(), AddNotesActivity::class.java).apply {
                    putExtra(AddNotesActivity.EXTRA_BOOK_ID, bookId)
                    if (book != null) {
                        putExtra(AddNotesActivity.EXTRA_MAX_PAGE, book.pageCount)
                    }
                }
                startActivityForResult(intent, REQUEST_ADD_NOTE)
            }
        }
    }

    private fun navigateToReadingTimer(bookId: Long) {
        val intent = Intent(requireContext(), TimerActivity::class.java).apply {
            putExtra(TimerActivity.EXTRA_BOOK_ID, bookId)
        }
        startActivityForResult(intent, REQUEST_READING_TIMER)
    }

    companion object {
        private const val REQUEST_ADD_NOTE = 1001
        private const val REQUEST_EDIT_BOOK = 1002
        private const val REQUEST_READING_TIMER = 1003
        private const val REQUEST_SAVE_SESSION = 1004
    }
}