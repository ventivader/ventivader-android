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

    override fun onCleared() {
        super.onCleared()

        getApplication<VentivaderApplication>().bleConnectionManager.close()
    }

    val solenoidParametersLiveData = MutableLiveData<SolenoidParameters>().apply {
        SolenoidParameters()
    }

    fun sendSolenoidParameters(parameter: SolenoidParameters) {
        getApplication<VentivaderApplication>().bleConnectionManager
            .writeSolenoidParameter(parameter)
    }

    companion object {
        const val TAG = "BLEConnectionViewModel"
    }
}