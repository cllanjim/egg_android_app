package com.lingyang.base.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.net.Uri;
import android.os.SystemClock;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.nostra13.universalimageloader.utils.L;

public class BitmapUtils {
	private static final String TAG = "BitmapUtils";

	/**
	 * 创建带有影子的图片
	 * 
	 * @param originalImage
	 *            原图片
	 * @param scale
	 *            缩放比例
	 * @return
	 */
	public static Bitmap createReflectedImage(Bitmap originalImage, float reflectRatio, float scale) {

		int width = (int) (originalImage.getWidth() * scale);
		int height = (int) (originalImage.getHeight() * scale);

		final Rect srcRect = new Rect(0, 0, originalImage.getWidth(), originalImage.getHeight());
		final Rect dstRect = new Rect(0, 0, width, height);

		final int reflectionGap = 1;

		Bitmap bitmapWithReflection = Bitmap.createBitmap(width, (int) (height + height * reflectRatio), Config.ARGB_8888);
		Canvas canvasRef = new Canvas(bitmapWithReflection);

		canvasRef.drawBitmap(originalImage, srcRect, dstRect, null);

		Matrix matrix = new Matrix();
		matrix.setTranslate(0, height + height + reflectionGap);
		matrix.preScale(scale, -scale);

		canvasRef.drawBitmap(originalImage, matrix, null);

		Paint paint = new Paint();
		LinearGradient shader = new LinearGradient(0, height, 0, bitmapWithReflection.getHeight() + reflectionGap, 0x80ffffff, 0x00ffffff,
				TileMode.CLAMP);
		paint.setShader(shader);
		paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
		canvasRef.drawRect(0, height, width, bitmapWithReflection.getHeight() + reflectionGap, paint);

		originalImage.recycle();
		return bitmapWithReflection;
	}

	/**
	 * 得到圆角图片
	 * 
	 * @param bitmap
	 *            原图像
	 * @param scale
	 *            缩放比例
	 * @param roundPx
	 *            圆角像素
	 * @return
	 */
	public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, float scale, float roundPx, Bitmap.Config config) {

		int width = (int) (bitmap.getWidth() * scale);
		int height = (int) (bitmap.getHeight() * scale);

		Bitmap output = Bitmap.createBitmap(width, height, config);
		Canvas canvas = new Canvas(output);

		final int color = 0xff000000;
		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
		final RectF rectF = new RectF(0, 0, width, height);

		if (roundPx > 0) {
			paint.setAntiAlias(true);
			canvas.drawARGB(0, 0, 0, 0);
			paint.setColor(color);
			canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

			paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		}

		// draw的方式缩放
		canvas.drawBitmap(bitmap, rect, rectF, paint);
		return output;
	}

	public static Bitmap getCircleBitmap(Bitmap bitmap, float scale, float radius, Bitmap.Config config) {
		int width = (int) (bitmap.getWidth() * scale);
		int height = (int) (bitmap.getHeight() * scale);
		radius = radius * scale;
		return getCircleBitmap(bitmap, width, height, radius, config);
	}

	public static Bitmap getCircleBitmap(Bitmap bitmap, int width, int height, float radius, Bitmap.Config config) {

		Bitmap output = Bitmap.createBitmap(width, height, config);
		Canvas canvas = new Canvas(output);

		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
		final RectF rectF = new RectF(0, 0, width, height);

		paint.setAntiAlias(true);
		canvas.drawCircle(width / 2, height / 2, radius, paint);

		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));

		canvas.drawBitmap(bitmap, rect, rectF, paint);
		return output;
	}

	public static Bitmap getScaleBitmap(Bitmap bitmap, float scale) {
		return getScaleBitmap(bitmap, scale, Config.ARGB_8888);
	}
	
	/**
	 * 得到缩放后的图片
	 * 
	 * @param bitmap
	 * @param scale
	 * @return
	 */
	public static Bitmap getScaleBitmap(Bitmap bitmap, float scale, Bitmap.Config config) {
		if (bitmap == null) {
			return null;
		}
		int width = (int) (bitmap.getWidth() * scale);
		int height = (int) (bitmap.getHeight() * scale);

		Bitmap output = Bitmap.createBitmap(width, height, config);
		Canvas canvas = new Canvas(output);

		final Paint paint = new Paint();
		paint.setAntiAlias(true);
		final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
		final RectF rectF = new RectF(0, 0, width, height);
		
		canvas.drawBitmap(bitmap, rect, rectF, paint);
		return output;
	}
//	public static Bitmap getScaleBitmap(Bitmap bitmap, float scale) {
//		if (bitmap == null) {
//			return null;
//		}
//		Matrix matrix = new Matrix();
//		matrix.postScale(scale, scale);
//		Bitmap dstbmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
//		return dstbmp;
//	}

	/**
	 * 
	 * @param inStream
	 * @throws IOException
	 * @return
	 */
	public static byte[] readStream(InputStream inStream) throws IOException {

		long t = SystemClock.elapsedRealtime();

		ByteArrayOutputStream outstream = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024]; // 用数据装
		int len = -1;
		while ((len = inStream.read(buffer)) != -1) {
			outstream.write(buffer, 0, len);
		}
		outstream.close();

		return outstream.toByteArray();
	}

	/**
	 * 得到手机data目录下的图片
	 * 
	 * @param context
	 * @param fileName
	 * @return
	 */
	public static Bitmap getBmpFromFile(Context context, String fileName) {
		try {
			BitmapFactory.Options opts = new BitmapFactory.Options();
			opts.inPreferredConfig = Config.RGB_565;
			opts.inJustDecodeBounds = false;
			FileInputStream imgInputStream = context.openFileInput(fileName);
			Bitmap bmp = BitmapFactory.decodeStream(imgInputStream, null, opts);
			// Bitmap bmp = BitmapFactory.decodeStream(imgInputStream);
			return bmp;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (OutOfMemoryError e) {

		}
		return null;
	}
	
	public static Bitmap getBmpFromFile(String filePath, int bmpWidth, Bitmap.Config config) throws Exception {
		return getBmpFromFile(filePath, bmpWidth, config, false);
	}
	
	public static Bitmap getBmpFromFile(String filePath, int bmpWidth, Bitmap.Config config, boolean isStrengthen) throws Exception { 
		return getBmpFromFile(filePath, bmpWidth, config, isStrengthen, true);
	}

	/**
	 * TODO
	 * 
	 * @param filePath
	 * @param bmpWidth
	 *            -1时候不特殊处理图片
	 * @param config
	 * @return
	 * @throws
	 */
	public static Bitmap getBmpFromFile(String filePath, int bmpWidth, Bitmap.Config config,
				boolean isStrengthen, boolean isCorrectWidth) throws Exception {

		if (filePath == null) {
			return null;
		}
		File file = new File(filePath);
		if (! file.exists()) {
			return null;
		}
		BitmapFactory.Options opts = new BitmapFactory.Options();
		Bitmap bmp = null;
		if (bmpWidth != -1) {
			opts.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(filePath, opts);
			int scaleWidth = opts.outWidth;
			int scaleHeight = opts.outHeight;
//			Log.i("load", "--trueWidth:" + scaleWidth + "| trueHeight:" + opts.outHeight + "|bmpWidth:" + bmpWidth);

			if (isStrengthen) {
				if (scaleWidth > scaleHeight) {
					int sampleSize = 1;
					if (scaleWidth > bmpWidth) {
						int scale = scaleWidth / bmpWidth;
						if (scale > 1) {
							sampleSize = scale;
						}
					}
					opts.inSampleSize = sampleSize;
				} else {
					int sampleSize = 1;
					if (scaleHeight > bmpWidth) {
						int scale = scaleHeight / bmpWidth;
						if (scale > 1) {
							sampleSize = scale;
						}
					}
					opts.inSampleSize = sampleSize;
				}
			} else {
				int sampleSize = 1;
				if (scaleWidth > bmpWidth) {
					int scale = scaleWidth / bmpWidth;
					if (scale > 1) {
						sampleSize = scale;
					}
				}
				opts.inSampleSize = sampleSize;
			}

			//Log.d("zz", "SampleSize的大小：" + opts.inSampleSize);
			opts.inPreferredConfig = config;
			opts.inJustDecodeBounds = false;
			bmp = BitmapFactory.decodeFile(filePath, opts);

			if (!isCorrectWidth) {
				return bmp;
			}
			
			if (bmp == null) {
				Log.v("debug", "getBmpFromFile(scaleSize)is bykk");
				return null;
			}
			//Log.v("zz", "fileScale: " + opts.inSampleSize + " -- opts.outWidth:" + opts.outWidth + "|outHeight:"
			//		+ opts.outHeight + "|bmpWidth:" + bmp.getWidth() + "|bmpHeight:" + bmp.getHeight());

			 int width = bmp.getWidth();
			 if (width > bmpWidth) {
				 
				 float scaleBmp = (float) bmpWidth / width;
				 Bitmap scaleBitmap = getScaleBitmap(bmp, scaleBmp, Config.ARGB_8888);
				 //Log.v("debug", "scaleBmp:" + scaleBmp);
				
				 bmp.recycle();
				 
				Log.v("debug", "getBmpFromFile(scaleSize 2)is " + bmp);
				 return scaleBitmap;
			 }
		} else {
			opts.inPreferredConfig = config;
			opts.inJustDecodeBounds = false;
			bmp = BitmapFactory.decodeFile(filePath, opts);
			
			Log.v("debug", "getBmpFromFile(org)is " + bmp);
		} 
		return bmp;
	}
	
	public static Bitmap getBmpFromUri(Context context, Uri uri, int bmpSize, Bitmap.Config config,
			boolean isStrengthen, boolean isCorrectWidth, boolean isCenterDrop) throws Exception {
		
		if (uri == null) {
			return null;
		}
		/**
		 * 从ContentResolver获取的InputStream为AutoCloseInputStream
		 * 使用一次后悔自动关闭，所以需要重复获取InputStream
		 */

		BitmapFactory.Options opts = new BitmapFactory.Options();
		Bitmap bmp = null;
		if (bmpSize != -1) {
			opts.inJustDecodeBounds = true;
	        InputStream inputstream = context.getContentResolver().openInputStream(uri);
			bmp = BitmapFactory.decodeStream(inputstream, null, opts);
			inputstream.close();
			int scaleWidth = opts.outWidth;
			int scaleHeight = opts.outHeight;
//			Log.i("load", "--trueWidth:" + scaleWidth + "| trueHeight:" + opts.outHeight + "|bmpSize:" + bmpSize);
			
			if (isStrengthen) {
				if (scaleWidth > scaleHeight) {
					int sampleSize = 1;
					if (scaleWidth > bmpSize) {
						int scale = scaleWidth / bmpSize;
						if (scale > 1) {
							sampleSize = scale;
						}
					}
					opts.inSampleSize = sampleSize;
				} else {
					int sampleSize = 1;
					if (scaleHeight > bmpSize) {
						int scale = scaleHeight / bmpSize;
						if (scale > 1) {
							sampleSize = scale;
						}
					}
					opts.inSampleSize = sampleSize;
				}
			} else {
				int sampleSize = 1;
				if (scaleWidth > bmpSize) {
					int scale = scaleWidth / bmpSize;
					if (scale > 1) {
						sampleSize = scale;
					}
				}
				opts.inSampleSize = sampleSize;
			}
			
			//Log.d("zz", "SampleSize的大小：" + opts.inSampleSize);
			opts.inPreferredConfig = config;
			opts.inJustDecodeBounds = false;
			Rect pad = new Rect();
			Resources res = context.getResources();
			opts.inScreenDensity = res.getDisplayMetrics().densityDpi;
			inputstream = context.getContentResolver().openInputStream(uri);
			bmp = BitmapFactory.decodeResourceStream(res, null, inputstream, pad, opts);
			inputstream.close();
			
			if (!isCorrectWidth) {
				return bmp;
			}
			
			if (bmp == null) {
				Log.v("debug", "getBmpFromFile(scaleSize)is bykk");
				return null;
			}
			//Log.v("zz", "fileScale: " + opts.inSampleSize + " -- opts.outWidth:" + opts.outWidth + "|outHeight:"
			//		+ opts.outHeight + "|bmpSize:" + bmp.getWidth() + "|bmpHeight:" + bmp.getHeight());
			
			if (isCenterDrop) {
				Bitmap scaleBitmap = getCenterDropBitmap(bmp, bmpSize, bmpSize);
				bmp.recycle();
				return scaleBitmap;
			} else {
				float scaleBmp = 1;
				if (bmp.getWidth() > bmpSize) {
					scaleBmp = (float) bmpSize / bmp.getWidth();
				} else if (bmp.getHeight() > bmpSize) {
					scaleBmp = (float) bmpSize / bmp.getHeight();
				}
				Log.v("debug", "getBmpFromFile(scaleSize 2)is " + scaleBmp);
				if (scaleBmp != 1) {
					Bitmap scaleBitmap = getScaleBitmap(bmp, scaleBmp, Config.ARGB_8888);
					bmp.recycle();
					return scaleBitmap;
				} else {
					return bmp;
				}
			}
		} else {
			opts.inPreferredConfig = config;
			opts.inJustDecodeBounds = false;
			Rect pad = new Rect();
	        InputStream inputstream = context.getContentResolver().openInputStream(uri);
			bmp = BitmapFactory.decodeStream(inputstream, pad, opts);
			inputstream.close();

			Log.v("debug", "getBmpFromFile(org)is " + bmp);
		}
		return bmp;
	}

	public static Bitmap getBitmap(String filePath) {
		return getBitmap(filePath, null);
	}
	
	public static Bitmap getBitmap(String filePath, Bitmap.Config config) {
		if (filePath != null) {
			File file = new File(filePath);
			if (file.exists()) {
				if (config == null) {
					return BitmapFactory.decodeFile(filePath);
				} else {
					BitmapFactory.Options opts = new BitmapFactory.Options();
					opts.inPreferredConfig = config;
					return BitmapFactory.decodeFile(filePath, opts);
				}
			}
		}
		return null;
	}

	/**
	 * 得到缩小的图片，这里缩小的是图片质量
	 * 
	 * @param dataBytes
	 * @param maxWidth
	 * @return
	 */
	public static Bitmap getCorrectBmp(byte dataBytes[], int bmpWidth, Bitmap.Config config) {

		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inPreferredConfig = Config.ALPHA_8;
		opts.inJustDecodeBounds = true;
		Bitmap bmp = BitmapFactory.decodeByteArray(dataBytes, 0, dataBytes.length, opts);
		//Log.v("debug", "opts.outWidth:" + opts.outWidth);
		int scaleWidth = opts.outWidth;
		if (opts.outWidth < opts.outHeight) {
			scaleWidth = opts.outHeight;
		}

		int scale = scaleWidth / bmpWidth;
		if (scale > 1) {
			opts.inSampleSize = scale;
		} else if (scale <= 0 && opts.inSampleSize <= 0) {
			opts.inSampleSize = 1;
		}
		opts.inPreferredConfig = config;
		opts.inJustDecodeBounds = false;
		try {
			bmp = BitmapFactory.decodeByteArray(dataBytes, 0, dataBytes.length, opts);
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (bmp != null) {
			if (bmp.getWidth() < bmpWidth) {

				float scaleBmp = (float) bmpWidth / bmp.getWidth();
				//Log.v("debug", "scaleBmp:" + scaleBmp);
				Bitmap scaleBitmap = getScaleBitmap(bmp, scaleBmp, Config.ARGB_8888);
				bmp.recycle();
				return scaleBitmap;
			}
		}

		return bmp;
	}
	
	public static int[] getBitmapResolution(String filePath) {
		try {
			BitmapFactory.Options opts = new BitmapFactory.Options();
			opts.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(filePath, opts);
			return new int[]{opts.outWidth, opts.outHeight};
		}catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 保存图片到指定位置
	 * 
	 * @param context
	 * @param bmp
	 * @param fileName
	 * @return
	 */
	public static boolean saveBmpToJpg(Bitmap bmp, String filePath, int quality) {
		if (filePath == null || "".equals(filePath)) {
			return false;
		}

		int index = filePath.lastIndexOf("/");
		if (index == -1) {
			return false;
		}

		String prePath = filePath.substring(0, index);

		try {
			File dir = new File(prePath);
			if (!dir.exists()) {
				dir.mkdirs();
			}

			File file = new File(filePath);
			if (file.exists()) {
				file.delete();
			}
			file.createNewFile();

			FileOutputStream fileOut = new FileOutputStream(filePath, true);
			bmp.compress(Bitmap.CompressFormat.JPEG, quality, fileOut);
			fileOut.flush();
			fileOut.close();

			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}
	
	/**
	 * 保存图片到指定位置
	 * 
	 * @param context
	 * @param bmp
	 * @param fileName
	 * @return
	 */
	public static boolean saveBmpToPng(Bitmap bmp, String filePath, int quality) {
		if (filePath == null || "".equals(filePath)) {
			return false;
		}

		int index = filePath.lastIndexOf("/");
		if (index == -1) {
			return false;
		}

		String prePath = filePath.substring(0, index);

		try {
			File dir = new File(prePath);
			if (!dir.exists()) {
				dir.mkdirs();
			}

			File file = new File(filePath);
			if (file.exists()) {
				file.delete();
			}
			file.createNewFile();

			FileOutputStream fileOut = new FileOutputStream(filePath, true);
			bmp.compress(Bitmap.CompressFormat.PNG, quality, fileOut);
			fileOut.flush();
			fileOut.close();

			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	public static Bitmap StringToBitmap(String data) {
		if (data != null && data.length() > 0) {
			byte[] buffer = data.getBytes();
			return BitmapFactory.decodeByteArray(buffer, 0, buffer.length);
		}
		return null;
	}
	
	public static Bitmap getCenterDropBitmap(Bitmap bitmap, int matrixWidth, int matrixHeight) {
		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(output);
		Matrix matrix = getCenterDropMatrix(bitmap.getWidth(), bitmap.getHeight(), matrixWidth, matrixHeight);
		canvas.drawBitmap(bitmap, matrix, null);
		return output;
	}

	public static Matrix getCenterDropMatrix(int bitmapWidth, int bitmapHeight, 
			int matrixWidth, int matrixHeight) {
	    Matrix matrix = new Matrix();
        float scale;
        float dx = 0, dy = 0;
        if (bitmapWidth * matrixHeight > matrixWidth * bitmapHeight) {
            scale = (float) matrixHeight / (float) bitmapHeight; 
            dx = (matrixWidth - bitmapWidth * scale) * 0.5f;
        } else {
            scale = (float) matrixWidth / (float) bitmapWidth;
            dy = (matrixHeight - bitmapHeight * scale) * 0.5f;
        }
        matrix.setScale(scale, scale);
        matrix.postTranslate((int) (dx + 0.5f), (int) (dy + 0.5f));
        
        return matrix;
	}
	
	public static void printBitmapMemory(Bitmap bitmap, String tag) {
		if (true) {
			//暂时不打印
			return;
		}
		long memoryBytes = bitmap.getRowBytes() * bitmap.getHeight();
		String m1 = FileUtil.parseFileSize(memoryBytes);
		Log.d(tag, "memoryBytes:" + m1);
	}
	
	public static Object[] getScaleParams(ScaleType scaleType, Bitmap bitmap, int viewWidth, int viewHeight) {
		int bw = bitmap.getWidth();
		int bh = bitmap.getHeight();
		int vw = viewWidth;
		int vh = viewHeight;
		if (vw <= 0) vw = bw;
		if (vh <= 0) vh = bh;

		int width, height;
		Rect srcRect;
		Rect destRect;
		switch (scaleType) {
			case CENTER_INSIDE:
				float vRation = (float) vw / vh;
				float bRation = (float) bw / bh;
				int destWidth;
				int destHeight;
				if (vRation > bRation) {
					destHeight = Math.min(vh, bh);
					destWidth = (int) (bw / ((float) bh / destHeight));
				} else {
					destWidth = Math.min(vw, bw);
					destHeight = (int) (bh / ((float) bw / destWidth));
				}
				int x = (vw - destWidth) / 2;
				int y = (vh - destHeight) / 2;
				srcRect = new Rect(0, 0, bw, bh);
				destRect = new Rect(x, y, x + destWidth, y + destHeight);
				width = vw;
				height = vh;
				break;
			case FIT_CENTER:
			case FIT_START:
			case FIT_END:
			default:
				vRation = (float) vw / vh;
				bRation = (float) bw / bh;
				if (vRation > bRation) {
					width = (int) (bw / ((float) bh / vh));
					height = vh;
				} else {
					width = vw;
					height = (int) (bh / ((float) bw / vw));
				}
				srcRect = new Rect(0, 0, bw, bh);
				destRect = new Rect(0, 0, width, height);
				break;
			case CENTER_CROP:
				vRation = (float) vw / vh;
				bRation = (float) bw / bh;
				int srcWidth;
				int srcHeight;
				if (vRation > bRation) {
					srcWidth = bw;
					srcHeight = (int) (vh * ((float) bw / vw));
					x = 0;
					y = (bh - srcHeight) / 2;
				} else {
					srcWidth = (int) (vw * ((float) bh / vh));
					srcHeight = bh;
					x = (bw - srcWidth) / 2;
					y = 0;
				}
				width = srcWidth;// Math.min(vw, bw);
				height = srcHeight;//Math.min(vh, bh);
				srcRect = new Rect(x, y, x + srcWidth, y + srcHeight);
				destRect = new Rect(0, 0, width, height);
				break;
			case FIT_XY:
				width = vw;
				height = vh;
				srcRect = new Rect(0, 0, bw, bh);
				destRect = new Rect(0, 0, width, height);
				break;
			case CENTER:
			case MATRIX:
				width = Math.min(vw, bw);
				height = Math.min(vh, bh);
				x = (bw - width) / 2;
				y = (bh - height) / 2;
				srcRect = new Rect(x, y, x + width, y + height);
				destRect = new Rect(0, 0, width, height);
				break;
		}
		return new Object[] {srcRect, destRect, width, height};
	}
	
	public static Bitmap roundCorners(Bitmap bitmap, ImageView imageView, ScaleType scaleType, float roundPrecent) {
		Bitmap roundBitmap;

		Object paramsObj[] = getScaleParams(scaleType, bitmap, imageView.getWidth(), imageView.getHeight());
		Rect srcRect = (Rect)paramsObj[0];
		Rect destRect = (Rect)paramsObj[1];
		int width = Integer.parseInt(paramsObj[2].toString());
		int height = Integer.parseInt(paramsObj[3].toString());
		int roundPixels = (int)(width * roundPrecent);
		try {
			roundBitmap = getRoundedCornerBitmap(bitmap, roundPixels, srcRect, destRect, width, height);
		} catch (OutOfMemoryError e) {
			L.e(e, "Can't create bitmap with rounded corners. Not enough memory.");
			roundBitmap = bitmap;
		}

		return roundBitmap;
	}

	private static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int roundPixels, Rect srcRect, Rect destRect, int width, int height) {
		Bitmap output = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		Canvas canvas = new Canvas(output);

		final Paint paint = new Paint();
		final RectF destRectF = new RectF(destRect);

		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(0xFF000000);
		canvas.drawRoundRect(destRectF, roundPixels, roundPixels, paint);

		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
		canvas.drawBitmap(bitmap, srcRect, destRectF, paint);

		return output;
	}
	
	public static Bitmap createMegerBitmap(Context context, Bitmap bitmap1,
			Bitmap bitmap2, ScaleType scaleType, int width, int height, 
			int singleWidth, int singleHeight, float roundPxPrecent, int topMargin, 
			int leftMargin, Bitmap.Config config) {
		if (bitmap1 == null || bitmap2 == null) {
			return null;
		}
		
		Object[] params = getScaleParams(scaleType, bitmap1, singleWidth, singleHeight);
		Rect srcRect = (Rect)params[0];
		Rect destRect = (Rect)params[1];
		int rWidth = Integer.parseInt(params[2].toString());
		int rHeight = Integer.parseInt(params[3].toString());
		Bitmap roudBitmap = getRoundedCornerBitmap(bitmap1, (int)(rWidth*roundPxPrecent), 
				srcRect, destRect, rWidth, rHeight);
		bitmap1.recycle();
		if (roudBitmap == null) {
			bitmap2.recycle();
			return null;
		}
		
		Object[] params2 = getScaleParams(scaleType, bitmap2, singleWidth, singleHeight);
		Rect srcRect2 = (Rect)params2[0];
		Rect destRect2 = (Rect)params2[1];
		int rWidth2 = Integer.parseInt(params2[2].toString());
		int rHeight2 = Integer.parseInt(params2[3].toString());
		Bitmap roudBitmap2 = getRoundedCornerBitmap(bitmap2, (int)(rWidth2*roundPxPrecent), 
				srcRect2, destRect2, rWidth2, rHeight2);
		bitmap2.recycle();
		if (roudBitmap2 == null) {
			roudBitmap.recycle();
			return null;
		}
		
		Bitmap output = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		Canvas canvas = new Canvas(output);
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		
		int offset = (int)(2 * context.getResources().getDisplayMetrics().density);
		Rect mergerSrcRect = new Rect(0, 0, roudBitmap.getWidth(), roudBitmap.getHeight());
		Rect mergerDestRect = new Rect(leftMargin + offset, offset,
				leftMargin + singleWidth + offset, 0 + singleHeight + offset);
		canvas.drawBitmap(roudBitmap, mergerSrcRect, mergerDestRect, paint);
		
		Rect mergerSrcRect2 = new Rect(0, 0, roudBitmap2.getWidth(), roudBitmap2.getHeight());
		Rect mergerDestRect2 = new Rect(offset, topMargin - offset,
				0 + singleWidth + offset, topMargin + singleHeight - offset);
		canvas.drawBitmap(roudBitmap2, mergerSrcRect2, mergerDestRect2, paint);
		
		roudBitmap.recycle();
		roudBitmap2.recycle();
		
		return output;
	}
	
	@SuppressLint("NewApi")
	public static boolean isBitmapFile(String filePath){
		if(filePath == null){
			return false;
		}
		
		Options opt = new Options();
		opt.inPreferQualityOverSpeed = false;
		opt.inPreferredConfig = Bitmap.Config.RGB_565;   
	    opt.inPurgeable = true;  
	    opt.inInputShareable = true;  
		opt.inSampleSize = 2048;
		Log.d(TAG, "isBitmapFile begain filePath:  " + filePath);
		Bitmap bm = BitmapFactory.decodeFile(filePath, opt);
		boolean result = bm != null;
		Log.d(TAG, "isBitmapFile finish bm:  " + bm + " result: " + result);
		if(bm != null){
			bm.recycle();
			bm = null;
		}
		return result;
	}
	
	/**
	 * [保存本地图片的缩略图]<br/>
	 * 
	 * @param bitmap
	 * @param filePath
	 * @param videoItemWidth
	 * @return
	 */
	public static boolean saveThumb(Bitmap bitmap, String filePath, int videoItemWidth) {
		return saveThumb(bitmap, filePath, videoItemWidth, 80);
	}

	public static boolean saveThumb(Bitmap bitmap, String filePath, int videoItemWidth, int qualit) {
		float scale = videoItemWidth / (float) bitmap.getWidth();
		try {
			Bitmap smallBitmap = BitmapUtils.getScaleBitmap(bitmap, scale, Config.ARGB_8888);
			BitmapUtils.printBitmapMemory(smallBitmap, "【截图】裁剪图片");
			bitmap.recycle();
			if (smallBitmap != null) {
				BitmapUtils.saveBmpToJpg(smallBitmap, filePath, qualit);
				smallBitmap.recycle();
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
}
