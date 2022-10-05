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
import com.delivery.sopo.util.SopoLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.KoinComponent

@TypeConverters(Converters::class)
@Database(
    entities = [CarrierEntity::class, CarrierPatternEntity::class, ParcelEntity::class, ParcelStatusEntity::class, CompletedParcelHistoryEntity::class, AppPasswordEntity::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase(), KoinComponent
{
    abstract fun carrierDao(): CarrierDao
    abstract fun carrierPatternDao(): CarrierPatternDao
    abstract fun parcelDao(): ParcelDao
    abstract fun parcelStatusDAO(): ParcelStatusDao
    abstract fun completeParcelStatusDao(): CompleteParcelStatusDao
    abstract fun securityDao(): AppPasswordDao

//    val carrierDataSource: CarrierDataSource by inject()

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

                        CoroutineScope(Dispatchers.Default).launch {

                            SopoLog.d("DB 초기화 - 택배사")

//                            getInstance(context).carrierDataSource.initCarrierTable()
//                            getInstance(context).carrierDataSource.initCarrierPatternTable()
                        }
                    }
                }).build()
        }
    }
}