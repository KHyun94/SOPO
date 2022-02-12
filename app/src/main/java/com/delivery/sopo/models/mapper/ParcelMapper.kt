package com.delivery.sopo.models.mapper

import com.delivery.sopo.data.database.room.dto.CompletedParcelHistory
import com.delivery.sopo.data.database.room.entity.CompletedParcelHistoryEntity
import com.delivery.sopo.data.database.room.entity.ParcelEntity
import com.delivery.sopo.data.database.room.entity.ParcelStatusEntity
import com.delivery.sopo.models.inquiry.InquiryListItem
import com.delivery.sopo.models.parcel.Parcel
import com.delivery.sopo.models.parcel.ParcelStatus

object ParcelMapper
{
    fun parcelEntityToObject(req:ParcelEntity): Parcel.Common {
        return with(req){ Parcel.Common(parcelId = parcelId, userId = userId, waybillNum = waybillNum, carrier = carrier, alias = alias, inquiryResult = inquiryResult, inquiryHash = inquiryHash, deliveryStatus = deliveryStatus, regDte = regDte, arrivalDte = arrivalDte, auditDte = auditDte, status = status)}
    }

    fun parcelObjectToEntity(req:Parcel.Common):ParcelEntity {
        return with(req) { ParcelEntity(parcelId, userId, waybillNum, carrier, alias, inquiryResult, inquiryHash, deliveryStatus, arrivalDte?:"", regDte, auditDte, status?:0)}
    }

    fun parcelStatusEntityToObject(req:ParcelStatusEntity):ParcelStatus{
        return with(req) { ParcelStatus(parcelId = parcelId, isBeDelete = isBeDelete, updatableStatus = updatableStatus, unidentifiedStatus = unidentifiedStatus, deliveredStatus = deliveredStatus, isNowVisible = isNowVisible, auditDte = auditDte) }
    }

    fun parcelStatusObjectToEntity(req:ParcelStatus):ParcelStatusEntity{
        return with(req) { ParcelStatusEntity(parcelId = parcelId, isBeDelete = isBeDelete, updatableStatus = updatableStatus, unidentifiedStatus = unidentifiedStatus, deliveredStatus = deliveredStatus, isNowVisible = isNowVisible, auditDte = auditDte) }
    }

    fun parcelToParcelStatus(parcel:Parcel.Common):ParcelStatus{
        return with(parcel) { ParcelStatus(parcelId = parcelId) }
    }

    fun completeParcelStatusEntityToDTO(entity: CompletedParcelHistoryEntity): CompletedParcelHistory
    {
        val dates = entity.date.split('-')
        return with(entity){ CompletedParcelHistory(date = date, count = count, visibility = visibility, status = status, auditDte = auditDte) }
    }

    fun completeParcelStatusDTOToEntity(dto: CompletedParcelHistory): CompletedParcelHistoryEntity
    {
        return with(dto){ CompletedParcelHistoryEntity("${parseYear()}-${parseMonth()}", count, visibility, status, auditDte) }
    }

    fun parcelToParcelManagementEntity(parcelResponse: Parcel.Common): ParcelStatusEntity
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

    fun parcelEntityToParcel(parcelEntity: ParcelEntity): Parcel.Common{
        return Parcel.Common(parcelId = parcelEntity.parcelId,
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
                              regDte = parcelEntity.regDte
        )
    }

    fun parcelToParcelEntity(parcelResponse: Parcel.Common): ParcelEntity
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
            regDte = parcelResponse.regDte,
            status = parcelResponse.status ?: 0
        )
    }

    fun parcelEntityToParcelId(parcelEntity: ParcelEntity): Int{
        return parcelEntity.parcelId
    }

    fun parcelListToInquiryItemList(list: List<Parcel.Common>): MutableList<InquiryListItem>{
        return list.map { InquiryListItem(parcelResponse = it) }.toMutableList()
    }
}