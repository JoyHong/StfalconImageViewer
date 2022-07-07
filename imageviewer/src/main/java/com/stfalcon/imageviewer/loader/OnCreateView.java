package com.stfalcon.imageviewer.loader;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;

public interface OnCreateView {

    /**
     * 根据自己的视图类型创建PagerAdapter里面的itemView对象
     * @param context 上下文
     * @param viewType  itemView的类型
     * @return  View  创建的itemVIew
     */
    View createView(Context context, int viewType);
}