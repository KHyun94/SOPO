package com.delivery.sopo.models.mapper

import com.delivery.sopo.data.repository.database.room.entity.ParcelEntity
import com.delivery.sopo.data.repository.database.room.entity.ParcelStatusEntity
import com.delivery.sopo.models.inquiry.InquiryListItem
import com.delivery.sopo.models.parcel.ParcelDTO
import com.delivery.sopo.models.parcel.ParcelId
import com.delivery.sopo.util.SopoLog

object ParcelMapper
{
    fun parcelToParcelManagementEntity(parcelDTO: ParcelDTO): ParcelStatusEntity
    {
        return ParcelStatusEntity(
            regDt = parcelDTO.parcelId.regDt,
            parcelUid = parcelDTO.parcelId.parcelUid
        )
    }

    fun parcelEntityToParcelManagementEntity(parcelEntity: ParcelEntity): ParcelStatusEntity
    {
        return ParcelStatusEntity(
            regDt = parcelEntity.regDt,
            parcelUid = parcelEntity.parcelUid
        )
    }

    fun parcelEntityToParcel(parcelEntity: ParcelEntity): ParcelDTO{
        return ParcelDTO(parcelId = ParcelId(regDt = parcelEntity.regDt, parcelUid = parcelEntity.parcelUid),
            userId = parcelEntity.userId,
            waybillNum = parcelEntity.waybillNum,
            carrier = parcelEntity.carrier,
            alias = parcelEntity.alias,
            inquiryResult = parcelEntity.inquiryResult,
            inquiryHash = parcelEntity.inquiryHash,
            deliveryStatus = parcelEntity.deliveryStatus,
            arrivalDte = parcelEntity.arrivalDte,
            auditDte = parcelEntity.auditDte,
            status = parcelEntity.status)
    }

    fun parcelToParcelEntity(parcelDTO: ParcelDTO): ParcelEntity
    {
        SopoLog.d(msg = """
            Parcel
            $parcelDTO
            parcel Carrier >>> ${parcelDTO.carrier}
        """.trimIndent())

        return ParcelEntity(
            regDt = parcelDTO.parcelId.regDt,
            parcelUid = parcelDTO.parcelId.parcelUid,
            userId = parcelDTO.userId,
            waybillNum = parcelDTO.waybillNum,
            carrier = parcelDTO.carrier,
            alias = parcelDTO.alias,
            inquiryResult = parcelDTO.inquiryResult,
            inquiryHash = parcelDTO.inquiryHash,
            deliveryStatus = parcelDTO.deliveryStatus,
            arrivalDte = parcelDTO.arrivalDte.toString(),
            auditDte = parcelDTO.auditDte,
            status = parcelDTO.status ?: 0
        )
    }

    fun parcelEntityToParcelId(parcelEntity: ParcelEntity): ParcelId{
        return ParcelId(regDt = parcelEntity.regDt, parcelUid = parcelEntity.parcelUid)
    }

    fun parcelListToInquiryItemList(parcelDTOList: MutableList<ParcelDTO>): MutableList<InquiryListItem>{
        return parcelDTOList.map {
            InquiryListItem(parcelDTO = it)
        } as MutableList<InquiryListItem>

    }
}