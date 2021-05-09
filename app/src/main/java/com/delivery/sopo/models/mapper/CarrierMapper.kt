package com.delivery.sopo.models.mapper

import com.delivery.sopo.data.repository.database.room.entity.CarrierEntity
import com.delivery.sopo.enums.CarrierEnum
import com.delivery.sopo.models.CarrierDTO

object CarrierMapper
{
    fun entityToObject(carrierEntity: CarrierEntity?): CarrierDTO?{
        if(carrierEntity == null) return null
        return CarrierDTO(CarrierEnum.getCarrierByCode(carrierEntity.carrierCode), carrierEntity.clickRes?:0, carrierEntity.nonClickRes?:0, carrierEntity.iconRes?:0)
    }
}