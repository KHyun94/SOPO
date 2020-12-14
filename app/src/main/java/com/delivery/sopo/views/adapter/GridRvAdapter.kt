package com.delivery.sopo.views.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey
import com.delivery.sopo.BR
import com.delivery.sopo.R
import com.delivery.sopo.databinding.ItemImgBinding
import com.delivery.sopo.models.CourierItem
import com.delivery.sopo.models.SelectItem
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.views.adapter.GridRvAdapter.GridRvViewHolder

class GridRvAdapter(private var items: ArrayList<SelectItem<CourierItem>>?) :
    RecyclerView.Adapter<GridRvViewHolder>()
{
    var isClicked = MutableLiveData<Boolean>()
    var paste: Pair<View, SelectItem<CourierItem>>? = null

    init
    {
        isClicked.value = false
    }

    interface OnItemClickListener<T>
    {
        fun onItemClicked(v: View, pos: Int, item: T)
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
            SopoLog.d( tag = TAG, str = "$position")
            val selectItem = items!![position]
            holder.onBind(selectItem)
        }
        else
        {
            SopoLog.d( tag = TAG, str = "no item")
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
        init
        {
            binding.ivImg.setOnClickListener {

                val requestOptions = RequestOptions()
                requestOptions.diskCacheStrategy(DiskCacheStrategy.NONE)
                requestOptions.skipMemoryCache(false)
                requestOptions.signature(ObjectKey(System.currentTimeMillis()))

                if (paste != null)
                {
                    SopoLog.d( tag = TAG, str = "Paste => $paste")
                    val layout = paste!!.first as LinearLayout

                    (paste!!.first as LinearLayout).setBackgroundResource(R.drawable.border_non_click_img)

                    paste!!.second.isSelect = false

                    Glide.with(layout.getChildAt(0).context)
                        .load(paste!!.second.item.nonClickRes)
                        .apply(requestOptions)
                        .into(layout.getChildAt(0) as ImageView)
                }

                val pos = adapterPosition

                val item = items!![pos]

                if (pos != RecyclerView.NO_POSITION)
                {
                    if (mListener != null)
                    {
                        if (item.isSelect)
                        {
                            Glide.with(binding.ivImg.context)
                                .load(item.item.nonClickRes)
                                .apply(requestOptions)
                                .into(binding.ivImg)

                            item.isSelect = false
                        }
                        else
                        {
                            Glide.with(binding.ivImg.context)
                                .load(item.item.clickRes)
                                .apply(requestOptions)
                                .into(binding.ivImg)

                            item.isSelect = true
                        }

                        binding.isClick = item.isSelect

                        mListener!!.onItemClicked(it, pos, items!!)

                        paste = Pair(binding.layoutItem, item)

                        SopoLog.d( tag = TAG, str = "item ===> $item")
                    }
                }

                notifyDataSetChanged()
            }
        }

        fun onBind(selectItem: SelectItem<CourierItem>)
        {
            if (selectItem.isSelect)
            {
                binding.setVariable(BR.img, selectItem.item.clickRes)
                binding.setVariable(BR.isClick, selectItem.isSelect)
            }
            else
            {
                binding.setVariable(BR.img, selectItem.item.nonClickRes)
                binding.setVariable(BR.isClick, selectItem.isSelect)
            }
        }
    }
}