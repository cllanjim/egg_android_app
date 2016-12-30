package com.lingyang.camera.ui.widget;

import android.content.Context;
import android.util.AttributeSet;

import java.util.ArrayList;

/**
 * 文件名: CustomTabPageIndcator
 * 描    述: [自定义TabPageIndcator，重写Tab点击跳转功能]
 * 创建人: 廖蕾
 * 创建时间: 2015/12/15
 */
public class CustomTabPageIndicator extends TabPageIndicator {

    OnRedirectListener mOnRedirectListener;
    int mPosition;
    ArrayList<Integer> mRedirectTabPageList;

    public CustomTabPageIndicator(Context context) {
        this(context, null);
    }

    public CustomTabPageIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        mRedirectTabPageList = new ArrayList<Integer>();
    }

    public void addRedirectTabPage(int position) {
        if (mRedirectTabPageList!=null) {
            mRedirectTabPageList.add(position);
        }
    }

    @Override
    public void setViewPagerCurrentItem(int selected) {
        if (mRedirectTabPageList.contains(selected)) {
            mOnRedirectListener.redirect(selected);
        } else {
            super.setViewPagerCurrentItem(selected);
        }
    }

    public void setOnRedirectListener(OnRedirectListener l) {
        mOnRedirectListener = l;
    }

    public interface OnRedirectListener {
        void redirect(int selected);
    }
}
