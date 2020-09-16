package com.delivery.sopo.models.dao

import androidx.room.*
import androidx.room.OnConflictStrategy.REPLACE
import com.delivery.sopo.models.CourierItem
import com.delivery.sopo.models.entity.CourierEntity

@Dao
interface CourierDao
{
    @Query("SELECT * FROM COURIER")
    fun getAll() : List<CourierEntity>

    @Query("SELECT COUNT(*) FROM COURIER")
    fun getAllCnt() : Int

    @Query("SELECT * FROM COURIER WHERE courierName = :name")
    fun getWithName(name : String) : List<CourierEntity>

    @Query("SELECT courierName, courierCode, clickRes, nonClickRes, iconRes FROM COURIER WHERE minLen <= :len AND maxLen >= :len ORDER BY priority DESC LIMIT :cnt")
    fun getWithLen(len:Int, cnt : Int) : List<CourierItem>

    @Query("SELECT courierName, courierCode, clickRes, nonClickRes, iconRes FROM COURIER WHERE minLen <= :len AND maxLen >= :len AND courierName != :param1 ORDER BY priority DESC LIMIT :cnt")
    fun getWithLenAndCondition1(len:Int, param1:String, cnt : Int) : List<CourierItem>

    @Query("SELECT courierName, courierCode, clickRes, nonClickRes, iconRes FROM COURIER WHERE minLen <= :len AND maxLen >= :len AND courierName != :param1 AND courierName != :param2 ORDER BY priority DESC LIMIT :cnt")
    fun getWithLenAndCondition2(len:Int, param1:String, param2:String, cnt : Int) : List<CourierItem>

    @Insert(onConflict = REPLACE)
    fun insert(courierEntity: CourierEntity)

    @Insert(onConflict = REPLACE)
    fun insert(courierList: List<CourierEntity>)

    @Update
    fun update(courierEntity: CourierEntity)

    @Delete
    fun delete(courierEntity: CourierEntity)
}