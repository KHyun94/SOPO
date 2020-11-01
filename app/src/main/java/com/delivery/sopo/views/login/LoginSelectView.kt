package com.delivery.sopo.views.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.Observer
import com.delivery.sopo.views.dialog.GeneralDialog
import com.delivery.sopo.R
import com.delivery.sopo.SOPOApp
import com.delivery.sopo.consts.JoinTypeConst
import com.delivery.sopo.databinding.LoginSelectViewBinding
import com.delivery.sopo.enums.ResponseCodeEnum
import com.delivery.sopo.firebase.FirebaseUserManagement
import com.delivery.sopo.abstracts.BasicView
import com.delivery.sopo.models.api.APIResult
import com.delivery.sopo.models.LoginResult
import com.delivery.sopo.models.SopoJsonPatch
import com.delivery.sopo.networks.api.LoginAPI
import com.delivery.sopo.networks.NetworkManager
import com.delivery.sopo.networks.NetworkManager.publicRetro
import com.delivery.sopo.networks.api.UserAPI
import com.delivery.sopo.networks.dto.JsonPatchDto
import com.delivery.sopo.repository.impl.UserRepoImpl
import com.delivery.sopo.util.CodeUtil
import com.delivery.sopo.util.OtherUtil
import com.delivery.sopo.util.ui_util.CustomProgressBar
import com.delivery.sopo.viewmodels.login.LoginSelectViewModel
import com.delivery.sopo.views.main.MainView
import com.delivery.sopo.views.signup.SignUpView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kakao.auth.ISessionCallback
import com.kakao.auth.Session
import com.kakao.usermgmt.response.MeV2Response
import com.kakao.util.exception.KakaoException
import kotlinx.android.synthetic.main.login_select_view.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginSelectView : BasicView<LoginSelectViewBinding>(R.layout.login_select_view)
{
    private val userRepoImpl : UserRepoImpl by inject()

    private val loginSelectVM: LoginSelectViewModel by viewModel()
    private var sessionCallback: ISessionCallback? = null

    var email = ""
    var deviceInfo = ""
    var kakaoUserId = ""
    var firebaseUserId = ""

    var progressBar : CustomProgressBar? = null

    init
    {
        TAG += this.javaClass.simpleName
        parentActivity = this@LoginSelectView
        deviceInfo = OtherUtil.getDeviceID(SOPOApp.INSTANCE)
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        progressBar = CustomProgressBar(this@LoginSelectView)
    }

    override fun bindView()
    {
        binding.vm = loginSelectVM
        binding.lifecycleOwner = this
    }

    override fun setObserver()
    {
        loginSelectVM.loginType.observe(this, Observer {

            when (it)
            {
                "LOGIN" ->
                {
                    startActivity(
                        Intent(parentActivity, LoginView::class.java)
                    )
                }

                "SIGN_UP" ->
                {
                    startActivity(
                        Intent(parentActivity, SignUpView::class.java)
                    )
                }
                "KAKAO_LOGIN" ->
                {
                    btn_kakao_login.performClick()

                    if (Session.getCurrentSession() != null)
                        Session.getCurrentSession().removeCallback(sessionCallback)

                    sessionCallback = object : ISessionCallback
                    {
                        override fun onSessionOpened()
                        {
                            binding.vm?.requestMe {
                                if (it is MeV2Response)
                                {
                                    progressBar!!.onStartDialog()

                                    email = it.kakaoAccount.email
                                    kakaoUserId = it.id.toString()

                                    requestKakaoCustomToken(email = email, uid = kakaoUserId)
                                }
                                else
                                {
                                    Log.d(TAG, "카카오 에러: ${it}")
                                }
                            }
                        }

                        override fun onSessionOpenFailed(exception: KakaoException)
                        {
                            Log.d(TAG, "카카오 세션 에러: ${exception}")
                        }
                    }


                    Session.getCurrentSession().addCallback(sessionCallback)
                }
            }

        })
    }

    fun requestKakaoCustomToken(email: String, uid: String)
    {
        this.email = email

        NetworkManager.publicRetro.create(UserAPI::class.java)
            .requestCustomToken(
                email = email,
                deviceInfo = deviceInfo,
                joinType = JoinTypeConst.KAKAO,
                userId = uid
            ).enqueue(object : Callback<APIResult<String?>>
            {
                override fun onFailure(call: Call<APIResult<String?>>, t: Throwable)
                {
                    progressBar!!.onCloseDialog()

                    GeneralDialog(
                        act = parentActivity,
                        title = "오류",
                        msg = t.message.toString(),
                        detailMsg = null,
                        rHandler = Pair(
                            first = "네",
                            second = { it ->
                                it.dismiss()
                            })
                    ).show(supportFragmentManager, "tag")
                }

                override fun onResponse(
                    call: Call<APIResult<String?>>,
                    response: Response<APIResult<String?>>
                )
                {


                    val httpStatusCode = response.code()

                    val result = response.body()

                    when (httpStatusCode)
                    {
                        200 ->
                        {
                            if (result?.code == ResponseCodeEnum.SUCCESS.CODE)
                            {
                                val customToken = result.data as String

                                FirebaseUserManagement.firebaseCustomTokenLogin(token = customToken)
                                    .addOnCompleteListener {

                                        Log.d(TAG, "kakao ${it.result.user?.email}")

                                        firebaseUserId = it.result?.user?.uid!!


                                        it.result?.user?.updateEmail(email)
                                            ?.addOnCompleteListener {

                                                Log.d(TAG, "Firebase!!!!!!!!!!!!${firebaseUserId}")


                                                requestKakaoLogin(
                                                    email = email,
                                                    deviceInfo = deviceInfo,
                                                    kakaoUserId = kakaoUserId,
                                                    uid = firebaseUserId
                                                )
                                            }
                                    }
                            }
                            else
                            {
                                progressBar!!.onCloseDialog()
                                Log.d(TAG, "error code ${result?.code}")

                                GeneralDialog(
                                    act = parentActivity,
                                    title = "알림",
                                    msg = CodeUtil.returnCodeMsg(result?.code),
                                    detailMsg = null,
                                    rHandler = Pair(
                                        first = "네",
                                        second = { it ->
                                            it.dismiss()
                                        })
                                ).show(supportFragmentManager, "tag")
                            }
                        }
                        else ->
                        {
                            progressBar!!.onCloseDialog()
                            Log.d(TAG, "error code ${result?.code}")

                            GeneralDialog(
                                act = parentActivity,
                                title = "오류",
                                msg = CodeUtil.returnCodeMsg(result?.code),
                                detailMsg = null,
                                rHandler = Pair(
                                    first = "네",
                                    second = { it ->
                                        it.dismiss()
                                    })
                            ).show(supportFragmentManager, "tag")
                        }

                    }

                }

            })

    }

    fun requestKakaoLogin(email: String, deviceInfo: String, kakaoUserId: String, uid: String)
    {
        publicRetro.create(LoginAPI::class.java)
            .requestKakaoLogin(
                email = email,
                deviceInfo = deviceInfo,
                kakaoUserId = kakaoUserId,
                uid = uid
            ).enqueue(object : Callback<APIResult<Any?>>
            {
                override fun onFailure(call: Call<APIResult<Any?>>, t: Throwable)
                {
                    progressBar!!.onCloseDialog()
                }

                override fun onResponse(
                    call: Call<APIResult<Any?>>,
                    response: Response<APIResult<Any?>>
                )
                {
                    progressBar!!.onCloseDialog()

                    val httpStatusCode = response.code()

                    val result = response.body()

                    when (httpStatusCode)
                    {

                        ResponseCodeEnum.SUCCESS.HTTP_STATUS ->
                        {
                            when (result?.code)
                            {
                                ResponseCodeEnum.SUCCESS.CODE ->
                                {
                                    val gson = Gson()

                                    val type = object : TypeToken<LoginResult?>() {}.type

                                    val reader = gson.toJson(result.data)

                                    val user = gson.fromJson<LoginResult>(reader, type)

                                    userRepoImpl.setEmail(email = user.userName)
                                    userRepoImpl.setApiPwd(pwd = user.password)
                                    userRepoImpl.setDeviceInfo(info = deviceInfo)
                                    userRepoImpl.setJoinType(joinType = JoinTypeConst.KAKAO)
                                    userRepoImpl.setRegisterDate(user.regDt)
                                    userRepoImpl.setStatus(user.status)
                                    userRepoImpl.setSNSUId(kakaoUserId)

                                    startActivity(Intent(parentActivity, MainView::class.java))
                                    finish()
                                }
                                ResponseCodeEnum.ALREADY_LOGGED_IN.CODE ->
                                {
                                    val jwtToken = result.data as String

                                    GeneralDialog(
                                        act = parentActivity,
                                        title = "알림",
                                        msg = ResponseCodeEnum.ALREADY_LOGGED_IN.MSG,
                                        detailMsg = null,
                                        rHandler = Pair(
                                            first = "네",
                                            second = { it ->
                                                it.dismiss()
                                                updateDeviceInfo(jwtToken = jwtToken, email = email)
                                            })
                                    ).show(supportFragmentManager, "tag")
                                }
                                else ->
                                {
                                    GeneralDialog(
                                        act = parentActivity,
                                        title = "오류",
                                        msg = CodeUtil.returnCodeMsg(result?.code),
                                        detailMsg = null,
                                        rHandler = Pair(
                                            first = "네",
                                            second = { it ->
                                                it.dismiss()
                                            })
                                    ).show(supportFragmentManager, "tag")
                                }
                            }
                        }
                        else ->
                        {
                            GeneralDialog(
                                act = parentActivity,
                                title = "알림",
                                msg = CodeUtil.returnCodeMsg(result?.code),
                                detailMsg = null,
                                rHandler = Pair(
                                    first = "네",
                                    second = { it ->
                                        it.dismiss()
                                    })
                            ).show(supportFragmentManager, "tag")

                        }
                    }

                }

            })
    }

    fun updateDeviceInfo(email: String, jwtToken: String)
    {
        val jsonPatchList = mutableListOf<SopoJsonPatch>()
        jsonPatchList.add(SopoJsonPatch("replace", "/deviceInfo", OtherUtil.getDeviceID(SOPOApp.INSTANCE)))

        publicRetro.create(UserAPI::class.java)
            .patchUser(
                email = email,
                jwt = jwtToken,
                jsonPatch = JsonPatchDto(jsonPatchList)
            ).enqueue(object : Callback<APIResult<String?>>
            {
                override fun onFailure(call: Call<APIResult<String?>>, t: Throwable)
                {
                    GeneralDialog(
                        act = parentActivity,
                        title = "오류",
                        msg = CodeUtil.returnCodeMsg(t.message),
                        detailMsg = null,
                        rHandler = Pair(
                            first = "네",
                            second = { it -> it.dismiss() })
                    ).show(supportFragmentManager, "tag")
                }

                override fun onResponse(
                    call: Call<APIResult<String?>>,
                    response: Response<APIResult<String?>>
                )
                {
                    val httpStatusCode = response.code()

                    val result = response.body()

                    when (httpStatusCode)
                    {
                        200 ->
                        {
                            requestKakaoLogin(
                                email = email,
                                deviceInfo = deviceInfo,
                                kakaoUserId = kakaoUserId,
                                uid = firebaseUserId
                            )
                        }
                        else ->
                        {
                            GeneralDialog(
                                act = parentActivity,
                                title = "오류",
                                msg = CodeUtil.returnCodeMsg(result?.code),
                                detailMsg = null,
                                rHandler = Pair(
                                    first = "네",
                                    second = { it -> it.dismiss() })
                            ).show(supportFragmentManager, "tag")
                        }
                    }

                }

            }
            )

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    {
        if (Session.getCurrentSession().handleActivityResult(requestCode, resultCode, data))
        {
            return;
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

//    override fun onBackPressed()
//    {
//        super.onBackPressed()
//    }


    override fun onDestroy()
    {
        super.onDestroy()
        if (sessionCallback != null)
            Session.getCurrentSession().removeCallback(sessionCallback)
    }
}