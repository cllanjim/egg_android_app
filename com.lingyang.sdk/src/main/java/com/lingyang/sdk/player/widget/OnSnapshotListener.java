package com.lingyang.sdk.player.widget;

import com.lingyang.sdk.exception.LYException;

/**
 * 截图过程监听
 */
public interface OnSnapshotListener {
	/**
	 * 截图返回的参数错误
	 */
	int ERROR_RETURN_PARAM = -101;
	/**
	 * 存储空间不足
	 */
	int ERROR_NOT_ENOUGH_SPACE = -102;
	/**
	 * 不能解码成jpeg
	 */
	int ERROR_NOT_DECODE_TO_JPEG = -103;

	/**
	 * 截图成功
	 * 
	 * @param saveFullPath
	 *            保存的完整路径
	 */
	void onSnapshotSuccess(String saveFullPath);

	/**
	 * 截图失败
	 */
	void onSnapshotFail(LYException exception);
}

