package com.delivery.sopo.util.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.delivery.sopo.BR
import com.delivery.sopo.R
import com.delivery.sopo.databinding.ItemImgBinding
import com.delivery.sopo.models.TestCor
import com.delivery.sopo.util.adapters.GridRvAdapter.GridRvViewHolder
import kotlinx.android.synthetic.main.item_img.view.*

class GridRvAdapter(private val items: ArrayList<TestCor>) :
    RecyclerView.Adapter<GridRvViewHolder>()
{
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
        val selectItem = items[position]

        holder.onBind(selectItem)

        holder.itemView.iv_img.setOnClickListener {

            Log.d(TAG, "click ${selectItem}")

            it.setBackgroundResource(selectItem.clickRes)
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
        return items.size
    }

    inner class GridRvViewHolder(binding: ItemImgBinding) : RecyclerView.ViewHolder(binding.root)
    {

        fun onBind(item: TestCor)
        {
            Log.d("LOG.SOPO", "vh -> $item")
            binding.setVariable(BR.img, item.nonClickRes)
        }

    }
}