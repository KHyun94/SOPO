package com.delivery.sopo.enums

enum class DeliveryStatusEnum(val code: String, val msg: String) {
    delivered("delivered", "배송완료"),
    out_for_delivery("out_for_delivery", "배송출발"),
    in_transit("in_transit", "상품이동중"),
    at_pickup("at_pickup", "상품인수"),
    information_received("information_received","상품준비중");
}