package com.delivery.sopo.database.room

import android.content.Context
import com.delivery.sopo.R
import com.delivery.sopo.R.drawable.*
import com.delivery.sopo.extensions.removeSpace
import com.delivery.sopo.models.CourierItem
import com.delivery.sopo.database.room.entity.CourierEntity
import com.delivery.sopo.repository.impl.CourierRepolmpl
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

    fun initCourierDB(context: Context)
    {
        try
        {
            val scope = CoroutineScope(Dispatchers.Default)

            scope.launch {
                roomDBHelper = AppDatabase.getInstance(context = context)

                rowCnt = roomDBHelper.courierDao().getAllCnt()
                SopoLog.d(tag = TAG, msg = "Room All Select row cnt => ${rowCnt}")

                if (rowCnt == 0)
                {
                    val courierList = listOf<CourierEntity>(
                        CourierEntity(
                            courierNo = 0,
                            courierName = "우체국 택배",
                            courierCode = "kr.epost",
                            minLen = 13,
                            maxLen = 13,
                            priority = 0.98,
                            clickRes = ic_color_korean,
                            nonClickRes = ic_gray_korean,
                            iconRes = ic_logo_korean
                        ),
                        CourierEntity(
                            courierNo = 0,
                            courierName = "CJ대한통운",
                            courierCode = "kr.cjlogistics",
                            minLen = 10,
                            maxLen = 10,
                            priority = 1.0,
                            clickRes = ic_color_daehan,
                            nonClickRes = ic_gray_daehan,
                            iconRes = ic_logo_daehan
                        ),
                        CourierEntity(
                            courierNo = 0,
                            courierName = "로젠택배",
                            courierCode = "kr.logen",
                            minLen = 11,
                            maxLen = 11,
                            priority = 0.97,
                            clickRes = ic_color_logen,
                            nonClickRes = ic_gray_logen,
                            iconRes = ic_logo_logen
                        ),
                        CourierEntity(
                            courierNo = 0,
                            courierName = "한진택배",
                            courierCode = "kr.hanjin",
                            minLen = 10,
                            maxLen = 12,
                            priority = 0.96,
                            clickRes = ic_color_hanjin,
                            nonClickRes = ic_gray_hanjin,
                            iconRes = ic_logo_hanjin
                        ),
                        CourierEntity(
                            courierNo = 0,
                            courierName = "DHL",
                            courierCode = "de.dhl",
                            minLen = 10,
                            maxLen = 10,
                            priority = 0.91,
                            clickRes = ic_color_dhl,
                            nonClickRes = ic_gray_dhl,
                            iconRes = ic_logo_dhl
                        ),
                        CourierEntity(
                            courierNo = 0,
                            courierName = "천일택배",
                            courierCode = "kr.chunilps",
                            minLen = 11,
                            maxLen = 11,
                            priority = 0.88,
                            clickRes = ic_color_chunil,
                            nonClickRes = ic_gray_chunil,
                            iconRes = ic_logo_chunil
                        ),
                        CourierEntity(
                            courierNo = 0,
                            courierName = "CU 편의점택배",
                            courierCode = "kr.cupost",
                            minLen = 10,
                            maxLen = 12,
                            priority = 0.95,
                            clickRes = ic_color_daeshin,
                            nonClickRes = ic_gray_daeshin,
                            iconRes = ic_logo_daeshin
                        ),
                        CourierEntity(
                            courierNo = 0,
                            courierName = "대신택배",
                            courierCode = "kr.daesin",
                            minLen = 13,
                            maxLen = 13,
                            priority = 0.92,
                            clickRes = ic_color_daeshin,
                            nonClickRes = ic_gray_daeshin,
                            iconRes = ic_logo_daeshin
                        ),
//                        CourierEntity(0, "한의사랑택배", 13, 13, 1.0, ic_color_ha, ic_gray_dhl, ic_color_dhl),
                        CourierEntity(
                            courierNo = 0,
                            courierName = "합동택배",
                            courierCode = "kr.hdexp",
                            minLen = 9,
                            maxLen = 16,
                            priority = 0.89,
                            clickRes = ic_color_habdong,
                            nonClickRes = ic_gray_habdong,
                            iconRes = ic_logo_habdong
                        ),
                        CourierEntity(
                            courierNo = 0,
                            courierName = "일양로지스",
                            courierCode = "kr.ilyanglogis",
                            minLen = 9,
                            maxLen = 11,
                            priority = 0.86,
                            clickRes = ic_color_ilyang,
                            nonClickRes = ic_gray_ilyang,
                            iconRes = ic_logo_ilyang
                        ),
                        CourierEntity(
                            courierNo = 0,
                            courierName = "경동택배",
                            courierCode = "kr.kunyoung",
                            minLen = 9,
                            maxLen = 16,
                            priority = 0.93,
                            clickRes = ic_color_kyungdong,
                            nonClickRes = ic_gray_kyungdong,
                            iconRes = ic_logo_kyungdong
                        ),
                        CourierEntity(
                            courierNo = 0,
                            courierName = "건영택배",
                            courierCode = "kr.kunyoung",
                            minLen = 10,
                            maxLen = 10,
                            priority = 0.5,
                            clickRes = ic_color_gunyoung,
                            nonClickRes = ic_gray_gunyoung,
                            iconRes = ic_logo_gunyoung
                        ),
                        CourierEntity(
                            courierNo = 0,
                            courierName = "롯데택배", courierCode = "kr.lotte",
                            minLen = 12,
                            maxLen = 12,
                            priority = 0.99,
                            clickRes = ic_color_lotte,
                            nonClickRes = ic_gray_lotte,
                            iconRes = ic_logo_lotte
                        ),
                        CourierEntity(
                            courierNo = 0,
                            courierName = "EMS", courierCode = "un.upu.ems",
                            minLen = 13,
                            maxLen = 13,
                            priority = 0.94,
                            clickRes = ic_color_ems,
                            nonClickRes = ic_gray_ems,
                            iconRes = ic_logo_ems
                        ),
                        CourierEntity(
                            courierNo = 0,
                            courierName = "TNT", courierCode = "nl.tnt",
                            minLen = 8,
                            maxLen = 9,
                            priority = 0.5,
                            clickRes = ic_color_tnt,
                            nonClickRes = ic_gray_tnt,
                            iconRes = ic_logo_tnt
                        ),
                        CourierEntity(
                            courierNo = 0,
                            courierName = "Fedex", courierCode = "us.fedex",
                            minLen = 12,
                            maxLen = 12,
                            priority = 0.5,
                            clickRes = ic_color_fedex,
                            nonClickRes = ic_gray_fedex,
                            iconRes = ic_logo_fedex
                        ),
                        CourierEntity(
                            courierNo = 0,
                            courierName = "USPS", courierCode = "us.usps",
                            minLen = 10,
                            maxLen = 22,
                            priority = 0.5,
                            clickRes = ic_color_usps,
                            nonClickRes = ic_gray_usps,
                            iconRes = ic_logo_usps
                        ),

                        //미확정
                        CourierEntity(
                            courierNo = 0,
                            courierName = "Sagawa", courierCode = "jp.sagawa",
                            minLen = 0,
                            maxLen = 0,
                            priority = 0.0,
                            clickRes = ic_color_sagawa,
                            nonClickRes = ic_gray_sagawa,
                            iconRes = ic_logo_sagawa
                        ),
                        CourierEntity(
                            courierNo = 0,
                            courierName = "Kuroneko Yamato", courierCode = "jp.yamato",
                            minLen = 0,
                            maxLen = 0,
                            priority = 0.0,
                            clickRes = ic_color_yamato,
                            nonClickRes = ic_gray_yamato,
                            iconRes = ic_logo_yamato
                        ),
                        CourierEntity(
                            courierNo = 0,
                            courierName = "Japan Post", courierCode = "jp.yuubin",
                            minLen = 0,
                            maxLen = 0,
                            priority = 0.0,
                            clickRes = ic_color_japan,
                            nonClickRes = ic_gray_japan,
                            iconRes = ic_logo_japan
                        ),
                        CourierEntity(
                            courierNo = 0,
                            courierName = "GS Postbox 택배", courierCode = "kr.cvsnet",
                            minLen = 0,
                            maxLen = 0,
                            priority = 0.90,
                            clickRes = ic_color_gs,
                            nonClickRes = ic_gray_gs,
                            iconRes = ic_logo_gs
                        ),
                        CourierEntity(
                            courierNo = 0,
                            courierName = "CWAY (Woori Express)", courierCode = "kr.cway",
                            minLen = 0,
                            maxLen = 0,
                            priority = 0.0,
                            clickRes = ic_color_cway,
                            nonClickRes = ic_gray_cway,
                            iconRes = ic_logo_cway
                        ),
                        CourierEntity(
                            courierNo = 0,
                            courierName = "홈픽", courierCode = "kr.homepick",
                            minLen = 0,
                            maxLen = 0,
                            priority = 0.85,
                            clickRes = ic_color_homepick,
                            nonClickRes = ic_gray_homepick,
                            iconRes = ic_logo_homepick
                        ),
                        CourierEntity(
                            courierNo = 0,
                            courierName = "한서호남택배", courierCode = "kr.honamlogis",
                            minLen = 0,
                            maxLen = 0,
                            priority = 0.0,
                            clickRes = ic_color_hanseohonam,
                            nonClickRes = ic_gray_hanseohonam,
                            iconRes = ic_logo_hanseohonam
                        ),
                        CourierEntity(
                            courierNo = 0,
                            courierName = "SLX", courierCode = "kr.slx",
                            minLen = 0,
                            maxLen = 0,
                            priority = 0.87,
                            clickRes = ic_color_slx,
                            nonClickRes = ic_gray_slx,
                            iconRes = ic_logo_slx
                        ),
                        CourierEntity(
                            courierNo = 0,
                            courierName = "성원글로벌카고", courierCode = "kr.swgexp",
                            minLen = 0,
                            maxLen = 0,
                            priority = 0.0,
                            clickRes = ic_color_sungone,
                            nonClickRes = ic_gray_sungone,
                            iconRes = ic_logo_sungone
                        ),
                        CourierEntity(
                            courierNo = 0,
                            courierName = "UPS", courierCode = "us.ups",
                            minLen = 0,
                            maxLen = 0,
                            priority = 0.0,
                            clickRes = ic_color_ups,
                            nonClickRes = ic_gray_ups,
                            iconRes = ic_logo_ups
                        )
                    )
                    roomDBHelper.courierDao().insert(courierList)

                    rowCnt = roomDBHelper.courierDao().getAllCnt()
                    SopoLog.d(tag = TAG, msg = "insert 확인 => ${rowCnt}")
                }

            }

        }
        catch (e: Exception)
        {
            SopoLog.d(tag = TAG, msg = "Room Error ${e.message}")
        }

    }

    fun recommendAutoCourier(
        context: Context,
        waybilNum: String,
        cnt: Int,
        courierRepolmpl: CourierRepolmpl
    ): MutableList<CourierItem?>?
    {
        roomDBHelper = AppDatabase.getInstance(context = context)

        try
        {
            var returnList: MutableList<CourierItem?>? = mutableListOf<CourierItem?>()

            runBlocking {
                launch {

                    val _waybilNum = waybilNum.removeSpace()
                    // - ㅐor _ 삭제 버젼
                    var mergeNum = ""

                    var front: String = ""
                    var middle: String = ""
                    var back: String = ""

                    var parserList = arrayListOf<String>()

                    when
                    {
                        _waybilNum.contains('-') ->
                        {
                            SopoLog.d(tag = TAG, msg = "waybil ${_waybilNum}")
                            parserList = _waybilNum.split('-') as ArrayList<String>
                        }
                        _waybilNum.contains('_') ->
                        {
                            parserList = _waybilNum.split('_') as ArrayList<String>
                        }
                        else ->
                        {
                            parserList.add(_waybilNum)
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
                                    CourierItem(
                                        courierName = "우체국 택배", courierCode = "",
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

                            for (i in parserList)
                            {
                                SopoLog.d(tag = TAG, msg = "$i -=> ${i.length}")
                            }

                            if (front.length == 3 && middle.length == 4 && back.length - 1 == 4)
                            {
                                //로젠 택배
                                returnList!!.add(
                                    CourierItem(
                                        courierName = "로젠택배", courierCode = "",
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
                                    CourierItem(
                                        courierName = "경동택배", courierCode = "",
                                        clickRes = R.drawable.ic_color_kyungdong,
                                        nonClickRes = R.drawable.ic_gray_kyungdong,
                                        iconRes = R.drawable.ic_color_kyungdong
                                    )
                                )
                            }
                            else if (front.length == 4 && middle.length == 4 && back.length - 1 == 4)
                            {
                                SopoLog.d(tag = TAG, msg = "cu or 롯데 $front - $middle - $back")
                                //롯데 or CU 편의점 택배
                                returnList!!.add(
                                    CourierItem(
                                        courierName = "CU 편의점 택배", courierCode = "kr.cupost",
                                        clickRes = R.drawable.ic_color_cu,
                                        nonClickRes = R.drawable.ic_gray_cu,
                                        iconRes = R.drawable.ic_color_cu
                                    )
                                )
                                returnList!!.add(
                                    CourierItem(
                                        courierName = "롯데택배", courierCode = "kr.lotte",
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
                                    CourierItem(
                                        courierName = "EMS", courierCode = "un.upu.ems",
                                        clickRes = R.drawable.ic_color_ems,
                                        nonClickRes = R.drawable.ic_gray_ems,
                                        iconRes = R.drawable.ic_color_ems
                                    )
                                )
                                returnList!!.add(
                                    CourierItem(
                                        courierName = "USPS", courierCode = "us.usps",
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
                                courierRepolmpl.getWithLen(
                                    len = mergeNum.length,
                                    cnt = cnt
                                )

                            returnList!!.addAll(
                                courierRepolmpl.getWithoutLen(
                                    len = mergeNum.length,
                                    cnt = cnt
                                ) as Collection<CourierItem>
                            )


                        }
                        1 ->
                        {
                            returnList!!.addAll(
                                courierRepolmpl.getWithLenAndCondition1(
                                    len = mergeNum.length,
                                    param1 = returnList!!.get(0)!!.courierName,
                                    cnt = cnt
                                ) as Collection<CourierItem>
                            )


                            returnList!!.addAll(
                                courierRepolmpl.getWithoutLenAndCondition1(
                                    len = mergeNum.length,
                                    param1 = returnList!!.get(0)!!.courierName,
                                    cnt = cnt
                                ) as Collection<CourierItem>
                            )


                        }
                        2 ->
                        {
                            returnList!!.addAll(
                                courierRepolmpl.getWithLenAndCondition2(
                                    len = mergeNum.length,
                                    param1 = returnList!!.get(0)!!.courierName,
                                    param2 = returnList!!.get(1)!!.courierName,
                                    cnt = cnt
                                ) as Collection<CourierItem?>
                            )

                            returnList!!.addAll(
                                courierRepolmpl.getWithoutLenAndCondition2(
                                    len = mergeNum.length,
                                    param1 = returnList!!.get(0)!!.courierName,
                                    param2 = returnList!!.get(1)!!.courierName,
                                    cnt = cnt
                                ) as Collection<CourierItem?>
                            )
                        }
                    }
                }
            }

            for (i in returnList!!)
            {
                SopoLog.d(tag = TAG, msg = "택배사 $i")
            }

            return returnList
        }
        catch (e: Exception)
        {
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