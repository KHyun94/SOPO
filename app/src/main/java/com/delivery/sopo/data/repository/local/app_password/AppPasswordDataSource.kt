package com.delivery.sopo.data.repository.local.app_password

import androidx.lifecycle.LiveData
import com.delivery.sopo.data.database.room.dto.AppPasswordDTO

interface AppPasswordDataSource {
   fun get(): AppPasswordDTO?
   fun getCntOfAppPasswordLiveData(): LiveData<Int>
   fun insert(entity: AppPasswordDTO)
   fun update(entity: AppPasswordDTO)
   fun delete(entity: AppPasswordDTO)
   fun deleteAll()
}