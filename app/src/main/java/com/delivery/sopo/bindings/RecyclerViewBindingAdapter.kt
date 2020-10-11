package com.delivery.sopo.bindings

import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.delivery.sopo.util.setting.GridSpacingItemDecoration

object RecyclerViewBindingAdapter
{
    @JvmStatic
    @BindingAdapter("linearRvAdapter")
    fun onBindLinearRvAdapter(
        rv: RecyclerView,
        adapter: RecyclerView.Adapter<*>?
    )
    {
        rv.layoutManager = LinearLayoutManager(rv.context)
        rv.adapter = adapter
    }


    @JvmStatic
    @BindingAdapter("gridRvAdapter", "gridSpan")
    fun onBindGridRvAdapter(
        rv: RecyclerView,
        adapter: RecyclerView.Adapter<*>?,
        count: Int
    )
    {
        rv.layoutManager = GridLayoutManager(rv.context, count)
        rv.adapter = adapter
    }

    @JvmStatic
    @BindingAdapter("layoutManager")
    fun onBindSetDecoration(
        recyclerView: RecyclerView,
        decoration: GridSpacingItemDecoration
    )
    {
        recyclerView.addItemDecoration(decoration)
    }

}