package com.stfalcon.sample.common.extensions

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import com.stfalcon.imageviewer.Util.Logger
import java.lang.Exception

fun SubsamplingScaleImageView.loadImage(url: String?){

    Picasso.get().load(url).into(downLoad(this))

}

class downLoad(subsamplingScaleImageView: SubsamplingScaleImageView) :Target{
    var imageView = subsamplingScaleImageView
    override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
        TODO("Not yet implemented")
        imageView.setImage(ImageSource.bitmap(bitmap!!))
    }

    override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
        TODO("Not yet implemented")
        Logger.i(e?.message ?: "no message")
    }

    override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
        TODO("Not yet implemented")
    }

}





