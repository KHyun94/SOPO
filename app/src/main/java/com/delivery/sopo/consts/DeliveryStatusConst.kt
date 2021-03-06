package com.delivery.sopo.consts

import com.delivery.sopo.R
import com.delivery.sopo.enums.DeliveryStatusEnum
import com.delivery.sopo.util.SopoLog

object DeliveryStatusConst
{
    const val NOT_REGISTER = "NOT_REGISTER"
    const val INFORMATION_RECEIVED = "information_received" // 0.5. 전산상 등록 상태
    const val AT_PICKUP = "at_pickup" // 1. 상품 인수
    const val IN_TRANSIT = "in_transit" // 2. 배송 중
    const val OUT_FOR_DELIVERRY = "out_for_delivery" // 3. 동네 도착(택배 준비 중)
    const val DELIVERED = "delivered" // 4. 배송 완료

    fun getDeliveryStatus(deliveryStatus: String): DeliveryStatusEnum
    {
        return when (deliveryStatus)
        {
            NOT_REGISTER -> DeliveryStatusEnum.NOT_REGISTER
            INFORMATION_RECEIVED -> DeliveryStatusEnum.INFORMATION_RECEIVED
            AT_PICKUP -> DeliveryStatusEnum.AT_PICKUP
            IN_TRANSIT -> DeliveryStatusEnum.IN_TRANSIT
            OUT_FOR_DELIVERRY -> DeliveryStatusEnum.OUT_OF_DELIVERY
            DELIVERED -> DeliveryStatusEnum.DELIVERED
            else -> DeliveryStatusEnum.ERROR
        }
    }
}