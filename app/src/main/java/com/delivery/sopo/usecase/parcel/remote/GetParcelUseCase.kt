package com.delivery.sopo.usecase.parcel.remote

import com.delivery.sopo.data.repository.local.repository.ParcelManagementRepoImpl
import com.delivery.sopo.data.repository.local.repository.ParcelRepository
import com.delivery.sopo.models.parcel.Parcel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GetParcelUseCase(private val parcelRepo: ParcelRepository, private val parcelStatusRepo: ParcelManagementRepoImpl)
{
    suspend operator fun invoke(parcelId: Int) = withContext(Dispatchers.IO) {
        val remoteParcel = parcelRepo.getRemoteParcelById(parcelId = parcelId)
        val localParcel = parcelRepo.getParcelById(remoteParcel.parcelId)

        insertParcels(listOf(remoteParcel))
        updateParcels(listOf(remoteParcel))

         if(!remoteParcel.reported)
         {
             CoroutineScope(Dispatchers.IO).launch {
                 if(!remoteParcel.reported) return@launch
                 parcelRepo.reportParcelStatus(listOf(parcelId))
             }
         }

        return@withContext remoteParcel
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