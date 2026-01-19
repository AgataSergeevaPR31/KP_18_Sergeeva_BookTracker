package com.example.libro.ui.achievement.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.libro.R
import com.example.libro.Database.Achievement
import com.example.libro.Database.UserAchievement
import com.example.libro.databinding.ItemAchievementBinding
import java.text.SimpleDateFormat
import java.util.Locale

class AchievementAdapter(
    private val onItemClick: (Achievement, UserAchievement?) -> Unit
) : ListAdapter<AchievementWithProgress, AchievementAdapter.ViewHolder>(AchievementDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAchievementBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        private val binding: ItemAchievementBinding,
        private val onItemClick: (Achievement, UserAchievement?) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: AchievementWithProgress) {
            binding.tvName.text = item.achievement.name
            binding.tvDescription.text = item.achievement.description

            if (item.isAchieved) {
                binding.ivIcon.setImageResource(R.drawable.ic_achievement_unlocked)
                binding.progressBar.visibility = View.GONE
                binding.tvProgress.visibility = View.GONE
                binding.tvUnlockedDate.visibility = View.VISIBLE

                item.userAchievement?.achievementDate?.let { date ->
                    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                    binding.tvUnlockedDate.text = "Получено: ${dateFormat.format(date)}"
                } ?: run {
                    binding.tvUnlockedDate.text = "Получено"
                }

                if (item.achievement.requiredValue > 0) {
                    binding.progressBar.progress = 100
                    binding.tvProgress.text = "${item.achievement.requiredValue.toInt()}/${item.achievement.requiredValue.toInt()}"
                }
            } else {

                binding.ivIcon.setImageResource(R.drawable.ic_achievement_locked)
                binding.progressBar.visibility = View.VISIBLE
                binding.tvProgress.visibility = View.VISIBLE
                binding.tvUnlockedDate.visibility = View.GONE

                if (item.achievement.requiredValue > 0) {
                    val progressPercent = if (item.achievement.requiredValue > 0) {
                        (item.progress / item.achievement.requiredValue * 100).toInt()
                    } else {
                        0
                    }
                    val safeProgress = progressPercent.coerceIn(0, 100)
                    binding.progressBar.progress = safeProgress
                    binding.tvProgress.text = "${item.progress.toInt()}/${item.achievement.requiredValue.toInt()}"
                } else {
                    binding.progressBar.progress = 0
                    binding.tvProgress.text = "Не получено"
                }
            }

            binding.root.setOnClickListener {
                onItemClick(item.achievement, item.userAchievement)
            }

            binding.tvType.text = when (item.achievement.type) {
                Achievement.AchievementType.PAGES_READ -> "Чтение"
                Achievement.AchievementType.BOOKS_READ -> "Книги"
                Achievement.AchievementType.READING_STREAK -> "Стрик"
                Achievement.AchievementType.NIGHT_READING -> "Ночное чтение"
                Achievement.AchievementType.LONG_BOOK -> "Длинная книга"
                Achievement.AchievementType.READING_TIME -> "Время чтения"
                Achievement.AchievementType.PAGES_PER_SESSION -> "За сессию"
                Achievement.AchievementType.FIRST_STEPS -> "Первые шаги"
                else -> "Другое"
            }
        }
    }
}


class AchievementDiffCallback : DiffUtil.ItemCallback<AchievementWithProgress>() {
    override fun areItemsTheSame(oldItem: AchievementWithProgress, newItem: AchievementWithProgress): Boolean {
        return oldItem.achievement.achievementId == newItem.achievement.achievementId
    }

    override fun areContentsTheSame(oldItem: AchievementWithProgress, newItem: AchievementWithProgress): Boolean {
        return oldItem.achievement == newItem.achievement &&
                oldItem.userAchievement == newItem.userAchievement &&
                oldItem.progress == newItem.progress &&
                oldItem.isAchieved == newItem.isAchieved
    }
}


data class AchievementWithProgress(
    val achievement: Achievement,
    val userAchievement: UserAchievement?,
    val progress: Double,
    val isAchieved: Boolean
)