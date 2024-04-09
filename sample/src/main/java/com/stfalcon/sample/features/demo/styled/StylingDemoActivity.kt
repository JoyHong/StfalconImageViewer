package com.stfalcon.sample.features.demo.styled

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.github.chrisbanes.photoview.PhotoView
import com.stfalcon.imageviewer.StfalconImageViewer
import com.stfalcon.imageviewer.common.pager.RecyclingPagerAdapter
import com.stfalcon.sample.R
import com.stfalcon.sample.common.extensions.showShortToast
import com.stfalcon.sample.common.models.Demo
import com.stfalcon.sample.common.models.Poster
import com.stfalcon.sample.common.ui.base.BaseActivity
import com.stfalcon.sample.common.ui.views.PosterOverlayView
import com.stfalcon.sample.features.demo.styled.options.StylingOptions
import com.stfalcon.sample.features.demo.styled.options.StylingOptions.Property.*
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

        val builder = StfalconImageViewer.Builder<Poster>(this, posters, ::loadPosterImage, ::getItemViewType, ::getItemViewSize, ::createItemView)
            .withStartPosition(startPosition)
            .withImageChangeListener { position ->
                if (options.isPropertyEnabled(SHOW_TRANSITION)) {
                    viewer?.updateTransitionImage(stylingPostersGridView.imageViews[position], position)
                }

                overlayView?.update(posters[position])
            }
            .withDismissListener { showShortToast(R.string.message_on_dismiss) }

        builder.withHiddenStatusBar(options.isPropertyEnabled(HIDE_STATUS_BAR))

        if (options.isPropertyEnabled(IMAGES_MARGIN)) {
            builder.withImagesMargin(R.dimen.image_margin)
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

        viewer = builder.show(supportFragmentManager)
    }

    //删除按钮回调位置
    private fun setupOverlayView(posterList: MutableList<Poster>, startPosition: Int) {
        overlayView = PosterOverlayView(this).apply {
            update(posterList[startPosition])

            onDeleteClick = {
                val currentPosition = viewer?.getCurrentItem() ?: 0
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

    fun getItemViewSize(position: Int): IntArray? {
        return null
    }

    fun createItemView (context : Context, viewType: Int): View {
        var itemView = View(context)
        when (viewType) {
            RecyclingPagerAdapter.VIEW_TYPE_IMAGE -> {
                itemView = PhotoView(context)
            }

            RecyclingPagerAdapter.VIEW_TYPE_SUBSAMPLING_IMAGE -> {
                itemView = SubsamplingScaleImageView(context).apply {
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

}