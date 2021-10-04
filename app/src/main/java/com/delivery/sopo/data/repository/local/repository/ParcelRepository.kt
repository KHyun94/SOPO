package com.delivery.sopo.data.repository.local.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.delivery.sopo.SOPOApp
import com.delivery.sopo.data.repository.database.room.AppDatabase
import com.delivery.sopo.data.repository.database.room.dto.CompletedParcelHistory
import com.delivery.sopo.data.repository.database.room.dto.DeleteParcelsDTO
import com.delivery.sopo.data.repository.database.room.entity.ParcelEntity
import com.delivery.sopo.models.mapper.ParcelMapper
import com.delivery.sopo.models.api.APIResult
import com.delivery.sopo.models.parcel.ParcelDTO
import com.delivery.sopo.networks.NetworkManager
import com.delivery.sopo.networks.api.ParcelAPI

import com.delivery.sopo.data.repository.local.datasource.ParcelDataSource
import com.delivery.sopo.data.repository.local.user.UserLocalRepository
import com.delivery.sopo.networks.call.ParcelCall
import com.delivery.sopo.util.TimeUtil

class ParcelRepository(private val userLocalRepository: UserLocalRepository,
                       private val appDatabase: AppDatabase): ParcelDataSource
{

    override suspend fun getRemoteOngoingParcels(): MutableList<ParcelDTO>? = NetworkManager.retro(SOPOApp.oAuth?.accessToken).create(
        ParcelAPI::class.java).getParcelsOngoing().data

    override suspend fun getRemoteMonths(): List<CompletedParcelHistory>
    {
       return try
       {
           ParcelCall.getCompleteParcelsMonth().data ?: emptyList<CompletedParcelHistory>()
       }catch(e:Exception){
           emptyList<CompletedParcelHistory>()
       }
    }
    override suspend fun getRemoteCompleteParcels(page: Int, inquiryDate: String): MutableList<ParcelDTO> {
        return ParcelCall.getCompleteParcelsByPage(page, inquiryDate).data?: emptyList<ParcelDTO>().toMutableList()
    }
    override suspend fun getLocalParcelById(parcelId: Int): ParcelEntity? {
        return appDatabase.parcelDao().getById(parcelId)
    }

    // 배송 중인 택배 리스트를 LiveData로 받기
    override fun getLocalOngoingParcelsAsLiveData(): LiveData<List<ParcelDTO>> {
        return Transformations.map(appDatabase.parcelDao().getOngoingLiveData()){ entityList ->
            entityList.map(ParcelMapper::parcelEntityToParcel)
        }
    }

    override fun getLocalCompleteParcelsLiveData(): LiveData<List<ParcelDTO>>{
            return Transformations.map(appDatabase.parcelDao().getCompleteLiveData()){
                    entity ->
                        entity.map(ParcelMapper::parcelEntityToParcel)
            }
    }

/*
    fun getCompleteParcelsByDateLiveData(date:String): LiveData<List<ParcelDTO>>{
        return Transformations.map(appDatabase.parcelDao().getCompleteParcelByDateAsLiveData(date)){ entity ->
            SopoLog.d("Test ---> ${entity.size} ${entity.joinToString()}")
            entity.filterNotNull().map(ParcelMapper::parcelEntityToParcel)
        }
    }
*/

    fun getCompleteParcelsByDate(date:String): List<ParcelDTO>{
        val entity = appDatabase.parcelDao().getCompleteParcelByDate(date)
        return entity.filterNotNull().map(ParcelMapper::parcelEntityToParcel)
    }


    override fun getLocalCompleteParcels(): List<ParcelDTO>
    {
        return appDatabase.parcelDao().getComplete().map(ParcelMapper::parcelEntityToParcel)
    }

    override suspend fun getLocalOngoingParcels(): List<ParcelDTO> {
        return appDatabase.parcelDao().getOngoingData().map(ParcelMapper::parcelEntityToParcel)
    }

    override fun getSoonDataCntLiveData(): LiveData<Int>
    {
        return appDatabase.parcelDao().getSoonDataCntLiveData()
    }

    override fun getOngoingDataCntLiveData(): LiveData<Int>
    {
        return  appDatabase.parcelDao().getOngoingDataCntLiveData()
    }

    override suspend fun isBeingUpdateParcel(parcelId:Int): LiveData<Int?> = appDatabase.parcelDao().isBeingUpdateParcel(parcelId = parcelId)

    override fun getIsUnidentifiedAsLiveData(parcelId: Int): LiveData<Int?>
    {
        return appDatabase.parcelDao().getIsUnidentifiedLiveData(parcelId)
    }

    override fun getLocalOnGoingParcelCnt(): LiveData<Int>
    {
        return appDatabase.parcelDao().getOngoingDataCntLiveData()
    }

    override suspend fun insertEntities(parcelDTOList: List<ParcelDTO>) {
        appDatabase.parcelDao().insert(parcelDTOList.map(ParcelMapper::parcelToParcelEntity))
    }

    override suspend fun insetEntity(parcel: ParcelEntity) {
        appDatabase.parcelDao().insert(parcel)
    }


    suspend fun saveLocalCompleteParcels(parcelDTOList: List<ParcelDTO>) {
        appDatabase.parcelDao().insert(parcelDTOList.map(ParcelMapper::parcelToParcelEntity))
    }


    override suspend fun updateEntity(parcel: ParcelEntity) : Int {
        parcel.auditDte = TimeUtil.getDateTime()
        return appDatabase.parcelDao().update(parcel)
    }

    override suspend fun updateEntities(parcelDTOList: List<ParcelDTO>) {
        appDatabase.parcelDao().update(parcelDTOList.map(ParcelMapper::parcelToParcelEntity))
    }

    override suspend fun deleteRemoteParcels(): APIResult<String?>? {
        val beDeletedData = appDatabase.parcelDao().getBeDeletedData()
        return if(beDeletedData.isNotEmpty()){
            NetworkManager.retro(SOPOApp.oAuth?.accessToken).create(ParcelAPI::class.java).deleteParcels(
                parcelIds = DeleteParcelsDTO(beDeletedData.map(ParcelMapper::parcelEntityToParcelId) as MutableList<Int>)
            )
        }
        else{
            null
        }
    }

    override suspend fun deleteLocalParcels(parcelIdList: List<Int>){
        for(parcelId in parcelIdList){
            appDatabase.parcelDao().getById(parcelId)?.let {
                it.status = 0
                it.auditDte = TimeUtil.getDateTime()

                appDatabase.parcelDao().update(it)
            }
        }
    }

    // 0922 kh 추가사항
    override suspend fun getSingleParcelWithWaybillNum(waybillNum : String): ParcelEntity? {
        return appDatabase.parcelDao().getSingleParcelWithwaybillNum(waybillNum = waybillNum)
    }

    override suspend fun getOnGoingDataCnt(): Int {
        return appDatabase.parcelDao().getOngoingDataCnt()
    }
}