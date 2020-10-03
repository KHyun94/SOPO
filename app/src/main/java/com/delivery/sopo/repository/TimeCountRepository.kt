package com.delivery.sopo.repository

import androidx.lifecycle.LiveData
import com.delivery.sopo.models.entity.TimeCountEntity

interface TimeCountRepository {
   fun getById(time: String): TimeCountEntity?
   fun getAll(): List<TimeCountEntity>?
   fun getAllLiveData(): LiveData<MutableList<TimeCountEntity>>
   fun getRefreshCriteriaLiveData(): LiveData<Int>
   fun getCurrentTimeCount(): TimeCountEntity?
   fun getLatestUpdatedEntity(time: String): TimeCountEntity?
   fun insertEntity(entity: TimeCountEntity)
   fun insertEntities(entities: List<TimeCountEntity>)
   fun updateEntity(entity: TimeCountEntity)
   fun updateEntities(entities: List<TimeCountEntity>)
   fun deleteAll()
   fun deleteEntities(entities: List<TimeCountEntity>)

   fun updateVisibilityToZero()
   fun getCurrentTimeCountLiveData(): LiveData<TimeCountEntity?>
}