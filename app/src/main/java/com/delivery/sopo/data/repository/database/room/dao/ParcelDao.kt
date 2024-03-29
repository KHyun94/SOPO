package com.delivery.sopo.data.repository.database.room.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.room.OnConflictStrategy.REPLACE
import com.delivery.sopo.data.repository.database.room.entity.ParcelEntity

@Dao
interface ParcelDao
{
    // 로컬에서 단일 택배 조회
    @Query("SELECT * FROM PARCEL WHERE WAYBILL_NUM = :waybillNum")
    suspend fun getSingleParcelWithwaybillNum(waybillNum: String): ParcelEntity?

    @Query("SELECT * FROM PARCEL as p LEFT JOIN PARCEL_STATUS as pm ON p.PARCEL_ID = pm.PARCEL_ID WHERE p.STATUS = 1 AND p.DELIVERY_STATUS = 'delivered' AND pm.isNowVisible = 1")
    fun getCompleteLiveData(): LiveData<List<ParcelEntity>>

    @Query("SELECT * FROM PARCEL as p LEFT JOIN PARCEL_STATUS as pm ON p.PARCEL_ID = pm.PARCEL_ID WHERE p.STATUS = 1 AND p.DELIVERY_STATUS = 'delivered' AND pm.isNowVisible = 1")
    fun getComplete(): List<ParcelEntity>

    @Query("SELECT * FROM PARCEL WHERE STATUS = 1 AND DELIVERY_STATUS <> 'delivered'")
    fun getOngoingLiveData(): LiveData<List<ParcelEntity>>

    @Query("SELECT * FROM PARCEL WHERE STATUS = 1 AND DELIVERY_STATUS <> 'delivered'")
    fun getOngoingData(): List<ParcelEntity>

    @Query("SELECT COUNT(*) FROM PARCEL WHERE STATUS = 1 AND DELIVERY_STATUS <> 'delivered'")
    suspend fun getOngoingDataCnt(): Int

    @Query("SELECT COUNT(*) FROM PARCEL WHERE STATUS = 1 AND DELIVERY_STATUS = 'out_for_delivery'")
    fun getSoonDataCntLiveData(): LiveData<Int>

    @Query("SELECT COUNT(*) FROM PARCEL WHERE STATUS = 1 AND DELIVERY_STATUS <> 'delivered'")
    fun getOngoingDataCntLiveData(): LiveData<Int>

    @Query("SELECT * FROM PARCEL as p LEFT JOIN PARCEL_STATUS as pm ON p.PARCEL_ID = pm.PARCEL_ID WHERE p.STATUS = 0 AND pm.isBeDelete = 1")
    suspend fun getBeDeletedData(): List<ParcelEntity>

    @Query("SELECT * FROM PARCEL WHERE PARCEL_ID = :parcelId")
    suspend fun getById(parcelId: Int): ParcelEntity?

//    // 업데이트 가능한 택배의 InquiryHash 값을 SELECT
//    @Query("SELECT p.* FROM PARCEL as p INNER JOIN PARCEL_STATUS as pm where p.PARCEL_ID = pm.PARCEL_ID AND p.STATUS = 1  AND p.DELIVERY_STATUS <> 'delivered'")
//    fun getUpdatableInquiryHash(): List<ParcelEntity?>

    @Query("SELECT pm.unidentifiedStatus FROM PARCEL as p INNER JOIN PARCEL_STATUS as pm where p.PARCEL_ID = pm.PARCEL_ID AND p.PARCEL_ID = :parcelId AND p.DELIVERY_STATUS != 'delivered'")
    fun getIsUnidentifiedLiveData(parcelId: Int): LiveData<Int?>

    @Query("SELECT pm.updatableStatus FROM PARCEL as p INNER JOIN PARCEL_STATUS as pm where p.PARCEL_ID = pm.PARCEL_ID AND p.PARCEL_ID = :parcelId AND p.DELIVERY_STATUS != 'delivered'")
    fun isBeingUpdateParcel(parcelId: Int): LiveData<Int?>

    @Insert(onConflict = REPLACE)
    fun insert(parcelEntity: ParcelEntity)

    @Insert(onConflict = REPLACE)
    fun insert(listParcelEntity: List<ParcelEntity>)

    @Delete
    fun delete(parcelEntity: ParcelEntity)

    @Delete
    fun delete(listParcelEntity: List<ParcelEntity>)

    @Update
    fun update(parcelEntity: ParcelEntity): Int

    @Update
    fun update(listParcelEntity: List<ParcelEntity>)
}