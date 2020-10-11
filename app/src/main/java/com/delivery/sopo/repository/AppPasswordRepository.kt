package com.delivery.sopo.repository

import androidx.lifecycle.LiveData
import com.delivery.sopo.database.room.entity.AppPasswordEntity

interface AppPasswordRepository {
   fun getAppPassword(): AppPasswordEntity?
   fun getCntOfAppPasswordLiveData(): LiveData<Int>
   fun insertEntity(entity: AppPasswordEntity)
   fun updateEntity(entity: AppPasswordEntity)
   fun deleteEntity(entity: AppPasswordEntity)
   fun deleteAll()
}