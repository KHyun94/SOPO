package com.delivery.sopo.database.room.dao

import android.util.Log
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.delivery.sopo.database.room.entity.LogEntity

@Dao
interface LogDao
{
    @Query("SELECT * FROM LOG")
    suspend fun getAll():List<LogEntity>?

    @Insert
    fun insert(logEntity: LogEntity)

}