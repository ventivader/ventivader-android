package com.ventivader.ui.mainscreen

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.ventivader.VentivaderApplication
import com.ventivader.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val bluetoothConnectionViewModel: BluetoothConnectionViewModel by viewModels {
        BluetoothConnectionViewModel.BluetoothConnectionViewModelFactory (
                applicationContext,
                intent.getParcelableExtra(EXTRA_BLUETOOTH_DEVICE_INTENT)!!
            )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
    }

    override fun onResume() {
        super.onResume()

        listenForVentilateButtonPress()
        observerBluetoothConnection()
        updateViews()
    }

    private fun listenForVentilateButtonPress() {
        binding.ventilateButton.setOnClickListener {
            bluetoothConnectionViewModel.sendSolenoidParameters()
        }
    }

    private fun observerBluetoothConnection() {
        bluetoothConnectionViewModel.solenoidParametersLiveData.observe (this, Observer {
            updateViews()
        })
    }

    private fun updateViews() {
        bluetoothConnectionViewModel.solenoidParametersLiveData.value?.let {
            binding.exhaleTimeLabel.input.setText(it.exhaleSec)
            binding.exhaleHoldLabel.input.setText(it.exhaleHoldSec)
            binding.inhaleTimeLabel.input.setText(it.inhaleSec)
            binding.inhaleHoldLabel.input.setText(it.inhaleHoldSec)
            binding.ventilationCyclesLabel.input.setText(it.numberVentCycles)
        }
    }

    companion object {

        const val EXTRA_BLUETOOTH_DEVICE_INTENT = "EXTRA_BLUETOOTH_DEVICE_INTENT"

        fun getIntent(context: Context, bluetoothDevice: BluetoothDevice) : Intent {
            return Intent(context, MainActivity::class.java).apply {
                putExtra(EXTRA_BLUETOOTH_DEVICE_INTENT, bluetoothDevice)
            }
        }
    }
}
