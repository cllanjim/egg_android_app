package com.lingyang.base;

public class ResIdDynamicGenerator {

	public static int sDynamicResId = 1000;
	
	public static synchronized int genarateResId() {
		return sDynamicResId++;
	}
}
