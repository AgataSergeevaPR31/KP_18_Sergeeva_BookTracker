package com.example.libro.ui.timer

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.*
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.example.libro.R
import java.util.concurrent.TimeUnit

class TimerWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    companion object {
        private const val CHANNEL_ID = "timer_channel"
        private const val NOTIFICATION_ID = 9999
        private var isAppInForeground = false

        fun setAppForegroundState(foreground: Boolean) {
            isAppInForeground = foreground
        }

        fun scheduleTimer(context: Context, bookTitle: String, delayMillis: Long, bookId: Long = -1) {
            try {
                val inputData = Data.Builder()
                    .putString("book_title", bookTitle)
                    .putLong("book_id", bookId)
                    .build()

                val constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                    .setRequiresBatteryNotLow(false)
                    .setRequiresCharging(false)
                    .build()

                val timerWork = OneTimeWorkRequest.Builder(TimerWorker::class.java)
                    .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
                    .setConstraints(constraints)
                    .setInputData(inputData)
                    .addTag("timer_work")
                    .build()

                WorkManager.getInstance(context).enqueue(timerWork)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        private fun createNotificationChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                try {
                    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                    val channel = notificationManager.getNotificationChannel(CHANNEL_ID)
                    if (channel != null) {
                        return
                    }

                    val newChannel = NotificationChannel(
                        CHANNEL_ID,
                        "Таймер чтения",
                        NotificationManager.IMPORTANCE_HIGH
                    ).apply {
                        description = "Уведомления о завершении таймера чтения"
                        enableVibration(true)
                        vibrationPattern = longArrayOf(0, 1000, 500, 1000)
                        setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), null)
                        lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                    }
                    notificationManager.createNotificationChannel(newChannel)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override suspend fun doWork(): Result {
        return try {
            val bookTitle = inputData.getString("book_title") ?: "Таймер чтения"
            val bookId = inputData.getLong("book_id", -1)

            if (isAppInForeground) {
                playSimpleSound()
            } else {
                showNotification(bookTitle, bookId)
            }

            Result.success()

        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }

    private fun playSimpleSound() {
        try {
            val ringtone = RingtoneManager.getRingtone(
                applicationContext,
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            )
            ringtone.play()

            Handler(Looper.getMainLooper()).postDelayed({
                try {
                    ringtone.stop()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, 3000)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showNotification(bookTitle: String, bookId: Long) {
        try {
            val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            createNotificationChannel(applicationContext)

            val intent = Intent(applicationContext, TimerActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("book_id", bookId)
            }

            val pendingIntent = PendingIntent.getActivity(
                applicationContext,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                .setContentTitle("Время чтения вышло!")
                .setContentText("Таймер для книги \"$bookTitle\" завершился")
                .setSmallIcon(R.drawable.timer)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setAutoCancel(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setVibrate(longArrayOf(0, 1000, 500, 1000))
                .build()

            notificationManager.notify(NOTIFICATION_ID, notification)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}