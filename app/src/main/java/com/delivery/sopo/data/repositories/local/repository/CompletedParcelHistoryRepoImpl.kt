package com.delivery.sopo.data.repositories.local.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.delivery.sopo.data.database.room.AppDatabase
import com.delivery.sopo.data.database.room.dto.DeliveredParcelHistory
import com.delivery.sopo.data.database.room.entity.CompletedParcelHistoryEntity
import com.delivery.sopo.data.repositories.local.datasource.CompleteParcelStatusRepository
import com.delivery.sopo.models.mapper.ParcelMapper
import com.delivery.sopo.util.TimeUtil
import javax.inject.Inject

class CompletedParcelHistoryRepoImpl @Inject constructor(private val appDatabase: AppDatabase): CompleteParcelStatusRepository
{
    override fun findById(year: String): List<DeliveredParcelHistory>
    {
        return appDatabase.completeParcelStatusDao().findById(year)?.flatMap {
            listOf(ParcelMapper.completeParcelStatusEntityToDTO(it))
        }?: emptyList()
    }

    override fun getById(time: String): CompletedParcelHistoryEntity?
    {
        return appDatabase.completeParcelStatusDao().getById(time)
    }

    override fun getAll(): List<CompletedParcelHistoryEntity>?
    {
        return appDatabase.completeParcelStatusDao().getAllTimeCount()
    }

    override fun getAllAsLiveData(): LiveData<List<DeliveredParcelHistory>>
    {
        return Transformations.map(appDatabase.completeParcelStatusDao().getAllTimeCountLiveData()){ entity ->
            entity.map(ParcelMapper::completeParcelStatusEntityToDTO)
        }
    }

    override fun getRefreshCriteriaLiveData(): LiveData<Int>
    {
        return appDatabase.completeParcelStatusDao().getRefreshCriteriaLiveData()
    }

    override fun getCurrentTimeCount(): CompletedParcelHistoryEntity?
    {
        return appDatabase.completeParcelStatusDao().getCurrentTimeCount()
    }

    override fun getLatestUpdatedEntity(time: String): CompletedParcelHistoryEntity?
    {
        return appDatabase.completeParcelStatusDao().getLatestUpdatedEntity(time)
    }

    override fun getSumOfCountLiveData(): LiveData<Int>
    {
        return appDatabase.completeParcelStatusDao().getSumOfCountLiveData()
    }

    override fun getCurrentTimeCountLiveData(): LiveData<CompletedParcelHistoryEntity?>
    {
        return appDatabase.completeParcelStatusDao().getCurrentTimeCountLiveData()
    }

    override fun insertEntity(entityCompleted: CompletedParcelHistoryEntity)
    {
        entityCompleted.auditDte = TimeUtil.getDateTime()
        appDatabase.completeParcelStatusDao().insert(entityCompleted)
    }

    override fun insertEntities(entityCompleteds: List<CompletedParcelHistoryEntity>)
    {
        entityCompleteds.forEach { it.auditDte = TimeUtil.getDateTime() }
        appDatabase.completeParcelStatusDao().insert(entityCompleteds)
    }

    override fun updateEntity(entityCompleted: CompletedParcelHistoryEntity)
    {
        entityCompleted.auditDte = TimeUtil.getDateTime()
        appDatabase.completeParcelStatusDao().update(entityCompleted)
    }

    override fun updateEntities(entityCompleteds: List<CompletedParcelHistoryEntity>)
    {
        entityCompleteds.forEach { it.auditDte = TimeUtil.getDateTime() }
        appDatabase.completeParcelStatusDao().update(entityCompleteds)
    }


    override fun deleteAll()
    {
        appDatabase.completeParcelStatusDao().deleteAll()
    }

    override fun deleteEntities(entityCompleteds: List<CompletedParcelHistoryEntity>)
    {
        appDatabase.completeParcelStatusDao().delete(entityCompleteds)
    }

    override fun updateVisibilityToZero()
    {
        appDatabase.completeParcelStatusDao().getCurrentTimeCount()?.let {
            it.visibility = 0
            it.auditDte = TimeUtil.getDateTime()
            updateEntity(it)
        }
    }
}