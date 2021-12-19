package com.delivery.sopo.usecase.parcel.local

import androidx.lifecycle.Transformations
import com.delivery.sopo.data.repository.local.repository.ParcelRepository
import com.delivery.sopo.enums.DeliveryStatusEnum
import com.delivery.sopo.models.inquiry.InquiryListItem
import com.delivery.sopo.models.mapper.ParcelMapper
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.viewmodels.inquiry.InquiryViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

class GetOngoingParcelByLocalUseCase(private val parcelRepo: ParcelRepository)
{
    suspend operator fun invoke() = withContext(Dispatchers.Default) {
            SopoLog.i("GetOngoingParcelByLocalUseCase(...)")

            parcelRepo.getLocalOngoingParcels().map {
//                sortByDeliveryStatus(it).toMutableList()
            }

            Transformations.map(parcelRepo.getLocalOngoingParcelsAsLiveData()) { parcelList ->
                val list: MutableList<InquiryListItem> =
                    ParcelMapper.parcelListToInquiryItemList(parcelList)

            }
        }

    private fun sortByDeliveryStatus(list: List<InquiryListItem>): List<InquiryListItem>
    {

        val sortedList = mutableListOf<InquiryListItem>()
        val multiList =
            listOf<MutableList<InquiryListItem>>(mutableListOf(), mutableListOf(), mutableListOf(), mutableListOf(), mutableListOf(), mutableListOf(), mutableListOf())

        val elseList = list.asSequence().filter { item ->

            if(item.parcelResponse.deliveryStatus == DeliveryStatusEnum.DELIVERED.CODE)
            {
                multiList[0].add(item)
            }

            item.parcelResponse.deliveryStatus != DeliveryStatusEnum.DELIVERED.CODE
        }.filter { item ->
            if(item.parcelResponse.deliveryStatus == DeliveryStatusEnum.OUT_FOR_DELIVERY.CODE)
            {
                multiList[1].add(item)
            }

            item.parcelResponse.deliveryStatus != DeliveryStatusEnum.OUT_FOR_DELIVERY.CODE
        }.filter { item ->
            if(item.parcelResponse.deliveryStatus == DeliveryStatusEnum.IN_TRANSIT.CODE)
            {
                multiList[2].add(item)
            }

            item.parcelResponse.deliveryStatus != DeliveryStatusEnum.IN_TRANSIT.CODE
        }.filter { item ->
            if(item.parcelResponse.deliveryStatus == DeliveryStatusEnum.AT_PICKUP.CODE)
            {
                multiList[3].add(item)
            }

            item.parcelResponse.deliveryStatus != DeliveryStatusEnum.AT_PICKUP.CODE
        }.filter { item ->
            if(item.parcelResponse.deliveryStatus == DeliveryStatusEnum.INFORMATION_RECEIVED.CODE)
            {
                multiList[4].add(item)
            }

            item.parcelResponse.deliveryStatus != DeliveryStatusEnum.INFORMATION_RECEIVED.CODE
        }.filter { item ->
            if(item.parcelResponse.deliveryStatus == DeliveryStatusEnum.NOT_REGISTERED.CODE)
            {
                //                SopoLog.d("미등록(not_register)[${item.parcelDTO.alias}]")
                multiList[5].add(item)
            }

            item.parcelResponse.deliveryStatus != DeliveryStatusEnum.NOT_REGISTERED.CODE
        }.toList()

        multiList[6].addAll(elseList)

        multiList.forEach {
            Collections.sort(it, InquiryViewModel.SortByDate())
            sortedList.addAll(it)
        }
        return sortedList
    }

    inner class SortByDate: Comparator<InquiryListItem>
    {
        override fun compare(p0: InquiryListItem, p1: InquiryListItem): Int
        {
            return p0.parcelResponse.auditDte.compareTo(p1.parcelResponse.auditDte)
        }
    }

}


