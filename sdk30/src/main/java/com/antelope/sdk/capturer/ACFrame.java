package com.antelope.sdk.capturer;

import java.nio.ByteBuffer;

/**
* @author liaolei 
* @version 创建时间：2016年9月8日 
* 该类是定义音视频流媒体数据参数类，包括数据buffer，偏移量，宽高，采样率，码率等信息
*/
public class ACFrame {
	public ByteBuffer buffer;
	public int offset;
	public int size;
	/**
	 * 视频请参考{@link ACImageFormat}，音频请参考{@link ACSampleFormat}
	 */
	public int format;
	public long timestamp;
}
