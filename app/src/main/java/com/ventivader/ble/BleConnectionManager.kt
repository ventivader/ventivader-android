package com.ventivader.ble

import android.app.Application
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.ParcelUuid
import android.util.Log
import com.ventivader.models.SolenoidParameters
import java.util.*


class BleConnectionManager(private val application: Application) {

    private val gattConnectionCallback = ConnectionCallback()

    // If defined, this BluetoothGatt will be connected.
    private var connectedGATT: BluetoothGatt? = null

    // If defined, represents a pending BLE Chararacteristics request
    private lateinit var solenoidParametersCallback: ((SolenoidParameters?, Exception?) -> Unit)

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

    fun retrieveSolenoidParameters(_solenoidParametersCallback: (SolenoidParameters?, Exception?) -> Unit) {

        solenoidParametersCallback = _solenoidParametersCallback
        connectedGATT?.discoverServices()
    }

    private inner class ConnectionCallback : BluetoothGattCallback() {

        var callback: BluetoothGattCallback? = null

        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)

            when (newState) {

                BluetoothProfile.STATE_CONNECTED -> {
                    Log.i(TAG, "Connected to GATT server.")

                    this@BleConnectionManager.connectedGATT = gatt
                }

                BluetoothProfile.STATE_DISCONNECTED -> {
                    Log.i(TAG, "Disconnected from GATT server.")
                }
            }

            callback?.onConnectionStateChange(gatt, status, newState)
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)

            when (status) {
                BluetoothGatt.GATT_SUCCESS -> readSolenoidParameterCharacteristic()
                else -> Log.e(TAG, "onServicesDiscovered received: $status")
            }
        }
    }

    private fun readSolenoidParameterCharacteristic() {
        val bluetoothGATTService = connectedGATT!!.getService(UUID.fromString(VENTI_VADER_SERVICE_UUID))

        bluetoothGATTService.getCharacteristic(UUID.fromString(SOLENOID_PARAMETERS_CHARACTERISTIC_UUID))?.let {

            var solenoidParametersCharacteristic = it.toString()
            Log.d(TAG, "Found Characteristic: '$solenoidParametersCharacteristic'")

            // NJD TODO - need to parse above String
            var solenoidParameters = SolenoidParameters(3,3,3,3,3)

            solenoidParametersCallback(solenoidParameters, null)
        }?:let {
            solenoidParametersCallback(null, Exception("Expected BLE characteristic NOT found!"))
        }
    }

    fun stopScan(scanCallback: ScanCallback) {
        bluetoothAdapter?.bluetoothLeScanner?.stopScan(scanCallback)
    }

    companion object {
        const val VENTI_VADER_SERVICE_UUID = "2E70DF6A-7FAB-44A4-9B20-C12F5D1E726C"
        const val SOLENOID_PARAMETERS_CHARACTERISTIC_UUID = "2E70DF6B-7FAB-44A4-9B20-C12F5D1E726C"
        const val TAG = "BleConnectionManager"
    }
}
