package com.example.salsabilapp.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.salsabilapp.data.repository.ActivityRepository
import com.example.salsabilapp.data.repository.HydrationRepository
import com.example.salsabilapp.data.repository.UserRepository
import com.example.salsabilapp.domain.HydrationEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val hydrationRepository: HydrationRepository,
    private val activityRepository: ActivityRepository,
    private val userRepository: UserRepository,
    private val hydrationEngine: HydrationEngine
) : ViewModel() {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val today = dateFormat.format(Date())

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            // Combine all data streams
            combine(
                hydrationRepository.getTotalForToday(),
                activityRepository.getTotalStepsForToday(),
                userRepository.getUserProfile()
            ) { currentIntake, steps, userProfile ->
                val intake = currentIntake ?: 0
                val dailySteps = steps ?: 0
                val profile = userProfile

                val dailyGoal = if (profile != null) {
                    hydrationEngine.calculateDailyGoal(profile, dailySteps)
                } else {
                    2000
                }

                val progress = if (dailyGoal > 0) {
                    (intake.toFloat() / dailyGoal * 100).toInt()
                } else {
                    0
                }

                val status = hydrationEngine.getHydrationStatus(intake, dailyGoal)

                HomeUiState(
                    currentIntake = intake,
                    dailyGoal = dailyGoal,
                    progress = progress,
                    steps = dailySteps,
                    hydrationStatus = status,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun addWater(amountMl: Int) {
        viewModelScope.launch {
            hydrationRepository.addHydrationLog(amountMl)
        }
    }

    fun quickAdd(amountMl: Int) {
        addWater(amountMl)
    }
}

data class HomeUiState(
    val currentIntake: Int = 0,
    val dailyGoal: Int = 2000,
    val progress: Int = 0,
    val steps: Int = 0,
    val hydrationStatus: HydrationEngine.HydrationStatus = HydrationEngine.HydrationStatus.FAIR,
    val isLoading: Boolean = true,
    val error: String? = null
)
