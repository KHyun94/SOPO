package com.delivery.sopo.data.repository.local.repository

import com.delivery.sopo.data.database.room.AppDatabase
import com.delivery.sopo.data.database.room.dto.CarrierPattern
import com.delivery.sopo.data.database.room.entity.CarrierEntity
import com.delivery.sopo.data.database.room.entity.CarrierPatternEntity
import com.delivery.sopo.enums.CarrierEnum
import com.delivery.sopo.models.Carrier
import com.delivery.sopo.models.mapper.CarrierMapper
import com.delivery.sopo.util.SopoLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CarrierRepository(private val appDB: AppDatabase)
{
    suspend fun initCarrierDB() = withContext(Dispatchers.Default) {

        if(getAllCnt() > 0) return@withContext

        val list = CarrierEnum.values().map { carrier ->
            CarrierEntity(carrierNo = carrier.NO, name = carrier.NAME, code = carrier.CODE)
        }

        insert(list)

        initCarrierPatternDB()
    }

    suspend fun initCarrierPatternDB() = withContext(Dispatchers.Default) {

        val list = arrayOf (
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

        appDB.carrierPatternDao().insert(*list)
    }

    suspend fun recommendCarrier(waybillNum: String):CarrierEnum?{

        val len: Int = waybillNum.length
        val header: String = waybillNum.substring(0, 4)

        val carriers = withContext(Dispatchers.Default){
            appDB.carrierPatternDao().getByLength(length = len)
        }.sortedByDescending { it.header }

        if(carriers.isEmpty())
        {
            return null
        }

        val recommendCarrier = selectSpecificCarrier(header, carriers) ?: CarrierEnum.getCarrierByCode(carriers.first().code)

        SopoLog.d("추천 택배사 ${recommendCarrier.toString()}")

        return recommendCarrier
    }

    suspend fun getCarriersByLength(){

    }

    fun selectSpecificCarrier(inputHeader: String, compareHeader: List<CarrierPattern>): CarrierEnum?
    {
        compareHeader.forEach {
            val isInclude = inputHeader.startsWith(it.header)
            if(isInclude) return CarrierEnum.getCarrierByCode(it.code)
        }

        return null
    }

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