package com.delivery.sopo.enums

import com.delivery.sopo.R

enum class DeliveryStatusEnum(val CODE: String, val TITLE: String, val MSG: String, val BACKGROUND: Int) {
    NOT_REGISTER("not_register", "미등록", "아직 배송상품 정보가 없습니다.", R.drawable.ic_parcel_not_register),
    INFORMATION_RECEIVED("information_received","배송정보 접", "아직 배송상품 정보가 없습니다.", R.drawable.ic_parcel_not_register),
    AT_PICKUP("at_pickup", "상품픽업", "상품이 집화처리 되었습니다.", R.drawable.ic_parcel_at_pickup),
    IN_TRANSIT("in_transit", "배송중", "상품이 출발했습니다.", R.drawable.ic_parcel_in_transit),
    OUT_OF_DELIVERY("out_for_delivery", "동네 도착", "집배원이 배달을 시작했습니다.", R.drawable.ic_parcl_out_of_delivery),
    DELIVERED("delivered", "배송완료", "상품이 도착했습니다.", R.drawable.ic_parcel_delivered),
    ERROR("error", "에러상태", "상품을 조회할 수 없습니다.", 0)
}