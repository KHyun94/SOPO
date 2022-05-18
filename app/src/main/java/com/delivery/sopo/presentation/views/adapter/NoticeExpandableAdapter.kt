package com.delivery.sopo.presentation.views.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.TextView
import com.delivery.sopo.R
import com.delivery.sopo.models.menu.NoticeItem
import java.lang.NullPointerException

class NoticeExpandableAdapter(private val context: Context, private val groupData: MutableList<NoticeItem>): BaseExpandableListAdapter()
{
    private val groupLay: Int = R.layout.expandable_notice_item_group
    private val childLay: Int = R.layout.expandable_notice_item_child
    val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater


    override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup?): View
    {
        var convertView = convertView
        if (convertView == null) {
            convertView = inflater.inflate(groupLay, parent, false)
        }
        convertView ?: throw NullPointerException("getGroupView() => convertView is null")

        val tvNoticeTitle = convertView.findViewById<TextView>(R.id.tv_notice_title)
        tvNoticeTitle.text = getGroup(groupPosition).title

        val tvNoticeDate = convertView.findViewById<TextView>(R.id.tv_notice_date)
        tvNoticeDate.text = getGroup(groupPosition).updatedDate

        return convertView
    }

    override fun getChildView(groupPosition: Int,childPosition: Int,isLastChild: Boolean,convertView: View?,parent: ViewGroup?): View
    {
        var convertView = convertView
        if (convertView == null) {
            convertView = inflater.inflate(childLay, parent, false)
        }

        convertView ?: throw NullPointerException("getChildView() => convertView is null")

        val content = getChild(groupPosition,childPosition)
        val tvNoticeContent = convertView.findViewById<TextView>(R.id.tv_notice_content)
        tvNoticeContent.text = content

        return convertView
    }

    override fun getGroupCount(): Int
    {
        return groupData.size
    }

    override fun getChildrenCount(groupPosition: Int): Int
    {
        return groupData[groupPosition].content.size
    }

    override fun getGroup(groupPosition: Int): NoticeItem
    {
        return groupData[groupPosition]
    }

    override fun getChild(groupPosition: Int, childPosition: Int): String
    {
        return groupData[groupPosition].content[childPosition]
    }

    override fun getGroupId(groupPosition: Int): Long
    {
        return groupPosition.toLong()
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long
    {
        return childPosition.toLong()
    }

    override fun hasStableIds(): Boolean
    {
        return true
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean
    {
        return true
    }
}