package com.delivery.sopo.enums

import com.delivery.sopo.R

enum class DeliveryStatusEnum(val CODE: String, val TITLE: String, val MSG: String, val BACKGROUND: Int) {
    NOT_REGISTERED("NOT_REGISTERED", "미등록", "배송 준비 중 입니다", 0),
    INFORMATION_RECEIVED("INFORMATION_RECEIVED","배송정보 접수중", "배송 준비 중 입니다", 0),
    AT_PICKUP("AT_PICKUP", "상품픽업", "상품을 픽업했습니다\n", R.raw.inquiry_cardview_at_pickup),
    IN_TRANSIT("IN_TRANSIT", "배송중", "상품이 출발했습니다", R.raw.inquiry_cardview_in_transit),
    OUT_FOR_DELIVERY("OUT_FOR_DELIVERY", "동네 도착", "집배원이 배달을 시작했습니다", R.raw.inquiry_cardview_out_for_delivery),
    DELIVERED("DELIVERED", "배송완료", "상품이 도착했습니다", R.raw.inquiry_cardview_delivered),
    ERROR("ERROR", "에러상태", "상품을 조회할 수 없습니다", 0);


}