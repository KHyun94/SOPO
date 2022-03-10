package com.delivery.sopo.services.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.delivery.sopo.exceptions.ParcelExceptionHandler
import com.delivery.sopo.enums.ErrorEnum
import com.delivery.sopo.interfaces.listener.OnSOPOErrorCallback
import com.delivery.sopo.usecase.LogoutUseCase
import com.delivery.sopo.usecase.parcel.remote.GetCompletedMonthUseCase
import com.delivery.sopo.usecase.parcel.remote.SyncParcelsUseCase
import com.delivery.sopo.util.SopoLog
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.KoinComponent
import org.koin.core.inject

class LogOutBroadcastReceiver: BroadcastReceiver(), KoinComponent
{
    private val logoutUseCase: LogoutUseCase by inject()

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
                logoutUseCase.invoke()
            }
            2 ->
            {
//                getCompletedMonth().start()
            }

        }
    }

    companion object{
        const val ACTION = "com.delivery.sopo.ACTION_LOGOUT"
    }


}