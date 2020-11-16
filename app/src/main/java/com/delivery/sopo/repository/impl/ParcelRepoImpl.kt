package com.delivery.sopo.repository.impl

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.delivery.sopo.networks.dto.TimeCountDTO
import com.delivery.sopo.database.room.AppDatabase
import com.delivery.sopo.models.api.APIResult
import com.delivery.sopo.database.room.dto.DeleteParcelsDTO
import com.delivery.sopo.database.room.entity.ParcelEntity
import com.delivery.sopo.mapper.ParcelMapper
import com.delivery.sopo.models.parcel.Parcel
import com.delivery.sopo.models.parcel.ParcelId
import com.delivery.sopo.networks.NetworkManager
import com.delivery.sopo.networks.api.ParcelAPI
import com.delivery.sopo.repository.interfaces.ParcelRepository
import com.delivery.sopo.util.TimeUtil

class ParcelRepoImpl(private val userRepoImpl: UserRepoImpl,
                     private val appDatabase: AppDatabase):
    ParcelRepository
{
    private val TAG = "LOG.SOPO${this.javaClass.simpleName}"

    override suspend fun getRemoteOngoingParcels(): MutableList<Parcel>? = NetworkManager.privateRetro.create(
        ParcelAPI::class.java).getParcelsOngoing(email = userRepoImpl.getEmail()).data

    override suspend fun getRemoteOngoingParcel(regDt: String, parcelUid: String): Parcel? = NetworkManager
                                                            .privateRetro.create(ParcelAPI::class.java)
                                                            .getParcel( email = userRepoImpl.getEmail(),
                                                                        regDt = regDt,
                                                                        parcelUid = parcelUid).data

    override suspend fun getRemoteMonths(): MutableList<TimeCountDTO>? = NetworkManager.privateRetro.create(ParcelAPI::class.java).getMonths(email = userRepoImpl.getEmail()).data

    override suspend fun getRemoteCompleteParcels(page: Int, inquiryDate: String): MutableList<Parcel>? = NetworkManager
                                                                                                        .privateRetro.create(ParcelAPI::class.java)
                                                                                                        .getParcelsComplete(email = userRepoImpl.getEmail(), page = page, inquiryDate = inquiryDate).data

    override suspend fun getLocalParcelById(regDt: String, parcelUid: String): ParcelEntity? {
        return appDatabase.parcelDao().getById(regDt,parcelUid)
    }

    override fun getLocalOngoingParcelsLiveData(): LiveData<List<Parcel>> {
        return Transformations.map(appDatabase.parcelDao().getOngoingLiveData()){
            entity ->
             entity.map(ParcelMapper::parcelEntityToParcel)
        }
    }

    override fun getLocalCompleteParcelsLiveData(): LiveData<List<Parcel>>{
            return Transformations.map(appDatabase.parcelDao().getCompleteLiveData()){
                    entity ->
                        entity.map(ParcelMapper::parcelEntityToParcel)
            }
    }

    override fun getLocalCompleteParcels(): List<Parcel>
    {
        return appDatabase.parcelDao().getComplete().map(ParcelMapper::parcelEntityToParcel)
    }

    override suspend fun getLocalOngoingParcels(): List<Parcel> {
        return appDatabase.parcelDao().getOngoingData().map(ParcelMapper::parcelEntityToParcel)
    }

//    override suspend fun getUpdatableInquiryHash(): List<ParcelEntity?> = appDatabase.parcelDao().getUpdatableInquiryHash()

    override fun getSoonDataCntLiveData(): LiveData<Int>
    {
        return appDatabase.parcelDao().getSoonDataCntLiveData()
    }

    override fun getOngoingDataCntLiveData(): LiveData<Int>
    {
        return  appDatabase.parcelDao().getOngoingDataCntLiveData()
    }

    override suspend fun isBeingUpdateParcel(regDt: String, parcelUid: String): LiveData<Int?> = appDatabase.parcelDao().isBeingUpdateParcel(regDt, parcelUid)

    override suspend fun insertEntities(parcelList: List<Parcel>) {
        appDatabase.parcelDao().insert(parcelList.map(ParcelMapper::parcelToParcelEntity))
    }

    override suspend fun insetEntity(parcel: ParcelEntity) {
        appDatabase.parcelDao().insert(parcel)
    }


    suspend fun saveLocalCompleteParcels(parcelList: List<Parcel>) {
        appDatabase.parcelDao().insert(parcelList.map(ParcelMapper::parcelToParcelEntity))
    }


    override suspend fun updateEntity(parcel: ParcelEntity) : Int {
        parcel.auditDte = TimeUtil.getDateTime()
        return appDatabase.parcelDao().update(parcel)
    }

    override suspend fun updateEntities(parcelList: List<Parcel>) {
        appDatabase.parcelDao().update(parcelList.map(ParcelMapper::parcelToParcelEntity))
    }

    override suspend fun deleteRemoteParcels(): APIResult<String?>? {
        val beDeletedData = appDatabase.parcelDao().getBeDeletedData()
        return if(beDeletedData.isNotEmpty()){
            NetworkManager.privateRetro.create(ParcelAPI::class.java).deleteParcels(email = userRepoImpl.getEmail(),
                parcelIds = DeleteParcelsDTO(beDeletedData.map(ParcelMapper::parcelEntityToParcelId) as MutableList<ParcelId>)
            )
        }
        else{
            null
        }
    }

    override suspend fun deleteLocalParcels(parcelIdList: List<ParcelId>){
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