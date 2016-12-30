package com.lingyang.camera.util;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;

import com.nostra13.universalimageloader.core.assist.LoadedFrom;
import com.nostra13.universalimageloader.core.display.BitmapDisplayer;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;

/**
 * 文件名: MobileLiveCoverDisplayer
 * 描    述: 手机直播封面裁剪
 * 创建人: 杜舒
 * 创建时间: 2016/1/28
 */
public class MobileLiveCoverDisplayer implements BitmapDisplayer {

    public static final int CORNER_NONE = 0;
    public static final int CORNER_TOP_LEFT = 1;
    public static final int CORNER_TOP_RIGHT = 1 << 1;
    public static final int CORNER_BOTTOM_LEFT = 1 << 2;
    public static final int CORNER_BOTTOM_RIGHT = 1 << 3;
    public static final int CORNER_ALL = CORNER_TOP_LEFT | CORNER_TOP_RIGHT | CORNER_BOTTOM_LEFT | CORNER_BOTTOM_RIGHT;
    protected final int cornerRadius;
    protected final int margin;
    protected final int cornors;

    public MobileLiveCoverDisplayer(int cornerRadiusPixels, int cornors) {
        this(cornerRadiusPixels, 0, cornors);
    }

    public MobileLiveCoverDisplayer(int cornerRadiusPixels, int marginPixels, int cornors) {
        this.cornerRadius = cornerRadiusPixels;
        this.margin = marginPixels;
        this.cornors = cornors;
    }

    @Override
    public void display(Bitmap bitmap, ImageAware imageAware, LoadedFrom loadedFrom) {
        if (!(imageAware instanceof ImageViewAware)) {
            throw new IllegalArgumentException("ImageAware should wrap ImageView. ImageViewAware is expected.");
        }
        Bitmap cutBitmap = getCutBitmap(bitmap);
        imageAware.setImageDrawable(new RoundedDrawable(cutBitmap, cornerRadius, margin, cornors));
    }

    /**
     * 裁剪
     * @param bitmap
     * @return
     */
    private Bitmap getCutBitmap(Bitmap bitmap) {
        return Bitmap.createBitmap(bitmap, 0, bitmap.getHeight() * 3 / 10,
                bitmap.getWidth(), bitmap.getHeight() * 2 / 5);
    }

    public static class RoundedDrawable extends Drawable {

        protected final float cornerRadius;
        protected final int margin;

        protected final RectF mRect = new RectF(),
                mBitmapRect;
        protected final BitmapShader bitmapShader;
        protected final Paint paint;
        private int corners;

        public RoundedDrawable(Bitmap bitmap, int cornerRadius, int margin, int corners) {
            this.cornerRadius = cornerRadius;
            this.margin = margin;
            this.corners = corners;
            bitmapShader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
            mBitmapRect = new RectF(margin, margin, bitmap.getWidth() - margin, bitmap.getHeight() - margin);

            paint = new Paint();
            paint.setAntiAlias(true);
            paint.setShader(bitmapShader);
        }

        @Override
        protected void onBoundsChange(Rect bounds) {
            super.onBoundsChange(bounds);
            mRect.set(margin, margin, bounds.width() - margin, bounds.height() - margin);

            // Resize the original bitmap to fit the new bound
            Matrix shaderMatrix = new Matrix();
            shaderMatrix.setRectToRect(mBitmapRect, mRect, Matrix.ScaleToFit.FILL);
            bitmapShader.setLocalMatrix(shaderMatrix);

        }

        @Override
        public void draw(Canvas canvas) {
            canvas.drawRoundRect(mRect, cornerRadius, cornerRadius, paint);
            int notRoundedCorners = corners ^ CORNER_ALL;
            if ((notRoundedCorners & CORNER_TOP_LEFT) != 0) {
                canvas.drawRect(0, 0, cornerRadius, cornerRadius, paint);
            }
            if ((notRoundedCorners & CORNER_TOP_RIGHT) != 0) {
                canvas.drawRect(mRect.right - cornerRadius, 0, mRect.right, cornerRadius, paint);
            }
            if ((notRoundedCorners & CORNER_BOTTOM_LEFT) != 0) {
                canvas.drawRect(0, mRect.bottom - cornerRadius, cornerRadius, mRect.bottom, paint);
            }
            if ((notRoundedCorners & CORNER_BOTTOM_RIGHT) != 0) {
                canvas.drawRect(mRect.right - cornerRadius, mRect.bottom - cornerRadius, mRect.right, mRect.bottom, paint);
            }
        }

        @Override
        public int getOpacity() {
            return PixelFormat.TRANSLUCENT;
        }

        @Override
        public void setAlpha(int alpha) {
            paint.setAlpha(alpha);
        }

        @Override
        public void setColorFilter(ColorFilter cf) {
            paint.setColorFilter(cf);
        }
    }
}
