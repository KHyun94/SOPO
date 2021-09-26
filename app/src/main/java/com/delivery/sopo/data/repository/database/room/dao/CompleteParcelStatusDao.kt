package com.delivery.sopo.data.repository.database.room.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.room.OnConflictStrategy.REPLACE
import com.delivery.sopo.data.repository.database.room.entity.CompleteParcelStatusEntity

@Dao
interface CompleteParcelStatusDao
{
    @Query("SELECT * FROM COMPLETE_PARCEL_STATUS WHERE TIME LIKE :time")
    fun findById(time: String): List<CompleteParcelStatusEntity>?

    @Query("SELECT * FROM COMPLETE_PARCEL_STATUS WHERE TIME = :time")
    fun getById(time: String): CompleteParcelStatusEntity?

    @Query("SELECT * FROM COMPLETE_PARCEL_STATUS WHERE VISIBILITY >= 0")
    fun getAllTimeCount(): List<CompleteParcelStatusEntity>?

    @Query("SELECT * FROM COMPLETE_PARCEL_STATUS WHERE VISIBILITY >= 0")
    fun getAllTimeCountLiveData(): LiveData<MutableList<CompleteParcelStatusEntity>>

    @Query("SELECT * FROM COMPLETE_PARCEL_STATUS WHERE VISIBILITY = 1 ")
    fun getCurrentTimeCount(): CompleteParcelStatusEntity?

    @Query("SELECT * FROM COMPLETE_PARCEL_STATUS WHERE VISIBILITY = 1")
    fun getCurrentTimeCountLiveData(): LiveData<CompleteParcelStatusEntity?>

    @Query("SELECT COUNT(*) FROM COMPLETE_PARCEL_STATUS WHERE VISIBILITY = 1 AND COUNT = 0")
    fun getRefreshCriteriaLiveData(): LiveData<Int>

    @Query("SELECT * FROM COMPLETE_PARCEL_STATUS WHERE TIME = :time AND AUDIT_DTE >= DATETIME('now', 'localtime', '-3.0 seconds')")
    fun getLatestUpdatedEntity(time: String) : CompleteParcelStatusEntity?

    @Query("SELECT SUM(COUNT) FROM COMPLETE_PARCEL_STATUS WHERE VISIBILITY >= 0")
    fun getSumOfCountLiveData(): LiveData<Int>

    @Insert(onConflict = REPLACE)
    fun insert(completeParcelStatusEntity: CompleteParcelStatusEntity)

    @Insert(onConflict = REPLACE)
    fun insert(listCompleteParcelStatusEntity: List<CompleteParcelStatusEntity>)

    @Delete
    fun delete(completeParcelStatusEntity: CompleteParcelStatusEntity)

    @Delete
    fun delete(listCompleteParcelStatusEntity: List<CompleteParcelStatusEntity>)

    @Query("DELETE FROM COMPLETE_PARCEL_STATUS")
    fun deleteAll()

    @Update
    fun update(completeParcelStatusEntity: CompleteParcelStatusEntity)

    @Update
    fun update(listCompleteParcelStatusEntity: List<CompleteParcelStatusEntity>)
}