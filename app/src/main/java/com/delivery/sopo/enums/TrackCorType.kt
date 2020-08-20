package com.delivery.sopo.enums

import com.delivery.sopo.R
import com.delivery.sopo.R.drawable
import com.delivery.sopo.R.drawable.*

enum class TrackCorType(val corName:String, val clickRes:Int, val nonClickRes:Int)
{
    DAEHAN("CJ대한통운", daehan_post_color, daehan_post_gray),
    DHL("DHL", dhl_post_color, dhl_post_gray),
    SAGAWA("SAGAWA", sagawa_post_color, sagawa_post_gray),
    YAMATO("Kuroneko Yamato", yamato_post_color, yamato_post_gray),
    JAPAN("Japan Post", japan_post_color, japan_post_gray),
    CHUNIL("천일택배", chunil_post_color, chunil_post_gray),
    CU("CU 편의점택배", cu_post_color, cu_post_gray),
    GS("GS Postbox 택배", gs_post_color, gs_post_gray),
    CWAY("CWAY (Woori Express)", cway_post_color, cway_post_gray),
    DAESHIN("대신택배", daeshin_post_color, daeshin_post_gray),
    KOREAN("우체국 택배", korean_post_color, korean_post_gray),
    HANJIN("한진택배", hanjin_post_color, hanjin_post_gray),
    HABDONG("합동택배", habdong_post_color, habdong_post_gray),
    HOMEPICK("홈픽", home_pick_post_color, home_pick_post_gray),
    HANSEOHO("한서호남택배", hanseoho_post_color, hanseoho_post_gray),
    ILYANG("일양로지스", ilyang_post_color, ilyang_post_gray),
    GYUNGDONG("경동택배", gyungdonng_post_color, gyungdong_post_gray),
    GUNYOUNG("건영택배", gunyoung_post_color, gunyoung_post_gray),
    LOGEN("로젠택배", logen_post_color, logen_post_gray),
    LOTTE("롯데택배", lotte_post_color, lotte_post_gray),
    SLX("SLX", slx_post_color, slx_post_gray),
    SUNGONE("성원글로벌카고", sungone_post_color, sungone_post_gray),
    TNT("TNT", tnt_post_color, tnt_post_gray),
    EMS("EMS", ems_post_color, ems_post_gray),
    FEDEX("Fedex", fedex_post_color, fedex_post_gray),
    UPS("UPS", ups_post_color, ups_post_gray),
    USPS("USPS", usps_post_color, usps_post_gray)

}