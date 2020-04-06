package com.ventivader.ui.mainscreen

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.ventivader.VentivaderApplication
import com.ventivader.models.SolenoidParameters


class BLEConnectionViewModel(application: Application) : AndroidViewModel(application) {

    init {
        getApplication<VentivaderApplication>().bleConnectionManager
            .retrieveSolenoidParameters { solenoidParameters, exception ->
                solenoidParameters?.let {
                    solenoidParametersLiveData.postValue(solenoidParameters)
                }?: let {
                    Log.e(TAG, "Exception while retrieving solenoidParameters.", exception)
                }
        }

        // NJD TODO - listen for Notifications from the Service
    }

    val solenoidParametersLiveData = MutableLiveData<SolenoidParameters>().apply {
        SolenoidParameters()
    }

    fun sendSolenoidParameters() {
        // TODO - GATT write operation
        // For now, we will assume best effort.. eventually we would use blocking 'pending' modal and not
        // dismiss until either we get a notification back from rPi or timeout...
    }

    companion object {
        const val TAG = "BLEConnectionViewModel"
    }
}