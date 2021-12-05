package com.delivery.sopo.usecase.parcel.remote

import com.delivery.sopo.data.repository.local.repository.ParcelRepository
import com.delivery.sopo.models.UpdateParcelAliasRequest
import com.delivery.sopo.models.mapper.ParcelMapper
import com.delivery.sopo.util.SopoLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UpdateParcelAliasUseCase(private val parcelRepo: ParcelRepository)
{
    suspend operator fun invoke(parcelId:Int, parcelAlias: String) = withContext(Dispatchers.IO) {
        SopoLog.i("UpdateParcelAliasUseCase(...)")

        parcelRepo.updateParcelAlias(parcelId, parcelAlias)

        withContext(Dispatchers.Default) {
            val parcelEntity =
                (parcelRepo.getLocalParcelById(parcelId) ?: return@withContext).run {
                    alias = parcelAlias
                    ParcelMapper.parcelObjectToEntity(this)
                }
            parcelRepo.update(parcelEntity)
        }

    }
}