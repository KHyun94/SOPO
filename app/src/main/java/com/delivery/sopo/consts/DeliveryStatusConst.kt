package com.delivery.sopo.consts

object DeliveryStatusConst
{

    const val INFORMATION_RECEIVED = "information_received" // 0.5. 전산상 등록 상태
    const val AT_PICKUP = "at_pickup" // 1. 상품 인수
    const val IN_TRANSIT = "in_transit" // 2. 배송 중
    const val OUT_FOR_DELIVERRY = "out_for_delivery" // 3. 동네 도착(택배 준비 중)
    const val DELIVERED = "delivered" // 4. 배송 완료
}