package com.delivery.sopo.data.models

import com.delivery.sopo.R
import com.delivery.sopo.util.SopoLog
import com.google.gson.annotations.SerializedName
import java.io.Serializable

class Carrier{
    data class Info(
        @SerializedName("carrier")
        val carrier: String,
        @SerializedName("name")
        val name: String,
        @SerializedName("available")
        val isAvailable: Boolean,
    ): Serializable {
        fun getThumbnail(): Int {
            SopoLog.d("Carrier => $carrier")
            return when(carrier){
                "CHUNILPS" ->  R.drawable.ic_thumbnail_chunil
                "CJ_LOGISTICS" -> R.drawable.ic_thumbnail_cj
                "CU_POST"-> R.drawable.ic_thumbnail_cu
                "CVSNET" -> R.drawable.ic_thumbnail_gs
                "DAESIN"-> R.drawable.ic_thumbnail_daeshin
                "EPOST"-> R.drawable.ic_thumbnail_korea
                "HANJINS"-> R.drawable.ic_thumbnail_hanjin
                "HDEXP"-> R.drawable.ic_thumbnail_habdong
                "LOGEN"-> R.drawable.ic_thumbnail_logen
                "LOTTE"-> R.drawable.ic_thumbnail_lotte
                "KDEXP"-> R.drawable.ic_thumbnail_kyungdong
                else -> throw Exception("택배사 오류")
            }
        }
    }
}