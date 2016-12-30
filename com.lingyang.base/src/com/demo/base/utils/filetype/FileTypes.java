package com.lingyang.base.utils.filetype;

import java.io.File;

/**
 * [文件类型]
 */
public class FileTypes {

	public static final int TYPE_UNKOWN = -999;
	public static final int TYPE_ALL_FILE = -1;
	public static final int TYPE_ALL_MEDIA = -2;
	public static final int TYPE_VIDEO = 1;
	public static final int TYPE_AUDIO = 2;
	public static final int TYPE_IMAGE = 3;
	public static final int TYPE_FOLDER = 4;
	public static final int TYPE_TXT = 5;
	public static final int TYPE_APK = 6;
	public static final int TYPE_RAR = 7;
	public static final int TYPE_PPT = 8;
	public static final int TYPE_EXCEL = 9;
	public static final int TYPE_HTML = 10;
	public static final int TYPE_PDF = 11;
	public static final int TYPE_WORD = 12;

	public static String getAudioTypes() {
		StringBuilder sb = new StringBuilder();
		AudioType[] types = AudioType.values();
		for (AudioType t : types) {
			sb.append(t.toString()).append("|");
		}
		return sb.toString();
	}

	public static String getImageTypes() {
		StringBuilder sb = new StringBuilder();
		ImageType[] types = ImageType.values();
		for (ImageType t : types) {
			sb.append(t.toString()).append("|");
		}
		return sb.toString();
	}

	public static String getVideoType() {
		StringBuilder sb = new StringBuilder();
		VideoType[] types = VideoType.values();
		for (VideoType t : types) {
			sb.append(t.toString()).append("|");
		}
		return sb.toString();
	}

	public static int getFileType(String path) {
		File file = new File(path);
		if (file.isDirectory()) {
			return TYPE_FOLDER;
		} 
		String value = path.substring(path.lastIndexOf(".") + 1);
		if (VideoType.isContainsType(value)) {
			return TYPE_VIDEO;
		} else if (AudioType.isContainsType(value)) {
			return TYPE_AUDIO;
		} else if (ImageType.isContainsType(value)) {
			return TYPE_IMAGE;
		} else if ("txt".equals(value)) {
			return TYPE_TXT;
		} else if ("rar".equals(value)) {
			return TYPE_RAR;
		} else if ("word".equals(value)) {
			return TYPE_WORD;
		} else if ("ppt".equals(value)) {
			return TYPE_PPT;
		} else if ("html".equals(value)) {
			return TYPE_HTML;
		} else if ("apk".equals(value)) {
			return TYPE_APK;
		} else if ("excel".equals(value)) {
			return TYPE_EXCEL;
		} else if ("pdf".equals(value)) {
			return TYPE_PDF;
		}
		return TYPE_UNKOWN;
	}
}
