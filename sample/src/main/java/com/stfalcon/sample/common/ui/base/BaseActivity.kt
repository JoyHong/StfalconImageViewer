package com.stfalcon.sample.common.ui.base

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.stfalcon.imageviewer.common.pager.RecyclingPagerAdapter
import com.stfalcon.imageviewer.loader.ImageLoader
import com.stfalcon.sample.R
import com.stfalcon.sample.common.extensions.getDrawableCompat
import com.stfalcon.sample.common.extensions.getImageFromAssetsFile
import com.stfalcon.sample.common.extensions.loadImage
import com.stfalcon.sample.common.models.Poster

abstract class BaseActivity : AppCompatActivity() {

    protected fun loadPosterImage(view: View, poster: Poster?) {
        view.apply {
            background = getDrawableCompat(R.drawable.shape_placeholder)
            when (poster?.viewType) {
                RecyclingPagerAdapter.VIEW_TYPE_IMAGE -> {
                    val imageView = view as ImageView
                    imageView.loadImage(poster.url)
                }

                RecyclingPagerAdapter.VIEW_TYPE_SUBSAMPLING_IMAGE -> {
                    val subsamplingScaleImageView = view as SubsamplingScaleImageView
                    subsamplingScaleImageView.loadImage(poster.url)

                }

                RecyclingPagerAdapter.VIEW_TYPE_TEXT -> {
                    val textView = view as TextView
                    textView.text = poster.description

                }


            }
        }
    }

    protected fun loadImage(imageView: ImageView, url: String?, openType: Int) {
        imageView.apply {
            background = getDrawableCompat(R.drawable.shape_placeholder)
            loadImage(url)
        }
    }


}