package com.ventivader.ui.splashscreen

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.ventivader.R
import com.ventivader.VentivaderApplication
import com.ventivader.ble.BluetoothConnectionStatus
import com.ventivader.ui.mainscreen.MainActivity
import com.ventivader.ui.splashscreen.ble.BluetoothScanViewModel
import kotlinx.android.synthetic.main.splash_layout.*

class SplashActivity : AppCompatActivity(), Observer<BluetoothConnectionStatus> {

    private val bluetoothConnectionViewModel: BluetoothScanViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.splash_layout)

        if( (application as VentivaderApplication).bluetoothHelper.isBluetoothDisabled()) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent,
                REQUEST_ENABLE_BT
            )
        } else {
            startBleScan()
        }
    }

    private fun startBleScan() {
        bluetoothConnectionViewModel.liveData.observe(this, this)
        bluetoothConnectionViewModel.findBleDevice()
    }

    override fun onChanged(connectionStatus: BluetoothConnectionStatus?) {

        when(connectionStatus) {

            is BluetoothConnectionStatus.Connecting -> {
                bluetooth_status_label.text = connectionStatus.plainConnectionStatus
                progress_bar.show()
            }

            is BluetoothConnectionStatus.Success -> {
                progress_bar.hide()

                val statusText = connectionStatus.plainConnectionStatus + "" + connectionStatus.bluetoothDevice.name
                bluetooth_status_label.text = statusText

                startActivity(
                    MainActivity.getIntent(
                        this@SplashActivity,
                        connectionStatus.bluetoothDevice
                    )
                )
            }

            is BluetoothConnectionStatus.Error -> {
                progress_bar.hide()

                bluetooth_status_label.text = connectionStatus.plainConnectionStatus

                // TODO - should allow user to try again.. or just retry indefinitely
                // but this is good for MVP
                finish()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode == RESULT_OK && requestCode == REQUEST_ENABLE_BT) {
            startBleScan()
        }
    }

    companion object {
        private const val REQUEST_ENABLE_BT: Int = 1
    }
}