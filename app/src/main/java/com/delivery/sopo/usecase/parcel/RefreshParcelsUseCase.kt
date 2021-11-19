package com.delivery.sopo.usecase.parcel

import com.delivery.sopo.data.repository.local.repository.ParcelRepository
import com.delivery.sopo.util.SopoLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RefreshParcelsUseCase(private val parcelRepo: ParcelRepository)
{
    operator fun invoke() = CoroutineScope(Dispatchers.IO).launch {
        SopoLog.i("RefreshParcelsUseCase(...)")

        parcelRepo.requestParcelsForRefresh()
    }
}