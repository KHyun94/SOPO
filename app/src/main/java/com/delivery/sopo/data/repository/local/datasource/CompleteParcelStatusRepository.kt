package com.delivery.sopo.data.repository.local.datasource

import androidx.lifecycle.LiveData
import com.delivery.sopo.data.repository.database.room.dto.CompletedParcelHistory
import com.delivery.sopo.data.repository.database.room.entity.CompletedParcelHistoryEntity

interface CompleteParcelStatusRepository {
   fun findById(year: String): List<CompletedParcelHistory>
   fun getById(time: String): CompletedParcelHistoryEntity?
   fun getAll(): List<CompletedParcelHistoryEntity>?
   fun getAllAsLiveData(): LiveData<List<CompletedParcelHistory>>
   fun getRefreshCriteriaLiveData(): LiveData<Int>
   fun getCurrentTimeCount(): CompletedParcelHistoryEntity?
   fun getLatestUpdatedEntity(time: String): CompletedParcelHistoryEntity?
   fun getSumOfCountLiveData(): LiveData<Int>
   fun insertEntity(entityCompleted: CompletedParcelHistoryEntity)
   fun insertEntities(entityCompleteds: List<CompletedParcelHistoryEntity>)
   fun updateEntity(entityCompleted: CompletedParcelHistoryEntity)
   fun updateEntities(entityCompleteds: List<CompletedParcelHistoryEntity>)
   fun deleteAll()
   fun deleteEntities(entityCompleteds: List<CompletedParcelHistoryEntity>)

   fun updateVisibilityToZero()
   fun getCurrentTimeCountLiveData(): LiveData<CompletedParcelHistoryEntity?>
}