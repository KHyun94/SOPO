package com.delivery.sopo.consts

object DeliveryStatus {
    const val DELIVERED = "delivered" // 배송완료
    const val OUT_FOR_DELIVERY = "out_for_delivery" // 배송출발
    const val IN_TRANSIT = "in_transit" // 상품이동중
    const val AT_PICKUP = "at_pickup" // 상품인수
    const val INFORMATION_RECEIVED = "information_received" // 상품준비중
}