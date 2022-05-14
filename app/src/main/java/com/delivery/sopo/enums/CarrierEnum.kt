package com.delivery.sopo.enums

import com.delivery.sopo.util.SopoLog
import com.google.gson.annotations.SerializedName
import java.lang.NullPointerException

enum class CarrierEnum(
        @SerializedName("no") val NO: Int,
        @SerializedName("code") val CODE: String,
        @SerializedName("name") val NAME: String)
{

    CHUNILPS(1, "CHUNILPS", "천일택배"),
    CJ_LOGISTICS(2,"CJ_LOGISTICS", "CJ대한통운"),
    CU_POST(3,"CU_POST", "CU 편의점 택배"),
    CVSNET(4,"CVSNET", "GS 편의점 택배"),
    DAESIN(5,"DAESIN", "대신택배"),
    EPOST(6,"EPOST", "우체국택배"),
    HANJINS(7,"HANJINS", "한진택배"),
    HDEXP(8,"HDEXP", "합동택배"),
    LOGEN(9,"LOGEN", "로젠택배"),
    LOTTE(10,"LOTTE", "롯데택배"),
    KDEXP(11,"KDEXP", "경동택배");

    companion object
    {
        fun getCarrierByCode(code: String): CarrierEnum
        {
            SopoLog.d("TEST CARREIR ENUM $code")
            val carrier = values().findLast { it.CODE == code }
            carrier ?: throw NullPointerException("not defined enum")
            return carrier
        }
    }
}