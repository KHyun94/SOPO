package com.delivery.sopo.domain.usecase.parcel.remote

import com.delivery.sopo.data.repositories.local.repository.ParcelRepository
import com.delivery.sopo.firebase.FirebaseRepository
import com.delivery.sopo.models.parcel.Parcel
import com.delivery.sopo.util.SopoLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RegisterParcelUseCase(private val parcelRepo: ParcelRepository)
{
    suspend operator fun invoke(parcelRegister: Parcel.Register) = withContext(Dispatchers.IO) {
        SopoLog.i("RegisterParcelUseCase(...)")

        val parcelId = parcelRepo.registerParcel(parcelRegister)
        val parcel = parcelRepo.getRemoteParcelById(parcelId = parcelId)
        parcelRepo.insert(parcel)

        FirebaseRepository.subscribedTopic(isForce = true)

        return@withContext parcel
    }
}