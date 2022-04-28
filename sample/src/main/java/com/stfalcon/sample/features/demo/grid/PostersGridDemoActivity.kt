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
import com.stfalcon.imageviewer.common.bean.ItemViewStateBean
import com.stfalcon.imageviewer.common.pager.RecyclingPagerAdapter
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
            imageLoader = ::loadPosterImage
            onPosterClick = ::openViewer
        }
    }

    private fun openViewer(startPosition: Int, target: ImageView) {
        viewer = StfalconImageViewer.Builder<Poster>(this, Demo.posters, ::loadPosterImage,::getItemViewType,::createItemView,::bindItemView)
            .withStartPosition(startPosition)
            .withTransitionFrom(target)
            .withUseDialogStyle(true)
            .withImageChangeListener {
                viewer.updateTransitionImage(postersGridView.imageViews[it])
            }
            .show()

    }


    //itemView 加载数据的回调方法
    private fun loadPosterImage(view: View, poster: Poster?, openType : Int) {
        view.apply {
            background = getDrawableCompat(R.drawable.shape_placeholder)
            when (poster?.viewType) {
                RecyclingPagerAdapter.VIEW_TYPE_IMAGE -> { //普通类型的view
                    val imageView = view as ImageView
                    imageView.loadImage(poster?.url)
                }

                RecyclingPagerAdapter.VIEW_TYPE_SUBSAMPLING_IMAGE -> { //长图的view
                    if (openType == ImageLoader.OPENTYPE_FROM_ITEM_VIEW){  //是原图，用SubsamplingScaleImageView加载
                        val subsamplingScaleImageView = view as SubsamplingScaleImageView
                        subsamplingScaleImageView.loadImage(poster?.url)
                    }else{  //缩略图用普通imageview加载
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
                    if (openType == ImageLoader.OPENTYPE_FROM_ITEM_VIEW){  //是原图，用itemView的控件加载
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

    //获取视图类型的回调方法
    fun getItemViewType(position: Int): Int {
        return  Demo.posters[position].viewType
    }

    //根据需要加载控件的不同加载不同的itemView
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

            RecyclingPagerAdapter.VIEW_TYPE_TEXT->{
                itemView = TextView(context).apply {
                    textSize = 20F
                    setTextColor(Color.WHITE)
                    width = ViewGroup.LayoutParams.MATCH_PARENT
                    height = ViewGroup.LayoutParams.MATCH_PARENT
                }
            }

        }
        return itemView
    }


    //绑定视图的操作,因不同适配加载方式不一样,因此在外部根据不同的视图进行不同的操作
    private fun bindItemView (itemView : View, viewType: Int, position: Int, imageLoader: ImageLoader<Poster> ): ItemViewStateBean{

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
                imageLoader.loadImage(itemView, Demo.posters[position], ImageLoader.OPENTYPE_FROM_IMAGE_VIEW)
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
                imageLoader.loadImage(itemView, Demo.posters[position], ImageLoader.OPENTYPE_FROM_ITEM_VIEW)
            }

            RecyclingPagerAdapter.VIEW_TYPE_TEXT->{
                imageLoader.loadImage(itemView,Demo.posters[position], ImageLoader.OPENTYPE_FROM_ITEM_VIEW)
            }
        }
        val itemViewStateBean = ItemViewStateBean()
        itemViewStateBean.isInitState = isInitState
        itemViewStateBean.topOrBottom = topOrBottom
        itemViewStateBean.isScaled = isScaled
        return itemViewStateBean
    }

}




