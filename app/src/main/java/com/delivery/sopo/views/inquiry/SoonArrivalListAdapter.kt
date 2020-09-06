package com.delivery.sopo.views.inquiry

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.delivery.sopo.R
import com.delivery.sopo.databinding.InquiryListSoonItemBinding
import com.delivery.sopo.models.inquiry.InquiryListData
import kotlinx.android.synthetic.main.inquiry_list_soon_item.view.*


class SoonArrivalListAdapter(private val cntOfSelectedItem: MutableLiveData<Int>, lifecycleOwner: LifecycleOwner,
                             private var list: MutableList<InquiryListData>) : RecyclerView.Adapter<SoonArrivalListAdapter.ViewHolder>()
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

    class ViewHolder(private val binding: InquiryListSoonItemBinding) : RecyclerView.ViewHolder(binding.root) {

        val inquiryBinding = binding

        fun bind(inquiryListData: InquiryListData){
            binding.apply {
                soonInquiryData = inquiryListData
            }
        }
    }

    // onCreateViewHolder() - 아이템 뷰를 위한 뷰홀더 객체 생성하여 리턴.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val viewHolder = ViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.inquiry_list_soon_item,
                parent,
                false
            )
        )
        return viewHolder
    }

    // onBindViewHolder() - position에 해당하는 데이터를 뷰홀더의 아이템뷰에 표시.
    override fun onBindViewHolder(holder: ViewHolder, position: Int)
    {
        val inquiryListData = list[position]

        holder.apply{
            bind(inquiryListData)
            itemView.tag = inquiryListData
        }
        holder.inquiryBinding.root.cv_parent.setOnClickListener{

            if(isRemovable && !inquiryListData.isSelected){
                inquiryListData.isSelected = true
                cntOfSelectedItem.value = (cntOfSelectedItem.value ?: 0) + 1
            }
            else if (isRemovable && inquiryListData.isSelected){
                inquiryListData.isSelected = false
                cntOfSelectedItem.value = (cntOfSelectedItem.value ?: 0) - 1
            }
            else{
                Log.d(TAG, "2122")
            }
        }

    }

    fun setRemovable(flag: Boolean){
        isRemovable = flag
        notifyDataSetChanged()
    }

    fun setDataList(parcel: MutableList<InquiryListData>) {
        this.list = parcel
        notifyDataSetChanged()
    }

    fun isFullListItem(isFull : Boolean){
        this.isMoreView = isFull
        notifyDataSetChanged()
    }


    override fun getItemCount(): Int
    {
        return list?.let {
            if(it.size > limitOfItem && !isMoreView){
                limitOfItem
            }
            else
            {
                it.size
            }
        } ?: 0
    }
}