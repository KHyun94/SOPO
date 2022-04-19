package com.delivery.sopo.usecase.parcel.remote

import com.delivery.sopo.data.repository.local.datasource.ParcelManagementRepository
import com.delivery.sopo.data.repository.local.repository.ParcelRepository
import com.delivery.sopo.models.parcel.Parcel
import com.delivery.sopo.util.SopoLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UpdateParcelsUseCase(private val parcelRepo: ParcelRepository, private val parcelStatusRepo: ParcelManagementRepository)
{
    suspend operator fun invoke(parcelIds: List<Int>) = withContext(Dispatchers.IO) {
        SopoLog.i("UpdateParcelsUseCase(...)")
        val parcels: List<Parcel.Common> = parcelRepo.getRemoteParcelById(parcelIds = parcelIds)
        parcelStatusRepo.updateUnidentifiedStatus(parcels)
        parcelRepo.insertParcels(parcels)
        parcelRepo.updateParcels(parcels)
        parcelRepo.getRemoteMonths()

        val reportParcelIds = parcels.mapNotNull {
            if(!it.reported) it.parcelId else null
        }

        CoroutineScope(Dispatchers.IO).launch { parcelRepo.reportParcelStatus(reportParcelIds) }
    }
}