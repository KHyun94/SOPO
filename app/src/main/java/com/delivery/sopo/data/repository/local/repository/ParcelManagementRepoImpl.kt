package com.delivery.sopo.data.repository.local.repository

import androidx.lifecycle.LiveData
import com.delivery.sopo.data.repository.database.room.AppDatabase
import com.delivery.sopo.data.repository.database.room.entity.ParcelStatusEntity
import com.delivery.sopo.data.repository.local.datasource.ParcelManagementRepository
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

    override fun insertEntity(parcelStatusEntity: ParcelStatusEntity){
        appDatabase.parcelManagementDao().insert(parcelStatusEntity)
    }

    override fun insertEntities(parcelStatusEntityList: List<ParcelStatusEntity>){
        parcelStatusEntityList.forEach { it.auditDte = TimeUtil.getDateTime() }
        appDatabase.parcelManagementDao().insert(parcelStatusEntityList)
    }

    override suspend fun update(parcelStatusEntity: ParcelStatusEntity)= withContext(Dispatchers.Default){
        parcelStatusEntity.auditDte = TimeUtil.getDateTime()
        appDatabase.parcelManagementDao().update(parcelStatusEntity)
    }

    override suspend fun updateEntities(parcelStatusEntityList: List<ParcelStatusEntity>){
        parcelStatusEntityList.forEach { it.auditDte = TimeUtil.getDateTime() }
        appDatabase.parcelManagementDao().update(parcelStatusEntityList)
    }

    override suspend fun updateUpdatableStatus(parcelId:Int, status : Int) = appDatabase.parcelManagementDao().updateIsBeUpdate(parcelId, status)

    override fun getEntity(parcelId:Int): ParcelStatusEntity? {
        return appDatabase.parcelManagementDao().getById(parcelId)
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

    override suspend fun initializeIsBeUpdate(parcelId:Int){
        getEntity(parcelId = parcelId)?.apply {
            this.updatableStatus = 0
            this.auditDte = TimeUtil.getDateTime()
            insertEntity(this)
        }
    }

    override suspend fun getAll(): List<ParcelStatusEntity>?{
        return appDatabase.parcelManagementDao().getAll()
    }

}