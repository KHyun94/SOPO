package com.delivery.sopo.util.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
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
    interface OnItemClickListener<T>
    {
        fun onItemClicked(v: View, pos : Int, item: T)
    }

    var mListener: OnItemClickListener<List<SelectItem<CourierItem>>>? = null

    fun setOnItemClickListener(listener: OnItemClickListener<List<SelectItem<CourierItem>>>)
    {
        this.mListener = listener
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
        if (items != null)
        {
            Log.d(TAG, "$position")
            val selectItem = items!![position]
            holder.onBind(selectItem)
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
        var beforeItem : SelectItem<CourierItem>? = null

        init
        {
            binding.ivImg.setOnClickListener {
                val pos = adapterPosition
                
                if(pos != RecyclerView.NO_POSITION)
                {
                    if(mListener != null)
                    {
                        mListener!!.onItemClicked(it, pos, items!!)
                        beforeItem = items!![pos]
                    }
                }
            }
        }

        fun onBind(selectItem: SelectItem<CourierItem>)
        {
            Log.d("LOG.SOPO", "vh -> $selectItem")
            if(selectItem.isSelect)
                binding.setVariable(BR.img, selectItem.item.clickRes)
            else
                binding.setVariable(BR.img, selectItem.item.nonClickRes)
        }
    }
}