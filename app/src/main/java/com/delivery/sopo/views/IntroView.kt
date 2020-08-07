package com.delivery.sopo.views

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import kotlinx.android.synthetic.main.intro_view.*
import com.delivery.sopo.R
import com.delivery.sopo.util.ui_util.IntroPageAdapter

class IntroView : AppCompatActivity() {

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.intro_view)
        requestedOrientation = SCREEN_ORIENTATION_PORTRAIT
        var viewPager: ViewPager = findViewById(R.id.viewPager)
        var introPageAdapter: IntroPageAdapter =
            IntroPageAdapter(this)
        viewPager.adapter = introPageAdapter

        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(pos: Int, posOffset: Float, posOffsetPixels: Int) {
            }

            override fun onPageSelected(pos: Int) {
                indicator.selectDot(pos)

                if(pos < viewPager.adapter!!.count - 1)
                    btn_login.visibility = View.INVISIBLE
                else
                    btn_login.visibility = View.VISIBLE
            }
        })

        indicator.createDotPanel(viewPager.adapter!!.count, R.drawable.indicator_default_dot, R.drawable.indicator_select_dot, 0)

        btn_login.setOnClickListener{
            val intent = Intent(this, LoginSelectView::class.java)
            startActivity(intent)
            finish()
        }
    }

}
