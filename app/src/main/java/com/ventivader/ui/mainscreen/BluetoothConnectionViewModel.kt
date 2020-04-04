package com.ventivader.ui.mainscreen

import android.app.Application
import android.bluetooth.BluetoothDevice
import androidx.lifecycle.AndroidViewModel


class BluetoothConnectionViewModel(application: Application) : AndroidViewModel(application) {

    fun connectDevice(device : BluetoothDevice) {
        // device.connectGatt(getApplication(), true, )
    }

}