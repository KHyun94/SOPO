package com.delivery.sopo.views.intro

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import androidx.viewpager.widget.ViewPager
import com.delivery.sopo.R
import com.delivery.sopo.databinding.IntroViewBinding
import com.delivery.sopo.databinding.SnackBarCustomBinding
import com.delivery.sopo.enums.SnackBarEnum
import com.delivery.sopo.interfaces.listener.OnIntroClickListener
import com.delivery.sopo.interfaces.listener.OnPermissionRequestListener
import com.delivery.sopo.models.base.BaseView
import com.delivery.sopo.util.PermissionUtil
import com.delivery.sopo.util.ui_util.CustomSnackBar
import com.delivery.sopo.viewmodels.IntroViewModel
import com.delivery.sopo.views.adapter.IntroPageAdapter
import com.delivery.sopo.views.dialog.GeneralDialog
import com.delivery.sopo.views.login.LoginSelectView
import com.google.android.material.snackbar.Snackbar
import org.koin.androidx.viewmodel.ext.android.viewModel

class IntroView: BaseView<IntroViewBinding, IntroViewModel>()
{
    override val layoutRes: Int = R.layout.intro_view
    override val vm: IntroViewModel by viewModel()

    var numOfPage = 0
    var lastIndexOfPage = 0

    override fun initUI()
    {
        super.initUI()

        setViewPager()
        setListener()

        CustomSnackBar.make(view = binding.linearIntro,
                            content = "알 수 없는 서버 에러입니다.",
                            duration = 3000,
                            type = SnackBarEnum.ERROR).show()
    }

    private fun setViewPager(){


        val introPageAdapter = IntroPageAdapter(this, object: OnIntroClickListener{
            override fun onIntroClicked()
            {
                PermissionUtil.requestPermission(this@IntroView, object: OnPermissionRequestListener{
                    override fun onPermissionGranted()
                    {
                        val intent = Intent(this@IntroView, LoginSelectView::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        startActivity(intent)
                    }

                    override fun onPermissionDenied()
                    {
                        // NOT PERMISSION GRANT
                        GeneralDialog(act = this@IntroView,
                                      title = getString(R.string.DIALOG_ALARM),
                                      msg = getString(R.string.DIALOG_PERMISSION_REQ_MSG),
                                      detailMsg = null,
                                      rHandler = Pair(first = getString(R.string.DIALOG_OK), second = { it ->
                                          it.dismiss()
                                          finish()
                                      })).show(supportFragmentManager, "permission")
                    }
                })
            }
        })

        binding.viewPager.adapter = introPageAdapter

        binding.indicator.createDotPanel(cnt = binding.viewPager.adapter?.count ?: 0,
                                         defaultCircle = R.drawable.indicator_default_dot,
                                         selectCircle = R.drawable.indicator_select_dot, pos = 0)

        lastIndexOfPage = introPageAdapter.count - 1
    }

    fun setButtonVisible(position: Int){

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

    private fun setListener(){

        val onPageChangeListener = object: ViewPager.OnPageChangeListener
        {
            override fun onPageScrollStateChanged(state: Int) {

            }
            override fun onPageScrolled(pos: Int, posOffset: Float, posOffsetPixels: Int) {}
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


