package com.delivery.sopo.models.mapper

import com.delivery.sopo.data.repository.database.room.dto.CompletedParcelHistory
import com.delivery.sopo.data.repository.database.room.entity.CompletedParcelHistoryEntity
import com.delivery.sopo.data.repository.database.room.entity.ParcelEntity
import com.delivery.sopo.data.repository.database.room.entity.ParcelStatusEntity
import com.delivery.sopo.models.inquiry.InquiryListItem
import com.delivery.sopo.models.parcel.ParcelDTO

object ParcelMapper
{
    fun completeParcelStatusEntityToDTO(entity: CompletedParcelHistoryEntity): CompletedParcelHistory
    {
        val dates = entity.date.split('-')
        return with(entity){ CompletedParcelHistory(date = date, count = count, visibility = visibility, status = status, auditDte = auditDte) }
    }

    fun completeParcelStatusDTOToEntity(dto: CompletedParcelHistory): CompletedParcelHistoryEntity
    {
        return with(dto){ CompletedParcelHistoryEntity("${parseYear()}-${parseMonth()}", count, visibility, status, auditDte) }
    }

    fun parcelToParcelManagementEntity(parcelDTO: ParcelDTO): ParcelStatusEntity
    {
        return ParcelStatusEntity(
            parcelId = parcelDTO.parcelId
        )
    }

    fun parcelEntityToParcelManagementEntity(parcelEntity: ParcelEntity): ParcelStatusEntity
    {
        return ParcelStatusEntity(
            parcelId = parcelEntity.parcelId
        )
    }

    fun parcelEntityToParcel(parcelEntity: ParcelEntity): ParcelDTO{
        return ParcelDTO(parcelId = parcelEntity.parcelId,
            userId = parcelEntity.userId,
            waybillNum = parcelEntity.waybillNum,
            carrier = parcelEntity.carrier,
            alias = parcelEntity.alias,
            inquiryResult = parcelEntity.inquiryResult,
            inquiryHash = parcelEntity.inquiryHash,
            deliveryStatus = parcelEntity.deliveryStatus,
            arrivalDte = parcelEntity.arrivalDte,
            auditDte = parcelEntity.auditDte,
            status = parcelEntity.status,
            regDt = parcelEntity.regDt
        )
    }

    fun parcelToParcelEntity(parcelDTO: ParcelDTO): ParcelEntity
    {
        return ParcelEntity(
            parcelId = parcelDTO.parcelId,
            userId = parcelDTO.userId,
            waybillNum = parcelDTO.waybillNum,
            carrier = parcelDTO.carrier,
            alias = parcelDTO.alias,
            inquiryResult = parcelDTO.inquiryResult,
            inquiryHash = parcelDTO.inquiryHash,
            deliveryStatus = parcelDTO.deliveryStatus,
            arrivalDte = parcelDTO.arrivalDte.toString(),
            auditDte = parcelDTO.auditDte,
            regDt = parcelDTO.regDt,
            status = parcelDTO.status ?: 0
        )
    }

    fun parcelEntityToParcelId(parcelEntity: ParcelEntity): Int{
        return parcelEntity.parcelId
    }

    fun parcelListToInquiryItemList(list: List<ParcelDTO>): MutableList<InquiryListItem>{
        return list.map { InquiryListItem(parcelDTO = it) }.toMutableList()
    }
}