package com.delivery.sopo.domain.usecase.parcel.remote

import com.delivery.sopo.data.repositories.parcels.ParcelRepository
import com.delivery.sopo.firebase.FirebaseRepository
import com.delivery.sopo.models.parcel.Parcel
import com.delivery.sopo.util.SopoLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RegisterParcelUseCase(private val parcelRepository: ParcelRepository)
{
    suspend operator fun invoke(parcelRegister: Parcel.Register) = withContext(Dispatchers.IO) {
        SopoLog.i("호출")

        val parcel = parcelRepository.registerParcel(parcelRegister = parcelRegister)
        FirebaseRepository.subscribedTopic(isForce = true)

        return@withContext parcel
    }
}