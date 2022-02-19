package com.delivery.sopo.util

import com.delivery.sopo.R
import com.delivery.sopo.enums.CarrierEnum
import com.delivery.sopo.enums.CarrierEnum.*

object CarrierUtil
{
    fun getCarrierImages(enum: CarrierEnum):  List<Int>{

        val list = when(enum){
            CHUNILPS ->
            {
                listOf(R.drawable.ic_logo_chunil, R.drawable.ic_color_chunil, R.drawable.ic_gray_chunil)
            }
            CJ_LOGISTICS ->
            {
                listOf(R.drawable.ic_logo_daehan, R.drawable.ic_color_daehan, R.drawable.ic_gray_daehan)
            }
            CU_POST ->
            {
                listOf(R.drawable.ic_logo_cu, R.drawable.ic_color_cu, R.drawable.ic_gray_cu)
            }
            CVSNET ->
            {
                listOf(R.drawable.ic_logo_gs, R.drawable.ic_color_gs, R.drawable.ic_gray_gs)
            }
            DAESIN ->
            {
                listOf(R.drawable.ic_logo_daeshin, R.drawable.ic_color_daeshin, R.drawable.ic_gray_daeshin)
            }
            EPOST ->
            {
                listOf(R.drawable.ic_logo_korean, R.drawable.ic_color_korean, R.drawable.ic_gray_korean)
            }
            HANJINS ->
            {
                listOf(R.drawable.ic_logo_hanjin, R.drawable.ic_color_hanjin, R.drawable.ic_gray_hanjin)
            }
            HDEXP ->
            {
                listOf(R.drawable.ic_logo_habdong, R.drawable.ic_color_habdong, R.drawable.ic_gray_habdong)
            }
            LOGEN ->
            {
                listOf(R.drawable.ic_logo_logen, R.drawable.ic_color_logen, R.drawable.ic_gray_logen)
            }
            LOTTE ->
            {
                listOf(R.drawable.ic_logo_lotte, R.drawable.ic_color_lotte, R.drawable.ic_gray_lotte)
            }
            KDEXP ->
            {
                listOf(R.drawable.ic_logo_kyungdong, R.drawable.ic_color_kyungdong, R.drawable.ic_gray_kyungdong)
            }
        }

        return list
    }
}