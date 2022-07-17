package com.delivery.sopo.presentation.services

import android.content.Intent
import com.delivery.sopo.enums.DeliveryStatusEnum
import com.delivery.sopo.enums.NotificationEnum
import com.delivery.sopo.data.models.FcmPushDTO
import com.delivery.sopo.notification.NotificationImpl
import com.delivery.sopo.data.repositories.local.repository.ParcelManagementRepoImpl
import com.delivery.sopo.data.repositories.local.repository.ParcelRepository
import com.delivery.sopo.models.parcel.Parcel
import com.delivery.sopo.models.push.NotificationMessage
import com.delivery.sopo.presentation.services.workmanager.SOPOWorkManager
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.util.TimeUtil
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

    override fun onMessageReceived(remoteMessage: RemoteMessage)
    {
        SopoLog.i("onMessageReceived() 호출")
        if(remoteMessage.data.isEmpty()) return SopoLog.e("omMessageReceived [data:null]")

        val notificationId = remoteMessage.data.getValue("notificationId")
        val data = remoteMessage.data.getValue("data")

        val fcmPushDto = FcmPushDTO(notificationId, data)

        SopoLog.d("[notificationId:${notificationId}] / [data:$data]")
        //        SopoLog.d("[FcmPushDTO:${fcmPushDto.toString()}")

        when(fcmPushDto.notificationId)
        {
            // 사용자에게 택배 상태가 업데이트되었다고 알려줌
            NotificationEnum.PUSH_UPDATE_PARCEL.notificationId ->
            {
                SopoLog.i("Push 종류:택배 업데이트")

                val parcelIds = fcmPushDto.getUpdateParcel()

                if(parcelIds.isEmpty()) return

                SopoLog.d("업데이트 리스트 [${parcelIds.joinToString()}]")

                alertUpdateParcel(parcelIds).start()
            }
            // 친구 추천
            NotificationEnum.PUSH_FRIEND_RECOMMEND.notificationId ->
            {
                SopoLog.i("Push 종류:친구 추천")
                // Nothing to do yet..
            }
            // 전체 공지사항
            NotificationEnum.PUSH_FRIEND_RECOMMEND.notificationId ->
            {
                SopoLog.i("Push 종류:전체 공지사항")
                // Nothing to do yet..
            }
            NotificationEnum.PUSH_AWAKEN_DEVICE.notificationId ->
            {
                SopoLog.i("Push 종류:앱 어웨이큰")
                SOPOWorkManager.updateWorkManager(applicationContext)
//                NotificationImpl.awakenDeviceNoti(remoteMessage, this, Intent())
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

    /**
     * 1. 'Update parcel's ids'를 기존 로컬 디비 내 택배가 있는지 체크
     * 2. 1번에서 가져온 로컬 택배를 단일 조회로 있는지 체크
     * 2-1. 존재하는 택배는 'exist insert list'에 저장
     * 2-2. 존재하지 않은 택배는 'new insert list'에 저장
     * 3. 2-1. 택배는 inquiryHash 변경사항이 있을 경우 로컬 디비에 업데이트 -> delivery status가 변경되었을 때 노티피케이션 발생
     *
     */

    private suspend fun makeRefreshParcelStatus(updatableParcels: List<Parcel.Common>) = withContext(Dispatchers.Default) {

        return@withContext updatableParcels.map { parcel ->
            val status = parcelManagementRepo.getParcelStatusById(parcel.parcelId)

            status.updatableStatus = 1
            status.auditDte = TimeUtil.getDateTime()

            if(parcel.deliveryStatus != DeliveryStatusEnum.DELIVERED.CODE) return@map status

            status.deliveredStatus = 1

            return@map status
        }
    }

    private fun alertUpdateParcel(parcelIds: List<Int>) = CoroutineScope(Dispatchers.IO).launch {

        SopoLog.i("호출")

        val newInsertParcelIds = mutableListOf<Int>()

        // 1단계 업데이트 가능한 택배가 로컬 내 존재하는지 및 신규 택배 구분
        val updatableLocalParcels = parcelIds.mapNotNull { parcelId ->
            val localParcel = parcelRepository.getParcelById(parcelId = parcelId)
            if(localParcel == null)
            {
                SopoLog.d("이게 왜 찍히는거지? $parcelId")
                newInsertParcelIds.add(parcelId)
            }
            localParcel
        }

        // 3단계 기존 택배 중 inquiryHash에 변동있는 택배를 필터링
        val updatableRemoteParcels = updatableLocalParcels.mapNotNull { local ->
            val remote = parcelRepository.getRemoteParcelById(local.parcelId)
            return@mapNotNull if(remote.inquiryHash != local.inquiryHash) remote else null
        }

        // 2단계 신규 택배를 서버로부터 받아오는 작업 - 택배, 상태
        val newInsertParcels = newInsertParcelIds.map { parcelId -> parcelRepository.getRemoteParcelById(parcelId) }

        SopoLog.d("업데이트 가능 택배 리스트 [data:${updatableRemoteParcels.joinToString()}]")
        SopoLog.d("신규 택배 리스트 [data:${newInsertParcels.joinToString()}]")

        val updatableParcelStatuses = makeRefreshParcelStatus(updatableRemoteParcels)
        val newParcelStatuses = makeRefreshParcelStatus(newInsertParcels)

        parcelManagementRepo.updateParcelStatuses(updatableParcelStatuses)
        parcelManagementRepo.insertParcelStatuses(newParcelStatuses)

        val notifyParcel = updatableRemoteParcels.filter { parcel ->
            val local = parcelRepository.getParcelById(parcel.parcelId)
            SopoLog.d("서버 택배 상태 ${local?.deliveryStatus} >>> ${parcel.deliveryStatus}")
            parcel.deliveryStatus != local?.deliveryStatus
        } + newInsertParcels

        SopoLog.d("Notification 예정 택배 [data:${notifyParcel.joinToString()}]")

        val reportParcelIds = notifyParcel.mapNotNull {
            if(!it.reported) it.parcelId else null
        }

        launch {
            if(reportParcelIds.isEmpty()) return@launch
            parcelRepository.reportParcelStatus(reportParcelIds) }

        notifyParcel.flatMap {
            listOf(NotificationMessage.getUpdatePusMessage(it))
        }

        val messages = notifyParcel.map { NotificationMessage.getUpdatePusMessage(it) }

        messages.forEach { message ->
            NotificationImpl.notifyRegisterParcel(context = applicationContext, notificationMessage = message)
        }
    }
}

/*
* 1.
* */