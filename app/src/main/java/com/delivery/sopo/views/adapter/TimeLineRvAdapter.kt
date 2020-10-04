package com.delivery.sopo.views.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.delivery.sopo.R
import com.delivery.sopo.databinding.TimeLineItemBinding
import com.delivery.sopo.views.adapter.TimeLineRvAdapter.*

class TimeLineRvAdapter : RecyclerView.Adapter<TimeLineViewHolder>()
{
    val TAG = "LOG.SOPO"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeLineViewHolder
    {
        val inflater = LayoutInflater.from(parent.context)
        val binding : TimeLineItemBinding = DataBindingUtil.inflate(inflater, R.layout.time_line_item, parent, false)
        Log.d(TAG, "RecyclerView TimeLine onCreateViewHolder")
        return TimeLineViewHolder(binding = binding)
    }

    override fun getItemId(position: Int): Long
    {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int
    {
        return position
    }

    override fun getItemCount(): Int
    {
        return 5
    }

    override fun onBindViewHolder(holder: TimeLineViewHolder, position: Int)
    {
        holder.itemView.visibility = View.VISIBLE
        Log.d("LOG.SOPO", "!!!!$position")
        Log.d("LOG.SOPO", "실행여부")
    }

    inner class TimeLineViewHolder(binding: TimeLineItemBinding) : RecyclerView.ViewHolder(binding.root)
    {
        var itemBindingUtil : TimeLineItemBinding =  DataBindingUtil.bind(binding.root)!!


    }

}