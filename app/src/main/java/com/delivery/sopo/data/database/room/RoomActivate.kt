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

        val list = CarrierEnum.values().map { carrier ->
            CarrierEntity(carrierNo = 0, name = carrier.NAME, code = carrier.CODE)
        }
//        val carrierList = listOf(
//            CarrierEntity(carrierNo = 0, name = CHUNILPS.NAME, code = CHUNILPS.CODE),
//            CarrierEntity(carrierNo = 0, name = CJ_LOGISTICS.NAME, code = CJ_LOGISTICS.CODE),
//            CarrierEntity(carrierNo = 0, name = CU_POST.NAME, code = CU_POST.CODE),
//            CarrierEntity(carrierNo = 0, name = CVSNET.NAME, code = CVSNET.CODE),
//            CarrierEntity(carrierNo = 0, name = DAESIN.NAME, code = DAESIN.CODE),
//            CarrierEntity(carrierNo = 0, name = EPOST.NAME, code = EPOST.CODE),
//            CarrierEntity(carrierNo = 0, name = HDEXP.NAME, code = HDEXP.CODE),
//            CarrierEntity(carrierNo = 0, name = KDEXP.NAME, code = KDEXP.CODE),
//            CarrierEntity(carrierNo = 0, name = LOGEN.NAME, code = LOGEN.CODE),
//            CarrierEntity(carrierNo = 0, name = HANJINS.NAME, code = HANJINS.CODE),
//            CarrierEntity(carrierNo = 0, name = LOTTE.NAME, code = LOTTE.CODE)
//        )
        carrierRepo.insert(list)
    }

    suspend fun recommendAutoCarrier(waybillNum: String, cnt: Int) = withContext(Dispatchers.Default)
    {
        SopoLog.d("recommend carrier >>> $waybillNum / $cnt 개")

        mutableListOf<Carrier?>().apply {
//            addAll(carrierRepo.getWithLen(waybillNum.length, cnt))
//            addAll(carrierRepo.getWithoutLen(waybillNum.length, 27))
        }
    }
}

