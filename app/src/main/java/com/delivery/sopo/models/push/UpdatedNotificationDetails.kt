package com.delivery.sopo.models.push

data class UpdatedNotificationDetails(val content: String, val summaryText: String?)
{
    companion object
    {
//        fun makeContent(): UpdatedNotificationDetails
//        {
//
//
//        }

//        private fun getMessage(parcel: ParcelResponse): Pair<String, String>
//        {
//            // ParcelEntity 중 inquiryResult(json의 String화)를 ParcelItem으로 객체화
//            val gson = Gson()
//
//            val type = object: TypeToken<ParcelItem?>()
//            {}.type
//
//            val reader = gson.toJson(parcel.inquiryResult)
//            val replaceStr = reader.replace("\\", "")
//            val subStr = replaceStr.substring(1, replaceStr.length - 1)
//
//            val parcelItem = gson.fromJson<ParcelItem?>(subStr, type)
//
//            return when(parcel.deliveryStatus)
//            {
//                DeliveryStatusConst.ORPHANED ->
//                {
//                    ""
//                }
//                DeliveryStatusConst.NOT_REGISTERED ->
//                {
//                    ""
//                }
//                DeliveryStatusConst.INFORMATION_RECEIVED ->
//                {
//                    ""
//                }
//                DeliveryStatusConst.AT_PICKUP ->
//                {
//                    "${parcelItem?.from?.name}님이 보내신 ${parcel.alias}가 배송을 위해 집하되었습니다."
//                }
//                DeliveryStatusConst.IN_TRANSIT ->
//                {
//                    val size = parcelItem?.progresses?.size ?: 0
//
//                    "${parcelItem?.progresses?.get(size - 1)?.location?.name ?: "위치불명"}에서 ${parcel.alias}가 출발했어요."
//                }
//                DeliveryStatusConst.OUT_FOR_DELIVERY ->
//                {
//                    "${parcelItem?.from?.name}님이 보내신 ${parcel.alias}가 우리동네에 도착했습니다!"
//                }
//                DeliveryStatusConst.DELIVERED ->
//                {
//                    "고객님의 택배가 도착했습니다."
//                }
//                else ->
//                {
//                    "ERROR"
//                }
//            }
//        }
    }
}
