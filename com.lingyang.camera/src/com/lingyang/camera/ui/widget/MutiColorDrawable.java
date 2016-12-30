package com.lingyang.camera.ui.widget;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.ViewDebug;

import com.lingyang.base.utils.CLog;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * @author 波
 *         变色画图资源
 */
public class MutiColorDrawable extends Drawable {
    @ViewDebug.ExportedProperty(deepExport = true, prefix = "state_")
    private ColorState mState;
    private final Paint mPaint = new Paint();
    private boolean mMutated;

    /**
     * Creates a new black ColorDrawable.
     */
    public MutiColorDrawable() {
        this(new ColorState(null));
    }

    /**
     * Creates a new ColorDrawable with the specified color.
     *
     * @param color The color to draw.
     */
    public MutiColorDrawable(MutiColorEntity[] color) {
        this(new ColorState(null));
        setColor(color);
    }

    private MutiColorDrawable(ColorState state) {
        mState = new ColorState(state);
    }

    @Override
    public int getChangingConfigurations() {
        return super.getChangingConfigurations()
                | mState.mChangingConfigurations;
    }

    /**
     * A mutable BitmapDrawable still shares its Bitmap with any other Drawable
     * that comes from the same resource.
     *
     * @return This drawable.
     */
    @Override
    public Drawable mutate() {
        if (!mMutated && super.mutate() == this) {
            mState = new ColorState(mState);
            mMutated = true;
        }
        return this;
    }

    @Override
    public void draw(Canvas canvas) {
        if (mState.mUseColor.length > 0) {
            if ((mState.mUseColor[0].color >>> 24) != 0) {
                CLog.v("draw");
                int left = getBounds().left;
                int right = getBounds().right;
                int width = getBounds().width();
                Rect rect;
                for (MutiColorEntity item : mState.mUseColor) {
                    mPaint.setColor(item.color);
                    rect = new Rect((int) (left + width * item.percent / 100), getBounds()
                            .centerY() - getBounds().height() / 2, right,
                            getBounds().centerY() + getBounds().height() / 2);
                    canvas.drawRect(rect, mPaint);
                }
            }
        }

    }

    /**
     * Gets the drawable's color value.
     *
     * @return int The color to draw.
     */
    public MutiColorEntity[] getColor() {
        return mState.mUseColor;
    }

    /**
     * Sets the drawable's color value. This action will clobber the results of
     * prior calls to {@link #setAlpha(int)} on this object, which side-affected
     * the underlying color.
     *
     * @param color The color to draw.
     */
    public void setColor(MutiColorEntity[] color) {
        if (mState.mBaseColor != color || mState.mUseColor != color) {
            CLog.v("setColor");
            invalidateSelf();
            mState.mBaseColor = mState.mUseColor = color;
        }
    }

    /**
     * Returns the alpha value of this drawable's color.
     *
     * @return A value between 0 and 255.
     */
    @Override
    public int getAlpha() {
        if (mState.mUseColor.length > 0) {
            return mState.mUseColor[0].color >>> 24;
        } else {
            return 0;
        }
    }

    /**
     * Sets the color's alpha value.
     *
     * @param alpha The alpha value to set, between 0 and 255.
     */
    public void setAlpha(int alpha) {
        alpha += alpha >> 7; // make it 0..256

        int baseAlpha = mState.mBaseColor[0].color >>> 24;
        int useAlpha = baseAlpha * alpha >> 8;
        MutiColorEntity[] oldUseColor = mState.mUseColor;
        for (int i = 0; i < mState.mBaseColor.length; i++) {
            mState.mUseColor[i].color = (mState.mBaseColor[i].color << 8 >>> 8)
                    | (useAlpha << 24);
        }
        if (oldUseColor != mState.mUseColor) {
            invalidateSelf();
        }
    }

    /**
     * Setting a color filter on a ColorDrawable has no effect.
     *
     * @param colorFilter Ignore.
     */
    public void setColorFilter(ColorFilter colorFilter) {
    }

    public int getOpacity() {
        if (mState.mUseColor.length > 0) {
            switch (mState.mUseColor[0].color >>> 24) {
                case 255:
                    return PixelFormat.OPAQUE;
                case 0:
                    return PixelFormat.TRANSPARENT;
            }
        }
        return PixelFormat.TRANSLUCENT;
    }

    @Override
    public void inflate(Resources r, XmlPullParser parser, AttributeSet attrs)
            throws XmlPullParserException, IOException {
        super.inflate(r, parser, attrs);
        //
        // TypedArray a = r.obtainAttributes(attrs,
        // com.android.internal.R.styleable.ColorDrawable);
        //
        // int[] color = mState.mBaseColor;
        // color = a.getColor(
        // com.android.internal.R.styleable.ColorDrawable_color, color);
        // mState.mBaseColor = mState.mUseColor = color;
        //
        // a.recycle();
    }

    @Override
    public ConstantState getConstantState() {
        mState.mChangingConfigurations = getChangingConfigurations();
        return mState;
    }

    public static class MutiColorEntity {
        /**
         * %之几
         */
        public float percent;
        public int color;

        public MutiColorEntity(float percent, int color) {
            this.percent = percent;
            this.color = color;
        }

    }

    final static class ColorState extends ConstantState {
        MutiColorEntity[] mBaseColor; // base color, independent of setAlpha()
        @ViewDebug.ExportedProperty
        MutiColorEntity[] mUseColor; // basecolor modulated by setAlpha()
        int mChangingConfigurations;

        ColorState(ColorState state) {
            if (state != null) {
                mBaseColor = state.mBaseColor;
                mUseColor = state.mUseColor;
                mChangingConfigurations = state.mChangingConfigurations;
            }
        }

        @Override
        public Drawable newDrawable() {
            return new MutiColorDrawable(this);
        }

        @Override
        public Drawable newDrawable(Resources res) {
            return new MutiColorDrawable(this);
        }

        @Override
        public int getChangingConfigurations() {
            return mChangingConfigurations;
        }
    }
}
