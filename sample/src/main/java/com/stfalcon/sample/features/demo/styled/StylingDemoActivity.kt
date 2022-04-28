package com.stfalcon.sample.features.demo.styled

import android.content.Context
import android.graphics.Color
import android.graphics.PointF
import android.graphics.Rect
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.github.chrisbanes.photoview.OnScaleChangedListener
import com.github.chrisbanes.photoview.PhotoView
import com.stfalcon.imageviewer.StfalconImageViewer
import com.stfalcon.imageviewer.common.bean.ItemViewStateBean
import com.stfalcon.imageviewer.common.pager.RecyclingPagerAdapter
import com.stfalcon.imageviewer.loader.ImageLoader
import com.stfalcon.imageviewer.viewer.adapter.ImagesPagerAdapter
import com.stfalcon.sample.R
import com.stfalcon.sample.common.extensions.showShortToast
import com.stfalcon.sample.common.models.Demo
import com.stfalcon.sample.common.models.Poster
import com.stfalcon.sample.common.ui.base.BaseActivity
import com.stfalcon.sample.common.ui.views.PosterOverlayView
import com.stfalcon.sample.features.demo.styled.options.StylingOptions
import com.stfalcon.sample.features.demo.styled.options.StylingOptions.Property.CONTAINER_PADDING
import com.stfalcon.sample.features.demo.styled.options.StylingOptions.Property.HIDE_STATUS_BAR
import com.stfalcon.sample.features.demo.styled.options.StylingOptions.Property.IMAGES_MARGIN
import com.stfalcon.sample.features.demo.styled.options.StylingOptions.Property.RANDOM_BACKGROUND
import com.stfalcon.sample.features.demo.styled.options.StylingOptions.Property.SHOW_OVERLAY
import com.stfalcon.sample.features.demo.styled.options.StylingOptions.Property.SHOW_TRANSITION
import com.stfalcon.sample.features.demo.styled.options.StylingOptions.Property.SWIPE_TO_DISMISS
import com.stfalcon.sample.features.demo.styled.options.StylingOptions.Property.ZOOMING
import kotlinx.android.synthetic.main.activity_demo_styling.*

class StylingDemoActivity : BaseActivity() {

    private var options = StylingOptions()
    private var overlayView: PosterOverlayView? = null
    private var viewer: StfalconImageViewer<Poster>? = null
    var posters : MutableList<Poster> = Demo.posters.toMutableList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_demo_styling)

        stylingPostersGridView.apply {
            imageLoader = ::loadPosterImage
            onPosterClick = ::openViewer
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.styling_options_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        options.showDialog(this)
        return super.onOptionsItemSelected(item)
    }

    private fun openViewer(startPosition: Int, imageView: ImageView) {
//        var posters = Demo.posters.toMutableList()
//        var posters = Demo.posters.toMutableList()

        val builder = StfalconImageViewer.Builder<Poster>(this, posters, ::loadPosterImage,::getItemViewType,::createItemView,::bindItemView)
            .withStartPosition(startPosition)
            .withImageChangeListener { position ->
                if (options.isPropertyEnabled(SHOW_TRANSITION)) {
                    viewer?.updateTransitionImage(stylingPostersGridView.imageViews[position])
                }

                overlayView?.update(posters[position])
            }
            .withDismissListener { showShortToast(R.string.message_on_dismiss) }

        builder.withHiddenStatusBar(options.isPropertyEnabled(HIDE_STATUS_BAR))

        if (options.isPropertyEnabled(IMAGES_MARGIN)) {
            builder.withImagesMargin(R.dimen.image_margin)
        }

        if (options.isPropertyEnabled(CONTAINER_PADDING)) {
            builder.withContainerPadding(R.dimen.image_margin)
        }

        if (options.isPropertyEnabled(SHOW_TRANSITION)) {
            builder.withTransitionFrom(imageView)
        }

        builder.allowSwipeToDismiss(options.isPropertyEnabled(SWIPE_TO_DISMISS))
        builder.allowZooming(options.isPropertyEnabled(ZOOMING))

        if (options.isPropertyEnabled(SHOW_OVERLAY)) {
            setupOverlayView(posters, startPosition)
            builder.withOverlayView(overlayView)
        }

        if (options.isPropertyEnabled(RANDOM_BACKGROUND)) {
            builder.withBackgroundColor(getRandomColor())
        }

        viewer = builder.show()
    }

    //删除按钮回调位置
    private fun setupOverlayView(posterList: MutableList<Poster>, startPosition: Int) {
        overlayView = PosterOverlayView(this).apply {
            update(posterList[startPosition])

            onDeleteClick = {
                val currentPosition = viewer?.currentPosition() ?: 0
                if (posterList.size > 1){
                    posterList.removeAt(currentPosition)
                    viewer?.updateImages(posterList)
                }else{
                    viewer?.close()
                    posters = Demo.posters.toMutableList()
                }

                posterList.getOrNull(currentPosition)
                    ?.let { poster -> update(poster) }
            }
        }
    }

    private fun getRandomColor(): Int {
        val random = java.util.Random()
        return android.graphics.Color.argb(255, random.nextInt(156), random.nextInt(156), random.nextInt(156))
    }

    fun getItemViewType(position: Int): Int {
        return  posters[position].viewType
    }

    fun createItemView (context : Context, viewType: Int, isZoomingAllowed : Boolean): View {
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

    private fun bindItemView (itemView : View, viewType: Int, position: Int, imageLoader: ImageLoader<Poster>): ItemViewStateBean {

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
                imageLoader.loadImage(itemView, posters[position], ImageLoader.OPENTYPE_FROM_IMAGE_VIEW)
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

                imageLoader.loadImage(itemView, posters[position], ImageLoader.OPENTYPE_FROM_ITEM_VIEW)
            }

            RecyclingPagerAdapter.VIEW_TYPE_TEXT->{
                imageLoader.loadImage(itemView,posters[position], ImageLoader.OPENTYPE_FROM_ITEM_VIEW)
            }
        }
        val itemViewStateBean = ItemViewStateBean()
        itemViewStateBean.isInitState = isInitState
        itemViewStateBean.topOrBottom = topOrBottom
        itemViewStateBean.isScaled = isScaled
        return itemViewStateBean
    }
}