package com.lingyang.base.utils.downloadmanager.storage;

import java.util.List;

import android.content.Context;

/**
 * [下载任务存储]<br/>
 */
public interface IDownloadTaskStorage {

	void init(Context context, String storageName);
	
	List<DownloadBean> queryAllTask();
	
	List<DownloadBean> queryAllTask(String owner);
	
	DownloadBean queryTask(String downloadId);
	
	boolean updateTask(DownloadBean downloadBean);
	
	boolean addTask(DownloadBean downloadBean);
	
	boolean addTasks(List<DownloadBean> list);
	
	boolean deleteTask(String downloadId);
	
	boolean deleteAllTask();
}

