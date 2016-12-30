package com.antelope.sdk.utils;

import java.nio.ByteBuffer;

public class ACUtilAPI {
	static {
		System.loadLibrary("acutil");
	}

	public static final int IMAGE_NV21 = 17;
	public static final int IMAGE_YV12 = 0x32315659;

	public static final int COLOR_FormatYUV420Planar = 19;
	public static final int COLOR_FormatYUV420SemiPlanar = 21;
	
	public static native int ProcessCameraPreview(int width, int height, int rotation, int inFormat, int outFormat,
			ByteBuffer inBuf, int inOffset, ByteBuffer outBuf, int outOffset);
	
}
