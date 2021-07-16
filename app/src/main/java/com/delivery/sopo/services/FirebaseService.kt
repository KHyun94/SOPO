package com.delivery.sopo.services

import android.content.Intent
import com.delivery.sopo.SOPOApp
import com.delivery.sopo.enums.DeliveryStatusEnum
import com.delivery.sopo.enums.NotificationEnum
import com.delivery.sopo.models.mapper.ParcelMapper
import com.delivery.sopo.models.push.UpdatedParcelInfo
import com.delivery.sopo.networks.dto.FcmPushDTO
import com.delivery.sopo.notification.NotificationImpl
import com.delivery.sopo.data.repository.local.repository.ParcelManagementRepoImpl
import com.delivery.sopo.data.repository.local.repository.ParcelRepository
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
    private val parcelRepository: ParcelRepository by inject()
    private val parcelManagementRepo: ParcelManagementRepoImpl by inject()

    private suspend fun alertUpdateParcel(remoteMessage: RemoteMessage, intent: Intent, data: UpdatedParcelInfo)
    {
        SopoLog.d("alertUpdateParcel() call >>> ${data.updatedParcelId.size}개 업데이트 준비 중")

        val msgList = mutableListOf<String>()

        data.updatedParcelId.forEach { updateParcelDao ->

            val parcelEntity =
                parcelRepository.getLocalParcelById(updateParcelDao.parcelId) ?: return

            // DeliveryStatus 변경됐을 시 True

            if(!updateParcelDao.compareDeliveryStatus(parcelEntity)) return

            // 현재 해당 택배가 가지고 있는 배송 상태와 fcm으로 넘어온 배송상태가 다른 경우만 노티피케이션을 띄운다!
            val parcelManagementEntity = parcelManagementRepo.getEntity(updateParcelDao.parcelId)
                ?: ParcelMapper.parcelEntityToParcelManagementEntity(
                    updateParcelDao.getParcel() ?: return)

            parcelManagementEntity.run {

                SopoLog.d("parcelManagementEntity Update>>> ")

                updatableStatus = 1
                auditDte = TimeUtil.getDateTime()

                // 배송 중 -> 배송완료]가 됐다면 앱을 켰을때 몇개가 수정되었는지 보여줘야하기 때문에 save해서 저장함.
                if(updateParcelDao.deliveryStatus == DeliveryStatusEnum.DELIVERED.CODE) deliveredStatus =
                    1

                SopoLog.d("""
                        parcelManagement >>> 
                        isBeUpdate = ${updatableStatus}
                        auditDte = ${auditDte}
                        deliveredStatus = ${deliveredStatus}
                    """.trimIndent())
            }

            withContext(Dispatchers.Default) {
                parcelManagementRepo.insertEntity(parcelManagementEntity)
            }

            with(updateParcelDao.getMessage(parcelEntity)) {
                SopoLog.d("Noti Msg >>> $this")
                msgList.add(this)
            }
        }

        msgList.forEach {
            NotificationImpl.alertUpdateParcel(remoteMessage = remoteMessage,
                                               context = applicationContext, intent = intent,
                                               message = *arrayOf(it))
        }

        SOPOApp.cntOfBeUpdate.postValue(data.updatedParcelId.size)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage)
    {
        if(remoteMessage.data.isEmpty()) return SopoLog.e("omMessageReceived[data is null]")

        SopoLog.d(msg = "onMessageReceived[${remoteMessage.data.toString()}]")

        val notificationId = remoteMessage.data.getValue("notificationId")
        val data = remoteMessage.data.getValue("data")

        val fcmPushDto = FcmPushDTO(notificationId, data)

        SopoLog.d("FcmPushDTO[${fcmPushDto.toString()}")

        when(fcmPushDto.notificationId)
        {
            // 사용자에게 택배 상태가 업데이트되었다고 알려줌
            NotificationEnum.PUSH_UPDATE_PARCEL.notificationId ->
            {
                val updatedParcelList = fcmPushDto.getUpdateParcel()

                SopoLog.d("""
                    updatedParcelList
                    ${updatedParcelList.updatedParcelId.joinToString()}
                """.trimIndent())

                if(updatedParcelList.updatedParcelId.isEmpty()) return

                /**
                 * TODO 해당 처리는 번들로 들어오는 데이터를 구분해서 전역으로 데이터를 들고 있다가
                 *  선택한 업데이트 노티에 해당하 택배 상세 페이지로 이동
                 */
                CoroutineScope(Dispatchers.IO).launch {
                    alertUpdateParcel(remoteMessage,
                                      Intent(this@FirebaseService, SplashView::class.java),
                                      updatedParcelList)
                }

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
                NotificationImpl.awakenDeviceNoti(remoteMessage = remoteMessage,
                                                  context = applicationContext,
                                                  intent = Intent(this, SplashView::class.java))
                SOPOWorkManager.updateWorkManager(applicationContext)
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