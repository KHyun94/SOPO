package com.delivery.sopo.views

import android.content.Intent
import android.util.Log
import androidx.lifecycle.Observer
import com.delivery.sopo.util.ui_util.GeneralDialog
import com.delivery.sopo.R
import com.delivery.sopo.SOPOApp
import com.delivery.sopo.databinding.LoginSelectViewBinding
import com.delivery.sopo.enums.ResponseCode
import com.delivery.sopo.extentions.getCommonMessage
import com.delivery.sopo.interfaces.BasicView
import com.delivery.sopo.networks.NetworkManager.getUserAPI
import com.delivery.sopo.repository.UserRepo
import com.delivery.sopo.util.fun_util.CodeUtil
import com.delivery.sopo.util.fun_util.OtherUtil
import com.delivery.sopo.viewmodels.LoginSelectViewModel
import com.google.android.gms.tasks.OnCompleteListener
import com.kakao.auth.ISessionCallback
import com.kakao.auth.Session
import com.kakao.usermgmt.response.MeV2Response
import com.kakao.util.exception.KakaoException
import io.reactivex.schedulers.Schedulers.io
import kotlinx.android.synthetic.main.login_select_view.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class LoginSelectView : BasicView<LoginSelectViewBinding>(R.layout.login_select_view)
{

    private val userRepo : UserRepo by inject()

    private val loginSelectVM: LoginSelectViewModel by viewModel()
    private var sessionCallback: ISessionCallback? = null

    init
    {
        TAG += this.javaClass.simpleName
        parentActivity = this@LoginSelectView
    }

    override fun bindView()
    {
        binding.vm = loginSelectVM
        binding.lifecycleOwner = this
    }

    override fun setObserver()
    {
        loginSelectVM.loginType.observe(this, Observer {

            Log.d(TAG, "클릭 이벤트: $it")

            when (it)
            {
                "LOGIN" ->
                {
                    startActivity(
                        Intent(parentActivity, LoginView::class.java)
                    )
                    finish()
                }

                "SIGN_UP" ->
                {
                    startActivity(
                        Intent(parentActivity, SignUpView::class.java)
                    )
                    finish()
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
                                    Log.d(TAG, "카카오 정보: ${it.id}, ${it.kakaoAccount.email}")

                                    val kakaoEmail = it.kakaoAccount.email

                                    getUserAPI().requestCustomToken(
                                        deviceInfo = OtherUtil.getDeviceID(this@LoginSelectView),
                                        email = kakaoEmail,
                                        joinType = "kakao",
                                        userId = it.id.toString()
                                    )
                                        .subscribeOn(io())
                                        .subscribe(
                                            {
                                                if (it.code == ResponseCode.SUCCESS.CODE)
                                                {
                                                    val token = it.data!!
                                                    SOPOApp.auth.signInWithCustomToken(token)
                                                        .addOnCompleteListener {
                                                            if (it.isSuccessful)
                                                            {
                                                                Log.d(TAG, "토큰 로그인 성공")
                                                                SOPOApp.auth.currentUser?.updateEmail(kakaoEmail)
                                                                requestKakaoLogin(email = kakaoEmail, token = token)
                                                            }
                                                            else
                                                            {
                                                                GeneralDialog(
                                                                    parentActivity,
                                                                    "오류",
                                                                    it.exception.getCommonMessage(this@LoginSelectView),
                                                                    null,
                                                                    Pair("네", { it ->
                                                                        it.dismiss()
                                                                    })
                                                                ).show(
                                                                    supportFragmentManager.beginTransaction(),
                                                                    "TAG"
                                                                )
                                                            }
                                                        }
                                                }
                                                else
                                                {
                                                    GeneralDialog(
                                                        parentActivity,
                                                        "오류",
                                                        CodeUtil.returnCodeMsg(it.code),
                                                        null,
                                                        Pair("네", { it ->
                                                            it.dismiss()
                                                        })
                                                    ).show(
                                                        supportFragmentManager.beginTransaction(),
                                                        "TAG"
                                                    )
                                                }

                                                Log.d(TAG, "카카오 토큰: $it")



                                            },
                                            {
                                                Log.d(TAG, "카카오 토큰 에러: $it")
                                            }
                                        )

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

    private fun firebaseTokenLogin(token: String)
    {
        SOPOApp.auth.signInWithCustomToken(token)
            .addOnCompleteListener(parentActivity!!, OnCompleteListener {
                Log.d(TAG, "커스텀 토큰 로그인")
                val user = SOPOApp.auth.currentUser
                Log.d(TAG, "${user?.email}")
            })
    }


    fun requestKakaoLogin(email: String, token: String)
    {
        getUserAPI()
            .requestKakaoLogin(email = email, token = token)
            .subscribeOn(io())
            .subscribe({
                Log.d(TAG, "$it")

                if(it.code == ResponseCode.SUCCESS.CODE)
                {
                    userRepo.setEmail(email = it.data?.userName!!)
                    userRepo.setApiPwd(pwd = it.data?.password!!)
                    userRepo.setDeviceInfo(info = OtherUtil.getDeviceID(this@LoginSelectView))
                    userRepo.setJoinType(joinType = "kakao")
                    userRepo.setRegisterDate(it.data?.regDt!!)
                    userRepo.setStatus(it.data?.status!!)

                    startActivity(Intent(parentActivity, MainView::class.java))
                    finish()
                } else {
                    GeneralDialog(
                        parentActivity,
                        "오류",
                        CodeUtil.returnCodeMsg(it.code),
                        null,
                        Pair("네", { it ->
                            it.dismiss()
                        })
                    ).show(
                        supportFragmentManager.beginTransaction(),
                        "TAG"
                    )
                }
            }, {
                GeneralDialog(
                    parentActivity,
                    "오류",
                    it.message!!,
                    null,
                    Pair("네", { it ->
                        it.dismiss()
                    })
                ).show(
                    supportFragmentManager.beginTransaction(),
                    "TAG"
                )
            })
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