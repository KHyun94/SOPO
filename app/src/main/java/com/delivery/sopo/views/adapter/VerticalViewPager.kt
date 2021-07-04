package com.delivery.sopo.views.adapter

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.viewpager.widget.ViewPager
import com.delivery.sopo.util.SopoLog

class VerticalViewPager @JvmOverloads constructor(
        context: Context?,
        attrs: AttributeSet? = null
): ViewPager(context!!, attrs) {
    private fun swapTouchEvent(event: MotionEvent): MotionEvent {
        val width: Float = width.toFloat()
        val height: Float = height.toFloat()
        val swappedX = event.y / height * width
        val swappedY = event.x / width * height

        SopoLog.d(""" 좌표
            width:$width
            height:$height
            swaX:$swappedX
            swaY:$swappedY
        """.trimIndent())

        event.setLocation(swappedX, swappedY)
        return event//위아래로 스크롤시 페이지 전환 일어나도록 하는 부분
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        val intercept: Boolean = super.onInterceptTouchEvent(swapTouchEvent(event))
        swapTouchEvent(event)
        return intercept
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        return super.onTouchEvent(swapTouchEvent(ev))
    }

    init {
        setPageTransformer(false, DefaultTransformer())
    }
}

class DefaultTransformer : ViewPager.PageTransformer {
    override fun transformPage(view: View, position: Float) {
        var alpha = 0f
        if (position in 0.0..1.0) {
            alpha = 1 - position     //position은 페이지 이동할때마다 -1부터0 또는 0부터 1 값 가짐
        } else if (-1 < position && position < 0) {
            alpha = position + 1
        }
        view.alpha = alpha
        view.translationX = view.width * -position//이전에 그려져 있던 뷰와 시작점이 같도록 설정해주는 것
        //위 부분 설정하지 않으면 오른쪽 끝점을 기준으로 새로운 뷰가 그려진다.
        val yPosition = position * view.height
        view.translationY = yPosition//뷰의 맨 아래를 기준으로 새로운 뷰를 그리도록 함.
        //설정하지 않으면 이전에 그렸던 뷰의 윗부분이 시작점이 되어 뷰가 그려지게 됨
    }
}