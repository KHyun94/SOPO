package com.delivery.sopo.data.repository.local.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.delivery.sopo.consts.StatusConst.ACTIVATE
import com.delivery.sopo.consts.StatusConst.DEACTIVATE
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
import com.delivery.sopo.interfaces.BaseDataSource
import com.delivery.sopo.services.network_handler.BaseService
import com.delivery.sopo.services.network_handler.NetworkResponse
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.util.TimeUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ParcelRepository(private val parcelManagementRepo: ParcelManagementRepoImpl, private val appDatabase: AppDatabase):
        BaseDataSource<Parcel.Common>, ParcelDataSource,
        BaseService()
{
    override fun get(): List<Parcel.Common>
    {
        return appDatabase.parcelDao().get().map(ParcelMapper::parcelEntityToObject)
    }

    override fun insert(vararg data: Parcel.Common)
    {
        val entities = data.map(ParcelMapper::parcelObjectToEntity)
        appDatabase.parcelDao().insert(entities)
    }

    override fun update(vararg data: Parcel.Common)
    {
        val entities = data.map(ParcelMapper::parcelObjectToEntity)
        appDatabase.parcelDao().update(entities)
    }

    override fun delete(vararg data: Parcel.Common)
    {
        val entities = data.map(ParcelMapper::parcelObjectToEntity)
        appDatabase.parcelDao().delete(entities)
    }

    fun hasLocalParcel(parcel: Parcel.Common): Boolean{
        return appDatabase.parcelDao().getById(parcel.parcelId) != null
    }

    fun compareInquiryHash(parcel: Parcel.Common): Boolean{
        val local = appDatabase.parcelDao().getById(parcel.parcelId)?.let { ParcelMapper.parcelEntityToObject(it) }?:return false
        return parcel.inquiryHash != local.inquiryHash
    }

    fun makeParcelStatus(parcel: Parcel.Common): Parcel.Status{
        return parcelManagementRepo.getParcelStatus(parcel.parcelId).apply {
            unidentifiedStatus = if(!parcel.reported) ACTIVATE else DEACTIVATE
            auditDte = TimeUtil.getDateTime()
        }
    }

    suspend fun insertParcelsFromServer(parcels: List<Parcel.Common>)
    {
        SopoLog.d("호출")

        val insertParcels = parcels.filterNot(::hasLocalParcel)
        SopoLog.d("신규 택배 리스트\n${insertParcels.joinToString("번 | ") { it.parcelId.toString() }}")
        val insertParcelStatuses = insertParcels.map(::makeParcelStatus)
        insertParcels(insertParcels)
        parcelManagementRepo.insertParcelStatuses(insertParcelStatuses)
    }

    suspend fun updateParcelsFromServer(parcels: List<Parcel.Common>)
    {
        SopoLog.d("호출")
        val updateParcels = parcels.filter(::compareInquiryHash)
        SopoLog.d("업데이트 택배 리스트\n${updateParcels.joinToString("번 | ") { it.parcelId.toString() }}")
        val updateParcelStatuses = updateParcels.map(::makeParcelStatus)
        update(*updateParcels.toTypedArray())
        parcelManagementRepo.updateParcelStatuses(updateParcelStatuses)
    }

    suspend fun updateUnidentifiedStatus(parcels: List<Parcel.Common>)
    {
        val parcelStatuses = parcels.map { parcelManagementRepo.getParcelStatus(it.parcelId) }
            .filter { it.unidentifiedStatus == 1 }
        parcelStatuses.forEach { it.unidentifiedStatus = 0 }
        parcelManagementRepo.updateParcelStatuses(parcelStatuses)
    }

    suspend fun deleteParcelFromServer(parcels: List<Parcel.Common>)
    {
        val insertList = parcels.filter { getLocalParcelById(it.parcelId) == null }
        insertParcels(insertList)
    }


    override suspend fun getLocalParcelById(parcelId: Int): Parcel.Common? = withContext(Dispatchers.Default) {
            return@withContext appDatabase.parcelDao()
                .getById(parcelId)
                ?.let { ParcelMapper.parcelEntityToObject(it) }
    }

    suspend fun getLocalParcelByIdAsLiveData(parcelId: Int): LiveData<Parcel.Common> =
        withContext(Dispatchers.Default) {
            return@withContext Transformations.map(appDatabase.parcelDao()
                                                       .getByIdAsLiveData(parcelId)) {
                ParcelMapper.parcelEntityToObject(it)
            }
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

    fun getUnidentifiedStatus(parcelId: Int) = appDatabase.parcelDao().getUnidentifiedStatus(parcelId = parcelId)

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

    // 0922 kh 추가사항
    override suspend fun getSingleParcelWithWaybillNum(waybillNum: String): ParcelEntity?
    {
        return appDatabase.parcelDao().getSingleParcelWithwaybillNum(waybillNum = waybillNum)
    }

    override suspend fun getOnGoingDataCnt(): Int = withContext(Dispatchers.Default) {
        return@withContext appDatabase.parcelDao().getOngoingDataCnt()
    }

    suspend fun registerParcel(parcel: Parcel.Register): Int
    {
        val registerParcel =
            NetworkManager.setLoginMethod(NetworkEnum.O_AUTH_TOKEN_LOGIN, ParcelAPI::class.java)
                .registerParcel(register = parcel)
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

    suspend fun getRemoteParcelById(parcelIds: List<Int>): List<Parcel.Common>
    {
        val getRemoteParcel =
            NetworkManager.setLoginMethod(NetworkEnum.O_AUTH_TOKEN_LOGIN, ParcelAPI::class.java)
                .getParcels(parcelId = parcelIds.joinToString(", "))
        val result = apiCall { getRemoteParcel }
        return result.data?.data ?: throw NullPointerException()
    }

    override suspend fun getOngoingParcelsFromRemote(): List<Parcel.Common>
    {
        val getOngoingParcelsFromRemote =
            NetworkManager.setLoginMethod(NetworkEnum.O_AUTH_TOKEN_LOGIN, ParcelAPI::class.java)
                .getOngoingParcels()
        val result = apiCall { getOngoingParcelsFromRemote }
        return result.data?.data ?: emptyList()
    }

    override suspend fun getRemoteMonths(): List<CompletedParcelHistory>
    {
        val getRemoteMonths =
            NetworkManager.setLoginMethod(NetworkEnum.O_AUTH_TOKEN_LOGIN, ParcelAPI::class.java)
                .getCompletedMonths()
        val result = apiCall { getRemoteMonths }
        return result.data?.data ?: emptyList()
    }

    override suspend fun getCompleteParcelsByRemote(page: Int, inquiryDate: String): List<Parcel.Common>
    {
        val getCompleteParcelsByRemote =
            NetworkManager.setLoginMethod(NetworkEnum.O_AUTH_TOKEN_LOGIN, ParcelAPI::class.java)
                .getCompletedParcelsByPaging(page = page, inquiryDate = inquiryDate)
        val result = apiCall { getCompleteParcelsByRemote }
        return result.data?.data ?: emptyList()
    }

    suspend fun reportParcelStatus(parcelIds: List<Int>)
    {
        val wrapParcelIds = parcelIds.wrapBodyAliasToHashMap<List<Int>>("parcelIds")
        val reportParcelStatus =
            NetworkManager.setLoginMethod(NetworkEnum.O_AUTH_TOKEN_LOGIN, ParcelAPI::class.java)
                .reportParcelStatus(wrapParcelIds)
        apiCall { reportParcelStatus }
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
                }
            }

            update(*updateParcelsByDelete.toTypedArray())
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