package com.delivery.sopo.usecase.parcel.local

import com.delivery.sopo.data.repository.local.repository.ParcelRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetLocalParcelUseCase(private val parcelRepo: ParcelRepository)
{
    suspend operator fun invoke(parcelId: Int) = withContext(Dispatchers.Default) {
        val parcel = parcelRepo.getParcelById(parcelId = parcelId)
        return@withContext parcel
    }
}