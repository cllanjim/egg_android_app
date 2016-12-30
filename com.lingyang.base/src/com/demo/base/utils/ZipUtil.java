package com.lingyang.base.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

/**
 * GZIP和ZIP压缩、解压操作类
 *
 */
public class ZipUtil {

	/**
	 * gZip解压方法
	 **/
	public static byte[] unGZip(byte[] data) {
		if (data == null) {
			return null;
		}
		try {
			byte[] buf = new byte[1024];
			int num = -1;
			ByteArrayInputStream bis = new ByteArrayInputStream(data);
			GZIPInputStream gzip = new MultiMemberGZIPInputStream(bis);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			while ((num = gzip.read(buf, 0, buf.length)) != -1) {
				baos.write(buf, 0, num);
			}
			byte[] b = baos.toByteArray();
			baos.flush();
			baos.close();
			gzip.close();
			bis.close();
			return b;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	/**
	 * [对GZIP文件进行解压]
	 * 
	 * @param targetFilePath
	 * @param saveFilePath
	 * @return
	 */
	public static boolean unGZip(String targetFilePath, String saveFilePath) {
		if (targetFilePath == null || saveFilePath == null) {
			return false;
		}
		try {
		    GZIPInputStream in = new MultiMemberGZIPInputStream(new FileInputStream(targetFilePath));
		    OutputStream out = new FileOutputStream(saveFilePath);

		    byte[] buf = new byte[1024];
		    int len;
		    while ((len = in.read(buf)) > 0) {
		        out.write(buf, 0, len);
		    }

		    in.close();
		    out.close();
		    return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * [对zip文件进行解压]
	 * 
	 * @param zipPath
	 * @param descDir
	 * @author isea533
	 */
	public static void unZipFiles(String zipPath, String descDir)
			throws ZipException, IOException {
		unZipFiles(new File(zipPath), descDir);
	}

	/**
	 * 解压文件到指定目录
	 * 
	 * @param zipFile
	 * @param descDir
	 * @author isea533
	 */
	public static void unZipFiles(File zipFile, String descDir)
			throws ZipException, IOException {
		File pathFile = new File(descDir);
		if (pathFile.exists()) {
			if (pathFile.isFile()) {
				pathFile.delete();
				pathFile.mkdir();
			}
		} else {
			pathFile.mkdirs();
		}
		
		ZipFile zip = new ZipFile(zipFile);
		for (Enumeration entries = zip.entries(); entries.hasMoreElements();) {
			ZipEntry entry = (ZipEntry) entries.nextElement();
			String zipEntryName = entry.getName();
			InputStream in = zip.getInputStream(entry);
			String outPath = (descDir + zipEntryName).replaceAll("\\*", "/");
			// 判断路径是否存在,不存在则创建文件路径
			File file = new File(outPath.substring(0, outPath.lastIndexOf('/')));
			if (!file.exists()) {
				file.mkdirs();
			}
			// 判断文件全路径是否为文件夹,如果是上面已经上传,不需要解压
			if (new File(outPath).isDirectory()) {
				continue;
			}

			OutputStream out = new FileOutputStream(outPath);
			byte[] buf1 = new byte[1024];
			int len;
			while ((len = in.read(buf1)) > 0) {
				out.write(buf1, 0, len);
			}
			in.close();
			out.close();
		}
	}
}
