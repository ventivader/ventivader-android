package com.ventivader.ui.ble

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context


class BluetoothHelper(private val application: Application) {

    private val bluetoothAdapter: BluetoothAdapter? by lazy(LazyThreadSafetyMode.NONE) {
        val bluetoothManager = application.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    fun isBluetoothEnabled() : Boolean {
        return bluetoothAdapter?.isEnabled == true
    }

    fun isBluetoothDisabled() : Boolean {
        return bluetoothAdapter?.isEnabled == false
    }

    fun scanLeDevices(scanCallback: ScanCallback) {
        if(isBluetoothEnabled()) {
            stopScan(scanCallback)

            bluetoothAdapter?.bluetoothLeScanner?.startScan(scanCallback)
        }
    }

    fun stopScan(scanCallback: ScanCallback) {
        bluetoothAdapter?.bluetoothLeScanner?.stopScan(scanCallback)
    }
}