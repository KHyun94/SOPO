package com.delivery.sopo.repository.impl

import androidx.lifecycle.LiveData
import com.delivery.sopo.database.room.AppDatabase
import com.delivery.sopo.database.room.entity.AppPasswordEntity
import com.delivery.sopo.repository.interfaces.AppPasswordRepository
import com.delivery.sopo.util.TimeUtil

class AppPasswordRepoImpl(private val appDatabase: AppDatabase):
    AppPasswordRepository
{
    private val TAG = "LOG.SOPO${this.javaClass.simpleName}"

    override fun getAppPassword(): AppPasswordEntity?
    {
        return appDatabase.securityDao().getAppPassword()
    }

    override fun getCntOfAppPasswordLiveData(): LiveData<Int>
    {
        return appDatabase.securityDao().getCntOfAppPasswordLiveData()
    }

    override fun insertEntity(entity: AppPasswordEntity)
    {
        entity.apply {
            auditDte = TimeUtil.getDateTime()
        }
        return appDatabase.securityDao().insert(entity)
    }

    override fun updateEntity(entity: AppPasswordEntity)
    {
        entity.apply {
            auditDte = TimeUtil.getDateTime()
        }
        return appDatabase.securityDao().update(entity)
    }

    override fun deleteEntity(entity: AppPasswordEntity)
    {
        return appDatabase.securityDao().delete(entity)
    }

    override fun deleteAll()
    {
        return appDatabase.securityDao().deleteAll()
    }


}