package com.delivery.sopo.data.repository.local.repository

import com.delivery.sopo.data.database.room.AppDatabase
import com.delivery.sopo.data.database.room.entity.CarrierEntity
import com.delivery.sopo.enums.CarrierEnum
import com.delivery.sopo.models.Carrier
import com.delivery.sopo.models.mapper.CarrierMapper
import com.delivery.sopo.util.SopoLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CarrierRepository(private val appDB: AppDatabase)
{
    suspend fun getAll(): List<Carrier?> = withContext(Dispatchers.Default) {
        return@withContext appDB.carrierDao().getAll().map(CarrierMapper::entityToObject).toList()
    }

    suspend fun getAllCnt(): Int = withContext(Dispatchers.Default) {
        return@withContext appDB.carrierDao().getAllCnt()
    }

//    suspend fun getWithLen(len: Int, cnt: Int): List<Carrier> =
//        withContext(Dispatchers.Default) {
//            return@withContext appDB.carrierDao()
//                .getWithLen(len = len, cnt = cnt)
//                .filterNotNull()
//                .map(CarrierMapper::entityToObject)
//        }

    suspend fun getCarrierWithCode(code: String): Carrier = withContext(Dispatchers.Default) {
        appDB.carrierDao().getWithCode(code = code)?.let { CarrierMapper.entityToObject(it) }
            ?: throw Exception("해당하는 택배사가 존재하지 않습니다.")
    }

//    suspend fun getWithoutLen(len: Int, cnt: Int): List<Carrier> =
//        withContext(Dispatchers.Default) {
//            return@withContext appDB.carrierDao()
//                .getWithoutLen(len = len, cnt = cnt)
//                .filterNotNull()
//                .map(CarrierMapper::entityToObject)
//        }


    suspend fun getCarrierEntityWithCode(code: String): Carrier = withContext(Dispatchers.Default)
    {
        return@withContext CarrierMapper.entityToObject(appDB.carrierDao().getCarrierEntityWithCode(code = code))
    }

    fun getCarrierEntityWithPartName(name: String): List<CarrierEntity?>
    {
        return appDB.carrierDao().getWithPartName(name)
    }

    fun insert(list: List<CarrierEntity>)
    {
        return appDB.carrierDao().insert(list)
    }

//    suspend fun recommendAutoCarrier(waybillNum: String, cnt: Int) = withContext(Dispatchers.Default)
//    {
//        SopoLog.d("recommend carrier >>> $waybillNum / $cnt 개")
//
//        mutableListOf<Carrier?>().apply {
//            addAll(getWithLen(waybillNum.length, cnt))
//            addAll(getWithoutLen(waybillNum.length, 10))
//        }
//    }
}