package com.delivery.sopo.views.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.delivery.sopo.R
import com.delivery.sopo.databinding.InquiryListPopupWindowItemBinding
import com.delivery.sopo.models.inquiry.InquiryMenuItem

class PopupMenuListAdapter(private var list: MutableList<InquiryMenuItem>) : RecyclerView.Adapter<RecyclerView.ViewHolder>()
{
    private val TAG = "LOG.SOPO${this.javaClass.simpleName}"
    private lateinit var popUpMenuOnclick: InquiryPopUpMenuItemOnclick
    private lateinit var historyPopUpItemOnclick: HistoryPopUpItemOnclick


    init {
        notifyDataSetChanged()
    }

    interface InquiryPopUpMenuItemOnclick{
        fun removeItem(v: View)
        fun refreshItems(v: View)
        fun help(v: View)
    }

    interface HistoryPopUpItemOnclick{
        fun changeTimeCount(v: View, time: String)
    }

    class PopupMenuViewHolder(private val binding: InquiryListPopupWindowItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val menuBinding = binding

        fun bind(menuItem: InquiryMenuItem){
            binding.apply {
                vm = menuItem
            }
        }
    }

    fun setPopUpMenuOnclick(popUpMenuOnclick: InquiryPopUpMenuItemOnclick){
        this.popUpMenuOnclick = popUpMenuOnclick
    }

    fun setHistoryPopUpItemOnclick(historyPopUpItemOnclick: HistoryPopUpItemOnclick){
        this.historyPopUpItemOnclick = historyPopUpItemOnclick
    }

    // onCreateViewHolder() - 아이템 뷰를 위한 뷰홀더 객체 생성하여 리턴.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return PopupMenuViewHolder(
            DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.inquiry_list_popup_window_item, parent, false)
        )
    }

    // onBindViewHolder() - position에 해당하는 데이터를 뷰홀더의 아이템뷰에 표시.
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int)
    {
        val inquiryMenuData = list[position]

        when(holder){
            is PopupMenuViewHolder ->{
                holder.bind(inquiryMenuData)
                holder.itemView.tag = inquiryMenuData

                when(getViewType(position)){
                    InquiryMenuItem.InquiryMenuType.MainMenu -> {
                        inquiryMenuData.menuTitle.also {title ->
                            holder.menuBinding.tvMenuText.text = title
                            holder.menuBinding.constraintV.setOnClickListener {
                                when(position){
                                    0 -> {
                                        popUpMenuOnclick.removeItem(it)
                                    }
                                    1 -> {
                                        popUpMenuOnclick.refreshItems(it)
                                    }
                                    2 -> {
                                        popUpMenuOnclick.help(it)
                                    }
                                }
                            }
                        }
                    }
                    InquiryMenuItem.InquiryMenuType.CompleteHistoryList -> {
                        inquiryMenuData.timeCount?.also { timeCnt->
                            holder.menuBinding.tvMenuText.text = timeCnt.time.replace("-", "년 ") + "월"
                            if(timeCnt.count > 0){
                                holder.menuBinding.tvMenuText.setTextColor(ContextCompat.getColor(holder.menuBinding.root.context, R.color.MAIN_BLACK))
                            }
                            else{
                                holder.menuBinding.tvMenuText.setTextColor(ContextCompat.getColor(holder.menuBinding.root.context, R.color.COLOR_GRAY_300))
                            }

                            holder.menuBinding.constraintV.setOnClickListener {
                                historyPopUpItemOnclick.changeTimeCount(it, timeCnt.time)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun getViewType(position: Int): InquiryMenuItem.InquiryMenuType {
        return list[position].viewType
    }


    fun setDataList(newList: MutableList<InquiryMenuItem>) {
        list = newList
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return list.size
    }
}