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

        parcelDataSource.getParcelById(parcelId)

        return parcel
    }

    override suspend fun getParcel(parcelId: Int): Parcel.Common?
    {
        return parcelDataSource.getParcelById(parcelId)
    }

    override suspend fun updateParcel(parcelId: Int): Parcel.Common
    {
        val parcelUpdatable = parcelRemoteDataSource.requestParcelUpdate(parcelId = parcelId)
        val parcel = parcelUpdatable.parcel

        val parcelStatus = parcelStatusDataSource.getById(parcelId) ?: parcelStatusDataSource.makeParcelStatus(parcel = parcel)

        parcelStatus.apply {
            unidentifiedStatus = 0
            updatableStatus = 0
        }

        if(!parcelUpdatable.updated) return parcel

        parcelDataSource.update(parcel)

        return parcel
    }
}