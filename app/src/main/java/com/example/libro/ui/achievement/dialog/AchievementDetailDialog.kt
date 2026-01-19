package com.example.libro.ui.achievement.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.example.libro.Database.Achievement
import com.example.libro.Database.UserAchievement
import com.example.libro.R

class AchievementDetailDialog : DialogFragment() {

    companion object {
        private const val ARG_ACHIEVEMENT = "achievement"
        private const val ARG_USER_ACHIEVEMENT = "user_achievement"

        fun newInstance(
            achievement: Achievement,
            userAchievement: UserAchievement
        ): AchievementDetailDialog {
            val args = Bundle().apply {
                putSerializable(ARG_ACHIEVEMENT, achievement)
                putSerializable(ARG_USER_ACHIEVEMENT, userAchievement)
            }
            return AchievementDetailDialog().apply {
                arguments = args
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_achievement_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val achievement = arguments?.getSerializable(ARG_ACHIEVEMENT) as? Achievement
        val userAchievement = arguments?.getSerializable(ARG_USER_ACHIEVEMENT) as? UserAchievement

        if (achievement != null && userAchievement != null) {
            setupViews(view, achievement, userAchievement)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return dialog
    }

    private fun setupViews(
        view: View,
        achievement: Achievement,
        userAchievement: UserAchievement
    ) {
        val title: TextView = view.findViewById(R.id.detailTitle)
        val description: TextView = view.findViewById(R.id.detailDescription)
        val status: TextView = view.findViewById(R.id.detailStatus)
        val closeButton: View = view.findViewById(R.id.closeButton)


        title.text = achievement.name

        description.text = achievement.description

        status.text = if (userAchievement.isAchieved) {
            "Получено"
        } else {
            "Не получено"
        }

        // Кнопка закрытия
        closeButton.setOnClickListener {
            dismiss()
        }
    }
}