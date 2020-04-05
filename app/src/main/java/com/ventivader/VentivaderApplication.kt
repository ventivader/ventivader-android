package com.ventivader

import android.app.Application
import com.ventivader.ble.BleConnectionManager


class VentivaderApplication : Application() {

    lateinit var application: Application
    lateinit var bleConnectionManager: BleConnectionManager

    override fun onCreate() {
        super.onCreate()

        application = this
        bleConnectionManager = BleConnectionManager(this)
    }
}