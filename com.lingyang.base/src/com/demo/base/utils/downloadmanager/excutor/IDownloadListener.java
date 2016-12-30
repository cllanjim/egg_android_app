package com.lingyang.base.utils.downloadmanager.excutor;


public interface IDownloadListener {

	/**
	 * 下载状态改变事件
	 * 当具体的网络任务发送状态改变的时候会被调用
	 * 
	 * @param item
	 */
	boolean onDownloadStateChanged(DownloadState state);
}
