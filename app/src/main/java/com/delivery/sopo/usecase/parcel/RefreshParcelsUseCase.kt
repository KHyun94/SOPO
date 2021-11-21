package com.delivery.sopo.usecase.parcel

import com.delivery.sopo.data.repository.local.repository.ParcelRepository
import com.delivery.sopo.util.SopoLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RefreshParcelsUseCase(private val parcelRepo: ParcelRepository)
{
    suspend operator fun invoke() = withContext(Dispatchers.IO) {
        SopoLog.i("RefreshParcelsUseCase(...)")

        parcelRepo.requestParcelsForRefresh()
    }
}