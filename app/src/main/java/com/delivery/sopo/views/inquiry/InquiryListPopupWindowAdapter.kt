package com.delivery.sopo.views.inquiry

import android.content.Context
import android.util.Log
import android.view.*
import android.widget.BaseAdapter
import android.widget.TextView
import com.delivery.sopo.R

class InquiryListPopupWindowAdapter(private val ctx: Context,
                                    private val menu: Menu): BaseAdapter()
{
    private val menuList = menu
    private val TAG = this.javaClass.simpleName

    override fun getCount(): Int {
        return menuList.size()
    }

    override fun getItem(position: Int): MenuItem?
    {
        return menuList.getItem(position)
    }

    override fun getItemId(position: Int): Long
    {
        return menuList.getItem(position).itemId.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View
    {
        val view: View = LayoutInflater.from(ctx).inflate(R.layout.inquiry_list_popup_window_item, null)
        val menu = view.findViewById<TextView>(R.id.tv_menu_text)
        menu.text = getItem(position)?.title

//        when(getItem(position)?.itemId) {
//            menuList.getItem(0).itemId -> {
//                view.setOnClickListener{
//                    Log.d(TAG, "delete!!")
//                    false
//                }
//            }
//            menuList.getItem(1).itemId -> {
//                view.setOnClickListener{
//                    Log.d(TAG, "refresh!!")
//                }
//            }
//            menuList.getItem(2).itemId -> {
//                view.setOnClickListener{
//                    Log.d(TAG, "help!!")
//                }
//            }
//
//        }
        return view
    }
}