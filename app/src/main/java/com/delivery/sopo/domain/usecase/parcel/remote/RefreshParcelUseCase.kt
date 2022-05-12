package com.delivery.sopo.domain.usecase.parcel.remote

import com.delivery.sopo.data.repository.local.repository.ParcelRepository
import com.delivery.sopo.util.SopoLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RefreshParcelUseCase(private val parcelRepo: ParcelRepository)
{
    suspend operator fun invoke(parcelId: Int) = withContext(Dispatchers.IO) {
        SopoLog.i("RefreshParcelUseCase(...)")
        val updatableParcel = parcelRepo.requestParcelForRefresh(parcelId = parcelId)
        parcelRepo.update(updatableParcel.parcel)
        return@withContext updatableParcel.parcel
    }
}