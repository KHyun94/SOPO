package com.delivery.sopo.data.repositories.local.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.delivery.sopo.data.database.room.AppDatabase
import com.delivery.sopo.data.database.room.dto.DeliveredParcelHistory
import com.delivery.sopo.data.database.room.entity.ParcelEntity
import com.delivery.sopo.data.networks.serivces.ParcelService
import com.delivery.sopo.data.repositories.local.datasource.ParcelDataSource
import com.delivery.sopo.extensions.wrapBodyAliasToHashMap
import com.delivery.sopo.extensions.wrapBodyAliasToMap
import com.delivery.sopo.interfaces.BaseDataSource
import com.delivery.sopo.models.mapper.ParcelMapper
import com.delivery.sopo.models.parcel.Parcel
import com.delivery.sopo.presentation.services.network_handler.BaseService
import com.delivery.sopo.presentation.services.network_handler.NetworkResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import com.delivery.sopo.data.models.Result
import com.delivery.sopo.enums.DeliveryStatus
import com.delivery.sopo.models.inquiry.InquiryListItem
import com.delivery.sopo.presentation.viewmodels.inquiry.OngoingTypeViewModel
import kotlinx.coroutines.flow.catch
import java.util.*
import javax.inject.Inject

class ParcelRepository @Inject constructor(private val appDatabase: AppDatabase, private val parcelService: ParcelService): BaseDataSource<Parcel.Common>,
        ParcelDataSource,
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

    fun getParcelById(parcelId: Int): Parcel.Common?
    {
        return appDatabase.parcelDao()
            .getById(parcelId = parcelId)
            ?.run(ParcelMapper::parcelEntityToObject)
    }

    fun hasLocalParcel(parcel: Parcel.Common): Boolean
    {
        return appDatabase.parcelDao().getById(parcel.parcelId) != null
    }

    fun getNotExistParcels(parcels: List<Parcel.Common>): List<Parcel.Common>
    {
        val parcelIds = parcels.map { it.parcelId }
        return appDatabase.parcelDao().getNotExistParcels(parcelIds = parcelIds).map(ParcelMapper::parcelEntityToObject)
    }

    fun compareInquiryHash(parcel: Parcel.Common): Boolean
    {
        val local = appDatabase.parcelDao().getById(parcel.parcelId)?.let { ParcelMapper.parcelEntityToObject(it) } ?: return false
        return parcel.inquiryHash != local.inquiryHash
    }

    // 배송 중인 택배 리스트를 LiveData로 받기
    override fun getOngoingParcelAsLiveData(): LiveData<List<Parcel.Common>>
    {
        return Transformations.map(appDatabase.parcelDao().getOngoingLiveData()) { entityList ->
            entityList.map(ParcelMapper::parcelEntityToParcel)
        }
    }

    fun getOngoingParcels() = flow {

        emit(Result.Loading)
        appDatabase.parcelDao().getFlowOngoingParcel().collect {
            if(it.isEmpty())
            {
                emit(Result.Empty)
            }
            else
            {
                val parcels = it.map{
                    val parcel = ParcelMapper.parcelEntityToObject(it)
                    InquiryListItem(parcel, false)
                }

                emit(Result.Success(sortByDeliveryStatus(parcels)))
            }
        }
    }.catch { e -> emit(Result.Error(e)) }

    fun getCompletedParcels() = flow {

        emit(Result.Loading)
        appDatabase.parcelDao().getFlowCompletedParcels().collect {
            if(it.isEmpty())
            {
                emit(Result.Empty)
            }
            else
            {
                val parcels = it.map{
                    val parcel = ParcelMapper.parcelEntityToObject(it)
                    InquiryListItem(parcel, false)
                }

                emit(Result.Success(sortByDeliveryStatus(parcels)))
            }
        }
    }.catch { e -> emit(Result.Error(e)) }

    override fun getCompleteParcelsAsLiveData(): LiveData<List<Parcel.Common>>
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
        val registerParcel = parcelService.registerParcel(parcelRegister = parcel)
        val result = apiCall { registerParcel }
        return result.data ?: throw NullPointerException()
    }

    suspend fun getRemoteParcelById(parcelId: Int): Parcel.Common
    {
        val getRemoteParcel =
            parcelService.fetchParcelById(parcelId = parcelId)
        val result = apiCall { getRemoteParcel }
        return result.data ?: throw NullPointerException()
    }

    suspend fun getRemoteParcelById(parcelIds: List<Int>): List<Parcel.Common>
    {
        if(parcelIds.isEmpty()) return emptyList()
        val getRemoteParcel = parcelService.fetchParcelById(parcelId = parcelIds.joinToString(", "))
        val result = apiCall { getRemoteParcel }
        return result.data ?: emptyList()
    }

    override suspend fun getOngoingParcelsFromRemote(): List<Parcel.Common>
    {
        val getOngoingParcelsFromRemote =
            parcelService
                .fetchOngoingParcels()
        val result = apiCall { getOngoingParcelsFromRemote }
        return result.data ?: emptyList()
    }

    override suspend fun getRemoteMonths(): List<DeliveredParcelHistory>
    {
        val getRemoteMonths =
            parcelService
                .fetchDeliveredMonth()
        val result = apiCall { getRemoteMonths }
        return result.data ?: emptyList()
    }

    override suspend fun getCompleteParcelsByRemote(page: Int, inquiryDate: String): List<Parcel.Common>
    {
        val getCompleteParcelsByRemote =
            parcelService
                .fetchDeliveredParcelsByPaging(page = page, inquiryDate = inquiryDate)
        val result = apiCall { getCompleteParcelsByRemote }
        return result.data ?: emptyList()
    }

    suspend fun reportParcelStatus(parcelIds: List<Int>)
    {
        val wrapParcelIds = parcelIds.wrapBodyAliasToHashMap<List<Int>>("parcelIds")
        val reportParcelStatus = parcelService.reportParcelStatus(wrapParcelIds)
        apiCall { reportParcelStatus }
    }

    /**
     * 택배 Alias 업데이트
     */
    suspend fun updateParcelAlias(parcelId: Int, parcelAlias: String)
    {
        val wrapParcelAlias = mapOf<String, String>(Pair("alias", parcelAlias))

        val updateParcelAlias =
            parcelService
                .updateParcelAlias(parcelId, wrapParcelAlias)
        apiCall { updateParcelAlias }
    }

    /**
     * 택배 삭제 관련
     */
/*

    suspend fun getDeletableParcelIds(): List<Int> = withContext(Dispatchers.Default) {
        return@withContext appDatabase.parcelDao()
            .getBeDeletedData()
            .map { parcelEntity -> parcelEntity.parcelId } ?: emptyList()
    }

    override suspend fun updateParcelsToDeletable(parcelIds: List<Int>) =
        withContext(Dispatchers.Default) {
            val updateParcelsByDelete = parcelIds.mapNotNull { parcelId ->
                getParcelById(parcelId)?.apply {
                    status = 0
                }
            }

            update(*updateParcelsByDelete.toTypedArray())
        }
*/

    suspend fun deleteRemoteParcels(parcelIds: List<Int>)
    {
        val wrapBody = parcelIds.wrapBodyAliasToHashMap("parcelIds")
        val deleteParcels =
            parcelService
                .deleteParcels(parcelIds = wrapBody)
        apiCall { deleteParcels }
    }

    /**
     * 택배 업데이트 관련
     * 'Tracking Server'로 업데이트 요청
     */
    suspend fun requestParcelsForRefresh(): NetworkResponse<String>
    {
        val result =
            parcelService
                .requestParcelsRefresh()
        return apiCall { result }
    }

    suspend fun requestParcelForRefresh(parcelId: Int): Parcel.Updatable
    {
        val wrapBody = parcelId.wrapBodyAliasToMap("parcelId")
        val result =
            parcelService
                .requestParcelsUpdate(parcelId = wrapBody)
        return apiCall { result }.data ?: throw NullPointerException("택배 데이터가 조회되지 않습니다.")
    }

    fun sortByDeliveryStatus(list: List<InquiryListItem>): List<InquiryListItem>
    {
        val sortedList = mutableListOf<InquiryListItem>()
        val multiList = listOf<MutableList<InquiryListItem>>(mutableListOf(), mutableListOf(), mutableListOf(), mutableListOf(), mutableListOf(), mutableListOf(), mutableListOf(), mutableListOf())

        val elseList = list.asSequence().filter { item ->

            if(item.parcel.deliveryStatus == DeliveryStatus.DELIVERED.CODE)
            {
                multiList[0].add(item)
            }

            item.parcel.deliveryStatus != DeliveryStatus.DELIVERED.CODE
        }.filter { item ->
            if(item.parcel.deliveryStatus == DeliveryStatus.OUT_FOR_DELIVERY.CODE)
            {
                multiList[1].add(item)
            }

            item.parcel.deliveryStatus != DeliveryStatus.OUT_FOR_DELIVERY.CODE
        }.filter { item ->
            if(item.parcel.deliveryStatus == DeliveryStatus.IN_TRANSIT.CODE)
            {
                multiList[2].add(item)
            }

            item.parcel.deliveryStatus != DeliveryStatus.IN_TRANSIT.CODE
        }.filter { item ->
            if(item.parcel.deliveryStatus == DeliveryStatus.AT_PICKUP.CODE)
            {
                multiList[3].add(item)
            }

            item.parcel.deliveryStatus != DeliveryStatus.AT_PICKUP.CODE
        }.filter { item ->
            if(item.parcel.deliveryStatus == DeliveryStatus.INFORMATION_RECEIVED.CODE)
            {
                multiList[4].add(item)
            }

            item.parcel.deliveryStatus != DeliveryStatus.INFORMATION_RECEIVED.CODE
        }.filter { item ->
            if(item.parcel.deliveryStatus == DeliveryStatus.NOT_REGISTERED.CODE)
            {
                multiList[5].add(item)
            }

            item.parcel.deliveryStatus != DeliveryStatus.NOT_REGISTERED.CODE
        }.filter { item ->
            if(item.parcel.deliveryStatus == DeliveryStatus.ORPHANED.CODE)
            {
                multiList[6].add(item)
            }

            item.parcel.deliveryStatus != DeliveryStatus.ORPHANED.CODE
        }.toList()

        multiList[7].addAll(elseList)

        multiList.forEach {
            Collections.sort(it, OngoingTypeViewModel.SortByDate())
            sortedList.addAll(it)
        }
        return sortedList
    }
}