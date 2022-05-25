package com.delivery.sopo.data.resources.parcel.local

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.delivery.sopo.data.database.room.dao.ParcelDao
import com.delivery.sopo.data.database.room.entity.ParcelEntity
import com.delivery.sopo.models.mapper.ParcelMapper
import com.delivery.sopo.models.parcel.Parcel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ParcelDataSourceImpl(private val parcelDao:ParcelDao):ParcelDataSource
{
    override fun get(): List<Parcel.Common>
    {
        return parcelDao.get().map(ParcelMapper::parcelEntityToObject)
    }

    override fun insert(vararg data: Parcel.Common)
    {
        val entities = data.map(ParcelMapper::parcelObjectToEntity)
        parcelDao.insert(entities)
    }

    override fun update(vararg data: Parcel.Common)
    {
        val entities = data.map(ParcelMapper::parcelObjectToEntity)
        parcelDao.update(entities)
    }

    override fun delete(vararg data: Parcel.Common)
    {
        val entities = data.map(ParcelMapper::parcelObjectToEntity)
        parcelDao.delete(entities)
    }

    override fun getParcelById(parcelId: Int): Parcel.Common?
    {
        return parcelDao
            .getById(parcelId = parcelId)
            ?.run(ParcelMapper::parcelEntityToObject)
    }

    override fun hasLocalParcel(parcel: Parcel.Common): Boolean
    {
        return parcelDao.getById(parcel.parcelId) != null
    }

    override fun getNotExistParcels(parcels: List<Parcel.Common>): List<Parcel.Common>
    {
        val parcelIds = parcels.map { it.parcelId }
        return parcelDao.getNotExistParcels(parcelIds = parcelIds).map(ParcelMapper::parcelEntityToObject)
    }

    override fun compareInquiryHash(parcel: Parcel.Common): Boolean
    {
        val local = parcelDao.getById(parcel.parcelId)?.let { ParcelMapper.parcelEntityToObject(it) } ?: return false
        return parcel.inquiryHash != local.inquiryHash
    }

    // 배송 중인 택배 리스트를 LiveData로 받기
    override fun getOngoingParcelAsLiveData(): LiveData<List<Parcel.Common>>
    {
        return Transformations.map(parcelDao.getOngoingLiveData()) { entityList ->
            entityList.map(ParcelMapper::parcelEntityToParcel)
        }
    }

    override fun getCompleteParcelsAsLiveData(): LiveData<List<Parcel.Common>>
    {
        return Transformations.map(parcelDao.getCompleteLiveData()) { entity ->
            entity.map(ParcelMapper::parcelEntityToParcel)
        }
    }

    override fun getCompleteParcelsByDate(date: String): List<Parcel.Common>
    {
        val entity = parcelDao.getCompleteParcelByDate(date)
        return entity.filterNotNull().map(ParcelMapper::parcelEntityToParcel)
    }

    override suspend fun getLocalOngoingParcels(): List<Parcel.Common>
    {
        return parcelDao.getOngoingData().map(ParcelMapper::parcelEntityToParcel)
    }

    override fun getSoonDataCntLiveData(): LiveData<Int>
    {
        return parcelDao.getSoonDataCntLiveData()
    }

    override fun getOngoingDataCntLiveData(): LiveData<Int>
    {
        return parcelDao.getOngoingDataCntLiveData()
    }

    override suspend fun isBeingUpdateParcel(parcelId: Int): LiveData<Int?> =
        parcelDao.isBeingUpdateParcel(parcelId = parcelId)

    override fun getUnidentifiedStatus(parcelId: Int): Int =
        parcelDao.getUnidentifiedStatus(parcelId = parcelId)

    override fun getIsUnidentifiedAsLiveData(parcelId: Int): LiveData<Int?>
    {
        return parcelDao.getIsUnidentifiedLiveData(parcelId)
    }

    override fun getLocalOnGoingParcelCnt(): LiveData<Int>
    {
        return parcelDao.getOngoingDataCntLiveData()
    }

    // 0922 kh 추가사항
    override suspend fun getSingleParcelWithWaybillNum(waybillNum: String): ParcelEntity?
    {
        return parcelDao.getSingleParcelWithwaybillNum(waybillNum = waybillNum)
    }

    override suspend fun getOnGoingDataCnt(): Int = withContext(Dispatchers.Default) {
        return@withContext parcelDao.getOngoingDataCnt()
    }
}