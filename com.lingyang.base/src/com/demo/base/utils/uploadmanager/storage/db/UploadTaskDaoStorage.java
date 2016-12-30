package com.lingyang.base.utils.uploadmanager.storage.db;

import android.content.Context;

import com.lingyang.base.utils.uploadmanager.UploadTools;
import com.lingyang.base.utils.uploadmanager.UploadConfiguration.UploadTable;
import com.lingyang.base.utils.uploadmanager.storage.IUploadTaskStorage;
import com.lingyang.base.utils.uploadmanager.storage.UploadBean;

import java.util.List;

public class UploadTaskDaoStorage implements IUploadTaskStorage  {
	
	private UploadTaskDao dao;
	
	public UploadTaskDaoStorage() {
	}
	
	@Override
	public void init(Context context, String tableName) {
		if (tableName == null) {
			tableName = UploadTable.UPLOAD_FILE_TABLE.name();
		}
		boolean isSupport = UploadTools.isSupportTable(tableName);
		if (! isSupport) {
			throw new RuntimeException("DownloadTaskDaoStorage 初始化错误，不支持的TableName:" + tableName);
		}
		dao = new UploadTaskDao(context, tableName);
	}
	
	public List<UploadBean> queryAllTask() {
		return dao.queryAll();
	}
	
	public List<UploadBean> queryAllTask(int state) {
	    return dao.queryAll(state);
	}
	
	public List<UploadBean> queryExcludeStateAllTask(int state) {
        return dao.queryExcludeStateAll(state);
    }
	
	public List<UploadBean> queryAllTask(String owner) {
		return dao.queryAll(owner);
	}
	
	public UploadBean queryTask(String downloadId) {
		return dao.queryById(downloadId);
	}
	
	public boolean updateTask(UploadBean UploadBean) {
		return dao.update(UploadBean);
	}
	
	public boolean addTask(UploadBean UploadBean) {
		long ret = dao.save(UploadBean);
		if (ret == -1) {
			return false;
		}
		return true;
	}
	
	public boolean addTasks(List<UploadBean> list) {
		return dao.save(list);
	}
	
	public boolean deleteTask(String downloadID) {
		return dao.delete(downloadID);
	}
	
	public boolean deleteAllTask() {
		return dao.deleteAll();
	}
	
}

