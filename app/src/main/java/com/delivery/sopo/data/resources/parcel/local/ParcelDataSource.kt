package com.delivery.sopo.data.resources.parcel.local

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.delivery.sopo.data.database.room.entity.ParcelEntity
import com.delivery.sopo.interfaces.BaseDataSource
import com.delivery.sopo.models.mapper.ParcelMapper
import com.delivery.sopo.models.parcel.Parcel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface ParcelDataSource:BaseDataSource<Parcel.Common>
{
    override fun delete(vararg data: Parcel.Common)
    override fun get(): List<Parcel.Common>
    override fun insert(vararg data: Parcel.Common)
    override fun update(vararg data: Parcel.Common)

    suspend fun getParcelById(parcelId: Int): Parcel.Common?
    fun hasLocalParcel(parcel: Parcel.Common): Boolean
    fun getNotExistParcels(parcels: List<Parcel.Common>): List<Parcel.Common>

    fun compareInquiryHash(parcel: Parcel.Common): Boolean

    // 배송 중인 택배 리스트를 LiveData로 받기
    fun getOngoingParcelAsLiveData(): LiveData<List<Parcel.Common>>

    fun getCompleteParcelsAsLiveData(): LiveData<List<Parcel.Common>>

    fun getCompleteParcelsByDate(date: String): List<Parcel.Common>

    suspend fun getLocalOngoingParcels(): List<Parcel.Common>
    fun getSoonDataCntLiveData(): LiveData<Int>
    fun getOngoingDataCntLiveData(): LiveData<Int>
    suspend fun isBeingUpdateParcel(parcelId: Int): LiveData<Int?>

    fun getUnidentifiedStatus(parcelId: Int): Int

    fun getIsUnidentifiedAsLiveData(parcelId: Int): LiveData<Int?>

    fun getLocalOnGoingParcelCnt(): LiveData<Int>
    suspend fun getSingleParcelWithWaybillNum(waybillNum: String): ParcelEntity?

    suspend fun getOnGoingDataCnt(): Int
}