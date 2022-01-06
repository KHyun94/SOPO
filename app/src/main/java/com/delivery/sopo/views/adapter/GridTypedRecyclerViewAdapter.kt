package com.delivery.sopo.views.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey
import com.delivery.sopo.BR
import com.delivery.sopo.R
import com.delivery.sopo.SOPOApp
import com.delivery.sopo.databinding.ItemImgBinding
import com.delivery.sopo.models.Carrier
import com.delivery.sopo.models.SelectItem
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.views.adapter.GridTypedRecyclerViewAdapter.GridRvViewHolder

class GridTypedRecyclerViewAdapter(items: List<SelectItem<Carrier?>>): RecyclerView.Adapter<GridRvViewHolder>()
{
    private var carriers: List<SelectItem<Carrier?>> = items

    var isClicked = MutableLiveData<Boolean>()
    var paste: Pair<View, SelectItem<Carrier?>>? = null

    init
    {
        isClicked.value = false
    }

    interface OnItemClickListener<T>
    {
        fun onItemClicked(v: View, pos: Int, item: T)
    }

    var mListener: OnItemClickListener<List<SelectItem<Carrier?>>>? = null

    fun setOnItemClickListener(listener: OnItemClickListener<List<SelectItem<Carrier?>>>)
    {
        this.mListener = listener
    }

    lateinit var binding: ItemImgBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GridRvViewHolder
    {
        val inflater = LayoutInflater.from(parent.context)
        binding = DataBindingUtil.inflate(inflater, R.layout.item_img, parent, false)
        return GridRvViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GridRvViewHolder, position: Int)
    {
        //        if (carriers == null || carriers?.get(position)?.item == null) return

        val selectItem = carriers[position]

        SopoLog.d(msg = "[$position]:$selectItem")

        holder.onBind(selectItem)
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
        return carriers.size
    }

    fun setItems(items: List<SelectItem<Carrier?>>)
    {
        carriers = items
        notifyDataSetChanged()
    }

    inner class GridRvViewHolder(binding: ItemImgBinding): RecyclerView.ViewHolder(binding.root)
    {
        init
        {
            binding.ivImg.setOnClickListener {

                val requestOptions = RequestOptions()
                requestOptions.diskCacheStrategy(DiskCacheStrategy.NONE)
                requestOptions.skipMemoryCache(false)
                requestOptions.signature(ObjectKey(System.currentTimeMillis()))

                if(paste != null)
                {
                    val layout = paste!!.first as LinearLayout

                    (paste!!.first as LinearLayout).setBackgroundResource(R.drawable.border_non_click_img)

                    paste!!.second.isSelect = false

                    Glide.with(layout.getChildAt(0).context)
                        .load(paste!!.second.item?.icons?.get(2))
                        .apply(requestOptions)
                        .into(layout.getChildAt(0) as ImageView)
                }

                val pos = adapterPosition

                val item = carriers[pos]

                if(pos == RecyclerView.NO_POSITION) return@setOnClickListener


                if(mListener == null) return@setOnClickListener

                if(item.isSelect)
                {
                    Glide.with(binding.ivImg.context)
                        .load(item.item?.icons?.get(2))
                        .apply(requestOptions)
                        .into(binding.ivImg)

                    binding.tvName.setTextColor(ContextCompat.getColor(SOPOApp.INSTANCE, R.color.COLOR_GRAY_400))

                    item.isSelect = false
                }
                else
                {
                    Glide.with(binding.ivImg.context)
                        .load(item.item?.icons?.get(1))
                        .apply(requestOptions)
                        .into(binding.ivImg)

                    binding.tvName.setTextColor(ContextCompat.getColor(SOPOApp.INSTANCE, R.color.COLOR_MAIN_700))

                    item.isSelect = true
                }



                binding.isClick = item.isSelect

                mListener!!.onItemClicked(it, pos, carriers)

                paste = Pair(binding.layoutItem, item)

                SopoLog.d(msg = "item ===> $item")

                notifyDataSetChanged()
            }
        }

        fun onBind(selectItem: SelectItem<Carrier?>)
        {
            binding.setVariable(BR.text, selectItem.item?.carrier?.NAME)

            if(selectItem.isSelect)
            {
                binding.setVariable(BR.img, selectItem.item?.icons?.get(1))
                binding.setVariable(BR.isClick, selectItem.isSelect)
                binding.setVariable(BR.textColor, R.color.COLOR_MAIN_700)
            }
            else
            {
                binding.setVariable(BR.img, selectItem.item?.icons?.get(2))
                binding.setVariable(BR.isClick, selectItem.isSelect)
                binding.setVariable(BR.textColor, R.color.COLOR_GRAY_400)
            }
        }
    }
}