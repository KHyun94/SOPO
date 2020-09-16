package com.delivery.sopo.repository.remote

import com.delivery.sopo.database.room.AppDatabase
import com.delivery.sopo.models.APIResult
import com.delivery.sopo.models.dto.DeleteParcelsDTO
import com.delivery.sopo.models.entity.ParcelEntity
import com.delivery.sopo.models.mapper.ParcelMapper
import com.delivery.sopo.models.parcel.Parcel
import com.delivery.sopo.models.parcel.ParcelId
import com.delivery.sopo.networks.NetworkManager
import com.delivery.sopo.repository.ParcelRepository
import com.delivery.sopo.repository.local.UserRepo
import java.util.stream.Collector
import java.util.stream.Collectors


class RemoteParcelRepoImpl(private val userRepo: UserRepo,
                            private val appDatabase: AppDatabase): ParcelRepository {
    private val TAG = "LOG.SOPO${this.javaClass.simpleName}"

    override suspend fun getRemoteParcels(): MutableList<Parcel>? = NetworkManager.getPrivateParcelAPI(userRepo.getEmail(), userRepo.getApiPwd()).getParcelsOngoing(email = userRepo.getEmail()).data

    override suspend fun getLocalParcels(): MutableList<Parcel>? = appDatabase.parcelDao().getAll().map(ParcelMapper::entityToObject) as MutableList<Parcel>

    override suspend fun saveLocalParcels(parcelList: List<Parcel>) {
        appDatabase.parcelDao().insert(parcelList.map(ParcelMapper::objectToEntity))
    }


    override suspend fun deleteRemoteParcels(): APIResult<String?> {
        val beDeletedData = appDatabase.parcelDao().getBeDeletedData()
        //TODO: beDeletedData가 0일때는?
        return NetworkManager.getPrivateParcelAPI(userRepo.getEmail(), userRepo.getApiPwd()).deleteParcels(email = userRepo.getEmail(),
            parcelIds = DeleteParcelsDTO(beDeletedData.map(ParcelMapper::entityToParcelId) as MutableList<ParcelId>)
        )
    }

    override suspend fun deleteLocalParcelsStep1(parcelIdList: List<ParcelId>) {
        for(parcelId in parcelIdList){
            val parcelEntity = appDatabase.parcelDao().getById(parcelId.regDt, parcelId.parcelUid)
            parcelEntity.apply {
                this.status = 3
            }
            appDatabase.parcelDao().insert(parcelEntity)
        }
    }

    override suspend fun deleteLocalParcelsStep2() {
        val beDeletedData = appDatabase.parcelDao().getBeDeletedData().stream().
        map{
            it.status  = 0
            it
        }.collect(Collectors.toList())
        appDatabase.parcelDao().update(beDeletedData)
    }
}