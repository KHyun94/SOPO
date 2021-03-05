package com.delivery.sopo.repository.interfaces

import androidx.lifecycle.LiveData
import com.delivery.sopo.database.room.entity.ParcelManagementEntity
import com.delivery.sopo.models.parcel.ParcelId

interface ParcelManagementRepository {
   suspend fun getAll(): List<ParcelManagementEntity>?
   fun getIsDeleteCntLiveData():LiveData<Int>
   fun getIsUpdateCntLiveData(): LiveData<Int>
   fun getIsDeliveredCntLiveData(): LiveData<Int>
   suspend fun getIsUpdateCnt(): Int
   suspend fun getIsDeleteCnt(): Int
   suspend fun getIsDeliveredCnt(): Int
   suspend fun getCancelIsBeDelete():  List<ParcelManagementEntity>?
   suspend fun getIsUnidentifiedByParcelId(parcelId: ParcelId) : Int
   fun insertEntity(parcelManagementEntity: ParcelManagementEntity)
   fun insertEntities(parcelManagementEntityList: List<ParcelManagementEntity>)
   suspend fun updateEntity(parcelManagementEntity: ParcelManagementEntity)
   suspend fun updateEntities(parcelManagementEntityList: List<ParcelManagementEntity>)
   suspend fun updateIsBeUpdate(regDt: String, parcelUid: String, status : Int?)
   fun getEntity(regDt: String, parcelUid: String): ParcelManagementEntity?
   suspend fun initializeIsBeUpdate(regDt: String, parcelUid: String)
   suspend fun updateTotalIsBeDeliveredToZero()
   suspend fun updateIsBeDeleteToOneByParcelIdList(parcelIdList: List<ParcelId>)
   fun updateIsUnidentified(parcelId: ParcelId, value : Int) : Int

}