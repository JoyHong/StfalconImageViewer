package com.stfalcon.sample.features.demo.grid

import android.content.Context
import android.graphics.Color
import android.graphics.PointF
import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.github.chrisbanes.photoview.OnScaleChangedListener
import com.github.chrisbanes.photoview.PhotoView

import com.stfalcon.imageviewer.StfalconImageViewer
import com.stfalcon.imageviewer.common.pager.RecyclingPagerAdapter
import com.stfalcon.imageviewer.listeners.OnChildAttachStateChangeListener
import com.stfalcon.imageviewer.loader.ImageLoader
import com.stfalcon.imageviewer.viewer.adapter.ImagesPagerAdapter
import com.stfalcon.sample.R
import com.stfalcon.sample.common.extensions.getDrawableCompat
import com.stfalcon.sample.common.extensions.getImageFromAssetsFile
import com.stfalcon.sample.common.extensions.loadImage
import com.stfalcon.sample.common.models.Demo
import com.stfalcon.sample.common.models.Poster
import kotlinx.android.synthetic.main.activity_demo_posters_grid.*

class PostersGridDemoActivity : AppCompatActivity() {

    private lateinit var viewer: StfalconImageViewer<Poster>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_demo_posters_grid)

        postersGridView.apply {
            imageLoader = ::loadPosterImage2
            onPosterClick = ::openViewer
        }
    }

    private fun openViewer(startPosition: Int, target: ImageView) {

        viewer = StfalconImageViewer.Builder<Poster>(
            this,
            Demo.posters,
            ::loadPosterImage,
            ::getItemViewType,
            ::createItemView,
            ::bindItemView
        )
            .withStartPosition(startPosition)
            .withTransitionFrom(target)
            .withUseDialogStyle(true)
            .withImageChangeListener {
                viewer.updateTransitionImage(postersGridView.imageViews[it])
            }
            .withChildAttachStateChangeListener(object : OnChildAttachStateChangeListener {
                override fun onChildViewAttachedToWindow(view: View?) {

                }

                override fun onChildViewDetachedFromWindow(view: View?) {

                }
            })
            .show()
    }


    //itemView 加载数据的回调方法
    private fun loadPosterImage(view: View, poster: Poster?) {
        view.apply {
            background = getDrawableCompat(R.drawable.shape_placeholder)
            when (poster?.viewType) {
                RecyclingPagerAdapter.VIEW_TYPE_IMAGE -> { //普通类型的view
                    val imageView = view as ImageView
                    imageView.loadImage(poster?.url)
                }

                RecyclingPagerAdapter.VIEW_TYPE_SUBSAMPLING_IMAGE -> { //长图的view
                    val subsamplingScaleImageView = view as SubsamplingScaleImageView
                    subsamplingScaleImageView.loadImage(poster?.url)

                }

                RecyclingPagerAdapter.VIEW_TYPE_TEXT -> {
                    val textView = view as TextView
                    textView.text = poster.description
                }
            }
        }
    }

    private fun loadPosterImage2(view: View, poster: Poster?) {
        view.apply {
            val imageView = view as ImageView
            imageView.loadImage(poster?.url)
        }
    }

    //获取视图类型的回调方法
    fun getItemViewType(position: Int): Int {
        return Demo.posters[position].viewType
    }

    //根据需要加载控件的不同加载不同的itemView
    private fun createItemView(context: Context, viewType: Int, isZoomingAllowed: Boolean): View {
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

            RecyclingPagerAdapter.VIEW_TYPE_TEXT -> {
                itemView = TextView(context).apply {
                    textSize = 20F
                    setTextColor(Color.WHITE)
                    width = ViewGroup.LayoutParams.MATCH_PARENT
                    height = ViewGroup.LayoutParams.MATCH_PARENT
                }
            }

        }

        itemView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        return itemView
    }


    //绑定视图的操作,因不同适配加载方式不一样,因此在外部根据不同的视图进行不同的操作
    private fun bindItemView(
        itemView: View,
        viewType: Int,
        position: Int,
        imageLoader: ImageLoader<Poster>
    ) {

        when (viewType) {
            RecyclingPagerAdapter.VIEW_TYPE_IMAGE -> {

                imageLoader.loadImage(itemView, Demo.posters[position])
            }

            RecyclingPagerAdapter.VIEW_TYPE_SUBSAMPLING_IMAGE -> {

                imageLoader.loadImage(itemView, Demo.posters[position])
            }

            RecyclingPagerAdapter.VIEW_TYPE_TEXT -> {
                imageLoader.loadImage(itemView, Demo.posters[position])
            }
        }
    }

}




