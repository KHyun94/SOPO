package com.delivery.sopo.models

import com.delivery.sopo.data.repository.local.user.UserLocalRepository
import com.delivery.sopo.util.TimeUtil
import org.koin.core.KoinComponent
import org.koin.core.inject

data class PersonalMessage(var message: String, val type: Int):KoinComponent
{
    private val userLocalRepo: UserLocalRepository by inject()

    var emojiCode: String = ""

    init
    {
        val startTime = userLocalRepo.getDisturbStartTime()?.replace(":", "")?.toInt()?:0
        val endTime = userLocalRepo.getDisturbEndTime()?.replace(":", "")?.toInt()?:0

        val currentTime = TimeUtil.getTime().toInt()

        emojiCode += if(currentTime in startTime..endTime){
            message = "쉿, \n" + "지금은 조용한 집중이 필요한 시간"
            "129323"
        } else
        {
            when(type)
            {
                1 ->
                {
                    //                0x1F64C
                    "128588"
                }
                2 ->
                {
                    //                0x1F308
                    "127752"
                }
                3 ->
                {
                    //                0x1F957
                    "129367"
                }
                4 ->
                {
                    //                0x1F933
                    "129331"
                }

                5 ->
                {
                    //                0x1F4E6
                    "128230"
                }
                6 ->
                {
                    //                0x1F60E
                    "128526"
                }
                else -> ""
            }
        }


        emojiCode += ";"
    }

    /*


1: 만세:U+1F64C

2: 무지개: U+1F308


3: 그린 푸드:U+1F957

4:침묵:U+1F92B

5:선글라스:U+1F60E

6-1:U+1F4E6


6-2:셀카:U+1F933

 */
}