package com.delivery.sopo.presentation.views.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.delivery.sopo.R
import com.delivery.sopo.data.models.Carrier
import com.delivery.sopo.databinding.ItemCarrierBinding
import com.delivery.sopo.presentation.views.adapter.CarrierRecyclerViewAdapter.CarrierRecyclerViewHolder
import com.delivery.sopo.util.SopoLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

sealed class SelectType {
    object Select : SelectType()
    object Unselect : SelectType()
    object NotSelectable : SelectType()
}

data class SelectCarrier(val selectType: SelectType, val carrier: Carrier.Info)

interface OnItemClickListener<T> {
    fun onSelectedItemClicked(data: T)
    fun onNotSelectableItemClicked()
}

/**
 * 1. 택배사 활성화 상태
 *  선택 (기존 택배 초기화)
 *  미선택
 * 2. 택배사 비활성화 상태
 *
 */
class CarrierRecyclerViewAdapter : RecyclerView.Adapter<CarrierRecyclerViewHolder>() {
    private val list: MutableList<SelectCarrier> = emptyList<SelectCarrier>().toMutableList()

    private var currentPos = -1

    private var onClickListener: OnItemClickListener<Carrier.Info>? = null

    fun setOnItemClickListener(onClickListener: OnItemClickListener<Carrier.Info>){
        this.onClickListener = onClickListener
    }

    fun setItems(items: List<SelectCarrier>) {
        list.clear()
        list.addAll(items)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarrierRecyclerViewHolder {
        val binding = ItemCarrierBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CarrierRecyclerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CarrierRecyclerViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val selectItem = list[position]

        holder.onBind(selectItem)

        holder.itemView.setOnClickListener {

            when (selectItem.selectType) {
                is SelectType.Unselect -> {
                    SopoLog.d("CLICK EVENT CARRIER SELECT")

                    if(currentPos > -1)
                    {
                        val beforeItem = list[currentPos].copy(selectType = SelectType.Unselect)

                        list[currentPos] = beforeItem
                        notifyItemChanged(currentPos)
                    }

                    currentPos = position

                    val clickItem = selectItem.copy(selectType = SelectType.Select)
                    list[currentPos] = clickItem
                    holder.onBind(clickItem)
                    notifyItemChanged(currentPos, clickItem)

                    onClickListener?.onSelectedItemClicked(clickItem.carrier)
                }
                is SelectType.Select -> {
                    SopoLog.d("CLICK EVENT CARRIER UNSELECT")

                    val clickItem = selectItem.copy(selectType = SelectType.Unselect)
                    list[currentPos] = clickItem
                    holder.onBind(clickItem)
                    notifyItemChanged(position, clickItem)

                    currentPos = -1
                }
                is SelectType.NotSelectable -> {
                    notifyItemChanged(position)
                    onClickListener?.onNotSelectableItemClicked()
                }
            }

        }
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class CarrierRecyclerViewHolder(val binding: ItemCarrierBinding) :
        RecyclerView.ViewHolder(binding.root) {

        val context: Context by lazy { binding.root.context }

        fun onBind(item: SelectCarrier) {
            binding.tvCarrier.text = item.carrier.name
            binding.ivCarrier.background = ContextCompat.getDrawable(context, item.carrier.getThumbnail())
            binding.ivNotSelectableBadge.visibility = if (item.selectType is SelectType.NotSelectable) View.VISIBLE else View.GONE

            when (item.selectType) {
                is SelectType.Select -> onSelectedItem()
                is SelectType.Unselect -> onUnselectedItem()
                is SelectType.NotSelectable -> onNotSelectableItem()
            }
        }

        private fun onSelectedItem() {
            binding.tvCarrier.setTextColor(ContextCompat.getColor(context, R.color.COLOR_MAIN_700))
            binding.constraintImage.background = ContextCompat.getDrawable(context, R.drawable.ic_squirecle_stroke_blue)
            binding.constraintImage.backgroundTintList = null
            binding.ivCarrier.setColorFilter(
                Color.parseColor("#ff437DF8"),
                PorterDuff.Mode.SRC_ATOP
            );
        }

        private fun onUnselectedItem() {
            binding.tvCarrier.setTextColor(ContextCompat.getColor(context, R.color.COLOR_GRAY_500))
            binding.constraintImage.background = ContextCompat.getDrawable(context, R.drawable.ic_squirecle_stroke_blue)
            binding.constraintImage.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.COLOR_GRAY_100))
            binding.ivCarrier.colorFilter = null;
        }

        private fun onNotSelectableItem() {
            binding.tvCarrier.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.COLOR_GRAY_300
                )
            )
            binding.constraintImage.background =
                ContextCompat.getDrawable(context, R.drawable.ic_squircle)
            binding.constraintImage.backgroundTintList = null
            binding.ivCarrier.setColorFilter(
                Color.parseColor("#ffD5D5DC"),
                PorterDuff.Mode.SRC_ATOP
            );
        }
    }
}