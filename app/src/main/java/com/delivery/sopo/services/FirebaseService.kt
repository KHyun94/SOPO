package com.delivery.sopo.services

import android.content.Intent
import android.util.Log
import com.delivery.sopo.enums.DeliveryStatusEnum
import com.delivery.sopo.enums.NotificationEnum
import com.delivery.sopo.mapper.ParcelMapper
import com.delivery.sopo.networks.dto.FcmPushDTO
import com.delivery.sopo.notification.NotificationImpl
import com.delivery.sopo.repository.impl.ParcelManagementRepoImpl
import com.delivery.sopo.repository.impl.ParcelRepoImpl
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.util.TimeUtil
import com.delivery.sopo.views.splash.SplashView
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
    private val parcelManagementRepo: ParcelManagementRepoImpl by inject()

    var TAG = "LOG.SOPO.FCM"

    private fun alertUpdateParcel(remoteMessage: RemoteMessage, intent: Intent, fcmPushDto: FcmPushDTO){
        CoroutineScope(Dispatchers.IO).launch {

            // 업데이트 사항이 있는 택배를 로컬 DB에서 조회
           val localOngoingParcels = parcelRepo.getLocalParcelById(fcmPushDto.regDt, fcmPushDto.parcelUid)

            SopoLog.d("Update Parcel Data => ${localOngoingParcels?:"조회 결과 없음."}", "FirebaseService")

            // 만약에.. 내부 데이터베이스에 검색된 택배가 없다면.. 알람을 띄우지 않는다.
            localOngoingParcels?.let {

                // 현재 해당 택배가 가지고 있는 배송 상태와 fcm으로 넘어온 배송상태가 다른 경우만 노티피케이션을 띄운다!
                if(it.deliveryStatus != fcmPushDto.deliveryStatus && it.status == 1){
                    parcelManagementRepo.getEntity(fcmPushDto.regDt, fcmPushDto.parcelUid)?.let { entity ->
                            // 기본적으로 fcm으로 데이터가 업데이트 됐다고 수신 받은것이니 isBeUpdate를 1로 save해서 앱에 차후에 업데이트 해야함을 알림.
                            entity.apply {
                                isBeUpdate = 1
                                auditDte = TimeUtil.getDateTime()
                            }
                            // 배송 중 -> 배송완료가 됐다면 앱을 켰을때 몇개가 수정되었는지 보여줘야하기 때문에 save해서 저장함.
                            if(fcmPushDto.deliveryStatus == DeliveryStatusEnum.delivered.code){
                                entity.apply { isBeDelivered = 1 }
                            }
                            parcelManagementRepo.insertEntity(entity)
                    }

                    NotificationImpl.alertUpdateParcel(
                        remoteMessage = remoteMessage,
                        context = applicationContext,
                        intent = intent,
                        parcel = ParcelMapper.parcelEntityToParcel(it),
                        newDeliveryStatus = fcmPushDto.deliveryStatus
                    )
                }
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