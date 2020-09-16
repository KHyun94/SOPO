package com.delivery.sopo.database.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.delivery.sopo.models.dao.CourierDAO
import com.delivery.sopo.models.entity.CourierEntity
import com.delivery.sopo.models.entity.ParcelEntity

@Database(entities = arrayOf(CourierEntity::class, ParcelEntity::class), version = 1)
abstract class AppDatabase : RoomDatabase()
{
    abstract fun courierDao():CourierDAO

    companion object
    {
        private val DB_NAME = "SOPO_INNER_DB.db"
        private val instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase
        {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context = context)
            }
        }

        private fun buildDatabase(context: Context): AppDatabase
        {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                DB_NAME
            )
                .addCallback(object : RoomDatabase.Callback()
                {
                    override fun onCreate(db: SupportSQLiteDatabase)
                    {
                        super.onCreate(db)
                    }
                }).build()
        }
    }
}