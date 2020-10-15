package com.delivery.sopo.database.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import com.delivery.sopo.database.room.entity.WorkEntity

@Dao
interface WorkDao
{
    @Query("SELECT COUNT(*) FROM WORK")
    fun getCnt() : Int?

    @Query("SELECT * FROM WORK")
    fun getAll() : List<WorkEntity>?

    @Insert(onConflict = REPLACE)
    fun insert(work : WorkEntity)

    @Delete
    fun delete(work : WorkEntity)
}