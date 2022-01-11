package com.delivery.sopo.services

import android.content.Intent
import com.delivery.sopo.SOPOApp
import com.delivery.sopo.consts.DeliveryStatusConst
import com.delivery.sopo.consts.StatusConst
import com.delivery.sopo.enums.DeliveryStatusEnum
import com.delivery.sopo.enums.NotificationEnum
import com.delivery.sopo.models.mapper.ParcelMapper
import com.delivery.sopo.models.push.UpdatedParcelInfo
import com.delivery.sopo.networks.dto.FcmPushDTO
import com.delivery.sopo.notification.NotificationImpl
import com.delivery.sopo.data.repository.local.repository.ParcelManagementRepoImpl
import com.delivery.sopo.data.repository.local.repository.ParcelRepository
import com.delivery.sopo.models.parcel.ParcelItem
import com.delivery.sopo.models.parcel.ParcelResponse
import com.delivery.sopo.models.parcel.ParcelStatus
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

                alertUpdateParcel(remoteMessage, Intent(this@FirebaseService, SplashView::class.java), list).start()
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

    private fun alertUpdateParcel(remoteMessage: RemoteMessage, intent: Intent, parcelIds: List<Int>) = CoroutineScope(Dispatchers.IO).launch {

        SopoLog.d("alertUpdateParcel() 호출")

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
                ?: ParcelMapper.parcelToParcelStatus(parcel)
            val isFirstTimeUpdate = status.updatableStatus != 1

            if(parcel.deliveryStatus == DeliveryStatusEnum.DELIVERED.CODE)
            {
                status.deliveredStatus = 1
            }

            if(isFirstTimeUpdate)
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
    }

    private fun getMessage(parcel: ParcelResponse): String
    {
        // ParcelEntity 중 inquiryResult(json의 String화)를 ParcelItem으로 객체화
        val gson = Gson()

        val type = object: TypeToken<ParcelItem?>()
        {}.type

        val reader = gson.toJson(parcel.inquiryResult)
        val replaceStr = reader.replace("\\", "")
        val subStr = replaceStr.substring(1, replaceStr.length - 1)

        val parcelItem = gson.fromJson<ParcelItem?>(subStr, type)

        return when(parcel.deliveryStatus)
        {
            DeliveryStatusConst.ORPHANED ->
            {
                ""
            }
            DeliveryStatusConst.NOT_REGISTERED ->
            {
                ""
            }
            DeliveryStatusConst.INFORMATION_RECEIVED ->
            {
                ""
            }
            DeliveryStatusConst.AT_PICKUP ->
            {
                "${parcelItem?.from?.name}님이 보내신 ${parcel.alias}가 배송을 위해 집하되었습니다."
            }
            DeliveryStatusConst.IN_TRANSIT ->
            {
                val size = parcelItem?.progresses?.size ?: 0

                "${parcelItem?.progresses?.get(size - 1)?.location?.name ?: "위치불명"}에서 ${parcel.alias}가 출발했어요."
            }
            DeliveryStatusConst.OUT_FOR_DELIVERY ->
            {
                "${parcelItem?.from?.name}님이 보내신 ${parcel.alias}가 우리동네에 도착했습니다!"
            }
            DeliveryStatusConst.DELIVERED ->
            {
                "고객님의 택배가 도착했습니다."
            }
            else ->
            {
                "ERROR"
            }
        }
    }

}