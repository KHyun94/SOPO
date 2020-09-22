package com.delivery.sopo.views.inquiry

import android.annotation.SuppressLint
import android.content.Context
import android.view.*
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.delivery.sopo.R
import com.delivery.sopo.models.inquiry.MenuItem

class InquiryListPopupWindowAdapter(private val ctx: Context,
                                    private val menuItemList: List<MenuItem>): BaseAdapter()
{
    private val itemList = menuItemList
    private val TAG = this.javaClass.simpleName

    override fun getCount(): Int {
        return itemList.size
    }

    override fun getItem(position: Int): MenuItem
    {
        return itemList[position]
    }

    override fun getItemId(position: Int): Long
    {
        return position.toLong()
    }

    private fun getViewType(position: Int): MenuItem.MenuType
    {
        return getItem(position).viewType
    }

    @SuppressLint("ViewHolder", "SetTextI18n")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View
    {
        val view: View = LayoutInflater.from(ctx).inflate(R.layout.inquiry_list_popup_window_item, null)
        val menu = view.findViewById<TextView>(R.id.tv_menu_text)

        when(getViewType(position)){
            MenuItem.MenuType.MainMenu -> {
                getItem(position).menuTitle?.let {title ->
                    menu.text = title
                }
            }
            MenuItem.MenuType.CompleteHistoryList -> {
                getItem(position).timeCount?.let { timeCnt->
                    menu.text = timeCnt.time.replace("-", "년 ") + "월"
                    if(timeCnt.count > 0){
                        menu.setTextColor(ContextCompat.getColor(view.context, R.color.MAIN_BLACK))
                    }
                    else{
                        menu.setTextColor(ContextCompat.getColor(view.context, R.color.COLOR_GRAY_300))
                    }
                }
            }
        }

        return view
    }
}