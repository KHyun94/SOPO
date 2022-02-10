package com.delivery.sopo.data.database.room

import com.delivery.sopo.models.Carrier
import com.delivery.sopo.data.database.room.entity.CarrierEntity
import com.delivery.sopo.data.repository.local.repository.CarrierRepository
import com.delivery.sopo.enums.CarrierEnum
import com.delivery.sopo.enums.CarrierEnum.*
import com.delivery.sopo.util.SopoLog
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
            CarrierEntity(carrierNo = 0, carrierName = CarrierEnum.KDEXP.NAME, carrierCode = CarrierEnum.KDEXP.CODE,
                          min = 9, max = 16, priority = 0.99),
            CarrierEntity(carrierNo = 0, carrierName = CarrierEnum.LOGEN.NAME, carrierCode = CarrierEnum.LOGEN.CODE,
                          min = 11, max = 11, priority = 0.97),
            CarrierEntity(carrierNo = 0, carrierName = CarrierEnum.HANJINS.NAME, carrierCode = CarrierEnum.HANJINS.CODE,
                          min = 10, max = 12, priority = 0.96),

            CarrierEntity(carrierNo = 0, carrierName = CarrierEnum.LOTTE.NAME, carrierCode = CarrierEnum.LOTTE.CODE,
                          min = 12, max = 12, priority = 0.99)
        )
        carrierRepo.insert(carrierList)
    }

    suspend fun recommendAutoCarrier(waybillNum: String, cnt: Int) = withContext(Dispatchers.Default)
    {
        SopoLog.d("recommend carrier >>> $waybillNum / $cnt 개")

        mutableListOf<Carrier?>().apply {
            addAll(carrierRepo.getWithLen(waybillNum.length, cnt))
            addAll(carrierRepo.getWithoutLen(waybillNum.length, 27))
        }
    }
}

