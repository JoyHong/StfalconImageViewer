package com.stfalcon.sample.features.demo.rotation

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
import com.stfalcon.sample.common.extensions.getImageFromAssetsFile
import com.stfalcon.sample.common.extensions.loadImage
import com.stfalcon.sample.common.models.Demo
import com.stfalcon.sample.common.models.Poster
import kotlinx.android.synthetic.main.activity_demo_rotation.*

class RotationDemoActivity : AppCompatActivity() {

    companion object {
        private const val KEY_IS_DIALOG_SHOWN = "IS_DIALOG_SHOWN"
        private const val KEY_CURRENT_POSITION = "CURRENT_POSITION"
    }

    private var isDialogShown = false
    private var currentPosition: Int = 0

    private lateinit var viewer: StfalconImageViewer<Poster>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_demo_rotation)

        rotationDemoImage.setOnClickListener { openViewer(0) }
        loadPosterImage(rotationDemoImage, Demo.posters[0],ImageLoader.OPENTYPE_FROM_IMAGE_VIEW)
    }

    override fun onPause() {
        super.onPause()
        viewer.dismiss()
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        if (savedInstanceState != null) {
            isDialogShown = savedInstanceState.getBoolean(KEY_IS_DIALOG_SHOWN)
            currentPosition = savedInstanceState.getInt(KEY_CURRENT_POSITION)
        }

        if (isDialogShown) {
            openViewer(currentPosition)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(KEY_IS_DIALOG_SHOWN, isDialogShown)
        outState.putInt(KEY_CURRENT_POSITION, currentPosition)
        super.onSaveInstanceState(outState)
    }

    private fun openViewer(startPosition: Int) {
        viewer = StfalconImageViewer.Builder<Poster>(this, Demo.posters, ::loadPosterImage,::getItemViewType,::createItemView,::bindItemView)
            .withTransitionFrom(getTransitionTarget(startPosition))
            .withStartPosition(startPosition)
            .withImageChangeListener {
                currentPosition = it
                viewer.updateTransitionImage(getTransitionTarget(it))
            }
            .withDismissListener { isDialogShown = false }
            .show(!isDialogShown)

        currentPosition = startPosition
        isDialogShown = true
    }

    private fun loadPosterImage(view: View, poster: Poster?, openType : Int) {
        view.apply {
            background = getDrawableCompat(R.drawable.shape_placeholder)
            when (poster?.viewType) {
                RecyclingPagerAdapter.VIEW_TYPE_IMAGE -> {
                    val imageView = view as ImageView
                    imageView.loadImage(poster?.url)
                }

                RecyclingPagerAdapter.VIEW_TYPE_SUBSAMPLING_IMAGE -> {
                    if (openType == ImageLoader.OPENTYPE_FROM_ITEM_VIEW){  //是原图，用SubsamplingScaleImageView加载
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
            }
        }
    }

    private fun getTransitionTarget(position: Int) =
        if (position == 0) rotationDemoImage else null


    fun getItemViewType(position: Int): Int {
        return  Demo.posters[position].viewType
    }

    fun createItemView (context : Context, viewType: Int, isZoomingAllowed : Boolean): View{
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

    private fun bindItemView (itemView : View, viewType: Int, position: Int, imageLoader: ImageLoader<Poster> ){
        when (viewType) {
            RecyclingPagerAdapter.VIEW_TYPE_IMAGE -> {
                imageLoader.loadImage(itemView, Demo.posters[position], ImageLoader.OPENTYPE_FROM_IMAGE_VIEW)
            }

            RecyclingPagerAdapter.VIEW_TYPE_SUBSAMPLING_IMAGE -> {
                imageLoader.loadImage(itemView, Demo.posters[position], ImageLoader.OPENTYPE_FROM_ITEM_VIEW)
            }
        }
    }
}