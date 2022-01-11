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
            /*SAGAWA ->
            {
                listOf(R.drawable.ic_logo_sagawa, R.drawable.ic_color_sagawa, R.drawable.ic_gray_sagawa)
            }
            YAMATO ->
            {
                listOf(R.drawable.ic_logo_yamato, R.drawable.ic_color_yamato, R.drawable.ic_gray_yamato)
            }
            YUUBIN ->
            {
                listOf(R.drawable.ic_logo_japan, R.drawable.ic_color_japan, R.drawable.ic_gray_japan)
            }*/

            /*CWAY ->
            {
                listOf(R.drawable.ic_logo_cway, R.drawable.ic_color_cway, R.drawable.ic_gray_cway)
            }*/

            /*HOMEPICK ->
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
            }*/

            /*KUNYOUNG ->
            {
                listOf(R.drawable.ic_logo_gunyoung, R.drawable.ic_color_gunyoung, R.drawable.ic_gray_gunyoung)
            }

             */

            /*SLX ->
            {
                listOf(R.drawable.ic_logo_slx, R.drawable.ic_color_slx, R.drawable.ic_gray_slx)
            }*/
            /*SWGEXP ->
            {
                listOf(R.drawable.ic_logo_sungone, R.drawable.ic_color_sungone, R.drawable.ic_gray_sungone)
            }
            TNT ->
            {
                listOf(R.drawable.ic_logo_tnt, R.drawable.ic_color_tnt, R.drawable.ic_gray_tnt)
            }
            EMS ->
            {
                listOf(R.drawable.ic_logo_ems, R.drawable.ic_color_ems, R.drawable.ic_gray_ems)
            }
            FEDEX ->
            {
                listOf(R.drawable.ic_logo_fedex, R.drawable.ic_color_fedex, R.drawable.ic_gray_fedex)
            }
            UPS ->
            {
                listOf(R.drawable.ic_logo_ups, R.drawable.ic_color_ups, R.drawable.ic_gray_ups)
            }
            USPS ->
            {
                listOf(R.drawable.ic_logo_ilyang, R.drawable.ic_color_ilyang, R.drawable.ic_gray_ilyang)
            }
            DHL ->
            {
                listOf(R.drawable.ic_logo_dhl, R.drawable.ic_color_dhl, R.drawable.ic_gray_dhl)
            }*/
            else -> emptyList()
        }

        return list
    }
}