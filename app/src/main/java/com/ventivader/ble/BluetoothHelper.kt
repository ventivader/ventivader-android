package com.ventivader.ui.ble

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.ParcelUuid
import java.util.*


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

            val filter = ScanFilter.Builder()
                .setServiceUuid(ParcelUuid(UUID.fromString(VENTI_VADER_SERVICE_UUID)))
                .build()

            bluetoothAdapter?.bluetoothLeScanner?.startScan(listOf(filter),
                ScanSettings.Builder().build(), scanCallback)
        }
    }

    fun stopScan(scanCallback: ScanCallback) {
        bluetoothAdapter?.bluetoothLeScanner?.stopScan(scanCallback)
    }

    companion object {
        const val VENTI_VADER_SERVICE_UUID = "2E70DF6A-7FAB-44A4-9B20-C12F5D1E726C"
    }
}