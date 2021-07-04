package com.delivery.sopo.views.intro

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import androidx.viewpager.widget.ViewPager
import com.delivery.sopo.R
import com.delivery.sopo.consts.PermissionConst
import com.delivery.sopo.util.PermissionUtil
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.views.adapter.DefaultTransformer
import com.delivery.sopo.views.adapter.IntroPageAdapter
import com.delivery.sopo.views.adapter.VerticalViewPager
import com.delivery.sopo.views.dialog.GeneralDialog
import com.delivery.sopo.views.dialog.PermissionDialog
import com.delivery.sopo.views.login.LoginSelectView
import kotlinx.android.synthetic.main.intro_view.*
import kotlinx.android.synthetic.main.intro_view_3.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class IntroView: AppCompatActivity()
{
    var numOfPage = 0

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.intro_view)
        requestedOrientation = SCREEN_ORIENTATION_PORTRAIT

        val vp: VerticalViewPager = findViewById(R.id.vp_intro)
        val introPageAdapter: IntroPageAdapter = IntroPageAdapter(this)
        vp.adapter = introPageAdapter

        vp.addOnPageChangeListener(object: ViewPager.OnPageChangeListener
                                   {
                                       override fun onPageScrollStateChanged(state: Int)
                                       {

                                       }

                                       override fun onPageScrolled(pos: Int, posOffset: Float, posOffsetPixels: Int)
                                       {
                                       }

                                       override fun onPageSelected(pos: Int)
                                       {
                                           if(pos == 2)
                                           {
                                               vp[2].tv_next.setOnClickListener {

                                                   // TODO 간소화
                                                   if(!PermissionUtil.isPermissionGranted(this@IntroView, *PermissionConst.PERMISSION_ARRAY))
                                                   {
                                                       SopoLog.e("권한 비허가 상태")
                                                       PermissionUtil.permissionCallback(
                                                           this@IntroView,
                                                           *PermissionConst.PERMISSION_ARRAY) { isGranted ->

                                                           if(!isGranted)
                                                           {
                                                               SopoLog.e("권한 체크 실패")

                                                               // NOT PERMISSION GRANT
                                                               GeneralDialog(act = this@IntroView,
                                                                             title = getString(
                                                                                 R.string.DIALOG_ALARM),
                                                                             msg = getString(
                                                                                 R.string.DIALOG_PERMISSION_REQ_MSG),
                                                                             detailMsg = null,
                                                                             rHandler = Pair(
                                                                                 first = getString(
                                                                                     R.string.DIALOG_OK),
                                                                                 second = { it ->
                                                                                     it.dismiss()
                                                                                     finish()
                                                                                 })).show(
                                                                   supportFragmentManager,
                                                                   "permission")

                                                               return@permissionCallback
                                                           }

                                                           SopoLog.d("권한 체크 성공")
                                                           // permission all clear
                                                           val intent = Intent(this@IntroView,
                                                                               LoginSelectView::class.java)
                                                           startActivity(intent)
                                                           finish()
                                                           return@permissionCallback
                                                       }

                                                       return@setOnClickListener
                                                   }

                                                   SopoLog.e("권한 허가 상태")

                                                   val intent = Intent(this@IntroView,
                                                                       LoginSelectView::class.java)
                                                   startActivity(intent)
                                                   finish()
                                               }
                                           }

                                       }
                                   })

        vp.setPageTransformer(false, DefaultTransformer())


    }

}
