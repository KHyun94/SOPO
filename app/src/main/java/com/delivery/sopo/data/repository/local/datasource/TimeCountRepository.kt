package com.delivery.sopo.data.repository.local.datasource

import androidx.lifecycle.LiveData
import com.delivery.sopo.data.repository.database.room.entity.ParcelCntInfoEntity

interface TimeCountRepository {
   fun getById(time: String): ParcelCntInfoEntity?
   fun getAll(): List<ParcelCntInfoEntity>?
   fun getAllLiveData(): LiveData<MutableList<ParcelCntInfoEntity>>
   fun getRefreshCriteriaLiveData(): LiveData<Int>
   fun getCurrentTimeCount(): ParcelCntInfoEntity?
   fun getLatestUpdatedEntity(time: String): ParcelCntInfoEntity?
   fun getSumOfCountLiveData(): LiveData<Int>
   fun insertEntity(entity: ParcelCntInfoEntity)
   fun insertEntities(entities: List<ParcelCntInfoEntity>)
   fun updateEntity(entity: ParcelCntInfoEntity)
   fun updateEntities(entities: List<ParcelCntInfoEntity>)
   fun deleteAll()
   fun deleteEntities(entities: List<ParcelCntInfoEntity>)

   fun updateVisibilityToZero()
   fun getCurrentTimeCountLiveData(): LiveData<ParcelCntInfoEntity?>
}