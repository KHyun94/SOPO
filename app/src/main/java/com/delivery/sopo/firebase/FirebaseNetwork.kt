package com.delivery.sopo.firebase

import com.delivery.sopo.data.repository.local.user.UserLocalRepository
import com.delivery.sopo.data.repository.remote.user.UserUseCase
import com.delivery.sopo.util.DateUtil
import com.delivery.sopo.util.SopoLog
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.*
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.*

object FirebaseNetwork: KoinComponent
{
    private val userLocalRepo: UserLocalRepository by inject()

    fun subscribedToTopic(hour: Int? = null, minutes: Int? = null) = CoroutineScope(Dispatchers.Default).async {
            val loadTopic = userLocalRepo.getTopic().let { if(it == "") null else it }

            this.launch(Dispatchers.Default) {
                loadTopic?.let { topic ->
                    isUnsubscribedToTopic(topic = topic) { isSuccess ->
                        if(!isSuccess) return@isUnsubscribedToTopic else userLocalRepo.setTopic("")
                    }
                }
            }

            this.launch(Dispatchers.Default) {
                val newTopic = makeTopicForSubscribe(hour = hour, minutes = minutes)

                isSubscribedToTopic(topic = newTopic) { isSuccess ->
                    if(!isSuccess) return@isSubscribedToTopic else userLocalRepo.setTopic(newTopic)
                }
            }
        }


    private fun makeTopicForSubscribe(hour: Int? = null, minutes: Int? = null): String
    {
        SopoLog.i("makeTopicForSubscribe(...) 호출 [data: hour - $hour / min - $minutes]")

        val topicHour: Int
        val topicMinutes: Int

        // 인자가 null일 때, 현재 시간을 기준으로 한다.
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

        SopoLog.d(msg = "택배 등록 시간 $topicHour:$topicMinutes")

        val topic = DateUtil.getSubscribedTime(topicHour, topicMinutes)
        // 01 02 03  ~ 24(00)
        SopoLog.d(msg = "구독 시간 [data:$topic]")

        return topic
    }

    private fun isSubscribedToTopic(topic: String, callback: (Boolean) -> Unit)
    {
        SopoLog.d("isSubscribedToTopic(...) 호출 [data:$topic]")

        // 01:01 ~ => 01:00
        FirebaseMessaging.getInstance()
            .unsubscribeFromTopic(topic)
            .addOnCompleteListener { subscribeTask ->

                if(!subscribeTask.isSuccessful)
                {
                    SopoLog.e("subscribedToTopic 실패 [message:${subscribeTask.exception?.message}]",
                              subscribeTask.exception)
                    return@addOnCompleteListener callback.invoke(false)
                }

                SopoLog.d(" subscribedToTopic 성공")

                return@addOnCompleteListener callback.invoke(true)
            }
    }

    private fun isUnsubscribedToTopic(topic: String, callback: (Boolean) -> Unit)
    {
        SopoLog.i("isUnsubscribedToTopic(...) 호출 [data:$topic]")

        FirebaseMessaging.getInstance()
            .unsubscribeFromTopic(topic)
            .addOnCompleteListener { unsubscribeTask ->
                if(!unsubscribeTask.isSuccessful)
                {
                    SopoLog.e(
                        "unsubscribedToTopic 실패 [message:${unsubscribeTask.exception?.message}]",
                        unsubscribeTask.exception)
                    return@addOnCompleteListener callback.invoke(false)
                }

                SopoLog.d("unsubscribedToTopic 성공")

                return@addOnCompleteListener callback.invoke(true)
            }
    }

    /**
     * FCM 구독 요청
     * 등록 시 또는 앱 재설치 시 진행 중인 택배가 있을 시 구독 요청
     */
    fun subscribedToTopicInFCM(hour: Int? = null, minutes: Int? = null)
    {
        SopoLog.d("subscribedToTopicInFCM() call")

        val topicHour: Int
        val topicMinutes: Int

        // 인자가 null일 때, 현재 시간을 기준으로 한다.
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
        SopoLog.d(msg = "택배 등록 시간 $topicHour:$topicMinutes")

        val topic = DateUtil.getSubscribedTime(topicHour, topicMinutes)
        // 01 02 03  ~ 24(00)

        SopoLog.d(msg = "Topic >>> $topic")

        // 01:01 ~ => 01
        FirebaseMessaging.getInstance().subscribeToTopic(topic).addOnCompleteListener { task ->
                if(!task.isSuccessful)
                {
                    SopoLog.e("fail to subscribe topic", task.exception)
                    return@addOnCompleteListener
                }
                SopoLog.d("success to subscribe topic at $topic")
                userLocalRepo.setTopic(topic)
            }
    }

    /**
     * FCM 구독 요청
     * 등록 시 또는 앱 재설치 시 진행 중인 택배가 있을 시 구독 요청
     */
    fun unsubscribedToTopicInFCM()
    {
        val topic = userLocalRepo.getTopic()

        if(topic == "")
        {
            SopoLog.e("fail to unsubscribe topic", Exception("Topic is null or empty"))
            return
        }

        FirebaseMessaging.getInstance().unsubscribeFromTopic(topic).addOnCompleteListener { task ->
                if(!task.isSuccessful)
                {
                    SopoLog.e("fail to unsubscribe topic", task.exception)
                    return@addOnCompleteListener
                }

                SopoLog.d("success to unsubscribe topic")
                userLocalRepo.setTopic("")
            }
    }

    fun updateFCMToken()
    {
        SopoLog.d(msg = "updateFCMToken call()")
        FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener { task ->
            if(!task.isSuccessful)
            {
                return@addOnCompleteListener
            }

            CoroutineScope(Dispatchers.Main).launch {
                UserUseCase.updateFCMToken(task.result.token)
            }
        }
    }
}