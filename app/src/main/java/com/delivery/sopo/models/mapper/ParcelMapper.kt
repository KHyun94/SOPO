package com.delivery.sopo.models.mapper

import com.delivery.sopo.data.repository.database.room.entity.ParcelEntity
import com.delivery.sopo.data.repository.database.room.entity.ParcelStatusEntity
import com.delivery.sopo.models.inquiry.InquiryListItem
import com.delivery.sopo.models.parcel.ParcelDTO
import com.delivery.sopo.util.SopoLog

object ParcelMapper
{
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
            regDt = parcelEntity.auditDte
//        regDt = parcelEntity.regDt
        )
    }

    fun parcelToParcelEntity(parcelDTO: ParcelDTO): ParcelEntity
    {
        SopoLog.d(msg = """
            Parcel
            $parcelDTO
            parcel Carrier >>> ${parcelDTO.carrier}
        """.trimIndent())

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
//            regDt = parcelDTO.regDt,
            status = parcelDTO.status ?: 0
        )
    }

    fun parcelEntityToParcelId(parcelEntity: ParcelEntity): Int{
        return parcelEntity.parcelId
    }

    fun parcelListToInquiryItemList(parcelDTOList: MutableList<ParcelDTO>): MutableList<InquiryListItem>{
        return parcelDTOList.map {
            InquiryListItem(parcelDTO = it)
        } as MutableList<InquiryListItem>

    }
}