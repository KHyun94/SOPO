package com.delivery.sopo.views.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.signature.ObjectKey
import com.delivery.sopo.BR
import com.delivery.sopo.R
import com.delivery.sopo.SOPOApp
import com.delivery.sopo.databinding.InquiryListOngoingItemBinding
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
        fun onItemClicked(v: View, item: T)
    }

    var mListener: OnItemClickListener<SelectItem<Carrier?>>? = null

    fun setOnItemClickListener(listener: OnItemClickListener<SelectItem<Carrier?>>)
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
        val selectItem = carriers[position]

        SopoLog.d(msg = "[$position]:$selectItem")

        holder.binding.linearMainItem.setOnClickListener {

            if(paste != null)
            {
                val (pasteView, pasteItem) = paste!!

                pasteItem.isSelect = false

                DataBindingUtil.bind<ItemImgBinding>(pasteView)?.run {
                    setVariable(BR.icon, pasteItem.item?.icons?.get(2))
                    setVariable(BR.textColor, R.color.COLOR_GRAY_400)
                    setVariable(BR.textFont, R.font.pretendard_medium)
                }

            }

            selectItem.isSelect = true

            holder.binding.setVariable(BR.icon, selectItem.item?.icons?.get(1))
            holder.binding.setVariable(BR.textColor, R.color.COLOR_GRAY_800)
            holder.binding.setVariable(BR.textFont, R.font.pretendard_bold)

            mListener!!.onItemClicked(it, selectItem)

            paste = Pair(holder.binding.root, selectItem)
        }

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

    inner class GridRvViewHolder(itemImgBinding: ItemImgBinding): RecyclerView.ViewHolder(binding.root)
    {
        val binding: ItemImgBinding = itemImgBinding
/*
        init
        {
            binding.ivImg.setOnClickListener {

//                val requestOptions = RequestOptions()
//                requestOptions.diskCacheStrategy(DiskCacheStrategy.NONE)
//                requestOptions.skipMemoryCache(false)
//                requestOptions.signature(ObjectKey(System.currentTimeMillis()))

                if(paste != null)
                {
                    val layout = paste!!.first as LinearLayout

                    (paste!!.first as LinearLayout).setBackgroundResource(R.drawable.border_non_click_img)

                    paste!!.second.isSelect = false

//                    Glide.with(layout.getChildAt(0).context)
//                        .load(paste!!.second.item?.icons?.get(2))
//                        .apply(requestOptions)
//                        .into(layout.getChildAt(0) as ImageView)
                }

                val pos = adapterPosition

                val item = carriers[pos]

                if(pos == RecyclerView.NO_POSITION) return@setOnClickListener


                if(mListener == null) return@setOnClickListener

                if(item.isSelect)
                {
//                    Glide.with(binding.ivImg.context)
//                        .load(item.item?.icons?.get(2))
//                        .apply(requestOptions)
//                        .into(binding.ivImg)
//
//                    binding.tvName.setTextColor(ContextCompat.getColor(SOPOApp.INSTANCE, R.color.COLOR_GRAY_400))

                    item.isSelect = false
                }
                else
                {
//                    Glide.with(binding.ivImg.context)
//                        .load(item.item?.icons?.get(1))
//                        .apply(requestOptions)
//                        .into(binding.ivImg)
//
//                    binding.tvName.setTextColor(ContextCompat.getColor(SOPOApp.INSTANCE, R.color.COLOR_MAIN_700))

                    item.isSelect = true
                }



                binding.isClick = item.isSelect

                mListener!!.onItemClicked(it, pos, carriers)

                paste = Pair(binding.layoutItem, item)

                SopoLog.d(msg = "item ===> $item")

                notifyDataSetChanged()
            }
        }
*/

        fun onBind(selectItem: SelectItem<Carrier?>)
        {
            binding.setVariable(BR.text, selectItem.item?.carrier?.NAME)

            if(selectItem.isSelect)
            {
                binding.setVariable(BR.icon, selectItem.item?.icons?.get(1))
                binding.setVariable(BR.textColor, R.color.COLOR_GRAY_800)
                binding.setVariable(BR.textFont, R.font.pretendard_bold)
            }
            else
            {
                binding.setVariable(BR.icon, selectItem.item?.icons?.get(2))
                binding.setVariable(BR.textColor, R.color.COLOR_GRAY_400)
                binding.setVariable(BR.textFont, R.font.pretendard_medium)
            }
        }
    }
}