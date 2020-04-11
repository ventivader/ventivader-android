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
import java.nio.charset.Charset
import java.util.*


class BleConnectionManager(private val application: Application) {

    private val gattConnectionCallback = ConnectionCallback()

    // If defined, this BluetoothGatt will be connected.
    private var connectedGATT: BluetoothGatt? = null

    private val solenoidParameterCharacteristicUUID = UUID.fromString(SOLENOID_PARAMETERS_CHARACTERISTIC_UUID)
    private var readBleGattCharacteristic: BluetoothGattCharacteristic? = null

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
                BluetoothGatt.GATT_SUCCESS -> {
                    readSolenoidParameterCharacteristic()
                }
                else -> Log.e(TAG, "onServicesDiscovered received: $status")
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            super.onCharacteristicRead(gatt, characteristic, status)

            when (status) {
                BluetoothGatt.GATT_SUCCESS -> {
                    characteristic?.let {

                        buildAndPublishSolenoidModel(it)

                        readBleGattCharacteristic = it
                    }
                }
                else -> Log.e(TAG, "onCharacteristicRead : $status")
            }
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            super.onCharacteristicWrite(gatt, characteristic, status)

            when (status) {
                BluetoothGatt.GATT_SUCCESS -> {
                    characteristic?.let {

                        if (it.uuid == solenoidParameterCharacteristicUUID) {
                            Log.d(TAG, "onCharacteristicWritten : ${characteristic.value.toString(Charset.defaultCharset())}")
                        }
                    }
                }
                else -> Log.e(TAG, "onCharacteristicWrite : $status")
            }
        }
    }

    private fun buildAndPublishSolenoidModel(it: BluetoothGattCharacteristic) {
        if (it.uuid == solenoidParameterCharacteristicUUID) {
            val solenoidParameters = SolenoidParameters()
            it.value.toString(Charset.defaultCharset()).split("|").forEachIndexed { index, value ->
                when (index) {
                    INHALE_SEC_INDEX -> {
                        solenoidParameters.inhaleSec = value.toInt()
                    }

                    INHALE_HOLD_SEC_INDEX -> {
                        solenoidParameters.inhaleHoldSec = value.toInt()
                    }

                    EXHALE_SEC_INDEX -> {
                        solenoidParameters.exhaleSec = value.toInt()
                    }

                    EXHALE_HOLD_SEC_INDEX -> {
                        solenoidParameters.exhaleHoldSec = value.toInt()
                    }

                    VENTILATION_INDEX -> {
                        solenoidParameters.numberVentCycles = value.toInt()
                    }
                }
            }

            solenoidParametersCallback(solenoidParameters, null)

        }
    }

    fun writeSolenoidParameter(parameters: SolenoidParameters) {
        solenoidCharacteristic()?.let {
            it.value = parameters.toString().toByteArray(Charset.defaultCharset())

            connectedGATT?.writeCharacteristic(it)
        }
    }

    private fun readSolenoidParameterCharacteristic() {

        solenoidCharacteristic()?.let {

            val solenoidParametersCharacteristic = it.toString()
            Log.d(TAG, "Found Characteristic: '$solenoidParametersCharacteristic'")

            connectedGATT?.readCharacteristic(it)

        }?: solenoidParametersCallback(null, Exception("Expected BLE characteristic NOT found!"))
    }

    private fun solenoidCharacteristic() : BluetoothGattCharacteristic? =
        bluetoothGattService()?.getCharacteristic(solenoidParameterCharacteristicUUID)

    private fun bluetoothGattService() =
        connectedGATT?.getService(UUID.fromString(VENTI_VADER_SERVICE_UUID))

    fun stopScan(scanCallback: ScanCallback) {
        bluetoothAdapter?.bluetoothLeScanner?.stopScan(scanCallback)
    }

    fun close() {
        connectedGATT?.close()
    }

    companion object {
        const val VENTI_VADER_SERVICE_UUID = "6e400001-b5a3-f393-e0a9-e50e24dcca9e"
        const val SOLENOID_PARAMETERS_CHARACTERISTIC_UUID = "6e400002-b5a3-f393-e0a9-e50e24dcca9e"
        const val TAG = "BleConnectionManager"
        const val INHALE_SEC_INDEX = 0
        const val INHALE_HOLD_SEC_INDEX = 1
        const val EXHALE_SEC_INDEX = 2
        const val EXHALE_HOLD_SEC_INDEX = 3
        const val VENTILATION_INDEX = 4
    }
}
