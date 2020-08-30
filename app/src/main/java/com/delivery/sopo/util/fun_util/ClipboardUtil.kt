package com.delivery.sopo.util.fun_util

import android.content.ClipData
import android.content.ClipDescription.MIMETYPE_TEXT_PLAIN
import android.content.ClipboardManager
import android.content.Context
import android.util.Log

object ClipboardUtil
{
    // 20200829 최근 복사한 클립보드 내용 가져오기
    fun pasteClipboardText(con: Context): String
    {
        val clipboard = con.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        var trackNum = ""

        // 클립보드에 값이 있는지
        if (!(clipboard.hasPrimaryClip()))
        {
            Log.d("LOG.SOPO", "클립보드에 데이터가 없음")
        } else
        {
            val item: ClipData.Item = clipboard.primaryClip!!.getItemAt(0)
            trackNum = item.text.toString()

            var isDigit = false

            // 문자열 구성이 숫자로 구성되어있는지 체크
            for (c in trackNum)
            {
                if (!c.isDigit())
                {
                    isDigit = false
                    break
                }
                else
                {
                    isDigit = true
                }
            }

            // 숫자 텍스트가 아니라면 초기화
            if (!isDigit)
            {
                trackNum = ""
            }
            else
            {
                val len = trackNum.length

                // 복사한 값이 10자리 이하이면 초기화
                if (len < 10 || len > 15)
                    trackNum = ""
            }
        }

        return trackNum
    }

}