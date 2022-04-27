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
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.github.chrisbanes.photoview.OnScaleChangedListener
import com.github.chrisbanes.photoview.PhotoView
import com.stfalcon.imageviewer.common.pager.RecyclingPagerAdapter
import com.stfalcon.imageviewer.loader.GetViewType
import com.stfalcon.imageviewer.loader.ImageLoader

internal class ImagesPagerAdapter<T>(
        private val context: Context,
        _images: List<T>,
        private val imageLoader: ImageLoader<T>,
        private val isZoomingAllowed: Boolean,
        private var getViewType: GetViewType
) : RecyclingPagerAdapter<ImagesPagerAdapter<T>.ViewHolder>() {

    private var images = _images
    private val holders = mutableListOf<ViewHolder>()

    companion object {
        const val IMAGE_POSITION_DEFAULT = 0
        const val IMAGE_POSITION_TOP = 1
        const val IMAGE_POSITION_BOTTOM = 2
    }

    fun isScaled(position: Int): Boolean =
            holders.firstOrNull { it.position == position }?.isScaled ?: false

    fun isTopOrBottom(position: Int): Int =
        holders.firstOrNull{it.position == position}?.topOrBottom ?: IMAGE_POSITION_DEFAULT

    fun isScrolled(position: Int) =
        holders.firstOrNull{it.position == position}?.isScrolled ?: true;


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
                    setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_START)
                    maxScale = 8F
//                    setOnViewDragListener { _, _ -> setAllowParentInterceptOnEdge(scale == 1.0f) }
                }


            }
        }

        return ViewHolder(itemView, viewType).also { holders.add(it) }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(position)

    override fun getItemCount() = images.size

    override fun getViewType(position: Int) = getViewType.getViewType(position)

    internal fun updateImages(images: List<T>) {
        this.images = images
        notifyDataSetChanged()
    }

    internal inner class ViewHolder(itemView: View, viewType: Int)
        : RecyclingPagerAdapter.ViewHolder(itemView) {

        var isScaled: Boolean = false
        var isScrolled: Boolean = true  //初次加载的状态
        private var viewType: Int = viewType
        var topOrBottom: Int = IMAGE_POSITION_DEFAULT

        var center : PointF? = null
        var scale : Float = 0f
        fun bind(position: Int) {
            this.position = position
            when (viewType) {
                VIEW_TYPE_IMAGE -> {
                    val photoView: PhotoView = itemView as PhotoView
                    photoView.setOnScaleChangeListener(object : OnScaleChangedListener{
                        override fun onScaleChange(
                            scaleFactor: Float,
                            focusX: Float,
                            focusY: Float
                        ) {
                            isScaled = scaleFactor > 1f
                        }
                    })
                    imageLoader.loadImage(itemView, images[position], ImageLoader.OPENTYPE_IMAGE_VIEW)
                }

                VIEW_TYPE_SUBSAMPLING_IMAGE -> {
                    val subsamplingScaleImageView: SubsamplingScaleImageView = itemView as SubsamplingScaleImageView
                    isScaled = true
                    isScrolled = false
                    subsamplingScaleImageView.setOnImageEventListener(object : SubsamplingScaleImageView.OnImageEventListener{
                        override fun onReady() {
                           center =  subsamplingScaleImageView.center
                           scale =  subsamplingScaleImageView.scale
                        }

                        override fun onImageLoaded() {

                        }

                        override fun onPreviewLoadError(e: Exception?) {

                        }

                        override fun onImageLoadError(e: Exception?) {

                        }

                        override fun onTileLoadError(e: Exception?) {

                        }

                        override fun onPreviewReleased() {

                        }


                    })

                    subsamplingScaleImageView.setOnStateChangedListener(object : SubsamplingScaleImageView.OnStateChangedListener{
                        override fun onScaleChanged(newScale: Float, origin: Int) {

                        }

                        override fun onCenterChanged(newCenter: PointF?, origin: Int) {
                            val resouceWidth = subsamplingScaleImageView.sWidth   //源文件宽
                            val resouceHeight = subsamplingScaleImageView.sHeight   //源文件高
                            var rect  = Rect()
                            subsamplingScaleImageView.visibleFileRect(rect)
                            if (rect.top == 0 && rect.bottom == resouceHeight){
                                topOrBottom = IMAGE_POSITION_DEFAULT
                                isScaled = false
                            }else if (rect.top == 0 && rect.bottom < resouceHeight){
                                topOrBottom = IMAGE_POSITION_TOP
                                isScaled = true
                            }else if (rect.top > 0 && rect.bottom == resouceHeight){
                                topOrBottom = IMAGE_POSITION_BOTTOM
                                isScaled = true
                            }else{
                                topOrBottom = IMAGE_POSITION_DEFAULT
                                isScaled = true
                            }

                            isScrolled = true
                        }

                    })
                    imageLoader.loadImage(itemView, images[position], ImageLoader.OPENTYPE_SUBSAMPLINGSCALEIMAGEVIEW)
                }
            }


        }

    }
}