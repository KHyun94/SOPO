package com.delivery.sopo.models.mapper

import com.delivery.sopo.data.repository.database.room.dto.CompletedParcelHistory

import com.delivery.sopo.data.repository.database.room.entity.CompletedParcelHistoryEntity
import com.delivery.sopo.models.inquiry.InquiryListItem
import com.delivery.sopo.models.parcel.ParcelDTO

object CompletedParcelHistoryMapper
{
    fun dtoToEntity(dto: CompletedParcelHistory) =  with(dto) { CompletedParcelHistoryEntity(date, count) }
    fun timeCountEntityListToInquiryItemList(parcelDTOList: MutableList<ParcelDTO>): MutableList<InquiryListItem>{
        return parcelDTOList.map {
            InquiryListItem(parcelDTO = it)
        } as MutableList<InquiryListItem>
    }

    fun arrivalDateToTime(arrivalDate: String): String{
        return arrivalDate.substring(0, 7)
    }

    fun timeCountToInquiryDate(time: String): String{
        return time.replace("-", "")
    }
}