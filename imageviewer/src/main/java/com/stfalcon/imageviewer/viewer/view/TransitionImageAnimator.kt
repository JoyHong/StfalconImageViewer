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
import android.graphics.Point
import android.view.Display
import android.view.MotionEvent
import android.view.View
import android.view.animation.*
import android.widget.ImageView
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.stfalcon.imageviewer.common.extensions.*


internal class TransitionImageAnimator(
    private val externalImage: ImageView?,
    private var internalImage: View?
) {

    companion object {
        private const val TRANSITION_DURATION_OPEN = 200L
        private const val TRANSITION_DURATION_CLOSE = 250L
    }

    internal var isAnimating = false

    private var isClosing = false


    internal fun animateOpen(
        onTransitionStart: (Long) -> Unit,
        onTransitionEnd: () -> Unit
    ) {
        if (externalImage.isRectVisible) {
            onTransitionStart(TRANSITION_DURATION_OPEN)
            doOpenTransition(onTransitionEnd)
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

    private fun doOpenTransition(onTransitionEnd: () -> Unit) {
        isAnimating = true
        startAnimation(internalImage,externalImage,onTransitionEnd,true)

    }


    private fun doCloseTransition(onTransitionEnd: () -> Unit) {
        isAnimating = true
        isClosing = true
        startAnimation(internalImage,externalImage,onTransitionEnd,false)
    }


    fun updateTransitionView(itemView: View?) {
        this.internalImage = itemView!!
    }

    private fun startAnimation(itemView : View?, externalImage: ImageView?,onTransitionEnd: (() -> Unit)? = null, isOpen : Boolean){
        //缩放动画
        val toX = externalImage!!.width * 1f/ itemView!!.width / itemView.scaleX

        //以自己为中心进行缩放
        val scaleAnimation : ScaleAnimation = if (isOpen){
            ScaleAnimation(toX,1f,toX,1f,Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f)
        }else{
            ScaleAnimation(1f,toX,1f,toX,Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f)
        }

        //平移到外部imageView的中心点
        val location = IntArray(2)
        externalImage.getLocationOnScreen(location)

        val externalCenterX = (location[0] + externalImage.width /2)
        val externalCenterY = (location[1] + externalImage.height /2)

        //获取itemView中心点
        val itemViewLocation = IntArray(2)
        itemView.getLocationOnScreen(itemViewLocation)


//        val centerX = itemViewLocation[0] + itemView.width / 2 * itemView.scaleX  - itemView.translationX / 2
//        val centerY = itemViewLocation[1] + itemView.height / 2 * itemView.scaleY - itemView.translationY / 2

        val centerX = itemViewLocation[0] + itemView.width / 2 * itemView.scaleX
        val centerY = itemViewLocation[1] + itemView.height / 2 * itemView.scaleX

        System.out.println("centerX==" + centerX + "itemView.scaleX==" + itemView.scaleX)
        System.out.println("centerY==" + centerY)
        val toXValue = (externalCenterX - centerX ) * 1f
        val toYValue= (externalCenterY - centerY ) * 1f

        val translateAnimation : TranslateAnimation = if (isOpen){
            TranslateAnimation(Animation.ABSOLUTE,toXValue,Animation.ABSOLUTE,0f,Animation.ABSOLUTE,toYValue,Animation.ABSOLUTE,0f)
        }else{
            TranslateAnimation(Animation.ABSOLUTE,0f,Animation.ABSOLUTE,toXValue,Animation.ABSOLUTE,0f,Animation.ABSOLUTE,toYValue)
        }

        val animationSet = AnimationSet(true)
        animationSet.addAnimation(scaleAnimation)
        animationSet.addAnimation(translateAnimation)
        animationSet.duration = TRANSITION_DURATION_CLOSE;
        animationSet.fillAfter = true;
        itemView.startAnimation(animationSet)

        animationSet.setAnimationListener(object : Animation.AnimationListener{
            override fun onAnimationStart(p0: Animation?) {

            }

            override fun onAnimationEnd(p0: Animation?) {
                //结束以后关闭dialog即可
                if (!isClosing) {
                    isAnimating = false
                }
                onTransitionEnd?.invoke()
            }

            override fun onAnimationRepeat(p0: Animation?) {

            }
        })
    }

    fun startDragAnimation(distanceX: Float, distanceY: Float, itemView: View?, backgroundView: View, overlayView: View?, activity: Activity,moveDistanceY:Float) {
//        itemView?.translationX = distanceX
//        itemView?.translationY = distanceY

        itemView?.offsetTopAndBottom(distanceY.toInt())
        itemView?.offsetLeftAndRight(distanceX.toInt())

        val display: Display = activity.getWindowManager().getDefaultDisplay()
        val point = Point()
        display.getSize(point)
        val screenHeight = point.y
        val scale = (screenHeight - moveDistanceY) * 1f / screenHeight
        itemView?.scaleX = scale
        itemView?.scaleY = scale

        backgroundView.alpha = scale
        overlayView?.alpha = scale

    }

    fun startResetAnimation(
        event: MotionEvent,
        itemView: View?,
        backgroundView: View,
        overlayView: View?
    ) {
        itemView?.scaleX = 1f
        itemView?.scaleY = 1f
        backgroundView.alpha = 1f
        overlayView?.alpha = 1f
        //从现有位置重新移动屏幕左上角
        itemView!!.x = 0f
        itemView!!.y = 0f


        //获取itemView中心点
//        val itemViewLocation = IntArray(2)
//        itemView?.getLocationOnScreen(itemViewLocation)
//        val centerX = itemViewLocation[0] + itemView!!.width / 2 * itemView.scaleX
//        val centerY = itemViewLocation[1] + itemView.height / 2 * itemView.scaleX
//        val activity = itemView?.context as Activity
//        val display: Display = activity.getWindowManager().getDefaultDisplay()
//        val point = Point()
//        display.getSize(point)
//
//        val toXValue = (point.x / 2 - centerX) * 1f
//        val toYValue= (point.y / 2 - centerY) * 1f
//
//        val translateAnimation = TranslateAnimation(
//            Animation.ABSOLUTE,
//            0f,
//            Animation.ABSOLUTE,
//            toXValue,
//            Animation.ABSOLUTE,
//            0f,
//            Animation.ABSOLUTE,
//            toYValue
//        )
//        translateAnimation.duration = TRANSITION_DURATION_CLOSE;
//        translateAnimation.fillAfter = true;
//        itemView.startAnimation(translateAnimation)

    }

}