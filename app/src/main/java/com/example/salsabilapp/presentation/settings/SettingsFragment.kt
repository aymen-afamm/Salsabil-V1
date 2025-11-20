package com.example.salsabilapp.presentation.settings

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.salsabilapp.R
import com.example.salsabilapp.databinding.FragmentSettingsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSettingsBinding.bind(view)

        setupUI()
    }

    private fun setupUI() {
        // Settings will be implemented here
        // Theme selection, language, notifications, etc.
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
