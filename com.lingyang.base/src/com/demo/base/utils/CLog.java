package com.lingyang.base.utils;

import android.util.Log;

public class CLog {

	static String className;
	static String methodName;
	static boolean mIsDebuggable = false;
	static int lineNumber;
	
    private CLog(){
        /* Protect from instantiations */
    }
    
    public static void setDebuggable(boolean isDebuggable) {
    	mIsDebuggable =  isDebuggable;
	}

	public static boolean isDebuggable() {
		return mIsDebuggable;
	}

	private static String createLog( String log ) {
//		String utf8Sting = new String();
		String name = lineNumber+":"+log;
/*

		StringBuffer buffer = new StringBuffer();

		buffer.append("[");
		buffer.append(methodName);
		buffer.append(":");
		buffer.append(lineNumber);
		buffer.append("]");
		buffer.append(log);
		try {
			 utf8Sting = new String(buffer.toString().getBytes("gbk"),"utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
*/
		return name;
	}
	
	private static void getMethodNames(StackTraceElement[] sElements){
		className = sElements[1].getFileName();
		methodName = sElements[1].getMethodName();
		lineNumber = sElements[1].getLineNumber();
	}

	public static void e(String message){
		getMethodNames(new Throwable().getStackTrace());
		Log.e(className, createLog(message));
	}

	public static void i(String message){
		getMethodNames(new Throwable().getStackTrace());
		Log.i(className, createLog(message));
	}
	
	public static void d(String message){
		if (!isDebuggable())
			return;
		getMethodNames(new Throwable().getStackTrace());
		Log.d(className, createLog(message));
	}
	
	public static void v(String message){
		if (!isDebuggable())
			return;
		getMethodNames(new Throwable().getStackTrace());
		Log.v(className, createLog(message));
	}
	
	public static void w(String message){
		getMethodNames(new Throwable().getStackTrace());
		Log.w(className, createLog(message));
	}
	
	public static void wtf(String message){
		getMethodNames(new Throwable().getStackTrace());
		Log.wtf(className, createLog(message));
	}
}
