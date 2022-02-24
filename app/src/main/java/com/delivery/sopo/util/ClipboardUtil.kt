package com.delivery.sopo.util

import android.content.ClipData
import android.content.ClipDescription.MIMETYPE_TEXT_PLAIN
import android.content.ClipboardManager
import android.content.Context
import com.delivery.sopo.data.repository.local.repository.ParcelRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.KoinComponent
import org.koin.core.inject

object ClipboardUtil: KoinComponent
{
    private val PARCEL_IMPL: ParcelRepository by inject()

    fun copyTextToClipboard(con: Context, text: String)
    {
        val clipboard = con.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("waybillNum", text)
        clipboard.setPrimaryClip(clipData)
    }


    // 20200829 최근 복사한 클립보드 내용 가져오기
    suspend fun pasteClipboardText(context: Context): String?
    {
        SopoLog.d("pasteClipboardText(...) 호출")
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        var clipboardText = ""

        // 클립보드에 값이 있는지
        if(!clipboard.hasPrimaryClip())
        {
            SopoLog.d("클립보드 데이터가 없습니다.")
            return null
        }
        else if(clipboard.primaryClipDescription?.hasMimeType(MIMETYPE_TEXT_PLAIN) == false)
        {
            SopoLog.d("클립보드 데이터가 텍스트가 아닙니다.")
            return null
        }

        val clipData = clipboard.primaryClip?.getItemAt(0)?.coerceToText(context) ?: return null

        SopoLog.d("클립보드 순정 데이터 $clipData")


        clipboardText = getOnlyDigit(clipData.toString())

        SopoLog.d("클립보드 번호 데이터 $clipboardText")

        if(clipboardText.length < 9 || clipboardText.length >= 15)
        {
            return null
        }

        val isExistParcel = isExistParcel(clipboardText)

        if(isExistParcel)
        {
            return null
        }

        return clipboardText
    }

    private suspend fun isExistParcel(waybillNum: String): Boolean
    {
        return withContext(Dispatchers.Default) {
            PARCEL_IMPL.getSingleParcelWithWaybillNum(waybillNum = waybillNum)
        } != null
    }

    fun getOnlyDigit(inputData: String): String
    {
        val builder = StringBuilder()

        inputData.filter {
            it.isDigit()
        }.map {
            builder.append(it)
        }

        return builder.toString()
    }

}