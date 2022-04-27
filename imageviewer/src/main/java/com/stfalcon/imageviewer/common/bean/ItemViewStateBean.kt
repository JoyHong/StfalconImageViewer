package com.stfalcon.imageviewer.common.bean

import com.stfalcon.imageviewer.viewer.adapter.ImagesPagerAdapter

class ItemViewStateBean {
    var isScaled: Boolean = false
    var isInitState: Boolean = true
    var topOrBottom: Int = ImagesPagerAdapter.IMAGE_POSITION_DEFAULT
}