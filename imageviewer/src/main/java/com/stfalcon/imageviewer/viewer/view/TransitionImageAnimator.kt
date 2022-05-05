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
import android.graphics.Point
import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import android.view.animation.*
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.transition.AutoTransition
import androidx.transition.Transition
import androidx.transition.TransitionManager
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.stfalcon.imageviewer.common.extensions.*
import com.stfalcon.imageviewer.common.pager.RecyclingPagerAdapter

internal class TransitionImageAnimator(
    private val context: Context,
    private val externalImage: ImageView?,
    private var internalImage: View?,
    private val internalImageContainer: FrameLayout
) {

    companion object {
        private const val TRANSITION_DURATION_OPEN = 200L
        private const val TRANSITION_DURATION_CLOSE = 250L
    }

    internal var isAnimating = false

    private var isClosing = false
    private var viewType = RecyclingPagerAdapter.VIEW_TYPE_IMAGE

    private val transitionDuration: Long
        get() = if (isClosing) TRANSITION_DURATION_CLOSE else TRANSITION_DURATION_OPEN

    private val internalRoot: ViewGroup
        get() = internalImageContainer.parent as ViewGroup

    internal fun animateOpen(
        containerPadding: IntArray,
        onTransitionStart: (Long) -> Unit,
        onTransitionEnd: () -> Unit
    ) {
        if (externalImage.isRectVisible) {
            onTransitionStart(TRANSITION_DURATION_OPEN)
            doOpenTransition(containerPadding, onTransitionEnd)
        } else {
            onTransitionEnd()
        }
    }

    internal fun animateClose(
        shouldDismissToBottom: Boolean,
        onTransitionStart: (Long) -> Unit,
        onTransitionEnd: () -> Unit
    ) {
        if (externalImage.isRectVisible && !shouldDismissToBottom) {
            onTransitionStart(TRANSITION_DURATION_CLOSE)
            doCloseTransition(onTransitionEnd)
        } else {
            externalImage?.visibility = View.VISIBLE
            onTransitionEnd()
        }
    }

    private fun doOpenTransition(containerPadding: IntArray, onTransitionEnd: () -> Unit) {
        isAnimating = true

        prepareTransitionLayout()
        internalRoot.postApply {
            //ain't nothing but a kludge to prevent blinking when transition is starting
            externalImage?.postDelayed(50) { visibility = View.INVISIBLE }

            TransitionManager.beginDelayedTransition(internalRoot, createTransition {
                if (!isClosing) {
                    isAnimating = false
                    onTransitionEnd()
                }
            })

            internalImageContainer.makeViewMatchParent()
            internalImage?.makeViewMatchParent()

            internalRoot.applyMargin(
                containerPadding[0],
                containerPadding[1],
                containerPadding[2],
                containerPadding[3])

            internalImageContainer.requestLayout()
        }


    }



    private fun doCloseTransition(onTransitionEnd: () -> Unit) {
        isAnimating = true
        isClosing = true

//        TransitionManager.beginDelayedTransition(
//            internalRoot, createTransition { handleCloseTransitionEnd(onTransitionEnd) })
//
//        prepareTransitionLayout()
//        internalImageContainer.requestLayout()

        startCloseAnimation(internalImage,externalImage,onTransitionEnd)
    }

    private fun prepareTransitionLayout() {
        externalImage?.let {
            if (externalImage.isRectVisible) {
                with(externalImage.localVisibleRect) {
                    internalImage?.requestNewSize(it.width, it.height)
                    internalImage?.applyMargin(top = -top, start = -left)
                    if (viewType == RecyclingPagerAdapter.VIEW_TYPE_TEXT){
                        val textView = internalImage as TextView
                        val size = textView.textSize
                        textView.textSize = it.width*1f / textView.width * size / 4f
                    }

                    if (viewType == RecyclingPagerAdapter.VIEW_TYPE_SUBSAMPLING_IMAGE){
                        val subsamplingScaleImageView = internalImage as SubsamplingScaleImageView
                        subsamplingScaleImageView.maxScale = 1f
                        subsamplingScaleImageView.setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_START)
                    }

                }
                with(externalImage.globalVisibleRect) {
                    internalImageContainer.requestNewSize(width(), height())
                    internalImageContainer.applyMargin(left, top, right, bottom)
                }
            }

            resetRootTranslation()
        }
    }

    private fun handleCloseTransitionEnd(onTransitionEnd: () -> Unit) {
        externalImage?.visibility = View.VISIBLE
        internalImage?.post { onTransitionEnd() }
        isAnimating = false
    }

    private fun resetRootTranslation() {
        internalRoot
            .animate()
            .translationY(0f)
            .setDuration(transitionDuration)
            .start()
    }

    private fun createTransition(onTransitionEnd: (() -> Unit)? = null): Transition =
        AutoTransition()
            .setDuration(transitionDuration)
            .setInterpolator(DecelerateInterpolator())
            .addListener(onTransitionEnd = { onTransitionEnd?.invoke() })

    fun updateTransitionView(itemView: View?, viewType: Int?) {
        this.internalImage = itemView!!
        this.viewType = viewType!!
    }

    fun startCloseAnimation(itemView : View?, externalImage: ImageView?,onTransitionEnd: (() -> Unit)? = null){
        //缩放动画
        val toX = externalImage!!.width * 1f/ itemView!!.width
        var toY = externalImage!!.height * 1f/ itemView!!.height

        //以自己为中心进行缩放
        val scaleAnimation = ScaleAnimation(1f,toX,1f,toX,Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f)
        //平移到外部imageView的中心点
        val location = IntArray(2)
        externalImage?.getLocationOnScreen(location)

        val externalCenterX = (location[0] + externalImage.width /2)
        val externalCenterY = (location[1] + externalImage.height /2)

        val activity = context as Activity
        val display = activity.windowManager.defaultDisplay
        val point = Point()
        with(display) {
            getSize(point)
        }

        //获取状态栏高度
        val resources = context.getResources()
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        val height = resources.getDimensionPixelSize(resourceId)

        //获取屏幕中心点
        val centerX = point.x / 2
        val centerY = (point.y + height)/ 2

        val toXValue = (externalCenterX - centerX) * 1f
        val toYValue= (externalCenterY - centerY) * 1f

        val translateAnimation = TranslateAnimation(Animation.ABSOLUTE,0f,Animation.ABSOLUTE,toXValue,Animation.ABSOLUTE,0f,Animation.ABSOLUTE,toYValue)
        val animationSet = AnimationSet(true)
        animationSet.addAnimation(scaleAnimation)
        animationSet.addAnimation(translateAnimation)
        animationSet.setDuration(TRANSITION_DURATION_CLOSE);
        animationSet.setFillAfter(true);
        itemView.startAnimation(animationSet)

        animationSet.setAnimationListener(object : Animation.AnimationListener{
            override fun onAnimationStart(p0: Animation?) {

            }

            override fun onAnimationEnd(p0: Animation?) {
                //结束以后关闭dialog即可
                externalImage.makeVisible()
                onTransitionEnd?.invoke()
            }

            override fun onAnimationRepeat(p0: Animation?) {

            }

        })
    }

    private fun startOpenTransition(
        itemView : View?,
        externalImage: ImageView?,
        onTransitionEnd: () -> Unit
    ) {
        //缩放动画,从外部ImageView大小到正常大小
        val activity = context as Activity
        val display = activity.windowManager.defaultDisplay
        val point = Point()
        with(display) {
            getSize(point)
        }
        val fromX = externalImage!!.width * 1f/ point.x
        System.out.println("externalImage!!.width=" + externalImage!!.width)
        System.out.println("itemView.width=" + point.x)
        System.out.println("fromX=" + fromX)

        //以自己为中心进行缩放
        val scaleAnimation = ScaleAnimation(1f,2f,1f,2f,Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f)

        scaleAnimation.setDuration(1000);
        scaleAnimation.setFillAfter(true);
        itemView?.startAnimation(scaleAnimation)
        System.out.println("itemView=" + itemView?.width)
        //平移动画,itemView从外部ImageView中心到屏幕中心
//        val location = IntArray(2)
//        externalImage?.getLocationOnScreen(location)
//
//        val externalCenterX = (location[0] + externalImage.width /2)
//        val externalCenterY = (location[1] + externalImage.height /2)
//        val activity = context as Activity
//        val display = activity.windowManager.defaultDisplay
//        val point = Point()
//        with(display) {
//            getSize(point)
//        }
//
//        //获取状态栏高度
//        val resources = context.getResources()
//        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
//        val height = resources.getDimensionPixelSize(resourceId)
//
//        //获取屏幕中心点
//        val centerX = point.x / 2
//        val centerY = (point.y + height)/ 2
//
//        val fromXValue = (externalCenterX - centerX) * 1f
//        val fromYValue= (externalCenterY - centerY) * 1f
//
//        val translateAnimation = TranslateAnimation(Animation.ABSOLUTE,fromXValue,Animation.ABSOLUTE,0f,Animation.ABSOLUTE,fromYValue,Animation.ABSOLUTE,0f)
//        val animationSet = AnimationSet(true)
//        animationSet.addAnimation(scaleAnimation)
//        animationSet.addAnimation(translateAnimation)
//        animationSet.setDuration(TRANSITION_DURATION_CLOSE);
//        animationSet.setFillAfter(true);
//        itemView.startAnimation(animationSet)

        scaleAnimation.setAnimationListener(object : Animation.AnimationListener{
            override fun onAnimationStart(p0: Animation?) {

            }

            override fun onAnimationEnd(p0: Animation?) {
                //结束以后关闭dialog即可
                System.out.println("开启动画结束")
//                onTransitionEnd?.invoke()
            }

            override fun onAnimationRepeat(p0: Animation?) {

            }

        })

    }
}