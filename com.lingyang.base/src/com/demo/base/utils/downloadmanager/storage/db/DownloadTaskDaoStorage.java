package com.lingyang.base.utils.downloadmanager.storage.db;

import java.util.List;

import android.content.Context;

import com.lingyang.base.utils.downloadmanager.DownloadUtil;
import com.lingyang.base.utils.downloadmanager.DownloadConfiguration.DownloadTable;
import com.lingyang.base.utils.downloadmanager.storage.DownloadBean;
import com.lingyang.base.utils.downloadmanager.storage.IDownloadTaskStorage;


public class DownloadTaskDaoStorage implements IDownloadTaskStorage  {
	
	private DownloadTaskDao dao;
	
	public DownloadTaskDaoStorage() {
	}
	
	@Override
	public void init(Context context, String tableName) {
		if (tableName == null) {
			tableName = DownloadTable.DOWNLOAD_FILE_TABLE.name();
		}
		boolean isSupport = DownloadUtil.isSupportTable(tableName);
		if (! isSupport) {
			throw new RuntimeException("DownloadTaskDaoStorage 初始化错误，不支持的TableName:" + tableName);
		}
		dao = new DownloadTaskDao(context, tableName);
	}
	
	public List<DownloadBean> queryAllTask() {
		return dao.queryAll();
	}
	
	public List<DownloadBean> queryAllTask(String owner) {
		return dao.queryAll(owner);
	}
	
	public DownloadBean queryTask(String downloadId) {
		return dao.queryById(downloadId);
	}
	
	public boolean updateTask(DownloadBean downloadBean) {
		return dao.update(downloadBean);
	}
	
	public boolean addTask(DownloadBean downloadBean) {
		long ret = dao.save(downloadBean);
		if (ret == -1) {
			return false;
		}
		return true;
	}
	
	public boolean addTasks(List<DownloadBean> list) {
		return dao.save(list);
	}
	
	public boolean deleteTask(String downloadID) {
		return dao.delete(downloadID);
	}
	
	public boolean deleteAllTask() {
		return dao.deleteAll();
	}
	
}

