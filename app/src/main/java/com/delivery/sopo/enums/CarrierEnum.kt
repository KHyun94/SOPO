package com.delivery.sopo.enums

import com.delivery.sopo.util.SopoLog
import com.google.gson.annotations.SerializedName
import java.lang.NullPointerException

enum class CarrierEnum(
    @SerializedName("carrierCode")
    val CODE: String,
    @SerializedName("carrierName")
    val NAME: String) {

    CHUNILPS("CHUNILPS", "천일택배"),
    CJ_LOGISTICS("CJ_LOGISTICS", "CJ대한통운"),
    CU_POST("CU_POST","CU 편의점 택배"),
    CVSNET("CVSNET", "GS Postbox 택배"),
    DAESIN("DAESIN", "대신택배"),
    EPOST("EPOST", "우체국택배"),
    HANJINS("HANJINS", "한진택배"),
    HDEXP("HDEXP", "합동택배"),
    LOGEN("LOGEN", "로젠택배"),
    LOTTE("LOTTE", "롯데택배"),
    KDEXP("KDEXP", "경동택배");

/*
  SLX("SLX", "SLX")
  DHL("DHL","DHL"),
    SAGAWA("SAGAWA", "Sagawa"),
    YAMATO("YAMATO", "Kuroneko Yamato"),
    YUUBIN("YUUBIN", "Japan Post"),*/

/*    CWAY("CWAY", "CWAY (Woori Express)"),

    HOMEPICK("HOMEPICK", "홈픽"),
    HONAMLOGIS("HONAMLOGIS", "한서호남택배"),
    ILYANGLOGIS("ILYANGLOGIS", "일양로지스"),

    KUNYOUNG("KUNYOUNG", "건영택배"),

    SWGEXP("SWGEXP","성원글로벌카고"),
    TNT("TNT", "TNT"),
    EMS("EMS", "EMS"),
    FEDEX("FEDEX", "FEDEX"),
    UPS("UPS", "UPS"),
    USPS("USPS", "USPS");*/

    companion object{
        fun getCarrierByCode(code: String): CarrierEnum
        {
            SopoLog.d("TEST CARREIR ENUM $code")
            val carrier = values().findLast { it.CODE == code }
            carrier ?: throw NullPointerException("not defined enum")
            return carrier
        }
    }
}