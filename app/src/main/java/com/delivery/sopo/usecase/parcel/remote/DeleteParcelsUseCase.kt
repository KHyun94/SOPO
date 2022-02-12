package com.delivery.sopo.usecase.parcel.remote

import com.delivery.sopo.data.repository.local.repository.ParcelManagementRepoImpl
import com.delivery.sopo.data.repository.local.repository.ParcelRepository
import com.delivery.sopo.util.SopoLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DeleteParcelsUseCase(private val parcelRepo: ParcelRepository, private val parcelStatusRepo:ParcelManagementRepoImpl)
{
    suspend operator fun invoke() = withContext(Dispatchers.IO) {
        SopoLog.i("DeleteParcelsUseCase(...)")

        val parcelIds = parcelStatusRepo.getDeletableParcelStatuses().map { it.parcelId }
        parcelRepo.deleteRemoteParcels(parcelIds)
        parcelRepo.deleteLocalParcels(parcelIds)
        parcelStatusRepo.delete(parcelIds)
    }
}