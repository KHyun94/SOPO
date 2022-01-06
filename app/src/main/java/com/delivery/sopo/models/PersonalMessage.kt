package com.delivery.sopo.models

import androidx.annotation.RawRes
import com.delivery.sopo.R
import com.delivery.sopo.data.repository.local.user.UserLocalRepository
import com.delivery.sopo.enums.PersonalMessageEnum
import com.delivery.sopo.util.TimeUtil
import org.koin.core.KoinComponent
import org.koin.core.inject

data class PersonalMessage(var message: String, val type: Int):KoinComponent
{
    private val userLocalRepo: UserLocalRepository by inject()

    @RawRes
    var emojiIconRes: Int = 0

    lateinit var personalMessageEnum: PersonalMessageEnum

    init
    {
        val startTime = userLocalRepo.getDisturbStartTime()?.replace(":", "")?.toInt()?:0
        val endTime = userLocalRepo.getDisturbEndTime()?.replace(":", "")?.toInt()?:0

        val currentTime = TimeUtil.getTime().toInt()

        emojiIconRes = if(currentTime in startTime..endTime){
            message = "쉿, \n" + "지금은 조용한 집중이 필요한 시간"
            personalMessageEnum = PersonalMessageEnum.BE_QUIET
            R.raw.lottie_personal_status_7
        } else
        {
            when(type)
            {
                1 ->
                {
                    personalMessageEnum = PersonalMessageEnum.NEW
                    R.raw.lottie_personal_status_2
                }
                2 ->
                {
                    personalMessageEnum = PersonalMessageEnum.HELLO
                    R.raw.lottie_personal_status_2
                }
                3 ->
                {
                    personalMessageEnum = PersonalMessageEnum.SHOPPING_DETOX
                    R.raw.lottie_personal_status_3
                }
                4 ->
                {
                    personalMessageEnum = PersonalMessageEnum.PRO_UNBOXING
                    R.raw.lottie_personal_status_4
                }
                5 ->
                {personalMessageEnum = PersonalMessageEnum.SOPO_CNT
                    R.raw.lottie_personal_status_4
                }
                6 ->
                {
                    personalMessageEnum = PersonalMessageEnum.WAITING
                    R.raw.lottie_personal_status_4
                }
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