package com.stfalcon.imageviewer.loader;

import android.content.Context;
import android.view.View;

import com.stfalcon.imageviewer.common.bean.ItemViewStateBean;

public interface BindItemView<T> {

    /**
     * 根据自己的视图类型创建PagerAdapter里面的itemView对象
     * @param itemView 需要加载的itemView
     * @param viewType  itemView的类型
     * @param position  itemView的position
     * @param imageLoader  itemView的加载数据的回调接口
     * @return  ItemViewStateBean  itemVIew 的状态封装对象
     */
    ItemViewStateBean bindItemView(View itemView, int viewType,int position,ImageLoader<T> imageLoader);
}