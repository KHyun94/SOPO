package com.delivery.sopo.data.repository.local.repository

import androidx.lifecycle.LiveData
import com.delivery.sopo.data.database.room.AppDatabase
import com.delivery.sopo.data.database.room.entity.ParcelStatusEntity
import com.delivery.sopo.data.repository.local.datasource.ParcelManagementRepository
import com.delivery.sopo.models.mapper.ParcelMapper
import com.delivery.sopo.models.parcel.ParcelStatus
import com.delivery.sopo.util.TimeUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ParcelManagementRepoImpl(private val appDatabase: AppDatabase): ParcelManagementRepository
{
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

    suspend fun getDeletableParcelStatuses():List<ParcelStatus> = withContext(Dispatchers.Default){
        return@withContext appDatabase.parcelManagementDao().getDeletableParcelStatuses().map(ParcelMapper::parcelStatusEntityToObject)
    }

/*    override fun insertEntity(parcelStatusEntity: ParcelStatusEntity){
        appDatabase.parcelManagementDao().insert(parcelStatusEntity)
    }*/

    override fun insertParcelStatus(parcelStatus: ParcelStatus){
        appDatabase.parcelManagementDao().insert(ParcelMapper.parcelStatusObjectToEntity(parcelStatus))
    }

    override fun insertParcelStatuses(parcelStatusList: List<ParcelStatus>){
        val entities = parcelStatusList.map{
            ParcelMapper.parcelStatusObjectToEntity(it)
        }
        appDatabase.parcelManagementDao().insert(entities)
    }

    suspend fun update(parcelStatus: ParcelStatus)= withContext(Dispatchers.Default){
        appDatabase.parcelManagementDao().update(ParcelMapper.parcelStatusObjectToEntity(parcelStatus))
    }

    override suspend fun update(parcelStatusEntity: ParcelStatusEntity)= withContext(Dispatchers.Default){
        parcelStatusEntity.auditDte = TimeUtil.getDateTime()
        appDatabase.parcelManagementDao().update(parcelStatusEntity)
    }

    override suspend fun updateParcelStatuses(parcelStatuses: List<ParcelStatus>){
        val entities = parcelStatuses.map(ParcelMapper::parcelStatusObjectToEntity)
        appDatabase.parcelManagementDao().update(entities)
    }

    override suspend fun updateUpdatableStatus(parcelId:Int, status : Int) = appDatabase.parcelManagementDao().updateIsBeUpdate(parcelId, status)

    override fun getParcelStatus(parcelId:Int): ParcelStatus {
        val entity = appDatabase.parcelManagementDao().getById(parcelId)?:ParcelMapper.parcelStatusObjectToEntity(ParcelStatus(parcelId = parcelId))
        return ParcelMapper.parcelStatusEntityToObject(entity)
    }

    override suspend fun updateTotalIsBeDeliveredToZero(){
        appDatabase.parcelManagementDao().updateTotalIsBeDeliveredToZero()
    }

    override suspend fun updateIsBeDeleteToOneByParcelIdList(parcelIdList: List<Int>){
        for (parcelId in parcelIdList){
            appDatabase.parcelManagementDao().updateIsBeDeleteToOne(parcelId, TimeUtil.getDateTime())
        }
    }

    override suspend fun updateUnidentifiedStatus(parcelId:Int, value: Int) = withContext(Dispatchers.Default){
        appDatabase.parcelManagementDao().updateIsUnidentified(parcelId, value)
    }

    override suspend fun getAll(): List<ParcelStatusEntity>?{
        return appDatabase.parcelManagementDao().getAll()
    }

    suspend fun delete(parcelId: Int) = withContext(Dispatchers.Default){
        val entity = ParcelMapper.parcelStatusObjectToEntity(getParcelStatus(parcelId))
        appDatabase.parcelManagementDao().delete(entity)
    }

    suspend fun delete(parcelIds: List<Int>) = withContext(Dispatchers.Default){
        val entities = parcelIds.map {
            ParcelMapper.parcelStatusObjectToEntity(getParcelStatus(it))
        }
        appDatabase.parcelManagementDao().delete(entities)
    }
}