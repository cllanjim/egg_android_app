package com.lingyang.base.utils.downloadmanager;

import java.io.File;
import java.net.URLDecoder;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.os.Environment;
import android.os.StatFs;

import com.lingyang.base.utils.Log;
import com.lingyang.base.utils.downloadmanager.DownloadConfiguration.DownloadTable;
import com.lingyang.base.utils.downloadmanager.excutor.DownloadExecutorTarget;

public class DownloadUtil {

	public static boolean checkSDPath() {
		return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
	}
	
	/**
	 * 获取内置sd卡的路径,如果没有挂载则返回空
	 * 
	 * @param needMounted
	 *            需要挂载如果当前没有挂载则返回空，否则直接返回路径
	 * @return
	 */
	public static String getSDPath(boolean needMounted) {
		if (needMounted) {
			if (checkSDPath()) {
				return Environment.getExternalStorageDirectory().toString();
			} else {
				return null;
			}
		} else {
			return Environment.getExternalStorageDirectory().getPath();
		}
	}
	
	/**
	 * 获取可用空间
	 * 
	 * @param pathStr
	 * @return
	 */
	public static long getAvailaleSize(String pathStr) {
		File path = new File(pathStr); // 取得sdcard文件路径
		if (!path.exists()) {
			return -1;
		}
		StatFs stat = new StatFs(path.getPath());
		long blockSize = stat.getBlockSize();
		long availableBlocks = stat.getAvailableBlocks();
		return availableBlocks * blockSize;
	}
	
	public static String getFileName(String path) {
		if (path == null) {
			return null;
		}
		int index = -1;
		index = path.lastIndexOf("/");
		return path.substring(index + 1);
	}
	
	public static String getPreDirPath(String path) {
		if (path == null) {
			return null;
		}
		int index = path.lastIndexOf("/");
		if (index < 0) {
			return null;
		} else if (index == 0) {
			index += 1;
		}
		String prePath = path.substring(0, index);
		return prePath;
	}
	
	public static String getUrlFileName(String url) {
		int begin = url.lastIndexOf("/");
		if (begin < 0) {
			return null;
		}
		int end = url.length();
		int index2 = url.indexOf("?", begin);
		if (index2 > 0) {
			end = index2;
		}
		String name = url.substring(begin+1, end);
		name = URLDecoder.decode(name);
		return name;
	}
	
	public static DecimalFormat DIGITAL_FORMAT_1 = new DecimalFormat("####.0");
	public static String parseFileSize(long length) {

		if (length == 0) {
			return "0M";
		}

		String[] syn = { "B", "KB", "M", "G" };
		int i = 0;
		float f = length;
		while (f >= 1024) {
			if (i >= syn.length - 1) {
				break;
			}
			f = f / 1024;
			i++;
		}

		String size = DIGITAL_FORMAT_1.format(f) + syn[i];
		return size;
	}
	
	public static void printStack(String tag) {
		StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
		for(int i = 0;i < stackTraceElements.length;i++) {
			StackTraceElement element = stackTraceElements[i];
			String name = element.getMethodName();
			Log.i(tag, element.getClassName() + "." + name + "() " + element.getLineNumber());
		}
	}
	
	public static String getExceptionMsg(Exception e) {
		if (e == null) {
			return null;
		}
		StackTraceElement[] traces = e.getStackTrace();
		if (traces == null) {
			return null;
		}
		StringBuilder builder = new StringBuilder();
		builder.append(e.toString());
		builder.append("\n");
		for(StackTraceElement trace : traces) {
			builder.append("\tat\n ");
			builder.append(trace);
		}
		return builder.toString();
	}
	
	public static int getDownloadProgress(long totalSize, long downloadSize) {
		if (totalSize > 0 && downloadSize > 0) {
			int progress = (int)(((long)downloadSize*100)/totalSize);  
			return progress;
		}
		return 0;
	}
	
	public static boolean isSupportTable(String tableName) {
		try {
			DownloadTable.valueOf(tableName);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	public static String parseMapToString(Map<String, String> map) {
		if (map == null) {
			return null;
		}
		StringBuilder builder = new StringBuilder();
		Iterator<String> it = map.keySet().iterator();
		while(it.hasNext()) {
			String key = it.next();
			String value = map.get(key);
			builder.append(key);
			builder.append("=");
			builder.append(value);
			
			if (it.hasNext()) {
				builder.append(";");
			}
		}
		return builder.toString();
	}
	
	public static Map<String, String> parseStringToMap(String str) {
		if (str == null) {
			return null;
		}
		Map<String, String> map = new HashMap<String, String>();
		String[] pairs = str.split(";");
		for(String pair : pairs) {
			String values[] = pair.split("=");
			if (values.length != 2) {
				continue;
			}
			map.put(values[0], values[1]);
			System.out.println("key:" + values[0] + " - value:" + values[1]);
		}
		return map;
	}
	
	public static boolean containsDownloadExecutor(List<DownloadExecutorTarget> list, String url) {
		if (list == null || url == null) {
			return false;
		}
		for(DownloadExecutorTarget executor : list) {
			if (url.equals(executor.downloadId)) {
				return true;
			}
		}
		return false;
	}
}

