package com.delivery.sopo.repository.impl

import androidx.lifecycle.LiveData
import com.delivery.sopo.database.room.AppDatabase
import com.delivery.sopo.models.entity.TimeCountEntity
import com.delivery.sopo.repository.TimeCountRepository
import com.delivery.sopo.repository.shared.UserRepo
import com.delivery.sopo.util.fun_util.TimeUtil

class TimeCountRepoImpl(private val userRepo: UserRepo,
                        private val appDatabase: AppDatabase): TimeCountRepository
{
    override fun getById(time: String): TimeCountEntity?
    {
        return appDatabase.timeCountDao().getById(time)
    }

    override fun getAll(): List<TimeCountEntity>?
    {
        return appDatabase.timeCountDao().getAllTimeCount()
    }

    override fun getAllLiveData(): LiveData<MutableList<TimeCountEntity>>
    {
        return appDatabase.timeCountDao().getAllTimeCountLiveData()
    }

    override fun getRefreshCriteriaLiveData(): LiveData<Int>
    {
        return appDatabase.timeCountDao().getRefreshCriteriaLiveData()
    }

    override fun getCurrentTimeCount(): TimeCountEntity?
    {
        return appDatabase.timeCountDao().getCurrentTimeCount()
    }

    override fun getLatestUpdatedEntity(time: String): TimeCountEntity?
    {
        return appDatabase.timeCountDao().getLatestUpdatedEntity(time)
    }

    override fun getSumOfCountLiveData(): LiveData<Int>
    {
        return appDatabase.timeCountDao().getSumOfCountLiveData()
    }

    override fun getCurrentTimeCountLiveData(): LiveData<TimeCountEntity?>
    {
        return appDatabase.timeCountDao().getCurrentTimeCountLiveData()
    }

    override fun insertEntity(entity: TimeCountEntity)
    {
        entity.auditDte = TimeUtil.getDateTime()
        appDatabase.timeCountDao().insert(entity)
    }

    override fun insertEntities(entities: List<TimeCountEntity>)
    {
        entities.forEach { it.auditDte = TimeUtil.getDateTime() }
        appDatabase.timeCountDao().insert(entities)
    }

    override fun updateEntity(entity: TimeCountEntity)
    {
        entity.auditDte = TimeUtil.getDateTime()
        appDatabase.timeCountDao().update(entity)
    }

    override fun updateEntities(entities: List<TimeCountEntity>)
    {
        entities.forEach { it.auditDte = TimeUtil.getDateTime() }
        appDatabase.timeCountDao().update(entities)
    }


    override fun deleteAll()
    {
        appDatabase.timeCountDao().deleteAll()
    }

    override fun deleteEntities(entities: List<TimeCountEntity>)
    {
        appDatabase.timeCountDao().delete(entities)
    }

    override fun updateVisibilityToZero()
    {
        appDatabase.timeCountDao().getCurrentTimeCount()?.let {
            it.visibility = 0
            it.auditDte = TimeUtil.getDateTime()
            updateEntity(it)
        }
    }
}