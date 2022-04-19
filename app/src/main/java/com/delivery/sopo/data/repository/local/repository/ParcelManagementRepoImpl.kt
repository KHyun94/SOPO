package com.delivery.sopo.data.repository.local.repository

import androidx.lifecycle.LiveData
import com.delivery.sopo.data.database.room.AppDatabase
import com.delivery.sopo.data.database.room.entity.ParcelStatusEntity
import com.delivery.sopo.data.repository.local.datasource.ParcelManagementRepository
import com.delivery.sopo.interfaces.BaseDataSource
import com.delivery.sopo.models.mapper.ParcelMapper
import com.delivery.sopo.models.parcel.Parcel
import com.delivery.sopo.util.TimeUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ParcelManagementRepoImpl(private val appDatabase: AppDatabase): ParcelManagementRepository, BaseDataSource<Parcel.Status>
{
    override fun get(): List<Parcel.Status>
    {
        return appDatabase.parcelManagementDao().get().map(ParcelMapper::parcelStatusEntityToObject)
    }

    override fun insert(vararg data: Parcel.Status)
    {
        val entities = data.map(ParcelMapper::parcelStatusObjectToEntity)
        appDatabase.parcelManagementDao().insert(entities)
    }

    override fun update(vararg data: Parcel.Status)
    {
        val entities = data.map(ParcelMapper::parcelStatusObjectToEntity)
        appDatabase.parcelManagementDao().update(entities)
    }

    override fun delete(vararg data: Parcel.Status)
    {
        val entities = data.map(ParcelMapper::parcelStatusObjectToEntity)
        appDatabase.parcelManagementDao().delete(entities)
    }

    override fun getParcelStatusById(parcelId:Int): Parcel.Status
    {
        val entity = appDatabase.parcelManagementDao().getById(parcelId)?:ParcelMapper.parcelStatusObjectToEntity(Parcel.Status(parcelId = parcelId))
        return ParcelMapper.parcelStatusEntityToObject(entity)
    }

    fun getUpdatableParcelIds(): List<Int>{
        return appDatabase.parcelManagementDao().getUpdatableParcelIds()
    }

    fun getUpdatableParcelIdsAsLiveData(): LiveData<List<Int>>{
        return appDatabase.parcelManagementDao().getUpdatableParcelIdsAsLiveData()
    }

    override fun getIsDeleteCntLiveData(): LiveData<Int> {
        return appDatabase.parcelManagementDao().getIsDeleteCntLiveData()
    }

    override fun getIsUpdateCntLiveData(): LiveData<Int>
    {
        return appDatabase.parcelManagementDao().getIsUpdateCntLiveData()
    }

    override fun getIsDeliveredCntLiveData(): LiveData<Int> {
        return appDatabase.parcelManagementDao().getIsDeliveredCntLiveData()
    }

    override suspend fun getCountForUpdatableParcel(): Int {
        return appDatabase.parcelManagementDao().getCountForUpdatableParcel()
    }

    override suspend fun getIsDeleteCnt(): Int {
        return appDatabase.parcelManagementDao().getIsDeleteCnt()
    }

    override suspend fun getIsDeliveredCnt(): Int {
        return appDatabase.parcelManagementDao().getIsDeliveredCnt()
    }

    override suspend fun getCancelIsBeDelete(): List<ParcelStatusEntity>? {
        return appDatabase.parcelManagementDao().getCancelIsBeDelete()
    }

    // 업데이트 미확인 체크용도
    override suspend fun getUnidentifiedStatusByParcelId(parcelId:Int): Int = withContext(Dispatchers.Default) {
        appDatabase.parcelManagementDao().getUnidentifiedStatusByParcelId(parcelId = parcelId)
    }

    suspend fun getDeletableParcelStatuses():List<Parcel.Status> = withContext(Dispatchers.Default){
        return@withContext appDatabase.parcelManagementDao().getDeletableParcelStatuses().map(ParcelMapper::parcelStatusEntityToObject)
    }


    override fun insertParcelStatus(parcelStatus: Parcel.Status){
        appDatabase.parcelManagementDao().insert(ParcelMapper.parcelStatusObjectToEntity(parcelStatus))
    }

    override fun insertParcelStatuses(parcelStatuses: List<Parcel.Status>){
        val entities = parcelStatuses.map{
            ParcelMapper.parcelStatusObjectToEntity(it)
        }
        appDatabase.parcelManagementDao().insert(entities)
    }

    suspend fun update(parcelStatus: Parcel.Status)= withContext(Dispatchers.Default){
        appDatabase.parcelManagementDao().update(ParcelMapper.parcelStatusObjectToEntity(parcelStatus))
    }

    override suspend fun update(parcelStatusEntity: ParcelStatusEntity)= withContext(Dispatchers.Default){
        parcelStatusEntity.auditDte = TimeUtil.getDateTime()
        appDatabase.parcelManagementDao().update(parcelStatusEntity)
    }

    override suspend fun updateParcelStatuses(parcelStatus: List<Parcel.Status>){
        val entities = parcelStatus.map(ParcelMapper::parcelStatusObjectToEntity)
        appDatabase.parcelManagementDao().update(entities)
    }

    override suspend fun updateUpdatableStatus(parcelId:Int, status : Int) = appDatabase.parcelManagementDao().updateIsBeUpdate(parcelId, status)

    override suspend fun updateTotalIsBeDeliveredToZero(){
        appDatabase.parcelManagementDao().updateTotalIsBeDeliveredToZero()
    }

    override suspend fun updateIsBeDeleteToOneByParcelIdList(parcelIdList: List<Int>){
        for (parcelId in parcelIdList){
            appDatabase.parcelManagementDao().updateIsBeDeleteToOne(parcelId, TimeUtil.getDateTime())
        }
    }

    override suspend fun updateUnidentifiedStatusById(parcelId:Int, value: Int) = withContext(Dispatchers.Default){
        appDatabase.parcelManagementDao().updateIsUnidentified(parcelId, value)
    }

    suspend fun delete(parcelId: Int) = withContext(Dispatchers.Default){
        val entity = ParcelMapper.parcelStatusObjectToEntity(getParcelStatusById(parcelId))
        appDatabase.parcelManagementDao().delete(entity)
    }

    suspend fun delete(parcelIds: List<Int>) = withContext(Dispatchers.Default){
        val entities = parcelIds.map {
            ParcelMapper.parcelStatusObjectToEntity(getParcelStatusById(it))
        }
        appDatabase.parcelManagementDao().delete(entities)
    }

    suspend fun updateUnidentifiedStatus(parcels: List<Parcel.Common>)
    {
        val parcelStatuses = parcels.map { getParcelStatusById(it.parcelId) }
            .filter { it.unidentifiedStatus == 1 }
        parcelStatuses.forEach { it.unidentifiedStatus = 0 }
        updateParcelStatuses(parcelStatuses)
    }
}