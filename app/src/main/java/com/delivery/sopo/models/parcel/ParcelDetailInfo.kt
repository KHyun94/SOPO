package com.delivery.sopo.models.parcel

import com.delivery.sopo.models.CarrierDTO

data class ParcelDetailInfo(
    // 앱에서 택배 등록한 일자
        val regDt: String,
    // 택배 별칭 "Default:default" -> if default {from_name}이 보내신 택배
        val alias: String,
    // 택배사
        val carrierDTO: CarrierDTO,
    // 운송장 번호
        val waybillNum: String,
    // 택배 상세 정보
        val deliverStatus: String?,
        val timeLineProgresses: MutableList<TimeLineProgress>?
)