package com.delivery.sopo.data.repository.database.room.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.room.OnConflictStrategy.REPLACE
import com.delivery.sopo.data.repository.database.room.entity.AppPasswordEntity

@Dao
interface AppPasswordDao
{
    @Query("SELECT * FROM APP_PASSWORD")
    fun getAppPassword(): AppPasswordEntity?

    @Query("SELECT COUNT(*) FROM APP_PASSWORD")
    fun getCntOfAppPasswordLiveData(): LiveData<Int>

    @Insert(onConflict = REPLACE)
    fun insert(entity: AppPasswordEntity)

    @Delete
    fun delete(entity: AppPasswordEntity)

    @Query("DELETE FROM APP_PASSWORD")
    fun deleteAll()

    @Update
    fun update(entity: AppPasswordEntity)
}