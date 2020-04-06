package com.ventivader.ui.scanscreen

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
            checkBTPermission()
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
            checkBTPermission()
        }
    }

    private fun checkBTPermission() {
        val finePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarsePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
        val btPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH)
        val adminPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN)

        if (finePermission != PackageManager.PERMISSION_GRANTED ||
            coarsePermission != PackageManager.PERMISSION_GRANTED ||
            btPermission != PackageManager.PERMISSION_GRANTED ||
            adminPermission != PackageManager.PERMISSION_GRANTED) {

            Log.i(TAG, "Permissions not granted yet")
            getBTPermission()
        } else {
            startBleScan()
        }
    }

    private fun getBTPermission() {
        ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.ACCESS_FINE_LOCATION),
            BT_PERMISSION_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            BT_PERMISSION_REQUEST_CODE -> {

                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {

                    Log.i(TAG, "Permission has been denied by user")
                } else {
                    Log.i(TAG, "Permission has been granted by user")

                    startBleScan()
                }
            }
        }
    }

    companion object {
        const val TAG = "BleScanActivity"
        private const val REQUEST_ENABLE_BT: Int = 1
        private const val BT_PERMISSION_REQUEST_CODE = 101
    }
}