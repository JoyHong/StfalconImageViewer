package com.stfalcon.sample.features.demo.scroll

import android.content.Context
import android.graphics.PointF
import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.github.chrisbanes.photoview.OnScaleChangedListener
import com.github.chrisbanes.photoview.PhotoView
import com.stfalcon.imageviewer.StfalconImageViewer

import com.stfalcon.imageviewer.common.pager.RecyclingPagerAdapter
import com.stfalcon.imageviewer.loader.ImageLoader
import com.stfalcon.imageviewer.viewer.adapter.ImagesPagerAdapter
import com.stfalcon.sample.R
import com.stfalcon.sample.common.extensions.getDrawableCompat
import com.stfalcon.sample.common.extensions.loadImage
import com.stfalcon.sample.common.models.Demo
import kotlinx.android.synthetic.main.activity_demo_scrolling_images.*

class ScrollingImagesDemoActivity : AppCompatActivity() {

    private val horizontalImageViews by lazy {
        listOf(
            scrollingHorizontalFirstImage,
            scrollingHorizontalSecondImage,
            scrollingHorizontalThirdImage,
            scrollingHorizontalFourthImage)
    }

    private val verticalImageViews by lazy {
        listOf(
            scrollingVerticalFirstImage,
            scrollingVerticalSecondImage,
            scrollingVerticalThirdImage,
            scrollingVerticalFourthImage)
    }

    private lateinit var viewer: StfalconImageViewer<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_demo_scrolling_images)

        horizontalImageViews.forEachIndexed { index, imageView ->
            loadImage(imageView, Demo.horizontalImages.getOrNull(index))
            imageView.setOnClickListener {
                openViewer(index, imageView, Demo.horizontalImages, horizontalImageViews)
            }
        }

        verticalImageViews.forEachIndexed { index, imageView ->
            loadImage(imageView, Demo.verticalImages.getOrNull(index))
            imageView.setOnClickListener {
                openViewer(index, imageView, Demo.verticalImages, verticalImageViews)
            }
        }
    }

    private fun openViewer(
        startPosition: Int,
        target: ImageView,
        images: List<String>,
        imageViews: List<ImageView>) {
        viewer = StfalconImageViewer.Builder<String>(this, images, ::loadImage, ::getItemViewType,::createItemView,::bindItemView)
            .withStartPosition(startPosition)
            .withTransitionFrom(target)
            .withImageChangeListener { viewer.updateTransitionImage(imageViews.getOrNull(it)) }
            .show()
    }

    private fun loadImage(view: View, url: String?) {
        view.apply {
            background = getDrawableCompat(R.drawable.shape_placeholder)
            val imageView = view as ImageView
            imageView.loadImage(url)
        }
    }

    private fun getItemViewType(position: Int): Int {
        return  Demo.posters[position].viewType
    }

    private fun createItemView (context : Context, viewType: Int, isZoomingAllowed : Boolean): View{
        var itemView = View(context)
        when (viewType) {
            RecyclingPagerAdapter.VIEW_TYPE_IMAGE -> {
                itemView = PhotoView(context).apply {
                    isEnabled = isZoomingAllowed
                }
            }

            RecyclingPagerAdapter.VIEW_TYPE_SUBSAMPLING_IMAGE -> {
                itemView = SubsamplingScaleImageView(context).apply {
                    isEnabled = isZoomingAllowed
                    setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_START)
                    maxScale = 8F
                }
            }
        }
        return itemView
    }

    private fun bindItemView (itemView : View, viewType: Int, position: Int, imageLoader: ImageLoader<String> ) {
        when (viewType) {
            RecyclingPagerAdapter.VIEW_TYPE_IMAGE -> {
                imageLoader.loadImage(itemView, Demo.verticalImages.getOrNull(position))
            }

            RecyclingPagerAdapter.VIEW_TYPE_SUBSAMPLING_IMAGE -> {
                imageLoader.loadImage(itemView, Demo.verticalImages.getOrNull(position))
            }
        }
    }
}