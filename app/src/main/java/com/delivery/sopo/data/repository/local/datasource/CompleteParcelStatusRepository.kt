package com.delivery.sopo.data.repository.local.datasource

import androidx.lifecycle.LiveData
import com.delivery.sopo.data.repository.database.room.dto.CompleteParcelStatusDTO
import com.delivery.sopo.data.repository.database.room.entity.CompleteParcelStatusEntity

interface CompleteParcelStatusRepository {
   fun findById(year: String): List<CompleteParcelStatusDTO>
   fun getById(time: String): CompleteParcelStatusEntity?
   fun getAll(): List<CompleteParcelStatusEntity>?
   fun getAllAsLiveData(): LiveData<MutableList<CompleteParcelStatusDTO>>
   fun getRefreshCriteriaLiveData(): LiveData<Int>
   fun getCurrentTimeCount(): CompleteParcelStatusEntity?
   fun getLatestUpdatedEntity(time: String): CompleteParcelStatusEntity?
   fun getSumOfCountLiveData(): LiveData<Int>
   fun insertEntity(entityComplete: CompleteParcelStatusEntity)
   fun insertEntities(entityCompletes: List<CompleteParcelStatusEntity>)
   fun updateEntity(entityComplete: CompleteParcelStatusEntity)
   fun updateEntities(entityCompletes: List<CompleteParcelStatusEntity>)
   fun deleteAll()
   fun deleteEntities(entityCompletes: List<CompleteParcelStatusEntity>)

   fun updateVisibilityToZero()
   fun getCurrentTimeCountLiveData(): LiveData<CompleteParcelStatusEntity?>
}