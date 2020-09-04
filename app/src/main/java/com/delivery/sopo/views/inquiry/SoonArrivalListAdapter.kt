package com.delivery.sopo.views.inquiry

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.delivery.sopo.R
import com.delivery.sopo.models.inquiry.InquiryListData
import com.delivery.sopo.models.parcel.Parcel
import kotlinx.android.synthetic.main.inquiry_list_soon_item.view.*


class SoonArrivalListAdapter(private var list: MutableList<InquiryListData>?) : RecyclerView.Adapter<SoonArrivalListAdapter.ViewHolder>()
{
    // 아이템 뷰를 저장하는 뷰홀더 클래스.
    private val TAG = "LOG.SOPO${this.javaClass.simpleName}"
    private val limitOfItem = 2
    private var isMoreView = false
    var isRomovable = false

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var tvParcelName: TextView = itemView.tv_parcel_name
        internal var tvParcelDate: TextView = itemView.tv_parcel_date
    }

    // onCreateViewHolder() - 아이템 뷰를 위한 뷰홀더 객체 생성하여 리턴.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
    {
        return ViewHolder((parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater)
                            .inflate(R.layout.inquiry_list_soon_item, parent, false))
    }

    // onBindViewHolder() - position에 해당하는 데이터를 뷰홀더의 아이템뷰에 표시.
    override fun onBindViewHolder(holder: ViewHolder, position: Int)
    {
        if(list == null) {
            return
        }

        val data: Parcel = list!![position].parcel

        holder.tvParcelName.text = data.parcelAlias
        holder.tvParcelDate.text = data.auditDte.substring(0, data.auditDte.indexOf("T"))
    }

    fun setRemovable(flag: Boolean){
        isRomovable = flag
        notifyDataSetChanged()
    }


    fun setDataList(parcel: MutableList<InquiryListData>) {
        this.list = parcel
        notifyDataSetChanged()
    }

    fun isFullListItem(isFull : Boolean){
        this.isMoreView = isFull
        notifyDataSetChanged()
    }


    override fun getItemCount(): Int
    {
        return list?.let {
            if(it.size > limitOfItem && !isMoreView){
                limitOfItem
            }
            else
            {
                it.size
            }
        } ?: 0
    }
}