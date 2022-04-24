package com.stfalcon.sample.features.demo.grid

import android.app.ActionBar
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView

import com.stfalcon.imageviewer.StfalconImageViewer
import com.stfalcon.imageviewer.Util.Logger
import com.stfalcon.imageviewer.common.pager.RecyclingPagerAdapter
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
            imageLoader = ::loadPosterImage
            onPosterClick = ::openViewer
        }
    }

    private fun openViewer(startPosition: Int, target: ImageView) {
        viewer = StfalconImageViewer.Builder<Poster>(this, Demo.posters, ::loadPosterImage,::getImageType)
            .withStartPosition(startPosition)
            .withTransitionFrom(target)
            .withImageChangeListener {
                viewer.updateTransitionImage(postersGridView.imageViews[it])
            }
            .show()
    }

    private fun loadPosterImage(view: View, poster: Poster?) {
        view.apply {
            background = getDrawableCompat(R.drawable.shape_placeholder)
            when (poster?.imageType) {
                RecyclingPagerAdapter.VIEW_TYPE_IMAGE -> {
                    val imageView = view as ImageView
                    imageView.loadImage(poster?.url)
                }

                RecyclingPagerAdapter.VIEW_TYPE_SUBSAMPLING_IMAGE -> {
                    val subsamplingScaleImageView = view as SubsamplingScaleImageView
                    subsamplingScaleImageView.loadImage(poster?.url)
//                    subsamplingScaleImageView.setImage(ImageSource.asset("sanmartino.jpg"))
                }
            }

        }
    }

    fun getImageType(position: Int): Int {
        Logger.i("position= "+ position)
        return  Demo.posters[position].imageType
    }
}


