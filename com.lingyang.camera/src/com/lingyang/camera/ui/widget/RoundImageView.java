package com.lingyang.camera.ui.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

public class RoundImageView extends ImageView {

    public RoundImageView(Context context) {
        super(context);

    }

    public RoundImageView(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    public RoundImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

    }


    protected void onDraw(Canvas canvas) {
        int roundPx = 5;
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.WHITE);
        Drawable drawable = getDrawable();
        if (drawable == null) {
            return;
        }
        if (getWidth() == 0 || getHeight() == 0) {
            return;
        }

        Bitmap b = ((BitmapDrawable) drawable).getBitmap();
        Bitmap bitmap = null;
        try {
            bitmap = b.copy(Config.RGB_565, false);
        } catch (Exception e) {
            return;
        }
        int w = getWidth();
        if (bitmap != null) {
            Bitmap roundBitmap = getCroppedBitmap(bitmap, w, roundPx);

            canvas.drawARGB(0, 0, 0, 0);
            canvas.drawCircle(w / 2, w / 2, w / 2 , paint);
            paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
            canvas.drawBitmap(roundBitmap, 0, 0, null);

        }
    }

    public static Bitmap getCroppedBitmap(Bitmap bmp, int length, int roundPx) {

        Bitmap sbmp;
        if (bmp.getWidth() != length || bmp.getHeight() != length)
            sbmp = Bitmap.createScaledBitmap(bmp, length, length, false);
        else
            sbmp = bmp;

        Bitmap output = Bitmap.createBitmap(sbmp.getWidth(), sbmp.getHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final Rect rect = new Rect(0, 0, sbmp.getWidth(), sbmp.getHeight());
        final RectF rectF = new RectF(0, 0, sbmp.getWidth(), sbmp.getHeight());


        final Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        paint.setDither(true);
        //设置边框颜色会导致图片变成正方形
//        canvas.drawARGB(255, 190, 222, 240);  
        canvas.drawARGB(0, 0, 0, 0);

//        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);  
        paint.setColor(Color.WHITE); //bedef0
        canvas.drawCircle(length / 2, length / 2, length / 2 , paint);

        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));

        canvas.drawBitmap(sbmp, rect, rect, paint);

        return output;
    }
} 
