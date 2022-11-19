package com.delivery.sopo.presentation.services.receivers

import android.content.BroadcastReceiver
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import com.delivery.sopo.data.models.Carrier
import com.delivery.sopo.data.repositories.local.repository.CarrierDataSource
import com.delivery.sopo.models.parcel.Parcel
import com.delivery.sopo.presentation.services.workmanager.SOPOWorkManager
import com.delivery.sopo.util.SopoLog
import kotlinx.coroutines.*
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.Runnable

class MMSReceiver: BroadcastReceiver(), KoinComponent
{
    private val carrierRepo: CarrierDataSource by inject()

    init
    {
        SopoLog.i("MMSReceiver 호출")
    }

    override fun onReceive(context: Context?, intent: Intent?)
    {
        SopoLog.i("MMSReceiver onReceiver() 호출")

        context ?: return
        intent ?: return

        try
        {
            Handler(Looper.getMainLooper()).postDelayed(Runnable {
                parseMMS(context)
            }, 5000)
        }
        catch(e: Exception)
        {
            SopoLog.d("---> ERROR EXTRACTING MMS: " + e.localizedMessage)
        }
    }

    private fun parseMMS(context: Context)
    {
        val contentResolver: ContentResolver = context.contentResolver
        val projection = arrayOf("_id")
        val uri = Uri.parse("content://mms")
        val cursor = contentResolver.query(uri, projection, null, null, "_id desc limit 1") ?: return

        if(cursor.count == 0)
        {
            cursor.close()
            return
        }

        cursor.moveToFirst()
        val id = cursor.getString(cursor.getColumnIndex("_id"))
        cursor.close()
        //        val number = parseNumber(context, id)
        val mms = parseMessage(context, id) ?: return SopoLog.e("MMS를 읽어오는데 실패했습니다.")

        CoroutineScope(Dispatchers.IO).launch {
            val receivedData = getReceivedData(mms)

            SOPOWorkManager.registerParcelWorkManager(context = context, parcelRegister = receivedData)
        }
    }

    private fun parseMessage(context: Context, id: String): String?
    {
        var result: String? = null

        // 조회에 조건을 넣게되면 가장 마지막 한두개의 mms를 가져오지 않는다.
        val cursor: Cursor =
            context.contentResolver.query(Uri.parse("content://mms/part"), arrayOf("mid", "_id", "ct", "_data", "text"), null, null, null)
                ?: return null

        SopoLog.i("MMSReceiver.java | parseMessage | mms 메시지 갯수 : " + cursor.count + "|")

        if(cursor.count == 0)
        {
            cursor.close()
            return result
        }
        cursor.moveToFirst()
        while(!cursor.isAfterLast)
        {
            val mid = cursor.getString(cursor.getColumnIndex("mid"))
            if(id == mid)
            {
                val partId = cursor.getString(cursor.getColumnIndex("_id"))
                val type = cursor.getString(cursor.getColumnIndex("ct"))
                if("text/plain" == type)
                {
                    val data = cursor.getString(cursor.getColumnIndex("_data"))
                    result =
                        if(TextUtils.isEmpty(data)) cursor.getString(cursor.getColumnIndex("text"))
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
            inputStream = context.contentResolver.openInputStream(partURI)
            if(inputStream != null)
            {
                val isr = InputStreamReader(inputStream, "UTF-8")
                val reader = BufferedReader(isr)
                var temp: String = reader.readLine()
                while(!TextUtils.isEmpty(temp))
                {
                    sb.append(temp)
                    temp = reader.readLine()
                }
            }
        }
        catch(e: IOException)
        {
            e.printStackTrace()
        }
        finally
        {
            if(inputStream != null)
            {
                try
                {
                    inputStream.close()
                }
                catch(e: IOException)
                {
                }
            }
        }
        return sb.toString()
    }

    private fun parse(msg: String) = with(msg) {
        val index = indexOf(':')
        substring(index + 1).trim()
    }

    /**
     * 문자 내용 중 해당하는 택배사가 존재하는지 확인
     * 없을 시 throw Exception
     */
    private suspend fun getReceivedCarrier(content: String): Carrier.Info = withContext(Dispatchers.Default) {

            val carriers = carrierRepo.getAll().filterNotNull()

            var receivedCarrier: Carrier.Info? = null

            for(carrier in carriers)
            {
                if(content.contains(carrier.carrier))
                {
                    receivedCarrier = carrier
                    break
                }
            }

            return@withContext receivedCarrier ?: throw NullPointerException("일치하는 택배사가 존재하지 않습니다.")
        }

    private fun getReceivedWaybillNum(content: String): String
    {
        val rows = content.split("\n")

        var matchRow: String? = null

        for(row in rows)
        {
            if(!(row.contains("송장번호") || row.contains("운송장번호") || row.contains("운송장"))) continue
            matchRow = row
            break
        }

        matchRow ?: throw Exception("운송장번호가 존재하지 않습니다.")

        val extractedWaybillNum = parse(matchRow)

        return with(extractedWaybillNum) {
            when
            {
                contains('_') -> replace("_", "")
                contains('-') -> replace("-", "")
                else -> this
            }
        }
    }

    private fun getReceivedAlias(content: String): String
    {
        val rows = content.split("\n")

        var matchRow: String? = null

        for(row in rows)
        {
            if(!row.contains("상품명")) continue
            matchRow = row
            break
        }

        matchRow ?: throw Exception("Alias가 존재하지 않습니다.")

        return parse(matchRow)
    }

    private suspend fun getReceivedData(mms: String): Parcel.Register
    {
        try
        {
            val receivedAlias: String = getReceivedAlias(content = mms)
            val receivedWaybillsNum: String = getReceivedWaybillNum(content = mms)
            val receivedCarrier: Carrier.Info = getReceivedCarrier(content = mms)

            return Parcel.Register(receivedWaybillsNum, receivedCarrier, receivedAlias)
        }
        catch(e: Exception)
        {
            SopoLog.e("MMS 데이터 중 택배에 해당하는 데이터가 존재하지 않습니다. [message:${e.message}]")
            throw e
        }
    }
}