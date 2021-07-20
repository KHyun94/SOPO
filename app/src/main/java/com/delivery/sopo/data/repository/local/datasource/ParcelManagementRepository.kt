package com.delivery.sopo.data.repository.local.datasource

import androidx.lifecycle.LiveData
import com.delivery.sopo.data.repository.database.room.entity.ParcelStatusEntity

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
   fun insertEntity(parcelStatusEntity: ParcelStatusEntity)
   fun insertEntities(parcelStatusEntityList: List<ParcelStatusEntity>)
   suspend fun updateEntity(parcelStatusEntity: ParcelStatusEntity)
   suspend fun updateEntities(parcelStatusEntityList: List<ParcelStatusEntity>)
   suspend fun updateUpdatableStatus(parcelId:Int, status : Int)
   fun getEntity(parcelId: Int): ParcelStatusEntity?
   suspend fun initializeIsBeUpdate(parcelId:Int)
   suspend fun updateTotalIsBeDeliveredToZero()
   suspend fun updateIsBeDeleteToOneByParcelIdList(parcelIdList: List<Int>)
   fun updateIsUnidentified(parcelId: Int, value : Int) : Int

}