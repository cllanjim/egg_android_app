package com.lingyang.sdk.player.widget;

/**
 * 播放缓冲监听，点播状态下有效
 */
public interface OnPlayingBufferCacheListener {
	/**
	 * 当前缓冲的百分比
	 * 
	 * @param percent
	 */
	void onPlayingBufferCache(int percent);

	/**
	 * 开始缓冲
	 */
	void onBufferStart();

	/**
	 * 缓冲结束
	 */
	void onBufferEnd();
}
