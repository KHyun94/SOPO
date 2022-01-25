package com.delivery.sopo.views.intro

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.viewpager.widget.ViewPager
import com.delivery.sopo.R
import com.delivery.sopo.consts.PermissionConst
import com.delivery.sopo.databinding.IntroViewBinding
import com.delivery.sopo.extensions.moveToActivity
import com.delivery.sopo.interfaces.listener.OnIntroClickListener
import com.delivery.sopo.interfaces.listener.OnPermissionResponseCallback
import com.delivery.sopo.models.base.BaseView
import com.delivery.sopo.models.base.TestListener
import com.delivery.sopo.util.PermissionUtil
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.viewmodels.IntroViewModel
import com.delivery.sopo.views.adapter.IntroPageAdapter
import com.delivery.sopo.views.dialog.GeneralDialog
import com.delivery.sopo.views.login.LoginSelectView
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import org.koin.androidx.viewmodel.ext.android.viewModel

class IntroView: BaseView<IntroViewBinding, IntroViewModel>()
{
    override val layoutRes: Int = R.layout.intro_view
    override val vm: IntroViewModel by viewModel()
    override val mainLayout: View by lazy { binding.linearIntro }

    var numOfPage = 0
    var lastIndexOfPage = 0

    var isNotificationListener: Boolean = false

    private val onPermissionResponseCallback = object: OnPermissionResponseCallback
    {
        override fun onPermissionGranted()
        {
            if(isNotificationListener)
            {
                vm.setNavigator("TEST")
                val settingIntent = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
                {
                    Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                }
                else
                {
                    Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
                }

                //                launchActivityResult(settingIntent, ActivityResultCallback<ActivityResult> { result ->
                //
                //                    val isConfirm = PermissionUtil.checkNotificationListenerPermission(this@IntroView, packageName)
                //
                //                    SopoLog.d("""
                //                                설정 결과값
                //                                resultCode: ${result.resultCode}
                //                                action: ${result.data?.action}
                //                                type: ${result.data?.type}
                //                                isConfirm: $isConfirm
                //                            """.trimIndent())
                //                })
            }
            else
            {
                moveToActivity(LoginSelectView::class.java, Intent.FLAG_ACTIVITY_CLEAR_TASK)
                finish()
            }

        }

        override fun onPermissionDenied()
        {
            // NOT PERMISSION GRANT
            GeneralDialog(act = this@IntroView, title = getString(R.string.DIALOG_ALARM), msg = getString(R.string.DIALOG_PERMISSION_REQ_MSG), detailMsg = null, rHandler = Pair(first = getString(R.string.DIALOG_OK), second = { dialog ->
                dialog.dismiss()
                finish()
            })).show(supportFragmentManager, "permission")
        }
    }

    private val onIntroClickListener = object: OnIntroClickListener
    {
        override fun onIntroSettingClicked(isNow: Boolean)
        {
            SopoLog.d("onIntroSettingClicked(...) 호출")

            val isPermissionGranted = PermissionUtil.isPermissionGranted(this@IntroView, *PermissionConst.PERMISSION_ARRAY)

            if(!isPermissionGranted)
            {
                SopoLog.d("기본 권한 체크 X")
                binding.layoutMain.panelState = SlidingUpPanelLayout.PanelState.EXPANDED
                return
            }

            SopoLog.d("기본 권한 체크 O")

            val settingIntent = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            {
                Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
            }
            else
            {
                Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
            }

//            testListener(object: TestListener{
//                override fun test(result: ActivityResult)
//                {
//                    SopoLog.d("""
//                                                설정 결과값
//                                                resultCode: ${result.resultCode}
//                                                action: ${result.data?.action}
//                                                type: ${result.data?.type}
//                                            """.trimIndent())
//                }
//
//            })

            launchActivityResult(settingIntent)
//            launchActivityResult(settingIntent, ActivityResultCallback<ActivityResult> { result ->
//
//                val isConfirm =
//                    PermissionUtil.checkNotificationListenerPermission(this@IntroView, packageName)
//
//                SopoLog.d("""
//                                                설정 결과값
//                                                resultCode: ${result.resultCode}
//                                                action: ${result.data?.action}
//                                                type: ${result.data?.type}
//                                                isConfirm: $isConfirm
//                                            """.trimIndent())
//            })
        }
        /*override fun onIntroSettingLater()
        {
            SopoLog.d("onIntroSettingLater() 호출")

            isNotificationListener = false

            val isGranted = PermissionUtil.isPermissionGranted(this@IntroView,  *PermissionConst.PERMISSION_ARRAY)

            if(!isGranted)
            {
                binding.layoutMain.panelState = SlidingUpPanelLayout.PanelState.EXPANDED
                return
            }

            PermissionUtil.permissionCallback(this@IntroView, *PermissionConst.PERMISSION_ARRAY) { isGranted ->

                if(!isGranted)
                {
                    finish()
                    return@permissionCallback
                }
            }

        }

        override fun onIntroSettingNow()
        {
            SopoLog.d("onIntroSettingNow() 호출")

            isNotificationListener = true

            val isGranted = PermissionUtil.isPermissionGranted(this@IntroView)

            SopoLog.d("권한 체크가 되지 ${PermissionConst.PERMISSION_ARRAY.toString()}")

            if(!isGranted)
            {
                SopoLog.d("권한 체크가 되지 않음")

                binding.layoutMain.panelState = SlidingUpPanelLayout.PanelState.EXPANDED
                return
            }

            SopoLog.d("권한 체크가 됨")

            val settingIntent = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            {
                Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
            }
            else
            {
                Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
            }

            launchActivityResult(settingIntent, ActivityResultCallback<ActivityResult> { result ->

                val isConfirm =
                    PermissionUtil.checkNotificationListenerPermission(this@IntroView, packageName)

                SopoLog.d("""
                                                설정 결과값
                                                resultCode: ${result.resultCode}
                                                action: ${result.data?.action}
                                                type: ${result.data?.type}
                                                isConfirm: $isConfirm
                                            """.trimIndent())
            })
//            PermissionUtil.permissionCallback(this@IntroView, *PermissionConst.PERMISSION_ARRAY) { isGranted ->
//
//                if(!isGranted)
//                {
//                    SopoLog.d("권한 체크 안됨")
//                    finish()
//                    return@permissionCallback
//                }
//
//                SopoLog.d("노티 권한 시작")
//
//
//
//
//            }
        }*/
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

    }

    override fun setObserve()
    {
        super.setObserve()

        vm.navigator.observe(this) {
            if(it == "TEST")
            {

            }
        }
    }

    override fun onAfterBinding()
    {
        super.onAfterBinding()

        setViewPager()
        setListener()
    }

    private fun setViewPager()
    {

        val introPageAdapter = IntroPageAdapter(this, onIntroClickListener)

        binding.viewPager.adapter = introPageAdapter

        binding.indicator.createDotPanel(cnt = binding.viewPager.adapter?.count
            ?: 0, defaultCircle = R.drawable.indicator_default_dot, selectCircle = R.drawable.indicator_select_dot, pos = 0)

        lastIndexOfPage = introPageAdapter.count - 1
    }

    fun setButtonVisible(position: Int)
    {

        when(position)
        {
            0 ->
            {
                binding.ivPreviousPage.visibility = View.GONE
                binding.ivNextPage.visibility = View.VISIBLE
            }
            1 ->
            {
                binding.ivPreviousPage.visibility = View.VISIBLE
                binding.ivNextPage.visibility = View.VISIBLE
            }
            2 ->
            {
                binding.ivPreviousPage.visibility = View.VISIBLE
                binding.ivNextPage.visibility = View.GONE
            }
        }
    }

    private fun setListener()
    {

        val onPageChangeListener = object: ViewPager.OnPageChangeListener
        {
            override fun onPageScrollStateChanged(state: Int)
            {

            }

            override fun onPageScrolled(pos: Int, posOffset: Float, posOffsetPixels: Int)
            {
            }

            override fun onPageSelected(pos: Int)
            {
                numOfPage = pos

                // 해당 번호의 dot의 색상을 변경
                binding.indicator.selectDot(pos)

                // 현재 페이지의 수에 따라 상단바의 버튼의 가시성을 변경
                setButtonVisible(position = pos)
            }
        }

        binding.viewPager.addOnPageChangeListener(onPageChangeListener)

        binding.ivPreviousPage.setOnClickListener {
            if(numOfPage == 0) return@setOnClickListener
            binding.viewPager.currentItem = --numOfPage
        }

        binding.ivNextPage.setOnClickListener {
            if(numOfPage == lastIndexOfPage) return@setOnClickListener
            binding.viewPager.currentItem = ++numOfPage
        }

        binding.tvPermissionCheck.setOnClickListener {
            binding.layoutMain.panelState = SlidingUpPanelLayout.PanelState.COLLAPSED
            PermissionUtil.requestPermission(this, onPermissionResponseCallback)
        }

    }
}


