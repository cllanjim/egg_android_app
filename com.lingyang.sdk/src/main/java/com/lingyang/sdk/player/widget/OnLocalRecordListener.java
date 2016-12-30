package com.lingyang.sdk.player.widget;

import com.lingyang.sdk.exception.LYException;

/**
 * 本地录像状态监听
 */
public interface OnLocalRecordListener {

	void onRecordStart();

	/**
	 * @param size
	 *            录制大小，单位：KB
	 * @param time
	 *            录制时长，单位：second
	 */
	void onRecordSizeChange(long size, long time);

	void onRecordError(LYException e);

	void onRecordStop();

}
