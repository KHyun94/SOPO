package com.delivery.sopo.services

import android.content.Intent
import com.delivery.sopo.consts.DeliveryStatusConst
import com.delivery.sopo.enums.DeliveryStatusEnum
import com.delivery.sopo.enums.NotificationEnum
import com.delivery.sopo.networks.dto.FcmPushDTO
import com.delivery.sopo.notification.NotificationImpl
import com.delivery.sopo.data.repository.local.repository.ParcelManagementRepoImpl
import com.delivery.sopo.data.repository.local.repository.ParcelRepository
import com.delivery.sopo.models.parcel.TrackingInfo
import com.delivery.sopo.models.parcel.ParcelResponse
import com.delivery.sopo.services.workmanager.SOPOWorkManager
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.util.TimeUtil
import com.delivery.sopo.views.splash.SplashView
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
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

                val list = fcmPushDto.getUpdateParcel()

                if(list.isEmpty()) return

                SopoLog.d("""
                    업데이트 리스트
                    [${list.joinToString()}]
                """.trimIndent())

                alertUpdateParcel(list).start()
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
                // TODO 테스트용 노티피케이션
                NotificationImpl.awakenDeviceNoti(remoteMessage = remoteMessage, context = applicationContext, intent = Intent(this, SplashView::class.java))
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

    /**
     * 1. 'Update parcel's ids'를 기존 로컬 디비 내 택배가 있는지 체크
     * 2. 1번에서 가져온 로컬 택배를 단일 조회로 있는지 체크
     * 2-1. 존재하는 택배는 'exist insert list'에 저장
     * 2-2. 존재하지 않은 택배는 'new insert list'에 저장
     * 3. 2-1. 택배는 inquiryHash 변경사항이 있을 경우 로컬 디비에 업데이트 -> delivery status가 변경되었을 때 노티피케이션 발생
     *
     */

    private suspend fun makeRefreshParcelStatus(updatableParcels: List<ParcelResponse>) = withContext(Dispatchers.Default) {

        return@withContext updatableParcels.map { parcel ->
            val status = parcelManagementRepo.getParcelStatus(parcel.parcelId)

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
            val localParcel = parcelRepository.getLocalParcelById(parcelId = parcelId)
            if(localParcel == null) newInsertParcelIds.add(parcelId)
            localParcel
        }

        // 3단계 기존 택배 중 inquiryHash에 변동있는 택배를 필터링
        val updatableRemoteParcels = updatableLocalParcels.filter { local ->
            val remote = parcelRepository.getRemoteParcelById(local.parcelId)
            remote.inquiryHash != local.inquiryHash
        }

        // 2단계 신규 택배를 서버로부터 받아오는 작업 - 택배, 상태
        val newInsertParcels = newInsertParcelIds.map { parcelId -> parcelRepository.getRemoteParcelById(parcelId) }

        val updatableParcelStatuses = makeRefreshParcelStatus(updatableRemoteParcels)
        val newParcelStatuses = makeRefreshParcelStatus(newInsertParcels)

        parcelManagementRepo.updateParcelStatuses(updatableParcelStatuses)
        parcelManagementRepo.insertParcelStatuses(newParcelStatuses)

        val notifyParcel = updatableRemoteParcels.filter { parcel ->
            val local = parcelRepository.getLocalParcelById(parcel.parcelId)
            parcel.deliveryStatus != local?.deliveryStatus
        } + newInsertParcels

        notifyParcel.forEach { parcel ->
            NotificationImpl.notifyRegisterParcel(context = applicationContext, parcel)
        }
    }

    /*private fun alertUpdateParcel(remoteMessage: RemoteMessage, intent: Intent, parcelIds: List<Int>) = CoroutineScope(Dispatchers.IO).launch {

        SopoLog.d("호출")

        val msgList = mutableListOf<String>()
        val insertParcelIds = mutableListOf<Int>()

        val existLocalParcel = parcelIds.mapNotNull { parcelId ->
            val localParcel = parcelRepository.getLocalParcelById(parcelId = parcelId)
            if(localParcel == null) insertParcelIds.add(parcelId)
            localParcel
        }

        // 새로운 택배
        val insertParcels = insertParcelIds.map { parcelId -> parcelRepository.getRemoteParcelById(parcelId) }
        val insertParcelStatuses = insertParcels.map {
            msgList.add(getMessage(it))
            ParcelMapper.parcelToParcelStatus(it).apply {
                updatableStatus = 1
                auditDte = TimeUtil.getDateTime()
            }
        }

        val updateRemoteParcels = existLocalParcel.filter { local ->
            val remote = parcelRepository.getRemoteParcelById(local.parcelId)
            remote.inquiryHash != local.inquiryHash
        }

        val updateParcelStatuses = updateRemoteParcels.mapNotNull { parcel ->
            val status = parcelManagementRepo.getParcelStatus(parcel.parcelId)
            val hasDoneUpdated = status.updatableStatus != 1

            if(parcel.deliveryStatus == DeliveryStatusEnum.DELIVERED.CODE)
            {
                status.deliveredStatus = 1
            }

            if(hasDoneUpdated)
            {
                msgList.add(getMessage(parcel))
                status.updatableStatus = 1
                status.auditDte = TimeUtil.getDateTime()
                return@mapNotNull status
            }

            return@mapNotNull null
        }

        parcelManagementRepo.insertParcelStatuses(insertParcelStatuses)
        parcelManagementRepo.updateParcelStatuses(updateParcelStatuses)

        val notiParcel = insertParcels + updateRemoteParcels

        notiParcel.forEach {
            NotificationImpl.notifyRegisterParcel(applicationContext, it)
        }

        msgList.forEach {
            NotificationImpl.alertUpdateParcel(remoteMessage = remoteMessage, context = applicationContext, intent = intent, message = *arrayOf(it))
        }
    }*/
}