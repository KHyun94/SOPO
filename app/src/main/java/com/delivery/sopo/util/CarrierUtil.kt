package com.delivery.sopo.util

import com.delivery.sopo.R
import com.delivery.sopo.enums.CarrierEnum
import com.delivery.sopo.enums.CarrierEnum.*

object CarrierUtil
{
    fun getCarrierImages(enum: CarrierEnum): Map<CarrierEnum, List<Int>>{
        when(enum){
            SAGAWA ->
            {
                listOf(R.drawable.ic_logo_sagawa, R.drawable.ic_color_sagawa, R.drawable.ic_gray_sagawa)
            }
            YAMATO ->
            {
                listOf(R.drawable.ic_logo_yamato, R.drawable.ic_color_yamato, R.drawable.ic_gray_yamato)
            }
            YUUBIN ->
            {
            }
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
            }
            CWAY ->
            {
                listOf(R.drawable.ic_logo_cway, R.drawable.ic_color_cway, R.drawable.ic_gray_cway)
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
            HOMEPICK ->
            {
                listOf(R.drawable.ic_logo_homepick, R.drawable.ic_color_homepick, R.drawable.ic_gray_homepick)
            }
            HONAMLOGIS ->
            {
                listOf(R.drawable.ic_logo_hanseohonam, R.drawable.ic_color_hanseohonam, R.drawable.ic_gray_hanseohonam)
            }
            ILYANGLOGIS ->
            {
                listOf(R.drawable.ic_logo_ilyang, R.drawable.ic_color_ilyang, R.drawable.ic_gray_ilyang)
            }
            KDEXP ->
            {

            }
            KUNYOUNG ->
            {

            }
            LOGEN ->
            {

            }
            LOTTE ->
            {

            }
            SLX ->
            {

            }
            SWGEXP ->
            {

            }
            TNT ->
            {

            }
            EMS ->
            {

            }
            FEDEX ->
            {

            }
            UPS ->
            {

            }
            USPS ->
            {
                listOf(R.drawable.ic_logo_ilyang, R.drawable.ic_color_ilyang, R.drawable.ic_gray_ilyang)
            }
        }
    }
}