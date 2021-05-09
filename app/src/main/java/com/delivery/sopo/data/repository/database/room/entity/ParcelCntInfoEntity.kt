package com.delivery.sopo.data.repository.database.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.delivery.sopo.util.TimeUtil
@Entity(
    tableName = "PARCEL_CNT_INFO"
)
data class ParcelCntInfoEntity(
    // 배송완료 리스트에서 조회 가능한 년월 YYYYMM
    @PrimaryKey
    @ColumnInfo(
        name = "TIME",
        typeAffinity = ColumnInfo.TEXT
    )
    var time: String,

    // 배송완료 리스트에서 조회 가능한 아이템의 개수
    @ColumnInfo(
        name = "COUNT",
        typeAffinity = ColumnInfo.INTEGER
    )
    var count: Int,
    /**
     *  1 => 현재 화면에 표출
     *  0 => 현재 화면에 표출되지는 않지만 조회 가능한 데이터
     *  ------ 0 보다 큰 데이터만 유효한 데이터임(LiveData로 관찰하고 있는 데이터) --------
     *  -1 => 모든 데이터의 count(개수)가 0개이므로
     *  편하게 View라고 생각하면 편할듯하다.(VISIBLE[1] , INVISIBLE[0], GONE[-1])
     */
    @ColumnInfo(
        name = "VISIBILITY",
        typeAffinity = ColumnInfo.INTEGER
    )
    var visibility: Int = 0,
    /**
     * 0 => 다음 업데이트에서 삭제
     * 1 => 현재 사용가능한 데이터
     */
    @ColumnInfo(
        name = "STATUS",
        typeAffinity = ColumnInfo.INTEGER
    )
    var status: Int = 1,
    @ColumnInfo(
        name = "AUDIT_DTE",
        typeAffinity = ColumnInfo.TEXT
    )
    var auditDte: String = TimeUtil.getDateTime()
)