package com.ventivader.ui.mainscreen

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.ventivader.R
import com.ventivader.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val BLEConnectionViewModel: BLEConnectionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
    }

    override fun onResume() {
        super.onResume()

        listenForVentilateButtonPress()
        observeSolenoidParametersOverBluetooth()
        updateViews()
    }

    private fun listenForVentilateButtonPress() {
        binding.ventilateButton.setOnClickListener {
            BLEConnectionViewModel.sendSolenoidParameters()
        }
    }

    private fun observeSolenoidParametersOverBluetooth() {
        BLEConnectionViewModel.solenoidParametersLiveData.observe (this, Observer {
            updateViews()
        })
    }

    private fun updateViews() {

        binding.inhaleTimeLabel.label.text = getString(R.string.inhale_time_in_secs)
        binding.inhaleHoldLabel.label.text = getString(R.string.inhale_hold_time_in_secs)
        binding.exhaleTimeLabel.label.text = getString(R.string.exhale_time_in_secs)
        binding.exhaleHoldLabel.label.text = getString(R.string.exhale_hold_time_in_secs)
        binding.ventilationCyclesLabel.label.text = getString(R.string.num_ventilation_cycles)

        BLEConnectionViewModel.solenoidParametersLiveData.value?.let {
            binding.exhaleTimeLabel.input.setText(it.exhaleSec)
            binding.exhaleHoldLabel.input.setText(it.exhaleHoldSec)
            binding.inhaleTimeLabel.input.setText(it.inhaleSec)
            binding.inhaleHoldLabel.input.setText(it.inhaleHoldSec)
            binding.ventilationCyclesLabel.input.setText(it.numberVentCycles)
        }
    }

    companion object {

        fun getIntent(context: Context) : Intent {
            return Intent(context, MainActivity::class.java)
        }
    }
}
