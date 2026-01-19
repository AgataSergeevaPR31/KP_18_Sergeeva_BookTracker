package com.example.libro.ui.Views

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import java.util.*

class ReadingCalendarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var readingDates: List<Date> = emptyList()
    private var currentMonth: Int = Calendar.getInstance().get(Calendar.MONTH)
    private var currentYear: Int = Calendar.getInstance().get(Calendar.YEAR)

    private val monthNames = arrayOf(
        "Январь", "Февраль", "Март", "Апрель", "Май", "Июнь",
        "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь"
    )

    private val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 20f
        color = Color.BLACK
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }

    private val dayNamePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 14f
        color = Color.parseColor("#666666")
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT
    }

    private val dayPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 16f
        color = Color.BLACK
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT
    }

    private val readingDayPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#4ECDC4")
        style = Paint.Style.FILL
    }

    private val todayPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FFD700")
        style = Paint.Style.FILL
    }

    private val whiteDayPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 16f
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredHeight = 320
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val height = when (heightMode) {
            MeasureSpec.EXACTLY -> heightSize
            MeasureSpec.AT_MOST -> Math.min(desiredHeight, heightSize)
            else -> desiredHeight
        }

        setMeasuredDimension(widthMeasureSpec, height)
    }

    fun setReadingDates(dates: List<Date>) {
        readingDates = dates
        invalidate()
    }

    fun setMonthAndYear(month: Int, year: Int) {
        currentMonth = month
        currentYear = year
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width.toFloat()
        val height = height.toFloat()

        canvas.drawColor(Color.WHITE)

        val cellSize = width / 7
        val headerHeight = 40f
        val dayNamesHeight = 30f
        val startY = 10f

        val monthYear = "${monthNames[currentMonth]} $currentYear"
        canvas.drawText(monthYear, width / 2, startY + headerHeight / 2 + 5, headerPaint)

        val dayNames = arrayOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
        for (i in 0 until 7) {
            val x = i * cellSize + cellSize / 2
            val y = startY + headerHeight + dayNamesHeight / 2 + 5
            canvas.drawText(dayNames[i], x, y, dayNamePaint)
        }

        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, currentYear)
            set(Calendar.MONTH, currentMonth)
            set(Calendar.DAY_OF_MONTH, 1)
        }

        val today = Calendar.getInstance()
        val isCurrentMonth = today.get(Calendar.YEAR) == currentYear &&
                today.get(Calendar.MONTH) == currentMonth

        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        val firstDayOfWeek = (calendar.get(Calendar.DAY_OF_WEEK) - 2 + 7) % 7

        val startYDays = startY + headerHeight + dayNamesHeight + 5

        for (day in 1..daysInMonth) {
            val position = firstDayOfWeek + (day - 1)
            val row = position / 7
            val col = position % 7

            val centerX = col * cellSize + cellSize / 2
            val centerY = startYDays + row * cellSize + cellSize / 2

            val isToday = isCurrentMonth && day == today.get(Calendar.DAY_OF_MONTH)
            val isReadingDay = isReadingDay(day)

            if (isToday || isReadingDay) {
                val paint = if (isToday) todayPaint else readingDayPaint
                canvas.drawCircle(centerX, centerY, cellSize / 2.5f, paint)

                canvas.drawText(day.toString(), centerX, centerY + 5, whiteDayPaint)
            } else {
                canvas.drawText(day.toString(), centerX, centerY + 5, dayPaint)
            }
        }

        val gridPaint = Paint().apply {
            color = Color.parseColor("#E0E0E0")
            strokeWidth = 1f
        }

        for (i in 0..7) {
            val x = i * cellSize
            canvas.drawLine(x, startYDays, x, startYDays + 6 * cellSize, gridPaint)
        }

        for (i in 0..6) {
            val y = startYDays + i * cellSize
            canvas.drawLine(0f, y, width, y, gridPaint)
        }
    }

    private fun isReadingDay(day: Int): Boolean {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, currentYear)
            set(Calendar.MONTH, currentMonth)
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        return readingDates.any { readingDate ->
            val readingCalendar = Calendar.getInstance()
            readingCalendar.time = readingDate
            readingCalendar.set(Calendar.HOUR_OF_DAY, 0)
            readingCalendar.set(Calendar.MINUTE, 0)
            readingCalendar.set(Calendar.SECOND, 0)
            readingCalendar.set(Calendar.MILLISECOND, 0)

            readingCalendar.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) &&
                    readingCalendar.get(Calendar.MONTH) == calendar.get(Calendar.MONTH) &&
                    readingCalendar.get(Calendar.DAY_OF_MONTH) == calendar.get(Calendar.DAY_OF_MONTH)
        }
    }
}