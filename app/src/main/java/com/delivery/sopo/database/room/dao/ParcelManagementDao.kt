package com.delivery.sopo.database.room.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.room.OnConflictStrategy.REPLACE
import com.delivery.sopo.database.room.entity.ParcelManagementEntity

@Dao
interface ParcelManagementDao
{
    @Query("SELECT * FROM PARCEL_MANAGEMENT")
    suspend fun getAll() : List<ParcelManagementEntity>?

    @Query("SELECT COUNT(*) FROM PARCEL_MANAGEMENT WHERE isBeDelete = 1")
    fun getIsDeleteCntLiveData(): LiveData<Int>

    @Query("SELECT COUNT(*) FROM PARCEL_MANAGEMENT WHERE isBeUpdate = 1")
    fun getIsUpdateCntLiveData(): LiveData<Int>

    @Query("SELECT COUNT(*) FROM PARCEL_MANAGEMENT WHERE isBeDelivered = 1")
    fun getIsDeliveredCntLiveData(): LiveData<Int>

    @Query("SELECT COUNT(*) FROM PARCEL_MANAGEMENT WHERE isBeDelete = 1")
    fun getIsDeleteCnt(): Int

    @Query("SELECT COUNT(*) FROM PARCEL_MANAGEMENT WHERE isBeUpdate = 1")
    fun getIsUpdateCnt(): Int

    @Query("SELECT COUNT(*) FROM PARCEL_MANAGEMENT WHERE isBeDelivered = 1")
    fun getIsDeliveredCnt(): Int

    @Query("SELECT * FROM PARCEL_MANAGEMENT WHERE REG_DT = :regDt AND PARCEL_UID = :parcelUid")
    fun getById(regDt: String, parcelUid: String): ParcelManagementEntity?

    @Query("SELECT * FROM PARCEL_MANAGEMENT WHERE isBeDelete = 1 AND AUDIT_DTE >= DATETIME('now', 'localtime', '-10.0 seconds')")
    suspend fun getCancelIsBeDelete() : List<ParcelManagementEntity>?

    @Query("UPDATE PARCEL_MANAGEMENT SET isBeDelivered = 0")
    fun updateTotalIsBeDeliveredToZero()

    @Query("UPDATE PARCEL_MANAGEMENT SET isBeUpdate = 0 WHERE REG_DT = :regDt AND PARCEL_UID = :parcelUid")
    fun updateIsBeUpdateToZero(regDt: String, parcelUid: String)

    @Query("UPDATE PARCEL_MANAGEMENT SET isBeDelete = 1 , AUDIT_DTE = :auditDte WHERE REG_DT = :regDt AND PARCEL_UID = :parcelUid")
    fun updateIsBeDeleteToOne(regDt: String, parcelUid: String, auditDte: String)

    @Insert(onConflict = REPLACE)
    fun insert(parcelManagementEntity: ParcelManagementEntity)

    @Insert(onConflict = REPLACE)
    fun insert(parcelManagementEntityList: List<ParcelManagementEntity>)

    @Delete
    fun delete(parcelManagementEntity: ParcelManagementEntity)

    @Update
    fun update(parcelManagementEntity: ParcelManagementEntity)

    @Update
    fun update(parcelManagementEntityList: List<ParcelManagementEntity>)
}