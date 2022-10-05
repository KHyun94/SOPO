package com.delivery.sopo.domain.usecase.parcel.remote

import com.delivery.sopo.data.repositories.local.repository.ParcelRepository
import com.delivery.sopo.util.SopoLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RefreshParcelsUseCase @Inject constructor(private val parcelRepo: ParcelRepository)
{
    suspend operator fun invoke() = withContext(Dispatchers.IO) {
        SopoLog.i("RefreshParcelsUseCase(...)")

        parcelRepo.requestParcelsForRefresh()
    }
}