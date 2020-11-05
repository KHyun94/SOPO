package com.delivery.sopo.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.Log
import com.delivery.sopo.database.room.entity.ParcelEntity
import com.delivery.sopo.repository.impl.ParcelRepoImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object ClipboardUtil
{
    fun copyTextToClipboard(con: Context, text: String)
    {
        val clipboard = con.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("waybilNum", text)
        clipboard.setPrimaryClip(clipData)
    }


    // 20200829 최근 복사한 클립보드 내용 가져오기
    fun pasteClipboardText(con: Context, parcelImpl: ParcelRepoImpl, cb : (String) -> Unit)
    {
        CoroutineScope(Dispatchers.Main).launch {
            var clipboard = con.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager


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
                    cb.invoke(waybilNum)
                    return@launch
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
                            cb.invoke(waybilNum)
                            return@launch
                        }
                    }
                    else
                    {
                        waybilNum = ""
                        cb.invoke(waybilNum)
                        return@launch
                    }
                }
            }

            var parcel: ParcelEntity? = null


            withContext(Dispatchers.Default) {
                parcel = parcelImpl.getSingleParcelWithWaybilNum(waybilNum = waybilNum)
                Log.d("LOG.SOPO", "등록된 택배 Check $parcel")
            }



            if (parcel != null)
            {
                Log.d("LOG.SOPO", "등록된 택배가 있기에, 클립보드 안띄움")
                waybilNum = ""
                cb.invoke(waybilNum)
                return@launch
            }

            cb.invoke(waybilNum)
            return@launch
        }
    }


}