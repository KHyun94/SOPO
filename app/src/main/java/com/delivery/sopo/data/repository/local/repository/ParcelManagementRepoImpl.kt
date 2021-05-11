package com.delivery.sopo.data.repository.local.repository

import androidx.lifecycle.LiveData
import com.delivery.sopo.data.repository.database.room.AppDatabase
import com.delivery.sopo.data.repository.database.room.entity.ParcelStatusEntity
import com.delivery.sopo.models.parcel.ParcelId
import com.delivery.sopo.data.repository.local.datasource.ParcelManagementRepository
import com.delivery.sopo.util.TimeUtil

class ParcelManagementRepoImpl(private val appDatabase: AppDatabase): ParcelManagementRepository
{
    private val TAG = "LOG.SOPO${this.javaClass.simpleName}"
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
    override suspend fun getUnidentifiedStatusByParcelId(parcelId: ParcelId) = appDatabase.parcelManagementDao().getUnidentifiedStatusByParcelId(parcelId.regDt, parcelId.parcelUid)

    override fun insertEntity(parcelStatusEntity: ParcelStatusEntity){
        appDatabase.parcelManagementDao().insert(parcelStatusEntity)
    }

    override fun insertEntities(parcelStatusEntityList: List<ParcelStatusEntity>){
        parcelStatusEntityList.forEach { it.auditDte = TimeUtil.getDateTime() }
        appDatabase.parcelManagementDao().insert(parcelStatusEntityList)
    }

    override suspend fun updateEntity(parcelStatusEntity: ParcelStatusEntity){
        parcelStatusEntity.auditDte = TimeUtil.getDateTime()
        appDatabase.parcelManagementDao().update(parcelStatusEntity)
    }

    override suspend fun updateEntities(parcelStatusEntityList: List<ParcelStatusEntity>){
        parcelStatusEntityList.forEach { it.auditDte = TimeUtil.getDateTime() }
        appDatabase.parcelManagementDao().update(parcelStatusEntityList)
    }

    override suspend fun updateUpdatableStatus(regDt: String, parcelUid: String, status : Int?) = appDatabase.parcelManagementDao().updateIsBeUpdate(regDt, parcelUid, status)

    override fun getEntity(parcelId: ParcelId): ParcelStatusEntity? {
        return appDatabase.parcelManagementDao().getById(parcelId.regDt, parcelId.parcelUid)
    }

    override suspend fun updateTotalIsBeDeliveredToZero(){
        appDatabase.parcelManagementDao().updateTotalIsBeDeliveredToZero()
    }

    override suspend fun updateIsBeDeleteToOneByParcelIdList(parcelIdList: List<ParcelId>){
        for (parcelId in parcelIdList){
            appDatabase.parcelManagementDao().updateIsBeDeleteToOne(parcelId.regDt, parcelId.parcelUid, TimeUtil.getDateTime())
        }
    }

    override fun updateIsUnidentified(parcelId: ParcelId, value: Int) = appDatabase.parcelManagementDao().updateIsUnidentified(parcelId.regDt, parcelId.parcelUid, value)

    override suspend fun initializeIsBeUpdate(regDt: String, parcelUid: String){
        getEntity(ParcelId(regDt, parcelUid))?.apply {
            this.updatableStatus = 0
            this.auditDte = TimeUtil.getDateTime()
            insertEntity(this)
        }
    }

    override suspend fun getAll(): List<ParcelStatusEntity>?{
        return appDatabase.parcelManagementDao().getAll()
    }

}