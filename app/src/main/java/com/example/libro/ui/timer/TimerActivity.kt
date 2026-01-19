package com.example.libro.ui.timer

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.os.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.libro.Database.AppDatabase
import com.example.libro.Database.Book
import com.example.libro.Database.ReadingSession
import com.example.libro.R
import com.example.libro.ui.note.AddNotesActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class TimerActivity : AppCompatActivity() {

    private lateinit var database: AppDatabase
    private lateinit var timerTextView: TextView
    private lateinit var timerToggleButton: FloatingActionButton
    private lateinit var setCountdownTextView: TextView
    private lateinit var backButton: ImageButton
    private lateinit var finishButton: Button
    private lateinit var addNoteButton: ImageButton
    private lateinit var bookTitleTextView: TextView
    private lateinit var bookAuthorTextView: TextView
    private lateinit var pagesReadTextView: TextView
    private lateinit var notesCountTextView: TextView
    private lateinit var bookCoverImageView: ImageView

    private var bookId: Long = -1
    private var book: Book? = null

    private var isTimerRunning = false
    private var startTime = 0L
    private var elapsedTime = 0L
    private var countdownTime = 0L
    private var timerHandler = Handler(Looper.getMainLooper())
    private lateinit var timerRunnable: Runnable
    private var vibrator: Vibrator? = null

    companion object {
        const val EXTRA_BOOK_ID = "book_id"
        private const val COUNTDOWN_INTERVAL = 1000L
        private const val VIBRATE_PERMISSION_REQUEST_CODE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timer)

        database = AppDatabase.getDatabase(this)
        bookId = intent.getLongExtra(EXTRA_BOOK_ID, -1)

        TimerWorker.setAppForegroundState(true)

        initViews()
        setupClickListeners()
        loadBookData()
        setupTimer()
        checkVibratePermission()
    }

    override fun onResume() {
        super.onResume()
        TimerWorker.setAppForegroundState(true)
    }

    override fun onPause() {
        super.onPause()
        TimerWorker.setAppForegroundState(false)
    }

    override fun onDestroy() {
        super.onDestroy()
        timerHandler.removeCallbacks(timerRunnable)
        TimerWorker.setAppForegroundState(false)
    }

    private fun checkVibratePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.VIBRATE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.VIBRATE),
                    VIBRATE_PERMISSION_REQUEST_CODE
                )
            } else {
                setupVibrator()
            }
        } else {
            setupVibrator()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == VIBRATE_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupVibrator()
            }
        }
    }

    private fun setupVibrator() {
        vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
        if (vibrator != null) {
            val hasVibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                vibrator!!.hasVibrator()
            } else {
                vibrator!!.hasVibrator()
            }

            if (!hasVibrator) {
                vibrator = null
            }
        }
    }

    private fun initViews() {
        timerTextView = findViewById(R.id.timerTextView)
        timerToggleButton = findViewById(R.id.timerToggleButton)
        setCountdownTextView = findViewById(R.id.setCountdownTextView)
        backButton = findViewById(R.id.backButton)
        finishButton = findViewById(R.id.finishButton)
        addNoteButton = findViewById(R.id.addNoteButton)
        bookTitleTextView = findViewById(R.id.bookTitleTextView)
        bookAuthorTextView = findViewById(R.id.bookAuthorTextView)
        pagesReadTextView = findViewById(R.id.pagesReadTextView)
        notesCountTextView = findViewById(R.id.notesCountTextView)
        bookCoverImageView = findViewById(R.id.bookCoverImageView)
    }

    private fun setupClickListeners() {
        backButton.setOnClickListener { finish() }
        finishButton.setOnClickListener { finishTimer() }
        timerToggleButton.setOnClickListener { toggleTimer() }
        setCountdownTextView.setOnClickListener { showCountdownDialog() }
        addNoteButton.setOnClickListener {
            val intent = Intent(this, AddNotesActivity::class.java)
            intent.putExtra(AddNotesActivity.EXTRA_BOOK_ID, bookId)
            book?.let {
                intent.putExtra(AddNotesActivity.EXTRA_MAX_PAGE, it.pageCount)
            }
            startActivity(intent)
        }
    }

    private fun loadBookData() {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                book = database.bookDao().getBookById(bookId)
                val notesCount = database.noteDao().getNotesCountByBookId(bookId)

                withContext(Dispatchers.Main) {
                    book?.let {
                        bookTitleTextView.text = it.title
                        bookAuthorTextView.text = it.author ?: "Автор не указан"
                        pagesReadTextView.text = "${it.currentPage}/${it.pageCount} стр."
                        notesCountTextView.text = "$notesCount заметок"

                        if (!it.coverImage.isNullOrEmpty()) {
                            Glide.with(this@TimerActivity)
                                .load(it.coverImage)
                                .placeholder(R.drawable.plug)
                                .into(bookCoverImageView)
                        }
                    }
                }
            }
        }
    }

    private fun setupTimer() {
        timerRunnable = object : Runnable {
            override fun run() {
                if (isTimerRunning) {
                    if (countdownTime > 0) {
                        countdownTime -= COUNTDOWN_INTERVAL
                        if (countdownTime <= 0) {
                            countdownTime = 0
                            stopTimer()
                            onTimerCompletedInApp()
                        }
                        updateTimerDisplay(countdownTime)
                    } else {
                        elapsedTime = SystemClock.elapsedRealtime() - startTime
                        updateTimerDisplay(elapsedTime)
                    }
                    timerHandler.postDelayed(this, COUNTDOWN_INTERVAL)
                }
            }
        }
        updateTimerDisplay(0)
    }

    private fun onTimerCompletedInApp() {
        vibratePhone()
        playLocalAlarm()
        showCompletionDialog()
        saveReadingSession()
    }

    private fun toggleTimer() {
        if (isTimerRunning) {
            pauseTimer()
        } else {
            startTimer()
        }
    }

    private fun startTimer() {
        if (!isTimerRunning) {
            isTimerRunning = true
            timerToggleButton.setImageResource(R.drawable.pause)

            if (countdownTime > 0 && elapsedTime == 0L) {
                startTime = SystemClock.elapsedRealtime()
                updateTimerDisplay(countdownTime)
                scheduleTimerWork()
            } else if (elapsedTime == 0L) {
                startTime = SystemClock.elapsedRealtime()
            } else {
                startTime = SystemClock.elapsedRealtime() - elapsedTime
            }

            timerHandler.postDelayed(timerRunnable, COUNTDOWN_INTERVAL)
        }
    }

    private fun pauseTimer() {
        if (isTimerRunning) {
            isTimerRunning = false
            timerToggleButton.setImageResource(R.drawable.play)

            if (countdownTime == 0L) {
                elapsedTime = SystemClock.elapsedRealtime() - startTime
            }

            timerHandler.removeCallbacks(timerRunnable)
        }
    }

    private fun finishTimer() {
        var readingTimeMillis = elapsedTime

        if (countdownTime > 0) {
            readingTimeMillis = countdownTime
        }

        if (isTimerRunning) {
            readingTimeMillis = SystemClock.elapsedRealtime() - startTime
        }

        val isFirstReadingToday = checkIfFirstReadingToday()

        val intent = Intent(this, SaveReadingSessionActivity::class.java).apply {
            putExtra(SaveReadingSessionActivity.EXTRA_BOOK_ID, bookId)
            putExtra(SaveReadingSessionActivity.EXTRA_READING_TIME, readingTimeMillis)
            putExtra(SaveReadingSessionActivity.EXTRA_IS_FIRST_READING_TODAY, isFirstReadingToday)
        }
        startActivity(intent)
        finish()
    }

    private fun checkIfFirstReadingToday(): Boolean {
        val prefs = getSharedPreferences("reading_prefs", MODE_PRIVATE)
        val lastReadingDate = prefs.getString("last_reading_date", "")
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        return lastReadingDate != today
    }

    private fun stopTimer() {
        isTimerRunning = false
        timerToggleButton.setImageResource(R.drawable.play)
        timerHandler.removeCallbacks(timerRunnable)

        if (countdownTime > 0) {
            countdownTime = 0
        } else {
            elapsedTime = SystemClock.elapsedRealtime() - startTime
        }

        updateTimerDisplay(if (countdownTime > 0) countdownTime else elapsedTime)
    }

    private fun updateTimerDisplay(milliseconds: Long) {
        val hours = milliseconds / (1000 * 60 * 60)
        val minutes = (milliseconds % (1000 * 60 * 60)) / (1000 * 60)
        val seconds = (milliseconds % (1000 * 60)) / 1000
        timerTextView.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    private fun showCountdownDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_countdown, null)
        val numberPickerHour = dialogView.findViewById<NumberPicker>(R.id.numberPickerHour)
        val numberPickerMinute = dialogView.findViewById<NumberPicker>(R.id.numberPickerMinute)
        val numberPickerSecond = dialogView.findViewById<NumberPicker>(R.id.numberPickerSecond)

        numberPickerHour.minValue = 0
        numberPickerHour.maxValue = 23
        numberPickerHour.value = 0
        numberPickerMinute.minValue = 0
        numberPickerMinute.maxValue = 59
        numberPickerMinute.value = 0
        numberPickerSecond.minValue = 0
        numberPickerSecond.maxValue = 59
        numberPickerSecond.value = 0

        android.app.AlertDialog.Builder(this)
            .setTitle("Установить обратный отсчёт")
            .setView(dialogView)
            .setPositiveButton("Установить") { _, _ ->
                countdownTime = (numberPickerHour.value * 3600L +
                        numberPickerMinute.value * 60L +
                        numberPickerSecond.value) * 1000L
                elapsedTime = 0L
                if (isTimerRunning) stopTimer()
                updateTimerDisplay(countdownTime)
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun vibratePhone() {
        if (vibrator == null) {
            return
        }

        try {
            val hasVibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                vibrator!!.hasVibrator()
            } else {
                vibrator!!.hasVibrator()
            }

            if (!hasVibrator) {
                return
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val vibrationEffect = VibrationEffect.createOneShot(
                    1500,
                    VibrationEffect.DEFAULT_AMPLITUDE
                )
                vibrator!!.vibrate(vibrationEffect)
            } else {
                vibrator!!.vibrate(1500)
            }

        } catch (e: Exception) {
        }
    }

    private fun playLocalAlarm() {
        try {
            val notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val ringtone = RingtoneManager.getRingtone(this, notificationUri)
            ringtone.play()
        } catch (e: Exception) {
        }
    }

    private fun showCompletionDialog() {
        android.app.AlertDialog.Builder(this)
            .setTitle("Время вышло!")
            .setMessage("Таймер завершён. Вы можете продолжить читать или сделать заметку.")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }

    private fun scheduleTimerWork() {
        if (countdownTime > 0 && book != null) {
            val bookTitle = book?.title ?: "Книга"
            TimerWorker.scheduleTimer(this, bookTitle, countdownTime, bookId)
        }
    }

    private fun saveReadingSession() {
        val totalTime = if (countdownTime > 0) {
            countdownTime
        } else {
            elapsedTime
        }

        val finalTime = if (totalTime == 0L && isTimerRunning) {
            SystemClock.elapsedRealtime() - startTime
        } else {
            totalTime
        }

        if (finalTime > 0 && book != null) {
            lifecycleScope.launch(Dispatchers.IO) {
                val session = ReadingSession(
                    bookId = bookId,
                    startTime = Date(System.currentTimeMillis() - finalTime),
                    endTime = Date(),
                    startPage = book!!.currentPage,
                    endPage = book!!.currentPage,
                    duration = finalTime / 1000
                )

                val sessionId = database.readingSessionDao().insertReadingSession(session)

                val updatedTotalReadingTime = book!!.totalReadingTime + (finalTime / (1000 * 60))
                val updatedBook = book!!.copy(totalReadingTime = updatedTotalReadingTime)
                database.bookDao().updateBook(updatedBook)
            }
        }
    }
}