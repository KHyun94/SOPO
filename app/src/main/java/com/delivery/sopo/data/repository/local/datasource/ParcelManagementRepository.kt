package com.delivery.sopo.data.repository.local.datasource

import androidx.lifecycle.LiveData
import com.delivery.sopo.data.database.room.entity.ParcelStatusEntity
import com.delivery.sopo.models.parcel.ParcelStatus

interface ParcelManagementRepository {
   suspend fun getAll(): List<ParcelStatusEntity>?
   fun getIsDeleteCntLiveData():LiveData<Int>
   fun getIsUpdateCntLiveData(): LiveData<Int>
   fun getIsDeliveredCntLiveData(): LiveData<Int>
   suspend fun getCountForUpdatableParcel(): Int
   suspend fun getIsDeleteCnt(): Int
   suspend fun getIsDeliveredCnt(): Int
   suspend fun getCancelIsBeDelete():  List<ParcelStatusEntity>?
   suspend fun getUnidentifiedStatusByParcelId(parcelId: Int) : Int
   fun insertParcelStatus(parcelStatus: ParcelStatus)
   fun insertParcelStatuses(parcelStatusList: List<ParcelStatus>)
   suspend fun update(parcelStatusEntity: ParcelStatusEntity)
   suspend fun updateParcelStatuses(parcelStatuses: List<ParcelStatus>)
   suspend fun updateUpdatableStatus(parcelId:Int, status : Int)
   fun getParcelStatus(parcelId: Int): ParcelStatus?
   suspend fun updateTotalIsBeDeliveredToZero()
   suspend fun updateIsBeDeleteToOneByParcelIdList(parcelIdList: List<Int>)
   suspend fun updateUnidentifiedStatus(parcelId: Int, value : Int) : Int

}