package com.delivery.sopo.viewmodels.registesrs

import android.util.Log
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.delivery.sopo.R
import com.delivery.sopo.enums.FragmentType
import com.delivery.sopo.models.TestCor
import com.delivery.sopo.util.adapters.GridRvAdapter
import com.delivery.sopo.util.ui_util.GridSpacingItemDecoration

class RegisterViewModel : ViewModel()
{
    val TAG = "LOG.SOPO.RegisterVm"

    private val list = arrayListOf<TestCor>(
        TestCor("CJ대한통운", R.drawable.daehan_post_color, R.drawable.daehan_post_gray),
        TestCor("DHL", R.drawable.dhl_post_color, R.drawable.dhl_post_gray),
        TestCor("SAGAWA", R.drawable.sagawa_post_color, R.drawable.sagawa_post_gray),
        TestCor("Kuroneko Yamato", R.drawable.yamato_post_color, R.drawable.yamato_post_gray),
        TestCor("Japan Post", R.drawable.japan_post_color, R.drawable.japan_post_gray),
        TestCor("천일택배", R.drawable.chunil_post_color, R.drawable.chunil_post_gray),
        TestCor("CU 편의점택배", R.drawable.cu_post_color, R.drawable.cu_post_gray),
        TestCor("GS Postbox 택배", R.drawable.gs_post_color, R.drawable.gs_post_gray),
        TestCor("CWAY (Woori Express)", R.drawable.cway_post_color, R.drawable.cway_post_gray),
        TestCor("대신택배", R.drawable.daeshin_post_color, R.drawable.daeshin_post_gray),
        TestCor("우체국 택배", R.drawable.korean_post_color, R.drawable.korean_post_gray),
        TestCor("한진택배", R.drawable.hanjin_post_color, R.drawable.hanjin_post_gray),
        TestCor("합동택배", R.drawable.habdong_post_color, R.drawable.habdong_post_gray),
        TestCor("홈픽", R.drawable.home_pick_post_color, R.drawable.home_pick_post_gray),
        TestCor("한서호남택배", R.drawable.hanseoho_post_color, R.drawable.hanseoho_post_gray),
        TestCor("일양로지스", R.drawable.ilyang_post_color, R.drawable.ilyang_post_gray),
        TestCor("경동택배", R.drawable.gyungdonng_post_color, R.drawable.gyungdong_post_gray),
        TestCor("건영택배", R.drawable.gunyoung_post_color, R.drawable.gunyoung_post_gray),
        TestCor("로젠택배", R.drawable.logen_post_color, R.drawable.logen_post_gray),
        TestCor("롯데택배", R.drawable.lotte_post_color, R.drawable.lotte_post_gray),
        TestCor("SLX", R.drawable.slx_post_color, R.drawable.slx_post_gray),
        TestCor("성원글로벌카고", R.drawable.sungone_post_color, R.drawable.sungone_post_gray),
        TestCor("TNT", R.drawable.tnt_post_color, R.drawable.tnt_post_gray),
        TestCor("EMS", R.drawable.ems_post_color, R.drawable.ems_post_gray),
        TestCor("Fedex", R.drawable.fedex_post_color, R.drawable.fedex_post_gray),
        TestCor("UPS", R.drawable.ups_post_color, R.drawable.ups_post_gray),
        TestCor("USPS", R.drawable.usps_post_color, R.drawable.usps_post_gray)
    )

    val adapter: GridRvAdapter = GridRvAdapter(list)
    val decoration = GridSpacingItemDecoration(3, 10, true)
    val rowCnt = 3

    var moveFragment = MutableLiveData<String>()



     fun onMoveClicked()
    {
        moveFragment.value = FragmentType.REGISTER_STEP2.NAME
    }

    fun onClearClicked()
    {
//        moveFragment.value = FragmentType.REGISTER_STEP1.NAME
    }

}