package com.antelope.sdk.utils;

import java.io.File;
import java.io.IOException;

import android.os.Environment;
import android.text.format.DateFormat;

/**
* @author liaolei 
* @version 创建时间：2016年9月22日 
* 类说明
*/
public class Utils {
	public static final String ROOT_DIR="/sdcard/topvdn_test.mp4";
	
//	static{
//		File dir=new File(ROOT_DIR);
//		if(!dir.exists()&&dir.isDirectory()){
//			dir.mkdirs();
//		}
//	}
	
	public static File createFile(){
		File file=new File(ROOT_DIR);
		if(file.exists()){
			file.delete();
		}
		try {
			file.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return file;
	}
	
	
	public static String timestamp2YYMMDD(long timestamp){
		DateFormat format=new DateFormat();
		String str=(String) format.format("YY_MM_dd", timestamp);
		return str;
	}
	

}
