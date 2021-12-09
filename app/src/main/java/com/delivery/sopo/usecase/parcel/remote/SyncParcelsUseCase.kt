package com.delivery.sopo.usecase.parcel.remote

import com.delivery.sopo.data.repository.local.repository.ParcelRepository
import com.delivery.sopo.util.SopoLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SyncParcelsUseCase(private val parcelRepo: ParcelRepository)
{
    suspend operator fun invoke() = withContext(Dispatchers.IO){
        SopoLog.i("SyncParcelsUseCase(...)")
        val remoteParcels = parcelRepo.getRemoteParcelByOngoing().apply {
            SopoLog.d("테스트 로그 시발 ${this.joinToString()}")
        }
        parcelRepo.updateUnidentifiedStatus(remoteParcels)
        parcelRepo.insertParcelsFromServer(remoteParcels)
        parcelRepo.updateParcelsFromServer(remoteParcels)
    }
}