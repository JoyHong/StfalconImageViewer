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

    protected fun loadPosterImage(view: View, poster: Poster?,openType: Int) {
        view.apply {
            background = getDrawableCompat(R.drawable.shape_placeholder)
            when (poster?.viewType) {
                RecyclingPagerAdapter.VIEW_TYPE_IMAGE -> {
                    val imageView = view as ImageView
                    imageView.loadImage(poster?.url)
                }

                RecyclingPagerAdapter.VIEW_TYPE_SUBSAMPLING_IMAGE -> {
                    if (openType == ImageLoader.OPENTYPE_SUBSAMPLINGSCALEIMAGEVIEW){  //是原图，用SubsamplingScaleImageView加载
                        val subsamplingScaleImageView = view as SubsamplingScaleImageView
                        subsamplingScaleImageView.loadImage(poster?.url)
                    }else{
                        val imageView = view as ImageView
                        if (poster.viewType == 1){
                            val imageBitmap =  getImageFromAssetsFile(view.context, "WechatIMG6.jpg")
                            imageView.setImageBitmap(imageBitmap)
                            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                        }else{
                            imageView.loadImage(poster?.url)
                        }
                    }
                }

                RecyclingPagerAdapter.VIEW_TYPE_TEXT ->{
                    if (openType == ImageLoader.OPENTYPE_TEXT_VIEW){  //是原图，用SubsamplingScaleImageView加载
                        val textView = view as TextView
                        textView.text = poster.description
                    }else{  //缩略图用普通imageview加载
                        val imageView = view as ImageView
                        imageView.loadImage(poster?.url)
                    }
                }


            }
        }
    }

    protected fun loadImage(imageView: ImageView, url: String?,openType: Int) {
        imageView.apply {
            background = getDrawableCompat(R.drawable.shape_placeholder)
            loadImage(url)
        }
    }


}