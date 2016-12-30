package com.lingyang.base.utils.filetype;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.lingyang.base.utils.Log;

/**
 * [文件扫描工具类]
 */
public class FileScanUtils {
	private final static String TAG = "FileScanUtils";

	/**
	 * [获取系统中存储的媒体路径]<BR>
	 * 包括视频、音乐、图片文件路径
	 * @param context
	 * @param pathList
	 * @return
	 */
	public static List<String> getSysPathList(Context context, List<String> pathList) {
		
		getScanPathList(context, pathList, MediaStore.Video.Media.EXTERNAL_CONTENT_URI, MediaStore.Video.Media.DATA);
		getScanPathList(context, pathList, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, MediaStore.Audio.Media.DATA);
		getScanPathList(context, pathList, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, MediaStore.Images.Media.DATA);
		
		if(pathList == null) {
			return pathList;
		}
		Log.i(TAG, "SYSTEM - 可能存在媒体文件的路径 " + pathList.size());
		for(String path : pathList) {
			Log.i(TAG, path);
		}
		return pathList;
	}
	
	/**
	 * [获取系统存储的音乐文件路径列表]<BR>
	 * @param context
	 * @return
	 */
	public static List<String> getSysMusicPathList(Context context) {
		List<String> list = new ArrayList<String>();
		try {
			getScanPathList(context, list, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, MediaStore.Audio.Media.DATA);
		}catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}
	
	/**
	 * [获取系统存储的视频文件路径列表]<BR>
	 * @param context
	 * @return
	 */
	public static List<String> getSysVideoPathList(Context context) {
		List<String> list = new ArrayList<String>();
		try {
			getScanPathList(context, list, MediaStore.Video.Media.EXTERNAL_CONTENT_URI, MediaStore.Video.Media.DATA);
		}catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}
	
	public static List<String> getSysImagePathList(Context context) {
		List<String> list = new ArrayList<String>();
		try {
			getScanPathList(context, list, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, MediaStore.Images.Media.DATA);
			return list;
		}catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}
	
	public static void getAllDir(LinkedList<String> folderList, List<String> dirList) {
		if (dirList == null || folderList == null) {
			return;
		}
		while(! folderList.isEmpty()) {
			String folder = folderList.removeLast();
			File dir = new File(folder);
			//所有非隐藏目录全部递归添加
			if (! dir.exists() || !dir.isDirectory() || dir.isHidden()) {
				continue;
			}
			dirList.add(folder);
			
			String[] paths = dir.list();
			if (paths == null || paths.length == 0) {
				continue;
			}
			
			for (String path : paths) {
				String filePath = folder + "/" + path;
				File file = new File(filePath);
				if (! file.exists() || !file.isDirectory() || file.isHidden()) {
					continue;
				}
				folderList.add(filePath);
			}
		}
	}
	
	private static void getScanPathList(Context context, List<String> pathList, Uri uri, String searchColumns) {
		if (pathList == null || context == null || searchColumns == null) {
			return;
		}
		
		String[] mediaColumns = {searchColumns};
		Cursor cursor = context.getContentResolver().query(
				uri,
				mediaColumns, 
				null, 
				null,
				null);
		if(cursor==null){
			return;
		}
		if (cursor.moveToFirst()) {
			do {
				String filePath = cursor.getString(cursor.getColumnIndex(searchColumns));
				if (filePath == null) {
					continue;
				}
				int index = filePath.lastIndexOf("/");
				if (index == -1) {
					continue;
				}
				String dirPath = filePath.substring(0, index);
				if (pathList.contains(dirPath)) {
					continue;
				}
				File dirFile = new File(dirPath);
				if (! dirFile.exists()) {
					continue;
				}
				pathList.add(dirPath);
			} while (cursor.moveToNext());
		}
	}
}

