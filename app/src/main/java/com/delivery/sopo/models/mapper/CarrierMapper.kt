package com.delivery.sopo.models.mapper

import com.delivery.sopo.data.database.room.entity.CarrierEntity
import com.delivery.sopo.enums.CarrierEnum
import com.delivery.sopo.models.Carrier
import com.delivery.sopo.util.CarrierUtil

object CarrierMapper
{
    fun entityToObject(carrierEntity: CarrierEntity): Carrier{
        val carrierEnum = CarrierEnum.getCarrierByCode(carrierEntity.code)
        val icons = CarrierUtil.getCarrierImages(carrierEnum)
        return Carrier(carrierEnum, icons)
    }

    fun enumToObject(carrierEnum: CarrierEnum): Carrier {
        val icons = CarrierUtil.getCarrierImages(carrierEnum)
        return Carrier(carrierEnum, icons)
    }
}