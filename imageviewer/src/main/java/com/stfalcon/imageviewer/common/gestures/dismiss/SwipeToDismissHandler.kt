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

package com.stfalcon.imageviewer.common.gestures.dismiss


import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateInterpolator
import com.stfalcon.imageviewer.common.extensions.hitRect
import com.stfalcon.imageviewer.common.extensions.setAnimatorListener

internal class SwipeToDismissHandler(
    private val swipeView: View,
    private val onDismiss: (translationX : Float,translationY : Float,scaleTemp : Float) -> Unit,
    private val onSwipeTrackingStart: () -> Unit,
    private val onSwipeTrackingEnd: () -> Unit,
    private val onSwipeViewMove: (translationY: Float, translationLimit: Int) -> Unit,
    private val onAnimationStart: (willDismiss: Boolean) -> Unit,
    private val onAnimationEnd: (willDismiss: Boolean) -> Unit,
    private val shouldAnimateDismiss: () -> Boolean
) : View.OnTouchListener {
    companion object {
        private const val ANIMATION_DURATION = 200L
    }
    private var translationX = 0f
    private var translationY = 0f
    private var translationLimit: Int = swipeView.height / 9
    private var isTracking = false
    private var isTrackingRecorded = false
    private var startY: Float = 0f
    private var startX: Float = 0f
    private var scaleTemp: Float = 0f
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View, event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (swipeView.hitRect.contains(event.x.toInt(), event.y.toInt())) {
                    isTracking = true
                    isTrackingRecorded = false
                }
                startY = event.y
                startX = event.x
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (isTracking) {
                    isTracking = false
                    if (isTrackingRecorded) {
                        isTrackingRecorded = false
                        val translationX = event.x - startX
                        val translationY = event.y - startY
                        this.translationX = translationX
                        this.translationY = translationY
                        onSwipeTrackingEnd.invoke()
                        onTrackingEnd(v.height,v.width)
                    }
                }
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                if (isTracking) {
                    if (!isTrackingRecorded) {
                        isTrackingRecorded = true
                        onSwipeTrackingStart.invoke()
                    }

                    val translationY = event.y - startY
                    val translationX = event.x - startX
                    swipeView.translationY = translationY
                    swipeView.translationX = translationX

                    var scaleTemp = (swipeView.height - translationY) * 1f / swipeView.height
                    if (scaleTemp > 1){
                        scaleTemp = 1f
                    }

                    swipeView.scaleX = scaleTemp
                    swipeView.scaleY = scaleTemp
                    this.scaleTemp = scaleTemp
                    onSwipeViewMove(translationY, translationLimit)
                }
                return true
            }
            else -> {
                return false
            }
        }
    }

    internal fun initiateDismissToBottom() {
        animateTranslation(swipeView.height.toFloat(), 0F)
    }

    private fun onTrackingEnd(parentHeight: Int,parentWith : Int) {
        val animateToY = when {
            swipeView.translationY < -translationLimit -> {
                -parentHeight.toFloat()

            }
            swipeView.translationY > translationLimit -> {
                parentHeight.toFloat()
            }
            else -> 0f
        }

        val animateToX = when {
            swipeView.translationX < -translationLimit -> {
                -parentWith.toFloat()

            }
            swipeView.translationX > translationLimit -> {
                parentWith.toFloat()
            }
            else -> 0f
        }

        if ((animateToY != 0f || animateToX != 0f) && !shouldAnimateDismiss()) {
            onDismiss(translationX, translationY, scaleTemp)
        } else {
            animateTranslation(animateToY, animateToX)
        }
    }

    private fun animateTranslation(translationToY: Float, translationToX: Float) {
        swipeView.animate()
            .translationY(translationToY)
            .translationX(translationToX)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(ANIMATION_DURATION)
            .setInterpolator(AccelerateInterpolator())
            .setUpdateListener { onSwipeViewMove(swipeView.translationY, translationLimit) }
            .setAnimatorListener(
                onAnimationStart = {
                    onAnimationStart.invoke(false)
                },
                onAnimationEnd = {
                    // remove the update listener, otherwise it will be saved on the next animation execution:
                    swipeView.animate().setUpdateListener(null)
                    if (translationToY != 0f) {
                        onDismiss(translationX, translationY, scaleTemp)
                    }
                    onAnimationEnd.invoke(false)
                })
            .start()
    }
}