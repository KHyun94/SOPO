package com.delivery.sopo.usecase.parcel.remote

import com.delivery.sopo.data.repository.local.datasource.ParcelManagementRepository
import com.delivery.sopo.data.repository.local.repository.ParcelManagementRepoImpl
import com.delivery.sopo.data.repository.local.repository.ParcelRepository
import com.delivery.sopo.models.parcel.Parcel
import com.delivery.sopo.util.SopoLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SyncParcelsUseCase(private val parcelRepo: ParcelRepository, private val parcelStatusRepo: ParcelManagementRepoImpl)
{
    suspend operator fun invoke() = withContext(Dispatchers.IO){
        SopoLog.i("SyncParcelsUseCase(...)")
        val remoteParcels = parcelRepo.getOngoingParcelsFromRemote()

        parcelStatusRepo.updateUnidentifiedStatus(remoteParcels)
        insertParcels(remoteParcels)
        updateParcels(remoteParcels)

        val reportParcelIds = remoteParcels.mapNotNull {
            if(!it.reported) it.parcelId else null
        }

        CoroutineScope(Dispatchers.IO).launch {
            if(reportParcelIds.isEmpty()) return@launch
            parcelRepo.reportParcelStatus(reportParcelIds)
        }
    }

    private fun insertParcels(parcels:List<Parcel.Common>){
        val insertParcels = parcels.filterNot(parcelRepo::hasLocalParcel)
        val insertParcelStatuses = insertParcels.map(parcelStatusRepo::makeParcelStatus)
        parcelRepo.insert(*insertParcels.toTypedArray())
        parcelStatusRepo.insertParcelStatuses(insertParcelStatuses)
    }

    suspend fun updateParcels(parcels: List<Parcel.Common>)
    {
        val notExistParcelIds = parcelRepo.getNotExistParcels(parcels = parcels).map { it.parcelId }
        val notExistParcels = parcelRepo.getRemoteParcelById(parcelIds = notExistParcelIds)

        val updateParcels = parcels.filter(parcelRepo::compareInquiryHash) + notExistParcels
        val updateParcelStatuses = updateParcels.map(parcelStatusRepo::makeParcelStatus)

        parcelRepo.update(*updateParcels.toTypedArray())
        parcelStatusRepo.updateParcelStatuses(updateParcelStatuses)
    }
}