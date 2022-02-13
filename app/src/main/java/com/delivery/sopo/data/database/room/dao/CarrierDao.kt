package com.delivery.sopo.data.database.room.dao

import androidx.room.*
import androidx.room.OnConflictStrategy.REPLACE
import com.delivery.sopo.data.database.room.entity.CarrierEntity

@Dao
interface CarrierDao
{
    @Query("SELECT * FROM CARRIER")
    fun getAll() : List<CarrierEntity>

    @Query("SELECT COUNT(*) FROM CARRIER")
    fun getAllCnt() : Int

    @Query("SELECT * FROM CARRIER WHERE carrierName = :name")
    fun getWithName(name : String) : List<CarrierEntity>

    @Query("SELECT * FROM CARRIER WHERE carrierCode = :code")
    fun getWithCode(code : String) : CarrierEntity?

    @Query("SELECT * FROM CARRIER WHERE min <= :len AND max >= :len ORDER BY priority DESC LIMIT :cnt")
    fun getWithLen(len:Int, cnt : Int) : List<CarrierEntity?>

    @Query("SELECT * FROM CARRIER WHERE min <= :len AND max >= :len AND carrierName != :param1 ORDER BY priority DESC LIMIT :cnt")
    fun getWithLenAndCondition1(len:Int, param1:String, cnt : Int) : List<CarrierEntity?>

    @Query("SELECT * FROM CARRIER WHERE min <= :len AND max >= :len AND carrierName != :param1 AND carrierName != :param2 ORDER BY priority DESC LIMIT :cnt")
    fun getWithLenAndCondition2(len:Int, param1:String, param2:String, cnt : Int) : List<CarrierEntity?>

    @Query("SELECT * FROM CARRIER WHERE NOT( min <= :len AND max >= :len) ORDER BY priority DESC LIMIT :cnt")
    fun getWithoutLen(len:Int, cnt : Int) : List<CarrierEntity?>

    @Query("SELECT * FROM CARRIER WHERE NOT( min <= :len AND max >= :len) AND carrierName != :param1 ORDER BY priority DESC LIMIT :cnt")
    fun getWithoutLenAndCondition1(len:Int, param1:String, cnt : Int) : List<CarrierEntity?>

    @Query("SELECT * FROM CARRIER WHERE NOT( min <= :len AND max >= :len) AND carrierName != :param1 AND carrierName != :param2 ORDER BY priority DESC LIMIT :cnt")
    fun getWithoutLenAndCondition2(len:Int, param1:String, param2:String, cnt : Int) : List<CarrierEntity?>

    @Query("SELECT * FROM CARRIER WHERE carrierCode = :carrierCode")
    fun getCarrierEntityWithCode(carrierCode: String) : CarrierEntity

    @Query("SELECT * FROM CARRIER WHERE carrierName LIKE :name")
    fun getWithPartName(name : String) : List<CarrierEntity>

    @Insert(onConflict = REPLACE)
    fun insert(carrierEntity: CarrierEntity)

    @Insert(onConflict = REPLACE)
    fun insert(carrierList: List<CarrierEntity>)

    @Update
    fun update(carrierEntity: CarrierEntity)

    @Delete
    fun delete(carrierEntity: CarrierEntity)

}