package com.delivery.sopo.repository.impl

import androidx.lifecycle.LiveData
import com.delivery.sopo.database.room.AppDatabase
import com.delivery.sopo.database.room.entity.ParcelManagementEntity
import com.delivery.sopo.models.parcel.ParcelId
import com.delivery.sopo.repository.interfaces.ParcelManagementRepository
import com.delivery.sopo.util.TimeUtil

class ParcelManagementRepoImpl(private val appDatabase: AppDatabase):
    ParcelManagementRepository
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

    override suspend fun getIsUpdateCnt(): Int {
        return appDatabase.parcelManagementDao().getIsUpdateCnt()
    }

    override suspend fun getIsDeleteCnt(): Int {
        return appDatabase.parcelManagementDao().getIsDeleteCnt()
    }

    override suspend fun getIsDeliveredCnt(): Int {
        return appDatabase.parcelManagementDao().getIsDeliveredCnt()
    }

    override suspend fun getCancelIsBeDelete(): List<ParcelManagementEntity>? {
        return appDatabase.parcelManagementDao().getCancelIsBeDelete()
    }

    // 업데이트 미확인 체크용도
    override suspend fun getIsUnidentifiedByParcelId(parcelId: ParcelId) = appDatabase.parcelManagementDao().getIsUnidentifiedByParcelId(parcelId.regDt, parcelId.parcelUid)

    override fun insertEntity(parcelManagementEntity: ParcelManagementEntity){
        appDatabase.parcelManagementDao().insert(parcelManagementEntity)
    }

    override fun insertEntities(parcelManagementEntityList: List<ParcelManagementEntity>){
        parcelManagementEntityList.forEach { it.auditDte = TimeUtil.getDateTime() }
        appDatabase.parcelManagementDao().insert(parcelManagementEntityList)
    }

    override suspend fun updateEntity(parcelManagementEntity: ParcelManagementEntity){
        parcelManagementEntity.auditDte = TimeUtil.getDateTime()
        appDatabase.parcelManagementDao().update(parcelManagementEntity)
    }

    override suspend fun updateEntities(parcelManagementEntityList: List<ParcelManagementEntity>){
        parcelManagementEntityList.forEach { it.auditDte = TimeUtil.getDateTime() }
        appDatabase.parcelManagementDao().update(parcelManagementEntityList)
    }

    override suspend fun updateIsBeUpdate(regDt: String, parcelUid: String, status : Int?) = appDatabase.parcelManagementDao().updateIsBeUpdate(regDt, parcelUid, status)

    override fun getEntity(regDt: String, parcelUid: String): ParcelManagementEntity? {
        return appDatabase.parcelManagementDao().getById(regDt, parcelUid)
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
        getEntity(regDt, parcelUid)?.apply {
            this.isBeUpdate = 0
            this.auditDte = TimeUtil.getDateTime()
            insertEntity(this)
        }
    }

    override suspend fun getAll(): List<ParcelManagementEntity>?{
        return appDatabase.parcelManagementDao().getAll()
    }

}