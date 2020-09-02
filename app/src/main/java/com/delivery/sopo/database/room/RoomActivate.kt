package com.delivery.sopo.database.room

import android.content.Context
import android.util.Log
import com.delivery.sopo.R
import com.delivery.sopo.R.drawable.*
import com.delivery.sopo.models.CourierItem
import com.delivery.sopo.models.entity.CourierEntity

object RoomActivate
{
    val TAG = "LOG.SOPO.ROOM"

    lateinit var roomDBHelper: AppDatabase

    var rowCnt = 0

    fun initCourierDB(context: Context)
    {
        try
        {
            roomDBHelper = AppDatabase.getInstance(context = context)

            Thread(Runnable {
                rowCnt = roomDBHelper.courierDao().getAllCnt()
                Log.d(TAG, "Room All Select row cnt => ${rowCnt}")

                if (rowCnt == 0)
                {
                    val courierList = listOf<CourierEntity>(
                        CourierEntity(
                            0,
                            "우체국 택배",
                            13,
                            13,
                            1.0,
                            ic_color_korean,
                            ic_gray_korean,
                            ic_color_korean
                        ),
                        CourierEntity(
                            0,
                            "CJ대한통운",
                            10,
                            13,
                            1.0,
                            ic_color_daehan,
                            ic_gray_daehan,
                            ic_color_daehan
                        ),
                        CourierEntity(
                            0,
                            "로젠택배",
                            11,
                            11,
                            1.0,
                            ic_color_logen,
                            ic_gray_logen,
                            ic_color_logen
                        ),
                        CourierEntity(
                            0,
                            "한진택배",
                            10,
                            12,
                            1.0,
                            ic_color_hanjin,
                            ic_gray_hanjin,
                            ic_color_hanjin
                        ),
                        CourierEntity(
                            0,
                            "DHL",
                            10,
                            10,
                            1.0,
                            ic_color_dhl,
                            ic_gray_dhl,
                            ic_color_dhl
                        ),
                        CourierEntity(
                            0,
                            "천일택배",
                            11,
                            11,
                            1.0,
                            ic_color_chunil,
                            ic_gray_chunil,
                            ic_color_chunil
                        ),
                        CourierEntity(
                            0,
                            "CU 편의점택배",
                            10,
                            12,
                            1.0,
                            ic_color_daeshin,
                            ic_gray_daeshin,
                            ic_color_daeshin
                        ),
                        CourierEntity(
                            0,
                            "대신택배",
                            13,
                            13,
                            1.0,
                            ic_color_daehan,
                            ic_gray_daehan,
                            ic_color_daehan
                        ),
//                        CourierEntity(0, "한의사랑택배", 13, 13, 1.0, ic_color_ha, ic_gray_dhl, ic_color_dhl),
                        CourierEntity(
                            0,
                            "합동택배",
                            9,
                            16,
                            1.0,
                            ic_color_habdong,
                            ic_gray_habdong,
                            ic_color_habdong
                        ),
                        CourierEntity(
                            0,
                            "일양로지스",
                            9,
                            11,
                            1.0,
                            ic_color_ilyang,
                            ic_gray_ilyang,
                            ic_color_ilyang
                        ),
                        CourierEntity(
                            0,
                            "경동택배",
                            9,
                            16,
                            1.0,
                            ic_color_gyungdong,
                            ic_gray_gyungdong,
                            ic_color_gyungdong
                        ),
                        CourierEntity(
                            0,
                            "건영택배",
                            10,
                            10,
                            1.0,
                            ic_color_gunyoung,
                            ic_gray_gunyoung,
                            ic_color_gunyoung
                        ),
                        CourierEntity(
                            0,
                            "롯데택배",
                            12,
                            12,
                            1.0,
                            ic_color_lotte,
                            ic_gray_lotte,
                            ic_color_lotte
                        ),
                        CourierEntity(
                            0,
                            "EMS",
                            13,
                            13,
                            1.0,
                            ic_color_ems,
                            ic_gray_ems,
                            ic_color_ems
                        ),
                        CourierEntity(0, "TNT", 8, 9, 1.0, ic_color_tnt, ic_gray_tnt, ic_color_tnt),
                        CourierEntity(
                            0,
                            "Fedex",
                            12,
                            12,
                            1.0,
                            ic_color_fedex,
                            ic_gray_fedex,
                            ic_color_fedex
                        ),
                        CourierEntity(
                            0,
                            "USPS",
                            10,
                            22,
                            1.0,
                            ic_color_usps,
                            ic_gray_usps,
                            ic_color_usps
                        ),

                        //미확정
                        CourierEntity(
                            0,
                            "Sagawa",
                            0,
                            0,
                            1.0,
                            ic_color_sagawa,
                            ic_gray_sagawa,
                            ic_color_sagawa
                        ),
                        CourierEntity(
                            0,
                            "Kuroneko Yamato",
                            0,
                            0,
                            1.0,
                            ic_color_yamato,
                            ic_gray_yamato,
                            ic_color_yamato
                        ),
                        CourierEntity(
                            0,
                            "Japan Post",
                            0,
                            0,
                            1.0,
                            ic_color_japan,
                            ic_gray_japan,
                            ic_color_japan
                        ),
                        CourierEntity(
                            0,
                            "GS Postbox 택배",
                            0,
                            0,
                            1.0,
                            ic_color_gs,
                            ic_gray_gs,
                            ic_color_gs
                        ),
                        CourierEntity(
                            0,
                            "CWAY (Woori Express)",
                            0,
                            0,
                            1.0,
                            ic_color_cway,
                            ic_gray_cway,
                            ic_color_cway
                        ),
                        CourierEntity(
                            0,
                            "홈픽",
                            0,
                            0,
                            1.0,
                            ic_color_homepick,
                            ic_gray_homepick,
                            ic_color_homepick
                        ),
                        CourierEntity(
                            0,
                            "한서호남택배",
                            0,
                            0,
                            1.0,
                            ic_color_hanseohonam,
                            ic_gray_hanseohonam,
                            ic_color_hanseohonam
                        ),
                        CourierEntity(0, "SLX", 0, 0, 1.0, ic_color_slx, ic_gray_slx, ic_color_slx),
                        CourierEntity(
                            0,
                            "성원글로벌카고",
                            0,
                            0,
                            1.0,
                            ic_color_sungone,
                            ic_gray_sungone,
                            ic_color_sungone
                        ),
                        CourierEntity(0, "UPS", 0, 0, 1.0, ic_color_ups, ic_gray_ups, ic_color_ups)
                    )

                    roomDBHelper.courierDao().insert(courierList)
                }
            }).start()
        }
        catch (e: Exception)
        {
            Log.d(TAG, "Room Error ${e.message}")
        }

    }

    fun recommendAutoCourier(
        context: Context,
        waybilNum: String,
        cnt: Int,
        callback: Function1<List<CourierItem>?, Unit>
    )
    {
        roomDBHelper = AppDatabase.getInstance(context = context)

        try
        {
            Thread(Runnable {
                var returnList = mutableListOf<CourierItem>()

                // - ㅐor _ 삭제 버젼
                var mergeNum = ""

                var front: String = ""
                var middle: String = ""
                var back: String = ""

                var parserList = arrayListOf<String>()

                when
                {
                    waybilNum.contains('-') ->
                    {
                        parserList = waybilNum.split('-') as ArrayList<String>
                    }
                    waybilNum.contains('_') ->
                    {
                        parserList = waybilNum.split('_') as ArrayList<String>
                    }
                    else ->
                    {
                        parserList.add(waybilNum)
                    }
                }

                when (parserList.size)
                {
                    1 ->
                    {
                        mergeNum = parserList[0]
                    }
                    2 ->
                    {
                        front = parserList[0]
                        back = parserList[1]

                        if (front.length == 6 && back.length == 7)
                        {
                            // 우체국 반환

                            returnList.add(
                                CourierItem(
                                    courierName = "우체국 택배",
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

                        if (front.length == 3 && middle.length == 4 && back.length == 4)
                        {
                            //로젠 택배
                            returnList.add(CourierItem(
                                courierName = "로젠택배",
                                clickRes = R.drawable.ic_color_logen,
                                nonClickRes = R.drawable.ic_gray_logen,
                                iconRes = R.drawable.ic_color_logen
                            ))
                        }
                        else if (front.length == 4 && middle.length == 3 && back.length == 6)
                        {
                            // 경동 택배
                            returnList.add(CourierItem(
                                courierName = "경동택배",
                                clickRes = R.drawable.ic_color_gyungdong,
                                nonClickRes = R.drawable.ic_gray_gyungdong,
                                iconRes = R.drawable.ic_color_gyungdong
                            ))
                        }
                        else if (front.length == 4 && middle.length == 4 && back.length == 4)
                        {
                            //롯데 or CU 편의점 택배
                            returnList.add(CourierItem(
                                courierName = "CU 편의점 택배",
                                clickRes = R.drawable.ic_color_cu,
                                nonClickRes = R.drawable.ic_gray_cu,
                                iconRes = R.drawable.ic_color_cu
                            ))
                            returnList.add(CourierItem(
                                courierName = "롯데택배",
                                clickRes = R.drawable.ic_color_lotte,
                                nonClickRes = R.drawable.ic_gray_lotte,
                                iconRes = R.drawable.ic_color_lotte
                            ))
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
                            returnList.add(CourierItem(
                                courierName = "EMS",
                                clickRes = R.drawable.ic_color_ems,
                                nonClickRes = R.drawable.ic_gray_ems,
                                iconRes = R.drawable.ic_color_ems
                            ))
                            returnList.add(CourierItem(
                                courierName = "USPS",
                                clickRes = R.drawable.ic_color_usps,
                                nonClickRes = R.drawable.ic_gray_usps,
                                iconRes = R.drawable.ic_color_usps
                            ))
                        }
                    }
                }


                returnList =
                    roomDBHelper.courierDao()
                        .getWithLen(mergeNum.length, cnt) as MutableList<CourierItem>

                for (c in returnList) Log.d("LOG.SOPO", "===> $c")

                callback.invoke(returnList)
            }).start()
            // room으로 길이 검색
            return
        }
        catch (e: Exception)
        {
            callback.invoke(null)
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