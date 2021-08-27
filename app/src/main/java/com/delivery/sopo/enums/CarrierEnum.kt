package com.delivery.sopo.enums

import com.delivery.sopo.util.SopoLog
import com.google.gson.annotations.SerializedName
import java.lang.NullPointerException

enum class CarrierEnum(
    @SerializedName("carrierCode")
    val CODE: String,
    @SerializedName("carrierName")
    val NAME: String) {
    DHL("DHL","DHL"),
    SAGAWA("SAGAWA", "Sagawa"),
    YAMATO("YAMATO", "Kuroneko Yamato"),
    YUUBIN("YUUBIN", "Japan Post"),
    CHUNILPS("CHUNILPS", "천일택배"),
    CJ_LOGISTICS("CJ_LOGISTICS", "CJ대한통운"),
    CU_POST("CU_POST","CU 편의점 택배"),
    CVSNET("CVSNET", "GS Postbox 택배"),
    CWAY("CWAY", "CWAY (Woori Express)"),
    DAESIN("DAESIN", "대신택배"),
    EPOST("EPOST", "우체국택배"),
//    HANIPS("kr.hanips", "한의사랑택배"),
    HANJINS("HANJINS", "한진택배"),
    HDEXP("HDEXP", "합동택배"),
    HOMEPICK("HOMEPICK", "홈픽"),
    HONAMLOGIS("HONAMLOGIS", "한서호남택배"),
    ILYANGLOGIS("ILYANGLOGIS", "일양로지스"),
    KDEXP("KDEXP", "경동택배"),
    KUNYOUNG("KUNYOUNG", "건영택배"),
    LOGEN("LOGEN", "로젠택배"),
    LOTTE("LOTTE", "롯데택배"),
    SLX("SLX", "SLX"),
    SWGEXP("SWGEXP","성원글로벌카고"),
    TNT("TNT", "TNT"),
    EMS("EMS", "EMS"),
    FEDEX("FEDEX", "FEDEX"),
    UPS("UPS", "UPS"),
    USPS("USPS", "USPS");

    companion object{
        fun getCarrierByCode(code: String): CarrierEnum
        {
            val carrier = CarrierEnum.values().findLast {
                it.CODE == code
            }
            carrier ?: throw NullPointerException("not defined enum")
            return carrier
        }
    }
}