package com.delivery.sopo.util.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.delivery.sopo.BR
import com.delivery.sopo.R
import com.delivery.sopo.databinding.ItemImgBinding
import com.delivery.sopo.models.CourierItem
import com.delivery.sopo.util.adapters.GridRvAdapter.GridRvViewHolder
import kotlinx.android.synthetic.main.item_img.view.*

class GridRvAdapter :
    RecyclerView.Adapter<GridRvViewHolder>
{
    private var items: ArrayList<CourierItem>?

    constructor(items: ArrayList<CourierItem>?) : super()
    {
        this.items = items
    }

    lateinit var binding: ItemImgBinding

    var TAG = "LOG.SOPO.GridRvAdapter"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GridRvViewHolder
    {
        val inflater = LayoutInflater.from(parent.context)
        binding = DataBindingUtil.inflate(inflater, R.layout.item_img, parent, false)
        return GridRvViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GridRvViewHolder, position: Int)
    {
        if(items != null){
            val selectItem = items!!.get(position)

            holder.onBind(selectItem)

            binding.ivImg.setOnClickListener {

            }
        }
    }

    fun setItems(list: ArrayList<CourierItem>)
    {
        this.items = list
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
        return items?.size?:0
    }

    inner class GridRvViewHolder(binding: ItemImgBinding) : RecyclerView.ViewHolder(binding.root)
    {

        fun onBind(item: CourierItem)
        {
            Log.d("LOG.SOPO", "vh -> $item")
            binding.setVariable(BR.img, item.nonClickRes)
        }

        fun onClick(item: CourierItem){
            binding.setVariable(BR.img, item.clickRes)
        }
    }
}