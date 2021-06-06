package com.delivery.sopo.enums

import com.delivery.sopo.R

enum class DeliveryStatusEnum(val CODE: String, val TITLE: String, val MSG: String, val BACKGROUND: Int) {
//    NOT_REGISTER("NOT_REGISTER", "미등록", "배송 준비 중 입니다", 0),
//    INFORMATION_RECEIVED("information_received","배송정보 접수중", "배송 준비 중 입니다", 0),
//    AT_PICKUP("at_pickup", "상품픽업", "상품을 픽업했습니다\n", R.t.inquiry_2depth_at_pickup),
//    IN_TRANSIT("in_transit", "배송중", "상품이 출발했습니다", R.drawable.inquiry_2depth_in_transit),
//    OUT_FOR_DELIVERY("out_for_delivery", "동네 도착", "집배원이 배달을 시작했습니다", R.drawable.inquiry_2depth_out_for_delivery),
//    DELIVERED("delivered", "배송완료", "상품이 도착했습니다", R.drawable.inquiry_2depth_delivered),
//    ERROR("error", "에러상태", "상품을 조회할 수 없습니다", 0)

    NOT_REGISTER("NOT_REGISTER", "미등록", "배송 준비 중 입니다", 0),
    INFORMATION_RECEIVED("information_received","배송정보 접수중", "배송 준비 중 입니다", 0),
    AT_PICKUP("at_pickup", "상품픽업", "상품을 픽업했습니다\n", R.raw.inquiry_cardview_at_pickup),
    IN_TRANSIT("in_transit", "배송중", "상품이 출발했습니다", R.raw.inquiry_cardview_in_transit),
    OUT_FOR_DELIVERY("out_for_delivery", "동네 도착", "집배원이 배달을 시작했습니다", R.raw.inquiry_cardview_out_for_delivery),
    DELIVERED("delivered", "배송완료", "상품이 도착했습니다", R.raw.inquiry_cardview_delivered),
    ERROR("error", "에러상태", "상품을 조회할 수 없습니다", 0);


}