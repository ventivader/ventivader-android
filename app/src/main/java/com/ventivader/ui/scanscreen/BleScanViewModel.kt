package com.ventivader.ui.scanscreen

import android.app.Application
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.ventivader.R
import com.ventivader.VentivaderApplication
import com.ventivader.ble.BluetoothConnectionStatus


class BleScanViewModel(application: Application) : AndroidViewModel(application) {

    private val scanCallback = BluetoothScanCallback()
    private val gattCallback = GattCallback()

    val liveData = MutableLiveData<BluetoothConnectionStatus>().apply {
        BluetoothConnectionStatus.Connecting
    }

    fun findAndConnectBleDevice() {

        val bluetoothConnectionManager = bluetoothConnectionManager()
        if(bluetoothConnectionManager.isBluetoothEnabled()) {

            BluetoothConnectionStatus.Connecting.plainConnectionStatus = getApplication<VentivaderApplication>().resources.getString(
                R.string.checking_bluetooth)

            liveData.value = BluetoothConnectionStatus.Connecting

            bluetoothConnectionManager.scanLeDevices(scanCallback)
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

                stopScan()

                connectToGattServer(it)
            }
        }

        private fun stopScan() {
            bluetoothConnectionManager().stopScan(scanCallback)
        }
    }

    private fun connectToGattServer(device: BluetoothDevice) {
        bluetoothConnectionManager().connectToGattServer(device, gattCallback)
    }

    private inner class GattCallback : BluetoothGattCallback() {

        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {

            when (newState) {

                BluetoothProfile.STATE_CONNECTED -> {

                    liveData.postValue(BluetoothConnectionStatus.Success(gatt?.device?.name))
                }

                BluetoothProfile.STATE_DISCONNECTED -> {

                    liveData.postValue(BluetoothConnectionStatus.Error)
                }
            }
        }
    }

    private fun bluetoothConnectionManager() =
        getApplication<VentivaderApplication>().bleConnectionManager


}