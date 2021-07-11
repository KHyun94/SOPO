package com.delivery.sopo.models

import androidx.annotation.DrawableRes
import com.delivery.sopo.R
import com.delivery.sopo.data.repository.local.user.UserLocalRepository
import com.delivery.sopo.util.TimeUtil
import org.koin.core.KoinComponent
import org.koin.core.inject

data class PersonalMessage(var message: String, val type: Int):KoinComponent
{
    private val userLocalRepo: UserLocalRepository by inject()

    @DrawableRes
    var emojiIconRes: Int = 0

    init
    {
        val startTime = userLocalRepo.getDisturbStartTime()?.replace(":", "")?.toInt()?:0
        val endTime = userLocalRepo.getDisturbEndTime()?.replace(":", "")?.toInt()?:0

        val currentTime = TimeUtil.getTime().toInt()

        emojiIconRes = if(currentTime in startTime..endTime){
            message = "쉿, \n" + "지금은 조용한 집중이 필요한 시간"
            R.drawable.personal_status_7
        } else
        {
            when(type)
            {
                1 -> R.drawable.personal_status_1
                2 -> R.drawable.personal_status_2
                3 -> R.drawable.personal_status_3
                4 -> R.drawable.personal_status_4
                5 -> R.drawable.personal_status_5
                6 -> R.drawable.personal_status_6
                else -> 0
            }

//            when(type)
//            {
//                1 -> 0x1F64C
//                2 -> 0x1F308
//                3 -> 0x1F957
//                4 -> 0x1F933
//                5 -> 0x1F4E6
//                6 -> 0x1F60E
//                else -> 0
//            }
        }
    }

    fun getEmojiByUnicode(unicode: Int): String
    {
        return String(Character.toChars(unicode))
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