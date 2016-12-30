package com.lingyang.base.utils.filetype;


/**
 * [视频类型]
 */
public enum VideoType {
	GP("3gp"),
	AVI("avi"), 
	RM("rm"), 
	RMVB("rmvb"),
	MP4("mp4"), 
	FLV("flv"), 
	F4V("f4v"), 
	MPE("mpeg"), 
	MPEG("mpg"), 
	WMV("wmv"), 
	QMV("qmv"), 
	QMVB("qmvb"), 
	TS("ts"), 
	M4V("m4v"), 
	MKV("mkv"),
	MOV("mov"), 
	VOB("vob"), 
	AMV("amv"), 
	BIK("bik"), 
	M2TS("m2ts"), 
	PMP("pmp"), 
	RAM("ram"), 
	SMV("smv"), 
	TP("tp");

	private String value;

	VideoType(String v) {
		this.value = v;
	}

	@Override
	public String toString() {
		return this.value;
	}
	
	public static boolean isContainsType(String v) {
		VideoType[] types = VideoType.values();
		for (VideoType t : types) {
			if (t.value.equals(v.toLowerCase())) {
				return true;
			}
		}
		return false;
	}
}
