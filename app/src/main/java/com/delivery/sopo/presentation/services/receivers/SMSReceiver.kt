package com.delivery.sopo.presentation.services.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.telephony.SmsMessage
import com.delivery.sopo.util.SopoLog
import java.util.*


class SMSReceiver: BroadcastReceiver()
{
    override fun onReceive(context: Context?, intent: Intent?)
    {
        SopoLog.i("SMSReceiver onReceive() 호출")

        intent?:return

        val bundle = intent.extras
        val messages = parseSmsMessage(bundle!!)

        if (messages!!.size > 0)
        {
            val sender = messages[0]!!.originatingAddress
            val content = messages[0]!!.messageBody.toString()
            val date = Date(messages[0]!!.timestampMillis)
            SopoLog.d("sender: $sender")
            SopoLog.d("content: $content")
            SopoLog.d("date: $date")
        }
    }

    private fun parseSmsMessage(bundle: Bundle): Array<SmsMessage?>?
    {
        // PDU: Protocol Data Units
        val objs = bundle["pdus"] as Array<Any>?
        val messages = arrayOfNulls<SmsMessage>(objs?.size?:return null)
        for (i in objs.indices)
        {
            messages[i] = SmsMessage.createFromPdu(objs[i] as ByteArray)
        }
        return messages
    }}