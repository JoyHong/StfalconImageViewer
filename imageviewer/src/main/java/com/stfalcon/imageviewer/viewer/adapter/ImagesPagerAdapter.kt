/*
 * Copyright 2018 stfalcon.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.stfalcon.imageviewer.viewer.adapter

import android.content.Context
import android.graphics.PointF
import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.recyclerview.widget.RecyclerView
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.github.chrisbanes.photoview.PhotoView
import com.stfalcon.imageviewer.common.pager.RecyclingPagerAdapter
import com.stfalcon.imageviewer.loader.GetViewType
import com.stfalcon.imageviewer.loader.ImageLoader
import com.stfalcon.imageviewer.loader.OnCreateView

class ImagesPagerAdapter<T>(
    private val context: Context,
    private var images: List<T>,
    private val imageLoader: ImageLoader<T>,
    private val getViewType: GetViewType,
    private val onCreateView: OnCreateView
) : RecyclingPagerAdapter<T, ImagesPagerAdapter<T>.ViewHolder>() {

    private val holders = mutableListOf<ViewHolder>()

    companion object {
        const val IMAGE_POSITION_DEFAULT = 0
        const val IMAGE_POSITION_TOP = 1
        const val IMAGE_POSITION_BOTTOM = 2
    }

    fun isScaled(position: Int): Boolean =
        holders.firstOrNull {
            it.adapterPosition == position
        }?.isScaled ?: false

    fun scaledSize(position: Int): Float =
        holders.firstOrNull {
            it.adapterPosition == position
        }?.scaledSize ?: 1.0f

    fun isTopOrBottom(position: Int): Int =
        holders.firstOrNull {
            it.adapterPosition == position
        }?.topOrBottom ?: IMAGE_POSITION_DEFAULT

    fun isInitState(position: Int) =
        holders.firstOrNull {
            it.adapterPosition == position
        }?.isInitState ?: true

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = onCreateView.createView(context, viewType)
        return ViewHolder(itemView, viewType)
            .also {
                holders.add(it)
            }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(images[position])

    override fun getItemCount() = images.size

    override fun getItemViewType(position: Int) = getViewType.getItemViewType(position)

    internal fun updateImages(images: List<T>) {
        this.images = images
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View, private var viewType: Int) : RecyclerView.ViewHolder(itemView) {

        var isScaled = viewType == VIEW_TYPE_SUBSAMPLING_IMAGE
        var isInitState = true  // 初次加载的状态
        var topOrBottom = IMAGE_POSITION_DEFAULT
        var scaledSize = 1.0f

        init {
            when (viewType) {
                VIEW_TYPE_IMAGE -> {
                    val viewGroup = itemView as RelativeLayout
                    val photoView = viewGroup.getChildAt(0) as PhotoView
                    photoView.setOnScaleChangeListener { _, _, _ ->
                        isScaled = photoView.scale >= 1.1f
                        scaledSize = photoView.scale
                    }
                }
                VIEW_TYPE_SUBSAMPLING_IMAGE -> {
                    val viewGroup = itemView as RelativeLayout
                    val subsamplingScaleImageView = viewGroup.getChildAt(0) as SubsamplingScaleImageView
                    subsamplingScaleImageView.setOnStateChangedListener(object :
                        SubsamplingScaleImageView.OnStateChangedListener {
                        override fun onScaleChanged(newScale: Float, origin: Int) {
                        }
                        override fun onCenterChanged(newCenter: PointF?, origin: Int) {
                            val resourceWidth = subsamplingScaleImageView.sWidth   // 源文件宽
                            val resourceHeight = subsamplingScaleImageView.sHeight   // 源文件高
                            val rect = Rect()
                            subsamplingScaleImageView.visibleFileRect(rect)
                            topOrBottom = when {
                                rect.top == 0 -> {
                                    IMAGE_POSITION_TOP
                                }
                                rect.bottom == resourceHeight -> {
                                    IMAGE_POSITION_BOTTOM
                                }
                                else -> {
                                    IMAGE_POSITION_DEFAULT
                                }
                            }
                            isInitState = false
                        }
                    })
                }
            }
        }

        fun bind(image: T) {
            imageLoader.loadImage(itemView, image)
            when (viewType) {
                VIEW_TYPE_SUBSAMPLING_IMAGE -> {
                    isInitState = true
                }
            }
        }

    }
}