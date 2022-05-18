package com.delivery.sopo.domain.usecase.parcel.local

import com.delivery.sopo.data.repositories.local.repository.ParcelRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetLocalParcelUseCase(private val parcelRepo: ParcelRepository)
{
    suspend operator fun invoke(parcelId: Int) = withContext(Dispatchers.Default) {
        val parcel = parcelRepo.getParcelById(parcelId = parcelId)
        return@withContext parcel
    }
}