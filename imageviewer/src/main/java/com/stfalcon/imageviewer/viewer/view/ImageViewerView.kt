/*
 * Copyright 2018 stfalcon.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.stfalcon.imageviewer.viewer.view

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.core.view.GestureDetectorCompat
import androidx.viewpager.widget.ViewPager
import com.stfalcon.imageviewer.R

import com.stfalcon.imageviewer.common.extensions.animateAlpha
import com.stfalcon.imageviewer.common.extensions.applyMargin
import com.stfalcon.imageviewer.common.extensions.isRectVisible
import com.stfalcon.imageviewer.common.extensions.isVisible
import com.stfalcon.imageviewer.common.extensions.makeInvisible
import com.stfalcon.imageviewer.common.extensions.makeVisible
import com.stfalcon.imageviewer.common.extensions.switchVisibilityWithAnimation
import com.stfalcon.imageviewer.common.gestures.detector.SimpleOnGestureListener
import com.stfalcon.imageviewer.common.gestures.direction.SwipeDirection
import com.stfalcon.imageviewer.common.gestures.direction.SwipeDirection.*
import com.stfalcon.imageviewer.common.gestures.direction.SwipeDirectionDetector
import com.stfalcon.imageviewer.common.gestures.dismiss.SwipeToDismissHandler
import com.stfalcon.imageviewer.common.pager.MultiTouchViewPager
import com.stfalcon.imageviewer.common.pager.RecyclingPagerAdapter
import com.stfalcon.imageviewer.loader.BindItemView
import com.stfalcon.imageviewer.loader.CreateItemView
import com.stfalcon.imageviewer.loader.GetViewType
import com.stfalcon.imageviewer.loader.ImageLoader
import com.stfalcon.imageviewer.viewer.adapter.ImagesPagerAdapter
import kotlin.math.abs


internal class ImageViewerView<T> @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    internal var isZoomingAllowed = true
    internal var isSwipeToDismissAllowed = true

    internal var currentPosition: Int
        get() = imagesPager.currentItem
        set(value) {
            imagesPager.currentItem = value
        }

    internal var onDismiss: (() -> Unit)? = null
    internal var onPageChange: ((position: Int) -> Unit)? = null

    private val isScaled
        get() = imagesAdapter?.isScaled(currentPosition) ?: false

    private var topOrBottom: Int = ImagesPagerAdapter.IMAGE_POSITION_DEFAULT

    private val isInitState
        get() = imagesAdapter?.isInitState(currentPosition) ?: true

    internal var containerPadding = intArrayOf(0, 0, 0, 0)

    internal var imagesMargin
        get() = imagesPager.pageMargin
        set(value) {
            imagesPager.pageMargin = value
        }

    internal var overlayView: View? = null
        set(value) {
            field = value
            value?.let { rootContainer.addView(it) }
        }

    private var rootContainer: ViewGroup
    private var backgroundView: View
    private var dismissContainer: ViewGroup

    private val transitionImageContainer: FrameLayout
    private var externalTransitionImageView: ImageView? = null

    private var imagesPager: MultiTouchViewPager
    private var imagesAdapter: ImagesPagerAdapter<T>? = null

    private var directionDetector: SwipeDirectionDetector
    private var gestureDetector: GestureDetectorCompat
    private var scaleDetector: ScaleGestureDetector
    private lateinit var swipeDismissHandler: SwipeToDismissHandler

    private var wasScaled: Boolean = false
    private var wasDoubleTapped = false
    private var isOverlayWasClicked: Boolean = false
    private var swipeDirection: SwipeDirection? = null

    private var images: List<T> = listOf()
    private var imageLoader: ImageLoader<T>? = null
    private lateinit var transitionImageAnimator: TransitionImageAnimator
    private var trackEnable = false
    private var isStartInit = true

    var viewType = RecyclingPagerAdapter.VIEW_TYPE_IMAGE
    private var startPosition: Int = 0
        set(value) {
            field = value
            currentPosition = value
        }

    private val shouldDismissToBottom: Boolean
        get() = externalTransitionImageView == null
                || !externalTransitionImageView.isRectVisible
                || !isAtStartPosition

    private val isAtStartPosition: Boolean
        get() = currentPosition == startPosition

    init {
        View.inflate(context, R.layout.view_image_viewer, this)

        rootContainer = findViewById(R.id.rootContainer)
        backgroundView = findViewById(R.id.backgroundView)
        dismissContainer = findViewById(R.id.dismissContainer)

        transitionImageContainer = findViewById(R.id.transitionImageContainer)

        imagesPager = findViewById(R.id.imagesPager)
        imagesPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                if (isStartInit) {
                    val currentView = imagesAdapter?.getPrimaryItem()
                    isStartInit = false
                    transitionImageAnimator =
                        createTransitionImageAnimator(externalTransitionImageView, currentView)
                    animateOpen()
                    imagesPager.makeVisible()
                }
            }

            override fun onPageSelected(position: Int) {
                externalTransitionImageView?.apply {
                    if (isAtStartPosition) makeInvisible() else makeVisible()
                }
                onPageChange?.invoke(position)

            }

            override fun onPageScrollStateChanged(state: Int) {

            }

        })


        directionDetector = createSwipeDirectionDetector()
        gestureDetector = createGestureDetector()
        scaleDetector = createScaleGestureDetector()

    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (overlayView.isVisible && overlayView?.dispatchTouchEvent(event) == true) {
            return true
        }

        if (!this::transitionImageAnimator.isInitialized || transitionImageAnimator.isAnimating) {
            return true
        }

        //one more tiny kludge to prevent single tap a one-finger zoom which is broken by the SDK
        if (wasDoubleTapped &&
            event.action == MotionEvent.ACTION_MOVE &&
            event.pointerCount == 1
        ) {
            return true
        }

        viewType = imagesAdapter?.getViewType(currentPosition)!!
        topOrBottom = imagesAdapter?.isTopOrBottom(currentPosition)!!
        trackEnable = handleEventAction(event, topOrBottom)


        handleUpDownEvent(event)

        if (swipeDirection == null && (scaleDetector.isInProgress || event.pointerCount > 1 || wasScaled)) {
            wasScaled = true
            return imagesPager.dispatchTouchEvent(event)
        }


        if (viewType == RecyclingPagerAdapter.VIEW_TYPE_SUBSAMPLING_IMAGE) { //subsamplingView需要单独处理长图滑动状态
            //放大情况下正常滑动预览
            return if (!trackEnable && isScaled) {
                imagesPager.dispatchTouchEvent(event)
            } else {
                handleTouchIfNotScaled(event)
            }
        }
        return if (isScaled) super.dispatchTouchEvent(event) else handleTouchIfNotScaled(event)
    }


    override fun setBackgroundColor(color: Int) {
        findViewById<View>(R.id.backgroundView).setBackgroundColor(color)
    }

    internal fun setImages(
        images: List<T>,
        startPosition: Int,
        imageLoader: ImageLoader<T>,
        getViewType: GetViewType,
        createItemView: CreateItemView,
        bindItemView: BindItemView<T>
    ) {
        this.images = images
        this.imageLoader = imageLoader
        this.imagesAdapter =
            ImagesPagerAdapter(
                context,
                images,
                imageLoader,
                isZoomingAllowed,
                getViewType,
                createItemView,
                bindItemView
            )
        this.imagesPager.adapter = imagesAdapter
        this.startPosition = startPosition
    }

    internal fun open(transitionImageView: ImageView?, animate: Boolean) {
        prepareViewsForTransition()
        externalTransitionImageView = transitionImageView
        swipeDismissHandler = createSwipeToDismissHandler()
        rootContainer.setOnTouchListener(swipeDismissHandler)
    }


    internal fun close() {
        if (shouldDismissToBottom) {
            swipeDismissHandler.initiateDismissToBottom()
        } else {
            animateClose()
        }
    }

    internal fun updateImages(images: List<T>) {
        this.images = images
        imagesAdapter?.updateImages(images)
    }

    internal fun updateTransitionImage(imageView: ImageView?) {
        externalTransitionImageView?.makeVisible()
//        imageView?.makeInvisible()

        externalTransitionImageView = imageView
        startPosition = currentPosition
        val currentItem = imagesAdapter?.getPrimaryItem()
        transitionImageAnimator = createTransitionImageAnimator(imageView, currentItem)
    }

    private fun animateOpen() {
        transitionImageAnimator.animateOpen(
            onTransitionStart = { duration ->
                backgroundView.animateAlpha(0f, 1f, duration)
                overlayView?.animateAlpha(0f, 1f, duration)
            },
            onTransitionEnd = { prepareViewsForViewer() })
    }

    private fun animateClose() {
        dismissContainer.applyMargin(0, 0, 0, 0)
        val currentView = imagesAdapter?.getPrimaryItem()
        transitionImageAnimator.updateTransitionView(currentView)
        externalTransitionImageView!!.makeVisible()
        transitionImageAnimator.animateClose(
            shouldDismissToBottom = shouldDismissToBottom,
            onTransitionStart = { duration ->
                backgroundView.animateAlpha(backgroundView.alpha, 0f, duration)
                overlayView?.animateAlpha(overlayView?.alpha, 0f, duration)
            },
            onTransitionEnd = { onDismiss?.invoke() })
    }

    private fun prepareViewsForTransition() {
        transitionImageContainer.makeVisible()
        imagesPager.makeInvisible()
    }

    private fun prepareViewsForViewer() {
        backgroundView.alpha = 1f
        imagesPager.makeVisible()
    }

    private fun handleTouchIfNotScaled(event: MotionEvent): Boolean {
        directionDetector.handleTouchEvent(event)
        return when (swipeDirection) {
            UP, DOWN -> {
                if (isSwipeToDismissAllowed && !wasScaled && imagesPager.isIdle) {
                    swipeDismissHandler.onTouch(rootContainer, event)
                } else true
            }
            LEFT, RIGHT -> {
                imagesPager.dispatchTouchEvent(event)
            }
            else -> true
        }
    }


    private fun handleUpDownEvent(event: MotionEvent) {
        if (event.action == MotionEvent.ACTION_UP) {
            handleEventActionUp(event)
        }

        if (event.action == MotionEvent.ACTION_DOWN) {
            handleEventActionDown(event)
        }

        scaleDetector.onTouchEvent(event)
        gestureDetector.onTouchEvent(event)

    }

    private var startY: Float = 0f

    //    private var limitDistance = rootContainer.height / 20
    private var limitDistance = 20   //最小滑动距离

    /**
     * 判断是否已经到顶部或者底部
     * 顶部 + 下滑 = 取消
     * 底部 + 上滑 = 取消
     * 其他情况下正常滑动
     * */
    private fun handleEventAction(event: MotionEvent, topOrBottom: Int): Boolean {
        var tarckEnable = false
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                startY = event.y
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_MOVE -> {
                val distance = event.y - startY
                if ((distance > 0 && distance > limitDistance) && topOrBottom == ImagesPagerAdapter.IMAGE_POSITION_TOP) {  //下滑手势
                    tarckEnable = true
                } else if ((distance < 0 && abs(distance) > limitDistance) && topOrBottom == ImagesPagerAdapter.IMAGE_POSITION_BOTTOM) {//上滑手势
                    tarckEnable = true
                } else if ((distance > 0 && distance > limitDistance) && isInitState) { //初始状态，往下滑可以退出
                    tarckEnable = true
                }
            }

        }
        return tarckEnable

    }

    private fun handleEventActionDown(event: MotionEvent) {
        swipeDirection = null
        wasScaled = false
        imagesPager.dispatchTouchEvent(event)
        swipeDismissHandler.onTouch(rootContainer, event)
        isOverlayWasClicked = dispatchOverlayTouch(event)
    }

    private fun handleEventActionUp(event: MotionEvent) {
        wasDoubleTapped = false
        swipeDismissHandler.onTouch(rootContainer, event)
        imagesPager.dispatchTouchEvent(event)
        isOverlayWasClicked = dispatchOverlayTouch(event)
    }

    private fun handleSingleTap(event: MotionEvent, isOverlayWasClicked: Boolean) {
        if (overlayView != null && !isOverlayWasClicked) {
            overlayView?.switchVisibilityWithAnimation()
            super.dispatchTouchEvent(event)
        }
    }

    private fun handleSwipeViewMove(translationY: Float, translationLimit: Int) {
        val alpha = calculateTranslationAlpha(translationY, translationLimit)
        backgroundView.alpha = alpha
        overlayView?.alpha = alpha
    }

    private fun dispatchOverlayTouch(event: MotionEvent): Boolean =
        overlayView
            ?.let { it.isVisible && it.dispatchTouchEvent(event) }
            ?: false

    private fun calculateTranslationAlpha(translationY: Float, translationLimit: Int): Float =
        1.0f - 1.0f / translationLimit.toFloat() / 4f * Math.abs(translationY)

    private fun createSwipeDirectionDetector() =
        SwipeDirectionDetector(context) { swipeDirection = it }

    private fun createGestureDetector() =
        GestureDetectorCompat(context, SimpleOnGestureListener(
            onSingleTap = {
                if (imagesPager.isIdle) {
                    handleSingleTap(it, isOverlayWasClicked)
                }
                false
            },
            onDoubleTap = {
                wasDoubleTapped = !isScaled
                false
            }
        ))

    private fun createScaleGestureDetector() =
        ScaleGestureDetector(context, ScaleGestureDetector.SimpleOnScaleGestureListener())

    private fun createSwipeToDismissHandler()
            : SwipeToDismissHandler = SwipeToDismissHandler(
        swipeView = dismissContainer,
        shouldAnimateDismiss = { shouldDismissToBottom },
        onDismiss = { animateClose() },
        onSwipeViewMove = ::handleSwipeViewMove
    )

    private fun createTransitionImageAnimator(
        transitionImageView: ImageView?,
        internalImage: View?
    ) =
        TransitionImageAnimator(
            externalImage = transitionImageView,
            internalImage = internalImage
        )

}