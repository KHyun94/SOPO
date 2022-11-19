package com.delivery.sopo.data.repositories.parcels

import com.delivery.sopo.DateSelector
import com.delivery.sopo.data.database.room.entity.CarrierEntity
import com.delivery.sopo.data.repositories.local.repository.CarrierDataSource
import com.delivery.sopo.data.resources.parcel.local.ParcelDataSource
import com.delivery.sopo.data.resources.parcel.local.ParcelStatusDataSource
import com.delivery.sopo.data.resources.parcel.remote.ParcelRemoteDataSource
import com.delivery.sopo.models.parcel.Parcel

class ParcelRepositoryImpl(private val carrierDataSource: CarrierDataSource, private val parcelDataSource: ParcelDataSource, private val parcelStatusDataSource: ParcelStatusDataSource,private val parcelRemoteDataSource: ParcelRemoteDataSource): ParcelRepository
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

    override suspend fun fetchCompletedParcel(page: Int, inquiryDate: String): List<Parcel.Common>
    {
        val parcels = parcelRemoteDataSource.fetchCompletedParcels(page = page, inquiryDate = inquiryDate)

        insertParcels(parcels = parcels)
        updateParcels(parcels = parcels)

        return parcels
    }

    private fun insertParcels(parcels: List<Parcel.Common>)
    {
        val insertParcels = parcels.filterNot(parcelDataSource::hasLocalParcel)
        val insertParcelStatuses = insertParcels.map(parcelStatusDataSource::makeParcelStatus)
        parcelDataSource.insert(*insertParcels.toTypedArray())
        parcelStatusDataSource.insert(*insertParcelStatuses.toTypedArray())
    }

    suspend fun updateParcels(parcels: List<Parcel.Common>)
    {
        val notExistParcelIds = parcelDataSource.getNotExistParcels(parcels = parcels).map { it.parcelId }
        val notExistParcels = parcelRemoteDataSource.fetchParcelById(parcelIds = notExistParcelIds)

        val updateParcels = parcels.filter(parcelDataSource::compareInquiryHash) + notExistParcels
        val updateParcelStatuses = updateParcels.map(parcelStatusDataSource::makeParcelStatus)
        parcelDataSource.update(*updateParcels.toTypedArray())
        parcelStatusDataSource.updateParcelStatuses(updateParcelStatuses)
    }

    override suspend fun fetchCompletedDateInfo(cursorDate: String?): DateSelector {
        return parcelRemoteDataSource.fetchCompletedDateInfo(cursorDate)
    }

    override suspend fun updateCarrierInfo(){
        val data = parcelRemoteDataSource.fetchCarrierInfo().map {
            CarrierEntity(name = it.name, code = it.carrier, available = it.isAvailable)
        }

        carrierDataSource.insert(*data.toTypedArray())
    }
}