package com.delivery.sopo.data.repository.database.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.delivery.sopo.data.repository.database.room.dao.*
import com.delivery.sopo.data.repository.database.room.entity.*
import com.delivery.sopo.data.repository.database.room.util.Converters
import com.delivery.sopo.data.repository.local.o_auth.OAuthDAO
import com.delivery.sopo.data.repository.local.o_auth.OAuthEntity

@TypeConverters(Converters::class)
@Database(
    entities = [CarrierEntity::class, ParcelEntity::class, ParcelStatusEntity::class, CompletedParcelHistoryEntity::class, AppPasswordEntity::class, WorkEntity::class, LogEntity::class, OAuthEntity::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase()
{
    abstract fun carrierDAO(): CarrierDAO
    abstract fun parcelDao(): ParcelDao
    abstract fun parcelManagementDao(): ParcelStatusDAO
    abstract fun completeParcelStatusDao(): CompleteParcelStatusDao
    abstract fun securityDao(): AppPasswordDao
    abstract fun workDao(): WorkDao
    abstract fun logDao(): LogDao
    abstract fun oauthDao() : OAuthDAO

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