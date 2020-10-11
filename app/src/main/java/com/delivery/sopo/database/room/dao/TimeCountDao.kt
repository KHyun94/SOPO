package com.delivery.sopo.database.room.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.room.OnConflictStrategy.REPLACE
import com.delivery.sopo.database.room.entity.TimeCountEntity

@Dao
interface TimeCountDao
{
    @Query("SELECT * FROM TIME_COUNT WHERE TIME = :time")
    fun getById(time: String): TimeCountEntity?

    @Query("SELECT * FROM TIME_COUNT WHERE VISIBILITY >= 0")
    fun getAllTimeCount(): List<TimeCountEntity>?

    @Query("SELECT * FROM TIME_COUNT WHERE VISIBILITY >= 0")
    fun getAllTimeCountLiveData(): LiveData<MutableList<TimeCountEntity>>

    @Query("SELECT * FROM TIME_COUNT WHERE VISIBILITY = 1 ")
    fun getCurrentTimeCount(): TimeCountEntity?

    @Query("SELECT * FROM TIME_COUNT WHERE VISIBILITY = 1")
    fun getCurrentTimeCountLiveData(): LiveData<TimeCountEntity?>

    @Query("SELECT COUNT(*) FROM TIME_COUNT WHERE VISIBILITY = 1 AND COUNT = 0")
    fun getRefreshCriteriaLiveData(): LiveData<Int>

    @Query("SELECT * FROM TIME_COUNT WHERE TIME = :time AND AUDIT_DTE >= DATETIME('now', 'localtime', '-3.0 seconds')")
    fun getLatestUpdatedEntity(time: String) : TimeCountEntity?

    @Query("SELECT SUM(COUNT) FROM TIME_COUNT WHERE VISIBILITY >= 0")
    fun getSumOfCountLiveData(): LiveData<Int>

    @Insert(onConflict = REPLACE)
    fun insert(timeCountEntity: TimeCountEntity)

    @Insert(onConflict = REPLACE)
    fun insert(listTimeCountEntity: List<TimeCountEntity>)

    @Delete
    fun delete(timeCountEntity: TimeCountEntity)

    @Delete
    fun delete(listTimeCountEntity: List<TimeCountEntity>)

    @Query("DELETE FROM TIME_COUNT")
    fun deleteAll()

    @Update
    fun update(timeCountEntity: TimeCountEntity)

    @Update
    fun update(listTimeCountEntity: List<TimeCountEntity>)
}