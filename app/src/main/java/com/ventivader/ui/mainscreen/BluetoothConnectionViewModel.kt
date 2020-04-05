package com.ventivader.ui.mainscreen

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ventivader.models.SolenoidParameters


class BluetoothConnectionViewModel(application: Application) : AndroidViewModel(application) {

    init {
        connectDevice()
    }

    val solenoidParametersLiveData = MutableLiveData<SolenoidParameters>().apply {
        SolenoidParameters()
    }

    fun sendSolenoidParameters() {
        // TODO - GATT write operation
        // For now, we will assume best effort.. eventually we would use blocking 'pending' modal and not
        // dismiss until either we get a notification back from rPi or timeout...
    }

    private fun connectDevice() {
        bluetoothDevice.connectGatt(getApplication(), true, )

        // TODO - Presumably upon connection we would do a GATT read operation and update liveData.
    }

    /*We need to provide a custom Factory because our ViewModel needs to be passed in a BluetoothDevice.

    Note: BluetoothDevice would be stored inside BleConnectionManager already, No need to store it here.
     */
    class BluetoothConnectionViewModelFactory(var context: Context): ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return BluetoothConnectionViewModel(context as Application) as T
        }
    }
}