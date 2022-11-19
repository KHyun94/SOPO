package com.delivery.sopo.data.repositories.local.repository

import com.delivery.sopo.data.database.room.dao.CarrierDao
import com.delivery.sopo.data.database.room.dao.CarrierPatternDao
import com.delivery.sopo.data.database.room.entity.CarrierEntity
import com.delivery.sopo.data.database.room.entity.CarrierPatternEntity
import com.delivery.sopo.data.models.Carrier
import com.delivery.sopo.data.models.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CarrierDataSource @Inject constructor(private val carrierDao: CarrierDao, private val carrierPatternDao: CarrierPatternDao)
{
//    suspend fun initCarrierTable() = withContext(Dispatchers.Default) {
//
//        if(getAllCnt() > 0) return@withContext
//
//        val carriers = Carrier.values().map { carrier ->
//            CarrierEntity(no = carrier.NO, name = carrier.NAME, code = carrier.CODE)
//        }.toTypedArray()
//
//        insert(*carriers)
//    }
//
//    suspend fun initCarrierPatternTable() = withContext(Dispatchers.Default) {
//
//        val carrierPatterns = arrayOf (
//            CarrierPatternEntity(0, Carrier.CHUNILPS.NO, 11, "1"),
//            CarrierPatternEntity(0, Carrier.LOGEN.NO, 11, "3"),
//            CarrierPatternEntity(0, Carrier.LOGEN.NO, 11, "9"),
//            CarrierPatternEntity(0, Carrier.LOTTE.NO, 12, "2"),
//            CarrierPatternEntity(0, Carrier.LOTTE.NO, 12, "40"),
//            CarrierPatternEntity(0, Carrier.CJ_LOGISTICS.NO, 12, "35"),
//            CarrierPatternEntity(0, Carrier.CJ_LOGISTICS.NO, 12, "36"),
//            CarrierPatternEntity(0, Carrier.CJ_LOGISTICS.NO, 12, "38"),
//            CarrierPatternEntity(0, Carrier.CJ_LOGISTICS.NO, 12, "55"),
//            CarrierPatternEntity(0, Carrier.CJ_LOGISTICS.NO, 12, "6"),
//            CarrierPatternEntity(0, Carrier.CVSNET.NO, 12, "363"),
//            CarrierPatternEntity(0, Carrier.HANJINS.NO, 12, "42"),
//            CarrierPatternEntity(0, Carrier.HANJINS.NO, 12, "51"),
//            CarrierPatternEntity(0, Carrier.KDEXP.NO, 12, "9"),
//            CarrierPatternEntity(0, Carrier.EPOST.NO, 13, "6"),
//            CarrierPatternEntity(0, Carrier.KDEXP.NO, 13, "31"),
//            CarrierPatternEntity(0, Carrier.KDEXP.NO, 13, "4"))
//
//        insert(*carrierPatterns)
//    }
//
//    suspend fun recommendCarrier(waybillNum: String): Carrier?
//    {
//        val header: String = waybillNum.substring(0, 4)
//
//        val carriers = getByLength(waybillNum = waybillNum).sortedByDescending { it.header }
//
//        if(carriers.isEmpty()) return null
//
//        return selectSpecificCarrier(header, carriers) ?: Carrier.getCarrierByCode(carriers.first().code)
//    }

//    fun selectSpecificCarrier(inputHeader: String, compareHeader: List<CarrierPattern>): Carrier?
//    {
//        compareHeader.forEach {
//            val isInclude = inputHeader.startsWith(it.header)
//            if(isInclude) return Carrier.getCarrierByCode(it.code)
//        }
//
//        return null
//    }

   /* suspend fun getByLength(waybillNum: String): List<CarrierPattern> = withContext(Dispatchers.Default)
    {
        return@withContext carrierPatternDao.getByLength(length = waybillNum.length)
    }*/

    suspend fun getAllCarriers() = flow {
        emit(Result.Loading)
        carrierDao.getFlow().collect { entities ->
            if(entities.isEmpty()) return@collect emit(Result.Empty)
            val infoList = entities.map { Carrier.Info(it.code, it.name, it.available) }
            emit(Result.Success(infoList))
        }
    }.catch { e -> emit(Result.Error(e)) }

    suspend fun getAll(): List<Carrier.Info> = withContext(Dispatchers.Default) {
        return@withContext carrierDao.get().map {
            Carrier.Info(it.code, it.name, it.available)
        }
    }

    suspend fun getAllCnt(): Int = withContext(Dispatchers.Default) {
        return@withContext carrierDao.getAllCnt()
    }

    suspend fun getByCode(code: String): CarrierEntity = withContext(Dispatchers.Default) {
        carrierDao.getWithCode(code = code) ?: throw Exception("해당하는 택배사가 존재하지 않습니다.")
    }

    fun getCarrierEntityWithPartName(name: String): List<CarrierEntity?>
    {
        return carrierDao.getWithPartName(name)
    }

    fun insert(vararg carrier: CarrierEntity)
    {
        return carrierDao.insert(carrier.toList())
    }

    fun insert(vararg carrierPattern: CarrierPatternEntity)
    {
        return carrierPatternDao.insert(*carrierPattern)
    }
}