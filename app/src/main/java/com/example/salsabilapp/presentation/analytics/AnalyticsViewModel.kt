package com.example.salsabilapp.presentation.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.salsabilapp.data.repository.ActivityRepository
import com.example.salsabilapp.data.repository.HydrationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val hydrationRepository: HydrationRepository,
    private val activityRepository: ActivityRepository
) : ViewModel() {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    init {
        loadWeeklyData()
    }

    fun loadWeeklyData() {
        viewModelScope.launch {
            val calendar = Calendar.getInstance()
            val endDate = dateFormat.format(calendar.time)
            calendar.add(Calendar.DAY_OF_YEAR, -7)
            val startDate = dateFormat.format(calendar.time)

            val totalIntake = hydrationRepository.getTotalForDateRange(startDate, endDate)
            val totalSteps = activityRepository.getTotalStepsForDateRange(startDate, endDate)

            _uiState.value = AnalyticsUiState(
                period = AnalyticsPeriod.WEEKLY,
                totalIntake = totalIntake,
                totalSteps = totalSteps,
                avgDailyIntake = totalIntake / 7,
                isLoading = false
            )
        }
    }

    fun loadMonthlyData() {
        viewModelScope.launch {
            val calendar = Calendar.getInstance()
            val endDate = dateFormat.format(calendar.time)
            calendar.add(Calendar.DAY_OF_YEAR, -30)
            val startDate = dateFormat.format(calendar.time)

            val totalIntake = hydrationRepository.getTotalForDateRange(startDate, endDate)
            val totalSteps = activityRepository.getTotalStepsForDateRange(startDate, endDate)

            _uiState.value = AnalyticsUiState(
                period = AnalyticsPeriod.MONTHLY,
                totalIntake = totalIntake,
                totalSteps = totalSteps,
                avgDailyIntake = totalIntake / 30,
                isLoading = false
            )
        }
    }
}

data class AnalyticsUiState(
    val period: AnalyticsPeriod = AnalyticsPeriod.WEEKLY,
    val totalIntake: Int = 0,
    val totalSteps: Int = 0,
    val avgDailyIntake: Int = 0,
    val isLoading: Boolean = true,
    val error: String? = null
)

enum class AnalyticsPeriod {
    WEEKLY,
    MONTHLY
}
