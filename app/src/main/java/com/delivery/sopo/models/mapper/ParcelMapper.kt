package com.delivery.sopo.models.mapper

import com.delivery.sopo.data.repository.database.room.dto.CompletedParcelHistory
import com.delivery.sopo.data.repository.database.room.entity.CompletedParcelHistoryEntity
import com.delivery.sopo.data.repository.database.room.entity.ParcelEntity
import com.delivery.sopo.data.repository.database.room.entity.ParcelStatusEntity
import com.delivery.sopo.models.inquiry.InquiryListItem
import com.delivery.sopo.models.parcel.ParcelResponse

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

    fun parcelToParcelManagementEntity(parcelResponse: ParcelResponse): ParcelStatusEntity
    {
        return ParcelStatusEntity(
            parcelId = parcelResponse.parcelId
        )
    }

    fun parcelEntityToParcelManagementEntity(parcelEntity: ParcelEntity): ParcelStatusEntity
    {
        return ParcelStatusEntity(
            parcelId = parcelEntity.parcelId
        )
    }

    fun parcelEntityToParcel(parcelEntity: ParcelEntity): ParcelResponse{
        return ParcelResponse(parcelId = parcelEntity.parcelId,
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

    fun parcelToParcelEntity(parcelResponse: ParcelResponse): ParcelEntity
    {
        return ParcelEntity(
            parcelId = parcelResponse.parcelId,
            userId = parcelResponse.userId,
            waybillNum = parcelResponse.waybillNum,
            carrier = parcelResponse.carrier,
            alias = parcelResponse.alias,
            inquiryResult = parcelResponse.inquiryResult,
            inquiryHash = parcelResponse.inquiryHash,
            deliveryStatus = parcelResponse.deliveryStatus,
            arrivalDte = parcelResponse.arrivalDte.toString(),
            auditDte = parcelResponse.auditDte,
            regDt = parcelResponse.regDt,
            status = parcelResponse.status ?: 0
        )
    }

    fun parcelEntityToParcelId(parcelEntity: ParcelEntity): Int{
        return parcelEntity.parcelId
    }

    fun parcelListToInquiryItemList(list: List<ParcelResponse>): MutableList<InquiryListItem>{
        return list.map { InquiryListItem(parcelResponse = it) }.toMutableList()
    }
}