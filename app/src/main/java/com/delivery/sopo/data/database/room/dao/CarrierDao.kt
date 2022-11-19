package com.delivery.sopo.data.database.room.dao

import androidx.room.*
import androidx.room.OnConflictStrategy.REPLACE
import com.delivery.sopo.data.database.room.entity.CarrierEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CarrierDao
{
    @Query("SELECT * FROM CARRIER")
    fun get() : List<CarrierEntity>

    @Query("SELECT * FROM CARRIER")
    fun getFlow() : Flow<List<CarrierEntity>>

    @Query("SELECT COUNT(*) FROM CARRIER")
    fun getAllCnt() : Int

    @Query("SELECT * FROM CARRIER WHERE name = :name")
    fun getWithName(name : String) : List<CarrierEntity>

    @Query("SELECT * FROM CARRIER WHERE code = :code")
    fun getWithCode(code : String) : CarrierEntity?

    @Query("SELECT * FROM CARRIER WHERE code = :code")
    fun getCarrierEntityWithCode(code: String) : CarrierEntity

    @Query("SELECT * FROM CARRIER WHERE name LIKE :name")
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