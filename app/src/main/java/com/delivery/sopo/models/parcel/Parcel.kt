package com.delivery.sopo.models.parcel

import android.view.View
import com.delivery.sopo.enums.CarrierEnum
import com.delivery.sopo.enums.DeliveryStatusEnum
import com.delivery.sopo.enums.SnackBarEnum
import com.delivery.sopo.enums.SnackBarType
import com.delivery.sopo.models.Carrier
import com.delivery.sopo.models.parcel.tracking_info.TrackingInfo
import com.delivery.sopo.util.DateUtil
import com.delivery.sopo.util.TimeUtil
import com.delivery.sopo.util.ui_util.CustomSnackBar
import com.delivery.sopo.util.ui_util.OnSnackBarClickListener
import com.google.gson.annotations.SerializedName
import java.io.Serializable

class Parcel
{
    data class Common(
            @SerializedName("parcelId") val parcelId: Int,
            @SerializedName("userId") val userId: Int,
            @SerializedName("waybillNum") val waybillNum: String,
            @SerializedName("carrier") val carrier: String,
            @SerializedName("alias") var alias: String,
            @SerializedName("inquiryResult") var trackingInfo: TrackingInfo?,
            @SerializedName("inquiryHash") var inquiryHash: String?,
            @SerializedName("deliveryStatus") var deliveryStatus: String,
            @SerializedName("regDte") var regDte: String,
            @SerializedName("arrivalDte") var arrivalDte: String?,
            @SerializedName("auditDte") var auditDte: String,
            @SerializedName("status") var status: Int?,
            @SerializedName("reported") var reported: Boolean): Serializable
    {
        fun isDelivered(): Boolean {
            return deliveryStatus == DeliveryStatusEnum.DELIVERED.CODE
        }

        fun getDeliveredAlarm(): String?
        {
//            val char : CharSequence = "보기"
            val date = DateUtil.changeDateFormat(arrivalDte ?: "", oldPattern = DateUtil.DATE_TIME_TYPE_DEFAULT, newPattern = DateUtil.DATE_TYPE_KOREAN_SEMI)
//            val snackBar = CustomSnackBar.make(view = view, content = "${date}에 배송완료된 택배네요.", data = arrivalDte?:"", type = SnackBarEnum.COMMON, clickListener = listener)
//            snackBar.show()
            return date
        }
    }

    data class Detail(val regDt: String, val alias: String, val carrier: Carrier, val waybillNum: String, val deliverStatus: DeliveryStatusEnum?, val timeLineProgresses: MutableList<TimeLineProgress>)
    {
        fun changeRegDtFormat(): String
        {
            val yyMMdd = regDt.split(" ")[0].split("-")
            return with(yyMMdd) { "${get(0)}년 ${get(1)}월 ${get(2)}일 등록" }
        }
    }

    data class Status(var parcelId: Int, var isBeDelete: Int = 0, var updatableStatus: Int = 0, var unidentifiedStatus: Int = 0, var deliveredStatus: Int = 0, var isNowVisible: Int = 0, var auditDte: String = TimeUtil.getDateTime())

    data class Updatable(
            @SerializedName("parcel") val parcel: Common,
            @SerializedName("updated") val updated: Boolean)

    data class Register(
            @SerializedName("waybillNum") var waybillNum: String?,
            @SerializedName("carrier") var carrier: CarrierEnum?,
            @SerializedName("alias") var alias: String?): Serializable
    {
        fun setParcelAlias(alias: String)
        {
            this.alias = alias
        }
    }

}

