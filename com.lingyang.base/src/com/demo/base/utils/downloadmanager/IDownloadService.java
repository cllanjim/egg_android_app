package com.lingyang.base.utils.downloadmanager;

import com.lingyang.base.utils.NetworkStatus;
import com.lingyang.base.utils.downloadmanager.storage.IDownloadTaskStorage;

public interface IDownloadService extends IBaseDownloadService{
	
	/**
	 * 设置是否存储下载任务队列的记录
	 * 本地存储方式可以为：DB、本地文件的json、xml等
	 * 
	 * @param isUse
	 */
	void setUseStorageTask(boolean isUse);
	
	/**
	 * 设置下载通知器
	 * 通过设置实例的方式进行设置
	 * 
	 * @param notifier
	 */
	void setDownloadNotifier(IDownloadNotifier notifier);
	
	/**
	 * 设置下载通知器
	 * 通过class的设置，通过反射生成相应的实例
	 * 
	 * @param clz
	 */
	<T extends IDownloadNotifier> void setNotifationClass(Class<T> clz);

	/**
	 * 获取服务状态
	 * 
	 * @return 
	 *	 	0 服务正在启动中
	 * 		1 服务已初始化
	 * 		2 服务正在允许中
	 * 		3 服务崩溃
	 */
	int getServiceState();
	
	boolean addServiceStateListener(IServiceStateListener listener);
	
	boolean removeServiceStateListener(IServiceStateListener listener);

	void setDownloadTaskStorage(int type);
	
	void setDownloadTaskStorage(IDownloadTaskStorage taskStorage);
	
	/**
	 * 是否自动更新通知条状态
	 * 
	 * @param isUpdateNotify
	 */
	void setUpdateDownloadNotify(boolean isUpdateNotify);
	
	/**
	 * [设置允许进行下载的网络状态]
	 * @param status 网络状态
	 */
	public void setAllowDownloadState(NetworkStatus[] status);
	
	/**
	 * [设置超时重试下载时候的重试间隔时间]
	 * @param intervals
	 */
	public void setRetryIntervals(long[] intervals);
	
	void showDownloadNotify();
	
	void dismissDownloadNotify();
	
	/**
	 * 设置下载存储器的表名、文件名等
	 * 
	 * @param storageName
	 */
	void setStorageName(String storageName);
}

