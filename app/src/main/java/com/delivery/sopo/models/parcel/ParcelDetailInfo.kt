package com.delivery.sopo.models.parcel

import com.delivery.sopo.models.Carrier

data class ParcelDetailInfo(
    // 앱에서 택배 등록한 일자
        val regDt: String,
    // 택배 별칭 "Default:default" -> if default {from_name}이 보내신 택배
        val alias: String,
    // 택배사
        val carrier: Carrier,
    // 운송장 번호
        val waybillNum: String,
    // 택배 상세 정보
        val deliverStatus: String?,
        val timeLineProgresses: MutableList<TimeLineProgress>?
)
{
    fun changeRegDtFormat():String
    {
        val yyMMdd = regDt.split(" ")[0].split("-")
        return with(yyMMdd) { "${get(0)}년 ${get(1)}월 ${get(2)}일 등록" }
    }
}