package com.delivery.sopo.views.intro

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.delivery.sopo.R
import com.delivery.sopo.views.adapter.IntroPageAdapter
import com.delivery.sopo.views.login.LoginSelectView
import kotlinx.android.synthetic.main.intro_view.*

class IntroView : AppCompatActivity()
{
    var numOfPage = 0

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.intro_view)
        requestedOrientation = SCREEN_ORIENTATION_PORTRAIT

        var viewPager: ViewPager = findViewById(R.id.viewPager)
        var introPageAdapter: IntroPageAdapter = IntroPageAdapter(this)
        viewPager.adapter = introPageAdapter

        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener
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

                indicator.selectDot(pos)
            }
        })

        indicator.createDotPanel(
            viewPager.adapter!!.count,
            R.drawable.indicator_default_dot,
            R.drawable.indicator_select_dot,
            0
        )

        iv_next_page.setOnClickListener {
            if (numOfPage < viewPager.adapter!!.count - 1)
            {
                viewPager.currentItem = ++numOfPage
            }
            else
            {
                val intent = Intent(this, LoginSelectView::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

}
