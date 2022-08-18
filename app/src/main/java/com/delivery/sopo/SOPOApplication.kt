package com.delivery.sopo

import android.app.Application
import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.delivery.sopo.enums.NetworkStatus

class SOPOApplication: Application()
{
    override fun onCreate()
    {
        super.onCreate()

        INSTANCE = this@SOPOApplication
    }

    companion object
    {
        lateinit var INSTANCE: Context

        val networkStatus: MutableLiveData<NetworkStatus> by lazy {  MutableLiveData<NetworkStatus>()  }
        var cntOfBeUpdate: MutableLiveData<Int> = MutableLiveData<Int>()
    }
}