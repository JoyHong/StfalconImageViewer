package com.stfalcon.sample.common.extensions

import android.content.Context
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import com.stfalcon.imageviewer.Util.Logger
import java.io.IOException
import java.io.InputStream

fun SubsamplingScaleImageView.loadImage(url: String?){

//    Picasso.get().load(url).into(downLoad(this))

    this.setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_START)
    val imageFromAssetsFile = getImageFromAssetsFile(this.context,"WechatIMG6.jpg")
    val imageSourcePreview= ImageSource.bitmap(imageFromAssetsFile!!)
        .dimensions(imageFromAssetsFile.width, imageFromAssetsFile.height)

    val imageFromAssetsFileBig = getImageFromAssetsFile(this.context,"WechatIMG6_Big.jpg")
    val imageSource = ImageSource.asset("WechatIMG6_Big.jpg")
        .dimensions(imageFromAssetsFileBig!!.width, imageFromAssetsFileBig.height)

    this.setImage(imageSource,imageSourcePreview)
}

class downLoad(subsamplingScaleImageView: SubsamplingScaleImageView) :Target{
    var imageView = subsamplingScaleImageView
    override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
        imageView.setImage(ImageSource.cachedBitmap(bitmap!!))
    }

    override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
        Logger.i(e?.message ?: "no message")
    }

    override fun onPrepareLoad(placeHolderDrawable: Drawable?) {

    }
}

fun getImageFromAssetsFile(context: Context,fileName: String?): Bitmap? {
    var image: Bitmap? = null
    val am: AssetManager = context.getResources().getAssets()
    try {
        val `is`: InputStream = am.open(fileName.toString())
        image = BitmapFactory.decodeStream(`is`)
        `is`.close()
    } catch (e: IOException) {
        e.printStackTrace()
    }
    return image
}




