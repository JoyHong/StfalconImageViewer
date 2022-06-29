package com.stfalcon.imageviewer.common.pager

import android.os.Bundle
import android.os.Parcelable
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.stfalcon.imageviewer.common.extensions.forEach


abstract class RecyclingPagerAdapter<T, V : RecyclerView.ViewHolder>
    : RecyclerView.Adapter<V>() {

    companion object {
        private val STATE = RecyclingPagerAdapter::class.java.simpleName
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