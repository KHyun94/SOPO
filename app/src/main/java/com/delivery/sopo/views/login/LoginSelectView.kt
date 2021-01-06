package com.delivery.sopo.views.login

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.Observer
import com.delivery.sopo.R
import com.delivery.sopo.SOPOApp
import com.delivery.sopo.abstracts.BasicView
import com.delivery.sopo.databinding.LoginSelectViewBinding
import com.delivery.sopo.models.ErrorResult
import com.delivery.sopo.models.SopoJsonPatch
import com.delivery.sopo.models.api.APIResult
import com.delivery.sopo.networks.NetworkManager.publicRetro
import com.delivery.sopo.networks.api.UserAPI
import com.delivery.sopo.networks.dto.JsonPatchDto
import com.delivery.sopo.util.CodeUtil
import com.delivery.sopo.util.OtherUtil
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.util.ui_util.CustomProgressBar
import com.delivery.sopo.viewmodels.login.LoginSelectViewModel
import com.delivery.sopo.views.dialog.GeneralDialog
import com.delivery.sopo.views.main.MainView
import com.delivery.sopo.views.signup.SignUpView
import com.kakao.auth.ISessionCallback
import com.kakao.auth.Session
import com.kakao.util.exception.KakaoException
import kotlinx.android.synthetic.main.login_select_view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginSelectView : BasicView<LoginSelectViewBinding>(R.layout.login_select_view)
{
    private val loginSelectVm: LoginSelectViewModel by viewModel()

    private var sessionCallback: ISessionCallback? = null

    var progressBar: CustomProgressBar? = null

    init
    {
        TAG += this.javaClass.simpleName
        parentActivity = this@LoginSelectView
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        progressBar = CustomProgressBar(this@LoginSelectView)
    }

    override fun bindView()
    {
        binding.vm = loginSelectVm
        binding.lifecycleOwner = this
    }

    override fun setObserver()
    {
        loginSelectVm.loginType.observe(this, Observer {

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

                    if (Session.getCurrentSession() != null) Session.getCurrentSession()
                        .removeCallback(sessionCallback)

                    sessionCallback = object : ISessionCallback
                    {
                        override fun onSessionOpened()
                        {
                            binding.vm?.requestMe()
                        }

                        override fun onSessionOpenFailed(exception: KakaoException)
                        {
                            SopoLog.e(tag = TAG, msg = "카카오 세션 에러: ${exception}", e = exception)
                        }
                    }


                    Session.getCurrentSession().addCallback(sessionCallback)
                }
            }

        })

        binding.vm!!.successResult.observe(this, Observer {
            if (it == null) return@Observer

            when (it.first)
            {
                0 ->
                {
                    startActivity(Intent(parentActivity, MainView::class.java))
                    finish()
                }
                1 ->
                {
                    GeneralDialog(
                        act = this,
                        title = "오류",
                        msg = "중복 로그인 중\n로그인?",
                        detailMsg = "",
                        rHandler = Pair(first = "네", second = { it ->
                            updateDeviceInfo(binding.vm!!.email, binding.vm!!.deviceInfo)
                        })
                    ).show(supportFragmentManager, "error")
                }
            }
        })

        binding.vm!!.errorResult.observe(this, Observer {

            if (it == null) return@Observer

            when (it.errorType)
            {
                ErrorResult.ERROR_TYPE_TOAST ->
                {

                }
                ErrorResult.ERROR_TYPE_SNACK_BAR ->
                {

                }
                ErrorResult.ERROR_TYPE_DIALOG ->
                {
                    GeneralDialog(
                        act = this,
                        title = "오류",
                        msg = it.errorMsg,
                        detailMsg = "",
                        rHandler = Pair("네", null)
                    ).show(supportFragmentManager, "error")
                }
                ErrorResult.ERROR_TYPE_SCREEN ->
                {

                }
                else ->
                {

                }
            }
            SopoLog.e(tag = TAG, msg = it.toString())
        })
    }

    fun updateDeviceInfo(email: String, jwtToken: String)
    {
        val jsonPatchList = mutableListOf<SopoJsonPatch>()
        jsonPatchList.add(
            SopoJsonPatch(
                "replace",
                "/deviceInfo",
                OtherUtil.getDeviceID(SOPOApp.INSTANCE)
            )
        )

        publicRetro.create(UserAPI::class.java)
//            .requestTmpDeviceInfo(email, jwtToken)
            .patchUser(
                email = email,
                jwtToken = jwtToken,
                jsonPatch = JsonPatchDto(jsonPatchList)
            )
            .enqueue(object : Callback<APIResult<String?>>
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
                            binding.vm!!.requestKakaoLogin(
                                email = binding.vm!!.email,
                                deviceInfo = binding.vm!!.deviceInfo,
                                kakaoUserId = binding.vm!!.kakaoUserId,
                                uid = binding.vm!!.firebaseUserId
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

    override fun onDestroy()
    {
        super.onDestroy()
        if (sessionCallback != null)
            Session.getCurrentSession().removeCallback(sessionCallback)
    }
}