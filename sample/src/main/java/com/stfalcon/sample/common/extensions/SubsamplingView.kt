package com.stfalcon.sample.common.extensions

import android.content.Context
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Message

import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView

import java.io.IOException
import java.io.InputStream


fun SubsamplingScaleImageView.loadImage(url: String?){

//    Picasso.get().load(url).into(downLoad(this))

    this.setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_START)

    val myHandler : Handler =  MyHandler(this)
    Thread {
        val thumbnailImage = getImageFromAssetsFile(context, "WechatIMG6.jpg")
        val bigImage = getImageFromAssetsFile(context, "WechatIMG6_Big.jpg")
        val message = Message()
        val bundle = Bundle()
        bundle.putParcelable("thumbnail_image", thumbnailImage)
        bundle.putParcelable("big_image", bigImage)
        message.what = 101
        message.data = bundle
        myHandler.sendMessage(message)
    }.start()
}

private class MyHandler(subsamplingScaleImageView: SubsamplingScaleImageView) : Handler() {
    val subsamplingView = subsamplingScaleImageView
    override fun handleMessage(msg: Message) {
        super.handleMessage(msg)
        when (msg.what) {
            101 -> {
                val bundle : Bundle = msg.data
                val thumbnailImage = bundle.get("thumbnail_image") as Bitmap
                val imageSourcePreview= ImageSource.bitmap(thumbnailImage)
                    .dimensions(thumbnailImage.width, thumbnailImage.height)

                val bigImage =bundle.get("big_image") as Bitmap
                val imageSource = ImageSource.asset("WechatIMG6_Big.jpg")
                    .dimensions(bigImage.width, bigImage.height)
                subsamplingView.setImage(imageSource,imageSourcePreview)
            }
        }
    }
}




fun getImageFromAssetsFile(context: Context,fileName: String?): Bitmap? {
    var image: Bitmap? = null
    val am: AssetManager = context.resources.assets
    try {
        val `is`: InputStream = am.open(fileName.toString())
        image = BitmapFactory.decodeStream(`is`)
        `is`.close()
    } catch (e: IOException) {
        e.printStackTrace()
    }

    return image
}




