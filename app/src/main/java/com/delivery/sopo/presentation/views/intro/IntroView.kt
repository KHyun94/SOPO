package com.delivery.sopo.presentation.views.intro

import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.viewpager.widget.ViewPager
import com.delivery.sopo.R
import com.delivery.sopo.presentation.consts.NavigatorConst
import com.delivery.sopo.databinding.IntroViewBinding
import com.delivery.sopo.extensions.makeGone
import com.delivery.sopo.extensions.makeVisible
import com.delivery.sopo.extensions.moveToActivityWithFinish
import com.delivery.sopo.interfaces.listener.OnIntroClickListener
import com.delivery.sopo.models.base.BaseView
import com.delivery.sopo.models.base.OnActivityResultCallbackListener
import com.delivery.sopo.util.PermissionUtil
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.presentation.viewmodels.IntroViewModel
import com.delivery.sopo.presentation.views.adapter.IntroPageAdapter
import com.delivery.sopo.presentation.login.LoginSelectView
import org.koin.androidx.viewmodel.ext.android.viewModel

class IntroView: BaseView<IntroViewBinding, IntroViewModel>()
{
    override val layoutRes: Int = R.layout.intro_view
    override val vm: IntroViewModel by viewModel()
    override val mainLayout: View by lazy { binding.linearIntro }

    var numOfPage = 0
    var lastIndexOfPage = 0

    var isNotificationListener: Boolean = false

    private val onIntroClickListener = object: OnIntroClickListener
    {
        override fun onIntroSettingClicked(isNow: Boolean)
        {
            SopoLog.d("onIntroSettingClicked(...) 호출")

            if(!isNow)
            {
                return vm.postNavigator(NavigatorConst.Screen.LOGIN_SELECT)
            }

            launchNotificationSetting()
        }
    }

    fun launchNotificationSetting()
    {
        setOnActivityResultCallbackListener(object: OnActivityResultCallbackListener{
            override fun callback(activityResult: ActivityResult) {
                val isNotificationSetting = PermissionUtil.checkNotificationListenerPermission(this@IntroView, packageName)

                if(!isNotificationSetting)
                {
                    // 노티피케이션 설정을 하지 않았을 경우
                    Toast.makeText(this@IntroView, "노티피케이션 설정이 되지 않았어요.", Toast.LENGTH_SHORT).show()
                    return
                }

                vm.postNavigator(NavigatorConst.Screen.LOGIN_SELECT)
            }
        })

        val settingIntent = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
        {
            Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        }
        else
        {
            Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
        }

        launchActivityResult(settingIntent)
    }

    override fun setObserve()
    {
        super.setObserve()

        vm.navigator.observe(this) { navigator ->

            when(navigator)
            {
                NavigatorConst.Screen.LOGIN_SELECT ->
                {
                    moveToActivityWithFinish(LoginSelectView::class.java, Intent.FLAG_ACTIVITY_CLEAR_TASK)
                }
            }

        }



    }

    override fun onAfterBinding()
    {
        super.onAfterBinding()

        setViewPager()
        setListener()

        /*val onGlobalLayoutListener = ViewTreeObserver.OnGlobalLayoutListener { // 'year spinner'높이 수치만큼 'month sector'의 상단 공백을 생성

            val adapter = binding.viewPager.adapter as IntroPageAdapter
            val height = adapter.bottomView.height

            Toast.makeText(this@IntroView, "Height $height", Toast.LENGTH_SHORT).show()

            *//*val yearSpinnerHeight: Int = binding.linearMainYearSpinner.height

                        (binding.linearMainMonthSelector.layoutParams as FrameLayout.LayoutParams).apply {
                            topMargin = yearSpinnerHeight
                        }

                        // 'year spinner'높이 수치만큼 'completed space'의 상단 공백을 생성
                        // 'month sector'높이 수치만큼 'completed space'의 높이변경
                        val monthSelectorHeight = binding.linearMainMonthSelector.height

                        (binding.vInnerCompletedSpace.layoutParams as LinearLayout.LayoutParams).apply {
                            this.topMargin = yearSpinnerHeight
                            this.height = monthSelectorHeight
                        }

                        // 뷰 조절 후 옵저빙 리스너 제거
                        binding.frameMainCompleteInquiry.viewTreeObserver.removeOnGlobalLayoutListener(this)*//*
        }

        binding.linearIntro.viewTreeObserver.addOnGlobalLayoutListener(onGlobalLayoutListener)*/
    }

    private fun setViewPager()
    {

        val introPageAdapter = IntroPageAdapter(this, onIntroClickListener)

        binding.viewPager.adapter = introPageAdapter

        binding.indicator.createDotPanel(cnt = binding.viewPager.adapter?.count ?: 0, defaultCircle = R.drawable.indicator_default_dot, selectCircle = R.drawable.indicator_select_dot, pos = 0)

        lastIndexOfPage = introPageAdapter.count - 1
    }

    fun setButtonVisible(position: Int)
    {

        when(position)
        {
            0 ->
            {
                binding.ivPreviousPage.makeGone()
                binding.ivNextPage.makeVisible()
            }
            1 ->
            {
                binding.ivPreviousPage.makeVisible()
                binding.ivNextPage.makeVisible()
            }
            2 ->
            {
                binding.ivPreviousPage.makeVisible()
                binding.ivNextPage.makeGone()
            }
        }
    }

    private fun setListener()
    {

        val onPageChangeListener = object: ViewPager.OnPageChangeListener
        {
            override fun onPageScrollStateChanged(state: Int) { }

            override fun onPageScrolled(pos: Int, posOffset: Float, posOffsetPixels: Int) { }

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
    }
}


