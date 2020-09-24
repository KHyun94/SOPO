package com.delivery.sopo.repository

import com.delivery.sopo.database.dto.TimeCountDTO
import com.delivery.sopo.database.room.AppDatabase
import com.delivery.sopo.models.APIResult
import com.delivery.sopo.models.dto.DeleteParcelsDTO
import com.delivery.sopo.models.entity.ParcelEntity
import com.delivery.sopo.mapper.ParcelMapper
import com.delivery.sopo.models.parcel.Parcel
import com.delivery.sopo.models.parcel.ParcelId
import com.delivery.sopo.networks.NetworkManager
import com.delivery.sopo.repository.shared.UserRepo
import com.delivery.sopo.util.fun_util.TimeUtil
import java.lang.RuntimeException
import java.util.stream.Collectors


class ParcelRepoImpl(private val userRepo: UserRepo,
                     private val appDatabase: AppDatabase): ParcelRepository {
    private val TAG = "LOG.SOPO${this.javaClass.simpleName}"

    override suspend fun getRemoteOngoingParcels(): MutableList<Parcel>? = NetworkManager.getPrivateParcelAPI(userRepo.getEmail(), userRepo.getApiPwd()).getParcelsOngoing(email = userRepo.getEmail()).data

    override suspend fun getRemoteCompleteParcels(page: Int, inquiryDate: String): MutableList<Parcel>? = NetworkManager
                                                                                                        .getPrivateParcelAPI(userRepo.getEmail(), userRepo.getApiPwd())
                                                                                                        .getParcelsComplete(email = userRepo.getEmail(), page = page, inquiryDate = inquiryDate).data

    override suspend fun getLocalParcelById(regDt: String, parcelUid: String): ParcelEntity? {
        return appDatabase.parcelDao().getById(regDt,parcelUid)
    }

    override suspend fun getLocalBeDeleteCanceledParcel(): List<ParcelEntity>? {
        return appDatabase.parcelDao().getBeDeleteCanceledData()
    }

    override suspend fun getLocalOngoingParcels(): MutableList<Parcel>? = appDatabase.parcelDao().getOngoingData().map(ParcelMapper::entityToParcel) as MutableList<Parcel>

    override suspend fun getRemoteMonthList(): MutableList<TimeCountDTO>? = NetworkManager.getPrivateParcelAPI(userRepo.getEmail(), userRepo.getApiPwd()).getMonthList(email = userRepo.getEmail()).data

    override suspend fun saveLocalOngoingParcels(parcelList: List<Parcel>) {
        appDatabase.parcelDao().insert(parcelList.map(ParcelMapper::parcelToEntity))
    }

    override suspend fun saveLocalOngoingParcel(parcel: ParcelEntity) {
        appDatabase.parcelDao().insert(parcel)
    }

    override suspend fun updateLocalOngoingParcel(parcel: ParcelEntity) {
        appDatabase.parcelDao().update(parcel)
    }

    override suspend fun updateLocalOngoingParcels(parcelList: List<ParcelEntity>) {
        appDatabase.parcelDao().update(parcelList)
    }

    override suspend fun deleteRemoteOngoingParcels(): APIResult<String?> {
        val beDeletedData = appDatabase.parcelDao().getBeDeletedData()
        //TODO: beDeletedData가 0일때는?
        return NetworkManager.getPrivateParcelAPI(userRepo.getEmail(), userRepo.getApiPwd()).deleteParcels(email = userRepo.getEmail(),
            parcelIds = DeleteParcelsDTO(beDeletedData.map(ParcelMapper::entityToParcelId) as MutableList<ParcelId>)
        )
    }

    override suspend fun deleteLocalOngoingParcelsStep1(parcelIdList: List<ParcelId>) {
        for(parcelId in parcelIdList){
            val parcelEntity = appDatabase.parcelDao().getById(parcelId.regDt, parcelId.parcelUid) ?: throw RuntimeException("deleteLocalOngoingParcelsStep1 process cannot permit null object")
            parcelEntity.apply {
                this.status = 3
                this.auditDte = TimeUtil.getDateTime()
            }
            appDatabase.parcelDao().insert(parcelEntity)
        }
    }

    override suspend fun deleteLocalOngoingParcelsStep2() {
        val beDeletedData = appDatabase.parcelDao().getBeDeletedData().stream().
        map{
            it.status  = 0
            it
        }.collect(Collectors.toList())
        appDatabase.parcelDao().update(beDeletedData)
    }
}