package com.lingyang.sdk.util;

import android.util.Log;

public class CLog {

    static boolean debug = false;

    private CLog() {
        /* Protect from instantiations */
    }

    public static void e(String message) {
        if (!isDebuggable())
            return;
        StackTraceElement[] sElements=new Throwable().getStackTrace();
        String className=sElements[1].getFileName();
        String methodName=sElements[1].getMethodName();
        int lineNumber=sElements[1].getLineNumber();

        Log.e(className, createLog(methodName,lineNumber,message));
    }

    public static boolean isDebuggable() {
        return debug;
    }

    public static void setDebuggable(boolean debuggable) {
        debug = debuggable;
    }

    private static String createLog(String methodName,int lineNumber,String log) {
        String utf8Sting = new String();
//        String name = lineNumber + ":" + log;

        StringBuffer buffer = new StringBuffer();

        buffer.append("LYSDK[");
        buffer.append(methodName);
        buffer.append(":");
        buffer.append(lineNumber);
        buffer.append("]");
        buffer.append(log);
//        try {
//            utf8Sting = new
//                    String(buffer.toString().getBytes("gbk"), "utf-8");
//        } catch
//                (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }

        return buffer.toString();
    }

    public static void i(String message) {
        if (!isDebuggable())
            return;

        StackTraceElement[] sElements=new Throwable().getStackTrace();
        String className=sElements[1].getFileName();
        String methodName=sElements[1].getMethodName();
        int lineNumber=sElements[1].getLineNumber();

        Log.i(className, createLog(methodName,lineNumber,message));
    }

    public static void d(String message) {
        if (!isDebuggable())
            return;

        StackTraceElement[] sElements=new Throwable().getStackTrace();
        String className=sElements[1].getFileName();
        String methodName=sElements[1].getMethodName();
        int lineNumber=sElements[1].getLineNumber();

        Log.d(className, createLog(methodName,lineNumber,message));
    }

    public static void v(String message) {
        if (!isDebuggable())
            return;

        StackTraceElement[] sElements=new Throwable().getStackTrace();
        String className=sElements[1].getFileName();
        String methodName=sElements[1].getMethodName();
        int lineNumber=sElements[1].getLineNumber();

        Log.v(className, createLog(methodName,lineNumber,message));
    }

    public static void w(String message) {
        if (!isDebuggable())
            return;

        StackTraceElement[] sElements=new Throwable().getStackTrace();
        String className=sElements[1].getFileName();
        String methodName=sElements[1].getMethodName();
        int lineNumber=sElements[1].getLineNumber();

        Log.w(className, createLog(methodName,lineNumber,message));
    }

}
