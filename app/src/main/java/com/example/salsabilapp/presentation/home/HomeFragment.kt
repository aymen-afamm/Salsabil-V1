package com.example.salsabilapp.presentation.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.salsabilapp.R
import com.example.salsabilapp.databinding.FragmentHomeBinding
import com.example.salsabilapp.domain.HydrationEngine
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

    private val viewModel: HomeViewModel by viewModels()
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)

        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        // Quick add buttons
        binding.btnQuick250.setOnClickListener { viewModel.quickAdd(250) }
        binding.btnQuick500.setOnClickListener { viewModel.quickAdd(500) }
        binding.btnQuick750.setOnClickListener { viewModel.quickAdd(750) }

        // Custom add button
        binding.fabAddWater.setOnClickListener {
            // Show dialog for custom amount
            val amount = binding.etCustomAmount.text.toString().toIntOrNull() ?: 0
            if (amount > 0) {
                viewModel.addWater(amount)
                binding.etCustomAmount.text?.clear()
            }
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                updateUI(state)
            }
        }
    }

    private fun updateUI(state: HomeUiState) {
        binding.apply {
            // Update progress
            progressHydration.progress = state.progress
            tvCurrentIntake.text = "${state.currentIntake} ml"
            tvDailyGoal.text = "${state.dailyGoal} ml"
            tvRemaining.text = "${state.dailyGoal - state.currentIntake} ml"

            // Update status
            tvStatus.text = getStatusText(state.hydrationStatus)
            tvSteps.text = "${state.steps} steps"

            // Show/hide loading
            progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
        }
    }

    private fun getStatusText(status: HydrationEngine.HydrationStatus): String {
        return when (status) {
            HydrationEngine.HydrationStatus.EXCELLENT -> getString(R.string.status_excellent)
            HydrationEngine.HydrationStatus.GOOD -> getString(R.string.status_good)
            HydrationEngine.HydrationStatus.FAIR -> getString(R.string.status_fair)
            HydrationEngine.HydrationStatus.BEHIND -> getString(R.string.status_behind)
            HydrationEngine.HydrationStatus.CRITICAL -> getString(R.string.status_critical)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
