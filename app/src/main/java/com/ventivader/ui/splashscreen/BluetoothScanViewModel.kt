package com.ventivader.ui.splashscreen.ble

import android.app.Application
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.ventivader.R
import com.ventivader.VentivaderApplication
import com.ventivader.ble.BluetoothConnectionStatus


class BluetoothScanViewModel(application: Application) : AndroidViewModel(application) {
    private var deviceID: String = ""

    private val scanCallback = BluetoothScanCallback()

    val liveData = MutableLiveData<BluetoothConnectionStatus>().apply {
        BluetoothConnectionStatus.Connecting
    }

    fun findBleDevice(deviceID: String) {

        this.deviceID = deviceID

        val bluetoothHelper = getApplication<VentivaderApplication>().bluetoothHelper
        if(bluetoothHelper.isBluetoothEnabled()) {

            BluetoothConnectionStatus.Connecting.plainConnectionStatus = getApplication<VentivaderApplication>().resources.getString(
                R.string.checking_bluetooth)

            liveData.value = BluetoothConnectionStatus.Connecting

            bluetoothHelper.scanLeDevices(scanCallback)
        }
    }

    private inner class BluetoothScanCallback : ScanCallback() {
        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)

            stopScan()

            liveData.value = BluetoothConnectionStatus.Error
        }

        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)

            result?.device?.let {

                if (result.scanRecord?.deviceName == deviceID) {

                    stopScan()

                    liveData.value = BluetoothConnectionStatus.Success(it)
                }
            }
        }

        private fun stopScan() {
            getApplication<VentivaderApplication>().bluetoothHelper.stopScan(scanCallback)
        }

    }

}