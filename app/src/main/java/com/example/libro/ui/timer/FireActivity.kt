package com.example.libro.ui.timer

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.libro.databinding.ActivityFireBinding
import com.example.libro.ui.MainActivity

class FireActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFireBinding

    private var streakCount: Int = -1
    private var showAchievementDialog: Boolean = false
    private var achievementNames: Array<String> = emptyArray()

    companion object {
        const val EXTRA_STREAK_COUNT = "streak_count"
        const val EXTRA_NEW_ACHIEVEMENTS = "new_achievements"
        const val EXTRA_ACHIEVEMENT_NAMES = "achievement_names"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFireBinding.inflate(layoutInflater)
        setContentView(binding.root)

        streakCount = intent.getIntExtra(EXTRA_STREAK_COUNT, 0)
        showAchievementDialog = intent.getBooleanExtra(EXTRA_NEW_ACHIEVEMENTS, false)
        achievementNames = intent.getStringArrayExtra(EXTRA_ACHIEVEMENT_NAMES) ?: emptyArray()

        setupUI()
        setupClickListeners()

        if (showAchievementDialog && achievementNames.isNotEmpty()) {
            showAchievementDialog(achievementNames)
        }
    }

    private fun setupUI() {
        binding.tvStreakCount.text = streakCount.toString()

        val motivationPhrases = listOf(
            "Продолжай в том же духе!",
            "Каждая прочитанная страница делает тебя лучше!",
            "Ты на верном пути к своей цели!",
            "Чтение - это суперсила!",
            "Сегодня ты стал на шаг ближе к мечте!",
            "Умные читают, гении перечитывают!",
            "Твоя дисциплина впечатляет!",
            "Маленькие шаги приводят к большим победам!"
        )

        val randomPhrase = motivationPhrases.random()
        binding.tvMotivation.text = randomPhrase
    }

    private fun setupClickListeners() {
        binding.btnContinue.setOnClickListener {
            navigateToHome()
        }
    }

    private fun showAchievementDialog(achievementNames: Array<String>) {
        val title = if (achievementNames.size == 1) {
            "Новое достижение!"
        } else {
            "Новые достижения!"
        }

        val message = if (achievementNames.size == 1) {
            "Поздравляем! Вы получили достижение:\n\"${achievementNames[0]}\""
        } else {
            "Поздравляем! Вы получили достижения:\n${achievementNames.joinToString("\n") { "• $it" }}"
        }

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Отлично!") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }

    private fun navigateToHome() {
        if (achievementNames.isNotEmpty()) {
            val prefs = getSharedPreferences("achievements", 0)
            val editor = prefs.edit()
            editor.putBoolean("has_new_achievements", true)
            editor.putString("achievement_names", achievementNames.joinToString("|"))
            editor.apply()
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("FRAGMENT_TO_LOAD", "home")
            if (achievementNames.isNotEmpty()) {
                putExtra("NEW_ACHIEVEMENTS", true)
                putExtra("ACHIEVEMENT_NAMES", achievementNames)
            }
        }
        startActivity(intent)
        finish()
    }
}