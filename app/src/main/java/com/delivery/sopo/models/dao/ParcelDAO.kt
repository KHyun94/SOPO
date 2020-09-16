package com.delivery.sopo.models.dao

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.delivery.sopo.models.entity.CourierEntity
import com.delivery.sopo.models.entity.ParcelEntity
import com.delivery.sopo.models.parcel.Parcel

interface ParcelDAO
{
    @Query("SELECT * FROM PARCEL")
    fun getAll() : List<Parcel>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(parcel: Parcel)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(parcel: List<Parcel>)
}