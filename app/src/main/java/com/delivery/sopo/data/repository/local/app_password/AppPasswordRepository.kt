package com.delivery.sopo.data.repository.local.app_password

import androidx.lifecycle.LiveData
import com.delivery.sopo.data.repository.database.room.AppDatabase
import com.delivery.sopo.data.repository.database.room.entity.AppPasswordEntity
import com.delivery.sopo.util.TimeUtil

class AppPasswordRepository(private val appDatabase: AppDatabase): AppPasswordDataSource
{
    override fun get(): AppPasswordEntity?
    {
        return appDatabase.securityDao().get()
    }

    fun getByLiveData(): LiveData<AppPasswordEntity?> = appDatabase.securityDao().getByLiveData()

    override fun getCntOfAppPasswordLiveData(): LiveData<Int>
    {
        return appDatabase.securityDao().getCntOfAppPasswordLiveData()
    }

    override fun insert(entity: AppPasswordEntity)
    {
        entity.apply {
            auditDte = TimeUtil.getDateTime()
        }
        return appDatabase.securityDao().insert(entity)
    }

    override fun update(entity: AppPasswordEntity)
    {
        entity.apply {
            auditDte = TimeUtil.getDateTime()
        }
        return appDatabase.securityDao().update(entity)
    }

    override fun delete(entity: AppPasswordEntity)
    {
        return appDatabase.securityDao().delete(entity)
    }

    override fun deleteAll()
    {
        return appDatabase.securityDao().deleteAll()
    }


}