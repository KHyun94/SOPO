package com.delivery.sopo.util

import android.content.ClipData
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
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        var clipboardText = ""

        // 클립보드에 값이 있는지
        if(!clipboard.hasPrimaryClip()) return null

        val item: ClipData.Item = clipboard.primaryClip?.getItemAt(0) ?: return null

        clipboardText = getOnlyDigit(item.text.toString())

        if(clipboardText.length < 9)
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

    suspend fun isExistParcel(waybillNum: String): Boolean
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