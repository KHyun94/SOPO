package com.delivery.sopo.usecase.parcel.remote

import com.delivery.sopo.data.repository.local.repository.ParcelManagementRepoImpl
import com.delivery.sopo.data.repository.local.repository.ParcelRepository
import com.delivery.sopo.util.SopoLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DeleteParcelsUseCase(private val parcelRepo: ParcelRepository, private val parcelStatusRepo:ParcelManagementRepoImpl)
{
    suspend operator fun invoke() = withContext(Dispatchers.IO) {
        val parcelIds = parcelStatusRepo.getDeletableParcelStatuses().map { it.parcelId }
        parcelRepo.deleteRemoteParcels(parcelIds)

        val parcels = parcelIds.mapNotNull(parcelRepo::getParcelById).toTypedArray()
        val parcelStatuses = parcelIds.mapNotNull(parcelStatusRepo::getParcelStatusById).toTypedArray()

        parcelRepo.delete(*parcels)
        parcelStatusRepo.delete(*parcelStatuses)
    }

    suspend operator fun invoke(parcelId: Int) = withContext(Dispatchers.IO) {
        parcelRepo.deleteRemoteParcels(listOf(parcelId))

        parcelRepo.getParcelById(parcelId)?.let { parcel ->
            parcelRepo.delete(parcel)
        }

        parcelStatusRepo.getParcelStatusById(parcelId)?.let { parcelStatus ->
            parcelStatusRepo.delete(parcelStatus)
        }
    }
}