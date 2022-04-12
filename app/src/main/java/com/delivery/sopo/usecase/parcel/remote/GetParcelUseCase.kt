package com.delivery.sopo.usecase.parcel.remote

import com.delivery.sopo.data.repository.local.repository.ParcelManagementRepoImpl
import com.delivery.sopo.data.repository.local.repository.ParcelRepository
import com.delivery.sopo.models.mapper.ParcelMapper
import com.delivery.sopo.util.TimeUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetParcelUseCase(private val parcelRepo: ParcelRepository, private val parcelManagementRepoImpl: ParcelManagementRepoImpl)
{
    suspend operator fun invoke(parcelId: Int) = withContext(Dispatchers.IO) {
        val remoteParcel = parcelRepo.getRemoteParcelById(parcelId = parcelId)
        val localParcel = parcelRepo.getLocalParcelById(remoteParcel.parcelId)

        if(localParcel == null)
        {
            val status = ParcelMapper.parcelToParcelStatus(remoteParcel)
            parcelRepo.insert(remoteParcel)
            parcelManagementRepoImpl.insertParcelStatus(status)

            return@withContext remoteParcel
        }

        val status = parcelManagementRepoImpl.getParcelStatus(parcelId = localParcel.parcelId).apply { auditDte = TimeUtil.getDateTime() }

        parcelRepo.update(parcel = remoteParcel)
        parcelManagementRepoImpl.update(status)

        return@withContext remoteParcel
    }
}