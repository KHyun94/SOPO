package com.delivery.sopo.enums

import androidx.annotation.DrawableRes
import com.delivery.sopo.R
import com.delivery.sopo.R.drawable
import com.delivery.sopo.R.drawable.*

enum class TrackCorType(val corName:String, @DrawableRes val clickRes:Int, @DrawableRes val nonClickRes:Int)
{
    DAEHAN("CJ대한통운", R.drawable.ic_color_daehan, R.drawable.ic_gray_daehan),
    DHL("DHL", R.drawable.ic_color_dhl, R.drawable.ic_gray_dhl),
    SAGAWA("SAGAWA", R.drawable.ic_color_sagawa, R.drawable.ic_gray_sagawa),
    YAMATO("Kuroneko Yamato", R.drawable.ic_color_yamato, R.drawable.ic_gray_yamato),
    JAPAN("Japan Post", R.drawable.ic_color_japan, R.drawable.ic_gray_japan),
    CHUNIL("천일택배", R.drawable.ic_color_chunil, R.drawable.ic_gray_chunil),
    CU("CU 편의점택배", R.drawable.ic_color_cu, R.drawable.ic_gray_cu),
    GS("GS Postbox 택배", R.drawable.ic_color_gs, R.drawable.ic_gray_gs),
    CWAY("CWAY (Woori Express)", R.drawable.ic_color_cway, R.drawable.ic_gray_cway),
    DAESHIN("대신택배", R.drawable.ic_color_daeshin, R.drawable.ic_gray_daeshin),
    KOREAN("우체국 택배", R.drawable.ic_color_korean, R.drawable.ic_gray_korean),
    HANJIN("한진택배", R.drawable.ic_color_hanjin, R.drawable.ic_gray_hanjin),
    HABDONG("합동택배", R.drawable.ic_color_habdong, R.drawable.ic_gray_habdong),
    HOMEPICK("홈픽", R.drawable.ic_color_homepick, R.drawable.ic_gray_homepick),
    HANSEOHO("한서호남택배", R.drawable.ic_color_hanseohonam, R.drawable.ic_gray_hanseohonam),
    ILYANG("일양로지스", R.drawable.ic_color_ilyang, R.drawable.ic_gray_ilyang),
    GYUNGDONG("경동택배", R.drawable.ic_color_gyungdong, R.drawable.ic_gray_gyungdong),
    GUNYOUNG("건영택배", R.drawable.ic_color_gunyoung, R.drawable.ic_gray_gunyoung),
    LOGEN("로젠택배", R.drawable.ic_color_logen, R.drawable.ic_gray_logen),
    LOTTE("롯데택배", R.drawable.ic_color_lotte, R.drawable.ic_gray_lotte),
    SLX("SLX", R.drawable.ic_color_slx, R.drawable.ic_gray_slx),
    SUNGONE("성원글로벌카고", R.drawable.ic_color_sungone, R.drawable.ic_gray_sungone),
    TNT("TNT", R.drawable.ic_color_tnt, R.drawable.ic_gray_tnt),
    EMS("EMS", R.drawable.ic_color_ems, R.drawable.ic_gray_ems),
    FEDEX("Fedex", R.drawable.ic_color_fedex, R.drawable.ic_gray_fedex),
    UPS("UPS", R.drawable.ic_color_ups, R.drawable.ic_gray_ups),
    USPS("USPS", R.drawable.ic_color_usps, R.drawable.ic_gray_usps)

}