package com.delivery.sopo.data.repositories.local.repository

import androidx.lifecycle.LiveData
import com.delivery.sopo.consts.StatusConst
import com.delivery.sopo.data.database.room.AppDatabase
import com.delivery.sopo.data.database.room.entity.ParcelStatusEntity
import com.delivery.sopo.data.repositories.local.datasource.ParcelManagementRepository
import com.delivery.sopo.interfaces.BaseDataSource
import com.delivery.sopo.models.mapper.ParcelMapper
import com.delivery.sopo.models.parcel.Parcel
import com.delivery.sopo.util.TimeUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ParcelManagementRepoImpl @Inject constructor(private val appDatabase: AppDatabase): ParcelManagementRepository, BaseDataSource<Parcel.Status>
{
    override fun get(): List<Parcel.Status>
    {
        return appDatabase.parcelStatusDAO().get().map(ParcelMapper::parcelStatusEntityToObject)
    }

    override fun insert(vararg data: Parcel.Status)
    {
        val entities = data.map(ParcelMapper::parcelStatusObjectToEntity)
        appDatabase.parcelStatusDAO().insert(entities)
    }

    override fun update(vararg data: Parcel.Status)
    {
        val entities = data.map(ParcelMapper::parcelStatusObjectToEntity)
        appDatabase.parcelStatusDAO().update(entities)
    }

    override fun delete(vararg data: Parcel.Status)
    {
        val entities = data.map(ParcelMapper::parcelStatusObjectToEntity)
        appDatabase.parcelStatusDAO().delete(entities)
    }

    override fun getParcelStatusById(parcelId:Int): Parcel.Status
    {
        val entity = appDatabase.parcelStatusDAO().getById(parcelId)?:ParcelMapper.parcelStatusObjectToEntity(Parcel.Status(parcelId = parcelId))
        return ParcelMapper.parcelStatusEntityToObject(entity)
    }

    fun getUpdatableParcelIds(): List<Int>{
        return appDatabase.parcelStatusDAO().getUpdatableParcelIds()
    }

    fun getUpdatableParcelIdsAsLiveData(): LiveData<List<Int>>{
        return appDatabase.parcelStatusDAO().getUpdatableParcelIdsAsLiveData()
    }

    override fun getIsDeleteCntLiveData(): LiveData<Int> {
        return appDatabase.parcelStatusDAO().getIsDeleteCntLiveData()
    }

    override fun getIsUpdateCntLiveData(): LiveData<Int>
    {
        return appDatabase.parcelStatusDAO().getIsUpdateCntLiveData()
    }

    override fun getIsDeliveredCntLiveData(): LiveData<Int> {
        return appDatabase.parcelStatusDAO().getIsDeliveredCntLiveData()
    }

    override suspend fun getCountForUpdatableParcel(): Int {
        return appDatabase.parcelStatusDAO().getCountForUpdatableParcel()
    }

    override suspend fun getIsDeleteCnt(): Int {
        return appDatabase.parcelStatusDAO().getIsDeleteCnt()
    }

    override suspend fun getIsDeliveredCnt(): Int {
        return appDatabase.parcelStatusDAO().getIsDeliveredCnt()
    }

    override suspend fun getCancelIsBeDelete(): List<ParcelStatusEntity>? {
        return appDatabase.parcelStatusDAO().getCancelIsBeDelete()
    }

    // 업데이트 미확인 체크용도
    override suspend fun getUnidentifiedStatusByParcelId(parcelId:Int): Int = withContext(Dispatchers.Default) {
        appDatabase.parcelStatusDAO().getUnidentifiedStatus(parcelId = parcelId)
    }

    suspend fun getDeletableParcelStatuses():List<Parcel.Status> = withContext(Dispatchers.Default){
        return@withContext appDatabase.parcelStatusDAO().getDeletableParcelStatuses().map(ParcelMapper::parcelStatusEntityToObject)
    }


    override fun insertParcelStatus(parcelStatus: Parcel.Status){
        appDatabase.parcelStatusDAO().insert(ParcelMapper.parcelStatusObjectToEntity(parcelStatus))
    }

    override fun insertParcelStatuses(parcelStatuses: List<Parcel.Status>){
        val entities = parcelStatuses.map{
            ParcelMapper.parcelStatusObjectToEntity(it)
        }
        appDatabase.parcelStatusDAO().insert(entities)
    }

    suspend fun update(parcelStatus: Parcel.Status)= withContext(Dispatchers.Default){
        appDatabase.parcelStatusDAO().update(ParcelMapper.parcelStatusObjectToEntity(parcelStatus))
    }

    override suspend fun update(parcelStatusEntity: ParcelStatusEntity)= withContext(Dispatchers.Default){
        parcelStatusEntity.auditDte = TimeUtil.getDateTime()
        appDatabase.parcelStatusDAO().update(parcelStatusEntity)
    }

    override suspend fun updateParcelStatuses(parcelStatus: List<Parcel.Status>){
        val entities = parcelStatus.map(ParcelMapper::parcelStatusObjectToEntity)
        appDatabase.parcelStatusDAO().update(entities)
    }

    override suspend fun updateUpdatableStatus(parcelId:Int, status : Int) = appDatabase.parcelStatusDAO().updateIsBeUpdate(parcelId, status)

    override suspend fun updateTotalIsBeDeliveredToZero(){
        appDatabase.parcelStatusDAO().updateTotalIsBeDeliveredToZero()
    }

    override suspend fun updateIsBeDeleteToOneByParcelIdList(parcelIdList: List<Int>){
        for (parcelId in parcelIdList){
            appDatabase.parcelStatusDAO().updateIsBeDeleteToOne(parcelId, TimeUtil.getDateTime())
        }
    }

    override suspend fun updateUnidentifiedStatusById(parcelId:Int, value: Int) = withContext(Dispatchers.Default){
        appDatabase.parcelStatusDAO().updateIsUnidentified(parcelId, value)
    }

    suspend fun delete(parcelId: Int) = withContext(Dispatchers.Default){
        val entity = ParcelMapper.parcelStatusObjectToEntity(getParcelStatusById(parcelId))
        appDatabase.parcelStatusDAO().delete(entity)
    }

    suspend fun delete(parcelIds: List<Int>) = withContext(Dispatchers.Default){
        val entities = parcelIds.map {
            ParcelMapper.parcelStatusObjectToEntity(getParcelStatusById(it))
        }
        appDatabase.parcelStatusDAO().delete(entities)
    }

    override suspend fun updateUnidentifiedStatus(parcels: List<Parcel.Common>)
    {
        val parcelStatuses = parcels.map { getParcelStatusById(it.parcelId) }
            .filter { it.unidentifiedStatus == 1 }
        parcelStatuses.forEach { it.unidentifiedStatus = 0 }
        updateParcelStatuses(parcelStatuses)
    }

    override fun makeParcelStatus(parcel: Parcel.Common): Parcel.Status{
        return getParcelStatusById(parcel.parcelId).apply {
            unidentifiedStatus = if(!parcel.reported) StatusConst.ACTIVATE else StatusConst.DEACTIVATE
            updatableStatus = 0
            auditDte = TimeUtil.getDateTime()
        }
    }
}