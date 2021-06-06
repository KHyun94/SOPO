package com.delivery.sopo.data.repository.database.room.dao

import androidx.room.*
import androidx.room.OnConflictStrategy.REPLACE
import com.delivery.sopo.models.CarrierDTO
import com.delivery.sopo.data.repository.database.room.entity.CarrierEntity
import org.jetbrains.annotations.NotNull

@Dao
interface CarrierDAO
{
    @Query("SELECT * FROM CARRIER")
    fun getAll() : List<CarrierEntity>

    @Query("SELECT COUNT(*) FROM CARRIER")
    suspend fun getAllCnt() : Int

    @Query("SELECT * FROM CARRIER WHERE carrierName = :name")
    fun getWithName(name : String) : List<CarrierEntity>

    @Query("SELECT * FROM CARRIER WHERE carrierCode = :code")
    suspend fun getWithCode(code : String) : CarrierEntity?

    @Query("SELECT * FROM CARRIER WHERE min <= :len AND max >= :len ORDER BY priority DESC LIMIT :cnt")
    suspend fun getWithLen(len:Int, cnt : Int) : List<CarrierEntity?>

    @Query("SELECT * FROM CARRIER WHERE min <= :len AND max >= :len AND carrierName != :param1 ORDER BY priority DESC LIMIT :cnt")
    suspend fun getWithLenAndCondition1(len:Int, param1:String, cnt : Int) : List<CarrierEntity?>

    @Query("SELECT * FROM CARRIER WHERE min <= :len AND max >= :len AND carrierName != :param1 AND carrierName != :param2 ORDER BY priority DESC LIMIT :cnt")
    suspend fun getWithLenAndCondition2(len:Int, param1:String, param2:String, cnt : Int) : List<CarrierEntity?>

    @Query("SELECT * FROM CARRIER WHERE NOT( min <= :len AND max >= :len) ORDER BY priority DESC LIMIT :cnt")
    suspend fun getWithoutLen(len:Int, cnt : Int) : List<CarrierEntity?>

    @Query("SELECT * FROM CARRIER WHERE NOT( min <= :len AND max >= :len) AND carrierName != :param1 ORDER BY priority DESC LIMIT :cnt")
    suspend fun getWithoutLenAndCondition1(len:Int, param1:String, cnt : Int) : List<CarrierEntity?>

    @Query("SELECT * FROM CARRIER WHERE NOT( min <= :len AND max >= :len) AND carrierName != :param1 AND carrierName != :param2 ORDER BY priority DESC LIMIT :cnt")
    suspend fun getWithoutLenAndCondition2(len:Int, param1:String, param2:String, cnt : Int) : List<CarrierEntity?>

    @Query("SELECT * FROM CARRIER WHERE carrierCode = :carrierCode")
    suspend fun getCarrierEntityWithCode(carrierCode: String) : CarrierEntity

    @Query("SELECT * FROM CARRIER WHERE carrierName LIKE :name")
    fun getWithPartName(name : String) : List<CarrierEntity>

    @Insert(onConflict = REPLACE)
    suspend fun insert(carrierEntity: CarrierEntity)

    @Insert(onConflict = REPLACE)
    fun insert(carrierList: List<CarrierEntity>)

    @Update
    fun update(carrierEntity: CarrierEntity)

    @Delete
    fun delete(carrierEntity: CarrierEntity)
}