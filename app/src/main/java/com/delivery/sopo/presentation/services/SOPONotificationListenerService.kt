package com.delivery.sopo.presentation.services

import android.app.Notification
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.delivery.sopo.data.repositories.local.repository.CarrierDataSource
import com.delivery.sopo.data.repositories.local.repository.ParcelRepository
import com.delivery.sopo.data.resources.user.local.UserDataSource
import com.delivery.sopo.domain.usecase.parcel.remote.RegisterParcelUseCase
import com.delivery.sopo.models.Carrier
import com.delivery.sopo.models.parcel.Parcel
import com.delivery.sopo.models.push.NotificationMessage
import com.delivery.sopo.notification.NotificationImpl
import com.delivery.sopo.util.PermissionUtil
import com.delivery.sopo.util.SopoLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.KoinComponent
import org.koin.core.inject

class SOPONotificationListenerService: NotificationListenerService(), KoinComponent
{
    private val userDataSource: UserDataSource by inject()
    private val parcelRepo: ParcelRepository by inject()
    private val carrierRepo: CarrierDataSource by inject()
    private val registerParcelUseCase: RegisterParcelUseCase by inject()

    override fun onNotificationPosted(sbn: StatusBarNotification?)
    {
        super.onNotificationPosted(sbn)

        if(userDataSource.getStatus() != 1) return
        if(!PermissionUtil.checkNotificationListenerPermission(context = applicationContext, packageName)) return

        val notification: Notification = sbn?.notification?:return

        SopoLog.d("PackageName :: ${sbn.packageName}")

        val extras: Bundle = notification.extras

        CoroutineScope(Dispatchers.IO).launch {
            readKAKAONotificationListener(sbn.packageName, extras)
            readMMSNotificationListener(sbn.packageName, extras)
        }
    }


    suspend fun readKAKAONotificationListener(packageName: String, extras: Bundle){
        if(packageName != "com.kakao.talk") return

        val title = extras.getString(Notification.EXTRA_TITLE)
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)
        val subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)

        SopoLog.d("""
            KAKAO
            title:$title
            text:$text
            subText:$subText
        """.trimIndent())

        registerParcelInNotification(text.toString() ?: "")
    }

    suspend fun readMMSNotificationListener(packageName: String, extras: Bundle){
        if(packageName != "com.samsung.android.messaging") return

        val title = extras.getString(Notification.EXTRA_TITLE)
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)
        val subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)

        SopoLog.d("""
            SMS/MMS
            title:$title
            text:$text
            subText:$subText
        """.trimIndent())

        registerParcelInNotification(text.toString() ?: "")
    }

    private suspend fun registerParcelInNotification(content: String){
        try
        {
            val parcelRegister = getReceivedData(content)
            val parcel = registerParcelUseCase.invoke(parcelRegister)

            if(!parcel.reported)
            {
                CoroutineScope(Dispatchers.IO).launch {
                    parcelRepo.reportParcelStatus(listOf(parcel.parcelId)) }
            }

            val notificationMessage = NotificationMessage.getUpdatePusMessage(parcel)
            NotificationImpl.notifyRegisterParcel(context = applicationContext, notificationMessage = notificationMessage)
        }
        catch(e:Exception)
        {
            e.printStackTrace()
        }
    }

    /**
     * 문자 내용 중 해당하는 택배사가 존재하는지 확인
     * 없을 시 throw Exception
     */
    private suspend fun getReceivedCarrier(content: String) = withContext(Dispatchers.Default) {

        val carriers = carrierRepo.getAll().filterNotNull()

        var receivedCarrier: Carrier? = null

        for(carrier in carriers)
        {
            if(content.contains(carrier.carrier.NAME))
            {
                receivedCarrier = carrier
                break
            }
        }

        return@withContext receivedCarrier ?: throw NullPointerException("일치하는 택배사가 존재하지 않습니다.")
    }

    private fun getReceivedWaybillNum(content: String): String
    {
        val rows = content.split("\n")

        var matchRow: String? = null

        for(row in rows)
        {
            if(!(row.contains("송장번호") || row.contains("운송장번호") || row.contains("운송장"))) continue
            matchRow = row
            break
        }

        matchRow ?: throw Exception("운송장번호가 존재하지 않습니다.")

        val extractedWaybillNum = parse(matchRow)

        return with(extractedWaybillNum) {
            when
            {
                contains('_') -> replace("_", "")
                contains('-') -> replace("-", "")
                else -> this
            }
        }
    }

    private fun getReceivedAlias(content: String): String?
    {
        val rows = content.split("\n")

        var matchRow: String? = null

        for(row in rows)
        {
            if(!row.contains("상품명") || !row.contains("물품명")) continue
            matchRow = row
            break
        }
        if(matchRow != null) return parse(matchRow)
        return null
    }

    private suspend fun getReceivedData(mms: String): Parcel.Register
    {
        try
        {
            val receivedAlias: String? = getReceivedAlias(content = mms)
            val receivedWaybillsNum: String = getReceivedWaybillNum(content = mms)
            val receivedCarrier: Carrier = getReceivedCarrier(content = mms)

            return Parcel.Register(receivedWaybillsNum, receivedCarrier.carrier, receivedAlias)
        }
        catch(e: Exception)
        {
            SopoLog.e("MMS 데이터 중 택배에 해당하는 데이터가 존재하지 않습니다. [message:${e.message}]")
            throw e
        }
    }

    private fun parse(msg: String) = with(msg) {
        val index = indexOf(':')
        substring(index + 1).trim()
    }
}