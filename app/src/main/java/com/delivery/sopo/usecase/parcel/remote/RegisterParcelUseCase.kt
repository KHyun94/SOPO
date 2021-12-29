package com.delivery.sopo.usecase.parcel.remote

import com.delivery.sopo.data.repository.local.repository.ParcelRepository
import com.delivery.sopo.models.ParcelRegister
import com.delivery.sopo.util.SopoLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RegisterParcelUseCase(private val parcelRepo: ParcelRepository)
{
    suspend operator fun invoke(parcelRegister: ParcelRegister) = withContext(Dispatchers.IO) {
        SopoLog.i("RegisterParcelUseCase(...)")

        return@withContext parcelRepo.registerParcel(parcelRegister)
    }
}