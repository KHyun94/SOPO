package com.delivery.sopo.data.repository.database.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.delivery.sopo.data.repository.database.room.entity.LogEntity

@Dao
interface LogDao
{
    @Query("SELECT * FROM LOG")
    suspend fun getAll():List<LogEntity>?

    @Insert
    fun insert(logEntity: LogEntity)

}