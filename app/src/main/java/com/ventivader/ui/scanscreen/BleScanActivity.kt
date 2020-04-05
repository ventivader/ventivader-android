package com.ventivader.ui.scanscreen

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
import kotlinx.android.synthetic.main.splash_layout.*

class BleScanActivity : AppCompatActivity(), Observer<BluetoothConnectionStatus> {

    private val bleScanAndConnectViewModel: BleScanViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.splash_layout)

        if( (application as VentivaderApplication).bleConnectionManager.isBluetoothDisabled()) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent,
                REQUEST_ENABLE_BT
            )
        } else {
            startBleScan()
        }
    }

    private fun startBleScan() {

        bleScanAndConnectViewModel.liveData.observe(this, this)

        bleScanAndConnectViewModel.findAndConnectBleDevice()
    }

    override fun onChanged(connectionStatus: BluetoothConnectionStatus?) {

        bluetooth_status_label.text = connectionStatus?.plainConnectionStatus

        when(connectionStatus) {

            is BluetoothConnectionStatus.Connecting -> {
                progress_bar.show()
            }

            is BluetoothConnectionStatus.Success -> {
                progress_bar.hide()

                startActivity(
                    MainActivity.getIntent(
                        this@BleScanActivity
                    )
                )
            }

            is BluetoothConnectionStatus.Error -> {
                progress_bar.hide()

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