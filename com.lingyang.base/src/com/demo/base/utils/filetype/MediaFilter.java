package com.lingyang.base.utils.filetype;

import java.io.File;
import java.io.FileFilter;
import java.util.regex.Pattern;

import com.lingyang.base.utils.filetype.FileTypes;

/**
 * [Media文件过滤]
 */
public class MediaFilter implements FileFilter {
	
	public static final String TAG = MediaFilter.class.getSimpleName();
	
	public static final long AUDIO_FILE_SIZE = 0;
	
	public static final long VIDEO_FILE_SIZE = 0;
	
	public static final String regexpMedia = "\\.("
			+ FileTypes.getAudioTypes() + FileTypes.getAudioTypes() + FileTypes.getImageTypes() + ")$";
	public static final String regexpVideo = "\\.("
			+ FileTypes.getVideoType() + ")$";
	public static final String regexpAudio = "\\.("
			+ FileTypes.getAudioTypes() + ")$";
	public static final String regexpImage = "\\.("
			+ FileTypes.getImageTypes() + ")$";
	
	private int mFilterType;
	
	private boolean mIsScanDir = false;
	
	public MediaFilter(){
		mFilterType = FileTypes.TYPE_ALL_FILE;
	}
	
	public MediaFilter(int fileType){
		this.mFilterType = fileType;
	}
	
	public void setScanSubDir(boolean b) {
		this.mIsScanDir = b;
	}
	
	@Override
	public boolean accept(File file) {
		if (file == null) {
			return false;
		}
		
		if (file.isHidden()) {
			return false;
		}
		
		if (mFilterType == FileTypes.TYPE_ALL_FILE) {
			return true;
		}
		
		if (isDir(file)) {
			if (mFilterType == FileTypes.TYPE_FOLDER) {
				return true;
			}
			if (mIsScanDir) {
				return true;
			} else {
				return false;
			}
		}
		
		switch (mFilterType) {
		case FileTypes.TYPE_ALL_MEDIA:
			return isMedia(file);
		case FileTypes.TYPE_VIDEO:
			return isVideo(file);
		case FileTypes.TYPE_AUDIO:
			return isAudio(file);
		case FileTypes.TYPE_IMAGE:
			return isImage(file);
		default:
			break;
		}
		
		return false;
	}

	public boolean isDir(File file) {
		if (file.isDirectory()) {
			return true;
		}
		return false;
	}
	
	public static boolean isMedia(File file) {
		if (isMedia(file.getName())) {
//			Log.v(TAG, "扫描到【媒体】:" + file.getAbsolutePath());
			return file.length() > VIDEO_FILE_SIZE;
		}
		return false;
	}
	
	public static boolean isMedia(String fileName) {
		return Pattern.compile(regexpMedia).matcher(fileName.toLowerCase()).find();
	}
	
	public static boolean isVideo(File file) {
		if (isVideo(file.getName())) {
			return file.length() > VIDEO_FILE_SIZE;
		}
		return false;
	}
	
	public static boolean isVideo(String fileName) {
		return Pattern.compile(regexpVideo).matcher(fileName.toLowerCase()).find();
	}
	
	public static boolean isAudio(File file){
		if (isAudio(file.getName())) {
//			Log.v(TAG, "扫描到【音乐】:" + file.getAbsolutePath());
			return file.length() > VIDEO_FILE_SIZE;
		} else {
			return false;
		}
	}
	
	public static boolean isAudio(String filename) {
		return Pattern.compile(regexpAudio).matcher(filename.toLowerCase()).find();
	}
	
	public static boolean isImage(File file){
		if (isImage(file.getName())) {
//			Log.v(TAG, "扫描到【图片】:" + file.getAbsolutePath());
			return file.length() > VIDEO_FILE_SIZE;
		} else {
			return false;
		}
	}
	
	public static boolean isImage(String fileName) {
		return Pattern.compile(regexpImage).matcher(fileName.toLowerCase()).find();
	}
}
