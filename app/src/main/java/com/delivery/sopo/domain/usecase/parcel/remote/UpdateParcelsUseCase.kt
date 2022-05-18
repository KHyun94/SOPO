package com.delivery.sopo.domain.usecase.parcel.remote

import com.delivery.sopo.data.repositories.local.repository.ParcelManagementRepoImpl
import com.delivery.sopo.data.repositories.local.repository.ParcelRepository
import com.delivery.sopo.models.parcel.Parcel
import com.delivery.sopo.util.SopoLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UpdateParcelsUseCase(private val parcelRepo: ParcelRepository, private val parcelStatusRepo: ParcelManagementRepoImpl)
{
    suspend operator fun invoke(parcelIds: List<Int>) = withContext(Dispatchers.IO) {
        SopoLog.i("호출 [data:${parcelIds.joinToString(", ")}")

        val parcels: List<Parcel.Common> = parcelRepo.getRemoteParcelById(parcelIds = parcelIds) //        parcelStatusRepo.updateUnidentifiedStatus(parcels)
        insertParcels(parcels)
        updateParcels(parcels) //        parcelRepo.getRemoteMonths()

        val reportParcelIds = parcels.mapNotNull {
            if(!it.reported) it.parcelId else null
        }

        launch {
            if(reportParcelIds.isEmpty()) return@launch
            parcelRepo.reportParcelStatus(reportParcelIds)
        }
    }

    private fun insertParcels(parcels: List<Parcel.Common>)
    {
        val insertParcels = parcels.filterNot(parcelRepo::hasLocalParcel)
        val insertParcelStatuses = insertParcels.map(parcelStatusRepo::makeParcelStatus)
        parcelRepo.insert(*insertParcels.toTypedArray())
        parcelStatusRepo.insertParcelStatuses(insertParcelStatuses)
    }

    suspend fun updateParcels(parcels: List<Parcel.Common>)
    {
        val notExistParcelIds = parcelRepo.getNotExistParcels(parcels = parcels).map { it.parcelId }
        val notExistParcels = parcelRepo.getRemoteParcelById(parcelIds = notExistParcelIds)

        val updateParcels = parcels.filter(parcelRepo::compareInquiryHash) + notExistParcels
        val updateParcelStatuses = updateParcels.map(parcelStatusRepo::makeParcelStatus)
        parcelRepo.update(*updateParcels.toTypedArray())
        parcelStatusRepo.updateParcelStatuses(updateParcelStatuses)
    }
}