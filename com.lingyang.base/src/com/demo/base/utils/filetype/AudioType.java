package com.lingyang.base.utils.filetype;


/**
 * [音频类型]
 */
public enum AudioType {
	MP3("mp3"),
	WAV("wav"), 
	AMR("amr"), 
	AAC("aac"),
	M4A("m4a"), 
	OGG("ogg"), 
	MP2("mp2"), 
	WMA("wma"), 
	RA("ra"), 
	FLAC("flac"), 
	APE("ape"), 
	AC3("ac3");

	private String value;

	AudioType(String v) {
		this.value = v;
	}

	@Override
	public String toString() {
		return this.value;
	}
	
	public static boolean isContainsType(String v) {
		AudioType[] types = AudioType.values();
		for (AudioType t : types) {
			if (t.value.equals(v.toLowerCase())) {
				return true;
			}
		}
		return false;
	}
}
