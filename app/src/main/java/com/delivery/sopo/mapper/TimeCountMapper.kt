package com.delivery.sopo.mapper

import com.delivery.sopo.networks.dto.TimeCountDTO
import com.delivery.sopo.database.room.entity.TimeCountEntity
import com.delivery.sopo.models.inquiry.InquiryListItem
import com.delivery.sopo.models.parcel.Parcel

object TimeCountMapper
{
    fun timeCountDtoToTimeCountEntity(timeCountDTO: TimeCountDTO): TimeCountEntity
    {
        return TimeCountEntity(
            timeCountDTO.time,
            timeCountDTO.count
        )
    }

    fun timeCountEntityListToInquiryItemList(parcelList: MutableList<Parcel>): MutableList<InquiryListItem>{
        return parcelList.map {
            InquiryListItem(parcel = it)
        } as MutableList<InquiryListItem>
    }

    fun arrivalDateToTime(arrivalDate: String): String{
        return arrivalDate.substring(0, 7)
    }

    fun timeCountToInquiryDate(time: String): String{
        return time.replace("-", "")
    }
}