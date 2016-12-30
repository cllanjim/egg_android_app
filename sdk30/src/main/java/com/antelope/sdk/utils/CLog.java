package com.antelope.sdk.utils;

import java.nio.ByteBuffer;

import android.util.Log;

public class CLog {
	public static final int LOG_LEVEL_VERBOSE = 0;
	public static final int LOG_LEVEL_DEBUG = 1;
	public static final int LOG_LEVEL_INFO = 2;
	public static final int LOG_LEVEL_WARN = 3;
	public static final int LOG_LEVEL_ERROR = 4;
	public static final int LOG_LEVEL_ASSERT = 5;

	static int mLogLevel = LOG_LEVEL_VERBOSE;
	
    private CLog(){
        /* Protect from instantiations */
    }
    
    public static void setLogLevel(int level) {
    	mLogLevel =  level;
	}

	public static int getLogLevel() {
		return mLogLevel;
	}
	
	private static boolean writable(int level) {
		if (level > LOG_LEVEL_ASSERT) {
			return false;
		}
		return level >= mLogLevel ? true : false;
	}
	
	private static String createLog( String log, Throwable tr, StackTraceElement[] sElements, int stacks ) {
		String content = "[" + sElements[stacks].getMethodName() + "():" + sElements[stacks].getLineNumber() + "]" + log;
		if (tr != null) {
			content += '\n' + Log.getStackTraceString(tr);
		}
		return content;
	}
	
	private static String getMethodNames(StackTraceElement[] sElements, int stacks){
		String className = sElements[stacks].getFileName();
		className = className.substring(0, className.lastIndexOf('.'));
		return className;
	}
	
	private static void output(int level, String message, Throwable tr, int stacks) {
		if (!writable(level)) {
			return;
		}
		
		StackTraceElement[] sElements = new Throwable().getStackTrace();
		String className = getMethodNames(sElements, stacks+1);
		String log = createLog(message, tr, sElements, stacks+1);
		
		switch (level) {
		case LOG_LEVEL_VERBOSE:
			Log.v(className, log);
			break;
		case LOG_LEVEL_DEBUG:
			Log.d(className, log);
			break;
		case LOG_LEVEL_INFO:
			Log.i(className, log);
			break;
		case LOG_LEVEL_WARN:
			Log.w(className, log);
			break;
		case LOG_LEVEL_ERROR:
			Log.e(className, log);
			break;
		case LOG_LEVEL_ASSERT:
			Log.wtf(className, log);
			break;
		}
	}

	public static void e(String message){
		output(LOG_LEVEL_ERROR, message, null, 1);
	}
	
	public static void e(String message, Throwable tr) {
		output(LOG_LEVEL_ERROR, message, tr, 1);
	}

	public static void i(String message){
		output(LOG_LEVEL_INFO, message, null, 1);
	}
	
	public static void i(String message, Throwable tr){
		output(LOG_LEVEL_INFO, message, tr, 1);
	}
	
	public static void d(String message){
		output(LOG_LEVEL_DEBUG, message, null, 1);
	}
	
	public static void d(String message, Throwable tr){
		output(LOG_LEVEL_DEBUG, message, tr, 1);
	}
	
	public static void v(String message){
		output(LOG_LEVEL_VERBOSE, message, null, 1);
	}
	
	public static void v(String message, Throwable tr){
		output(LOG_LEVEL_VERBOSE, message, tr, 1);
	}	

	public static void w(String message){
		output(LOG_LEVEL_WARN, message, null, 1);
	}
	
	public static void w(String message, Throwable tr){
		output(LOG_LEVEL_WARN, message, tr, 1);
	}
	
	public static void wtf(String message){
		output(LOG_LEVEL_ASSERT, message, null, 1);
	}
	
	public static void wtf(String message, Throwable tr){
		output(LOG_LEVEL_ASSERT, message, tr, 1);
	}
	
	public static void dhs(String message, ByteBuffer buffer, int offset, int size) {
		String log = message + ": [ ";
		for (int i=offset; i<offset+size; i++) {
			String h=  Integer.toHexString(buffer.get(i)&0xff);
			if (h.length() == 1) {
				h = "0" + h;
			}
			log += h + " ";
		}
		output(LOG_LEVEL_DEBUG, log + "]", null, 1);
	}
	
}
