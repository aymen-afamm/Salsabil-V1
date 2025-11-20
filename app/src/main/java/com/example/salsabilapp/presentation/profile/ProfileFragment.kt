package com.example.salsabilapp.presentation.profile

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.salsabilapp.R
import com.example.salsabilapp.databinding.FragmentProfileBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private val viewModel: ProfileViewModel by viewModels()
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentProfileBinding.bind(view)

        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        binding.btnSaveProfile.setOnClickListener {
            saveProfile()
        }

        binding.switchKidneyCondition.setOnCheckedChangeListener { _, isChecked ->
            // Update UI based on kidney condition
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                updateUI(state)
            }
        }
    }

    private fun updateUI(state: ProfileUiState) {
        binding.apply {
            etName.setText(state.name)
            etAge.setText(if (state.age > 0) state.age.toString() else "")
            etHeight.setText(if (state.heightCm > 0) state.heightCm.toString() else "")
            etWeight.setText(if (state.weightKg > 0) state.weightKg.toString() else "")
            switchKidneyCondition.isChecked = state.hasKidneyCondition
            etDailyGoal.setText(state.dailyGoalMl.toString())

            // Set sex radio button
            when (state.sex) {
                "male" -> radioMale.isChecked = true
                "female" -> radioFemale.isChecked = true
                else -> radioOther.isChecked = true
            }

            progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE

            if (state.saveSuccess) {
                Toast.makeText(requireContext(), R.string.success_saved, Toast.LENGTH_SHORT).show()
                viewModel.clearSaveSuccess()
            }
        }
    }

    private fun saveProfile() {
        binding.apply {
            val name = etName.text.toString()
            val age = etAge.text.toString().toIntOrNull() ?: 0
            val height = etHeight.text.toString().toFloatOrNull() ?: 0f
            val weight = etWeight.text.toString().toFloatOrNull() ?: 0f
            val hasKidneyCondition = switchKidneyCondition.isChecked
            val dailyGoal = etDailyGoal.text.toString().toIntOrNull() ?: 2000

            val sex = when {
                radioMale.isChecked -> "male"
                radioFemale.isChecked -> "female"
                else -> "other"
            }

            viewModel.updateProfile(
                name = name,
                age = age,
                sex = sex,
                heightCm = height,
                weightKg = weight,
                hasKidneyCondition = hasKidneyCondition,
                dailyGoalMl = dailyGoal,
                isGoalOverridden = true
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
