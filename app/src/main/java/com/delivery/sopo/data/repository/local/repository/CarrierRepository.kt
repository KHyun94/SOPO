package com.delivery.sopo.data.repository.local.repository

import com.delivery.sopo.data.database.room.AppDatabase
import com.delivery.sopo.data.database.room.entity.CarrierEntity
import com.delivery.sopo.enums.CarrierEnum
import com.delivery.sopo.models.Carrier
import com.delivery.sopo.models.mapper.CarrierMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CarrierRepository(private val appDB: AppDatabase)
{
    fun recommendCarrier(waybillNum: String){

        val len: Int = waybillNum.length
        val header: String = waybillNum.substring(0, 4)

        val compareHeaders = when(len)
        {
            11 ->
            {
                listOf(
                    Pair(CarrierEnum.CHUNILPS, "1"),
                    Pair(CarrierEnum.LOGEN, "3"),
                    Pair(CarrierEnum.LOGEN, "9")
                )
            }
            12 ->
            {
                listOf(
                    Pair(CarrierEnum.LOTTE, "2"),
                    Pair(CarrierEnum.LOTTE, "40"),
                    Pair(CarrierEnum.CJ_LOGISTICS, "35"),
                    Pair(CarrierEnum.CJ_LOGISTICS, "36"),
                    Pair(CarrierEnum.CJ_LOGISTICS, "38"),
                    Pair(CarrierEnum.CJ_LOGISTICS, "55"),
                    Pair(CarrierEnum.CJ_LOGISTICS, "6"),
                    Pair(CarrierEnum.CVSNET, "363"),
                    Pair(CarrierEnum.HANJINS, "42"),
                    Pair(CarrierEnum.HANJINS, "51"),
                    Pair(CarrierEnum.KDEXP, "9"),
                )
            }
            13 ->
            {
                listOf(
                    Pair(CarrierEnum.EPOST, "6"),
                    Pair(CarrierEnum.KDEXP, "4"),
                    Pair(CarrierEnum.KDEXP, "31")
                )
            }
            else ->
            {
                throw NullPointerException()
            }
        }

        val recommendCarrier = selectSpecificCarrier(header, compareHeaders)

    }

    fun selectSpecificCarrier(inputHeader: String, compareHeader: List<Pair<CarrierEnum, String>>): CarrierEnum?
    {
        compareHeader.forEach {
            val isInclude = inputHeader.startsWith(it.second)
            if(isInclude) return it.first
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