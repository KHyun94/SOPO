package com.delivery.sopo.views.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.delivery.sopo.R
import com.delivery.sopo.databinding.TimeLineItemBinding
import com.delivery.sopo.models.parcel.TimeLineProgress
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.views.adapter.TimeLineRvAdapter.TimeLineViewHolder

class TimeLineRvAdapter : RecyclerView.Adapter<TimeLineViewHolder>()
{
    var list: List<TimeLineProgress?> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeLineViewHolder
    {
        val inflater = LayoutInflater.from(parent.context)
        val binding: TimeLineItemBinding =
            DataBindingUtil.inflate(inflater, R.layout.time_line_item, parent, false)
        SopoLog.d( msg = "RecyclerView TimeLine onCreateViewHolder")
        return TimeLineViewHolder(binding = binding)
    }

    override fun getItemId(position: Int): Long = position.toLong()
    override fun getItemViewType(position: Int): Int = position
    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: TimeLineViewHolder, position: Int)
    {
        val iv = holder.itemBindingUtil.ivTimeLineOval

        SopoLog.d( msg = "마지막 사이즈 ${list.size - 1}")

        if(position == list.size -1){
            holder.itemBindingUtil.vTimeLineBar.setBackgroundResource(0)
        }

        if (position == 0)
        {
            Glide.with(iv.context)
                .load(R.drawable.ic_time_line_current)
                .into(iv)
        }
        else
        {
            Glide.with(iv.context)
                .load(R.drawable.ic_time_line_past)
                .into(iv)
        }

        holder.onBind(list[position]!!)

        holder.setIsRecyclable(false)
    }

    fun setItemList(_list: MutableList<TimeLineProgress?>)
    {
        list = _list.reversed()
    }

    inner class TimeLineViewHolder(binding: TimeLineItemBinding) :
        RecyclerView.ViewHolder(binding.root)
    {
        var itemBindingUtil: TimeLineItemBinding = DataBindingUtil.bind(binding.root)!!

        fun onBind(item: TimeLineProgress)
        {
            itemBindingUtil.tvDateHhmmss.text = item.date!!.HHmmss
            itemBindingUtil.tvDateYymmdd.text = item.date.yyMMdd
            itemBindingUtil.tvDeliveryLocation.text = item.location
            itemBindingUtil.tvDeliveryStatus.text = item.status!!.text
        }
    }

}