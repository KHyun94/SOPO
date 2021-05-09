package com.delivery.sopo.enums

import com.google.gson.annotations.SerializedName
import java.lang.NullPointerException

enum class CarrierEnum(
    @SerializedName("carrierCode")
    val CODE: String,
    @SerializedName("carrierName")
    val NAME: String) {
    DHL("de.dhl","DHL"),
    SAGAWA("jp.sagawa", "Sagawa"),
    YAMATO("jp.yamato", "Kuroneko Yamato"),
    YUUBIN("jp.yuubin", "Japan Post"),
    CHUNILPS("kr.chunilps", "천일택배"),
    CJ_LOGISTICS("kr.cjlogistics", "CJ대한통운"),
    CU_POST("kr.cupost","CU 편의점 택배"),
    CVSNET("kr.cvsnet", "GS Postbox 택배"),
    CWAY("kr.cway", "CWAY (Woori Express)"),
    DAESIN("kr.daesin", "대신택배"),
    EPOST("kr.epost", "우체국택배"),
    HANIPS("kr.hanips", "한의사랑택배"),
    HANJINS("kr.hanjin", "한진택배"),
    HDEXP("kr.hdexp", "합동택배"),
    HOMEPICK("kr.homepick", "홈픽"),
    HONAMLOGIS("kr.honamlogis", "한서호남택배"),
    ILYANGLOGIS("kr.ilyanglogis", "일양로지스"),
    KDEXP("kr.kdexp", "경동택배"),
    KUNYOUNG("kr.kunyoung", "건영택배"),
    LOGEN("kr.logen", "로젠택배"),
    LOTTE("kr.lotte", "롯데택배"),
    SLX("kr.slx", "SLX"),
    SWGEXP("kr.swgexp","성원글로벌카고"),
    TNT("nl.tnt", "TNT"),
    EMS("un.upu.ems", "EMS"),
    FEDEX("us.fedex", "FEDEX"),
    UPS("us.ups", "UPS"),
    USPS("us.usps", "USPS");

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