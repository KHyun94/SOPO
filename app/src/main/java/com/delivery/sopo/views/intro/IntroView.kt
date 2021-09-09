package com.delivery.sopo.views.intro

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.viewpager.widget.ViewPager
import com.delivery.sopo.BR
import com.delivery.sopo.R
import com.delivery.sopo.databinding.IntroViewBinding
import com.delivery.sopo.viewmodels.IntroViewModel
import com.delivery.sopo.views.adapter.IntroPageAdapter
import org.koin.androidx.viewmodel.ext.android.viewModel


class IntroView: AppCompatActivity()
{
    var numOfPage = 0
    var lastIndexOfPage = 0

    lateinit var binding: IntroViewBinding
    private val vm: IntroViewModel by viewModel()

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        requestedOrientation = SCREEN_ORIENTATION_PORTRAIT

        binding = bindView(activity = this, layoutId = R.layout.intro_view, vm = vm)

        setViewPager()
        setViewEvent()
    }

    fun <T : ViewDataBinding> bindView(activity: FragmentActivity, @LayoutRes layoutId : Int, vm: ViewModel) : T
    {
        return DataBindingUtil.setContentView<T>(activity,layoutId).apply{
            this.lifecycleOwner = lifecycleOwner
            this.setVariable(BR.vm, vm)
            executePendingBindings()
        }
    }

    private fun setViewPager(){
        val introPageAdapter = IntroPageAdapter(this)
        binding.viewPager.adapter = introPageAdapter

        binding.indicator.createDotPanel(cnt = binding.viewPager.adapter?.count?:0,
                                         defaultCircle = R.drawable.indicator_default_dot,
                                         selectCircle = R.drawable.indicator_select_dot,
                                         pos = 0)

        lastIndexOfPage = introPageAdapter.count - 1
    }

    fun setButtonVisible(position:Int){

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

    fun setViewEvent(){

        binding.viewPager.addOnPageChangeListener(object: ViewPager.OnPageChangeListener
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
                                          })

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


