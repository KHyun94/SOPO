package com.delivery.sopo.data.repository.local.repository

import androidx.lifecycle.LiveData
import com.delivery.sopo.data.repository.database.room.AppDatabase
import com.delivery.sopo.data.repository.database.room.entity.ParcelCntInfoEntity
import com.delivery.sopo.data.repository.local.datasource.TimeCountRepository
import com.delivery.sopo.data.repository.local.user.UserLocalRepository
import com.delivery.sopo.util.TimeUtil

class TimeCountRepoImpl(private val userLocalRepository : UserLocalRepository,
                        private val appDatabase: AppDatabase):
    TimeCountRepository
{
    override fun getById(time: String): ParcelCntInfoEntity?
    {
        return appDatabase.timeCountDao().getById(time)
    }

    override fun getAll(): List<ParcelCntInfoEntity>?
    {
        return appDatabase.timeCountDao().getAllTimeCount()
    }

    override fun getAllLiveData(): LiveData<MutableList<ParcelCntInfoEntity>>
    {
        return appDatabase.timeCountDao().getAllTimeCountLiveData()
    }

    override fun getRefreshCriteriaLiveData(): LiveData<Int>
    {
        return appDatabase.timeCountDao().getRefreshCriteriaLiveData()
    }

    override fun getCurrentTimeCount(): ParcelCntInfoEntity?
    {
        return appDatabase.timeCountDao().getCurrentTimeCount()
    }

    override fun getLatestUpdatedEntity(time: String): ParcelCntInfoEntity?
    {
        return appDatabase.timeCountDao().getLatestUpdatedEntity(time)
    }

    override fun getSumOfCountLiveData(): LiveData<Int>
    {
        return appDatabase.timeCountDao().getSumOfCountLiveData()
    }

    override fun getCurrentTimeCountLiveData(): LiveData<ParcelCntInfoEntity?>
    {
        return appDatabase.timeCountDao().getCurrentTimeCountLiveData()
    }

    override fun insertEntity(entity: ParcelCntInfoEntity)
    {
        entity.auditDte = TimeUtil.getDateTime()
        appDatabase.timeCountDao().insert(entity)
    }

    override fun insertEntities(entities: List<ParcelCntInfoEntity>)
    {
        entities.forEach { it.auditDte = TimeUtil.getDateTime() }
        appDatabase.timeCountDao().insert(entities)
    }

    override fun updateEntity(entity: ParcelCntInfoEntity)
    {
        entity.auditDte = TimeUtil.getDateTime()
        appDatabase.timeCountDao().update(entity)
    }

    override fun updateEntities(entities: List<ParcelCntInfoEntity>)
    {
        entities.forEach { it.auditDte = TimeUtil.getDateTime() }
        appDatabase.timeCountDao().update(entities)
    }


    override fun deleteAll()
    {
        appDatabase.timeCountDao().deleteAll()
    }

    override fun deleteEntities(entities: List<ParcelCntInfoEntity>)
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