package com.delivery.sopo.models.dao

import androidx.room.*
import androidx.room.OnConflictStrategy.REPLACE
import com.delivery.sopo.models.entity.ParcelEntity

@Dao
interface ParcelDao
{
    // 로컬에서 단일 택배 조회
    @Query("SELECT * FROM PARCEL WHERE TRACK_NUM = :waybilNum")
    suspend fun getSingleParcelWithWaybilNum(waybilNum: String): ParcelEntity?

    @Query("SELECT * FROM PARCEL WHERE STATUS = 1 AND DELIVERY_STATUS <> 'delivered'")
    suspend fun getOngoingData(): List<ParcelEntity>

    @Query("SELECT COUNT(*) FROM PARCEL WHERE STATUS = 1 AND DELIVERY_STATUS <> 'delivered'")
    suspend fun getOngoingDataCnt(): Int

    @Query("SELECT * FROM PARCEL WHERE STATUS = 1 AND DELIVERY_STATUS = 'delivered'")
    suspend fun getCompleteData(): List<ParcelEntity>

    @Query("SELECT * FROM PARCEL WHERE STATUS = 3")
    suspend fun getBeDeletedData(): List<ParcelEntity>

    @Query("SELECT * FROM PARCEL WHERE STATUS = 3 AND AUDIT_DTE >= DATETIME('now', 'localtime', '-10.0 seconds')")
    suspend fun getBeDeleteCanceledData() : List<ParcelEntity>

    @Query("SELECT * FROM PARCEL WHERE REG_DT = :regDt AND PARCEL_UID = :parcelUid")
    suspend fun getById(regDt: String, parcelUid: String): ParcelEntity?

    @Insert(onConflict = REPLACE)
    fun insert(parcelEntity: ParcelEntity)

    @Insert(onConflict = REPLACE)
    fun insert(listParcelEntity: List<ParcelEntity>)

    @Delete
    fun delete(parcelEntity: ParcelEntity)

    @Delete
    fun delete(listParcelEntity: List<ParcelEntity>)

    @Update
    fun update(parcelEntity: ParcelEntity)

    @Update
    fun update(listParcelEntity: List<ParcelEntity>)
}