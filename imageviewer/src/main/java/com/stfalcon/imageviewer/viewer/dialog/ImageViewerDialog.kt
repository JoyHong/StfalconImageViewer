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

package com.stfalcon.imageviewer.viewer.dialog

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.DialogInterface.*
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.FragmentManager
import com.stfalcon.imageviewer.R
import com.stfalcon.imageviewer.viewer.builder.BuilderData
import com.stfalcon.imageviewer.viewer.view.ImageViewerView

internal class ImageViewerDialog<T>(
    context: Context,
    private val builderData: BuilderData<T>
) {

    private val viewerView: ImageViewerView<T> = ImageViewerView(context)
    private var dialog: ImageViewerDialogFragment<T>? = null

    private val dialogStyle
        get() = if (builderData.shouldStatusBarHide)
            R.style.ImageViewerDialog_NoStatusBar
        else
            R.style.ImageViewerDialog_Default

    init {
        setupViewerView()
    }

    fun show(fm: FragmentManager) {
        dialog = ImageViewerDialogFragment(viewerView,
            if (builderData.useDialogStyle) dialogStyle else 0,
            object : OnKeyListener {
                override fun onKey(dialog: DialogInterface, keyCode: Int, event: KeyEvent
                ): Boolean {
                    return onDialogKeyEvent(keyCode, event)
                }
            },
            object : OnShowListener {
                override fun onShow(dialog: DialogInterface) {
                    viewerView.open(builderData.transitionView, builderData.startPosition)
                }
            },
            object : OnDismissListener {
                override fun onDismiss(dialog: DialogInterface?) {
                    builderData.onDismissListener?.onDismiss()
                }
            },
            builderData.statusBarTransparent
        ).apply {
            show(fm, "ImageViewerDialog")
        }
    }

    fun close() {
        viewerView.close()
    }

    fun dismiss() {
        dialog?.dismissAllowingStateLoss()
    }

    fun updateImages(images: List<T>) {
        viewerView.updateImages(images)
    }

    fun getCurrentItem(): Int =
        viewerView.currentItem

    fun setCurrentItem(item: Int, smoothScroll: Boolean) {
        viewerView.setCurrentItem(item, smoothScroll)
    }

    fun updateTransitionImage(view: View?, position: Int) {
        viewerView.updateTransitionImage(view, position)
    }

    private fun onDialogKeyEvent(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK &&
            event.action == KeyEvent.ACTION_UP &&
            !event.isCanceled
        ) {
            viewerView.close()
            return true
        }
        return false
    }

    private fun setupViewerView() {
        viewerView.apply {
            isSwipeToDismissAllowed = builderData.isSwipeToDismissAllowed
            overlayView = builderData.overlayView
            overlayViewSwitchAnimationEnable = builderData.overlayViewSwitchAnimationEnable
            setBackgroundColor(builderData.backgroundColor)
            setImages(
                builderData.images,
                builderData.startPosition,
                builderData.imageLoader,
                builderData.getViewType,
                builderData.getViewSize,
                builderData.createItemView
            )
            onPageChanged = { position ->
                builderData.imageChangeListener?.onImageChange(position)
            }
            onAnimationStart = { willDismiss ->
                builderData.onStateListener?.onAnimationStart(viewerView, willDismiss)
            }
            onAnimationEnd = { willDismiss ->
                builderData.onStateListener?.onAnimationEnd(viewerView, willDismiss)
            }
            onTrackingStart = {
                builderData.onStateListener?.onTrackingStart(viewerView)
            }
            onTrackingEnd = {
                builderData.onStateListener?.onTrackingEnd(viewerView)
            }
            onDismiss = {
                dialog?.dismissAllowingStateLoss()
            }
        }
    }

    internal class ImageViewerDialogFragment<T> constructor(
        private val viewerView: ImageViewerView<T>? = null,
        private val themeResId: Int = 0,
        private val onKeyListener: DialogInterface.OnKeyListener? = null,
        private val onShowListener: OnShowListener? = null,
        private val onDismissListener: OnDismissListener? = null,
        private val statusBarTransparent: Boolean = false
    ) : AppCompatDialogFragment() {

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            return (if (themeResId > 0) {
                AlertDialog.Builder(requireContext(), themeResId)
            } else {
                AlertDialog.Builder(requireContext())
            }).setView(viewerView)
                .setOnKeyListener(onKeyListener)
                .create().apply {
                    setOnShowListener(onShowListener)
//                    setOnDismissListener(onDismissListener)
                    if (statusBarTransparent) {
                        val window = window!!
                        val lp = window.attributes
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                lp.layoutInDisplayCutoutMode =
                                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
                            }
                            window.attributes = lp
                            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)

                            window.statusBarColor = Color.TRANSPARENT
                            window.navigationBarColor = Color.TRANSPARENT
                            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                        } else {
                            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
                        }
                    }
                    if (viewerView == null) {
                        Handler(Looper.getMainLooper()).post {
                            dismiss();
                        }
                    }
                }
        }

        override fun onDismiss(dialog: DialogInterface) {
            super.onDismiss(dialog)
            onDismissListener?.onDismiss(dialog)
        }

    }

}