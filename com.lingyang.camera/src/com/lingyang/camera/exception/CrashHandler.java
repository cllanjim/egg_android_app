package com.lingyang.camera.exception;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Environment;

import com.lingyang.base.utils.CLog;
import com.lingyang.camera.CameraApplication;
import com.lingyang.camera.config.Const;
import com.lingyang.camera.util.FileUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * UncaughtException处理�?,当程序发生Uncaught异常的时�?,有该类来接管程序,并记录发送错误报�?.
 * 
 * @author user
 * 
 */
public class CrashHandler implements UncaughtExceptionHandler {

	// 系统默认的UncaughtException处理�?
	private UncaughtExceptionHandler mDefaultHandler;
	// CrashHandler实例
	private static CrashHandler INSTANCE = new CrashHandler();
	// 程序的Context对象
	private Context mContext;
	// 程序的Context对象
	private CameraApplication mCamApplication;
	// 用来存储设备信息和异常信�?
	private Map<String, String> infos = new HashMap<String, String>();

	/** 保证只有�?个CrashHandler实例 */
	private CrashHandler() {
	}

	/** 获取CrashHandler实例 ,单例模式 */
	public static CrashHandler getInstance() {
		return INSTANCE;
	}

	/**
	 * 初始�?
	 * 
	 * @param context
	 */
	public void init(Context context) {
		mContext = context;
		mCamApplication = (CameraApplication) context.getApplicationContext();
		// 获取系统默认的UncaughtException处理�?
		mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
		// 设置该CrashHandler为程序的默认处理�?
		Thread.setDefaultUncaughtExceptionHandler(this);
	}

	/**
	 * 当UncaughtException发生时会转入该函数来处理
	 */
	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		ex.printStackTrace();
		if (!handleException(ex) && mDefaultHandler != null) {
			// 如果用户没有处理则让系统默认的异常处理器来处�?
			mDefaultHandler.uncaughtException(thread, ex);
		} else {
			// �?出程�?
			CLog.v("killProcess");
			mCamApplication.setApplicationExit(true);
			mCamApplication.AppExit();
			android.os.Process.killProcess(android.os.Process.myPid());
		}
	}

	/**
	 * 自定义错误处�?,收集错误信息 发�?�错误报告等操作均在此完�?.
	 * 
	 * @param ex
	 * @return true:如果处理了该异常信息;否则返回false.
	 */
	private boolean handleException(Throwable ex) {
		if (ex == null) {
			return false;
		}
		// 保存日志文件
		saveCrashInfo2File(ex);
		if (Const.DEBUG) {
			saveHeapDump2File();
		}
		return true;
	}

	/**
	 * 收集设备参数信息
	 * 
	 * @param ctx
	 */
	public StringBuffer collectDeviceInfo(Context ctx, Throwable ex) {
		try {
			PackageManager pm = ctx.getPackageManager();
			PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(), PackageManager.GET_ACTIVITIES);
			if (pi != null) {
				String versionName = pi.versionName == null ? "null" : pi.versionName;
				String versionCode = pi.versionCode + "";
				infos.put("versionName", versionName);
				infos.put("versionCode", versionCode);
			}
		} catch (NameNotFoundException e) {
			CLog.e("an error occured when collect package info:" + e);
		}
		Field[] fields = Build.class.getDeclaredFields();
		for (Field field : fields) {
			try {
				field.setAccessible(true);
				infos.put(field.getName(), field.get(null).toString());
				CLog.d(field.getName() + " : " + field.get(null));
			} catch (Exception e) {
				CLog.e("an error occured when collect crash info:" + e);
			}
		}

		StringBuffer sb = new StringBuffer();
		for (Map.Entry<String, String> entry : infos.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();
			sb.append(key + "=" + value + "\n");
		}

		Writer writer = new StringWriter();
		PrintWriter printWriter = new PrintWriter(writer);
		ex.printStackTrace(printWriter);
		Throwable cause = ex.getCause();
		while (cause != null) {
			cause.printStackTrace(printWriter);
			cause = cause.getCause();
		}
		printWriter.close();
		String result = writer.toString();
		sb.append(result);
		return sb;
	}

	/**
	 * 保存错误信息到文件中
	 * 
	 * @param ex
	 * @return 返回文件名称,便于将文件传送到服务�?
	 */
	@SuppressLint("SdCardPath")
	private String saveCrashInfo2File(Throwable ex) {

		try {
			if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
				File dir = new File(FileUtil.getInstance().getLogFile(), "crash");
				if (!dir.exists()) {
					dir.mkdirs();
				}
				File file = new File(dir, getFilename());
				if (!file.exists()) {
					FileOutputStream fos = new FileOutputStream(file);
					fos.write(collectDeviceInfo(mContext, ex).toString().getBytes());
					fos.close();
				}
				return file.getAbsolutePath();
			}
		} catch (Exception e) {
			CLog.e("an error occured while writing file :" + e);
		}
		return null;
	}

	private void saveHeapDump2File() {
		try {
			if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
				// String path = "/sdcard/360jia/.crash/";
				File dir = new File(FileUtil.getInstance().getLogFile(), "heapDump");
				if (!dir.exists()) {
					dir.mkdirs();
				}
				File file = new File(dir, System.currentTimeMillis() + "dump.hprof");
				if (!file.exists()) {
					android.os.Debug.dumpHprofData(file.getAbsolutePath());
				}
			}
		} catch (Exception e) {
			CLog.e("an error occured while writing file :" + e);
		}
	}

	private String getFilename() {
		long times = System.currentTimeMillis();
		return "" + times / 1500 * 1500 + "error.txt";
	}
}
