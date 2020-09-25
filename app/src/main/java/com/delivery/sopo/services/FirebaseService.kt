package com.delivery.sopo.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.delivery.sopo.R
import com.delivery.sopo.enums.NotificationEnum
import com.delivery.sopo.mapper.ParcelMapper
import com.delivery.sopo.models.dto.FcmPushDTO
import com.delivery.sopo.notification.NotificationImpl
import com.delivery.sopo.repository.ParcelRepoImpl
import com.delivery.sopo.views.SplashView
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.lang.Exception

class FirebaseService: FirebaseMessagingService()
{
    private val parcelRepo: ParcelRepoImpl by inject()
    var TAG = "LOG.SOPO.FCM"

    private fun alertUpdateParcel(remoteMessage: RemoteMessage, intent: Intent, fcmPushDto: FcmPushDTO){
        CoroutineScope(Dispatchers.IO).launch {
            val localOngoingParcels = parcelRepo.getLocalParcelById(fcmPushDto.regDt, fcmPushDto.parcelUid)
            Log.d(TAG, "CoroutineScope`s parcel list : $localOngoingParcels")
            // 만약에.. 내부 데이터베이스에 검색된 택배가 없다면.. 알람을 띄우지 않는다.
            localOngoingParcels?.let {
                NotificationImpl.alertUpdateParcel(
                    remoteMessage = remoteMessage,
                    context = applicationContext,
                    intent = intent,
                    parcel = ParcelMapper.entityToParcel(localOngoingParcels)
                )
            }
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage){
        if (remoteMessage.data.isNotEmpty())
        {
            Log.d(TAG, "onMessageReceived: " + remoteMessage.data.toString())
            Log.d(TAG, "remoteMessage : ${remoteMessage.notification}")
            val fcmPushDto = Gson().fromJson(remoteMessage.data.toString(), FcmPushDTO::class.java)
            Log.d(TAG, "fromJson : $fcmPushDto")
            when (fcmPushDto.notificationId)
            {
                // 사용자에게 택배 상태가 업데이트되었다고 알려줌
                NotificationEnum.PUSH_UPDATE_PARCEL.notificationId ->
                {
                    alertUpdateParcel(
                        remoteMessage,
                        Intent(this, SplashView::class.java),
                        fcmPushDto
                    )
                }
                // 친구 추천
                NotificationEnum.PUSH_FRIEND_RECOMMEND.notificationId ->
                {
                    // Nothing to do yet..
                }
                // 전체 공지사항
                NotificationEnum.PUSH_FRIEND_RECOMMEND.notificationId ->
                {
                    // Nothing to do yet..
                }
            }
        }
    }

    override fun onDeletedMessages(){
        super.onDeletedMessages()
    }

    override fun onMessageSent(p0: String){
        super.onMessageSent(p0)
    }

    override fun onSendError(p0: String, p1: Exception){
        super.onSendError(p0, p1)
    }

    override fun onNewToken(s: String){
        super.onNewToken(s)
        Log.d(TAG, "onNewToken: $s")
        sendTokenToServer(s)
    }

    private fun sendTokenToServer(token: String){
        Log.d(TAG, "sendTokenToServer: $token")
    }
}