package com.delivery.sopo.data.repository.local.repository

import androidx.lifecycle.LiveData
import com.delivery.sopo.data.repository.database.room.AppDatabase
import com.delivery.sopo.data.repository.database.room.entity.ParcelStatusEntity
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

/*    override fun insertEntity(parcelStatusEntity: ParcelStatusEntity){
        appDatabase.parcelManagementDao().insert(parcelStatusEntity)
    }*/

    override fun insertParcelStatus(parcelStatus: ParcelStatus){
        appDatabase.parcelManagementDao().insert(ParcelMapper.parcelStatusObjectToEntity(parcelStatus))
    }

    override fun insertParcelStatuses(parcelStatusList: List<ParcelStatus>){
        val entities = parcelStatusList.map{
            it.auditDte =TimeUtil.getDateTime()
            ParcelMapper.parcelStatusObjectToEntity(it)
        }
        appDatabase.parcelManagementDao().insert(entities)
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

    override fun getParcelStatus(parcelId:Int): ParcelStatus? {
        val entity = appDatabase.parcelManagementDao().getById(parcelId)?:return null
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

}