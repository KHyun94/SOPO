package com.delivery.sopo.data.repository.local.datasource

import androidx.lifecycle.LiveData
import com.delivery.sopo.data.repository.database.room.entity.ParcelStatusEntity
import com.delivery.sopo.models.parcel.ParcelId

interface ParcelManagementRepository {
   suspend fun getAll(): List<ParcelStatusEntity>?
   fun getIsDeleteCntLiveData():LiveData<Int>
   fun getIsUpdateCntLiveData(): LiveData<Int>
   fun getIsDeliveredCntLiveData(): LiveData<Int>
   suspend fun getCountForUpdatableParcel(): Int
   suspend fun getIsDeleteCnt(): Int
   suspend fun getIsDeliveredCnt(): Int
   suspend fun getCancelIsBeDelete():  List<ParcelStatusEntity>?
   suspend fun getUnidentifiedStatusByParcelId(parcelId: ParcelId) : Int
   fun insertEntity(parcelStatusEntity: ParcelStatusEntity)
   fun insertEntities(parcelStatusEntityList: List<ParcelStatusEntity>)
   suspend fun updateEntity(parcelStatusEntity: ParcelStatusEntity)
   suspend fun updateEntities(parcelStatusEntityList: List<ParcelStatusEntity>)
   suspend fun updateUpdatableStatus(regDt: String, parcelUid: String, status : Int?)
   fun getEntity(parcelId: ParcelId): ParcelStatusEntity?
   suspend fun initializeIsBeUpdate(regDt: String, parcelUid: String)
   suspend fun updateTotalIsBeDeliveredToZero()
   suspend fun updateIsBeDeleteToOneByParcelIdList(parcelIdList: List<ParcelId>)
   fun updateIsUnidentified(parcelId: ParcelId, value : Int) : Int

}