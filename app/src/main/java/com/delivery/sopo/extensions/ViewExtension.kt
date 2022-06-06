package com.delivery.sopo.extensions

import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import com.delivery.sopo.util.SopoLog
import com.sothree.slidinguppanel.SlidingUpPanelLayout

internal fun View?.findSuitableParent(): ViewGroup?
{
    var view = this
    var fallback: ViewGroup? = null
    do
    {
        if(view is CoordinatorLayout)
        { // We've found a CoordinatorLayout, use it
            return view
        }
        else if(view is FrameLayout)
        {
            if(view.id == android.R.id.content)
            { // If we've hit the decor content view, then we didn't find a CoL in the
                // hierarchy, so use it.
                return view
            }
            else
            { // It's not the content view but we'll use it as our fallback
                fallback = view
            }
        }

        if(view != null)
        { // Else, we will loop and crawl up the view hierarchy and try to find a parent
            val parent = view.parent
            view = if(parent is View) parent else null
        }
    }
    while(view != null)

    // If we reach here then we didn't find a CoL or a suitable content view so we'll fallback
    return fallback
}

fun ViewPager2.reduceSensitive()
{
    try
    {
        val recyclerViewField = ViewPager2::class.java.getDeclaredField("mRecyclerView")
        recyclerViewField.isAccessible = true
        val recyclerView = recyclerViewField.get(this)
        val touchSlopField = RecyclerView::class.java.getDeclaredField("mTouchSlop")
        touchSlopField.isAccessible = true
        touchSlopField.set(recyclerView, (touchSlopField.getInt(recyclerView) * 3)) //6 is empirical value
    }
    catch(ignore: java.lang.Exception)
    {
        SopoLog.e("Fail to reduce ViewPager2 Sensitive [message:${ignore.toString()}]", ignore)
    }
}

fun View.convertBackground(@DrawableRes drawableRes: Int){
    this.background = ContextCompat.getDrawable(this.context, drawableRes)
}

fun TextView.convertTextColor(@ColorRes colorRes: Int){
    this.setTextColor(ContextCompat.getColor(this.context, colorRes))
}

fun View.makeVisible(){
    visibility = View.VISIBLE
}

fun View.makeInvisible(){
    visibility = View.INVISIBLE
}

fun View.makeGone(){
    visibility = View.GONE
}

fun View.enabledClick(){
    isFocusable = true
    isClickable = true
}

fun View.disabledClick(){
    isFocusable = false
    isClickable = false
}

fun SlidingUpPanelLayout.expanded(){
    this.panelState = SlidingUpPanelLayout.PanelState.EXPANDED
}

fun SlidingUpPanelLayout.collapsed(){
    this.panelState = SlidingUpPanelLayout.PanelState.COLLAPSED
}

fun SlidingUpPanelLayout.isExpanded(): Boolean
{
    return this.panelState == SlidingUpPanelLayout.PanelState.EXPANDED
}

fun SlidingUpPanelLayout.isCollapsed(): Boolean
{
    return this.panelState == SlidingUpPanelLayout.PanelState.COLLAPSED
}
