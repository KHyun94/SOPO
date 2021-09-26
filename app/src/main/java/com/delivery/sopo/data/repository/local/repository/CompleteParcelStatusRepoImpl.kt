package com.delivery.sopo.data.repository.local.repository

import android.os.Parcel
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.delivery.sopo.data.repository.database.room.AppDatabase
import com.delivery.sopo.data.repository.database.room.dto.CompleteParcelStatusDTO
import com.delivery.sopo.data.repository.database.room.entity.CompleteParcelStatusEntity
import com.delivery.sopo.data.repository.local.datasource.CompleteParcelStatusRepository
import com.delivery.sopo.models.mapper.ParcelMapper
import com.delivery.sopo.util.TimeUtil

class CompleteParcelStatusRepoImpl(private val appDatabase: AppDatabase): CompleteParcelStatusRepository
{
    override fun findById(year: String): List<CompleteParcelStatusDTO>
    {
        return appDatabase.completeParcelStatusDao().findById(year)?.flatMap {
            listOf(ParcelMapper.completeParcelStatusEntityToDTO(it))
        }?: emptyList()
    }

    override fun getById(time: String): CompleteParcelStatusEntity?
    {
        return appDatabase.completeParcelStatusDao().getById(time)
    }

    override fun getAll(): List<CompleteParcelStatusEntity>?
    {
        return appDatabase.completeParcelStatusDao().getAllTimeCount()
    }

    override fun getAllAsLiveData(): LiveData<MutableList<CompleteParcelStatusDTO>>
    {
        return Transformations.map(appDatabase.completeParcelStatusDao().getAllTimeCountLiveData()){ entity ->
            entity.map(ParcelMapper::completeParcelStatusEntityToDTO).toMutableList()
        }
    }

    override fun getRefreshCriteriaLiveData(): LiveData<Int>
    {
        return appDatabase.completeParcelStatusDao().getRefreshCriteriaLiveData()
    }

    override fun getCurrentTimeCount(): CompleteParcelStatusEntity?
    {
        return appDatabase.completeParcelStatusDao().getCurrentTimeCount()
    }

    override fun getLatestUpdatedEntity(time: String): CompleteParcelStatusEntity?
    {
        return appDatabase.completeParcelStatusDao().getLatestUpdatedEntity(time)
    }

    override fun getSumOfCountLiveData(): LiveData<Int>
    {
        return appDatabase.completeParcelStatusDao().getSumOfCountLiveData()
    }

    override fun getCurrentTimeCountLiveData(): LiveData<CompleteParcelStatusEntity?>
    {
        return appDatabase.completeParcelStatusDao().getCurrentTimeCountLiveData()
    }

    override fun insertEntity(entityComplete: CompleteParcelStatusEntity)
    {
        entityComplete.auditDte = TimeUtil.getDateTime()
        appDatabase.completeParcelStatusDao().insert(entityComplete)
    }

    override fun insertEntities(entityCompletes: List<CompleteParcelStatusEntity>)
    {
        entityCompletes.forEach { it.auditDte = TimeUtil.getDateTime() }
        appDatabase.completeParcelStatusDao().insert(entityCompletes)
    }

    override fun updateEntity(entityComplete: CompleteParcelStatusEntity)
    {
        entityComplete.auditDte = TimeUtil.getDateTime()
        appDatabase.completeParcelStatusDao().update(entityComplete)
    }

    override fun updateEntities(entityCompletes: List<CompleteParcelStatusEntity>)
    {
        entityCompletes.forEach { it.auditDte = TimeUtil.getDateTime() }
        appDatabase.completeParcelStatusDao().update(entityCompletes)
    }


    override fun deleteAll()
    {
        appDatabase.completeParcelStatusDao().deleteAll()
    }

    override fun deleteEntities(entityCompletes: List<CompleteParcelStatusEntity>)
    {
        appDatabase.completeParcelStatusDao().delete(entityCompletes)
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