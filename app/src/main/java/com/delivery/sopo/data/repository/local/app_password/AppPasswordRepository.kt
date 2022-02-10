package com.delivery.sopo.data.repository.local.app_password

import androidx.lifecycle.LiveData
import com.delivery.sopo.data.database.room.AppDatabase
import com.delivery.sopo.data.database.room.dto.AppPasswordDTO
import com.delivery.sopo.models.mapper.AppPasswordMapper
import com.delivery.sopo.util.TimeUtil

class AppPasswordRepository(private val appDatabase: AppDatabase): AppPasswordDataSource
{
    override fun get(): AppPasswordDTO?
    {
        return appDatabase.securityDao().get()?.let { AppPasswordMapper.entityToDto(it) }
    }

    override fun getCntOfAppPasswordLiveData(): LiveData<Int>
    {
        return appDatabase.securityDao().getCntOfAppPasswordLiveData()
    }

    override fun insert(dto: AppPasswordDTO)
    {
        val entity = AppPasswordMapper.dtoToEntity(dto.apply { auditDte = TimeUtil.getDateTime() })
        return appDatabase.securityDao().insert(entity)
    }

    override fun update(dto: AppPasswordDTO)
    {
        val entity = AppPasswordMapper.dtoToEntity(dto.apply { auditDte = TimeUtil.getDateTime() })
        return appDatabase.securityDao().update(entity)
    }

    override fun delete(dto: AppPasswordDTO)
    {
        val entity = AppPasswordMapper.dtoToEntity(dto.apply { auditDte = TimeUtil.getDateTime() })
        return appDatabase.securityDao().delete(entity)
    }

    override fun deleteAll()
    {
        return appDatabase.securityDao().deleteAll()
    }


}