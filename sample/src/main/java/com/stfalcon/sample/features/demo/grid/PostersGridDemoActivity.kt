package com.stfalcon.sample.features.demo.grid

import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView

import com.stfalcon.imageviewer.StfalconImageViewer
import com.stfalcon.imageviewer.common.pager.RecyclingPagerAdapter
import com.stfalcon.imageviewer.loader.ImageLoader
import com.stfalcon.sample.R
import com.stfalcon.sample.common.extensions.getDrawableCompat
import com.stfalcon.sample.common.extensions.getImageFromAssetsFile
import com.stfalcon.sample.common.extensions.loadImage
import com.stfalcon.sample.common.models.Demo
import com.stfalcon.sample.common.models.Poster
import kotlinx.android.synthetic.main.activity_demo_posters_grid.*
import java.io.IOException
import java.io.InputStream

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
            .withUseDialogStyle(true)
            .withImageChangeListener {
                viewer.updateTransitionImage(postersGridView.imageViews[it])
            }
            .show()

    }

    private fun loadPosterImage(view: View, poster: Poster?, openType : Int) {
        view.apply {
            background = getDrawableCompat(R.drawable.shape_placeholder)
            when (poster?.imageType) {
                RecyclingPagerAdapter.VIEW_TYPE_IMAGE -> { //普通类型的view
                    val imageView = view as ImageView
                    imageView.loadImage(poster?.url)
                }

                RecyclingPagerAdapter.VIEW_TYPE_SUBSAMPLING_IMAGE -> { //长图的view
                    if (openType == ImageLoader.OPENTYPE_SUBSAMPLINGSCALEIMAGEVIEW){  //是原图，用SubsamplingScaleImageView加载
                        val subsamplingScaleImageView = view as SubsamplingScaleImageView
                        subsamplingScaleImageView.loadImage(poster?.url)
                    }else{  //缩略图用普通imageview加载
                        val imageView = view as ImageView
                        if (poster.imageType == 1){
                            val imageBitmap =  getImageFromAssetsFile(view.context, "WechatIMG6.jpg")
                            imageView.setImageBitmap(imageBitmap)
                            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                        }else{
                            imageView.loadImage(poster?.url)
                        }
                    }
                }
            }
        }
    }

    fun getImageType(position: Int): Int {
        return  Demo.posters[position].imageType
    }

}




