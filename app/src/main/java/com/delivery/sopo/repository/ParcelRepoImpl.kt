package com.delivery.sopo.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
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
    override suspend fun getRemoteOngoingParcel(regDt: String, parcelUid: String): Parcel? = NetworkManager
                                                            .getPrivateParcelAPI(userRepo.getEmail(), userRepo.getApiPwd())
                                                            .getParcel( email = userRepo.getEmail(),
                                                                        regDt = regDt,
                                                                        parcelUid = parcelUid).data

    override suspend fun getRemoteMonthList(): MutableList<TimeCountDTO>? = NetworkManager.getPrivateParcelAPI(userRepo.getEmail(), userRepo.getApiPwd()).getMonthList(email = userRepo.getEmail()).data

    override suspend fun getRemoteCompleteParcels(page: Int, inquiryDate: String): MutableList<Parcel>? = NetworkManager
                                                                                                        .getPrivateParcelAPI(userRepo.getEmail(), userRepo.getApiPwd())
                                                                                                        .getParcelsComplete(email = userRepo.getEmail(), page = page, inquiryDate = inquiryDate).data

    override suspend fun getLocalParcelById(regDt: String, parcelUid: String): ParcelEntity? {
        return appDatabase.parcelDao().getById(regDt,parcelUid)
    }

    override fun getLocalOngoingParcelsLiveData(): LiveData<List<Parcel>> {
        return Transformations.map(appDatabase.parcelDao().getOngoingLiveData()){
            entity ->
             entity.map(ParcelMapper::parcelEntityToParcel)
        }
    }

    fun getLocalCompleteParcelsLiveData(): LiveData<List<Parcel>>{
            return Transformations.map(appDatabase.parcelDao().getCompleteLiveData()){
                    entity ->
                        entity.map(ParcelMapper::parcelEntityToParcel)
            }
    }

    override suspend fun getLocalOngoingParcels(): List<Parcel>? {
        return appDatabase.parcelDao().getOngoingData()?.map(ParcelMapper::parcelEntityToParcel)
    }

    override suspend fun saveLocalOngoingParcels(parcelList: List<Parcel>) {
        appDatabase.parcelDao().insert(parcelList.map(ParcelMapper::parcelToParcelEntity))
    }

    override suspend fun saveLocalOngoingParcel(parcel: ParcelEntity) {
        appDatabase.parcelDao().insert(parcel)
    }

    suspend fun saveLocalCompleteParcels(parcelList: List<Parcel>) {
        appDatabase.parcelDao().insert(parcelList.map(ParcelMapper::parcelToParcelEntity))
    }


    override suspend fun updateLocalOngoingParcel(parcel: ParcelEntity) {
        appDatabase.parcelDao().update(parcel)
    }

    override suspend fun updateLocalOngoingParcels(parcelList: List<ParcelEntity>) {
        appDatabase.parcelDao().update(parcelList)
    }

    override suspend fun deleteRemoteParcels(): APIResult<String?>? {
        val beDeletedData = appDatabase.parcelDao().getBeDeletedData()
        return if(beDeletedData.isNotEmpty()){
            NetworkManager.getPrivateParcelAPI(userRepo.getEmail(), userRepo.getApiPwd()).deleteParcels(email = userRepo.getEmail(),
                parcelIds = DeleteParcelsDTO(beDeletedData.map(ParcelMapper::parcelEntityToParcelId) as MutableList<ParcelId>))
        }
        else{
            null
        }
    }

    override suspend fun deleteLocalOngoingParcels(parcelIdList: List<ParcelId>){
        for(parcelId in parcelIdList){
            appDatabase.parcelDao().getById(parcelId.regDt, parcelId.parcelUid)?.let {
                it.status = 0
                it.auditDte = TimeUtil.getDateTime()

                appDatabase.parcelDao().update(it)
            }
        }
    }

    // 0922 kh 추가사항
    override suspend fun getSingleParcelWithWaybilNum(waybilNum : String): ParcelEntity? {
        return appDatabase.parcelDao().getSingleParcelWithWaybilNum(waybilNum = waybilNum)
    }

    override suspend fun getOnGoingDataCnt(): Int {
        return appDatabase.parcelDao().getOngoingDataCnt()
    }

}