package com.delivery.sopo.data.repositories.parcels

import com.delivery.sopo.models.parcel.Parcel

interface ParcelRepository
{
    suspend fun registerParcel(parcelRegister: Parcel.Register): Parcel.Common
}