package com.example.salsabilapp.presentation.smartwatch

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.salsabilapp.R
import com.example.salsabilapp.databinding.FragmentSmartwatchBinding
import com.example.salsabilapp.domain.ble.BleManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SmartwatchFragment : Fragment(R.layout.fragment_smartwatch) {

    private val viewModel: SmartwatchViewModel by viewModels()
    private var _binding: FragmentSmartwatchBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSmartwatchBinding.bind(view)

        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        binding.btnScan.setOnClickListener {
            if (viewModel.uiState.value.isScanning) {
                viewModel.stopScan()
            } else {
                viewModel.startScan()
            }
        }

        binding.btnDisconnect.setOnClickListener {
            viewModel.disconnect()
        }

        binding.btnUuidScanner.setOnClickListener {
            viewModel.scanUuids()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                updateUI(state)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.connectionState.collect { state ->
                updateConnectionState(state)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.smartwatchData.collect { data ->
                data?.let {
                    binding.tvSteps.text = "${it.steps} steps"
                    binding.tvHeartRate.text = "${it.heartRate} bpm"
                    binding.tvBattery.text = "${it.batteryLevel}%"
                }
            }
        }
    }

    private fun updateUI(state: SmartwatchUiState) {
        binding.apply {
            btnScan.text = if (state.isScanning) "Stop Scan" else getString(R.string.smartwatch_scan)
            progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE

            state.activeDevice?.let { device ->
                tvDeviceName.text = device.deviceName
                tvDeviceMac.text = device.deviceMac
            }
        }
    }

    private fun updateConnectionState(state: BleManager.ConnectionState) {
        binding.tvConnectionStatus.text = when (state) {
            BleManager.ConnectionState.DISCONNECTED -> getString(R.string.smartwatch_disconnected)
            BleManager.ConnectionState.CONNECTING -> "Connecting..."
            BleManager.ConnectionState.CONNECTED -> getString(R.string.smartwatch_connected)
            BleManager.ConnectionState.READY -> "${getString(R.string.smartwatch_connected)} & Ready"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
