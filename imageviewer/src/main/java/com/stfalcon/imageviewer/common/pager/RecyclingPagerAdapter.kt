package com.stfalcon.imageviewer.common.pager

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

abstract class RecyclingPagerAdapter<T, V : RecyclerView.ViewHolder>
    : RecyclerView.Adapter<V>() {

    companion object {
        const val VIEW_TYPE_IMAGE = 1
        const val VIEW_TYPE_SUBSAMPLING_IMAGE = 2
        const val VIEW_TYPE_MEDIA = 3
        const val VIEW_TYPE_TEXT = 4
    }

    abstract override fun getItemCount(): Int
    abstract override fun getItemViewType(position: Int): Int
    abstract override fun onBindViewHolder(holder: V, position: Int)
    abstract override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): V

}