package com.example.salsabilapp.presentation.proximity

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.salsabilapp.R
import com.example.salsabilapp.databinding.FragmentProximityBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProximityFragment : Fragment(R.layout.fragment_proximity) {

    private val viewModel: ProximityViewModel by viewModels()
    private var _binding: FragmentProximityBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentProximityBinding.bind(view)

        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        binding.switchProximityMode.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewModel.enableProximityMode()
            } else {
                viewModel.disableProximityMode()
            }
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                updateUI(state)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.nearbyUsers.collect { users ->
                binding.tvNearbyCount.text = "${users.size} nearby users"
            }
        }
    }

    private fun updateUI(state: ProximityUiState) {
        binding.apply {
            switchProximityMode.isChecked = state.isEnabled
            tvEventCount.text = "${state.recentEventCount} events logged"
            progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
