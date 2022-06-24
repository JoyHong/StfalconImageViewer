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


import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.view.View
import android.view.animation.*
import com.stfalcon.imageviewer.common.extensions.*



internal class TransitionImageAnimator(
    private val externalImage: View?,
    private var internalImage: View?
) {

    companion object {
        private const val TRANSITION_DURATION_OPEN = 200L
        private const val TRANSITION_DURATION_CLOSE = 250L
    }

    internal var isAnimating = false
    private var isClosing = false
    private var scaleNumber: Float = 0f
    private var resetToXValue: Float = 0f
    private var resetToYValue: Float = 0f

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

    internal fun transitionAnimateClose(
        translationX: Float,
        translationY: Float,
        scaleTemp: Float,
        shouldDismissToBottom: Boolean,
        onTransitionStart: (Long) -> Unit,
        onTransitionEnd: () -> Unit
    ) {
        if (externalImage.isRectVisible && !shouldDismissToBottom) {
            onTransitionStart(TRANSITION_DURATION_CLOSE)
            doCloseTransition(translationX, translationY, scaleTemp, onTransitionEnd)
        } else {
            externalImage?.visibility = View.VISIBLE
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
        startAnimation(internalImage, externalImage, onTransitionEnd, true)

    }


    private fun doCloseTransition(
        translationX: Float,
        translationY: Float,
        scaleTemp: Float,
        onTransitionEnd: () -> Unit
    ) {
        isAnimating = true
        isClosing = true

        val p1: PropertyValuesHolder =
            PropertyValuesHolder.ofFloat("translationX", translationX, resetToXValue)
        val p2: PropertyValuesHolder =
            PropertyValuesHolder.ofFloat("translationY", translationY, resetToYValue)
        val p3: PropertyValuesHolder =
            PropertyValuesHolder.ofFloat("scaleX", scaleTemp, scaleNumber)
        val p4: PropertyValuesHolder =
            PropertyValuesHolder.ofFloat("scaleY", scaleTemp, scaleNumber)
        val animator: ObjectAnimator =
            ObjectAnimator.ofPropertyValuesHolder(internalImage, p1, p2, p3, p4)
        animator.duration = TRANSITION_DURATION_CLOSE
        animator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(p0: Animator?) {

            }

            override fun onAnimationEnd(p0: Animator?) {
                if (!isClosing) {
                    isAnimating = false
                }
                onTransitionEnd.invoke()


            }

            override fun onAnimationCancel(p0: Animator?) {

            }

            override fun onAnimationRepeat(p0: Animator?) {

            }
        })
        animator.start()

    }


    private fun doCloseTransition(onTransitionEnd: () -> Unit) {
        isAnimating = true
        isClosing = true
        startAnimation(internalImage, externalImage, onTransitionEnd, false)

    }


    fun updateTransitionView(itemView: View?, externalImage: View?) {
        this.internalImage = itemView!!

        //缩放动画
        val toX = externalImage!!.width * 1f / itemView.width / itemView.scaleX
        //保存缩放比例,拖动缩小后恢复到原图大小需用到比例
        scaleNumber = toX

        //平移到外部imageView的中心点
        val location = IntArray(2)
        externalImage.getLocationOnScreen(location)

        val externalCenterX = (location[0] + externalImage.width / 2)
        val externalCenterY = (location[1] + externalImage.height / 2)

        //获取itemView中心点
        val itemViewLocation = IntArray(2)
        itemView.getLocationOnScreen(itemViewLocation)


        val centerX = itemViewLocation[0] + itemView.width / 2 * itemView.scaleX
        val centerY = itemViewLocation[1] + itemView.height / 2 * itemView.scaleY

        val toXValue = (externalCenterX - centerX) * 1f
        val toYValue = (externalCenterY - centerY) * 1f


        resetToXValue = toXValue
        resetToYValue = toYValue

    }

    private fun startAnimation(
        itemView: View?,
        externalImage: View?,
        onTransitionEnd: (() -> Unit)? = null,
        isOpen: Boolean
    ) {

        //缩放动画
        val toX = externalImage!!.width * 1f / itemView!!.width / itemView.scaleX

        //保存缩放比例,拖动缩小后恢复到原图大小需用到比例
        scaleNumber = toX

        //以自己为中心进行缩放
        val scaleAnimation: ScaleAnimation = if (isOpen) {
            ScaleAnimation(
                toX,
                1f,
                toX,
                1f,
                Animation.RELATIVE_TO_SELF,
                0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f
            )
        } else {
            ScaleAnimation(
                1f,
                toX,
                1f,
                toX,
                Animation.RELATIVE_TO_SELF,
                0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f
            )
        }

        //平移到外部imageView的中心点
        val location = IntArray(2)
        externalImage.getLocationOnScreen(location)

        val externalCenterX = (location[0] + externalImage.width / 2)
        val externalCenterY = (location[1] + externalImage.height / 2)

        //获取itemView中心点
        val itemViewLocation = IntArray(2)
        itemView.getLocationOnScreen(itemViewLocation)


        val centerX = itemViewLocation[0] + itemView.width / 2 * itemView.scaleX
        val centerY = itemViewLocation[1] + itemView.height / 2 * itemView.scaleY

        val toXValue = (externalCenterX - centerX) * 1f
        val toYValue = (externalCenterY - centerY) * 1f

        resetToXValue = toXValue
        resetToYValue = toYValue

        val translateAnimation: TranslateAnimation = if (isOpen) {
            TranslateAnimation(
                Animation.ABSOLUTE,
                toXValue,
                Animation.ABSOLUTE,
                0f,
                Animation.ABSOLUTE,
                toYValue,
                Animation.ABSOLUTE,
                0f
            )
        } else {
            TranslateAnimation(
                Animation.ABSOLUTE,
                0f,
                Animation.ABSOLUTE,
                toXValue,
                Animation.ABSOLUTE,
                0f,
                Animation.ABSOLUTE,
                toYValue
            )
        }

        val animationSet = AnimationSet(true)
        animationSet.addAnimation(scaleAnimation)
        animationSet.addAnimation(translateAnimation)
        animationSet.duration = TRANSITION_DURATION_CLOSE
        animationSet.fillAfter = true
        itemView.startAnimation(animationSet)

        animationSet.setAnimationListener(object : Animation.AnimationListener {
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

}