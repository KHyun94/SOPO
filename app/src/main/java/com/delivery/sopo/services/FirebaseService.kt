package com.delivery.sopo.services

import android.content.Intent
import com.delivery.sopo.database.room.AppDatabase
import com.delivery.sopo.enums.DeliveryStatusEnum
import com.delivery.sopo.enums.NotificationEnum
import com.delivery.sopo.mapper.ParcelMapper
import com.delivery.sopo.models.parcel.ParcelId
import com.delivery.sopo.models.push.UpdateParcelDao
import com.delivery.sopo.models.push.UpdatedParcelInfo
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
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import java.lang.Exception

class FirebaseService: FirebaseMessagingService()
{
    private val parcelRepoImpl: ParcelRepoImpl by inject()
    private val parcelManagementRepo: ParcelManagementRepoImpl by inject()

    private fun alertUpdateParcel(remoteMessage: RemoteMessage, intent: Intent, data: UpdatedParcelInfo)
    {
        SopoLog.d("alertUpdateParcel() call >>> ${data.updatedParcelId.size}개 업데이트 준비 중")

        val cntOfUpdatedParcel = data.updatedParcelId.size

        if(cntOfUpdatedParcel == 0) return

        CoroutineScope(Dispatchers.IO).launch {

            val msgList = mutableListOf<String>()

            data.updatedParcelId.forEach {updateParcelDao ->

                val parcelEntity = parcelRepoImpl.getLocalParcelById(ParcelId(updateParcelDao.regDt, updateParcelDao.parcelUid)) ?: return@launch

                // DeliveryStatus 변경됐을 시 True
                val isChange = updateParcelDao.compareDeliveryStatus(parcelEntity)

                if (!isChange) return@launch

                // 현재 해당 택배가 가지고 있는 배송 상태와 fcm으로 넘어온 배송상태가 다른 경우만 노티피케이션을 띄운다!
                val parcelManagementEntity = parcelManagementRepo.getEntity(updateParcelDao.getParcelId()) ?: ParcelMapper.parcelEntityToParcelManagementEntity(updateParcelDao.getParcel() ?: return@launch).apply {

                    isBeUpdate = 1
                    auditDte = TimeUtil.getDateTime()

                    // 배송 중 -> 배송완료가 됐다면 앱을 켰을때 몇개가 수정되었는지 보여줘야하기 때문에 save해서 저장함.
                    if (updateParcelDao.deliveryStatus == DeliveryStatusEnum.DELIVERED.CODE) isBeDelivered = 1
                }

                withContext(Dispatchers.Default) { parcelManagementRepo.insertEntity(parcelManagementEntity) }

                with(updateParcelDao.getMessage(parcelEntity)){
                    SopoLog.d("Noti Msg >>> $this")
                    msgList.add(this)
                }
            }

            msgList.forEach {
                NotificationImpl.alertUpdateParcel(remoteMessage = remoteMessage, context = applicationContext, intent = intent, message = *arrayOf(it))
            }

        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage)
    {

        if (remoteMessage.data.isNotEmpty())
        {
            SopoLog.d(msg = "onMessageReceived: " + remoteMessage.data.toString())

            val notificationId = remoteMessage.data.getValue("notificationId")
            val data = remoteMessage.data.getValue("data")

            val fcmPushDto = FcmPushDTO(notificationId, data)

            when (fcmPushDto.notificationId)
            {
                // 사용자에게 택배 상태가 업데이트되었다고 알려줌
                NotificationEnum.PUSH_UPDATE_PARCEL.notificationId ->
                {
                    val updatedParcelList = fcmPushDto.getUpdateParcel()

                    SopoLog.d("${NotificationEnum.PUSH_UPDATE_PARCEL}")

                    // TODO 업데이트 조회 화면으로 이동
                    alertUpdateParcel(remoteMessage, Intent(this, SplashView::class.java), updatedParcelList)
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
                        remoteMessage = remoteMessage, context = applicationContext, intent = Intent(this, SplashView::class.java)
                    )
                    SOPOWorkManager.updateWorkManager(applicationContext)
                }
            }
        }
    }

    override fun onDeletedMessages()
    {
        super.onDeletedMessages()
    }

    override fun onMessageSent(p0: String)
    {
        super.onMessageSent(p0)
    }

    override fun onSendError(p0: String, p1: Exception)
    {
        super.onSendError(p0, p1)
    }

    override fun onNewToken(s: String)
    {
        super.onNewToken(s)
        SopoLog.d(msg = "onNewToken: $s")
        sendTokenToServer(s)
    }

    private fun sendTokenToServer(token: String)
    {
        SopoLog.d(msg = "sendTokenToServer: $token")
    }
}