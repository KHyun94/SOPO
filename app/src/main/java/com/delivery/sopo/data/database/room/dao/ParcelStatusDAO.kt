package com.delivery.sopo.data.database.room.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.room.OnConflictStrategy.REPLACE
import com.delivery.sopo.consts.StatusConst
import com.delivery.sopo.data.database.room.entity.ParcelStatusEntity

@Dao
interface ParcelStatusDAO
{
    @Query("SELECT * FROM PARCEL_STATUS")
    fun getAll() : List<ParcelStatusEntity>?

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

    @Query("SELECT PARCEL_STATUS.PARCEL_ID FROM PARCEL_STATUS WHERE updatableStatus = ${StatusConst.ACTIVATE}")
    fun getUpdatableParcelIds(): LiveData<List<Int>>

    @Query("SELECT unidentifiedStatus FROM PARCEL_STATUS WHERE PARCEL_ID = :parcelId")
    fun getUnidentifiedStatusByParcelId(parcelId: Int) : Int

    @Query("UPDATE PARCEL_STATUS SET unidentifiedStatus = :value WHERE PARCEL_ID = :parcelId")
    fun updateIsUnidentified(parcelId:Int, value : Int) : Int

    @Query("SELECT COUNT(*) FROM PARCEL_STATUS WHERE deliveredStatus = 1")
    fun getIsDeliveredCnt(): Int

    @Query("SELECT * FROM PARCEL_STATUS WHERE PARCEL_ID = :parcelId")
    fun getById(parcelId: Int): ParcelStatusEntity?

    @Query("SELECT * FROM PARCEL_STATUS WHERE isBeDelete = 1 AND AUDIT_DTE >= DATETIME('now', 'localtime', '-10.0 seconds')")
    fun getCancelIsBeDelete() : List<ParcelStatusEntity>?

    @Query("SELECT * FROM PARCEL_STATUS WHERE isBeDelete = 1")
    fun getDeletableParcelStatuses(): List<ParcelStatusEntity>


    @Query("UPDATE PARCEL_STATUS SET deliveredStatus = 0")
    fun updateTotalIsBeDeliveredToZero()

    @Query("UPDATE PARCEL_STATUS SET updatableStatus = :status WHERE  PARCEL_ID = :parcelId")
    fun updateIsBeUpdate(parcelId: Int, status : Int)

    @Query("UPDATE PARCEL_STATUS SET isBeDelete = 1 , AUDIT_DTE = :auditDte WHERE  PARCEL_ID = :parcelId")
    fun updateIsBeDeleteToOne(parcelId: Int, auditDte: String)

    @Insert(onConflict = REPLACE)
    fun insert(parcelStatusEntity: ParcelStatusEntity)

    @Insert(onConflict = REPLACE)
    fun insert(parcelStatusEntityList: List<ParcelStatusEntity>)

    @Delete
    fun delete(parcelStatusEntity: ParcelStatusEntity)

    @Delete
    fun delete(parcelStatusEntities: List<ParcelStatusEntity>)

    @Update
    fun update(parcelStatusEntity: ParcelStatusEntity)

    @Update
    fun update(parcelStatusEntityList: List<ParcelStatusEntity>)
}