package com.delivery.sopo.services

import android.content.Intent
import com.delivery.sopo.database.room.AppDatabase
import com.delivery.sopo.enums.DeliveryStatusEnum
import com.delivery.sopo.enums.NotificationEnum
import com.delivery.sopo.mapper.ParcelMapper
import com.delivery.sopo.models.push.UpdateParcelDao
import com.delivery.sopo.networks.dto.FcmPushDTO
import com.delivery.sopo.notification.NotificationImpl
import com.delivery.sopo.repository.impl.ParcelManagementRepoImpl
import com.delivery.sopo.repository.impl.ParcelRepoImpl
import com.delivery.sopo.services.workmanager.SOPOWorkManager
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.util.TimeUtil
import com.delivery.sopo.views.splash.SplashView
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.lang.Exception

class FirebaseService: FirebaseMessagingService()
{
    private val parcelRepo: ParcelRepoImpl by inject()
    private val parcelManagementRepo: ParcelManagementRepoImpl by inject()
    private val appDatabase : AppDatabase by inject()

    var TAG = "FirebaseService"

    private fun alertUpdateParcel(remoteMessage: RemoteMessage, intent: Intent, updateParcelDao: UpdateParcelDao){
        CoroutineScope(Dispatchers.IO).launch {

            // 업데이트 사항이 있는 택배를 로컬 DB에서 조회

            SopoLog.d("Update Parcel Data => ${updateParcelDao?:"조회 결과 없음."}")

            // 만약에.. 내부 데이터베이스에 검색된 택배가 없다면.. 알람을 띄우지 않는다.
            updateParcelDao.compareDeliveryStatus {value ->

                // 현재 해당 택배가 가지고 있는 배송 상태와 fcm으로 넘어온 배송상태가 다른 경우만 노티피케이션을 띄운다!
                if(value)
                {
                    parcelManagementRepo.let { repoImpl ->

                        val parcelManagementEntity = parcelManagementRepo.getEntity(updateParcelDao.regDt, updateParcelDao.parcelUid)

                        if(parcelManagementEntity != null)
                        {
                            // 배송 상태가 변경 여부를 저
                            parcelManagementEntity.apply {
                                isBeUpdate = 1
                                auditDte = TimeUtil.getDateTime()
                            }

                            // 배송 중 -> 배송완료가 됐다면 앱을 켰을때 몇개가 수정되었는지 보여줘야하기 때문에 save해서 저장함.
                            if(updateParcelDao.deliveryStatus == DeliveryStatusEnum.DELIVERED.CODE){
                                parcelManagementEntity.apply { isBeDelivered = 1 }
                            }

                            CoroutineScope(Dispatchers.Default).launch {
                                repoImpl.updateEntity(parcelManagementEntity)
                            }
                        }
                        else
                        {
                            CoroutineScope(Dispatchers.Default).launch {

                                val parcelEntity = updateParcelDao.getParcel()?:return@launch
                                val parcel = ParcelMapper.parcelEntityToParcel(parcelEntity)
                                val parcelManagementEntity = ParcelMapper.parcelToParcelManagementEntity(parcel)

                                parcelManagementEntity.apply {
                                    isBeUpdate = 1
                                    auditDte = TimeUtil.getDateTime()
                                }

                                // 배송 중 -> 배송완료가 됐다면 앱을 켰을때 몇개가 수정되었는지 보여줘야하기 때문에 save해서 저장함.
                                if(updateParcelDao.deliveryStatus == DeliveryStatusEnum.DELIVERED.CODE){
                                    parcelManagementEntity.apply { isBeDelivered = 1 }
                                }

                                repoImpl.insertEntity(parcelManagementEntity)
                            }
                        }
                    }

                    NotificationImpl.alertUpdateParcel(
                        remoteMessage = remoteMessage,
                        context = applicationContext,
                        intent = intent,
                        message = updateParcelDao.getMessage()
                    )
                }
            }
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage){

        if (remoteMessage.data.isNotEmpty())
        {
            SopoLog.d( msg = "onMessageReceived: " + remoteMessage.data.toString())

            val notificationId = remoteMessage.data.getValue("notificationId")
            val data = remoteMessage.data.getValue("data")

            val fcmPushDto = FcmPushDTO(notificationId, data)

            when (fcmPushDto.notificationId)
            {
                // 사용자에게 택배 상태가 업데이트되었다고 알려줌
                NotificationEnum.PUSH_UPDATE_PARCEL.notificationId ->
                {
                    val updateParcelDao = fcmPushDto.setUpdateParcel()

                    alertUpdateParcel(
                        remoteMessage,
                        Intent(this, SplashView::class.java),
                        updateParcelDao
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
                NotificationEnum.PUSH_AWAKEN_DEVICE.notificationId ->
                {
                    NotificationImpl.awakenDeviceNoti(
                        remoteMessage = remoteMessage,
                        context = applicationContext,
                        intent = Intent(this, SplashView::class.java)
                    )
                    SOPOWorkManager.updateWorkManager(applicationContext)
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
        SopoLog.d( msg = "onNewToken: $s")
        sendTokenToServer(s)
    }

    private fun sendTokenToServer(token: String){
        SopoLog.d( msg = "sendTokenToServer: $token")
    }
}