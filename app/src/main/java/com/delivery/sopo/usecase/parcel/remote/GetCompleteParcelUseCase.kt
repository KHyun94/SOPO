package com.delivery.sopo.usecase.parcel.remote

import com.delivery.sopo.data.repository.local.datasource.ParcelManagementRepository
import com.delivery.sopo.data.repository.local.repository.ParcelManagementRepoImpl
import com.delivery.sopo.data.repository.local.repository.ParcelRepository
import com.delivery.sopo.models.inquiry.PagingManagement
import com.delivery.sopo.models.parcel.Parcel
import com.delivery.sopo.util.SopoLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GetCompleteParcelUseCase(private val parcelRepo: ParcelRepository, private val parcelStatusRepo: ParcelManagementRepoImpl)
{
    suspend operator fun invoke(currentPagingManagement: PagingManagement): List<Parcel.Common> =
        withContext(Dispatchers.IO) {
            SopoLog.i("GetCompleteParcelUseCase(...)")

            val completeParcels = parcelRepo.getCompleteParcelsByRemote(page = currentPagingManagement.pagingNum, inquiryDate = currentPagingManagement.inquiryDate)

            insertParcels(completeParcels)
            updateParcels(completeParcels)

            val reportParcelIds = completeParcels.mapNotNull {
                if(!it.reported) it.parcelId else null
            }

            CoroutineScope(Dispatchers.IO).launch { parcelRepo.reportParcelStatus(reportParcelIds) }

            return@withContext completeParcels
        }

    private fun insertParcels(parcels:List<Parcel.Common>){
        val insertParcels = parcels.filterNot(parcelRepo::hasLocalParcel)
        val insertParcelStatuses = insertParcels.map(parcelStatusRepo::makeParcelStatus)
        parcelRepo.insert(*insertParcels.toTypedArray())
        parcelStatusRepo.insertParcelStatuses(insertParcelStatuses)
    }

    suspend fun updateParcels(parcels: List<Parcel.Common>)
    {
        val updateParcels = parcels.filter(parcelRepo::compareInquiryHash)
        val updateParcelStatuses = updateParcels.map(parcelStatusRepo::makeParcelStatus)
        parcelRepo.update(*updateParcels.toTypedArray())
        parcelStatusRepo.updateParcelStatuses(updateParcelStatuses)
    }
}