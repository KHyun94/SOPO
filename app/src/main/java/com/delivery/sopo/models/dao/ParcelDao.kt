package com.delivery.sopo.models.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.room.OnConflictStrategy.REPLACE
import com.delivery.sopo.models.entity.ParcelEntity

@Dao
interface ParcelDao
{
    // 로컬에서 단일 택배 조회
    @Query("SELECT * FROM PARCEL WHERE TRACK_NUM = :waybilNum")
    suspend fun getSingleParcelWithWaybilNum(waybilNum: String): ParcelEntity?

    @Query("SELECT * FROM PARCEL as p LEFT JOIN PARCEL_MANAGEMENT as pm ON p.REG_DT = pm.REG_DT AND p.PARCEL_UID = pm.PARCEL_UID WHERE p.STATUS = 1 AND p.DELIVERY_STATUS = 'delivered' AND pm.isNowVisible = 1")
    fun getCompleteLiveData(): LiveData<List<ParcelEntity>>

    @Query("SELECT * FROM PARCEL WHERE STATUS = 1 AND DELIVERY_STATUS <> 'delivered'")
    fun getOngoingLiveData(): LiveData<List<ParcelEntity>>

    @Query("SELECT * FROM PARCEL WHERE STATUS = 1 AND DELIVERY_STATUS <> 'delivered'")
    fun getOngoingData(): List<ParcelEntity>?

    @Query("SELECT COUNT(*) FROM PARCEL WHERE STATUS = 1 AND DELIVERY_STATUS <> 'delivered'")
    suspend fun getOngoingDataCnt(): Int

    @Query("SELECT * FROM PARCEL as p LEFT JOIN PARCEL_MANAGEMENT as pm ON p.REG_DT = pm.REG_DT AND p.PARCEL_UID = pm.PARCEL_UID WHERE p.STATUS = 0 AND pm.isBeDelete = 1")
    suspend fun getBeDeletedData(): List<ParcelEntity>

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