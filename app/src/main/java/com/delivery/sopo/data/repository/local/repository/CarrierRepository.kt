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

    suspend fun getWithLen(len: Int, cnt: Int): List<Carrier> =
        withContext(Dispatchers.Default) {
            return@withContext appDB.carrierDao()
                .getWithLen(len = len, cnt = cnt)
                .filterNotNull()
                .map(CarrierMapper::entityToObject)
        }

    suspend fun getCarrierWithCode(code: String): Carrier = withContext(Dispatchers.Default) {
        appDB.carrierDao().getWithCode(code = code)?.let { CarrierMapper.entityToObject(it) }
            ?: throw Exception("해당하는 택배사가 존재하지 않습니다.")
    }

    suspend fun getWithoutLen(len: Int, cnt: Int): List<Carrier> =
        withContext(Dispatchers.Default) {
            return@withContext appDB.carrierDao()
                .getWithoutLen(len = len, cnt = cnt)
                .filterNotNull()
                .map(CarrierMapper::entityToObject)
        }


    suspend fun getCarrierEntityWithCode(carrierCode: String): Carrier = withContext(Dispatchers.Default)
    {
        return@withContext CarrierMapper.entityToObject(appDB.carrierDao().getCarrierEntityWithCode(carrierCode = carrierCode))
    }

    fun getCarrierEntityWithPartName(name: String): List<CarrierEntity?>
    {
        return appDB.carrierDao().getWithPartName(name)
    }

    fun insert(list: List<CarrierEntity>)
    {
        return appDB.carrierDao().insert(list)
    }

    suspend fun initializeCarrierInfoIntoDB() = withContext(Dispatchers.Default) {

        if(getAllCnt() > 0) return@withContext

        val carrierList = listOf<CarrierEntity>(

            CarrierEntity(carrierNo = 0, carrierName = CarrierEnum.CHUNILPS.NAME, carrierCode = CarrierEnum.CHUNILPS.CODE,
                          min = 11, max =11, priority = 0.88),
            CarrierEntity(carrierNo = 0, carrierName = CarrierEnum.CJ_LOGISTICS.NAME,
                          carrierCode = CarrierEnum.CJ_LOGISTICS.CODE, min = 13, max = 13, priority = 1.0),
            CarrierEntity(carrierNo = 0, carrierName = CarrierEnum.CU_POST.NAME, carrierCode = CarrierEnum.CU_POST.CODE,
                          min = 10, max =12, priority = 0.95),
            CarrierEntity(carrierNo = 0, carrierName = CarrierEnum.CVSNET.NAME, carrierCode = CarrierEnum.CVSNET.CODE,
                          min = 0, max = 0, priority = 0.90),
            CarrierEntity(carrierNo = 0, carrierName = CarrierEnum.DAESIN.NAME, carrierCode = CarrierEnum.DAESIN.CODE,
                          min = 13, max =13, priority = 0.92),
            CarrierEntity(carrierNo = 0, carrierName = CarrierEnum.EPOST.NAME, carrierCode = CarrierEnum.EPOST.CODE,
                          min = 13, max =13, priority = 0.98),

            CarrierEntity(carrierNo = 0, carrierName = CarrierEnum.HDEXP.NAME, carrierCode = CarrierEnum.HDEXP.CODE,
                          min = 9, max = 16, priority = 0.89),



            CarrierEntity(carrierNo = 0, carrierName = CarrierEnum.LOGEN.NAME, carrierCode = CarrierEnum.LOGEN.CODE,
                          min = 11, max = 11, priority = 0.97),
            CarrierEntity(carrierNo = 0, carrierName = CarrierEnum.HANJINS.NAME, carrierCode = CarrierEnum.HANJINS.CODE,
                          min = 10, max = 12, priority = 0.96),
            CarrierEntity(carrierNo = 0, carrierName = CarrierEnum.KDEXP.NAME, carrierCode = CarrierEnum.KDEXP.CODE,
                          min = 9, max = 16, priority = 0.93),
            CarrierEntity(carrierNo = 0, carrierName = CarrierEnum.LOTTE.NAME, carrierCode = CarrierEnum.LOTTE.CODE,
                          min = 12, max = 12, priority = 0.99)

            /*CarrierEntity(carrierNo = 0, carrierName = CarrierEnum.DHL.NAME, carrierCode = CarrierEnum.DHL.CODE,
                          min = 10, max = 10, priority = 0.91),




            CarrierEntity(carrierNo = 0, carrierName = CarrierEnum.ILYANGLOGIS.NAME,
                          carrierCode = CarrierEnum.ILYANGLOGIS.CODE, min = 9, max = 11,
                          priority = 0.86),

            CarrierEntity(carrierNo = 0, carrierName = CarrierEnum.KUNYOUNG.NAME, carrierCode = CarrierEnum.KUNYOUNG.CODE,
                          min = 10, max = 10, priority = 0.5),

            CarrierEntity(carrierNo = 0, carrierName = CarrierEnum.EMS.NAME, carrierCode = CarrierEnum.EMS.CODE,
                          min = 13, max = 13, priority = 0.94),
            CarrierEntity(carrierNo = 0, carrierName = CarrierEnum.TNT.NAME, carrierCode = CarrierEnum.TNT.CODE,
                          min = 9, max = 9, priority = 0.5),
            CarrierEntity(carrierNo = 0, carrierName = CarrierEnum.FEDEX.NAME, carrierCode = CarrierEnum.FEDEX.CODE,
                          min =0, max =0, priority = 0.5),
            CarrierEntity(carrierNo = 0, carrierName = CarrierEnum.USPS.NAME, carrierCode = CarrierEnum.USPS.CODE,
                          min = 0, max = 0, priority = 0.5),

            //미확정
            CarrierEntity(carrierNo = 0, carrierName = CarrierEnum.SAGAWA.NAME, carrierCode = CarrierEnum.SAGAWA.CODE,
                          min = 0, max = 0, priority = 0.0),
            CarrierEntity(carrierNo = 0, carrierName = CarrierEnum.YAMATO.NAME, carrierCode = CarrierEnum.YAMATO.CODE,
                          min = 0, max = 0, priority = 0.0),
            CarrierEntity(carrierNo = 0, carrierName = CarrierEnum.YUUBIN.NAME, carrierCode = CarrierEnum.YUUBIN.CODE,
                          min = 0, max = 0, priority = 0.0),

            CarrierEntity(carrierNo = 0, carrierName = CarrierEnum.CWAY.NAME, carrierCode = CarrierEnum.CWAY.CODE,
                          min = 0, max = 0, priority = 0.0),
            CarrierEntity(carrierNo = 0, carrierName = CarrierEnum.HOMEPICK.NAME, carrierCode = CarrierEnum.HOMEPICK.CODE,
                          min = 0, max = 0, priority = 0.85),
            CarrierEntity(carrierNo = 0, carrierName = CarrierEnum.HONAMLOGIS.NAME,
                          carrierCode = CarrierEnum.HONAMLOGIS.CODE, min = 0, max = 0,
                          priority = 0.0),
            CarrierEntity(carrierNo = 0, carrierName = CarrierEnum.SLX.NAME, carrierCode = CarrierEnum.SLX.CODE,
                          min = 0, max = 0, priority = 0.87),
            CarrierEntity(carrierNo = 0, carrierName = CarrierEnum.SWGEXP.NAME, carrierCode = CarrierEnum.SWGEXP.CODE,
                          min = 0, max = 0, priority = 0.0),
            CarrierEntity(carrierNo = 0, carrierName = CarrierEnum.UPS.NAME, carrierCode = CarrierEnum.UPS.CODE,
                          min = 0, max = 0, priority = 0.0)*/
        )
        insert(carrierList)
    }

    suspend fun recommendAutoCarrier(waybillNum: String, cnt: Int) = withContext(Dispatchers.Default)
    {
        SopoLog.d("recommend carrier >>> $waybillNum / $cnt 개")

        mutableListOf<Carrier?>().apply {
            addAll(getWithLen(waybillNum.length, cnt))
            addAll(getWithoutLen(waybillNum.length, 10))
        }
    }
}