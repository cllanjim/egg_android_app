package com.lingyang.sdk.player.widget;

import com.lingyang.sdk.exception.LYException;

/**
 * seek结果监听，点播状态下有效
 */
public interface OnSeekCompleteListener {
	void onSeekSuccess(int time);
	
	void onSeekError(LYException exception);
}
