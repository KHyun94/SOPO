package com.delivery.sopo.models.mapper

import com.delivery.sopo.data.repository.database.room.entity.CarrierEntity
import com.delivery.sopo.enums.CarrierEnum
import com.delivery.sopo.models.CarrierDTO
import com.delivery.sopo.util.CarrierUtil
import com.delivery.sopo.util.SopoLog

object CarrierMapper
{
    fun entityToObject(carrierEntity: CarrierEntity?): CarrierDTO?{
        if(carrierEntity == null) return null
        val carrierEnum = CarrierEnum.getCarrierByCode(carrierEntity.carrierCode)
        val icons = CarrierUtil.getCarrierImages(carrierEnum).apply {
            SopoLog.d("이미지 >>> ${this[carrierEnum]?.size}")
        }
        return CarrierDTO(carrierEnum, carrierEntity.min, carrierEntity.max, icons[carrierEnum]?: emptyList())
    }
}