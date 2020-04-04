package com.ventivader

import android.app.Application
import com.ventivader.ui.ble.BluetoothHelper


class VentivaderApplication : Application() {

    lateinit var application: Application
    lateinit var bluetoothHelper: BluetoothHelper

    override fun onCreate() {
        super.onCreate()

        application = this
        bluetoothHelper = BluetoothHelper(this)
    }
}