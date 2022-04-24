package com.stfalcon.sample.common.extensions

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import com.stfalcon.imageviewer.Util.Logger

fun SubsamplingScaleImageView.loadImage(url: String?){

    Picasso.get().load(url).into(downLoad(this))

}

class downLoad(subsamplingScaleImageView: SubsamplingScaleImageView) :Target{
    var imageView = subsamplingScaleImageView
    override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
        imageView.setImage(ImageSource.cachedBitmap(bitmap!!))

//        imageView.setImage(ImageSource.asset("longImage.jpg"))
//        imageView.setImage(ImageSource.asset("sanmartino.jpg"))
    }

    override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
        Logger.i(e?.message ?: "no message")
    }

    override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
        imageView.setImageDrawable(placeHolderDrawable)
    }

}





