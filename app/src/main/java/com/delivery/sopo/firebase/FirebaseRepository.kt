package com.delivery.sopo.firebase

import android.os.Bundle
import com.delivery.sopo.SOPOApp
import com.delivery.sopo.enums.ResponseCode.*
import com.delivery.sopo.extensions.getCommonMessage
import com.delivery.sopo.extensions.match
import com.delivery.sopo.models.ErrorResult
import com.delivery.sopo.models.SuccessResult
import com.delivery.sopo.models.TestResult
import com.delivery.sopo.repository.impl.UserRepoImpl
import com.delivery.sopo.util.CodeUtil
import com.delivery.sopo.util.DateUtil
import com.delivery.sopo.util.SopoLog
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseException
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.InstanceIdResult
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.*
import kotlin.math.min

typealias FirebaseVoidCallback = (SuccessResult<String?>?, ErrorResult<String?>?) -> Unit
typealias FirebaseUserCallback = (SuccessResult<FirebaseUser?>?, ErrorResult<String?>?) -> Unit
//typealias FirebaseFCMCallback = (Task<InstanceIdResult>) -> Unit
typealias FirebaseFCMCallback = (TestResult) -> Unit
//todo kh firebase repository로 수정 예
object FirebaseRepository : FirebaseDataSource, KoinComponent
{
    private val TAG = this.javaClass.simpleName
    private val userRepoImpl : UserRepoImpl by inject()

    /** Firebase Create Account:Firebase 계정 생성
     * @param email:String
     * @param password:String
     * @param callback:FirebaseUserCallback
     * success data -> firebaseUser
     * error data -> null
     * */
    @Throws(FirebaseException::class)
    override fun createFirebaseAccount(email: String, password: String, callback: FirebaseUserCallback)
    {
        SOPOApp.auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if(!task.isSuccessful)
            {
                val exception = task.exception
                val errorCode = exception.match
                callback.invoke(null, ErrorResult(errorCode, errorCode.MSG, ErrorResult.ERROR_TYPE_DIALOG, null, exception))
            }
            else
            {
                callback.invoke(SuccessResult(SUCCESS, SUCCESS.MSG, task.result.user), null)
            }
        }
    }

    @Throws(FirebaseException::class)
    override fun sendFirebaseAuthEmail(auth: FirebaseUser, callback: FirebaseVoidCallback)
    {
        auth.sendEmailVerification().addOnCompleteListener { task ->
            if (!task.isSuccessful)
            {
                val exception = task.exception
                val errorCode = task.exception.match

                callback.invoke(null, ErrorResult(errorCode, errorCode.MSG, ErrorResult.ERROR_TYPE_DIALOG, null, exception))
            }
            else
            {
                val bundle = Bundle()
                bundle.putString(FirebaseAnalytics.Param.METHOD, "email")
                FirebaseAnalytics.getInstance(SOPOApp.INSTANCE)
                    .logEvent(FirebaseAnalytics.Event.SIGN_UP, bundle)

                callback.invoke(SuccessResult(SUCCESS, "회원가입에 성공하셨습니다.\n해당 이메일에 인증메일을 확인해주세요.", SUCCESS.MSG), null)
            }
        }
    }

    @Throws(FirebaseException::class)
    override fun updateFCMToken(callback: FirebaseFCMCallback)
    {
        SopoLog.d(tag = TAG, msg = "updateFCMToken call()")
        FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener { task ->
            if(!task.isSuccessful)
            {
                val code = CodeUtil.getCode(task.exception.getCommonMessage(SOPOApp.INSTANCE))
                callback.invoke(TestResult.ErrorResult<String?>(code, code.MSG, ErrorResult.ERROR_TYPE_NON, null, task.exception))
                return@addOnCompleteListener
            }

            callback.invoke(TestResult.SuccessResult<InstanceIdResult>(SUCCESS, SUCCESS.MSG, task.result))
        }
    }

    @Throws(FirebaseException::class)
    override fun loginFirebaseWithCustomToken(
        email: String,
        token: String,
        callback: FirebaseUserCallback
    )
    {
        SopoLog.d(tag = TAG, msg = "loginFirebaseWithCustomToken call()")

        SOPOApp.auth.signInWithCustomToken(token).addOnCompleteListener { task ->
            val exception = task.exception

            if (!task.isSuccessful)
            {
                callback.invoke(null, ErrorResult(code = exception.match, errorMsg = "Fail To Firebase Custom Login", errorType = ErrorResult.ERROR_TYPE_DIALOG, e = task.exception))
                return@addOnCompleteListener
            }
            else
            {
                val firebaseUser = task.result.user
                val firebaseUId = firebaseUser?.uid

                if (firebaseUId == "")
                {
                    callback.invoke(null, ErrorResult(null, errorMsg = "Fail To Get Firebase uid", errorType = ErrorResult.ERROR_TYPE_DIALOG, e = exception))
                    return@addOnCompleteListener
                }

                if (firebaseUser?.email == null || firebaseUser.email == "" || firebaseUser.email != email)
                {
                    // todo update Email

                    callback.invoke(null, ErrorResult(FIREBASE_ERROR_NOT_UPDATE_EMAIL, FIREBASE_ERROR_NOT_UPDATE_EMAIL.MSG,  ErrorResult.ERROR_TYPE_NON,null, null))
                }
                else
                {
                    // oauth login
                    callback.invoke(SuccessResult(SUCCESS, SUCCESS.MSG, null), null)
                }
            }
        }
    }

    @Throws(FirebaseException::class)
    override fun updateFirebaseEmail(email: String, user: FirebaseUser, callback: FirebaseVoidCallback)
    {
        SopoLog.d(tag = TAG, msg = "updateFirebaseEmail call()")

        user.updateEmail(email).addOnCompleteListener { task ->
            if (!task.isSuccessful)
            {
                val exceptionCode = if (task.exception.match == FIREBASE_ERROR_UNKNOWN) null else task.exception.match
                val errorMsg = exceptionCode?.MSG ?: "Fail To Update Firebase email"
                callback.invoke(null, ErrorResult(code = exceptionCode, errorMsg = errorMsg, errorType = ErrorResult.ERROR_TYPE_DIALOG, e = task.exception))
                return@addOnCompleteListener
            }
            else
            {
                callback.invoke(SuccessResult<String?>(SUCCESS, "SUCCESS", null), null)
            }
        }
    }

    @Throws(FirebaseException::class)
    fun firebaseSendEmail(auth: FirebaseUser?, callback: FirebaseVoidCallback)
    {

        if (auth == null)
        {
            callback.invoke(
                null, ErrorResult(
                    null, "No Firebase User", ErrorResult.ERROR_TYPE_DIALOG, null,
                    NullPointerException()
                )
            )
            return
        }

        auth.sendEmailVerification().addOnCompleteListener {

            if (!it.isSuccessful)
            {
                callback.invoke(
                    null, ErrorResult(
                        FIREBASE_ERROR_AUTH_EMAIL, "이메일 인증 메일 전송에 실패했습니다. 다시 한번 시도해주세요.",
                        ErrorResult.ERROR_TYPE_DIALOG, null, it.exception
                    )
                )
            }
            else
            {
                val bundle = Bundle()
                bundle.putString(FirebaseAnalytics.Param.METHOD, "email")
                FirebaseAnalytics.getInstance(SOPOApp.INSTANCE)
                    .logEvent(FirebaseAnalytics.Event.SIGN_UP, bundle)

                val str = ""
                str.isEmpty()
                callback.invoke(SuccessResult(SUCCESS, "SUCCESS", null), null)
            }
        }
    }

    @Throws(FirebaseException::class)
    fun firebaseCreateUser(email: String, password: String, callback: FirebaseUserCallback)
    {
        SOPOApp.auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if(!task.isSuccessful)
            {
                val exception = task.exception
                val errorCode = exception.match
                callback.invoke(null, ErrorResult(
                    errorCode,
                    errorCode.MSG,
                    ErrorResult.ERROR_TYPE_DIALOG,
                    null,
                    exception
                ))
            }
            else
            {
                callback.invoke(SuccessResult(SUCCESS, SUCCESS.MSG, task.result.user), null)
            }
        }
    }


    @Throws(FirebaseException::class)
    override fun firebaseSelfLogin(email: String, password: String, callback: FirebaseUserCallback)
    {
        SOPOApp.auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            // Firebase Email Login 실패
            if (!task.isSuccessful)
            {
                val exception = task.exception
                callback.invoke(
                    null, ErrorResult(
                        exception.match, exception?.message ?: "알 수 없는 에러",
                        ErrorResult.ERROR_TYPE_DIALOG, null, exception
                    )
                )
                return@addOnCompleteListener
            }
            else
            {
                // Firebase Email 인증 에러
                if (!task.result.user?.isEmailVerified!!)
                {
                    callback.invoke(
                        null, ErrorResult(
                            FIREBASE_ERROR_EMAIL_VERIFIED, FIREBASE_ERROR_EMAIL_VERIFIED.MSG,
                            ErrorResult.ERROR_TYPE_DIALOG, null, task.exception
                        )
                    )
                    return@addOnCompleteListener
                }
                else
                {
                    callback.invoke(SuccessResult(SUCCESS, "SUCCESS", task.result?.user), null)
                    return@addOnCompleteListener
                }
            }
        }

        SopoLog.d(tag = TAG, msg = "firebaseSelfLogin() call!!!")
    }

    /**
     * FCM 구독 요청
     * 등록 시 또는 앱 재설치 시 진행 중인 택배가 있을 시 구독 요청
     * TODO topic 주제 00 ~ 24H ex) 01:01 ~ 02:00 >>> 2시 구독
     */
    fun subscribedToTopicInFCM(hour: Int? = null, minutes: Int? = null, callback : FirebaseVoidCallback)
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

        SopoLog.d(tag = TAG, msg = "Topic >>> $topic")

        // 01:01 ~ => 01
        FirebaseMessaging.getInstance().subscribeToTopic(topic)
            .addOnCompleteListener { task ->
                if(!task.isSuccessful)
                {
                    callback.invoke(null, ErrorResult(null, "구독 실패", ErrorResult.ERROR_TYPE_DIALOG, null, task.exception))
                    return@addOnCompleteListener
                }

                userRepoImpl.setTopic(topic)
                callback.invoke(SuccessResult(SUCCESS, "구독 성공 >>> ${topic}", null), null)
            }
    }

    /**
     * FCM 구독 요청
     * 등록 시 또는 앱 재설치 시 진행 중인 택배가 있을 시 구독 요청
     * TODO topic 주제 00 ~ 24H ex) 01:01 ~ 02:00 >>> 2시 구독
     */
    fun unsubscribedToTopicInFCM(callback : FirebaseVoidCallback)
    {
        val topic = userRepoImpl.getTopic()

        if(topic == "")
        {
            callback.invoke(null, ErrorResult(null, "구독 해제 실패", ErrorResult.ERROR_TYPE_NON, null, null))
            return
        }

        FirebaseMessaging.getInstance().unsubscribeFromTopic(topic)
            .addOnCompleteListener { task ->
                if(!task.isSuccessful)
                {
                    callback.invoke(null, ErrorResult(null, "구독 해제 실패", ErrorResult.ERROR_TYPE_NON, null, task.exception))
                    return@addOnCompleteListener
                }

                userRepoImpl.setTopic("")
                callback.invoke(SuccessResult(SUCCESS, "구독 해제 성공 >>> $topic", null), null)
            }
    }
}
