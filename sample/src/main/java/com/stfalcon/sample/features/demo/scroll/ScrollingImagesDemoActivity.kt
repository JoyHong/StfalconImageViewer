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
import com.stfalcon.imageviewer.common.bean.ItemViewStateBean
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
            loadImage(imageView, Demo.horizontalImages.getOrNull(index), ImageLoader.OPENTYPE_IMAGE_VIEW)
            imageView.setOnClickListener {
                openViewer(index, imageView, Demo.horizontalImages, horizontalImageViews)
            }
        }

        verticalImageViews.forEachIndexed { index, imageView ->
            loadImage(imageView, Demo.verticalImages.getOrNull(index),ImageLoader.OPENTYPE_IMAGE_VIEW)
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

    private fun loadImage(view: View, url: String?, openType : Int) {
        view.apply {
            background = getDrawableCompat(R.drawable.shape_placeholder)
            var imageView = view as ImageView
            imageView.loadImage(url)
        }
    }

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

    private fun bindItemView (itemView : View, viewType: Int, position: Int, imageLoader: ImageLoader<String> ): ItemViewStateBean {

        var isScaled = false
        var isInitState = true  //初次加载的状态
        var topOrBottom: Int = ImagesPagerAdapter.IMAGE_POSITION_DEFAULT

        when (viewType) {
            RecyclingPagerAdapter.VIEW_TYPE_IMAGE -> {
                val photoView: PhotoView = itemView as PhotoView
                photoView.setOnScaleChangeListener(object : OnScaleChangedListener {
                    override fun onScaleChange(
                        scaleFactor: Float,
                        focusX: Float,
                        focusY: Float
                    ) {
                        isScaled = scaleFactor > 1f
                    }
                })
                imageLoader.loadImage(itemView, Demo.verticalImages.getOrNull(position), ImageLoader.OPENTYPE_IMAGE_VIEW)
            }

            RecyclingPagerAdapter.VIEW_TYPE_SUBSAMPLING_IMAGE -> {
                val subsamplingScaleImageView: SubsamplingScaleImageView = itemView as SubsamplingScaleImageView
                isScaled = true
                isInitState = true
                subsamplingScaleImageView.setOnStateChangedListener(object : SubsamplingScaleImageView.OnStateChangedListener{
                    override fun onScaleChanged(newScale: Float, origin: Int) {

                    }

                    override fun onCenterChanged(newCenter: PointF?, origin: Int) {
                        val resouceWidth = subsamplingScaleImageView.sWidth   //源文件宽
                        val resouceHeight = subsamplingScaleImageView.sHeight   //源文件高
                        var rect  = Rect()
                        subsamplingScaleImageView.visibleFileRect(rect)
                        if (rect.top == 0 && rect.bottom == resouceHeight){
                            topOrBottom = ImagesPagerAdapter.IMAGE_POSITION_DEFAULT
                            isScaled = false
                        }else if (rect.top == 0 && rect.bottom < resouceHeight){
                            topOrBottom = ImagesPagerAdapter.IMAGE_POSITION_TOP
                            isScaled = true
                        }else if (rect.top > 0 && rect.bottom == resouceHeight){
                            topOrBottom = ImagesPagerAdapter.IMAGE_POSITION_BOTTOM
                            isScaled = true
                        }else{
                            topOrBottom = ImagesPagerAdapter.IMAGE_POSITION_DEFAULT
                            isScaled = true
                        }

                        isInitState = false
                    }

                })
                imageLoader.loadImage(itemView, Demo.verticalImages.getOrNull(position), ImageLoader.OPENTYPE_SUBSAMPLINGSCALEIMAGEVIEW)
            }
        }
        val itemViewStateBean = ItemViewStateBean()
        itemViewStateBean.isInitState = isInitState
        itemViewStateBean.topOrBottom = topOrBottom
        itemViewStateBean.isScaled = isScaled
        return itemViewStateBean
    }
}