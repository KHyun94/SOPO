package com.delivery.sopo.models.mapper

import com.delivery.sopo.data.database.room.dto.DeliveredParcelHistory
import com.delivery.sopo.data.database.room.entity.CompletedParcelHistoryEntity
import com.delivery.sopo.data.database.room.entity.ParcelEntity
import com.delivery.sopo.data.database.room.entity.ParcelStatusEntity
import com.delivery.sopo.extensions.fromJson
import com.delivery.sopo.extensions.toJson
import com.delivery.sopo.models.parcel.Parcel
import com.delivery.sopo.models.parcel.tracking_info.TrackingInfo

object ParcelMapper
{
    fun parcelEntityToObject(req: ParcelEntity): Parcel.Common
    {
        val fromJson = req.inquiryResult?.fromJson<TrackingInfo?>()
        return with(req) { Parcel.Common(parcelId = parcelId, userId = userId, waybillNum = waybillNum, carrier = carrier, alias = alias, trackingInfo = fromJson, inquiryHash = inquiryHash, deliveryStatus = deliveryStatus, regDte = regDte, arrivalDte = arrivalDte, auditDte = auditDte, status = status, reported = true) }
    }

    fun parcelObjectToEntity(req: Parcel.Common): ParcelEntity
    {

        val toJson = req.trackingInfo?.toJson()

        return with(req) {
            ParcelEntity(parcelId, userId, waybillNum, carrier, alias, toJson, inquiryHash, deliveryStatus, arrivalDte
                ?: "", regDte, auditDte, status ?: 0)
        }
    }

    fun parcelStatusEntityToObject(req: ParcelStatusEntity): Parcel.Status
    {
        return with(req) { Parcel.Status(parcelId = parcelId, isBeDelete = isBeDelete, updatableStatus = updatableStatus, unidentifiedStatus = unidentifiedStatus, deliveredStatus = deliveredStatus, isNowVisible = isNowVisible, auditDte = auditDte) }
    }

    fun parcelStatusObjectToEntity(req: Parcel.Status): ParcelStatusEntity
    {
        return with(req) { ParcelStatusEntity(parcelId = parcelId, isBeDelete = isBeDelete, updatableStatus = updatableStatus, unidentifiedStatus = unidentifiedStatus, deliveredStatus = deliveredStatus, isNowVisible = isNowVisible, auditDte = auditDte) }
    }

    fun parcelToParcelStatus(parcel: Parcel.Common): Parcel.Status
    {
        return with(parcel) { Parcel.Status(parcelId = parcelId) }
    }

    fun completeParcelStatusEntityToDTO(entity: CompletedParcelHistoryEntity): DeliveredParcelHistory
    {
        val dates = entity.date.split('-')
        return with(entity) { DeliveredParcelHistory(date = date, count = count, visibility = visibility, status = status, auditDte = auditDte) }
    }

    fun completeParcelStatusDTOToEntity(dto: DeliveredParcelHistory): CompletedParcelHistoryEntity
    {
        return with(dto) { CompletedParcelHistoryEntity("${parseYear()}-${parseMonth()}", count, visibility, status, auditDte) }
    }

    fun parcelToParcelManagementEntity(parcelResponse: Parcel.Common): ParcelStatusEntity
    {
        return ParcelStatusEntity(parcelId = parcelResponse.parcelId)
    }

    fun parcelEntityToParcelManagementEntity(parcelEntity: ParcelEntity): ParcelStatusEntity
    {
        return ParcelStatusEntity(parcelId = parcelEntity.parcelId)
    }

    fun parcelEntityToParcel(parcelEntity: ParcelEntity): Parcel.Common
    {
        val fromJson = parcelEntity.inquiryResult?.fromJson<TrackingInfo?>()

        return Parcel.Common(parcelId = parcelEntity.parcelId, userId = parcelEntity.userId, waybillNum = parcelEntity.waybillNum, carrier = parcelEntity.carrier, alias = parcelEntity.alias, trackingInfo = fromJson, inquiryHash = parcelEntity.inquiryHash, deliveryStatus = parcelEntity.deliveryStatus, arrivalDte = parcelEntity.arrivalDte, auditDte = parcelEntity.auditDte, status = parcelEntity.status, regDte = parcelEntity.regDte, reported = true)
    }

    fun parcelToParcelEntity(parcel: Parcel.Common): ParcelEntity
    {
        val toJson = parcel.trackingInfo?.toJson()

        return ParcelEntity(parcelId = parcel.parcelId, userId = parcel.userId, waybillNum = parcel.waybillNum, carrier = parcel.carrier, alias = parcel.alias, inquiryResult = toJson, inquiryHash = parcel.inquiryHash, deliveryStatus = parcel.deliveryStatus, arrivalDte = parcel.arrivalDte.toString(), auditDte = parcel.auditDte, regDte = parcel.regDte, status = parcel.status
            ?: 0)
    }

    fun parcelEntityToParcelId(parcelEntity: ParcelEntity): Int
    {
        return parcelEntity.parcelId
    }


}