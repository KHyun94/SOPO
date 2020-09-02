package com.delivery.sopo.views.inquiry

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.delivery.sopo.R
import com.delivery.sopo.consts.DeliveryStatus
import com.delivery.sopo.models.parcel.Parcel
import kotlinx.android.synthetic.main.inquiry_list_registered_item.view.*


class RegisteredSopoListAdapter(private var list: List<Parcel>?) : RecyclerView.Adapter<RegisteredSopoListAdapter.ViewHolder>()
{
    // 아이템 뷰를 저장하는 뷰홀더 클래스.
    private val TAG = "LOG.SOPO${this.javaClass.simpleName}"

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        internal var constraintDeliveryStatusFront =itemView.constraint_delivery_status_front
        internal var imgDeliveryStatus: ImageView = itemView.image_delivery_status
        internal var tvDeliveryStatus: TextView = itemView.tv_delivery_status
        internal var tvParcelName: TextView = itemView.tv_registered_parcel_name
        internal var tvParcelDate: TextView = itemView.tv_registered_parcel_date
    }

    // onCreateViewHolder() - 아이템 뷰를 위한 뷰홀더 객체 생성하여 리턴.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
    {
        val context = parent.context
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.inquiry_list_registered_item, parent, false)

        return ViewHolder(view)
    }

    // onBindViewHolder() - position에 해당하는 데이터를 뷰홀더의 아이템뷰에 표시.
    override fun onBindViewHolder(holder: ViewHolder, position: Int)
    {
        if(list == null) {
            return
        }
        val data: Parcel = list?.get(position)!!

        when(data.deliveryStatus){
            //상품 준비중
            DeliveryStatus.INFORMATION_RECEIVED -> {
                holder.imgDeliveryStatus.setBackgroundResource(R.drawable.ic_parcel_status_registered)
                holder.constraintDeliveryStatusFront.setBackgroundResource(R.color.COLOR_MAIN_300)
                holder.tvDeliveryStatus.text = "송장등록"
            }
            //상품 인수
            DeliveryStatus.AT_PICKUP -> {
                holder.imgDeliveryStatus.setBackgroundResource(R.drawable.ic_parcel_status_before)
                holder.constraintDeliveryStatusFront.setBackgroundResource(R.color.COLOR_GRAY_400)
                holder.tvDeliveryStatus.text = "배송 전"
            }
            //상품 이동 중
            DeliveryStatus.IN_TRANSIT -> {
                holder.imgDeliveryStatus.setBackgroundResource(R.drawable.ic_parcel_status_ing)
                holder.constraintDeliveryStatusFront.setBackgroundResource(R.color.COLOR_MAIN_900)
                holder.tvDeliveryStatus.text = "배송 중"
            }
            //배송 출발
            DeliveryStatus.OUT_FOR_DELIVERY -> {
                // Nothing to do!!
            }
            //배송 도착
            DeliveryStatus.DELIVERED -> {
                // Nothing to do!!
            }
        }

        holder.tvParcelName.text = data.parcelAlias
        holder.tvParcelDate.text = data.auditDte.substring(0, data.auditDte.indexOf("T"))
    }

    fun setParcel(parcel: List<Parcel>) {

        Log.d(TAG, "In setParcel !!")
        this.list = parcel
        notifyDataSetChanged()
        Log.d(TAG, "After notifyDataSetChanged !!")
    }

    override fun getItemCount(): Int
    {
        Log.d(TAG, "@@ ==> getItemCount : ${list?.size}")
        return list?.size ?: 0
    }
}