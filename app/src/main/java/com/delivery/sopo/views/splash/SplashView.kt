package com.delivery.sopo.views.splash

import android.content.Intent
import android.os.Handler
import androidx.lifecycle.Observer
import com.delivery.sopo.R
import com.delivery.sopo.abstracts.BasicView
import com.delivery.sopo.consts.NavigatorConst
import com.delivery.sopo.consts.PermissionConst
import com.delivery.sopo.data.repository.local.o_auth.OAuthLocalRepository
import com.delivery.sopo.databinding.SplashViewBinding
import com.delivery.sopo.data.repository.local.user.UserLocalRepository
import com.delivery.sopo.data.repository.remote.user.UserRemoteRepository
import com.delivery.sopo.models.mapper.OAuthMapper
import com.delivery.sopo.util.AlertUtil
import com.delivery.sopo.util.DateUtil
import com.delivery.sopo.util.PermissionUtil
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.viewmodels.splash.SplashViewModel
import com.delivery.sopo.views.dialog.GeneralDialog
import com.delivery.sopo.views.dialog.PermissionDialog
import com.delivery.sopo.views.intro.IntroView
import com.delivery.sopo.views.main.MainView
import com.delivery.sopo.views.signup.RegisterNicknameView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class SplashView: BasicView<SplashViewBinding>(layoutRes = R.layout.splash_view)
{

    private val userLocalRepo: UserLocalRepository by inject()
    private val userRemoteRepo: UserRemoteRepository by inject()
    private val oAuthRepo: OAuthLocalRepository by inject()
    private val vm: SplashViewModel by viewModel()
    lateinit var permissionDialog: PermissionDialog

    init
    {
        parentActivity = this@SplashView
    }

    override fun bindView()
    {
        binding.vm = vm
        binding.lifecycleOwner = this
        binding.executePendingBindings()
    }

    override fun setObserver()
    {
        Handler().postDelayed(Runnable { moveToActivity() }, 1500)
    }

    private fun moveToActivity()
    {
        vm.navigator.observe(this, Observer {
            when(it)
            {
                NavigatorConst.TO_PERMISSION ->
                {
                    if(!PermissionUtil.isPermissionGranted(this, *PermissionConst.PERMISSION_ARRAY))
                    {
                        permissionDialog = PermissionDialog(act = parentActivity) { dialog ->

                            PermissionUtil.permissionCallback(parentActivity, *PermissionConst.PERMISSION_ARRAY) { isGranted ->

                                if(!isGranted)
                                {
                                    SopoLog.e("권한 비허가 상태")

                                    // NOT PERMISSION GRANT
                                    GeneralDialog(act = parentActivity,
                                                  title = getString(R.string.DIALOG_ALARM),
                                                  msg = getString(
                                                      R.string.DIALOG_PERMISSION_REQ_MSG),
                                                  detailMsg = null, rHandler = Pair(
                                            first = getString(R.string.DIALOG_OK), second = { it ->
                                                it.dismiss()
                                                finish()
                                            })).show(supportFragmentManager, "permission")

                                    return@permissionCallback
                                }

                                SopoLog.d("권한 허가 상태")
                                // permission all clear
                                CoroutineScope(Dispatchers.IO).launch {
                                    refreshTokenWithinWeek()
                                    vm.getUserInfoWithToken()
                                }

                            }

                            dialog.dismiss()
                        }

                        permissionDialog.show(supportFragmentManager, "PermissionTag")

                        return@Observer
                    }

                    SopoLog.d("권한 허가 상태")
                    // permission all clear
                    CoroutineScope(Dispatchers.IO).launch {
                        refreshTokenWithinWeek()
                        vm.getUserInfoWithToken()
                    }
                }
                NavigatorConst.TO_INTRO ->
                {
                    startActivity(Intent(parentActivity, IntroView::class.java))
                    finish()
                }
                NavigatorConst.TO_MAIN ->
                {
                    goToMainOrNickname(userLocalRepo.getNickname())
                }
                NavigatorConst.TO_INIT ->
                {
                    CoroutineScope(Dispatchers.Main).launch {
                        AlertUtil.alertExpiredToken(this@SplashView, vm.errorMessage)
                    }
                }
            }
        })
    }

    // 정상적으로 로그인했을 때 닉네임의 여부에 따라 UpdateNicknameView or MainView로 이동
    private fun goToMainOrNickname(nickname: String)
    {
        val clz = if(nickname == "") RegisterNicknameView::class.java else MainView::class.java

        Intent(this@SplashView, clz).let {intent ->
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            finish()
        }
    }

    /**
     * 토큰 만료일 기준 1주일 내외 일 때
     * 토큰을 새로 요청함
     */
    private suspend fun refreshTokenWithinWeek(){
        SopoLog.i("refreshTokenWithinWeek() 호출")

        // 로컬 내 oAuth Token의 만료 기일을 로드
        val currentExpiredDate = withContext(Dispatchers.Default) {
            oAuthRepo.get(userLocalRepo.getUserId()).run {
                val dto = OAuthMapper.entityToObject(this?:throw Exception("O AUTH DATA IS NULL"))
                dto.refreshTokenExpiredAt
            }
        }

        SopoLog.d("O-Auth Token Expired Date(갱신 전) [data:$currentExpiredDate]")

        val isOverDate = DateUtil.isExpiredDateWithinAWeek(currentExpiredDate)
        if(!isOverDate) return SopoLog.d("O-Auth Token Expired Date 만료 전 상태")

        val res = userRemoteRepo.requestLogin(userLocalRepo.getUserId(), userLocalRepo.getUserPassword())

        if(!res.result) { return SopoLog.e("O-Auth Token Expired Date 갱신 실패 [code:${res.code}] [message:${res.message}]") }

        res.data?.let {
            val entity = OAuthMapper.objectToEntity(it)
            oAuthRepo.update(entity)
            SopoLog.d("O-Auth Token Expired Date 갱신 성공 [data:${it.refreshTokenExpiredAt}]")
        }
    }

}
