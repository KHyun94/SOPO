package com.delivery.sopo.util.fun_util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.Log
import com.delivery.sopo.models.entity.ParcelEntity
import com.delivery.sopo.repository.impl.ParcelRepoImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object ClipboardUtil
{
    // 20200829 최근 복사한 클립보드 내용 가져오기
    fun pasteClipboardText(con: Context, parcelImpl : ParcelRepoImpl): String
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
                return waybilNum
            }
            else
            {
                val len = waybilNum.length
                var digitCnt = 0

                for (c in waybilNum)
                    if (c.isDigit()) digitCnt++

                // 클립보드에 저장된 텍스트에 일부 중 숫자 이외의 문자열 비율이 높을 경우 제외
                if (digitCnt > 0)
                {
                    val digitRate: Double = (digitCnt.toDouble() / len.toDouble())
                    val compareRate: Double = ((9.0 / 13.0))

                    if (digitRate < compareRate)
                    {
                        waybilNum = ""
                        return waybilNum
                    }
                }
                else
                {
                    waybilNum = ""
                    return waybilNum
                }
            }
        }

        var parcel : ParcelEntity? = null

        CoroutineScope(Dispatchers.Default).launch {
            withContext(Dispatchers.Default) {
                parcel = parcelImpl.getSingleParcelWithWaybilNum(waybilNum = waybilNum)
            }
        }

        if(parcel != null)
        {
            waybilNum = ""
            return waybilNum
        }

        return waybilNum
    }


}