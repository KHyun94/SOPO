package com.delivery.sopo.mapper

import com.delivery.sopo.models.entity.ParcelEntity
import com.delivery.sopo.models.parcel.Parcel
import com.delivery.sopo.models.parcel.ParcelId

object ParcelMapper
{
    fun entityToObject(parcelEntity: ParcelEntity): Parcel{
        return Parcel(parcelId = ParcelId(regDt = parcelEntity.regDt, parcelUid = parcelEntity.parcelUid),
            userName = parcelEntity.userName,
            trackNum = parcelEntity.trackNum,
            carrier = parcelEntity.carrier,
            parcelAlias = parcelEntity.parcelAlias,
            inqueryResult = parcelEntity.inqueryResult,
            inqueryHash = parcelEntity.inqueryHash,
            deliveryStatus = parcelEntity.deliveryStatus,
            arrivalDte = parcelEntity.arrivalDte,
            auditDte = parcelEntity.auditDte,
            status = parcelEntity.status)
    }

    fun objectToEntity(parcel: Parcel): ParcelEntity{
        return ParcelEntity(
            regDt = parcel.parcelId.regDt,
            parcelUid = parcel.parcelId.parcelUid,
            userName = parcel.userName,
            trackNum = parcel.trackNum,
            carrier = parcel.carrier,
            parcelAlias = parcel.parcelAlias,
            inqueryResult = parcel.inqueryResult,
            inqueryHash = parcel.inqueryHash,
            deliveryStatus = parcel.deliveryStatus,
            arrivalDte = parcel.arrivalDte.toString(),
            auditDte = parcel.auditDte,
            status = parcel.status ?: 0
        )
    }

    fun entityToParcelId(parcelEntity: ParcelEntity): ParcelId{
        return ParcelId(regDt = parcelEntity.regDt, parcelUid = parcelEntity.parcelUid)
    }
}