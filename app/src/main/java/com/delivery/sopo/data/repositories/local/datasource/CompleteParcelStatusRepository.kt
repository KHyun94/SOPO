package com.delivery.sopo.data.repositories.local.datasource

import androidx.lifecycle.LiveData
import com.delivery.sopo.data.database.room.dto.DeliveredParcelHistory
import com.delivery.sopo.data.database.room.entity.CompletedParcelHistoryEntity

interface CompleteParcelStatusRepository {
   fun findById(year: String): List<DeliveredParcelHistory>
   fun getById(time: String): CompletedParcelHistoryEntity?
   fun getAll(): List<CompletedParcelHistoryEntity>?
   fun getAllAsLiveData(): LiveData<List<DeliveredParcelHistory>>
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