package com.delivery.sopo.data.repository.database.room.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.room.OnConflictStrategy.REPLACE
import com.delivery.sopo.data.repository.database.room.entity.CompletedParcelHistoryEntity

@Dao
interface CompleteParcelStatusDao
{
    @Query("SELECT * FROM COMPLETED_PARCEL_HISTORY WHERE DATE LIKE :date")
    fun findById(date: String): List<CompletedParcelHistoryEntity>?

    @Query("SELECT * FROM COMPLETED_PARCEL_HISTORY WHERE DATE = :date")
    fun getById(date: String): CompletedParcelHistoryEntity?

    @Query("SELECT * FROM COMPLETED_PARCEL_HISTORY WHERE VISIBILITY >= 0")
    fun getAllTimeCount(): List<CompletedParcelHistoryEntity>?

    @Query("SELECT * FROM COMPLETED_PARCEL_HISTORY WHERE VISIBILITY >= 0")
    fun getAllTimeCountLiveData(): LiveData<MutableList<CompletedParcelHistoryEntity>>

    @Query("SELECT * FROM COMPLETED_PARCEL_HISTORY WHERE VISIBILITY = 1 ")
    fun getCurrentTimeCount(): CompletedParcelHistoryEntity?

    @Query("SELECT * FROM COMPLETED_PARCEL_HISTORY WHERE VISIBILITY = 1")
    fun getCurrentTimeCountLiveData(): LiveData<CompletedParcelHistoryEntity?>

    @Query("SELECT COUNT(*) FROM COMPLETED_PARCEL_HISTORY WHERE VISIBILITY = 1 AND COUNT = 0")
    fun getRefreshCriteriaLiveData(): LiveData<Int>

    @Query(
        "SELECT * FROM COMPLETED_PARCEL_HISTORY WHERE DATE = :date AND AUDIT_DTE >= DATETIME('now', 'localtime', '-3.0 seconds')")
    fun getLatestUpdatedEntity(date: String) : CompletedParcelHistoryEntity?

    @Query("SELECT SUM(COUNT) FROM COMPLETED_PARCEL_HISTORY WHERE VISIBILITY >= 0")
    fun getSumOfCountLiveData(): LiveData<Int>

    @Insert(onConflict = REPLACE)
    fun insert(completedParcelHistoryEntity: CompletedParcelHistoryEntity)

    @Insert(onConflict = REPLACE)
    fun insert(listCompletedParcelHistoryEntity: List<CompletedParcelHistoryEntity>)

    @Delete
    fun delete(completedParcelHistoryEntity: CompletedParcelHistoryEntity)

    @Delete
    fun delete(listCompletedParcelHistoryEntity: List<CompletedParcelHistoryEntity>)

    @Query("DELETE FROM COMPLETED_PARCEL_HISTORY")
    fun deleteAll()

    @Update
    fun update(completedParcelHistoryEntity: CompletedParcelHistoryEntity)

    @Update
    fun update(listCompletedParcelHistoryEntity: List<CompletedParcelHistoryEntity>)
}