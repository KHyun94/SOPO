package com.delivery.sopo.usecase.parcel.remote

import com.delivery.sopo.data.repository.local.repository.ParcelRepository
import com.delivery.sopo.models.inquiry.PagingManagement
import com.delivery.sopo.models.parcel.Parcel
import com.delivery.sopo.util.SopoLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GetCompleteParcelUseCase(private val parcelRepo: ParcelRepository)
{
    suspend operator fun invoke(currentPagingManagement: PagingManagement): List<Parcel.Common> =
        withContext(Dispatchers.IO) {
            SopoLog.i("GetCompleteParcelUseCase(...)")

            val completeParcels = parcelRepo.getCompleteParcelsByRemote(page = currentPagingManagement.pagingNum, inquiryDate = currentPagingManagement.inquiryDate)

            parcelRepo.insertParcels(completeParcels)
            parcelRepo.updateParcels(completeParcels)

            val reportParcelIds = completeParcels.mapNotNull {
                if(!it.reported) it.parcelId else null
            }

            CoroutineScope(Dispatchers.IO).launch { parcelRepo.reportParcelStatus(reportParcelIds) }

            return@withContext completeParcels
        }
}