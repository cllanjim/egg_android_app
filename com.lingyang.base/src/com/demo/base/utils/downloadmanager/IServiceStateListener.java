package com.lingyang.base.utils.downloadmanager;

public interface IServiceStateListener {

	/**
	 * 发现磁盘已满时提示
	 */
	void onDownloadErrorWithDiskNotEnough();
	
	/**
	 * @param state
	 * 		0 服务正在启动中
	 * 		1 服务已初始化
	 * 		2 服务正在允许中
	 * 		3 服务崩溃
	 */
	void onServiceStateChanged(int state);
}
