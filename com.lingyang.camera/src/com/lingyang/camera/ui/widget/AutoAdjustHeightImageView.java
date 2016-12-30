package com.lingyang.camera.ui.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.lingyang.base.utils.CLog;

public class AutoAdjustHeightImageView extends ImageView {

    private int imageWidth;
    private int imageHeight;

    public AutoAdjustHeightImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        getImageSize();
    }

    private void getImageSize() {
        Drawable background = this.getBackground();
        if (background == null)
            return;
        Bitmap bitmap = ((BitmapDrawable) background).getBitmap();
        imageWidth = bitmap.getWidth();
        imageHeight = bitmap.getHeight();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (imageWidth == 0 || imageHeight == 0) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        } else {
            int width = MeasureSpec.getSize(widthMeasureSpec);
            int height = width * imageHeight / imageWidth;
            CLog.v("onMeasure imageHeight:" + imageHeight + " imageWidth:" + imageWidth);
            this.setMeasuredDimension(width, height);
        }

    }
}