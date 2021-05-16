package com.delivery.sopo.data.repository.database.room

import android.content.Context
import com.delivery.sopo.R
import com.delivery.sopo.R.drawable.*
import com.delivery.sopo.extensions.removeSpace
import com.delivery.sopo.models.CarrierDTO
import com.delivery.sopo.data.repository.database.room.entity.CarrierEntity
import com.delivery.sopo.data.repository.local.repository.CarrierRepository
import com.delivery.sopo.enums.CarrierEnum
import com.delivery.sopo.util.SopoLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

object RoomActivate
{
    val TAG = "LOG.SOPO.ROOM"

    lateinit var roomDBHelper: AppDatabase

    var rowCnt = 0

    fun initializeCarrierInfoIntoDB(context: Context)
    {
        try
        {
            val scope = CoroutineScope(Dispatchers.Default)

            scope.launch {
                roomDBHelper = AppDatabase.getInstance(context = context)

                rowCnt = roomDBHelper.carrierDAO().getAllCnt()
                SopoLog.d( msg = "Room All Select row cnt => ${rowCnt}")

                if (rowCnt == 0)
                {
                    val carrierList = listOf<CarrierEntity>(
                        CarrierEntity(
                            carrierNo = 0,
                            carrierName = "우체국 택배",
                            carrierCode = "EPOST",
                            minLen = 13,
                            maxLen = 13,
                            priority = 0.98,
                            clickRes = ic_color_korean,
                            nonClickRes = ic_gray_korean,
                            iconRes = ic_logo_korean
                        ),
                        CarrierEntity(
                            carrierNo = 0,
                            carrierName = "CJ대한통운",
                            carrierCode = "CJ_LOGISTICS",
                            minLen = 10,
                            maxLen = 10,
                            priority = 1.0,
                            clickRes = ic_color_daehan,
                            nonClickRes = ic_gray_daehan,
                            iconRes = ic_logo_daehan
                        ),
                        CarrierEntity(
                            carrierNo = 0,
                            carrierName = "로젠택배",
                            carrierCode = "LOGEN",
                            minLen = 11,
                            maxLen = 11,
                            priority = 0.97,
                            clickRes = ic_color_logen,
                            nonClickRes = ic_gray_logen,
                            iconRes = ic_logo_logen
                        ),
                        CarrierEntity(
                            carrierNo = 0,
                            carrierName = "한진택배",
                            carrierCode = "HANJINS",
                            minLen = 10,
                            maxLen = 12,
                            priority = 0.96,
                            clickRes = ic_color_hanjin,
                            nonClickRes = ic_gray_hanjin,
                            iconRes = ic_logo_hanjin
                        ),
                        CarrierEntity(
                            carrierNo = 0,
                            carrierName = "DHL",
                            carrierCode = "DHL",
                            minLen = 10,
                            maxLen = 10,
                            priority = 0.91,
                            clickRes = ic_color_dhl,
                            nonClickRes = ic_gray_dhl,
                            iconRes = ic_logo_dhl
                        ),
                        CarrierEntity(
                            carrierNo = 0,
                            carrierName = "천일택배",
                            carrierCode = "일",
                            minLen = 11,
                            maxLen = 11,
                            priority = 0.88,
                            clickRes = ic_color_chunil,
                            nonClickRes = ic_gray_chunil,
                            iconRes = ic_logo_chunil
                        ),
                        CarrierEntity(
                            carrierNo = 0,
                            carrierName = "CU 편의점택배",
                            carrierCode = "CU_POST",
                            minLen = 10,
                            maxLen = 12,
                            priority = 0.95,
                            clickRes = ic_color_daeshin,
                            nonClickRes = ic_gray_daeshin,
                            iconRes = ic_logo_daeshin
                        ),
                        CarrierEntity(
                            carrierNo = 0,
                            carrierName = "대신택배",
                            carrierCode = "DAESIN",
                            minLen = 13,
                            maxLen = 13,
                            priority = 0.92,
                            clickRes = ic_color_daeshin,
                            nonClickRes = ic_gray_daeshin,
                            iconRes = ic_logo_daeshin
                        ),
//                        CourierEntity(0, "한의사랑택배", 13, 13, 1.0, ic_color_ha, ic_gray_dhl, ic_color_dhl),
                        CarrierEntity(
                            carrierNo = 0,
                            carrierName = "합동택배",
                            carrierCode = "HDEXP",
                            minLen = 9,
                            maxLen = 16,
                            priority = 0.89,
                            clickRes = ic_color_habdong,
                            nonClickRes = ic_gray_habdong,
                            iconRes = ic_logo_habdong
                        ),
                        CarrierEntity(
                            carrierNo = 0,
                            carrierName = "일양로지스",
                            carrierCode = "ILYANGLOGIS",
                            minLen = 9,
                            maxLen = 11,
                            priority = 0.86,
                            clickRes = ic_color_ilyang,
                            nonClickRes = ic_gray_ilyang,
                            iconRes = ic_logo_ilyang
                        ),
                        CarrierEntity(
                            carrierNo = 0,
                            carrierName = "경동택배",
                            carrierCode = "KDEXP",
                            minLen = 9,
                            maxLen = 16,
                            priority = 0.93,
                            clickRes = ic_color_kyungdong,
                            nonClickRes = ic_gray_kyungdong,
                            iconRes = ic_logo_kyungdong
                        ),
                        CarrierEntity(
                            carrierNo = 0,
                            carrierName = "건영택배",
                            carrierCode = "KUNYOUNG",
                            minLen = 10,
                            maxLen = 10,
                            priority = 0.5,
                            clickRes = ic_color_gunyoung,
                            nonClickRes = ic_gray_gunyoung,
                            iconRes = ic_logo_gunyoung
                        ),
                        CarrierEntity(
                            carrierNo = 0,
                            carrierName = "롯데택배", carrierCode = "LOTTE",
                            minLen = 12,
                            maxLen = 12,
                            priority = 0.99,
                            clickRes = ic_color_lotte,
                            nonClickRes = ic_gray_lotte,
                            iconRes = ic_logo_lotte
                        ),
                        CarrierEntity(
                            carrierNo = 0,
                            carrierName = "EMS", carrierCode = "EMS",
                            minLen = 13,
                            maxLen = 13,
                            priority = 0.94,
                            clickRes = ic_color_ems,
                            nonClickRes = ic_gray_ems,
                            iconRes = ic_logo_ems
                        ),
                        CarrierEntity(
                            carrierNo = 0,
                            carrierName = "TNT", carrierCode = "TNT",
                            minLen = 8,
                            maxLen = 9,
                            priority = 0.5,
                            clickRes = ic_color_tnt,
                            nonClickRes = ic_gray_tnt,
                            iconRes = ic_logo_tnt
                        ),
                        CarrierEntity(
                            carrierNo = 0,
                            carrierName = "Fedex", carrierCode = "FEDEX",
                            minLen = 12,
                            maxLen = 12,
                            priority = 0.5,
                            clickRes = ic_color_fedex,
                            nonClickRes = ic_gray_fedex,
                            iconRes = ic_logo_fedex
                        ),
                        CarrierEntity(
                            carrierNo = 0,
                            carrierName = "USPS", carrierCode = "USPS",
                            minLen = 10,
                            maxLen = 22,
                            priority = 0.5,
                            clickRes = ic_color_usps,
                            nonClickRes = ic_gray_usps,
                            iconRes = ic_logo_usps
                        ),

                        //미확정
                        CarrierEntity(
                            carrierNo = 0,
                            carrierName = "Sagawa", carrierCode = "SAGAWA",
                            minLen = 0,
                            maxLen = 0,
                            priority = 0.0,
                            clickRes = ic_color_sagawa,
                            nonClickRes = ic_gray_sagawa,
                            iconRes = ic_logo_sagawa
                        ),
                        CarrierEntity(
                            carrierNo = 0,
                            carrierName = "Kuroneko Yamato", carrierCode = "YAMATO",
                            minLen = 0,
                            maxLen = 0,
                            priority = 0.0,
                            clickRes = ic_color_yamato,
                            nonClickRes = ic_gray_yamato,
                            iconRes = ic_logo_yamato
                        ),
                        CarrierEntity(
                            carrierNo = 0,
                            carrierName = "Japan Post", carrierCode = "YUUBIN",
                            minLen = 0,
                            maxLen = 0,
                            priority = 0.0,
                            clickRes = ic_color_japan,
                            nonClickRes = ic_gray_japan,
                            iconRes = ic_logo_japan
                        ),
                        CarrierEntity(
                            carrierNo = 0,
                            carrierName = "GS Postbox 택배", carrierCode = "CVSNET",
                            minLen = 0,
                            maxLen = 0,
                            priority = 0.90,
                            clickRes = ic_color_gs,
                            nonClickRes = ic_gray_gs,
                            iconRes = ic_logo_gs
                        ),
                        CarrierEntity(
                            carrierNo = 0,
                            carrierName = "CWAY (Woori Express)", carrierCode = "CWAY",
                            minLen = 0,
                            maxLen = 0,
                            priority = 0.0,
                            clickRes = ic_color_cway,
                            nonClickRes = ic_gray_cway,
                            iconRes = ic_logo_cway
                        ),
                        CarrierEntity(
                            carrierNo = 0,
                            carrierName = "홈픽", carrierCode = "HOMEPICK",
                            minLen = 0,
                            maxLen = 0,
                            priority = 0.85,
                            clickRes = ic_color_homepick,
                            nonClickRes = ic_gray_homepick,
                            iconRes = ic_logo_homepick
                        ),
                        CarrierEntity(
                            carrierNo = 0,
                            carrierName = "한서호남택배", carrierCode = "HONAMLOGIS",
                            minLen = 0,
                            maxLen = 0,
                            priority = 0.0,
                            clickRes = ic_color_hanseohonam,
                            nonClickRes = ic_gray_hanseohonam,
                            iconRes = ic_logo_hanseohonam
                        ),
                        CarrierEntity(
                            carrierNo = 0,
                            carrierName = "SLX", carrierCode = "SLX",
                            minLen = 0,
                            maxLen = 0,
                            priority = 0.87,
                            clickRes = ic_color_slx,
                            nonClickRes = ic_gray_slx,
                            iconRes = ic_logo_slx
                        ),
                        CarrierEntity(
                            carrierNo = 0,
                            carrierName = "성원글로벌카고", carrierCode = "SWGEXP",
                            minLen = 0,
                            maxLen = 0,
                            priority = 0.0,
                            clickRes = ic_color_sungone,
                            nonClickRes = ic_gray_sungone,
                            iconRes = ic_logo_sungone
                        ),
                        CarrierEntity(
                            carrierNo = 0,
                            carrierName = "UPS", carrierCode = "UPS",
                            minLen = 0,
                            maxLen = 0,
                            priority = 0.0,
                            clickRes = ic_color_ups,
                            nonClickRes = ic_gray_ups,
                            iconRes = ic_logo_ups
                        )
                    )
                    roomDBHelper.carrierDAO().insert(carrierList)

                    rowCnt = roomDBHelper.carrierDAO().getAllCnt()
                    SopoLog.d( msg = "insert 확인 => ${rowCnt}")
                }

            }

        }
        catch (e: Exception)
        {
            SopoLog.d( msg = "Room Error ${e.message}")
        }
    }

    fun recommendAutoCarrier(context: Context, waybillNum: String, cnt: Int, carrierRepository: CarrierRepository): MutableList<CarrierDTO?>?
    {
        roomDBHelper = AppDatabase.getInstance(context = context)

        try
        {
            var returnList: MutableList<CarrierDTO?>? = mutableListOf<CarrierDTO?>()

            runBlocking {
                launch {

                    SopoLog.d("recommendAutoCarrier input data >>> $waybillNum")
                    val _waybillNum = waybillNum.removeSpace()
                    // - or _ 삭제 버젼
                    var mergeNum = ""

                    var front: String = ""
                    var middle: String = ""
                    var back: String = ""

                    var parserList = arrayListOf<String>()

                    when
                    {
                        _waybillNum.contains('-') ->
                        {
                            parserList = _waybillNum.split('-') as ArrayList<String>
                        }
                        _waybillNum.contains('_') ->
                        {
                            parserList = _waybillNum.split('_') as ArrayList<String>
                        }
                        else ->
                        {
                            parserList.add(_waybillNum)
                        }
                    }

                    when (parserList.size)
                    {
                        1 ->
                        {
                            mergeNum = parserList[0].removeSpace()
                        }
                        2 ->
                        {
                            front = parserList[0]
                            back = parserList[1]

                            if (front.length == 6 && back.length == 7)
                            {
                                // 우체국 반환
                                returnList!!.add(
                                    CarrierDTO(
                                        carrier = CarrierEnum.EPOST,
                                        clickRes = R.drawable.ic_color_korean,
                                        nonClickRes = R.drawable.ic_gray_korean,
                                        iconRes = R.drawable.ic_color_korean
                                    )
                                )
                            }
                            else
                            {
                                mergeNum = front + back
                            }
                        }
                        3 ->
                        {
                            front = parserList[0]
                            middle = parserList[1]
                            back = parserList[2]

                            if (front.length == 3 && middle.length == 4 && back.length - 1 == 4)
                            {
                                //로젠 택배
                                returnList!!.add(
                                    CarrierDTO(
                                        carrier = CarrierEnum.LOGEN,
                                        clickRes = R.drawable.ic_color_logen,
                                        nonClickRes = R.drawable.ic_gray_logen,
                                        iconRes = R.drawable.ic_color_logen
                                    )
                                )
                            }
                            else if (front.length == 4 && middle.length == 3 && back.length - 1 == 6)
                            {
                                // 경동 택배
                                returnList!!.add(
                                    CarrierDTO(
                                        carrier = CarrierEnum.KDEXP,
                                        clickRes = R.drawable.ic_color_kyungdong,
                                        nonClickRes = R.drawable.ic_gray_kyungdong,
                                        iconRes = R.drawable.ic_color_kyungdong
                                    )
                                )
                            }
                            else if (front.length == 4 && middle.length == 4 && back.length - 1 == 4)
                            {
                                //롯데 or CU 편의점 택배
                                returnList!!.add(
                                    CarrierDTO(
                                        carrier = CarrierEnum.CU_POST,
                                        clickRes = R.drawable.ic_color_cu,
                                        nonClickRes = R.drawable.ic_gray_cu,
                                        iconRes = R.drawable.ic_color_cu
                                    )
                                )
                                returnList!!.add(
                                    CarrierDTO(
                                        carrier = CarrierEnum.LOTTE,
                                        clickRes = R.drawable.ic_color_lotte,
                                        nonClickRes = R.drawable.ic_gray_lotte,
                                        iconRes = R.drawable.ic_color_lotte
                                    )
                                )
                            }
                            else
                            {
                                mergeNum = front + middle + back
                            }
                        }
                        else ->
                        {
                            mergeNum = parserList[0]
                        }
                    }

                    // 문자열이 포함되어 있을 때
                    if (!isDigit(mergeNum))
                    {
                        // 문자열 부분 포함
                        if (mergeNum.length == 13)
                        {
                            // EMS
                            front = mergeNum.substring(0, 2)
                            middle = mergeNum.substring(2, 11)
                            back = mergeNum.substring(11)

                            if (isUpper(front) && isDigit(middle) && isUpper(back))
                            {
                                returnList!!.add(
                                    CarrierDTO(
                                        carrier = CarrierEnum.EMS,
                                        clickRes = R.drawable.ic_color_ems,
                                        nonClickRes = R.drawable.ic_gray_ems,
                                        iconRes = R.drawable.ic_color_ems
                                    )
                                )
                                returnList!!.add(
                                    CarrierDTO(
                                        carrier = CarrierEnum.USPS,
                                        clickRes = R.drawable.ic_color_usps,
                                        nonClickRes = R.drawable.ic_gray_usps,
                                        iconRes = R.drawable.ic_color_usps
                                    )
                                )
                            }
                        }
                    }

                    when (returnList!!.size)
                    {
                        0 ->
                        {
                            returnList =
                                carrierRepository.getWithLen(
                                    len = mergeNum.length,
                                    cnt = cnt
                                ).toMutableList()

                            returnList!!.addAll(
                                carrierRepository.getWithoutLen(
                                    len = mergeNum.length,
                                    cnt = cnt
                                ) as MutableList<CarrierDTO?>
                            )


                        }
                        1 ->
                        {
                            returnList!!.addAll(
                                carrierRepository.getWithLenAndCondition1(
                                    len = mergeNum.length,
                                    param1 = returnList!![0]!!.carrier.NAME,
                                    cnt = cnt
                                ) as MutableList<CarrierDTO?>
                            )

                            returnList!!.addAll(
                                carrierRepository.getWithoutLenAndCondition1(
                                    len = mergeNum.length,
                                    param1 = returnList!![0]!!.carrier.NAME,
                                    cnt = cnt
                                ) as MutableList<CarrierDTO?>
                            )


                        }
                        2 ->
                        {
                            returnList!!.addAll(
                                carrierRepository.getWithLenAndCondition2(
                                    len = mergeNum.length,
                                    param1 = returnList!!.get(0)!!.carrier.NAME,
                                    param2 = returnList!!.get(1)!!.carrier.NAME,
                                    cnt = cnt
                                ) as MutableList<CarrierDTO?>
                            )

                            returnList!!.addAll(
                                carrierRepository.getWithoutLenAndCondition2(
                                    len = mergeNum.length,
                                    param1 = returnList!!.get(0)!!.carrier.NAME,
                                    param2 = returnList!!.get(1)!!.carrier.NAME,
                                    cnt = cnt
                                ) as MutableList<CarrierDTO?>
                            )
                        }
                    }
                }
            }

            return returnList
        }
        catch (e: Exception)
        {
            SopoLog.e("recommand error >>> ${e.message}", e)
//            callback.invoke(null)
            return null
        }
    }

    // 문자열 구성이 숫자로 구성되어있는지 체크
    private fun isDigit(input: String): Boolean
    {
        for (c in input) if (!c.isDigit()) return false
        return true
    }

    // 문자열 구성이 대문자로 구성되어있는지 체크
    private fun isUpper(input: String): Boolean
    {
        for (c in input) if (!c.isUpperCase()) return false
        return true
    }

}