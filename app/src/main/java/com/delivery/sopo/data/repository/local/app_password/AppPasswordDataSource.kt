package com.delivery.sopo.data.repository.local.app_password

import androidx.lifecycle.LiveData
import com.delivery.sopo.data.repository.database.room.entity.AppPasswordEntity

interface AppPasswordDataSource {
   fun get(): AppPasswordEntity?
   fun getCntOfAppPasswordLiveData(): LiveData<Int>
   fun insert(entity: AppPasswordEntity)
   fun update(entity: AppPasswordEntity)
   fun delete(entity: AppPasswordEntity)
   fun deleteAll()
}