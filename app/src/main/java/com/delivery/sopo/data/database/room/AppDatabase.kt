package com.delivery.sopo.data.database.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.delivery.sopo.data.database.room.dao.*
import com.delivery.sopo.data.database.room.entity.*
import com.delivery.sopo.data.database.room.util.Converters
import com.delivery.sopo.data.database.room.dao.OAuthDao
import com.delivery.sopo.data.database.room.entity.OAuthEntity

@TypeConverters(Converters::class)
@Database(
    entities = [CarrierEntity::class, CarrierPatternEntity::class, ParcelEntity::class, ParcelStatusEntity::class, CompletedParcelHistoryEntity::class, AppPasswordEntity::class, OAuthEntity::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase()
{
    abstract fun carrierDao(): CarrierDao
    abstract fun carrierPatternDao(): CarrierPatternDao
    abstract fun parcelDao(): ParcelDao
    abstract fun parcelManagementDao(): ParcelStatusDAO
    abstract fun completeParcelStatusDao(): CompleteParcelStatusDao
    abstract fun securityDao(): AppPasswordDao
    abstract fun oauthDao() : OAuthDao

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