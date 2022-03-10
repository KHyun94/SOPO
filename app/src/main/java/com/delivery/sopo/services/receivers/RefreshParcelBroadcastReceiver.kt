package com.delivery.sopo.services.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.delivery.sopo.exceptions.ParcelExceptionHandler
import com.delivery.sopo.enums.ErrorEnum
import com.delivery.sopo.interfaces.listener.OnSOPOErrorCallback
import com.delivery.sopo.usecase.parcel.remote.GetCompletedMonthUseCase
import com.delivery.sopo.usecase.parcel.remote.SyncParcelsUseCase
import com.delivery.sopo.util.SopoLog
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.KoinComponent
import org.koin.core.inject

class RefreshParcelBroadcastReceiver: BroadcastReceiver(), KoinComponent
{
    private val syncParcelsUseCase: SyncParcelsUseCase by inject()
    private val getCompletedMonthUseCase: GetCompletedMonthUseCase by inject()

    val exceptionHandler: CoroutineExceptionHandler by lazy {
        ParcelExceptionHandler(Dispatchers.Main, onSOPOErrorCallback)
    }

    private fun syncOngoingParcels() = CoroutineScope(Dispatchers.IO).launch {
        try
        {
            syncParcelsUseCase.invoke()
        }
        catch(e: Exception)
        {
            exceptionHandler.handleException(coroutineContext, e)
        }
    }

    private fun getCompletedMonth() = CoroutineScope(Dispatchers.IO).launch {
        try
        {
            getCompletedMonthUseCase.invoke()
        }
        catch(e: Exception)
        {
            exceptionHandler.handleException(coroutineContext, e)
        }

    }

    private fun refreshAllParcel() = CoroutineScope(Dispatchers.IO).launch {
        try
        {
            syncParcelsUseCase.invoke()
            getCompletedMonthUseCase.invoke()
        }
        catch(e: Exception)
        {
            exceptionHandler.handleException(coroutineContext, e)
        }

    }

    override fun onReceive(context: Context?, intent: Intent?)
    {
        context ?: return
        intent ?: return

        SopoLog.d("BroadCastReceiver Type:")
        if(intent.action != ACTION) return

        val type = intent.getIntExtra("TYPE", 0)

        when(type)
        {
            1 ->
            {
                syncOngoingParcels().start()
            }
            2 ->
            {
                getCompletedMonth().start()
            }
            3 ->
            {
                refreshAllParcel().start()
            }
        }
    }

    private val onSOPOErrorCallback = object: OnSOPOErrorCallback
    {
        override fun onRegisterParcelError(error: ErrorEnum)
        {
            super.onRegisterParcelError(error)

        }

        override fun onFailure(error: ErrorEnum)
        {
        }

        override fun onInternalServerError(error: ErrorEnum)
        {
            super.onInternalServerError(error)

        }

        override fun onAuthError(error: ErrorEnum)
        {
            super.onAuthError(error)

        }
    }

    companion object{
        const val ACTION = "com.delivery.sopo.ACTION_COMPLETED_REGISTER_PARCEL"
    }


}