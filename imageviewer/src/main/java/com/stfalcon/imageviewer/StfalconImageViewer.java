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

package com.stfalcon.imageviewer;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.DimenRes;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

import com.stfalcon.imageviewer.listeners.OnDismissListener;
import com.stfalcon.imageviewer.listeners.OnImageChangeListener;
import com.stfalcon.imageviewer.listeners.OnStateListener;
import com.stfalcon.imageviewer.loader.GetViewSize;
import com.stfalcon.imageviewer.loader.GetViewType;
import com.stfalcon.imageviewer.loader.ImageLoader;
import com.stfalcon.imageviewer.loader.OnCreateView;
import com.stfalcon.imageviewer.viewer.builder.BuilderData;
import com.stfalcon.imageviewer.viewer.dialog.ImageViewerDialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//N.B.! This class is written in Java for convenient use of lambdas due to languages compatibility issues.
@SuppressWarnings({"unused", "WeakerAccess"})
public class StfalconImageViewer<T> {

    private final Context context;
    private final BuilderData<T> builderData;
    private final ImageViewerDialog<T> dialog;

    protected StfalconImageViewer(@NonNull Context context, @NonNull BuilderData<T> builderData) {
        this.context = context;
        this.builderData = builderData;
        this.dialog = new ImageViewerDialog<>(context, builderData);
    }

    /**
     * Displays the built viewer if passed list of images is not empty
     */
    public void show(FragmentManager fm) {
        if (!builderData.getImages().isEmpty()) {
            dialog.show(fm);
        }
    }

    /**
     * Closes the viewer with suitable close animation
     */
    public void close() {
        dialog.close();
    }

    /**
     * Dismisses the dialog with no animation
     */
    public void dismiss() {
        dialog.dismiss();
    }

    /**
     * Updates an existing images list if a new list is not empty, otherwise closes the viewer
     */
    public void updateImages(T[] images) {
        updateImages(new ArrayList<>(Arrays.asList(images)));
    }

    /**
     * Updates an existing images list if a new list is not empty, otherwise closes the viewer
     */
    public void updateImages(List<T> images) {
        if (images.isEmpty()) {
            dialog.close();
        } else {
            dialog.updateImages(images);
        }
    }

    public int getCurrentItem() {
        return dialog.getCurrentItem();
    }

    public void setCurrentItem(int position, boolean smoothScroll) {
        dialog.setCurrentItem(position, smoothScroll);
    }

    /**
     * Updates transition image view.
     * Useful for a case when image position has changed and you want to update the transition animation target.
     */
    public void updateTransitionImage(View view, int position) {
        dialog.updateTransitionImage(view, position);
    }

    /**
     * Builder class for {@link StfalconImageViewer}
     */
    public static class Builder<T> {
        private final Context context;
        private final BuilderData<T> data;

        public Builder(Context context, List<T> images, ImageLoader<T> imageLoader, GetViewType getViewType, GetViewSize getViewSize, OnCreateView createItemView) {
            this.context = context;
            this.data = new BuilderData<>(images, imageLoader, getViewType, getViewSize, createItemView);
        }

        /**
         * Sets a position to start viewer from.
         *
         * @return This Builder object to allow calls chaining
         */
        public Builder<T> withStartPosition(int position) {
            this.data.setStartPosition(position);
            return this;
        }

        /**
         * Sets a background color value for the viewer
         *
         * @return This Builder object to allow calls chaining
         */
        public Builder<T> withBackgroundColor(@ColorInt int color) {
            this.data.setBackgroundColor(color);
            return this;
        }

        /**
         * Sets a background color resource for the viewer
         *
         * @return This Builder object to allow calls chaining
         */
        public Builder<T> withBackgroundColorResource(@ColorRes int color) {
            return this.withBackgroundColor(ContextCompat.getColor(context, color));
        }

        /**
         * Sets custom overlay view to be shown over the viewer.
         * Commonly used for image description or counter displaying.
         *
         * @return This Builder object to allow calls chaining
         */
        public Builder<T> withOverlayView(View view) {
            this.data.setOverlayView(view);
            return this;
        }

        /**
         * Enables or disables custom overlay view switch animation. True by default.
         *
         * @return This Builder object to allow calls chaining
         */
        public Builder<T> withOverlayViewSwitchAnimation(boolean enable) {
            this.data.setOverlayViewSwitchAnimationEnable(enable);
            return this;
        }

        /**
         * Sets space between the images using dimension.
         *
         * @return This Builder object to allow calls chaining
         */
        public Builder<T> withImagesMargin(@DimenRes int dimen) {
            this.data.setImageMarginPixels(Math.round(context.getResources().getDimension(dimen)));
            return this;
        }

        /**
         * Sets space between the images in pixels.
         *
         * @return This Builder object to allow calls chaining
         */
        public Builder<T> withImageMarginPixels(int marginPixels) {
            this.data.setImageMarginPixels(marginPixels);
            return this;
        }

        /**
         * Sets status bar visibility. True by default.
         *
         * @return This Builder object to allow calls chaining
         */
        public Builder<T> withHiddenStatusBar(boolean value) {
            this.data.setShouldStatusBarHide(value);
            return this;
        }

        /**
         * Enables or disables zooming. True by default.
         *
         * @return This Builder object to allow calls chaining
         */
        public Builder<T> allowZooming(boolean value) {
            this.data.setZoomingAllowed(value);
            return this;
        }

        /**
         * Enables or disables the "Swipe to Dismiss" gesture. True by default.
         *
         * @return This Builder object to allow calls chaining
         */
        public Builder<T> allowSwipeToDismiss(boolean value) {
            this.data.setSwipeToDismissAllowed(value);
            return this;
        }

        /**
         * Sets a target {@link ImageView} to be part of transition when opening or closing the viewer/
         *
         * @return This Builder object to allow calls chaining
         */
        public Builder<T> withTransitionFrom(View view) {
            this.data.setTransitionView(view);
            return this;
        }


        /**
         * Sets a target {@link Boolean} Whether dialog uses the theme of the parent class
         *
         * @return This Builder object to allow calls chaining
         */
        public Builder<T> withUseDialogStyle(Boolean useDialogStyle) {
            this.data.setUseDialogStyle(useDialogStyle);
            return this;
        }

        /**
         * 设置状态栏透明
         * @return This Builder object to allow calls chaining
         */
        public Builder<T> withStatusBarTransparent(Boolean statusBarTransparent) {
            this.data.setStatusBarTransparent(statusBarTransparent);
            return this;
        }

        /**
         * Sets {@link OnImageChangeListener} for the viewer.
         *
         * @return This Builder object to allow calls chaining
         */
        public Builder<T> withImageChangeListener(OnImageChangeListener imageChangeListener) {
            this.data.setImageChangeListener(imageChangeListener);
            return this;
        }

        /**
         * Sets {@link OnDismissListener} for viewer.
         *
         * @return This Builder object to allow calls chaining
         */
        public Builder<T> withDismissListener(OnDismissListener onDismissListener) {
            this.data.setOnDismissListener(onDismissListener);
            return this;
        }

        /**
         * Sets {@link OnStateListener} for viewer.
         *
         * @return This Builder object to allow calls chaining
         */
        public Builder<T> withStateListener(OnStateListener onStateListener) {
            this.data.setOnStateListener(onStateListener);
            return this;
        }

        /**
         * Creates a {@link StfalconImageViewer} with the arguments supplied to this builder. It does not
         * show the dialog. This allows the user to do any extra processing
         * before displaying the dialog. Use {@link #show(FragmentManager)} if you don't have any other processing
         * to do and want this to be created and displayed.
         */
        public StfalconImageViewer<T> build() {
            return new StfalconImageViewer<>(context, data);
        }

        /**
         * Creates the {@link StfalconImageViewer} with the arguments supplied to this builder and
         * shows the dialog.
         */
        public StfalconImageViewer<T> show(FragmentManager fm) {
            StfalconImageViewer<T> viewer = build();
            viewer.show(fm);
            return viewer;
        }
    }
}
