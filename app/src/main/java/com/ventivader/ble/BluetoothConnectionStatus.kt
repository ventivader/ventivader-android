package com.ventivader.ble

import android.bluetooth.BluetoothDevice


sealed class BluetoothConnectionStatus(var plainConnectionStatus: String) {

    object Connecting : BluetoothConnectionStatus("Connecting")

    class Success(val bluetoothDevice: BluetoothDevice) : BluetoothConnectionStatus("Found device ")

    object Error : BluetoothConnectionStatus("Error finding the device")
}