package com.stfalcon.sample.features.demo.grid

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.github.chrisbanes.photoview.PhotoView
import com.stfalcon.imageviewer.StfalconImageViewer
import com.stfalcon.imageviewer.common.pager.RecyclingPagerAdapter
import com.stfalcon.imageviewer.listeners.OnStateListener
import com.stfalcon.sample.R
import com.stfalcon.sample.common.extensions.getDrawableCompat
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
            ::getItemViewSize,
            ::createItemView
        )
            .withStartPosition(startPosition)
            .withTransitionFrom(target)
            .withUseDialogStyle(true)
            .withImageChangeListener {
                viewer.updateTransitionImage(postersGridView.imageViews[it], it)
            }
            .withStateListener(object : OnStateListener {
                override fun onAnimationStart(view: View, willDismiss: Boolean) {
                }

                override fun onAnimationEnd(view: View, willDismiss: Boolean) {
                }

                override fun onTrackingStart(view: View) {
                }

                override fun onTrackingEnd(view: View) {
                }
            })
            .show(supportFragmentManager)
    }


    //itemView 加载数据的回调方法
    private fun loadPosterImage(view: View, poster: Poster) {
        view.apply {
            background = getDrawableCompat(R.drawable.shape_placeholder)
            when (poster.viewType) {
                RecyclingPagerAdapter.VIEW_TYPE_IMAGE -> { //普通类型的view
                    val imageView = view.findViewById<PhotoView>(R.id.photo_view)
                    imageView.loadImage(poster.url)
                }
                RecyclingPagerAdapter.VIEW_TYPE_SUBSAMPLING_IMAGE -> { //长图的view
                    val subsamplingScaleImageView =
                        view.findViewById<SubsamplingScaleImageView>(R.id.subsampling_scale_imageView)
                    subsamplingScaleImageView.loadImage(poster.url)
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
    private fun getItemViewType(position: Int): Int {
        return Demo.posters[position].viewType
    }

    private fun getItemViewSize(position: Int): IntArray? {
        return null
    }

    //根据需要加载控件的不同加载不同的itemView
    private fun createItemView(context: Context, viewType: Int): View {
        val itemView = when (viewType) {
            RecyclingPagerAdapter.VIEW_TYPE_IMAGE -> {
                LayoutInflater.from(context)
                    .inflate(R.layout.imageview_image, null).apply {
                    }
            }
            else -> { // RecyclingPagerAdapter.VIEW_TYPE_SUBSAMPLING_IMAGE
                LayoutInflater.from(context).inflate(R.layout.imageview_long_image, null).apply {
                    val subsamplingScaleImageView =
                        this.findViewById<SubsamplingScaleImageView>(R.id.subsampling_scale_imageView)
                    subsamplingScaleImageView.setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_START)
                    subsamplingScaleImageView.maxScale = 8F
                }
            }
        }
        itemView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        return itemView
    }

}




