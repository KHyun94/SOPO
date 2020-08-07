package com.delivery.sopo.util.ui_util

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.LinearLayout
import com.delivery.sopo.util.fun_util.SizeUtil

class IntroIndicator : LinearLayout {

    private var mContext: Context? = null
    // defaultCircle: 디폴트 이미지, selectCircle: 선택 이미지
    private var defaultCircle = 0
    private var selectCircle = 0

    private var imgDot: MutableList<ImageView> = mutableListOf()

    constructor(context: Context?) : super(context) {
        this.mContext = context
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs){
        this.mContext = context
    }

    fun createDotPanel(cnt: Int, defaultCircle: Int, selectCircle: Int, pos: Int) {
        this.removeAllViews()

        this.defaultCircle = defaultCircle
        this.selectCircle = selectCircle

        // 매개 변수로 받은 점의 갯수만큼 이미지뷰를 생성
        // 이미지뷰의 각 옆 패딩을 설정
        for (i in 0 until cnt) {
            imgDot.add(ImageView(mContext).apply {
                setPadding(
                    SizeUtil.changeDpToPx(mContext!!, 10.0f),
                    0,
                    SizeUtil.changeDpToPx(mContext!!, 10.0f),
                    0
                )
            })
            this.addView(imgDot[i])
        }
        selectDot(pos)
    }
    // 선택된 해당 페이지를 selectCircle, 나머지 페이지를 defaultCircle 이미지로 변경
    fun selectDot(pos: Int) {

        for (i in imgDot.indices) {
            if (i == pos)
                imgDot[i].setImageResource(selectCircle)
            else
                imgDot[i].setImageResource(defaultCircle)
        }

    }

}