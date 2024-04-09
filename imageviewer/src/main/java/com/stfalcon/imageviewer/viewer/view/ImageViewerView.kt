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

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.core.view.GestureDetectorCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import com.stfalcon.imageviewer.R
import com.stfalcon.imageviewer.common.extensions.*
import com.stfalcon.imageviewer.common.gestures.detector.SimpleOnGestureListener
import com.stfalcon.imageviewer.common.gestures.direction.SwipeDirection
import com.stfalcon.imageviewer.common.gestures.direction.SwipeDirection.*
import com.stfalcon.imageviewer.common.gestures.direction.SwipeDirectionDetector
import com.stfalcon.imageviewer.common.gestures.dismiss.SwipeToDismissHandler
import com.stfalcon.imageviewer.common.pager.RecyclingPagerAdapter
import com.stfalcon.imageviewer.loader.GetViewSize
import com.stfalcon.imageviewer.loader.GetViewType
import com.stfalcon.imageviewer.loader.ImageLoader
import com.stfalcon.imageviewer.loader.OnCreateView
import com.stfalcon.imageviewer.viewer.adapter.ImagesPagerAdapter
import kotlin.math.abs

internal class ImageViewerView<T> @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    /**
     * 是否允许拖动退出
     */
    var isSwipeToDismissAllowed = true

    internal val currentItem
        get() = imagesPager.currentItem

    fun setCurrentItem(item: Int, smoothScroll: Boolean = true) {
        imagesPager.setCurrentItem(item, smoothScroll)
    }

    internal var onDismiss: (() -> Unit)? = null
    internal var onPageChanged: ((position: Int) -> Unit)? = null
    internal var onAnimationStart: ((willDismiss: Boolean) -> Unit)? = null
    internal var onAnimationEnd: ((willDismiss: Boolean) -> Unit)? = null
    internal var onTrackingStart: (() -> Unit)? = null
    internal var onTrackingEnd: (() -> Unit)? = null

    private val isScaled
        get() = imagesAdapter?.isScaled(currentItem) ?: false

    private var topOrBottom: Int = ImagesPagerAdapter.IMAGE_POSITION_DEFAULT

    private val isInitState
        get() = imagesAdapter?.isInitState(currentItem) ?: true

    private val scaledSize
        get() = imagesAdapter?.scaledSize(currentItem) ?: 1.0f

    internal var overlayView: View? = null
        set(value) {
            field = value
            value?.let {
                rootContainer.addView(it)
            }
        }
    internal var overlayViewSwitchAnimationEnable = true

    private val rootContainer: ViewGroup
    private val backgroundView: View
    private val dismissContainer: ViewGroup

    private var externalTransitionImageView: View? = null
    private var externalTransitionPosition = -1

    private val imagesPager: ViewPager2
    private var imagesAdapter: ImagesPagerAdapter<T>? = null

    private var getViewSize: GetViewSize? = null

    private val directionDetector: SwipeDirectionDetector
    private val gestureDetector: GestureDetectorCompat
    private val scaleDetector: ScaleGestureDetector
    private lateinit var swipeDismissHandler: SwipeToDismissHandler

    private var wasScaled = false
    private var wasDoubleTapped = false
    private var isOverlayWasClicked = false
    private var swipeDirection: SwipeDirection? = null

    private var transitionImageAnimator: TransitionImageAnimator? = null
    private var trackEnable = false
    private var isFirstChildAttached = false
    private var isIdle = true
    private var viewType = RecyclingPagerAdapter.VIEW_TYPE_IMAGE

    private val shouldDismissToBottom: Boolean
        get() = externalTransitionImageView?.isRectVisible?.not() ?: true

    init {
        View.inflate(context, R.layout.image_viewer_mage_viewer, this)
        rootContainer = findViewById(R.id.rootContainer)
        backgroundView = findViewById(R.id.backgroundView)
        dismissContainer = findViewById(R.id.dismissContainer)
        imagesPager = findViewById(R.id.imagesPager)
        val recyclerView = imagesPager.getChildAt(0) as RecyclerView
        recyclerView.addOnChildAttachStateChangeListener(object :
            RecyclerView.OnChildAttachStateChangeListener {
            override fun onChildViewAttachedToWindow(view: View) {
                if (!isFirstChildAttached) {
                    isFirstChildAttached = true
                    transitionImageAnimator = createTransitionImageAnimator(externalTransitionImageView, getViewSize?.getItemViewSize(externalTransitionPosition), dismissContainer)
                    imagesPager.makeVisible()
                    animateOpen()
                }
            }

            override fun onChildViewDetachedFromWindow(view: View) {
            }
        })
        imagesPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                onPageChanged?.invoke(position)
                // 同步退出动画的缩放比例
                transitionImageAnimator?.scaleSize = scaledSize
            }
            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)
                isIdle = state == ViewPager.SCROLL_STATE_IDLE
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

        if (transitionImageAnimator?.isAnimating != false) {
            return true
        }

        //one more tiny kludge to prevent single tap a one-finger zoom which is broken by the SDK
        if (wasDoubleTapped &&
            event.action == MotionEvent.ACTION_MOVE &&
            event.pointerCount == 1
        ) {
            return true
        }

        if (currentItem >= imagesAdapter?.itemCount ?: 0) {
            return true
        }

        viewType = imagesAdapter?.getItemViewType(currentItem)!!
        topOrBottom = imagesAdapter?.isTopOrBottom(currentItem)!!
        trackEnable = handleEventAction(event, topOrBottom)

        if (event.action == MotionEvent.ACTION_DOWN && !imagesPager.isUserInputEnabled) {
            imagesPager.isUserInputEnabled = true
        }

        handleUpDownEvent(event)

        if (swipeDirection == null && (scaleDetector.isInProgress || event.pointerCount > 1 || wasScaled)) {
            wasScaled = true
            return dispatchTouchEvent2ImagesPager(event)
        }

        if (viewType == RecyclingPagerAdapter.VIEW_TYPE_SUBSAMPLING_IMAGE) { //subsamplingView需要单独处理长图滑动状态
            //放大情况下正常滑动预览
            return if (!trackEnable && isScaled) {
                dispatchTouchEvent2ImagesPager(event)
            } else {
                handleTouchIfNotScaled(event)
            }
        }
        return if (isScaled) dispatchTouchEvent2ImagesPager(event) else handleTouchIfNotScaled(event)
    }

    override fun setBackgroundColor(color: Int) {
        findViewById<View>(R.id.backgroundView).setBackgroundColor(color)
    }

    internal fun setImages(
        images: List<T>,
        startPosition: Int,
        imageLoader: ImageLoader<T>,
        getViewType: GetViewType,
        getViewSize: GetViewSize,
        createItemView: OnCreateView
    ) {
        this.imagesAdapter = ImagesPagerAdapter(
            context,
            images,
            imageLoader,
            getViewType,
            createItemView
        )
        this.imagesPager.adapter = imagesAdapter
        this.imagesPager.setCurrentItem(startPosition, false)
        this.getViewSize = getViewSize
    }

    internal fun updateImages(images: List<T>) {
        imagesAdapter?.updateImages(images)
    }

    internal fun open(transitionImageView: View?, transitionPosition: Int) {
        prepareViewsForTransition()
        externalTransitionImageView = transitionImageView
        externalTransitionPosition = transitionPosition
        swipeDismissHandler = createSwipeToDismissHandler()
        rootContainer.setOnTouchListener(swipeDismissHandler)
    }

    internal fun close() {
        if (shouldDismissToBottom) {
            swipeDismissHandler.initiateDismissToBottom()
        } else {
            if (!transitionImageAnimator!!.isAnimating){
                animateClose()
            }
        }
    }

    internal fun updateTransitionImage(imageView: View?, position: Int) {
        externalTransitionImageView = imageView
        externalTransitionPosition = position
        transitionImageAnimator =
            createTransitionImageAnimator(externalTransitionImageView, getViewSize?.getItemViewSize(externalTransitionPosition), dismissContainer)
        transitionImageAnimator!!.updateTransitionView(
            dismissContainer,
            externalTransitionImageView
        )
    }

    private fun animateOpen() {
        transitionImageAnimator!!.animateOpen(
            onTransitionStart = { duration ->
                backgroundView.animateAlpha(0f, 1f, duration)
                overlayView?.animateAlpha(0f, 1f, duration)
                onAnimationStart?.invoke(false)
            },
            onTransitionEnd = {
                prepareViewsForViewer()
                onAnimationEnd?.invoke(false)
            })
        transitionImageAnimator!!.viewType = viewType
    }

    private fun animateClose(translationX: Float, translationY: Float, scaleTemp: Float) {
        dismissContainer.applyMargin(0, 0, 0, 0)
        transitionImageAnimator!!.transitionAnimateClose(
            translationX = translationX,
            translationY = translationY,
            scaleTemp = scaleTemp,
            shouldDismissToBottom = shouldDismissToBottom,
            onTransitionStart = { duration ->
                backgroundView.animateAlpha(backgroundView.alpha, 0f, duration)
                overlayView?.animateAlpha(overlayView?.alpha, 0f, duration)
                onAnimationStart?.invoke(true)
            },
            onTransitionEnd = {
                onAnimationEnd?.invoke(true)
                onDismiss?.invoke()
            })
        transitionImageAnimator!!.viewType = viewType
    }

    private fun animateClose() {
        if (scaledSize != -1.0f) {
            transitionImageAnimator!!.scaleSize = scaledSize
        }
        dismissContainer.applyMargin(0, 0, 0, 0)
        transitionImageAnimator!!.animateClose(
            shouldDismissToBottom = shouldDismissToBottom,
            onTransitionStart = { duration ->
                backgroundView.animateAlpha(backgroundView.alpha, 0f, duration)
                overlayView?.animateAlpha(overlayView?.alpha, 0f, duration)
                onAnimationStart?.invoke(true)
            },
            onTransitionEnd = {
                onAnimationEnd?.invoke(true)
                onDismiss?.invoke()
            }
        )
        transitionImageAnimator!!.viewType = viewType
    }

    private fun prepareViewsForTransition() {
        backgroundView.alpha = 0f
        imagesPager.makeInvisible()
    }

    private fun prepareViewsForViewer() {
        imagesPager.makeVisible()
    }

    /**
     * handleUpDownEvent 中 imagesPager 已处理了 ACTION_DOWN 和 ACTION_UP，需要过滤这两个事件，避免重复处理
     */
    private fun dispatchTouchEvent2ImagesPager(event: MotionEvent): Boolean {
        return if (event.action != MotionEvent.ACTION_DOWN && event.action != MotionEvent.ACTION_UP) {
            imagesPager.dispatchTouchEvent(event)
        } else {
            true
        }
    }

    private fun handleTouchIfNotScaled(event: MotionEvent): Boolean {
        directionDetector.handleTouchEvent(event)
        return when (swipeDirection) {
            UP, DOWN -> {
                if (isSwipeToDismissAllowed && !wasScaled && isIdle) {
                    if (imagesPager.isUserInputEnabled) {
                        imagesPager.isUserInputEnabled = false
                    }
                    imagesPager.dispatchTouchEvent(event)
                    swipeDismissHandler.onTouch(rootContainer, event)
                } else true
            }
            LEFT, RIGHT -> {
                imagesPager.dispatchTouchEvent(event)
            }
            else -> {
                if (event.action == MotionEvent.ACTION_CANCEL) {
                    imagesPager.dispatchTouchEvent(event)
                } else {
                    true
                }
            }
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
        var trackEnable = false
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                startY = event.y
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_MOVE -> {
                val distance = event.y - startY
                if ((distance > 0 && distance > limitDistance) && topOrBottom == ImagesPagerAdapter.IMAGE_POSITION_TOP) {  //下滑手势
                    trackEnable = true
                } else if ((distance < 0 && abs(distance) > limitDistance) && topOrBottom == ImagesPagerAdapter.IMAGE_POSITION_BOTTOM) {//上滑手势
                    trackEnable = true
                } else if ((distance > 0 && distance > limitDistance) && isInitState) { // 初始状态，往下滑可以退出
                    trackEnable = true
                }
            }
        }
        return trackEnable
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
            if (overlayViewSwitchAnimationEnable) {
                overlayView!!.switchVisibilityWithAnimation()
            }
//            super.dispatchTouchEvent(event)
        }
    }

    private fun handleSwipeViewMove(translationY: Float, translationLimit: Int) {
        val alpha = calculateTranslationAlpha(translationY, translationLimit)
        backgroundView.alpha = alpha
        overlayView?.alpha = alpha
    }

    private fun dispatchOverlayTouch(event: MotionEvent): Boolean =
        overlayView
            ?.let {
                it.isVisible && it.dispatchTouchEvent(event)
            }
            ?: false

    private fun calculateTranslationAlpha(translationY: Float, translationLimit: Int): Float =
        1.0f - 1.0f / translationLimit.toFloat() / 4f * abs(translationY)

    private fun createSwipeDirectionDetector() =
        SwipeDirectionDetector(context) {
            swipeDirection = it
        }

    private fun createGestureDetector() =
        GestureDetectorCompat(context, SimpleOnGestureListener(
            onSingleTap = {
                if (isIdle) {
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

    private fun createSwipeToDismissHandler() =
        SwipeToDismissHandler(
            swipeView = dismissContainer,
            shouldAnimateDismiss = { shouldDismissToBottom },
            onDismiss = { fl: Float, fl1: Float, fl2: Float -> animateClose(fl, fl1, fl2) },
            onSwipeTrackingStart = { onTrackingStart?.invoke() },
            onSwipeTrackingEnd = { onTrackingEnd?.invoke() },
            onSwipeViewMove = ::handleSwipeViewMove,
            onAnimationStart = { willDismiss ->  onAnimationStart?.invoke(willDismiss) },
            onAnimationEnd = { willDismiss -> onAnimationEnd?.invoke(willDismiss) }
        )

    private fun createTransitionImageAnimator(transitionImageView: View?, imageSize: IntArray?, internalImage: View?) =
        TransitionImageAnimator(
            externalImage = transitionImageView,
            internalImage = internalImage,
            imageSize = imageSize
        )

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        imagesPager.adapter = null
    }

}