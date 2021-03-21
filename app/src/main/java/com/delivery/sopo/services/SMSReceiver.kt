package com.delivery.sopo.services

import android.content.BroadcastReceiver
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Handler
import android.text.TextUtils
import com.delivery.sopo.util.SopoLog
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.text.MessageFormat


class SMSReceiver: BroadcastReceiver()
{
    override fun onReceive(context: Context?, intent: Intent?)
    {
        SopoLog.i("SMSReceiver - on Receiver")

        try
        {
            val runn = Runnable { parseMMS(context!!) }
            val handler = Handler()
            handler.postDelayed(runn, 10000) // 시간이 너무 짧으면 못 가져오는게 있더라

        }
        catch (e: Exception)
        {
            SopoLog.d("---> ERROR EXTRACTING MMS: " + e.localizedMessage)
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