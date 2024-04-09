package com.stfalcon.imageviewer.loader;

// 如果布局内容是完整显示, 未做过裁剪的, 则getItemViewSize 返回 null 即可; 否则返回原始尺寸, 不然某些比例的图片在动画缩放时会出现比例不正确
public interface GetViewSize {

    int[] getItemViewSize(int position);

}
