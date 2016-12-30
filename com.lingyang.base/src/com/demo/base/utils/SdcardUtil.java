package com.lingyang.base.utils;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;

public class SdcardUtil {
	/**
	 * 检查内置的sd卡是否可用
	 * 
	 * @return
	 */
	public static boolean checkSDPath() {
		return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
	}
	
	/**
	 * 获取内置sd卡的路径,如果没有挂载则返回空
	 * 
	 * @return
	 */
	public static String getSDPath() {
		return getSDPath(true);
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

	/**
	 * 
	 * 获取sd卡的大小
	 * 
	 * @param pathStr
	 *            sd卡的路径
	 * @return sd卡的大小，单位byte
	 */
	public static long getSdCardSize(String pathStr) {
		File path = new File(pathStr);
		if (!path.exists()) {
			return -1;
		}
		StatFs stat = new StatFs(path.getPath());
		long blockSize = stat.getBlockSize();
		long totalBlocks = stat.getBlockCount();
		return totalBlocks * blockSize;
	}
	
	
	/**
	 * 获取所有sd卡
	 * 
	 * @return
	 */
	public static String[] getAllStoragePaths(Context ctx) {
		return getAllStoragePaths(ctx, null);
	}
	
	private static final String MOUNTS_PATH = "/proc/mounts";
	private static final String VOLD_PATH = "/system/etc/vold.fstab";
	
	private static final int TYPE_ALL = 3;
	private static final int TYPE_INTERN = 1;
	private static final int TYPE_EXTERN = 2;
	
	/**
	 * 获取所有sd卡
	 * @param ctx
	 * @param filterPath 需要过滤的路径
	 * @return
	 */
	public static String[] getAllStoragePaths(Context ctx, String filterPath) {
		List<String> paths = null;
		// 首先通过sdk4.0以上的隐藏方法来获取挂载的sd卡
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD){
			paths = findByStorageManager(ctx, TYPE_ALL);
		}
		
		if(paths == null || paths.size() < 1){
			// 通过sdk隐藏方法获取失败，再通过读取系统文件获取
			paths = findByReadFile(TYPE_ALL);
		}
		 
		if(paths != null && paths.size() > 0){
			filterSamePath(paths, filterPath);
			return paths.size() > 0 ? paths.toArray(new String[paths.size()]) : null;
		}
		return null;
	}
	
	public static void filterSamePath(List<String> list, String path) {
		if (list == null || path == null) {
			return;
		}
		path = path.endsWith("/") ? path.substring(0, path.length()-1) : path;
		Iterator<String> it = list.iterator();
		while(it.hasNext()) {
			String str = it.next();
			String subStr = str.endsWith("/") ? str.substring(0, str.length()-1) : str;
			if (subStr.equals(path)) {
				it.remove();
			}
		}
	}
	
	/**
	 * 通过4.0以上的隐藏api获取
	 * @param ctx
	 * @param type
	 * @return
	 */
	@SuppressLint("NewApi")
	private static List<String> findByStorageManager(Context ctx, int type) {
		if (ctx == null) {
			return null;
		}
		try {
			StorageManager sm = (StorageManager) ctx.getSystemService(Context.STORAGE_SERVICE);
			Method method = StorageManager.class.getMethod("getVolumeList", new Class[]{});
			StorageVolume[] volumes = (StorageVolume[]) method.invoke(sm, new Object[]{});
			ArrayList<String> result = new ArrayList<String>(); 
			for (StorageVolume volume : volumes) {
				int volType = volume.isRemovable() ? TYPE_EXTERN : TYPE_INTERN;
				int resType = volType & type;
				if(resType != 0){
					result.add(volume.getPath());
				}
			}
			
//			// 如果是获取外置SD卡，需要将调用系统api获取到的内置SD卡路径过滤掉
//			String filterPath = type == TYPE_EXTERN ? 
//					Environment.getExternalStorageDirectory().getPath() : null;
			String filterPath = null;
			filterValidPaths(result, filterPath);
			return result;
		} catch (Exception e) {
		}
		return null;
	}
	
	/**
	 * 通过读取系统文件获取
	 * @param internPath
	 * @return
	 */
	private static List<String> findByReadFile(int type){
		String internPath = Environment.getExternalStorageDirectory().getPath();
		if(type == TYPE_INTERN){
			List<String> paths = new ArrayList<String>();
			paths.add(internPath);
			filterValidPaths(paths);
			return paths;
		}
		
		String filterPath = type == TYPE_EXTERN ? internPath : null;
		
		List<String> paths =  new ArrayList<String>();
		List<String> voldPaths = readVoldFile(filterPath);
		List<String> mountPaths = readMountsFile(filterPath);

		if (voldPaths == null || voldPaths.size() < 1 || mountPaths == null || mountPaths.size() < 1) {
			return null;
		}

		for (String path : voldPaths) {
			if (mountPaths.contains(path)) {
				paths.add(path);
			}
		}
		
		filterValidPaths(paths);
		return paths;
	}
	
	/**
	 * 读取系统文件mounts获取挂载的SD卡
	 * 
	 * @param filterPath
	 * @return
	 */
	private static List<String> readMountsFile(String filterPath) {
		File file = new File(MOUNTS_PATH);
		if (!file.exists()) {
			return null;
		}

		Scanner scanner = null;
		List<String> paths = null;
		try {
			scanner = new Scanner(file);
			paths = new ArrayList<String>();
			while (scanner.hasNext()) {
				String line = scanner.nextLine();
				if (line.startsWith("/dev/block/vold/")) {
					String[] lineElements = line.split(" ");
					String element = lineElements[1];
					// don't add the default mount path
					// it's already in the list.
					if (!element.equals(filterPath) && !paths.contains(element)) {
						paths.add(element);
					}
				}
			}

		} catch (Exception e) {
		} finally {
			if (scanner != null) {
				scanner.close();
			}
		}
		return paths;
	}
	
	/**
	 * 读取系统文件vold.fstab获取挂载的SD卡
	 * 
	 * @param filterPath
	 * @return
	 */
	private static List<String> readVoldFile(String filterPath) {
		File file = new File(VOLD_PATH);
		if (!file.exists()) {
			return null;
		}

		Scanner scanner = null;
		List<String> paths = null;
		try {
			scanner = new Scanner(file);
			paths = new ArrayList<String>();
			while (scanner.hasNext()) {
				String line = scanner.nextLine();
				if (line.startsWith("dev_mount")) {
					String[] lineElements = line.split(" ");
					String element = lineElements[2];

					if (element.contains(":"))
						element = element.substring(0, element.indexOf(":"));

					if (element.contains("usb"))
						continue;

					// don't add the default vold path
					// it's already in the list.
					if (!element.equals(filterPath) && !paths.contains(element)) {
						paths.add(element);
					}
				}
			}

		} catch (Exception e) {
		} finally {
			if (scanner != null) {
				scanner.close();
			}
		}
		return paths;
	}

	/**
	 * 将不存在的sd卡过滤掉；
	 * 
	 * @param paths
	 */
	private static void filterValidPaths(List<String> paths) {
		filterValidPaths(paths, null);
	}
	
	/**
	 * 将不存在的SD卡和需要过滤的路径过滤掉
	 * @param paths
	 * @param filterPath
	 */
	private static void filterValidPaths(List<String> paths, String filterPath) {
		if (paths == null || paths.size() < 1) {
			return;
		}

		Iterator<String> it = paths.iterator();
		while (it.hasNext()) {
			String path = it.next();
			File file = new File(path);
			if (path == null || path.equals(filterPath) || 
					!file.exists() || !file.isDirectory() || !file.canWrite()) {
				it.remove();
			}
		}
	}

}
