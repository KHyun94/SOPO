package com.delivery.sopo.data.database.room.dao

import androidx.room.*
import androidx.room.OnConflictStrategy.REPLACE
import com.delivery.sopo.data.database.room.dto.CarrierPattern
import com.delivery.sopo.data.database.room.entity.CarrierEntity
import com.delivery.sopo.data.database.room.entity.CarrierPatternEntity

@Dao
interface CarrierPatternDao
{
    @Query("SELECT * FROM CARRIER_PATTERN")
    fun get() : List<CarrierPatternEntity>

    @Insert(onConflict = REPLACE)
    fun insert(vararg carrierPatternEntity: CarrierPatternEntity)

    @Update
    fun update(carrierPatternEntity: CarrierPatternEntity)

    @Delete
    fun delete(carrierPatternEntity: CarrierPatternEntity)

  /*  @Query("SELECT c.code, cp.length, cp.header FROM CARRIER as c LEFT JOIN CARRIER_PATTERN as cp ON c.`no` = cp.carrierNo WHERE cp.length = :length")
    fun getByLength(length: Int) : List<CarrierPattern>
*/
//    @Query("SELECT c.code, cp.length, cp.header, cp.priority FROM CARRIER as c LEFT JOIN CARRIER_PATTERN as cp ON c.carrierNo = cp.carrierNo order by cp.priority DESC LIMIT :cnt")
//    fun getOrderByPriority(cnt: Int) : List<CarrierPattern>
}