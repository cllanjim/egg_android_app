package com.lingyang.base.utils;
public class ExceptionUtil {

	public static String getExceptionMsg(Exception e) {
		if (e == null) {
			return null;
		}
		StackTraceElement[] traces = e.getStackTrace();
		if (traces == null) {
			return null;
		}
		StringBuilder builder = new StringBuilder();
		builder.append(e.toString());
		builder.append("\n");
		for(StackTraceElement trace : traces) {
			builder.append("\tat ");
			builder.append(trace);
		}
		return builder.toString();
	}
}

