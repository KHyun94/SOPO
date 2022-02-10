package com.delivery.sopo.models.mapper

import com.delivery.sopo.data.database.room.dto.AppPasswordDTO
import com.delivery.sopo.data.database.room.entity.AppPasswordEntity

object AppPasswordMapper: MapperInterface<AppPasswordEntity, AppPasswordDTO>
{
    override fun entityToDto(entity: AppPasswordEntity): AppPasswordDTO
    {
        return AppPasswordDTO(entity.userId, entity.appPassword, entity.auditDte)
    }

    override fun dtoToEntity(dto: AppPasswordDTO): AppPasswordEntity
    {
        return AppPasswordEntity(dto.userId, dto.appPassword, dto.auditDte)
    }

}