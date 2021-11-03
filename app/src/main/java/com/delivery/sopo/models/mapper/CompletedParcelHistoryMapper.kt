package com.delivery.sopo.models.mapper

import com.delivery.sopo.data.repository.database.room.dto.CompletedParcelHistory

import com.delivery.sopo.data.repository.database.room.entity.CompletedParcelHistoryEntity
import com.delivery.sopo.models.inquiry.InquiryListItem
import com.delivery.sopo.models.parcel.ParcelResponse

object CompletedParcelHistoryMapper
{
    fun dtoToEntity(dto: CompletedParcelHistory) =  with(dto) { CompletedParcelHistoryEntity(date, count) }
    fun timeCountEntityListToInquiryItemList(parcelResponseList: MutableList<ParcelResponse>): MutableList<InquiryListItem>{
        return parcelResponseList.map {
            InquiryListItem(parcelResponse = it)
        } as MutableList<InquiryListItem>
    }

    fun arrivalDateToTime(arrivalDate: String): String{
        return arrivalDate.substring(0, 7)
    }

    fun timeCountToInquiryDate(time: String): String{
        return time.replace("-", "")
    }
}