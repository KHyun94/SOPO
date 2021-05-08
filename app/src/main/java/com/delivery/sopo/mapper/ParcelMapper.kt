package com.delivery.sopo.mapper

import com.delivery.sopo.database.room.entity.ParcelEntity
import com.delivery.sopo.database.room.entity.ParcelManagementEntity
import com.delivery.sopo.models.inquiry.InquiryListItem
import com.delivery.sopo.models.parcel.Parcel
import com.delivery.sopo.models.parcel.ParcelId
import com.delivery.sopo.util.SopoLog

object ParcelMapper
{
    fun parcelToParcelManagementEntity(parcel: Parcel): ParcelManagementEntity
    {
        return ParcelManagementEntity(
            regDt = parcel.parcelId.regDt,
            parcelUid = parcel.parcelId.parcelUid
        )
    }

    fun parcelEntityToParcelManagementEntity(parcelEntity: ParcelEntity): ParcelManagementEntity
    {
        return ParcelManagementEntity(
            regDt = parcelEntity.regDt,
            parcelUid = parcelEntity.parcelUid
        )
    }

    fun parcelEntityToParcel(parcelEntity: ParcelEntity): Parcel{
        return Parcel(parcelId = ParcelId(regDt = parcelEntity.regDt, parcelUid = parcelEntity.parcelUid),
            userName = parcelEntity.userName,
            waybillNum = parcelEntity.waybillNum,
            carrier = parcelEntity.carrier,
            parcelAlias = parcelEntity.parcelAlias,
            inquiryResult = parcelEntity.inquiryResult,
            inquiryHash = parcelEntity.inquiryHash,
            deliveryStatus = parcelEntity.deliveryStatus,
            arrivalDte = parcelEntity.arrivalDte,
            auditDte = parcelEntity.auditDte,
            status = parcelEntity.status)
    }

    fun parcelToParcelEntity(parcel: Parcel): ParcelEntity
    {
        SopoLog.d(msg = """
            Parcel
            $parcel
            parcel Carrier >>> ${parcel.carrier}
        """.trimIndent())

        return ParcelEntity(
            regDt = parcel.parcelId.regDt,
            parcelUid = parcel.parcelId.parcelUid,
            userName = parcel.userName,
            waybillNum = parcel.waybillNum,
            carrier = parcel.carrier,
            parcelAlias = parcel.parcelAlias,
            inquiryResult = parcel.inquiryResult,
            inquiryHash = parcel.inquiryHash,
            deliveryStatus = parcel.deliveryStatus,
            arrivalDte = parcel.arrivalDte.toString(),
            auditDte = parcel.auditDte,
            status = parcel.status ?: 0
        )
    }

    fun parcelEntityToParcelId(parcelEntity: ParcelEntity): ParcelId{
        return ParcelId(regDt = parcelEntity.regDt, parcelUid = parcelEntity.parcelUid)
    }

    fun parcelListToInquiryItemList(parcelList: MutableList<Parcel>): MutableList<InquiryListItem>{
        return parcelList.map {
            InquiryListItem(parcel = it)
        } as MutableList<InquiryListItem>

    }
}