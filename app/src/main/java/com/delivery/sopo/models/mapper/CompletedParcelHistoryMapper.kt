package com.delivery.sopo.models.mapper

import com.delivery.sopo.data.database.room.dto.DeliveredParcelHistory

import com.delivery.sopo.data.database.room.entity.CompletedParcelHistoryEntity

object CompletedParcelHistoryMapper
{
    fun dtoToEntity(dto: DeliveredParcelHistory) =  with(dto) { CompletedParcelHistoryEntity(date, count) }
//    fun timeCountEntityListToInquiryItemList(parcelResponseList: MutableList<Parcel.Common>): MutableList<InquiryListItem>{
//        return parcelResponseList.map {
//            InquiryListItem(parcelResponse = it)
//        } as MutableList<InquiryListItem>
//    }

    fun arrivalDateToTime(arrivalDate: String): String{
        return arrivalDate.substring(0, 7)
    }

    fun timeCountToInquiryDate(time: String): String{
        return time.replace("-", "")
    }
}