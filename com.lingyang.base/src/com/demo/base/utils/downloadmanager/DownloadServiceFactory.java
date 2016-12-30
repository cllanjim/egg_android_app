package com.lingyang.base.utils.downloadmanager;

import android.content.Context;

/**
 * [下载服务生成工厂]<br/>
 */
public class DownloadServiceFactory {

	/**
	 * 普通单例
	 */
	public static final int TYPE_NORMAL = 0;
	/**
	 * 进程内Service
	 */
	public static final int TYPE_SERVICE = 1;
	/**
	 * 跨进程Service
	 */
	public static final int TYPE_REMOTE_SERVICE = 2;
	
	public static IDownloadService getDownloadService(Context context, int type, String instanceId) {
		if (instanceId == null) {
			instanceId = KeyConstants.DOWNLOADER_INSTANCE_ID;
		}
		IDownloadService service = null;
		switch (type) {
		case TYPE_NORMAL:
			service = DownloaderBridge.getInstance(context, instanceId);
			break;
		case TYPE_SERVICE:
			service = new DownloadServiceController(context);
			break;
		case TYPE_REMOTE_SERVICE:
			
			break;
		default:
			break;
		}
		return service;
	}
}

