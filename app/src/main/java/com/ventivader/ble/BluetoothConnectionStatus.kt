package com.ventivader.ble


sealed class BluetoothConnectionStatus(var plainConnectionStatus: String) {

    object Connecting : BluetoothConnectionStatus("Connecting")

    class Success(name: String? = "") : BluetoothConnectionStatus("Found device $name")

    object Error : BluetoothConnectionStatus("Error finding the device")
}