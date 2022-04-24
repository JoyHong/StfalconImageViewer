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
import android.view.View
import android.view.ViewGroup
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView

import com.github.chrisbanes.photoview.PhotoView
import com.stfalcon.imageviewer.Util.Logger
import com.stfalcon.imageviewer.common.extensions.resetScale
import com.stfalcon.imageviewer.common.pager.RecyclingPagerAdapter
import com.stfalcon.imageviewer.loader.GetImageType
import com.stfalcon.imageviewer.loader.ImageLoader

internal class ImagesPagerAdapter<T>(
        private val context: Context,
        _images: List<T>,
        private val imageLoader: ImageLoader<T>,
        private val isZoomingAllowed: Boolean,
        private var getImageType: GetImageType
) : RecyclingPagerAdapter<ImagesPagerAdapter<T>.ViewHolder>() {

    private var images = _images
    private val holders = mutableListOf<ViewHolder>()

    fun isScaled(position: Int): Boolean =
            holders.firstOrNull { it.position == position }?.isScaled ?: false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var itemView = View(context)

        when (viewType) {
            VIEW_TYPE_IMAGE -> {
                itemView = PhotoView(context).apply {
                    isEnabled = isZoomingAllowed
                    setOnViewDragListener { _, _ -> setAllowParentInterceptOnEdge(scale == 1.0f) }
                }
            }

            VIEW_TYPE_SUBSAMPLING_IMAGE -> {

                itemView = SubsamplingScaleImageView(context).apply {
                    isEnabled = isZoomingAllowed
//                    setOnViewDragListener { _, _ -> setAllowParentInterceptOnEdge(scale == 1.0f) }
                }


            }
        }

        return ViewHolder(itemView, viewType).also { holders.add(it) }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(position)

    override fun getItemCount() = images.size

    override fun getViewType(position: Int) = getImageType.getViewType(position)

    internal fun updateImages(images: List<T>) {
        this.images = images
        notifyDataSetChanged()
    }

    internal fun resetScale(position: Int) =
            holders.firstOrNull { it.position == position }?.resetScale()


    internal inner class ViewHolder(itemView: View, viewType: Int)
        : RecyclingPagerAdapter.ViewHolder(itemView) {

        var isScaled: Boolean = false
        private var viewType: Int = viewType;

        fun bind(position: Int) {
            this.position = position


            when (viewType) {
                VIEW_TYPE_IMAGE -> {
                    val photoView: PhotoView = itemView as PhotoView
                    isScaled = photoView.scale > 1f
                }

                VIEW_TYPE_SUBSAMPLING_IMAGE -> {
                    val subsamplingScaleImageView: SubsamplingScaleImageView = itemView as SubsamplingScaleImageView


//                    subsamplingScaleImageView.setOnStateChangedListener(object : SubsamplingScaleImageView.OnStateChangedListener{
//                        override fun onScaleChanged(newScale: Float, origin: Int) {
//                           Logger.i("newScale" + newScale)
//
//                        }
//
//                        override fun onCenterChanged(newCenter: PointF?, origin: Int) {
//                            Logger.i("newCenter x ==" + newCenter!!.x)
//                            Logger.i("newCenter y ==" + newCenter!!.y)
//                        }
//
//                    })
                }
            }
            imageLoader.loadImage(itemView, images[position])


        }

        fun resetScale() {
            when (viewType) {
                VIEW_TYPE_IMAGE -> {
                    val photoView: PhotoView = itemView as PhotoView
                    photoView.resetScale(animate = true)
                }

                VIEW_TYPE_SUBSAMPLING_IMAGE -> {

                }
            }
        }

    }


//    internal inner class ViewHolder(itemView: View , viewType: Int)
//        : RecyclingPagerAdapter.ViewHolder(itemView) {
//
//        internal var isScaled: Boolean = false
//            get() = photoView.scale > 1f
//
//        private val photoView: PhotoView = itemView as PhotoView
//
//        fun bind(position: Int) {
//            this.position = position
//            imageLoader.loadImage(photoView, images[position])
//        }
//
//        fun resetScale() = photoView.resetScale(animate = true)
//    }

}