package com.delivery.sopo.data.repositories.parcels

import com.delivery.sopo.DateSelector
import com.delivery.sopo.models.parcel.Parcel
import kotlinx.coroutines.flow.Flow

interface ParcelRepository
{
    suspend fun registerParcel(parcelRegister: Parcel.Register): Parcel.Common
    suspend fun updateParcel(parcelId: Int): Parcel.Common
    suspend fun getParcel(parcelId: Int): Parcel.Common?
    suspend fun fetchCompletedParcel(page: Int, inquiryDate: String): List<Parcel.Common>
    suspend fun fetchCompletedDateInfo(cursorDate: String? = null): DateSelector
}