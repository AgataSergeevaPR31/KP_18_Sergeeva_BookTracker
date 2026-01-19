package com.example.libro.ui.achievement

import androidx.lifecycle.*
import com.example.libro.Database.*
import com.example.libro.ui.achievement.adapter.AchievementWithProgress // Импортируйте из адаптера
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AchievementViewModel @Inject constructor(
    private val achievementDao: AchievementDao,
    private val userAchievementDao: UserAchievementDao
) : ViewModel() {

    private val _filterType = MutableStateFlow(FilterType.ALL)
    private val filterType = _filterType.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val achievementsFlow = achievementDao.getAllAchievementsFlow()
    private val userAchievementsFlow = userAchievementDao.getAllUserAchievementsFlow()

    val filteredAchievements: LiveData<List<AchievementWithProgress>> = combine(
        achievementsFlow,
        userAchievementsFlow,
        filterType
    ) { achievements, userAchievements, filter ->
        _isLoading.value = false

        val achievementsWithProgress = achievements.map { achievement ->
            val userAchievement = userAchievements.find { it.achievementId == achievement.achievementId }
            AchievementWithProgress( // Используйте класс из адаптера
                achievement = achievement,
                userAchievement = userAchievement,
                progress = userAchievement?.currentProgress ?: 0.0,
                isAchieved = userAchievement?.isAchieved ?: false
            )
        }

        when (filter) {
            FilterType.ALL -> achievementsWithProgress
            FilterType.ACHIEVED -> achievementsWithProgress.filter { it.isAchieved }
            FilterType.NOT_ACHIEVED -> achievementsWithProgress.filter { !it.isAchieved }
        }
    }.asLiveData()

    fun setFilterType(filterType: FilterType) {
        _filterType.value = filterType
    }

    fun loadAchievements() {
        _isLoading.value = true
    }

    enum class FilterType {
        ALL, ACHIEVED, NOT_ACHIEVED
    }
}