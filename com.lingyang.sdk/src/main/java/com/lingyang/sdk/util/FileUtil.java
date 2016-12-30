package com.lingyang.sdk.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.os.StatFs;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class FileUtil {

	private static FileUtil fileUtil;

	private FileUtil() {
	}

	public static FileUtil getInstance() {
		if (fileUtil == null) {
			fileUtil = new FileUtil();
		}
		return fileUtil;
	}

	public static void writeFile(File file, byte[] content, boolean append) throws IOException {
		FileOutputStream psuhOutputStream = new FileOutputStream(file, append);
		psuhOutputStream.write(content);
		psuhOutputStream.close();
	}

	public static String readStringFile(File file) throws IOException {
		return new String(readFile(file));
	}

	public static byte[] readFile(File file) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		FileInputStream fis = new FileInputStream(file);
		byte[] buffer = new byte[1024];
		int len = 0;
		while ((len = fis.read(buffer)) != -1) {
			baos.write(buffer, 0, len);
		}
		byte[] result = baos.toByteArray();
		fis.close();
		baos.close();
		return result;
	}

	public static File createFile(File dir, String name) throws IOException {
		return createFile(dir.getPath() + "/" + name);
	}

	public static File createFile(String filePath) throws IOException {
		File file = new File(filePath);
		deleteFile(file);
		if (!file.exists()) {
			file.createNewFile();
		}
		return file;
	}

	public static void deleteFile(File file) {
		if (file.exists())
			file.delete();
	}

	public static void EventCacheManagement(File eventFile) {
		if (eventFile.exists()) {
			List<File> events = Arrays.asList(eventFile.listFiles());
			if (events.size() > 100) {
				Collections.sort(events, new FileComparator());
				for (int i = 50; i < events.size(); i++) {
					File event = events.get(i);
					if (event != null && event.exists() && event.isFile()) {
						event.delete();
					}
				}
			}
		}
	}

	public static class FileComparator implements Comparator<File> {

		public int compare(File file1, File file2) {
			if (file1.lastModified() < file2.lastModified()) {
				return -1;
			} else {
				return 1;
			}
		}
	}

	public long getAvailableSpare() {
		return calcAvailableSpare();
	}

	@SuppressLint("NewApi")
	public void updateGallery(String filename, Context context)// filename是我们的文件全名，包括后缀哦
	{
		MediaScannerConnection.scanFile(context, new String[] { filename }, null,
				new MediaScannerConnection.OnScanCompletedListener() {

					public void onScanCompleted(String path, Uri uri) {
						CLog.i("Scanned " + path + ":");
						CLog.i("-> uri=" + uri);
					}
				});
	}

	/**
	 * 
	 * @return 单位 : M
	 */
	@SuppressWarnings("deprecation")
	public long calcAvailableSpare() {
		String sdCard = "";
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			sdCard = Environment.getExternalStorageDirectory().getPath();
		} else {
			sdCard = Environment.getDataDirectory().getPath();
		}
		StatFs statFs = new StatFs(sdCard);
		long blockSize = statFs.getBlockSize();
		long blocks = statFs.getAvailableBlocks();
		long spare = (blocks * blockSize) / (1024 * 1024);
		// CLog.d("calcAvailableSpare:" + spare);
		return spare;
	}
}
