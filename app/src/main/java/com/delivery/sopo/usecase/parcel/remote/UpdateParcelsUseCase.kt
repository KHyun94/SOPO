package com.delivery.sopo.usecase.parcel.remote

import com.delivery.sopo.data.repository.local.repository.ParcelRepository
import com.delivery.sopo.models.UpdateParcelAliasRequest
import com.delivery.sopo.models.mapper.ParcelMapper
import com.delivery.sopo.models.parcel.Parcel
import com.delivery.sopo.util.SopoLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UpdateParcelsUseCase(private val parcelRepo: ParcelRepository)
{
    suspend operator fun invoke(parcelIds: List<Int>) = withContext(Dispatchers.IO) {
        SopoLog.i("UpdateParcelsUseCase(...)")
        val parcels: List<Parcel.Common> = parcelRepo.getRemoteParcelById(parcelIds = parcelIds)
        parcelRepo.updateUnidentifiedStatus(parcels)
        parcelRepo.insertParcelsFromServer(parcels)
        parcelRepo.updateParcelsFromServer(parcels)
        parcelRepo.getRemoteMonths()
    }
}