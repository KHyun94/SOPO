package com.delivery.sopo.data.database.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "CARRIER_PATTERN", inheritSuperIndices = true)
data class CarrierPatternEntity(@PrimaryKey(autoGenerate = true)
                                @ColumnInfo(name = "carrierPatternNo", typeAffinity = ColumnInfo.INTEGER)
                                val carrierPatternNo: Int = 1,
                                @ColumnInfo(name = "carrierNo", typeAffinity = ColumnInfo.INTEGER)
                                val carrierNo: Int,
                                @ColumnInfo(name = "length", typeAffinity = ColumnInfo.INTEGER)
                                val length: Int,
                                @ColumnInfo(name = "header", typeAffinity = ColumnInfo.TEXT)
                                val header: String)