package com.delivery.sopo.models.mapper

import com.delivery.sopo.networks.dto.TimeCountDTO
import com.delivery.sopo.data.repository.database.room.entity.ParcelCntInfoEntity
import com.delivery.sopo.models.inquiry.InquiryListItem
import com.delivery.sopo.models.parcel.ParcelDTO

object TimeCountMapper
{
    fun timeCountDtoToTimeCountEntity(timeCountDTO: TimeCountDTO): ParcelCntInfoEntity
    {
        return ParcelCntInfoEntity(
            timeCountDTO.time,
            timeCountDTO.count
        )
    }

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