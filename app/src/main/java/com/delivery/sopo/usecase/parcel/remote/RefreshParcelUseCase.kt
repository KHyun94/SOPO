package com.delivery.sopo.usecase.parcel.remote

import com.delivery.sopo.data.repository.local.repository.ParcelRepository
import com.delivery.sopo.util.SopoLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RefreshParcelUseCase(private val parcelRepo: ParcelRepository)
{
    suspend operator fun invoke(parcelId: Int) = withContext(Dispatchers.IO) {
        SopoLog.i("RefreshParcelsUseCase(...)")

        val parcel = parcelRepo.requestParcelForRefresh(parcelId = parcelId)

        parcelRepo.updateParcelsFromServer(listOf(parcel))
    }
}