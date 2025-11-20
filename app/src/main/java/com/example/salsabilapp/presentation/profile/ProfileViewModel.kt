package com.example.salsabilapp.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.salsabilapp.data.local.LedgerManager
import com.example.salsabilapp.data.local.entity.UserProfile
import com.example.salsabilapp.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val ledgerManager: LedgerManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            userRepository.getUserProfile().collect { profile ->
                _uiState.value = if (profile != null) {
                    ProfileUiState(
                        name = profile.name,
                        age = profile.age,
                        sex = profile.sex,
                        heightCm = profile.heightCm,
                        weightKg = profile.weightKg,
                        hasKidneyCondition = profile.hasKidneyCondition,
                        dailyGoalMl = profile.dailyGoalMl,
                        isGoalOverridden = profile.isGoalOverridden,
                        isLoading = false
                    )
                } else {
                    ProfileUiState(isLoading = false)
                }
            }
        }
    }

    fun updateProfile(
        name: String,
        age: Int,
        sex: String,
        heightCm: Float,
        weightKg: Float,
        hasKidneyCondition: Boolean,
        dailyGoalMl: Int,
        isGoalOverridden: Boolean
    ) {
        viewModelScope.launch {
            val profile = UserProfile(
                name = name,
                age = age,
                sex = sex,
                heightCm = heightCm,
                weightKg = weightKg,
                hasKidneyCondition = hasKidneyCondition,
                dailyGoalMl = dailyGoalMl,
                isGoalOverridden = isGoalOverridden
            )

            userRepository.saveUserProfile(profile)

            // Log to ledger
            ledgerManager.addEntry(
                LedgerManager.EVENT_PROFILE_UPDATED,
                mapOf(
                    "name" to name,
                    "age" to age,
                    "weightKg" to weightKg,
                    "hasKidneyCondition" to hasKidneyCondition
                )
            )

            _uiState.value = _uiState.value.copy(saveSuccess = true)
        }
    }

    fun clearSaveSuccess() {
        _uiState.value = _uiState.value.copy(saveSuccess = false)
    }
}

data class ProfileUiState(
    val name: String = "",
    val age: Int = 0,
    val sex: String = "male",
    val heightCm: Float = 0f,
    val weightKg: Float = 0f,
    val hasKidneyCondition: Boolean = false,
    val dailyGoalMl: Int = 2000,
    val isGoalOverridden: Boolean = false,
    val isLoading: Boolean = true,
    val saveSuccess: Boolean = false,
    val error: String? = null
)
