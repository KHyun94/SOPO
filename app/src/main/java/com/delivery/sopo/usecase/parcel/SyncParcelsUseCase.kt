package com.delivery.sopo.usecase.parcel

import com.delivery.sopo.data.repository.local.repository.ParcelRepository
import com.delivery.sopo.util.SopoLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SyncParcelsUseCase(private val parcelRepo: ParcelRepository)
{
    operator fun invoke() = CoroutineScope(Dispatchers.IO).launch {
        SopoLog.i("SyncParcelsUseCase(...)")
        val remoteParcels = parcelRepo.getRemoteParcelByOngoing()
        parcelRepo.updateUnidentifiedStatus(remoteParcels)
        parcelRepo.insertParcelsFromServer(remoteParcels)
        parcelRepo.updateParcelsFromServer(remoteParcels)
    }
}