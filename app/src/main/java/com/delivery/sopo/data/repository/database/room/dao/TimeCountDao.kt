package com.delivery.sopo.data.repository.database.room.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.room.OnConflictStrategy.REPLACE
import com.delivery.sopo.data.repository.database.room.entity.ParcelCntInfoEntity

@Dao
interface TimeCountDao
{
    @Query("SELECT * FROM PARCEL_CNT_INFO WHERE TIME = :time")
    fun getById(time: String): ParcelCntInfoEntity?

    @Query("SELECT * FROM PARCEL_CNT_INFO WHERE VISIBILITY >= 0")
    fun getAllTimeCount(): List<ParcelCntInfoEntity>?

    @Query("SELECT * FROM PARCEL_CNT_INFO WHERE VISIBILITY >= 0")
    fun getAllTimeCountLiveData(): LiveData<MutableList<ParcelCntInfoEntity>>

    @Query("SELECT * FROM PARCEL_CNT_INFO WHERE VISIBILITY = 1 ")
    fun getCurrentTimeCount(): ParcelCntInfoEntity?

    @Query("SELECT * FROM PARCEL_CNT_INFO WHERE VISIBILITY = 1")
    fun getCurrentTimeCountLiveData(): LiveData<ParcelCntInfoEntity?>

    @Query("SELECT COUNT(*) FROM PARCEL_CNT_INFO WHERE VISIBILITY = 1 AND COUNT = 0")
    fun getRefreshCriteriaLiveData(): LiveData<Int>

    @Query("SELECT * FROM PARCEL_CNT_INFO WHERE TIME = :time AND AUDIT_DTE >= DATETIME('now', 'localtime', '-3.0 seconds')")
    fun getLatestUpdatedEntity(time: String) : ParcelCntInfoEntity?

    @Query("SELECT SUM(COUNT) FROM PARCEL_CNT_INFO WHERE VISIBILITY >= 0")
    fun getSumOfCountLiveData(): LiveData<Int>

    @Insert(onConflict = REPLACE)
    fun insert(parcelCntInfoEntity: ParcelCntInfoEntity)

    @Insert(onConflict = REPLACE)
    fun insert(listParcelCntInfoEntity: List<ParcelCntInfoEntity>)

    @Delete
    fun delete(parcelCntInfoEntity: ParcelCntInfoEntity)

    @Delete
    fun delete(listParcelCntInfoEntity: List<ParcelCntInfoEntity>)

    @Query("DELETE FROM PARCEL_CNT_INFO")
    fun deleteAll()

    @Update
    fun update(parcelCntInfoEntity: ParcelCntInfoEntity)

    @Update
    fun update(listParcelCntInfoEntity: List<ParcelCntInfoEntity>)
}