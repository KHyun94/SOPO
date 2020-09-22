package com.delivery.sopo.views.inquiry

import android.annotation.SuppressLint
import android.content.Context
import android.view.*
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.delivery.sopo.R
import com.delivery.sopo.models.inquiry.InquiryMenuItem

class InquiryListPopupWindowAdapter(private val ctx: Context,
                                    private val inquiryMenuItemList: List<InquiryMenuItem>): BaseAdapter()
{
    private val itemList = inquiryMenuItemList
    private val TAG = this.javaClass.simpleName

    override fun getCount(): Int {
        return itemList.size
    }

    override fun getItem(position: Int): InquiryMenuItem
    {
        return itemList[position]
    }

    override fun getItemId(position: Int): Long
    {
        return position.toLong()
    }

    private fun getViewType(position: Int): InquiryMenuItem.InquiryMenuType
    {
        return getItem(position).viewType
    }

    @SuppressLint("ViewHolder", "SetTextI18n")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View
    {
        val view: View = LayoutInflater.from(ctx).inflate(R.layout.inquiry_list_popup_window_item, null)
        val menu = view.findViewById<TextView>(R.id.tv_menu_text)

        when(getViewType(position)){
            InquiryMenuItem.InquiryMenuType.MainMenu -> {
                getItem(position).menuTitle?.let {title ->
                    menu.text = title
                }
            }
            InquiryMenuItem.InquiryMenuType.CompleteHistoryList -> {
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