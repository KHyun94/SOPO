package com.delivery.sopo.data.repository.database.room.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.room.OnConflictStrategy.REPLACE
import com.delivery.sopo.consts.StatusConst
import com.delivery.sopo.data.repository.database.room.entity.ParcelStatusEntity

@Dao
interface ParcelStatusDAO
{
    @Query("SELECT * FROM PARCEL_STATUS")
    suspend fun getAll() : List<ParcelStatusEntity>?

    @Query("SELECT COUNT(*) FROM PARCEL_STATUS WHERE isBeDelete = 1")
    fun getIsDeleteCntLiveData(): LiveData<Int>

    @Query("SELECT COUNT(*) FROM PARCEL_STATUS WHERE updatableStatus = 1")
    fun getIsUpdateCntLiveData(): LiveData<Int>

    @Query("SELECT COUNT(*) FROM PARCEL_STATUS WHERE deliveredStatus = 1")
    fun getIsDeliveredCntLiveData(): LiveData<Int>

    @Query("SELECT COUNT(*) FROM PARCEL_STATUS WHERE isBeDelete = 1")
    fun getIsDeleteCnt(): Int

    @Query("SELECT COUNT(*) FROM PARCEL_STATUS WHERE updatableStatus = ${StatusConst.ACTIVATE}")
    fun getCountForUpdatableParcel(): Int

    @Query("SELECT unidentifiedStatus FROM PARCEL_STATUS WHERE REG_DT = :regDt AND PARCEL_UID = :parcelUid")
    fun getUnidentifiedStatusByParcelId(regDt: String, parcelUid: String) : Int

    @Query("UPDATE PARCEL_STATUS SET unidentifiedStatus = :value WHERE REG_DT = :regDt AND PARCEL_UID = :parcelUid")
    fun updateIsUnidentified(regDt: String, parcelUid: String, value : Int) : Int

    @Query("SELECT COUNT(*) FROM PARCEL_STATUS WHERE deliveredStatus = 1")
    fun getIsDeliveredCnt(): Int

    @Query("SELECT * FROM PARCEL_STATUS WHERE REG_DT = :regDt AND PARCEL_UID = :parcelUid")
    fun getById(regDt: String, parcelUid: String): ParcelStatusEntity?

    @Query("SELECT * FROM PARCEL_STATUS WHERE isBeDelete = 1 AND AUDIT_DTE >= DATETIME('now', 'localtime', '-10.0 seconds')")
    suspend fun getCancelIsBeDelete() : List<ParcelStatusEntity>?

    @Query("UPDATE PARCEL_STATUS SET deliveredStatus = 0")
    fun updateTotalIsBeDeliveredToZero()

    @Query("UPDATE PARCEL_STATUS SET updatableStatus = :status WHERE REG_DT = :regDt AND PARCEL_UID = :parcelUid")
    fun updateIsBeUpdate(regDt: String, parcelUid: String, status : Int? = 0)

    @Query("UPDATE PARCEL_STATUS SET isBeDelete = 1 , AUDIT_DTE = :auditDte WHERE REG_DT = :regDt AND PARCEL_UID = :parcelUid")
    fun updateIsBeDeleteToOne(regDt: String, parcelUid: String, auditDte: String)

    @Insert(onConflict = REPLACE)
    fun insert(parcelStatusEntity: ParcelStatusEntity)

    @Insert(onConflict = REPLACE)
    fun insert(parcelStatusEntityList: List<ParcelStatusEntity>)

    @Delete
    fun delete(parcelStatusEntity: ParcelStatusEntity)

    @Update
    fun update(parcelStatusEntity: ParcelStatusEntity)

    @Update
    fun update(parcelStatusEntityList: List<ParcelStatusEntity>)
}