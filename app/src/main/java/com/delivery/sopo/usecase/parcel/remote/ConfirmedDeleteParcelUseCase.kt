package com.delivery.sopo.usecase.parcel.remote

import com.delivery.sopo.data.repository.local.repository.ParcelRepository
import com.delivery.sopo.util.SopoLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ConfirmedDeleteParcelUseCase(private val parcelRepo: ParcelRepository)
{
    suspend operator fun invoke() = withContext(Dispatchers.IO) {
        SopoLog.i("ConfirmedDeleteParcelUseCase(...)")

        val deletableParcelIds = parcelRepo.getDeletableParcelIds()

        if(deletableParcelIds.isEmpty()) return@withContext

        parcelRepo.deleteLocalParcels(parcelIds = deletableParcelIds)
        parcelRepo.deleteRemoteParcels(parcelIds = deletableParcelIds)
    }
}