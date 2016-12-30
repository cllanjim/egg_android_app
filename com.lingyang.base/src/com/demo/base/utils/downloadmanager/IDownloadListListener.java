package com.lingyang.base.utils.downloadmanager;


public interface IDownloadListListener {

	/**
	 * 下载列表更新，当存在该监听器的时候，会每秒调用一次通知更新
	 * 调用方需要获取列表的时候通过主动Get相应的列表即可
	 * 
	 * @param item
	 */
	void onDownloadListStateChanged();
}
