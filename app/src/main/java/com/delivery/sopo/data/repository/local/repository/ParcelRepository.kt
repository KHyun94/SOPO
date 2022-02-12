package com.delivery.sopo.data.repository.local.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.delivery.sopo.data.database.room.AppDatabase
import com.delivery.sopo.data.database.room.dto.CompletedParcelHistory
import com.delivery.sopo.data.database.room.entity.ParcelEntity
import com.delivery.sopo.models.mapper.ParcelMapper
import com.delivery.sopo.models.api.APIResult
import com.delivery.sopo.models.parcel.Parcel
import com.delivery.sopo.networks.NetworkManager
import com.delivery.sopo.networks.api.ParcelAPI

import com.delivery.sopo.data.repository.local.datasource.ParcelDataSource
import com.delivery.sopo.enums.NetworkEnum
import com.delivery.sopo.extensions.wrapBodyAliasToHashMap
import com.delivery.sopo.extensions.wrapBodyAliasToMap
import com.delivery.sopo.models.ParcelRegister
import com.delivery.sopo.services.network_handler.BaseServiceBeta
import com.delivery.sopo.services.network_handler.NetworkResponse
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.util.TimeUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ParcelRepository(
        private val parcelManagementRepo: ParcelManagementRepoImpl,
        private val appDatabase: AppDatabase):
        ParcelDataSource,
        BaseServiceBeta()
{
    suspend fun insertParcelsFromServer(parcels: List<Parcel.Common>)
    {

        val insertParcels = parcels.filter { getLocalParcelById(it.parcelId) == null }
        val insertParcelStatuses = insertParcels.map {

            val status = parcelManagementRepo.getParcelStatus(it.parcelId)

            status.apply {
                unidentifiedStatus = 1
                updatableStatus = 0
                auditDte = TimeUtil.getDateTime()
            }
        }

        insertParcels(insertParcels)
        parcelManagementRepo.insertParcelStatuses(insertParcelStatuses)
    }

    suspend fun updateParcelFromServer(parcel: Parcel.Common)
    {
        val local = getLocalParcelById(parcel.parcelId) ?: return

        val unidentifiedStatus = getUnidentifiedStatus(parcelId = parcel.parcelId)

        if(unidentifiedStatus == 1)
        {
            parcelManagementRepo.updateUnidentifiedStatus(parcelId = parcel.parcelId, 0)
        }

        if(parcel.inquiryHash == local.inquiryHash) return

        val parcelStatus = parcelManagementRepo.getParcelStatus(parcel.parcelId).apply {
            auditDte = TimeUtil.getDateTime()
        }

        update(parcel = ParcelMapper.parcelObjectToEntity(parcel))
        parcelManagementRepo.update(parcelStatus)
    }

    suspend fun updateParcelsFromServer(parcels: List<Parcel.Common>)
    {

        val updateParcels = parcels.filter { remote ->
            val local = getLocalParcelById(remote.parcelId) ?: return@filter false

            val unidentifiedStatus = getIsUnidentifiedAsLiveData(remote.parcelId).value ?: 0
            if(unidentifiedStatus == 1)
            {
                parcelManagementRepo.updateUnidentifiedStatus(remote.parcelId, 0)
            }

            remote.inquiryHash != local.inquiryHash
        }

        val updateParcelStatuses = updateParcels.map {

            val parcelStatus = parcelManagementRepo.getParcelStatus(it.parcelId)

            parcelStatus.apply {

                if(unidentifiedStatus == 1) this.unidentifiedStatus = 0 else unidentifiedStatus = 1
                updatableStatus = 0
                auditDte = TimeUtil.getDateTime()
            }
        }

        updateLocalParcels(updateParcels)
        parcelManagementRepo.updateParcelStatuses(updateParcelStatuses)
    }

    suspend fun updateUnidentifiedStatus(parcels: List<Parcel.Common>)
    {
        val parcelStatuses =
            parcels.mapNotNull { parcelManagementRepo.getParcelStatus(it.parcelId) }
                .filter { it.unidentifiedStatus == 1 }

        parcelStatuses.forEach { it.unidentifiedStatus = 0 }

        parcelManagementRepo.updateParcelStatuses(parcelStatuses)
    }

    suspend fun deleteParcelFromServer(parcels: List<Parcel.Common>)
    {
        val insertList = parcels.filter { getLocalParcelById(it.parcelId) == null }
        insertParcels(insertList)
    }


    override suspend fun getLocalParcelById(parcelId: Int): Parcel.Common? =
        withContext(Dispatchers.Default) {
            return@withContext appDatabase.parcelDao()
                .getById(parcelId)
                ?.let { ParcelMapper.parcelEntityToObject(it) }
        }

    // 배송 중인 택배 리스트를 LiveData로 받기
    override fun getLocalOngoingParcelsAsLiveData(): LiveData<List<Parcel.Common>>
    {
        return Transformations.map(appDatabase.parcelDao().getOngoingLiveData()) { entityList ->
            entityList.map(ParcelMapper::parcelEntityToParcel)
        }
    }

    override fun getLocalCompleteParcelsLiveData(): LiveData<List<Parcel.Common>>
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

    fun getCompleteParcelsByDate(date: String): List<Parcel.Common>
    {
        val entity = appDatabase.parcelDao().getCompleteParcelByDate(date)
        return entity.filterNotNull().map(ParcelMapper::parcelEntityToParcel)
    }


    override fun getLocalCompleteParcels(): List<Parcel.Common>
    {
        return appDatabase.parcelDao().getComplete().map(ParcelMapper::parcelEntityToParcel)
    }

    override suspend fun getLocalOngoingParcels(): List<Parcel.Common>
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

    fun getUnidentifiedStatus(parcelId: Int) =
        appDatabase.parcelDao().getUnidentifiedStatus(parcelId = parcelId)

    override fun getIsUnidentifiedAsLiveData(parcelId: Int): LiveData<Int?>
    {
        return appDatabase.parcelDao().getIsUnidentifiedLiveData(parcelId)
    }

    override fun getLocalOnGoingParcelCnt(): LiveData<Int>
    {
        return appDatabase.parcelDao().getOngoingDataCntLiveData()
    }

    override suspend fun insertParcels(parcels: List<Parcel.Common>)
    {
        appDatabase.parcelDao().insert(parcels.map(ParcelMapper::parcelToParcelEntity))
    }

    override suspend fun insetEntity(parcel: ParcelEntity)
    {
        appDatabase.parcelDao().insert(parcel)
    }

    suspend fun update(parcel: Parcel.Common): Int = withContext(Dispatchers.Default) {

        SopoLog.d("TEST::이전 데이터=>${parcel.toString()}")

        parcel.auditDte = TimeUtil.getDateTime()
        val entity = ParcelMapper.parcelObjectToEntity(req = parcel)

        SopoLog.d("TEST::이후 데이터=>${entity.toString()}")
        return@withContext appDatabase.parcelDao().update(entity)
    }

    override suspend fun update(parcel: ParcelEntity): Int = withContext(Dispatchers.Default) {
        parcel.auditDte = TimeUtil.getDateTime()
        return@withContext appDatabase.parcelDao().update(parcel)
    }

    override suspend fun updateLocalParcels(parcelResponseList: List<Parcel.Common>)
    {
        appDatabase.parcelDao().update(parcelResponseList.map(ParcelMapper::parcelToParcelEntity))
    }

    // 0922 kh 추가사항
    override suspend fun getSingleParcelWithWaybillNum(waybillNum: String): ParcelEntity?
    {
        return appDatabase.parcelDao().getSingleParcelWithwaybillNum(waybillNum = waybillNum)
    }

    override suspend fun getOnGoingDataCnt(): Int = withContext(Dispatchers.Default) {
        return@withContext appDatabase.parcelDao().getOngoingDataCnt()
    }

    suspend fun registerParcel(parcel: ParcelRegister): Int
    {
        val registerParcel = NetworkManager.setLoginMethod(NetworkEnum.O_AUTH_TOKEN_LOGIN, ParcelAPI::class.java).registerParcel(register = parcel)
        val result = apiCall { registerParcel }
        return result.data?.data ?: throw NullPointerException()
    }

    suspend fun getRemoteParcelById(parcelId: Int): Parcel.Common
    {
        val getRemoteParcel =
            NetworkManager.setLoginMethod(NetworkEnum.O_AUTH_TOKEN_LOGIN, ParcelAPI::class.java)
                .getParcel(parcelId = parcelId)
        val result = apiCall { getRemoteParcel }
        return result.data?.data ?: throw NullPointerException()
    }

    override suspend fun getRemoteParcelByOngoing(): List<Parcel.Common>
    {
        val getRemoteParcelByOngoing =
            NetworkManager.setLoginMethod(NetworkEnum.O_AUTH_TOKEN_LOGIN, ParcelAPI::class.java)
                .getParcelsOngoing()
        val result = apiCall { getRemoteParcelByOngoing }
        return result.data?.data ?: emptyList()
    }

    override suspend fun getRemoteMonths(): List<CompletedParcelHistory>
    {
        val getRemoteMonths = NetworkManager.setLoginMethod(NetworkEnum.O_AUTH_TOKEN_LOGIN, ParcelAPI::class.java).getCompletedMonths()
        val result = apiCall { getRemoteMonths }
        return result.data?.data ?: emptyList()
    }

    override suspend fun getCompleteParcelsByRemote(page: Int, inquiryDate: String): List<Parcel.Common>
    {
        val getCompleteParcelsByRemote =
            NetworkManager.setLoginMethod(NetworkEnum.O_AUTH_TOKEN_LOGIN, ParcelAPI::class.java)
                .getParcelsComplete(page = page, inquiryDate = inquiryDate)
        val result = apiCall { getCompleteParcelsByRemote }
        return result.data?.data ?: emptyList()
    }

    /**
     * 택배 Alias 업데이트
     */
    suspend fun updateParcelAlias(parcelId: Int, parcelAlias: String)
    {
        val wrapParcelAlias = mapOf<String, String>(Pair("alias", parcelAlias))

        val updateParcelAlias =
            NetworkManager.setLoginMethod(NetworkEnum.O_AUTH_TOKEN_LOGIN, ParcelAPI::class.java)
                .updateParcelAlias(parcelId, wrapParcelAlias)
        apiCall { updateParcelAlias }
    }

    /**
     * 택배 삭제 관련
     */

    suspend fun getDeletableParcelIds(): List<Int> = withContext(Dispatchers.Default) {
        return@withContext appDatabase.parcelDao()
            .getBeDeletedData()
            .map { parcelEntity -> parcelEntity.parcelId } ?: emptyList()
    }

    fun deleteLocalParcels(parcelIds: List<Int>)
    {
        val parcels = parcelIds.mapNotNull { parcelId ->
            appDatabase.parcelDao().getById(parcelId = parcelId)
        }
        val parcelStatuses = parcelIds.mapNotNull { parcelId ->
            appDatabase.parcelManagementDao().getById(parcelId = parcelId)
        }
        appDatabase.parcelManagementDao().delete(parcelStatuses)
        appDatabase.parcelDao().delete(parcels)

    }

    suspend fun deleteRemoteParcels(parcelIds: List<Int>)
    {
        val wrapBody = parcelIds.wrapBodyAliasToHashMap("parcelIds")
        val deleteParcels =
            NetworkManager.setLoginMethod(NetworkEnum.O_AUTH_TOKEN_LOGIN, ParcelAPI::class.java)
                .deleteParcels(parcelIds = wrapBody)
        apiCall { deleteParcels }
    }

    override suspend fun updateParcelsToDeletable(parcelIds: List<Int>) =
        withContext(Dispatchers.Default) {
            val updateParcelsByDelete = parcelIds.mapNotNull { parcelId ->
                getLocalParcelById(parcelId)?.apply {
                    status = 0
                    auditDte = TimeUtil.getDateTime()
                }
            }

            updateLocalParcels(updateParcelsByDelete)
        }

    /**
     * 택배 업데이트 관련
     * 'Tracking Server'로 업데이트 요청
     */
    suspend fun requestParcelsForRefresh(): NetworkResponse<APIResult<String>>
    {
        val result =
            NetworkManager.setLoginMethod(NetworkEnum.O_AUTH_TOKEN_LOGIN, ParcelAPI::class.java)
                .requestParcelsForRefresh()
        return apiCall { result }
    }

    suspend fun requestParcelForRefresh(parcelId: Int): Parcel.Updatable
    {
        val wrapBody = parcelId.wrapBodyAliasToMap("parcelId")
        val result =
            NetworkManager.setLoginMethod(NetworkEnum.O_AUTH_TOKEN_LOGIN, ParcelAPI::class.java)
                .requestParcelForRefresh(parcelId = wrapBody)
        return apiCall { result }.data?.data ?: throw NullPointerException("택배 데이터가 조회되지 않습니다.")
    }
}