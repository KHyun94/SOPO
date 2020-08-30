package com.delivery.sopo.viewmodels.registesrs

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.delivery.sopo.R
import com.delivery.sopo.enums.FragmentType
import com.delivery.sopo.models.CourierType
import com.delivery.sopo.util.adapters.GridRvAdapter
import com.delivery.sopo.util.fun_util.SingleLiveEvent
import com.delivery.sopo.util.ui_util.FragmentManager
import com.delivery.sopo.util.ui_util.GridSpacingItemDecoration
import com.delivery.sopo.viewmodels.FocusChangeCallback


class RegisterViewModel : ViewModel()
{
    val TAG = "LOG.SOPO.RegisterVm"

    private val courierList = arrayListOf<CourierType>(
        CourierType("CJ대한통운", R.drawable.daehan_post_color, R.drawable.daehan_post_gray),
        CourierType("DHL", R.drawable.dhl_post_color, R.drawable.dhl_post_gray),
        CourierType("SAGAWA", R.drawable.sagawa_post_color, R.drawable.sagawa_post_gray),
        CourierType("Kuroneko Yamato", R.drawable.yamato_post_color, R.drawable.yamato_post_gray),
        CourierType("Japan Post", R.drawable.japan_post_color, R.drawable.japan_post_gray),
        CourierType("천일택배", R.drawable.chunil_post_color, R.drawable.chunil_post_gray),
        CourierType("CU 편의점택배", R.drawable.cu_post_color, R.drawable.cu_post_gray),
        CourierType("GS Postbox 택배", R.drawable.gs_post_color, R.drawable.gs_post_gray),
        CourierType("CWAY (Woori Express)", R.drawable.cway_post_color, R.drawable.cway_post_gray),
        CourierType("대신택배", R.drawable.daeshin_post_color, R.drawable.daeshin_post_gray),
        CourierType("우체국 택배", R.drawable.korean_post_color, R.drawable.korean_post_gray),
        CourierType("한진택배", R.drawable.hanjin_post_color, R.drawable.hanjin_post_gray),
        CourierType("합동택배", R.drawable.habdong_post_color, R.drawable.habdong_post_gray),
        CourierType("홈픽", R.drawable.home_pick_post_color, R.drawable.home_pick_post_gray),
        CourierType("한서호남택배", R.drawable.hanseoho_post_color, R.drawable.hanseoho_post_gray),
        CourierType("일양로지스", R.drawable.ilyang_post_color, R.drawable.ilyang_post_gray),
        CourierType("경동택배", R.drawable.gyungdonng_post_color, R.drawable.gyungdong_post_gray),
        CourierType("건영택배", R.drawable.gunyoung_post_color, R.drawable.gunyoung_post_gray),
        CourierType("로젠택배", R.drawable.logen_post_color, R.drawable.logen_post_gray),
        CourierType("롯데택배", R.drawable.lotte_post_color, R.drawable.lotte_post_gray),
        CourierType("SLX", R.drawable.slx_post_color, R.drawable.slx_post_gray),
        CourierType("성원글로벌카고", R.drawable.sungone_post_color, R.drawable.sungone_post_gray),
        CourierType("TNT", R.drawable.tnt_post_color, R.drawable.tnt_post_gray),
        CourierType("EMS", R.drawable.ems_post_color, R.drawable.ems_post_gray),
        CourierType("Fedex", R.drawable.fedex_post_color, R.drawable.fedex_post_gray),
        CourierType("UPS", R.drawable.ups_post_color, R.drawable.ups_post_gray),
        CourierType("USPS", R.drawable.usps_post_color, R.drawable.usps_post_gray)
    )

    var trackNumStr = MutableLiveData<String>()

    // 가져온 클립보드 문자열
    var clipboardStr = SingleLiveEvent<String>()

    var courier = MutableLiveData<String>()

    var hideKeyboard = SingleLiveEvent<Boolean>()

    var moveFragment = MutableLiveData<String>()

    val adapter: GridRvAdapter = GridRvAdapter(courierList)
    val decoration = GridSpacingItemDecoration(3, 10, true)
    val rowCnt = 3

    init
    {
        moveFragment.value = ""
        trackNumStr.value = ""
        clipboardStr.value = ""
        hideKeyboard.value = false
    }

    var callback: FocusChangeCallback = FocusChangeCallback@{ type, focus ->
        hideKeyboard.value = !focus
    }

    fun getCourierType(courier:String?) : CourierType?{
        for(c in courierList){
            if(courier == c.name){
                return c
            }
        }

        return null
    }

    fun onMoveStep2Clicked()
    {
        moveFragment.value = FragmentType.REGISTER_STEP2.NAME
    }

    fun onClearClicked()
    {
        moveFragment.value = FragmentManager.currentFragment1st.NAME
    }

    fun onPasteClicked()
    {
        trackNumStr.value = clipboardStr.value
        clipboardStr.call()
    }

}