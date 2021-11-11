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
import com.delivery.sopo.networks.call.ParcelCall
import com.delivery.sopo.services.network_handler.BaseServiceBeta
import com.delivery.sopo.util.TimeUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ParcelRepository(private val userLocalRepo: UserLocalRepository,
                       private val oAuthRepo: OAuthLocalRepository,
                       private val appDatabase: AppDatabase):
        ParcelDataSource,
        BaseServiceBeta()
{

    private val userId: String by lazy { userLocalRepo.getUserId() }


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
        return@withContext appDatabase.parcelDao().getById(parcelId)?.let { ParcelMapper.entityToObject(it) }
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

    override suspend fun insertEntities(parcelResponseList: List<ParcelResponse>)
    {
        appDatabase.parcelDao().insert(parcelResponseList.map(ParcelMapper::parcelToParcelEntity))
    }

    override suspend fun insetEntity(parcel: ParcelEntity)
    {
        appDatabase.parcelDao().insert(parcel)
    }


    suspend fun saveLocalCompleteParcels(parcelResponseList: List<ParcelResponse>)
    {
        appDatabase.parcelDao().insert(parcelResponseList.map(ParcelMapper::parcelToParcelEntity))
    }


    override suspend fun updateEntity(parcel: ParcelEntity): Int
    {
        parcel.auditDte = TimeUtil.getDateTime()
        return appDatabase.parcelDao().update(parcel)
    }

    override suspend fun updateEntities(parcelResponseList: List<ParcelResponse>)
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