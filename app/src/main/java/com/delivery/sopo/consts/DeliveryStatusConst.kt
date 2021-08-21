package com.delivery.sopo.consts

import com.delivery.sopo.enums.DeliveryStatusEnum

object DeliveryStatusConst
{
    const val NOT_REGISTERED = "NOT_REGISTERED"
    const val INFORMATION_RECEIVED = "INFORMATION_RECEIVED" // 0.5. 전산상 등록 상태
    const val AT_PICKUP = "AT_PICKUP" // 1. 상품 인수
    const val IN_TRANSIT = "IN_TRANSIT" // 2. 배송 중
    const val OUT_FOR_DELIVERY = "OUT_FOR_DELIVERY" // 3. 동네 도착(택배 준비 중)
    const val DELIVERED = "DELIVERED" // 4. 배송 완료

    const val CHANGED="CHANGED"
    const val UNCHANGED="UNCHANGED"
    const val UNIDENTIFIED_DELIVERED_PARCEL="UNIDENTIFIED_DELIVERED_PARCEL"
    const val ORPHAN_PARCEL="ORPHAN_PARCEL"

    fun getDeliveryStatus(deliveryStatus: String): DeliveryStatusEnum
    {
        return when (deliveryStatus)
        {
            NOT_REGISTERED -> DeliveryStatusEnum.NOT_REGISTERED
            INFORMATION_RECEIVED -> DeliveryStatusEnum.INFORMATION_RECEIVED
            AT_PICKUP -> DeliveryStatusEnum.AT_PICKUP
            IN_TRANSIT -> DeliveryStatusEnum.IN_TRANSIT
            OUT_FOR_DELIVERY -> DeliveryStatusEnum.OUT_FOR_DELIVERY
            DELIVERED -> DeliveryStatusEnum.DELIVERED
            else -> DeliveryStatusEnum.ERROR
        }
    }
}