package com.delivery.sopo.firebase

import com.delivery.sopo.repository.impl.UserRepoImpl
import com.delivery.sopo.util.DateUtil
import com.delivery.sopo.util.SopoLog
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.*

object FirebaseNetwork: KoinComponent
{
    private val userRepo: UserRepoImpl by inject()

    /**
     * FCM 구독 요청
     * 등록 시 또는 앱 재설치 시 진행 중인 택배가 있을 시 구독 요청
     * TODO topic 주제 00 ~ 24H ex) 01:01 ~ 02:00 >>> 2시 구독
     */
    fun subscribedToTopicInFCM(hour: Int? = null, minutes: Int? = null)
    {
        var topicHour : Int
        var topicMinutes : Int

        if(hour == null || minutes == null)
        {
            val calendar = Calendar.getInstance()

            topicHour = calendar.get(Calendar.HOUR_OF_DAY)
            topicMinutes = calendar.get(Calendar.MINUTE)
        }
        else
        {
            topicHour = hour
            topicMinutes = minutes
        }

        val topic = DateUtil.getSubscribedTime(topicHour, topicMinutes)
        // 01 02 03  ~ 24(00)

        SopoLog.d( msg = "Topic >>> $topic")

        // 01:01 ~ => 01
        FirebaseMessaging.getInstance().subscribeToTopic(topic)
            .addOnCompleteListener { task ->
                if(!task.isSuccessful)
                {
//                    callback.invoke(null, ErrorResult(null, "구독 실패", ErrorResult.ERROR_TYPE_DIALOG, null, task.exception))
                    return@addOnCompleteListener
                }

                userRepo.setTopic(topic)
//                callback.invoke(SuccessResult(SUCCESS, "구독 성공 >>> ${topic}", null), null)
            }
    }

    /**
     * FCM 구독 요청
     * 등록 시 또는 앱 재설치 시 진행 중인 택배가 있을 시 구독 요청
     * TODO topic 주제 00 ~ 24H ex) 01:01 ~ 02:00 >>> 2시 구독
     */
    fun unsubscribedToTopicInFCM()
    {
        val topic = userRepo.getTopic()

        if(topic == "")
        {
//            callback.invoke(null, ErrorResult(null, "구독 해제 실패", ErrorResult.ERROR_TYPE_NON, null, null))
            return
        }

        FirebaseMessaging.getInstance().unsubscribeFromTopic(topic)
            .addOnCompleteListener { task ->
                if(!task.isSuccessful)
                {
//                    callback.invoke(null, ErrorResult(null, "구독 해제 실패", ErrorResult.ERROR_TYPE_NON, null, task.exception))
                    return@addOnCompleteListener
                }

                userRepo.setTopic("")
//                callback.invoke(SuccessResult(SUCCESS, "구독 해제 성공 >>> $topic", null), null)
            }
    }

    // TODO Firebase Update Token 활성화
    fun updateFCMToken()
    {
        SopoLog.d( msg = "updateFCMToken call()")
        FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener { task ->
            if(!task.isSuccessful)
            {
//                val code = CodeUtil.getCode(task.exception.getCommonMessage(SOPOApp.INSTANCE))
//                callback.invoke(TestResult.ErrorResult<String?>(code, code.MSG, ErrorResult.ERROR_TYPE_NON, null, task.exception))
                return@addOnCompleteListener
            }

//            callback.invoke(TestResult.SuccessResult<InstanceIdResult>(SUCCESS, SUCCESS.MSG, task.result))
        }
    }
}