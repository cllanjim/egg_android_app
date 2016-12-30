package com.lingyang.base.utils.downloadmanager;

import java.util.List;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.lingyang.base.utils.Log;
import com.lingyang.base.utils.NetworkStatus;
import com.lingyang.base.utils.downloadmanager.DownloadService.DownloadBinder;
import com.lingyang.base.utils.downloadmanager.excutor.IDownloadListener;
import com.lingyang.base.utils.downloadmanager.storage.IDownloadTaskStorage;

/**
 * [下载管理器的Service控制方式]<br/>
 */
public class DownloadServiceController implements IDownloadService{
	
	private static final String TAG = "DownloadServiceController";

	private Context mContext;
	
	private IDownloadService mDownloadService;
	
	private Object mLock = new Object();
	
	public DownloadServiceController(Context context) {
		this.mContext = context;
		initService();
	}
	
	private void initService() {
		if (mDownloadService != null) {
			return;
		}
		if (mServiceConnection == null) {
			mServiceConnection = new MyServiceConnection();
		}
		Intent intent = new Intent();
		intent.setClass(mContext, DownloadService.class);
		boolean isBind = mContext.getApplicationContext().bindService(
				intent, mServiceConnection,
				Context.BIND_AUTO_CREATE);
		
		if (! isBind) {
			Log.e(TAG, "init 初始化service失败");
			return;
		}
		
		try {
			synchronized (mLock) {
				//等待Service绑定
				if (mDownloadService == null)
					mLock.wait();
			}
		} catch (Exception e) {}
	}
	
	@Override
	public void init() {
		if (mDownloadService == null) {
			return;
		}
		mDownloadService.init();
	}

	private ServiceConnection mServiceConnection;
	private class MyServiceConnection implements ServiceConnection {

		public void onServiceConnected(ComponentName className, IBinder service) {
			synchronized (mLock) {
				DownloadBinder binder = (DownloadBinder)service;
				mDownloadService = binder.getService();
//				mQvodPay = QvodCooperateInterface.Stub.asInterface(service);
				mLock.notify();
			}
		}

		public void onServiceDisconnected(ComponentName className) {
			mDownloadService = null;
		}
	};
		
	@Override
	public int createTask(String downloadId, int flag, DownloadTaskParam param,
			boolean autoRun) {
		if (mDownloadService == null) {
			return -1;
		}
		return mDownloadService.createTask(downloadId, flag, param, autoRun);
	}

	@Override
	public DownloadTaskInfo queryTask(String downloadId) {
		if (mDownloadService == null) {
			return null;
		}
		return mDownloadService.queryTask(downloadId);
	}

	@Override
	public List<DownloadTaskInfo> queryAllTask(String owner) {
		if (mDownloadService == null) {
			return null;
		}
		return mDownloadService.queryAllTask(owner);
	}

	@Override
	public List<DownloadTaskInfo> getPending() {
		if (mDownloadService == null) {
			return null;
		}
		return mDownloadService.getPending();
	}

	@Override
	public List<DownloadTaskInfo> getDownloading() {
		if (mDownloadService == null) {
			return null;
		}
		return mDownloadService.getDownloading();
	}

	@Override
	public int getPendingTaskCount() {
		if (mDownloadService == null) {
			return -1;
		}
		return mDownloadService.getPendingTaskCount();
	}

	@Override
	public int getRunningTaskCount() {
		if (mDownloadService == null) {
			return -1;
		}
		return mDownloadService.getRunningTaskCount();
	}

	@Override
	public int runTask(String url, boolean needJumpQueue) {
		if (mDownloadService == null) {
			return -1;
		}
		return mDownloadService.runTask(url, needJumpQueue);
	}

	@Override
	public int pauseTask(String url) {
		if (mDownloadService == null) {
			return -1;
		}
		return mDownloadService.pauseTask(url);
	}

	@Override
	public void pauseAll() {
		if (mDownloadService == null) {
			return;
		}
		mDownloadService.pauseAll();
	}

	@Override
	public int pauseTaskById(String downloadId) {
		if (mDownloadService == null) {
			return -1;
		}
		return mDownloadService.pauseTaskById(downloadId);
	}

	@Override
	public int runTaskById(String downloadId, boolean needJumpQueue) {
		if (mDownloadService == null) {
			return -1;
		}
		return mDownloadService.runTaskById(downloadId, needJumpQueue);
	}

	@Override
	public boolean deleteTaskById(String downloadId, boolean deleteFile) {
		if (mDownloadService == null) {
			return false;
		}
		return mDownloadService.deleteTaskById(downloadId, deleteFile);
	}

	@Override
	public boolean deleteTask(String url, boolean deleteFile) {
		if (mDownloadService == null) {
			return false;
		}
		return mDownloadService.deleteTask(url, deleteFile);
	}

	@Override
	public boolean deleteAllTask(boolean deleteFile) {
		if (mDownloadService == null) {
			return false;
		}
		return mDownloadService.deleteAllTask(deleteFile);
	}

	@Override
	public boolean setMaxActiveTaskCount(int count) {
		if (mDownloadService == null) {
			return false;
		}
		return mDownloadService.setMaxActiveTaskCount(count);
	}

	@Override
	public boolean setDownloadThreadCount(int count) {
		if (mDownloadService == null) {
			return false;
		}
		return mDownloadService.setDownloadThreadCount(count);
	}

	@Override
	public boolean setRateLimit(int speed) {
		if (mDownloadService == null) {
			return false;
		}
		return mDownloadService.setRateLimit(speed);
	}

	@Override
	public boolean hasTasks() {
		if (mDownloadService == null) {
			return false;
		}
		return mDownloadService.hasTasks();
	}

	@Override
	public void release(boolean stopTask) {
		if (mDownloadService == null) {
			return;
		}
		mDownloadService.release(stopTask);
	}

	@Override
	public boolean addDownloadListListener(IDownloadListListener listener) {
		if (mDownloadService == null) {
			return false;
		}
		return mDownloadService.addDownloadListListener(listener);
	}

	@Override
	public boolean removeDownloadListListener(IDownloadListListener listener) {
		if (mDownloadService == null) {
			return false;
		}
		return mDownloadService.removeDownloadListListener(listener);
	}

	@Override
	public boolean addDownloadListener(IDownloadListener listener) {
		if (mDownloadService == null) {
			return false;
		}
		return mDownloadService.addDownloadListener(listener);
	}

	@Override
	public boolean removeDownloadListener(IDownloadListener listener) {
		if (mDownloadService == null) {
			return false;
		}
		return mDownloadService.removeDownloadListener(listener);
	}

	@Override
	public void setUseStorageTask(boolean isUse) {
		if (mDownloadService == null) {
			return;
		}
		mDownloadService.setUseStorageTask(isUse);
	}

	@Override
	public void setDownloadNotifier(IDownloadNotifier notifier) {
		if (mDownloadService == null) {
			return;
		}
		mDownloadService.setDownloadNotifier(notifier);
	}

	@Override
	public <T extends IDownloadNotifier> void setNotifationClass(Class<T> clz) {
		if (mDownloadService == null) {
			return;
		}
		mDownloadService.setNotifationClass(clz);
	}

	@Override
	public int getServiceState() {
		if (mDownloadService == null) {
			return -1;
		}
		return mDownloadService.getServiceState();
	}

	@Override
	public boolean addServiceStateListener(IServiceStateListener listener) {
		if (mDownloadService == null) {
			return false;
		}
		return mDownloadService.addServiceStateListener(listener);
	}

	@Override
	public boolean removeServiceStateListener(IServiceStateListener listener) {
		if (mDownloadService == null) {
			return false;
		}
		return mDownloadService.removeServiceStateListener(listener);
	}

	@Override
	public void setUpdateDownloadNotify(boolean isUpdateNotify) {
		if (mDownloadService == null) {
			return;
		}
		mDownloadService.setUpdateDownloadNotify(isUpdateNotify);
	}

	@Override
	public void showDownloadNotify() {
		if (mDownloadService == null) {
			return;
		}
		mDownloadService.showDownloadNotify();
	}

	@Override
	public void dismissDownloadNotify() {
		if (mDownloadService == null) {
			return;
		}
		mDownloadService.dismissDownloadNotify();
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

