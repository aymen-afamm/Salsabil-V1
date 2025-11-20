package com.example.salsabilapp.presentation.analytics

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.salsabilapp.R
import com.example.salsabilapp.databinding.FragmentAnalyticsBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AnalyticsFragment : Fragment(R.layout.fragment_analytics) {

    private val viewModel: AnalyticsViewModel by viewModels()
    private var _binding: FragmentAnalyticsBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAnalyticsBinding.bind(view)

        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        binding.chipWeekly.setOnClickListener {
            viewModel.loadWeeklyData()
        }

        binding.chipMonthly.setOnClickListener {
            viewModel.loadMonthlyData()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                updateUI(state)
            }
        }
    }

    private fun updateUI(state: AnalyticsUiState) {
        binding.apply {
            tvTotalIntake.text = "${state.totalIntake} ml"
            tvAvgDaily.text = "${state.avgDailyIntake} ml"
            tvTotalSteps.text = "${state.totalSteps} steps"

            // Update chip selection
            when (state.period) {
                AnalyticsPeriod.WEEKLY -> {
                    chipWeekly.isChecked = true
                    chipMonthly.isChecked = false
                }
                AnalyticsPeriod.MONTHLY -> {
                    chipWeekly.isChecked = false
                    chipMonthly.isChecked = true
                }
            }

            progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
