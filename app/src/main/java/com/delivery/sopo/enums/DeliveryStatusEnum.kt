package com.delivery.sopo.enums

import com.delivery.sopo.R

enum class DeliveryStatusEnum(val CODE: String, val TITLE: String, val MSG: String, val BACKGROUND: Int) {
    NOT_REGISTERED("NOT_REGISTERED", "미등록", "배송 준비 중이에요", R.drawable.ic_inquiry_cardview_not_registered),
    ORPHANED("ORPHANED", "고아상태", "배송 준비 중이에요", R.drawable.ic_inquiry_cardview_orphaned),
    INFORMATION_RECEIVED("INFORMATION_RECEIVED","배송정보 접수중", "배송 준비 중이에요", R.drawable.ic_inquiry_cardview_not_registered),
    AT_PICKUP("AT_PICKUP", "상품픽업", "상품을 픽업했어요", R.raw.inquiry_2depth_at_pickup),
    IN_TRANSIT("IN_TRANSIT", "배송중", "열심히 배송 중...", R.raw.inquiry_2depth_in_transit),
    OUT_FOR_DELIVERY("OUT_FOR_DELIVERY", "동네 도착", "잠시후 도착합니다.", R.raw.inquiry_2depth_out_for_delivery),
    DELIVERED("DELIVERED", "배송완료", "상품이 도착했어요", R.raw.inquiry_2depth_delivered),
    ERROR("ERROR", "에러상태", "상품을 조회할 수 없습니다", 0);


}