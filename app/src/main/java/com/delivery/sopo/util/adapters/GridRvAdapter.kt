package com.delivery.sopo.util.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.delivery.sopo.BR
import com.delivery.sopo.R
import com.delivery.sopo.databinding.ItemImgBinding
import com.delivery.sopo.models.CourierItem
import com.delivery.sopo.models.SelectItem
import com.delivery.sopo.util.adapters.GridRvAdapter.GridRvViewHolder
import kotlinx.android.synthetic.main.item_img.view.*

class GridRvAdapter(private var items: ArrayList<SelectItem<CourierItem>>?) :
    RecyclerView.Adapter<GridRvViewHolder>()
{
    var beforePos = -1

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
        if (items != null)
        {
            val selectItem = items!!.get(position)

            holder.onBind(selectItem)

            holder.itemView.iv_img.setOnClickListener {

                val res =
                    if (selectItem.isSelect) selectItem.item.nonClickRes else selectItem.item.clickRes

                Glide.with(holder.itemView.iv_img.context)
                    .load(res)
                    .into(holder.itemView.iv_img as ImageView)

                items!![position].isSelect = !selectItem.isSelect
            }
        }
        else
        {
            Log.d(TAG, "no item")
        }
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
        return items?.size ?: 0
    }

    inner class GridRvViewHolder(binding: ItemImgBinding) : RecyclerView.ViewHolder(binding.root)
    {
        fun onBind(selectItem: SelectItem<CourierItem>)
        {
            Log.d("LOG.SOPO", "vh -> $selectItem")
            binding.setVariable(BR.img, selectItem.item.nonClickRes)
        }

        fun onReverseSelectStatus()
        {
            val pos = adapterPosition
            if (pos != RecyclerView.NO_POSITION)
            {
                val item = items!![pos]

                for (i in items!!)
                {
                    if(i.hashCode() == item.hashCode())
                    {
                        Log.d(TAG, "")
                    }
                }

            }
        }

    }
}