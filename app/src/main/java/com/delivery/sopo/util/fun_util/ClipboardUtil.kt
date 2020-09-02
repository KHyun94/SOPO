package com.delivery.sopo.util.fun_util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.Log
import com.delivery.sopo.database.room.AppDatabase
import com.delivery.sopo.models.entity.CourierEntity

object ClipboardUtil
{
    // 20200829 최근 복사한 클립보드 내용 가져오기
    fun pasteClipboardText(con: Context): String
    {
        val clipboard = con.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        var waybilNum = ""

        // 클립보드에 값이 있는지
        if (!(clipboard.hasPrimaryClip()))
        {
            Log.d("LOG.SOPO", "클립보드에 데이터가 없음")
        }
        else
        {
            val item: ClipData.Item = clipboard.primaryClip!!.getItemAt(0)

            waybilNum = item.text.toString()

//            if(waybilNum.contains("_"))
//            {
//                waybilNum = waybilNum
//            }
//            else if(waybilNum.contains("-"))
//            {
//
//            }
//            else
//            {
//
//            }
//
//            var isDigit = false
//
//            // 문자열 구성이 숫자로 구성되어있는지 체크
//            for (c in waybilNum)
//            {
//                if (!c.isDigit())
//                {
//                    isDigit = false
//                    break
//                }
//                else
//                {
//                    isDigit = true
//                }
//            }
//
//            // 숫자 텍스트가 아니라면 초기화
//            if (!isDigit)
//            {
//
//                waybilNum = ""
//            }
//            else
//            {
//                val len = waybilNum.length
//
//                // 복사한 값이 10자리 이하이면 초기화
//                if (len < 10 || len > 15)
//                    waybilNum = ""
//            }
        }

        return waybilNum
    }



}