package com.delivery.sopo.util.fun_util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.Log

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

            if (waybilNum.length < 9)
            {
                waybilNum = ""
            }
            else
            {
                val len = waybilNum.length
                var digitCnt = 0

                for (c in waybilNum)
                    if (c.isDigit()) digitCnt++

                if(digitCnt > 0)
                {
                    Log.d("LOG.SOPO", "digit => $digitCnt")
                    Log.d("LOG.SOPO", "len => $len")

                    val digitRate : Double = (digitCnt.toDouble()/len.toDouble())
                    val compareRate : Double = ((9.0/13.0))

                    Log.d("LOG.SOPO", "digit => $digitRate")
                    Log.d("LOG.SOPO", "compare => $compareRate")

                    if(digitRate < compareRate) waybilNum = ""
                }
                else
                {
                    waybilNum = ""
                }
            }
        }

        return waybilNum
    }



}