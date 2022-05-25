package com.delivery.sopo.data.repositories.parcels

import com.delivery.sopo.data.resources.parcel.local.ParcelDataSource
import com.delivery.sopo.data.resources.parcel.local.ParcelStatusDataSource
import com.delivery.sopo.data.resources.parcel.remote.ParcelRemoteDataSource
import com.delivery.sopo.models.parcel.Parcel

class ParcelRepositoryImpl(private val parcelDataSource: ParcelDataSource, private val parcelStatusDataSource: ParcelStatusDataSource,private val parcelRemoteDataSource: ParcelRemoteDataSource): ParcelRepository
{
    override suspend fun registerParcel(parcelRegister: Parcel.Register): Parcel.Common
    {
        val parcelId: Int = parcelRemoteDataSource.registerParcel(parcelRegister = parcelRegister)

        val parcel: Parcel.Common = parcelRemoteDataSource.fetchParcelById(parcelId = parcelId)
        val parcelStatus: Parcel.Status = parcelStatusDataSource.makeParcelStatus(parcel = parcel)

        parcelDataSource.insert(parcel)
        parcelStatusDataSource.insert(parcelStatus)

        return parcel
    }
}