package com.lingyang.camera.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;

import java.io.File;

/**
 * [APK文件管理]<BR>
 * 
 * @author 刘波
 */
public class ApkUtil {

	public static final String PACKAGE = "package:";

	/**
	 * [安装APK]<BR>
	 * 
	 * @param filepath
	 *            apk完整路径
	 * @return
	 */
	public static void installApk(Context context, String filepath) {
		Intent intent = new Intent();
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setAction(Intent.ACTION_VIEW);
		Uri uri = Uri.fromFile(new File(filepath));
		intent.setDataAndType(uri, "application/vnd.android.package-archive");
		try {
			context.startActivity(intent);
		} catch (Exception e) {
			// TODO: handle exception
		}

	}

	/**
	 * [打開apk]<BR>
	 * 
	 * @param context
	 * @param pkName
	 *            此处要打开的程序的包名
	 */
	public static void openApk(Context context, String pkName) {
		Intent intent = new Intent();
		intent = context.getPackageManager().getLaunchIntentForPackage(pkName);
		try {
			context.startActivity(intent);
		} catch (Exception e) {
			// TODO: handle exception
		}

	}

	/**
	 * [卸载apk]<BR>
	 * 
	 * @param context
	 * @param pkName
	 *            此处写要卸载的程序的包名
	 */
	public static void uninstallApk(Context context, String pkName) {
		Intent intent = new Intent(Intent.ACTION_DELETE, Uri.parse(PACKAGE + pkName));
		try {
			context.startActivity(intent);
		} catch (Exception e) {
			// TODO: handle exception
		}

	}

	/**
	 * [获取已安装apk的信息]<BR>
	 * 
	 * @param context
	 * @param pak
	 * @return
	 */
	public static PackageInfo getInstallAPKInfo(Context context, String pak) {

		PackageManager pm = context.getPackageManager();
		PackageInfo pakinfo = null;
		try {
			pakinfo = pm.getPackageInfo(pak, PackageManager.GET_ACTIVITIES);

		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return pakinfo;
	}

	/**
	 * [获取未安装apk的版本、图标等信息]<BR>
	 * 
	 * @param context
	 * @param archiveFilePath
	 * @return
	 */
	public static String getUninstallAPKInfo(Context context, String archiveFilePath) {
		// archiveFilePath=Environment.getExternalStorageDirectory()+"/"+"TestB.apk"
		String versionName = null;
		/*
		 * String appName = null; String pakName = null; PackageManager pm =
		 * context.getPackageManager(); PackageInfo pakinfo =
		 * pm.getPackageArchiveInfo(archiveFilePath,
		 * PackageManager.GET_ACTIVITIES); if (pakinfo != null) {
		 * ApplicationInfo appinfo = pakinfo.applicationInfo; versionName =
		 * pakinfo.versionName; Drawable icon = pm.getApplicationIcon(appinfo);
		 * appName = (String) pm.getApplicationLabel(appinfo); pakName =
		 * appinfo.packageName;
		 * 
		 * }
		 */
		return versionName;
	}

	/**
	 * [检查应用程序是否安装]<BR>
	 * 
	 * @param context
	 * @param packageName
	 * @return
	 */
	public static boolean checkApkExist(Context context, String packageName) {
		if (packageName == null || "".equals(packageName))
			return false;
		try {
			ApplicationInfo info = context.getPackageManager().getApplicationInfo(packageName,
					PackageManager.GET_META_DATA);
			return true;
		} catch (NameNotFoundException e) {
			return false;
		}

	}

}
