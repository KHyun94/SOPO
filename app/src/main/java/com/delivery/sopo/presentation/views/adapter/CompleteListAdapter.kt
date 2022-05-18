/* TODO 삭제 여부?
package com.delivery.sopo.presentation.views.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.delivery.sopo.R
import com.delivery.sopo.databinding.InquiryListCompleteItemBinding
import com.delivery.sopo.models.inquiry.InquiryListItem
import com.delivery.sopo.util.SopoLog
import java.util.stream.Stream


class CompleteListAdapter(private val cntOfSelectedItem: MutableLiveData<Int>, lifecycleOwner: LifecycleOwner,
                          private var list: MutableList<InquiryListItem>) : RecyclerView.Adapter<CompleteListAdapter.ViewHolder>()
{
    // 아이템 뷰를 저장하는 뷰홀더 클래스.
    private val TAG = "LOG.SOPO${this.javaClass.simpleName}"
    private val limitOfItem = 2
    private var isMoreView = false
    private var isRemovable = false

    init {
        cntOfSelectedItem.observe(lifecycleOwner, Observer {
            Log.d(TAG,"[2] @@ => $it")
        })
    }

    class ViewHolder(private val binding: InquiryListCompleteItemBinding) : RecyclerView.ViewHolder(binding.root) {

        val inquiryBinding = binding

        fun bind(inquiryListItem: InquiryListItem){
            binding.apply {
                completeInquiryData = inquiryListItem
            }
        }
    }

    // onCreateViewHolder() - 아이템 뷰를 위한 뷰홀더 객체 생성하여 리턴.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
    {
        return ViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.inquiry_list_complete_item,
                parent,
                false
            )
        )
    }

    // onBindViewHolder() - position에 해당하는 데이터를 뷰홀더의 아이템뷰에 표시.
    override fun onBindViewHolder(holder: ViewHolder, position: Int)
    {
        val inquiryListData = list[position]


        holder.apply{
            bind(inquiryListData)
            itemView.tag = inquiryListData
        }

        if(inquiryListData.isSelected){
            viewSettingForSelected(holder.inquiryBinding)
        }
        else{
            viewInitialize(holder.inquiryBinding)
        }

        holder.inquiryBinding.root.cv_ongoing_parent.setOnClickListener{

            SopoLog.d(
                msg = "isSelect : ${inquiryListData.isSelected} && isRemovable : $isRemovable"
            )

            if(isRemovable && !inquiryListData.isSelected){
                inquiryListData.isSelected = true
                cntOfSelectedItem.value = (cntOfSelectedItem.value ?: 0) + 1
                viewSettingForSelected(holder.inquiryBinding)
            }
            else if (isRemovable && inquiryListData.isSelected){
                inquiryListData.isSelected = false
                cntOfSelectedItem.value = (cntOfSelectedItem.value ?: 0) - 1
                viewInitialize(holder.inquiryBinding)
            }
            else{
                SopoLog.d( msg = "2122")
            }
        }
    }

    fun setSelectAll(flag: Boolean){
        if(flag){
            for(item in list){
                if(!item.isSelected){
                    item.isSelected = true
                    cntOfSelectedItem.value = (cntOfSelectedItem.value ?: 0) + 1
                }
            }
            notifyDataSetChanged()
        }
        else{
            for(item in list){
                item.isSelected = false
            }
            cntOfSelectedItem.value = 0
            notifyDataSetChanged()
        }
    }

    fun getSelectedListData(): List<Int>
    {
        return list.filter {
            it.isSelected
        }.map {
            it.parcelDTO.parcelId
        }
    }

    fun getList(): MutableList<InquiryListItem>
    {
        return list
    }

    private fun viewSettingForSelected(binding: InquiryListCompleteItemBinding){
        SopoLog.d( msg = "viewSettingForSelected")
        binding.root.constraint_item_part_complete.makeVisible
        binding.root.constraint_date_complete.makeVisible
        binding.root.constraint_item_part_delete_complete.visibility = View.VISIBLE
        binding.root.constraint_delivery_status_front_complete.visibility = View.VISIBLE
    }

    private fun viewInitialize(binding: InquiryListCompleteItemBinding){
        SopoLog.d( msg = "viewInitialize")
        binding.root.constraint_item_part_complete.visibility = View.VISIBLE
        binding.root.constraint_date_complete.visibility = View.VISIBLE
        binding.root.constraint_item_part_delete_complete.makeVisible
        binding.root.constraint_delivery_status_front_complete.makeVisible
    }

    fun setRemovable(flag: Boolean){
        isRemovable = flag
        if(!isRemovable){
            for(item in list){
                item.isSelected = false
            }
        }
        notifyDataSetChanged()
    }

    fun setDataList(parcel: MutableList<InquiryListItem>) {
        this.list = parcel
        Stream.of(parcel).map {
            it
        }
        notifyDataSetChanged()
    }

    fun isFullListItem(isFull : Boolean){
        this.isMoreView = isFull
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int
    {
        return list.size
    }

    fun getListSize(): Int{
        return list.size
    }
}*/
