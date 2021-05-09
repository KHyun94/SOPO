package com.delivery.sopo.services.receivers

import android.content.BroadcastReceiver
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Handler
import android.text.TextUtils
import com.delivery.sopo.data.repository.local.repository.CarrierRepository
import com.delivery.sopo.util.SopoLog
import kotlinx.coroutines.*
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.Runnable
import java.text.MessageFormat


class MMSReceiver: BroadcastReceiver(), KoinComponent
{
    private val carrierRepo: CarrierRepository by inject()

    override fun onReceive(context: Context?, intent: Intent?)
    {
        SopoLog.i("SMSReceiver - on Receiver")

        context?:return

        try
        {
            Handler().postDelayed(Runnable {
                parseMMS(context)
            }, 5000)
        }
        catch (e: Exception)
        {
            SopoLog.d("---> ERROR EXTRACTING MMS: " + e.localizedMessage)
        }
    }

    fun parse(msg: String) = with(msg) {
        val index = indexOf(':')
        substring(index+1).trim()
    }

    fun checkMessageAboutParcel(mms: String)
    {
        val list = mms.split("\n")

        var parcelNickname: String? = null
        var parcelWayBilNo: String? = null
        var parcelCarrier: String? = null

        var cnt = 1

        list.forEach { value ->

            SopoLog.d("${cnt++}번째 row >>> $value")

            if(value.contains("상품명")) parcelNickname = parse(value)
            if(value.contains("운송장번호"))parcelWayBilNo = parse(value)
            if(value.contains("택배사")) parcelCarrier = parse(value).replace("택배", "")

            if(parcelCarrier == null && value.contains("택배"))
            {

            }
        }

        SopoLog.d("상품명>>>$parcelNickname")
        SopoLog.d("운송장번호>>>$parcelWayBilNo")

        if(parcelCarrier != null)
        {
            val carrierList = runBlocking(Dispatchers.Default) { carrierRepo.getCarrierEntityWithPartName("%${parcelCarrier}%") }
            SopoLog.d("택배사(${parcelCarrier}) >>> ${carrierList.joinToString()}")
        }


    }

    private fun parseMMS(context: Context)
    {
        val contentResolver: ContentResolver = context.contentResolver
        val projection = arrayOf("_id")
        val uri = Uri.parse("content://mms")
        val cursor = contentResolver.query(uri, projection, null, null, "_id desc limit 1")
        if (cursor!!.count == 0)
        {
            cursor.close()
            return
        }
        cursor.moveToFirst()
        val id = cursor.getString(cursor.getColumnIndex("_id"))
        cursor.close()
        val number = parseNumber(context, id)
        val msg = parseMessage(context, id)

        SopoLog.d("MMSReceiver.java | parseMMS >>> |$number|$msg")

        checkMessageAboutParcel(msg?:"")
    }

    private fun parseNumber(context: Context, id: String): String?
    {
        var result: String? = null
        val uri = Uri.parse(MessageFormat.format("content://mms/{0}/addr", id))
        val projection = arrayOf("address")
        val selection = "msg_id = ? and type = 137" // type=137은 발신자
        val selectionArgs = arrayOf(id)
        val cursor: Cursor = context.contentResolver
            .query(uri, projection, selection, selectionArgs, "_id asc limit 1")?:return null
        if (cursor.count == 0)
        {
            cursor.close()
            return result
        }
        cursor.moveToFirst()
        result = cursor.getString(cursor.getColumnIndex("address"))
        cursor.close()
        return result
    }

    private fun parseMessage(context: Context, id: String): String?
    {
        var result: String? = null

        // 조회에 조건을 넣게되면 가장 마지막 한두개의 mms를 가져오지 않는다.
        val cursor: Cursor = context.getContentResolver()
            .query(Uri.parse("content://mms/part"), arrayOf("mid", "_id", "ct", "_data", "text"), null, null, null)?:return null

        SopoLog.i("MMSReceiver.java | parseMessage | mms 메시지 갯수 : " + cursor.count + "|")
        if (cursor.count == 0)
        {
            cursor.close()
            return result
        }
        cursor.moveToFirst()
        while (!cursor.isAfterLast)
        {
            val mid = cursor.getString(cursor.getColumnIndex("mid"))
            if (id == mid)
            {
                val partId = cursor.getString(cursor.getColumnIndex("_id"))
                val type = cursor.getString(cursor.getColumnIndex("ct"))
                if ("text/plain" == type)
                {
                    val data = cursor.getString(cursor.getColumnIndex("_data"))
                    result =
                        if (TextUtils.isEmpty(data)) cursor.getString(cursor.getColumnIndex("text"))
                        else parseMessageWithPartId(context, partId)
                }
            }
            cursor.moveToNext()
        }
        cursor.close()
        return result
    }


    private fun parseMessageWithPartId(context: Context, id: String): String?
    {
        val partURI = Uri.parse("content://mms/part/$id")
        var inputStream: InputStream? = null
        val sb = StringBuilder()
        try
        {
            inputStream = context.getContentResolver().openInputStream(partURI)
            if (inputStream != null)
            {
                val isr = InputStreamReader(inputStream, "UTF-8")
                val reader = BufferedReader(isr)
                var temp: String = reader.readLine()
                while (!TextUtils.isEmpty(temp))
                {
                    sb.append(temp)
                    temp = reader.readLine()
                }
            }
        }
        catch (e: IOException)
        {
            e.printStackTrace()
        }
        finally
        {
            if (inputStream != null)
            {
                try
                {
                    inputStream.close()
                }
                catch (e: IOException)
                {
                }
            }
        }
        return sb.toString()
    }
}