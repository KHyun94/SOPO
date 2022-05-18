package com.delivery.sopo.data.repositories.local.repository

import com.delivery.sopo.data.database.room.AppDatabase
import com.delivery.sopo.data.database.room.dto.CarrierPattern
import com.delivery.sopo.data.database.room.entity.CarrierEntity
import com.delivery.sopo.data.database.room.entity.CarrierPatternEntity
import com.delivery.sopo.enums.CarrierEnum
import com.delivery.sopo.models.Carrier
import com.delivery.sopo.models.mapper.CarrierMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CarrierRepository(private val appDB: AppDatabase)
{
    suspend fun initCarrierDB() = withContext(Dispatchers.Default) {

        if(getAllCnt() > 0) return@withContext

        val carriers = CarrierEnum.values().map { carrier ->
            CarrierEntity(carrierNo = carrier.NO, name = carrier.NAME, code = carrier.CODE)
        }.toTypedArray()

        insert(*carriers)

        initCarrierPatternDB()
    }

    suspend fun initCarrierPatternDB() = withContext(Dispatchers.Default) {

        val carrierPatterns = arrayOf (
            CarrierPatternEntity(0, CarrierEnum.CHUNILPS.NO, 11, "1"),
            CarrierPatternEntity(0, CarrierEnum.LOGEN.NO, 11, "3"),
            CarrierPatternEntity(0, CarrierEnum.LOGEN.NO, 11, "9"),
            CarrierPatternEntity(0, CarrierEnum.LOTTE.NO, 12, "2"),
            CarrierPatternEntity(0, CarrierEnum.LOTTE.NO, 12, "40"),
            CarrierPatternEntity(0, CarrierEnum.CJ_LOGISTICS.NO, 12, "35"),
            CarrierPatternEntity(0, CarrierEnum.CJ_LOGISTICS.NO, 12, "36"),
            CarrierPatternEntity(0, CarrierEnum.CJ_LOGISTICS.NO, 12, "38"),
            CarrierPatternEntity(0, CarrierEnum.CJ_LOGISTICS.NO, 12, "55"),
            CarrierPatternEntity(0, CarrierEnum.CJ_LOGISTICS.NO, 12, "6"),
            CarrierPatternEntity(0, CarrierEnum.CVSNET.NO, 12, "363"),
            CarrierPatternEntity(0, CarrierEnum.HANJINS.NO, 12, "42"),
            CarrierPatternEntity(0, CarrierEnum.HANJINS.NO, 12, "51"),
            CarrierPatternEntity(0, CarrierEnum.KDEXP.NO, 12, "9"),
            CarrierPatternEntity(0, CarrierEnum.EPOST.NO, 13, "6"),
            CarrierPatternEntity(0, CarrierEnum.KDEXP.NO, 13, "31"),
            CarrierPatternEntity(0, CarrierEnum.KDEXP.NO, 13, "4"))

        insert(*carrierPatterns)
    }

    suspend fun recommendCarrier(waybillNum: String): CarrierEnum?
    {
        val header: String = waybillNum.substring(0, 4)

        val carriers = getByLength(waybillNum = waybillNum).sortedByDescending { it.header }

        if(carriers.isEmpty()) return null

        return selectSpecificCarrier(header, carriers) ?: CarrierEnum.getCarrierByCode(carriers.first().code)
    }

    fun selectSpecificCarrier(inputHeader: String, compareHeader: List<CarrierPattern>): CarrierEnum?
    {
        compareHeader.forEach {
            val isInclude = inputHeader.startsWith(it.header)
            if(isInclude) return CarrierEnum.getCarrierByCode(it.code)
        }

        return null
    }

    suspend fun getByLength(waybillNum: String): List<CarrierPattern> = withContext(Dispatchers.Default)
    {
        return@withContext appDB.carrierPatternDao().getByLength(length = waybillNum.length)
    }

    suspend fun getAll(): List<Carrier?> = withContext(Dispatchers.Default) {
        return@withContext appDB.carrierDao().getAll().map(CarrierMapper::entityToObject).toList()
    }

    suspend fun getAllCnt(): Int = withContext(Dispatchers.Default) {
        return@withContext appDB.carrierDao().getAllCnt()
    }

    suspend fun getByCode(code: String): Carrier = withContext(Dispatchers.Default) {
        appDB.carrierDao().getWithCode(code = code)?.let { CarrierMapper.entityToObject(it) }
            ?: throw Exception("해당하는 택배사가 존재하지 않습니다.")
    }

    fun getCarrierEntityWithPartName(name: String): List<CarrierEntity?>
    {
        return appDB.carrierDao().getWithPartName(name)
    }

    fun insert(vararg carrier: CarrierEntity)
    {
        return appDB.carrierDao().insert(carrier.toList())
    }

    fun insert(vararg carrierPattern: CarrierPatternEntity)
    {
        return appDB.carrierPatternDao().insert(*carrierPattern)
    }
}