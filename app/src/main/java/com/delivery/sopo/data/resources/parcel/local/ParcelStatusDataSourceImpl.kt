package com.delivery.sopo.data.resources.parcel.local

import androidx.lifecycle.LiveData
import com.delivery.sopo.consts.StatusConst
import com.delivery.sopo.data.database.room.dao.ParcelStatusDao
import com.delivery.sopo.data.database.room.entity.ParcelStatusEntity
import com.delivery.sopo.models.mapper.ParcelMapper
import com.delivery.sopo.models.parcel.Parcel
import com.delivery.sopo.util.TimeUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ParcelStatusDataSourceImpl(private val parcelStatusDao: ParcelStatusDao): ParcelStatusDataSource
{
    override fun get(): List<Parcel.Status>
    {
        return parcelStatusDao.get().map(ParcelMapper::parcelStatusEntityToObject)
    }

    override fun insert(vararg data: Parcel.Status)
    {
        val entities = data.map(ParcelMapper::parcelStatusObjectToEntity)
        parcelStatusDao.insert(entities)
    }

    override fun update(vararg data: Parcel.Status)
    {
        val entities = data.map(ParcelMapper::parcelStatusObjectToEntity)
        parcelStatusDao.update(entities)
    }

    override fun delete(vararg data: Parcel.Status)
    {
        val entities = data.map(ParcelMapper::parcelStatusObjectToEntity)
        parcelStatusDao.delete(entities)
    }

    override fun isUnidentified(parcelId: Int): Boolean
    {
        return parcelStatusDao.getUnidentifiedStatus(parcelId = parcelId) == 1
    }

    override fun getById(parcelId:Int): Parcel.Status
    {
        val entity = parcelStatusDao.getById(parcelId)?: ParcelMapper.parcelStatusObjectToEntity(Parcel.Status(parcelId = parcelId))
        return ParcelMapper.parcelStatusEntityToObject(entity)
    }

    override fun getIsDeleteCntLiveData(): LiveData<Int>
    {
        return parcelStatusDao.getIsDeleteCntLiveData()
    }

    override fun getIsUpdateCntLiveData(): LiveData<Int>
    {
        return parcelStatusDao.getIsUpdateCntLiveData()
    }

    override fun getIsDeliveredCntLiveData(): LiveData<Int>
    {
        return parcelStatusDao.getIsDeliveredCntLiveData()
    }

    override suspend fun getCountForUpdatableParcel(): Int {
        return parcelStatusDao.getCountForUpdatableParcel()
    }

    override suspend fun getIsDeleteCnt(): Int {
        return parcelStatusDao.getIsDeleteCnt()
    }

    override suspend fun getIsDeliveredCnt(): Int {
        return parcelStatusDao.getIsDeliveredCnt()
    }

    override suspend fun getCancelIsBeDelete(): List<ParcelStatusEntity>? {
        return parcelStatusDao.getCancelIsBeDelete()
    }

    // 업데이트 미확인 체크용도
    override suspend fun getUnidentifiedStatus(parcelId:Int): Int = withContext(Dispatchers.Default) {
        parcelStatusDao.getUnidentifiedStatus(parcelId = parcelId)
    }

    override suspend fun updateParcelStatuses(parcelStatus: List<Parcel.Status>){
        val entities = parcelStatus.map(ParcelMapper::parcelStatusObjectToEntity)
        parcelStatusDao.update(entities)
    }

    override suspend fun updateUpdatableStatus(parcelId:Int, status : Int) = parcelStatusDao.updateIsBeUpdate(parcelId, status)

    override suspend fun updateTotalIsBeDeliveredToZero(){
        parcelStatusDao.updateTotalIsBeDeliveredToZero()
    }

    override suspend fun updateIsBeDeleteToOneByParcelIdList(parcelIdList: List<Int>){
        for (parcelId in parcelIdList){
            parcelStatusDao.updateIsBeDeleteToOne(parcelId, TimeUtil.getDateTime())
        }
    }

    override suspend fun updateUnidentifiedStatusById(parcelId:Int, value: Int) = withContext(Dispatchers.Default){
        parcelStatusDao.updateIsUnidentified(parcelId, value)
    }

    override suspend fun updateUnidentifiedStatus(parcels: List<Parcel.Common>)
    {
        val parcelStatuses = parcels.map { getById(it.parcelId) }
            .filter { it.unidentifiedStatus == 1 }
        parcelStatuses.forEach { it.unidentifiedStatus = 0 }
        updateParcelStatuses(parcelStatuses)
    }

    override fun makeParcelStatus(parcel: Parcel.Common): Parcel.Status{
        return getById(parcel.parcelId).apply {
            unidentifiedStatus = if(!parcel.reported) StatusConst.ACTIVATE else StatusConst.DEACTIVATE
            updatableStatus = 0
            auditDte = TimeUtil.getDateTime()
        }
    }

}