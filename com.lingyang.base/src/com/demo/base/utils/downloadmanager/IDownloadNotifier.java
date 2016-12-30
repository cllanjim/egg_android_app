package com.lingyang.base.utils.downloadmanager;

import android.content.Context;

/**
 * 下载通知者
 */
public interface IDownloadNotifier {
	
	/**
	 * 下载状态改变事件
	 * @param context
	 * @param item
	 * @param downloadingCount
	 */
	void onDownloadStateChanged(Context context, IDownloadService service);
	
	void dismissNotification(Context context);
}

