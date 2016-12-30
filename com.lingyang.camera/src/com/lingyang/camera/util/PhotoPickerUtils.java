package com.lingyang.camera.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;

import com.lingyang.camera.dialog.DialogPhotoPicker;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PhotoPickerUtils {

	public static Bitmap compressImage(Bitmap image) {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
		int options = 100;
		while (baos.toByteArray().length / 1024 > 100) {
			baos.reset();
			options -= 10;
			image.compress(Bitmap.CompressFormat.JPEG, options, baos);

			if (options <= 10) {
				break;
			}
		}
		ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
		Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);
		return bitmap;
	}

	public static boolean deleteFile(String filePathAndName) {
		try {
			String filePath = filePathAndName;
			filePath = filePath.toString();
			File myDelFile = new File(filePath);
			if (myDelFile != null && myDelFile.exists()) {
				if (myDelFile.delete()) {
					return true;
				} else {
					return false;
				}
			} else {
				return false;
			}
		} catch (Exception e) {
			return false;
		}

	}

	public static String startPhotoCrop(Activity activity, Uri uri, int width,
			int height) {

		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(uri, "image/*");
		// crop为true是设置在开启的intent中设置显示的view可以剪裁
		intent.putExtra("crop", "true");

		// aspectX aspectY 是宽高的比例
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);

		// outputX,outputY 是剪裁图片的宽高
		// intent.putExtra("outputX", width);
		// intent.putExtra("outputY", height);
		intent.putExtra("return-data", false);
		intent.putExtra("noFaceDetection", true);
		String path = PhotoPickerUtils.getPhotoFileName(activity);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(path)));
		activity.startActivityForResult(intent, DialogPhotoPicker.PHOTO_CROP);

		return path;
	}

	public static String getPhotoFileName(Context mContext) {
		Date date = new Date(System.currentTimeMillis());
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MMdd_HHmmss");
		return new StringBuilder().append(getSaveCameraPhotoPath(mContext))
				.append(dateFormat.format(date) + ".jpg").toString();
	}

	public static void startGallery(Context context) {
		Intent pick = new Intent(Intent.ACTION_GET_CONTENT, null);
		pick.setType("image/*");
		pick.putExtra("scale", true);
		pick.putExtra("return-data", false);
		pick.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
		pick.putExtra("noFaceDetection", false);
		((Activity) context).startActivityForResult(pick,
				DialogPhotoPicker.PICTURE_DATA);
	}

	public static String startTakePhoto(Context context) {
		Intent take = new Intent();
		take.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
		String path = PhotoPickerUtils.getPhotoFileName(context);
		take.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(path)));

		((Activity) context).startActivityForResult(take,
				DialogPhotoPicker.CAMERA_DATA);
		return path;
	}

	public static String getSaveCameraPhotoPath(Context mContext) {
		String path = null;
		boolean sdCardExist = Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED);
		if (sdCardExist) {
			path = Environment.getExternalStorageDirectory().getAbsolutePath()
					+ "/lingyang/";
		} else {
			path = mContext.getCacheDir().getAbsolutePath() + "/lingyang/";
		}

		File dir = new File(path);
		if (!dir.exists())
			dir.mkdir();

		return path;
	}

	// 使用BitmapFactory.Options的inSampleSize参数来缩放
	public static Bitmap loadImageWithSize(String path, int width, int height) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, options);
		int outWidth = options.outWidth;
		int outHeight = options.outHeight;
		options.inDither = false;
		options.inPreferredConfig = Bitmap.Config.ARGB_8888;
		options.inSampleSize = 1;

		if (outHeight > height || outWidth > width) {
			final int heightRatio = Math
					.round((float) outWidth / (float) width);
			final int widthRatio = Math.round((float) outHeight
					/ (float) height);
			options.inSampleSize = heightRatio < widthRatio ? heightRatio
					: widthRatio;
		}
		// int degree = readPictureDegree(path);
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeFile(path, options);
	}

	// 使用BitmapFactory.Options的inSampleSize参数来缩放
	public static Bitmap loadImageWithSize(ContentResolver cr, Uri uri,
			int width, int height) {
		InputStream input;
		try {
			input = cr.openInputStream(uri);
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(input, null, options);
			int outWidth = options.outWidth;
			int outHeight = options.outHeight;
			options.inDither = false;
			options.inPreferredConfig = Bitmap.Config.ARGB_8888;
			options.inSampleSize = 1;

			if (outHeight > height || outWidth > width) {
				final int heightRatio = Math.round((float) outWidth
						/ (float) width);
				final int widthRatio = Math.round((float) outHeight
						/ (float) height);
				options.inSampleSize = heightRatio < widthRatio ? heightRatio
						: widthRatio;
			}

			input = cr.openInputStream(uri);
			options.inJustDecodeBounds = false;
			return BitmapFactory.decodeStream(input, null, options);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return null;

	}

	public static Bitmap resizeImage(Bitmap bitmap, int degree, int w, int h) {
		if (bitmap == null)
			return null;

		Bitmap BitmapOrg = bitmap;
		int width = BitmapOrg.getWidth();
		int height = BitmapOrg.getHeight();
		int newWidth = w;
		int newHeight = h;

		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeight) / height;

		Matrix matrix = new Matrix();
		if (degree > 0)
			matrix.postRotate(degree);
		matrix.postScale(scaleWidth, scaleHeight);
		Bitmap resizedBitmap = Bitmap.createBitmap(BitmapOrg, 0, 0, width,
				height, matrix, true);
		Log.e("resizeImage2", "resizeImage2: " + resizedBitmap.getWidth());
		BitmapOrg.recycle();
		return resizedBitmap;
	}

	public static int readPictureDegree(String path) {
		int degree = 0;
		try {
			ExifInterface exifInterface = new ExifInterface(path);
			int orientation = exifInterface.getAttributeInt(
					ExifInterface.TAG_ORIENTATION,
					ExifInterface.ORIENTATION_NORMAL);
			switch (orientation) {
			case ExifInterface.ORIENTATION_ROTATE_90:
				degree = 90;
				break;
			case ExifInterface.ORIENTATION_ROTATE_180:
				degree = 180;
				break;
			case ExifInterface.ORIENTATION_ROTATE_270:
				degree = 270;
				break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return degree;
	}

	/**
	 * android 4.4 的图片路径获取，返回来的uri有两种格式
	 * 1，content://media/external/images/media/233592 正常的
	 * 2，content://com.android.providers.media.documents/document/image%3A234709
	 * 需转换的
	 * 
	 * @param uri
	 *            需要转换的uri
	 * @param context
	 * @return
	 */

	@SuppressLint("NewApi")
	public static String getUri(Uri uri, Context context) {
		/**
		 * Build.VERSION_CODES 目前已知的SDK版本号的枚举类 Build.VERSION_CODES.KITKAT 4.4版
		 * Build.VERSION.SDK_INT sdk版本号
		 */
		boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

		if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
			if (isMediaDocument(uri)) {
				String docId = DocumentsContract.getDocumentId(uri);
				String[] split = docId.split(":");
				String type = split[0];

				Uri contentUri = null;
				if (type.equals("image")) {
					contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
				}
				String selection = "_id=?";
				String[] selectionArgs = new String[] { split[1] };

				return getDataColumn(context, contentUri, selection,
						selectionArgs);
			}
		}

		return null;
	}

	/**
	 * 文件路径得uri
	 * 
	 * @param context
	 * @param uriStr
	 *            文件路径
	 * @return
	 */
	public static Uri getProviderUri(Context context, String uriStr) {
		Uri mUri = Uri.parse("content://media/external/images/media");
		Uri mImageUri = null;

		Cursor cursor = context.getContentResolver().query(
				MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null,
				MediaStore.Images.Media.DEFAULT_SORT_ORDER);
		cursor.moveToFirst();

		while (!cursor.isAfterLast()) {
			String data = cursor.getString(cursor
					.getColumnIndex(MediaStore.MediaColumns.DATA));
			if (uriStr.equals(data)) {
				int ringtoneID = cursor.getInt(cursor
						.getColumnIndex(MediaStore.MediaColumns._ID));
				mImageUri = Uri.withAppendedPath(mUri, "" + ringtoneID);
				break;
			}
			cursor.moveToNext();
		}
		return mImageUri;
	}

	/**
	 * 获取文件路径
	 * 
	 * @param context
	 * @param uri
	 * @param selection
	 * @param selectionArgs
	 * @return
	 */
	public static String getDataColumn(Context context, Uri uri,
			String selection, String[] selectionArgs) {

		Cursor cursor = null;
		final String column = "_data";
		final String[] projection = { column };

		try {
			cursor = context.getContentResolver().query(uri, projection,
					selection, selectionArgs, null);
			if (cursor != null && cursor.moveToFirst()) {
				final int column_index = cursor.getColumnIndexOrThrow(column);
				return cursor.getString(column_index);
			}
		} finally {
			if (cursor != null)
				cursor.close();
		}
		return null;
	}

	/**
	 * @param uri
	 *            The Uri to check.
	 * @return Whether the Uri authority is MediaProvider.
	 */
	public static boolean isMediaDocument(Uri uri) {
		return "com.android.providers.media.documents".equals(uri
				.getAuthority());
	}


	public static Bitmap url2Bitmap(String url){

	URL fileUrl = null;
	Bitmap bitmap = null;

	try {
		fileUrl = new URL(url);
	} catch (MalformedURLException e) {
		e.printStackTrace();
	}

	try {
		HttpURLConnection conn = (HttpURLConnection) fileUrl
				.openConnection();
		conn.setDoInput(true);
		conn.connect();
		InputStream is = conn.getInputStream();
		bitmap = BitmapFactory.decodeStream(is);
		is.close();
	} catch (IOException e) {
		e.printStackTrace();
	}
	return bitmap;
	}

}
