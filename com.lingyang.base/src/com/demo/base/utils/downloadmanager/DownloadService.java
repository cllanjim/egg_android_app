package com.lingyang.base.utils.downloadmanager;

import java.util.List;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.lingyang.base.utils.Log;
import com.lingyang.base.utils.NetworkStatus;
import com.lingyang.base.utils.downloadmanager.excutor.IDownloadListener;
import com.lingyang.base.utils.downloadmanager.storage.IDownloadTaskStorage;

/**
 * [下载Service]<br/>
 * 
 */
public class DownloadService extends Service implements IDownloadService{

	private final static String TAG = "DownloadService";
	
	private DownloaderBridge mDownloadService;
	
	@Override
	public void onCreate() {
		super.onCreate();
		mDownloadService = DownloaderBridge.getInstance(this, KeyConstants.DOWNLOADER_INSTANCE_ID);
		Log.v(TAG, "onCreate");
	}

	@Override
	public IBinder onBind(Intent intent) {
		DownloadBinder binder = new DownloadBinder();
		return binder;
	}

	public class DownloadBinder extends Binder {
		public IDownloadService getService(){
			return DownloadService.this;
		}
	}

	@Override
	public int getServiceState() {
		return 0;
	}
	
	@Override
	public boolean addServiceStateListener(IServiceStateListener listener) {
		return false;
	}

	@Override
	public boolean removeServiceStateListener(IServiceStateListener listener) {
		return false;
	}

	@Override
	public int createTask(String downloadId, int flag, DownloadTaskParam param,
			boolean autoRun) {
		
		return mDownloadService.createTask(downloadId, flag, param, autoRun);
	}

	@Override
	public DownloadTaskInfo queryTask(String downloadId) {
		
		return mDownloadService.queryTask(downloadId);
	}

	@Override
	public List<DownloadTaskInfo> queryAllTask(String owner) {
		
		return mDownloadService.queryAllTask(owner);
	}

	@Override
	public List<DownloadTaskInfo> getPending() {
		
		return mDownloadService.getPending();
	}

	@Override
	public List<DownloadTaskInfo> getDownloading() {
		
		return mDownloadService.getDownloading();
	}

	@Override
	public int getPendingTaskCount() {
		
		return mDownloadService.getPendingTaskCount();
	}

	@Override
	public int getRunningTaskCount() {
		
		return mDownloadService.getRunningTaskCount();
	}

	@Override
	public int runTask(String url, boolean needJumpQueue) {
		
		return mDownloadService.runTask(url, needJumpQueue);
	}

	@Override
	public int pauseTask(String url) {
		
		return mDownloadService.pauseTask(url);
	}

	@Override
	public void pauseAll() {
		mDownloadService.pauseAll();
	}

	@Override
	public int pauseTaskById(String id) {
		
		return mDownloadService.pauseTaskById(id);
	}

	@Override
	public int runTaskById(String id, boolean needJumpQueue) {
		
		return mDownloadService.runTaskById(id, needJumpQueue);
	}

	@Override
	public boolean deleteTaskById(String downloadId, boolean deleteFile) {
		
		return mDownloadService.deleteTaskById(downloadId, deleteFile);
	}

	@Override
	public boolean deleteTask(String url, boolean deleteFile) {
		
		return mDownloadService.deleteTask(url, deleteFile);
	}

	@Override
	public boolean deleteAllTask(boolean deleteFile) {
		
		return mDownloadService.deleteAllTask(deleteFile);
	}

	@Override
	public boolean setMaxActiveTaskCount(int count) {
		
		return mDownloadService.setMaxActiveTaskCount(count);
	}

	@Override
	public boolean setDownloadThreadCount(int count) {
		
		return mDownloadService.setDownloadThreadCount(count);
	}

	@Override
	public boolean setRateLimit(int speed) {
		
		return mDownloadService.setRateLimit(speed);
	}

	@Override
	public boolean hasTasks() {
		
		return mDownloadService.hasTasks();
	}

	@Override
	public void release(boolean stopTask) {
		mDownloadService.release(stopTask);
	}

	@Override
	public boolean addDownloadListListener(IDownloadListListener listener) {
		
		return mDownloadService.addDownloadListListener(listener);
	}

	@Override
	public boolean removeDownloadListListener(IDownloadListListener listener) {
		
		return mDownloadService.removeDownloadListListener(listener);
	}

	@Override
	public boolean addDownloadListener(IDownloadListener listener) {
		
		return mDownloadService.addDownloadListener(listener);
	}

	@Override
	public boolean removeDownloadListener(IDownloadListener listener) {
		
		return mDownloadService.removeDownloadListener(listener);
	}

	@Override
	public void setUseStorageTask(boolean isUse) {
		mDownloadService.setUseStorageTask(isUse);
	}

	@Override
	public void setDownloadNotifier(IDownloadNotifier notifier) {
		mDownloadService.setDownloadNotifier(notifier);
	}

	@Override
	public <T extends IDownloadNotifier> void setNotifationClass(Class<T> clz) {
		mDownloadService.setNotifationClass(clz);
	}

	@Override
	public void setUpdateDownloadNotify(boolean isUpdateNotify) {
		mDownloadService.setUpdateDownloadNotify(isUpdateNotify);
	}

	@Override
	public void showDownloadNotify() {
		mDownloadService.showDownloadNotify();
	}

	@Override
	public void dismissDownloadNotify() {
		mDownloadService.dismissDownloadNotify();
	}

	@Override
	public void init() {
		mDownloadService.init();
	}

	@Override
	public void setNotAcceptTypeList(List<String> list) {
		if (mDownloadService == null) {
			return;
		}
		mDownloadService.setNotAcceptTypeList(list);
	}

	@Override
	public void setDownloadTaskStorage(int type) {
		if (mDownloadService == null) {
			return;
		}
		mDownloadService.setDownloadTaskStorage(type);
	}

	@Override
	public void setDownloadTaskStorage(IDownloadTaskStorage taskStorage) {
		if (mDownloadService == null) {
			return;
		}
		mDownloadService.setDownloadTaskStorage(taskStorage);
	}

	@Override
	public void setStorageName(String storageName) {
		if (mDownloadService == null) {
			return;
		}
		mDownloadService.setStorageName(storageName);
	}

	@Override
	public int runAllTask() {
		if (mDownloadService == null) {
			return -1;
		}
		return mDownloadService.runAllTask();
	}

	@Override
	public void recordAllTaskState() {
		if (mDownloadService == null) {
			return;
		}
		mDownloadService.recordAllTaskState();
	}

	@Override
	public void resumeAllTaskState() {
		if (mDownloadService == null) {
			return;
		}
		mDownloadService.resumeAllTaskState();
	}

	@Override
	public void runAllStateTask(int state) {
		if (mDownloadService == null) {
			return;
		}
		mDownloadService.runAllStateTask(state);
	}

	@Override
	public List<DownloadTaskInfo> queryTaskByType(int type) {
		if (mDownloadService == null) {
			return null;
		}
		return mDownloadService.queryTaskByType(type);
	}

	@Override
	public int runTaskByType(int type, boolean needJumpQueue) {
		if (mDownloadService == null) {
			return -1;
		}
		return mDownloadService.runTaskByType(type, needJumpQueue);
	}

	@Override
	public int pauseTaskByType(int type) {
		if (mDownloadService == null) {
			return -1;
		}
		return mDownloadService.pauseTaskByType(type);
	}

	@Override
	public boolean deleteTaskByType(int type, boolean deleteFile) {
		if (mDownloadService == null) {
			return false;
		}
		return mDownloadService.deleteTaskByType(type, deleteFile);
	}
	
	@Override
	public int insertRunTaskById(String downloadId) {
		if (mDownloadService == null) {
			return -1;
		}
		return mDownloadService.insertRunTaskById(downloadId);
	}

	@Override
	public List<DownloadTaskInfo> queryAllComplete() {
		if (mDownloadService == null) {
			return null;
		}
		return mDownloadService.queryAllComplete();
	}

	@Override
	public int getAllTaskCount() {
		if (mDownloadService == null) {
			return -1;
		}
		return mDownloadService.getAllTaskCount();
	}
	
	@Override
	public int getMaxActiveTaskCount() {
		if (mDownloadService == null) {
			return -1;
		}
		return mDownloadService.getMaxActiveTaskCount();
	}
	
	@Override
	public void pauseAllAndRecordTask() {
		if (mDownloadService == null) {
			return;
		}
		mDownloadService.pauseAllAndRecordTask();
	}

	@Override
	public void setAllowDownloadState(NetworkStatus[] status) {
		if (mDownloadService == null) {
			return;
		}
		mDownloadService.setAllowDownloadState(status);
	}

	@Override
	public void setRetryIntervals(long[] intervals) {
		if (mDownloadService == null) {
			return;
		}
		mDownloadService.setRetryIntervals(intervals);
	}
}

