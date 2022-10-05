package com.delivery.sopo.enums

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import com.delivery.sopo.R

enum class DeliveryStatus(
    val CODE: String,
    val TITLE: String,
    val MSG: String,
    val BACKGROUND: Int
) {
    NOT_REGISTERED(
        "NOT_REGISTERED",
        "미등록",
        "배송 준비 중이에요",
        R.drawable.ic_inquiry_2depth_not_registered
    ),
    INFORMATION_RECEIVED(
        "INFORMATION_RECEIVED",
        "배송정보 접수중",
        "배송 준비 중이에요",
        R.drawable.ic_inquiry_2depth_not_registered
    ),
    AT_PICKUP("AT_PICKUP", "상품픽업", "상품을 픽업했어요", R.raw.inquiry_2depth_at_pickup),
    IN_TRANSIT("IN_TRANSIT", "배송중", "열심히 배송 중...", R.raw.inquiry_2depth_in_transit),
    OUT_FOR_DELIVERY(
        "OUT_FOR_DELIVERY",
        "동네 도착",
        "잠시후 도착합니다.",
        R.raw.inquiry_2depth_out_for_delivery
    ),
    DELIVERED("DELIVERED", "배송완료", "상품이 도착했어요", R.raw.inquiry_2depth_delivered),
    ORPHANED("ORPHANED", "고아상태", "배송 준비 중이에요", R.drawable.ic_inquiry_cardview_orphaned),
    ERROR("ERROR", "에러상태", "상품을 조회할 수 없습니다", 0);


}

class ParcelDepth {

    companion object {
        fun getParcelFirstDepth(deliveryStatus: String): First {
            return when (deliveryStatus) {
                DeliveryStatus.NOT_REGISTERED.CODE -> First(
                    iconRes = R.drawable.ic_inquiry_cardview_not_registered,
                    bgColorRes = R.color.STATUS_PREPARING,
                    status = "준비중",
                    statusColor = R.color.COLOR_GRAY_300
                )
                DeliveryStatus.ORPHANED.CODE -> First(
                    iconRes = R.drawable.ic_inquiry_cardview_orphaned,
                    bgColorRes = R.color.MAIN_WHITE,
                    status = "조회불가",
                    statusColor = R.color.COLOR_GRAY_300
                )
                DeliveryStatus.INFORMATION_RECEIVED.CODE ->  First(
                    iconRes = R.drawable.ic_inquiry_cardview_not_registered,
                    bgColorRes = R.color.STATUS_PREPARING,
                    status = "준비중",
                    statusColor = R.color.COLOR_GRAY_300
                )
                DeliveryStatus.AT_PICKUP.CODE ->  First(
                    iconRes = R.drawable.ic_inquiry_cardview_at_pickup,
                    bgColorRes = R.color.STATUS_PREPARING,
                    status = "상품인수",
                    statusColor = R.color.COLOR_GRAY_300
                )
                DeliveryStatus.IN_TRANSIT.CODE ->  First(
                    iconRes = R.drawable.ic_inquiry_cardview_in_transit_test,
                    bgColorRes = R.color.STATUS_ING,
                    status = "배송중",
                    statusColor = R.color.COLOR_GRAY_300
                )
                DeliveryStatus.OUT_FOR_DELIVERY.CODE ->  First(
                    iconRes = R.drawable.ic_inquiry_cardview_out_for_delivery,
                    bgColorRes = R.color.COLOR_MAIN_700,
                    status = "동네도착",
                    statusColor = R.color.COLOR_GRAY_300
                )
                else ->  First(
                    iconRes = R.drawable.ic_inquiry_cardview_not_registered,
                    bgColorRes = R.color.STATUS_PREPARING,
                    status = "에러",
                    statusColor = R.color.COLOR_GRAY_300
                )
            }
        }
    }

    data class First(
        @DrawableRes val iconRes: Int,
        @ColorRes val bgColorRes: Int,
        val status: String,
        @ColorRes val statusColor: Int
    )
//    data class Second()
}