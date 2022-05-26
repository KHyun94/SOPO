package com.delivery.sopo.data.resources.parcel.local

import androidx.lifecycle.LiveData
import com.delivery.sopo.data.database.room.entity.ParcelStatusEntity
import com.delivery.sopo.interfaces.BaseDataSource
import com.delivery.sopo.models.parcel.Parcel

interface ParcelStatusDataSource: BaseDataSource<Parcel.Status>
{
    override fun get(): List<Parcel.Status>
    override fun insert(vararg data: Parcel.Status)
    override fun update(vararg data: Parcel.Status)
    override fun delete(vararg data: Parcel.Status)

    fun getIsDeleteCntLiveData(): LiveData<Int>
    fun getIsUpdateCntLiveData(): LiveData<Int>
    fun getIsDeliveredCntLiveData(): LiveData<Int>
    suspend fun getCountForUpdatableParcel(): Int
    suspend fun getIsDeleteCnt(): Int
    suspend fun getIsDeliveredCnt(): Int
    suspend fun getCancelIsBeDelete():  List<ParcelStatusEntity>?
    suspend fun getUnidentifiedStatus(parcelId: Int) : Int
    suspend fun updateParcelStatuses(parcelStatus: List<Parcel.Status>)
    suspend fun updateUpdatableStatus(parcelId:Int, status : Int)
    fun getById(parcelId: Int): Parcel.Status?
    suspend fun updateTotalIsBeDeliveredToZero()
    suspend fun updateIsBeDeleteToOneByParcelIdList(parcelIdList: List<Int>)
    suspend fun updateUnidentifiedStatusById(parcelId: Int, value : Int) : Int
    suspend fun updateUnidentifiedStatus(parcels: List<Parcel.Common>)
    fun makeParcelStatus(parcel: Parcel.Common): Parcel.Status

}