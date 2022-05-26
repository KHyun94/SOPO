package com.delivery.sopo.domain.usecase.parcel.remote

import com.delivery.sopo.data.repositories.parcels.ParcelRepository
import com.delivery.sopo.models.parcel.Parcel
import com.delivery.sopo.util.SopoLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UpdateParcelUseCase(private val parcelRepository: ParcelRepository)
{
    suspend fun getLocalParcel(parcelId: Int): Parcel.Common?
    {
        return parcelRepository.getParcel(parcelId = parcelId)
    }

    suspend operator fun invoke(parcelId: Int) = withContext(Dispatchers.IO) {
        SopoLog.i("호출")
        return@withContext parcelRepository.updateParcel(parcelId)
    }
}