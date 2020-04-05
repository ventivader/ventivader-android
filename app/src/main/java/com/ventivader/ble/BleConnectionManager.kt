package com.ventivader.ble

import android.app.Application
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.ParcelUuid
import android.util.Log
import java.util.*


class BleConnectionManager(private val application: Application) {

    private val gattConnectionCallback = ConnectionCallback()

    private var gatt: BluetoothGatt? = null

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

    fun connectToGattServer(device: BluetoothDevice, listener: BluetoothGattCallback) {

        gattConnectionCallback.callback = listener

        device.connectGatt(application, false, gattConnectionCallback)
    }

    private inner class ConnectionCallback : BluetoothGattCallback() {

        var callback: BluetoothGattCallback? = null

        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)

            when (newState) {

                BluetoothProfile.STATE_CONNECTED -> {
                    Log.i(TAG, "Connected to GATT server.")

                    this@BleConnectionManager.gatt = gatt
                }

                BluetoothProfile.STATE_DISCONNECTED -> {
                    Log.i(TAG, "Disconnected from GATT server.")
                }
            }

            callback?.onConnectionStateChange(gatt, status, newState)
        }
    }

    fun stopScan(scanCallback: ScanCallback) {
        bluetoothAdapter?.bluetoothLeScanner?.stopScan(scanCallback)
    }

    companion object {
        const val VENTI_VADER_SERVICE_UUID = "2E70DF6A-7FAB-44A4-9B20-C12F5D1E726C"
        const val TAG = "BleConnectionManager"
    }
}
