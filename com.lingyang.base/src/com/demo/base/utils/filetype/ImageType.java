package com.lingyang.base.utils.filetype;


/**
 * [图片类型]
 */
public enum ImageType {
	JPG("jpg"),
	JPEG("jpeg"), 
	BMP("bmp"), 
	PNG("png"),
	GIF("gif");

	private String value;

	ImageType(String v) {
		this.value = v;
	}

	@Override
	public String toString() {
		return this.value;
	}
	
	public static boolean isContainsType(String v) {
		ImageType[] types = ImageType.values();
		for (ImageType t : types) {
			if (t.value.equals(v.toLowerCase())) {
				return true;
			}
		}
		return false;
	}
}
