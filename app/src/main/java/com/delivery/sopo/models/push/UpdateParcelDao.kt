package com.delivery.sopo.models.push

import com.delivery.sopo.consts.DeliveryStatusConst
import com.delivery.sopo.data.repository.database.room.entity.ParcelEntity
import com.delivery.sopo.models.mapper.ParcelMapper
import com.delivery.sopo.models.parcel.ParcelItem
import com.delivery.sopo.data.repository.local.repository.ParcelRepository
import com.delivery.sopo.util.SopoLog
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import org.koin.core.KoinComponent
import org.koin.core.inject

data class UpdateParcelDao(
    @SerializedName("parcelId")
    val parcelId: Int,
    @SerializedName("deliveryStatus")
    val deliveryStatus: String
) : KoinComponent
{
    private val parcelRepository : ParcelRepository by inject()

    suspend fun getParcel() = parcelRepository.getLocalParcelById(parcelId = parcelId)

    fun compareDeliveryStatus(parcelEntity: ParcelEntity): Boolean {
        SopoLog.d("""
            compareDeliveryStatus() call >>> [기존: ${parcelEntity.deliveryStatus} VS 변경: ${deliveryStatus}] 활성화 상태: ${parcelEntity.status}
        """.trimIndent())
        return parcelEntity.deliveryStatus != deliveryStatus && parcelEntity.status == 1
    }

    fun getMessage(parcelEntity: ParcelEntity) : String
    {
        val parcel = ParcelMapper.parcelEntityToParcel(parcelEntity)

        // ParcelEntity 중 inquiryResult(json의 String화)를 ParcelItem으로 객체화
        val gson = Gson()

        val type = object : TypeToken<ParcelItem?>()
        {}.type

        val reader = gson.toJson(parcelEntity.inquiryResult)
        val replaceStr = reader.replace("\\", "")
        val subStr = replaceStr.substring(1, replaceStr.length - 1)

        val parcelItem = gson.fromJson<ParcelItem?>(subStr, type)

        return when(deliveryStatus)
        {
            DeliveryStatusConst.NOT_REGISTER ->
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
            DeliveryStatusConst.IN_TRANSIT->
            {
                val size = parcelItem?.progresses?.size?:0

                "${parcelItem?.progresses?.get(size - 1)?.location?.name?:"위치불명"}에서 ${parcel.alias}가 출발했어요."
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