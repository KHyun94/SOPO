package com.delivery.sopo.data.repository.database.room

import com.delivery.sopo.models.CarrierDTO
import com.delivery.sopo.data.repository.database.room.entity.CarrierEntity
import com.delivery.sopo.data.repository.local.repository.CarrierRepository
import com.delivery.sopo.enums.CarrierEnum.*
import kotlinx.coroutines.*
import org.koin.core.KoinComponent
import org.koin.core.inject

object RoomActivate: KoinComponent
{
    private val carrierRepo: CarrierRepository by inject()

    var rowCnt = 0

    suspend fun initializeCarrierInfoIntoDB() = withContext(Dispatchers.Default) {

        rowCnt = carrierRepo.getAllCnt()

        if(rowCnt > 0) return@withContext

        val carrierList = listOf<CarrierEntity>(
            CarrierEntity(carrierNo = 0, carrierName = EPOST.NAME, carrierCode = EPOST.CODE,
                          min = 13, max =13, priority = 0.98),
            CarrierEntity(carrierNo = 0, carrierName = CJ_LOGISTICS.NAME,
                          carrierCode = CJ_LOGISTICS.CODE, min = 13, max = 13, priority = 1.0),
            CarrierEntity(carrierNo = 0, carrierName = LOGEN.NAME, carrierCode = LOGEN.CODE,
                          min = 11, max = 11, priority = 0.97),
            CarrierEntity(carrierNo = 0, carrierName = HANJINS.NAME, carrierCode = HANJINS.CODE,
                          min = 10, max = 12, priority = 0.96),
            CarrierEntity(carrierNo = 0, carrierName = DHL.NAME, carrierCode = DHL.CODE,
                          min = 10, max = 10, priority = 0.91),
            CarrierEntity(carrierNo = 0, carrierName = CHUNILPS.NAME, carrierCode = CHUNILPS.CODE,
                          min = 11, max =11, priority = 0.88),
            CarrierEntity(carrierNo = 0, carrierName = CU_POST.NAME, carrierCode = CU_POST.CODE,
                          min = 10, max =12, priority = 0.95),
            CarrierEntity(carrierNo = 0, carrierName = DAESIN.NAME, carrierCode = DAESIN.CODE,
                          min = 13, max =13, priority = 0.92),
            CarrierEntity(carrierNo = 0, carrierName = HDEXP.NAME, carrierCode = HDEXP.CODE,
                          min = 9, max = 16, priority = 0.89),
            CarrierEntity(carrierNo = 0, carrierName = ILYANGLOGIS.NAME,
                          carrierCode = ILYANGLOGIS.CODE, min = 9, max = 11,
                          priority = 0.86),
            CarrierEntity(carrierNo = 0, carrierName = KDEXP.NAME, carrierCode = KDEXP.CODE,
                          min = 9, max = 16, priority = 0.93),
            CarrierEntity(carrierNo = 0, carrierName = KUNYOUNG.NAME, carrierCode = KUNYOUNG.CODE,
                          min = 10, max = 10, priority = 0.5),
            CarrierEntity(carrierNo = 0, carrierName = LOTTE.NAME, carrierCode = LOTTE.CODE,
                          min = 12, max = 12, priority = 0.99),
            CarrierEntity(carrierNo = 0, carrierName = EMS.NAME, carrierCode = EMS.CODE,
                          min = 13, max = 13, priority = 0.94),
            CarrierEntity(carrierNo = 0, carrierName = TNT.NAME, carrierCode = TNT.CODE,
                          min = 9, max = 9, priority = 0.5),
            CarrierEntity(carrierNo = 0, carrierName = FEDEX.NAME, carrierCode = FEDEX.CODE,
                          min =0, max =0, priority = 0.5),
            CarrierEntity(carrierNo = 0, carrierName = USPS.NAME, carrierCode = USPS.CODE,
                          min = 0, max = 0, priority = 0.5),

            //미확정
            CarrierEntity(carrierNo = 0, carrierName = SAGAWA.NAME, carrierCode = SAGAWA.CODE,
                          min = 0, max = 0, priority = 0.0),
            CarrierEntity(carrierNo = 0, carrierName = YAMATO.NAME, carrierCode = YAMATO.CODE,
                          min = 0, max = 0, priority = 0.0),
            CarrierEntity(carrierNo = 0, carrierName = YUUBIN.NAME, carrierCode = YUUBIN.CODE,
                          min = 0, max = 0, priority = 0.0),
            CarrierEntity(carrierNo = 0, carrierName = CVSNET.NAME, carrierCode = CVSNET.CODE,
                          min = 0, max = 0, priority = 0.90),
            CarrierEntity(carrierNo = 0, carrierName = CWAY.NAME, carrierCode = CWAY.CODE,
                          min = 0, max = 0, priority = 0.0),
            CarrierEntity(carrierNo = 0, carrierName = HOMEPICK.NAME, carrierCode = HOMEPICK.CODE,
                          min = 0, max = 0, priority = 0.85),
            CarrierEntity(carrierNo = 0, carrierName = HONAMLOGIS.NAME,
                          carrierCode = HONAMLOGIS.CODE, min = 0, max = 0,
                          priority = 0.0),
            CarrierEntity(carrierNo = 0, carrierName = SLX.NAME, carrierCode = SLX.CODE,
                          min = 0, max = 0, priority = 0.87),
            CarrierEntity(carrierNo = 0, carrierName = SWGEXP.NAME, carrierCode = SWGEXP.CODE,
                          min = 0, max = 0, priority = 0.0),
            CarrierEntity(carrierNo = 0, carrierName = UPS.NAME, carrierCode = UPS.CODE,
                          min = 0, max = 0, priority = 0.0))
        carrierRepo.insert(carrierList)
    }

    suspend fun recommendAutoCarrier(waybillNum: String, cnt: Int) = withContext(Dispatchers.Default)
    {
        carrierRepo.getWithLen(waybillNum.length, cnt)
    }
}

    // 문자열 구성이 숫자로 구성되어있는지 체크
    private fun isDigit(input: String): Boolean
    {
        for(c in input) if(!c.isDigit()) return false
        return true
    }

    // 문자열 구성이 대문자로 구성되어있는지 체크
    private fun isUpper(input: String): Boolean
    {
        for(c in input) if(!c.isUpperCase()) return false
        return true
    }

