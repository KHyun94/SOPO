package com.delivery.sopo.data.repository.local.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.delivery.sopo.data.repository.database.room.AppDatabase
import com.delivery.sopo.data.repository.database.room.dto.CompletedParcelHistory
import com.delivery.sopo.data.repository.database.room.dto.DeleteParcelsDTO
import com.delivery.sopo.data.repository.database.room.entity.ParcelEntity
import com.delivery.sopo.models.mapper.ParcelMapper
import com.delivery.sopo.models.api.APIResult
import com.delivery.sopo.models.parcel.ParcelResponse
import com.delivery.sopo.networks.NetworkManager
import com.delivery.sopo.networks.api.ParcelAPI

import com.delivery.sopo.data.repository.local.datasource.ParcelDataSource
import com.delivery.sopo.data.repository.local.o_auth.OAuthLocalRepository
import com.delivery.sopo.data.repository.local.user.UserLocalRepository
import com.delivery.sopo.enums.NetworkEnum
import com.delivery.sopo.models.ParcelRegister
import com.delivery.sopo.models.parcel.ParcelStatus
import com.delivery.sopo.networks.call.ParcelCall
import com.delivery.sopo.services.network_handler.BaseServiceBeta
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.util.TimeUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ParcelRepository(private val userLocalRepo: UserLocalRepository,
                       private val oAuthRepo: OAuthLocalRepository,
                       private val parcelManagementRepo:ParcelManagementRepoImpl,
                       private val appDatabase: AppDatabase):
        ParcelDataSource, BaseServiceBeta()
{

    private val userId: String by lazy { userLocalRepo.getUserId() }

    suspend fun insertNewParcelFromServer(parcels:List<ParcelResponse>){

        val insertParcels = parcels.filter { getLocalParcelById(it.parcelId) == null }.apply {
            SopoLog.d("!!!!!!!!!!! insert1 size - ${this.size}")
        }
        val insertParcelStatuses = insertParcels.map{
            ParcelMapper.parcelToParcelStatus(it).apply {
                unidentifiedStatus = 1
                auditDte = TimeUtil.getDateTime()
            }
        }.apply {
            SopoLog.d("!!!!!!!!!!! insert2 size - ${this.size}")
        }

        insertParcels(insertParcels)
        parcelManagementRepo.insertParcelStatuses(insertParcelStatuses)
    }

    suspend fun updateParcelFromServer(parcels:List<ParcelResponse>){

        val updateParcels = parcels.filter { remote ->
            val local = getLocalParcelById(remote.parcelId)?:return@filter false

            val unidentifiedStatus = getIsUnidentifiedAsLiveData(remote.parcelId).value?:0
            if(unidentifiedStatus == 1)
            {
                SopoLog.d("!!!!!!!!!!! 상태 변경 !!!!!")
                parcelManagementRepo.updateUnidentifiedStatus(remote.parcelId, 0)
            }

            remote.inquiryHash != local.inquiryHash
        }.apply {
            SopoLog.d("!!!!!!!!!!! update1 size - ${this.size}")
        }

        val updateParcelStatuses = updateParcels.map{

            val parcelStatus = parcelManagementRepo.getParcelStatus(it.parcelId) ?: ParcelStatus(parcelId = it.parcelId)

            parcelStatus.apply {

                if(unidentifiedStatus == 1) this.unidentifiedStatus = 0 else unidentifiedStatus = 1
                auditDte = TimeUtil.getDateTime()
            }
        }.apply {
            SopoLog.d("!!!!!!!!!!! update2 size - ${this.size}")
        }

        updateLocalParcels(updateParcels)
        parcelManagementRepo.updateParcelStatuses(updateParcelStatuses)
    }

    suspend fun updateUnidentifiedStatus(parcels:List<ParcelResponse>){
        val parcelStatuses = parcels.mapNotNull { parcelManagementRepo.getParcelStatus(it.parcelId) }
            .filter { it.unidentifiedStatus == 1 }

        parcelStatuses.forEach { it.unidentifiedStatus = 0 }

        parcelManagementRepo.updateParcelStatuses(parcelStatuses)
    }

    suspend fun deleteParcelFromServer(parcels:List<ParcelResponse>){
        val insertList = parcels.filter { getLocalParcelById(it.parcelId) == null }
        insertParcels(insertList)
    }

    suspend fun registerParcel(parcel: ParcelRegister):Int
    {
        val oAuthToken = oAuthRepo.get(userId = userId)
        val registerParcel = NetworkManager.retro(oAuthToken.accessToken).create(ParcelAPI::class.java).registerParcel(register = parcel)
        val result = apiCall{ registerParcel }
        return result.data?.data?:throw NullPointerException()
    }

    suspend fun getRemoteParcelById(parcelId: Int):ParcelResponse
    {
        val oAuthToken = oAuthRepo.get(userId = userId)
        val getRemoteParcel = NetworkManager.retro(oAuthToken.accessToken).create(ParcelAPI::class.java).getParcel(parcelId = parcelId)
        val result = apiCall { getRemoteParcel }
        return result.data?.data?:throw NullPointerException()
    }

    override suspend fun getRemoteParcelByOngoing(): List<ParcelResponse>
    {
        val getRemoteParcelByOngoing = NetworkManager.setLoginMethod(NetworkEnum.O_AUTH_TOKEN_LOGIN, ParcelAPI::class.java).getParcelsOngoing()
        val result = apiCall { getRemoteParcelByOngoing }
        return result.data?.data?: emptyList<ParcelResponse>()
    }

    override suspend fun getRemoteMonths(): List<CompletedParcelHistory>
    {
        return try
        {
            ParcelCall.getCompleteParcelsMonth().data ?: emptyList<CompletedParcelHistory>()
        }
        catch(e: Exception)
        {
            emptyList<CompletedParcelHistory>()
        }
    }

    override suspend fun getRemoteCompleteParcels(page: Int, inquiryDate: String): MutableList<ParcelResponse>
    {
        return ParcelCall.getCompleteParcelsByPage(page, inquiryDate).data
            ?: emptyList<ParcelResponse>().toMutableList()
    }

    override suspend fun getLocalParcelById(parcelId: Int): ParcelResponse? = withContext(Dispatchers.Default){
        return@withContext appDatabase.parcelDao().getById(parcelId)?.let { ParcelMapper.parcelEntityToObject(it) }
    }

    // 배송 중인 택배 리스트를 LiveData로 받기
    override fun getLocalOngoingParcelsAsLiveData(): LiveData<List<ParcelResponse>>
    {
        return Transformations.map(appDatabase.parcelDao().getOngoingLiveData()) { entityList ->
            entityList.map(ParcelMapper::parcelEntityToParcel)
        }
    }

    override fun getLocalCompleteParcelsLiveData(): LiveData<List<ParcelResponse>>
    {
        return Transformations.map(appDatabase.parcelDao().getCompleteLiveData()) { entity ->
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

    fun getCompleteParcelsByDate(date: String): List<ParcelResponse>
    {
        val entity = appDatabase.parcelDao().getCompleteParcelByDate(date)
        return entity.filterNotNull().map(ParcelMapper::parcelEntityToParcel)
    }


    override fun getLocalCompleteParcels(): List<ParcelResponse>
    {
        return appDatabase.parcelDao().getComplete().map(ParcelMapper::parcelEntityToParcel)
    }

    override suspend fun getLocalOngoingParcels(): List<ParcelResponse>
    {
        return appDatabase.parcelDao().getOngoingData().map(ParcelMapper::parcelEntityToParcel)
    }

    override fun getSoonDataCntLiveData(): LiveData<Int>
    {
        return appDatabase.parcelDao().getSoonDataCntLiveData()
    }

    override fun getOngoingDataCntLiveData(): LiveData<Int>
    {
        return appDatabase.parcelDao().getOngoingDataCntLiveData()
    }

    override suspend fun isBeingUpdateParcel(parcelId: Int): LiveData<Int?> =
        appDatabase.parcelDao().isBeingUpdateParcel(parcelId = parcelId)

    override fun getIsUnidentifiedAsLiveData(parcelId: Int): LiveData<Int?>
    {
        return appDatabase.parcelDao().getIsUnidentifiedLiveData(parcelId)
    }

    override fun getLocalOnGoingParcelCnt(): LiveData<Int>
    {
        return appDatabase.parcelDao().getOngoingDataCntLiveData()
    }

    override suspend fun insertParcels(parcels: List<ParcelResponse>)
    {
        appDatabase.parcelDao().insert(parcels.map(ParcelMapper::parcelToParcelEntity))
    }

    override suspend fun insetEntity(parcel: ParcelEntity)
    {
        appDatabase.parcelDao().insert(parcel)
    }

    override suspend fun update(parcel: ParcelEntity): Int = withContext(Dispatchers.Default) {
        parcel.auditDte = TimeUtil.getDateTime()
        return@withContext appDatabase.parcelDao().update(parcel)
    }

    override suspend fun updateLocalParcels(parcelResponseList: List<ParcelResponse>)
    {
        appDatabase.parcelDao().update(parcelResponseList.map(ParcelMapper::parcelToParcelEntity))
    }

    override suspend fun deleteRemoteParcels(): APIResult<String?>?
    {
        val beDeletedData = appDatabase.parcelDao().getBeDeletedData()
        return if(beDeletedData.isNotEmpty())
        {

            val userId = userLocalRepo.getUserId()
            val oAuthToken = oAuthRepo.get(userId = userId)

            NetworkManager.retro(oAuthToken.accessToken)
                .create(ParcelAPI::class.java)
                .deleteParcels(parcelIds = DeleteParcelsDTO(beDeletedData.map(ParcelMapper::parcelEntityToParcelId) as MutableList<Int>))
        }
        else
        {
            null
        }
    }

    override suspend fun deleteLocalParcels(parcelIdList: List<Int>)
    {
        for(parcelId in parcelIdList)
        {
            appDatabase.parcelDao().getById(parcelId)?.let {
                it.status = 0
                it.auditDte = TimeUtil.getDateTime()

                appDatabase.parcelDao().update(it)
            }
        }
    }

    // 0922 kh 추가사항
    override suspend fun getSingleParcelWithWaybillNum(waybillNum: String): ParcelEntity?
    {
        return appDatabase.parcelDao().getSingleParcelWithwaybillNum(waybillNum = waybillNum)
    }

    override suspend fun getOnGoingDataCnt(): Int
    {
        return appDatabase.parcelDao().getOngoingDataCnt()
    }
}