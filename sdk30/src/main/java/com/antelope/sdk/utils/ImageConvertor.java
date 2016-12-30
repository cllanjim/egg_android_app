package com.antelope.sdk.utils;

public class ImageConvertor {
	public static void convertYUV420PToNV21(byte[] src, int width, int height, int[] strides, byte[] dst) {
		int ylen = width * height;
		int yStride = width;
		int halfWidth = width / 2;
		int halfHeight = height / 2;
		int uStride = halfWidth;
		int vStride = halfWidth;
		if (strides != null) {
			yStride = strides[0];
			uStride = strides[1];
			vStride = strides[2];
		}
		if (yStride == width) {
			System.arraycopy(src, 0, dst, 0, ylen);
		} else {
			for (int i=0; i<height; i++) {
				System.arraycopy(src, i*yStride, dst, i*width, width);
			}
		}
		int offset = yStride * height;
		for (int i=0; i<halfHeight; i++) {
			for (int j=0; j<halfWidth; j++) {
				dst[ylen + i*width + j*2 + 1] = src[offset + i*uStride + j];
			}
		}
		offset += uStride * halfHeight;
		for (int i=0; i<halfHeight; i++) {
			for (int j=0; j<halfWidth; j++) {
				dst[ylen + i*width + j*2] = src[offset + i*vStride + j];
			}
		}
	}
	
	public static void convertYUV420SPToNV21(byte[] src, int width, int height, int[] strides, byte[] dst) {
		int ylen = width * height;
		int yStride = width;
		int halfHeight = height / 2;
		int uvStride = width;
		if (strides != null) {
			yStride = strides[0];
			uvStride = strides[1];
		}
		if (yStride == width) {
			System.arraycopy(src, 0, dst, 0, ylen);
		} else {
			for (int i=0; i<height; i++) {
				System.arraycopy(src, i*yStride, dst, i*width, width);
			}
		}
		int offset = yStride * height;
		for (int i=0; i<halfHeight; i++) {
			for (int j=0; j<width; j+=2) {
				dst[ylen + i*width + j + 1] = src[offset + i*uvStride + j];
				dst[ylen + i*width + j] = src[offset + i*uvStride + j + 1];
			}
		}
	}
}
