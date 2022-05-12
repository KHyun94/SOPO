package com.delivery.sopo.services.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.delivery.sopo.exceptions.ParcelExceptionHandler
import com.delivery.sopo.enums.ErrorEnum
import com.delivery.sopo.firebase.FirebaseRepository
import com.delivery.sopo.interfaces.listener.OnSOPOErrorCallback
import com.delivery.sopo.domain.usecase.parcel.remote.GetCompletedMonthUseCase
import com.delivery.sopo.domain.usecase.parcel.remote.GetParcelUseCase
import com.delivery.sopo.domain.usecase.parcel.remote.SyncParcelsUseCase
import com.delivery.sopo.util.SopoLog
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.KoinComponent
import org.koin.core.inject

class RefreshParcelBroadcastReceiver: BroadcastReceiver(), KoinComponent
{
    private val getParcelUseCase: GetParcelUseCase by inject()
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

        SopoLog.d("BroadCastReceiver Type:${intent.action}")

        when(intent.action)
        {
            COMPLETE_REGISTER_ACTION ->
            {
                FirebaseRepository.subscribedTopic(isForce = true)

                val parcelId  = intent.getIntExtra("PARCEL_ID", 0)

                CoroutineScope(Dispatchers.IO).launch {
                    val parcel = getParcelUseCase.invoke(parcelId= parcelId)
                    SopoLog.d("업데이트 택배:${parcel.toString()}")
                }
            }
        }
    }

    val onSOPOErrorCallback = object: OnSOPOErrorCallback
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
        const val COMPLETE_REGISTER_ACTION = "com.delivery.sopo.ACTION_COMPLETED_REGISTER_PARCEL"
//        const val ACTION = "com.delivery.sopo.ACTION_COMPLETED_REGISTER_PARCEL"
//        const val ACTION = "com.delivery.sopo.ACTION_COMPLETED_REGISTER_PARCEL"
//        const val ACTION = "com.delivery.sopo.ACTION_COMPLETED_REGISTER_PARCEL"
    }


}