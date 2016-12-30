package com.lingyang.base.utils;

import java.util.List;

import android.content.Context;

import com.lingyang.base.utils.downloadmanager.DownloadConfiguration;
import com.lingyang.base.utils.downloadmanager.DownloadServiceFactory;
import com.lingyang.base.utils.downloadmanager.DownloadTaskInfo;
import com.lingyang.base.utils.downloadmanager.DownloadTaskParam;
import com.lingyang.base.utils.downloadmanager.IDownloadService;
import com.lingyang.base.utils.downloadmanager.KeyConstants;
import com.lingyang.base.utils.downloadmanager.DownloadConfiguration.DownloadTable;
import com.lingyang.base.utils.downloadmanager.excutor.IDownloadListener;
import com.lingyang.base.utils.downloadmanager.storage.DownloadTaskStorageFractory;
import com.lingyang.base.utils.executor.ThreadPoolManager;

/**
 * [下载管理器]<BR>
 * 提供一个可以简便操作DownloadManager的外观类
 * 可以自己根据DownloadServiceFactory来生成下载管理器，以便进行更加复杂和操作
 * 但是切记在下载管理器类中做业务操作
 */
public class FacadeDownloadManager {

	private final static String TAG = "FacadeDownloadManager";

	private IDownloadService mDownladManager;
	
	private Context mContext;
	
	private String mDownloadPath;
	
	private boolean mDownloadNeedWifi;

	private FacadeDownloadManager(Context context, String instanceName) {
		mContext = context;
		ThreadPoolManager.initThreadPoolManager(context);
		mDownladManager = DownloadServiceFactory.getDownloadService(context,
				DownloadServiceFactory.TYPE_NORMAL, instanceName);
	}

	/**
	 * [设置下载数据是否指定只能在Wifi下才能下载]<BR>
	 * 
	 * @param b
	 */
	public void setDownloadNeedWifi(boolean b) {
		this.mDownloadNeedWifi = b;
	}
	
	public static class Builder {
		
		String taskInstanceName = TAG;
		
		/**
		 * 最大同时可下载数
		 */
		int maxActiveTaskCount = 1;
		
		/**
		 * 下载存储器的表名、文件名等
		 */
		DownloadTable downloadTable = DownloadConfiguration.DownloadTable.DOWNLOAD_FILE_TABLE;
		
		/**
		 * 是否存储下载任务
		 * 不存储下载任务则为无状态下载，无法进行断点续传、保存任务下载状态
		 */
		boolean useStorageTask = true;
		
		/**
		 * 下载数据是否指定只能在Wifi下才能下载
		 */
		boolean downloadNeedWifi = true;
		
		/**
		 * 文件下载路径
		 */
		String downloadPath;
		
		
		public Builder setMaxActiveTaskCount(int maxTask) {
			this.maxActiveTaskCount = maxTask;
			return this;
		}
		
		/**
		 * 设置存储表名，由于表名需要提前全局创建而不依赖与某一个DownloadManager，所以统一在
		 * @param storageName 存储表名
		 * @return
		 */
		public Builder setDownloadTable(DownloadTable downloadTable) {
			this.downloadTable = downloadTable;
			return this;
		}
		
		public Builder setUseStorageTask(boolean b) {
			this.useStorageTask = b;
			return this;
		}
		
		public Builder setTaskInstanceName(String name) {
			this.taskInstanceName = name;
			return this;
		}

		public Builder setDownloadPath(String path) {
			this.downloadPath = path;
			return this;
		}
		
		public Builder setDownloadNeedWifi(boolean b) {
			this.downloadNeedWifi = b;
			return this;
		}
		
		/**
		 * [创建一个DownloadManager]<BR>
		 * 必须指定属性 downloadPath
		 * 
		 * @param context
		 * @return
		 */
		public FacadeDownloadManager build(Context context) {
			if (downloadPath == null) {
				throw new RuntimeException("DownloadManager must set downloadPath");
			}
			FacadeDownloadManager manager = new FacadeDownloadManager(context, taskInstanceName);
			manager.mDownloadPath = downloadPath;
			manager.mDownloadNeedWifi = downloadNeedWifi;
			
			IDownloadService downloadService = manager.getDownloadService();
			downloadService.setMaxActiveTaskCount(maxActiveTaskCount);
			downloadService.setStorageName(downloadTable.name());
			downloadService.setDownloadTaskStorage(DownloadTaskStorageFractory.STORAGE_TYPE_DB_FILE_MANAGER);
			downloadService.setUseStorageTask(useStorageTask);
			downloadService.init();
			return manager;
		}
	}
	
	/**
	 * [获得下载操作源]<BR>
	 * 
	 * @return
	 */
	public IDownloadService getDownloadService() {
		return mDownladManager;
	}

	/**
	 * [下载文件]<BR>
	 * 创建并开始下载任务
	 * 或将已经创建的任务恢复下载
	 * 
	 * @param url
	 * @param name
	 * @return
	 */
	public int downloadFile(String url, String name) {
		if (url == null) {
			return KeyConstants.OPERATE_FAIL;
		}

		String cache = mDownloadPath;
		if (cache == null) {
			return KeyConstants.OPERATE_NO_SPACE;
		}
		DownloadTaskInfo task = mDownladManager.queryTask(url);
		if (task != null) {
			if (!cache.equals(task.folder)) {
				// 任务的下载地址与当前获取缓存目录不一致，需要先删除任务
				mDownladManager.deleteTask(url, true);
				Log.d(TAG, "downloadFile 任务的下载地址与当前获取缓存目录不一致");
			} else {
				return runTask(url);
			}
		}
		boolean isAutoRun = true;
		if (mDownloadNeedWifi) {
			isAutoRun = (NetworkStatus.getNetworkStatus(mContext) == NetworkStatus.NETWORK_WIFI);
		}
		DownloadTaskParam param = new DownloadTaskParam();
		param.fileName = name;
		param.saveFolder = cache;
		param.uri = url;
		int result = mDownladManager.createTask(url, 0, param, isAutoRun);
		Log.d(TAG, "downloadFiles createTask result: " + result);
		return result;
	}
	
	/**
	 * [启动下载]<BR>
	 * 
	 * 在非Wifi网络环境下不进行下载
	 * 网络任务进行队列下载，将当前指定的下载任务Push到队列顶部，优先执行
	 * 注：执行该命名不会导致当前正在下载的任务停止，执行时间为当前任务执行结束后的下一个任务
	 * @param url
	 * @return
	 */
	public int runTask(String url) {
		if (url == null) {
			return -1;
		}
		if (mDownloadNeedWifi) {
			if (NetworkStatus.getNetworkStatus(mContext) != NetworkStatus.NETWORK_WIFI) {
				Log.w(TAG, "runTask 当前网络不为Wifi，不允许下载");
				return -1;
			}
		}
		mDownladManager.runTaskById(url, true);
		int count = mDownladManager.getRunningTaskCount();
		if (count == 0) {
			return KeyConstants.OPERATE_FAIL;
		}
		return KeyConstants.OPERATE_SUC;
	}
	
	/**
	 * [释放资源]<BR>
	 * 释放内存资源及停止下载
	 */
	public void release() {
		mDownladManager.release(true);
	}
	
	/**
	 * [获取所有下载任务总数]<BR>
	 * 
	 * @return
	 */
	public int getAllTaskCount() {
		return mDownladManager.getAllTaskCount();
	}

	/**
	 * [添加下载监听]<BR>
	 * 
	 * @param l
	 */
	public void addDownloadListener(IDownloadListener l) {
		mDownladManager.addDownloadListener(l);
	}

	/**
	 * [移除下载监听]<BR>
	 * 
	 * @param l
	 */
	public void removeDownloadListener(IDownloadListener l) {
		mDownladManager.removeDownloadListener(l);
	}

	/**
	 * [查询网络任务]<BR>
	 * 
	 * @param urlId
	 * @return
	 */
	public DownloadTaskInfo query(String urlId) {
		return mDownladManager.queryTask(urlId);
	}
	
	/**
	 * [查询所有下载任务]<BR>
	 * 
	 * @return
	 */
	public List<DownloadTaskInfo> queryAllTask() {
		return mDownladManager.queryAllTask(null);
	}

	/**
	 * [暂停下载任务]<BR>
	 * 
	 * @param url
	 * @return
	 */
	public int pauseTask(String url) {
		if (url == null) {
			return -1;
		}
		return mDownladManager.pauseTask(url);
	}

	/**
	 * [删除下载任务]<BR>
	 * 
	 * @param url
	 * @param deleteFile 是否删除文件
	 */
	public void deleteTask(String url, boolean deleteFile) {
		mDownladManager.deleteTaskById(url, deleteFile);
		Log.d(TAG, "deleteTask url: " + url);
	}

	/**
	 * [恢复所有下载任务]<BR>
	 * 
	 * 只能在Wifi网络环境下才有作用
	 */
	public void runAllTask() {
		if (mDownloadNeedWifi) {
			if (NetworkStatus.getNetworkStatus(mContext) != NetworkStatus.NETWORK_WIFI) {
				Log.w(TAG, "runAllTask 当前网络不为Wifi，不允许下载");
				return;
			}
		}
		mDownladManager.runAllTask();
		Log.d(TAG, "runAllTask AllTask:" + mDownladManager.getAllTaskCount());
	}

	/**
	 * [暂停所有任务]<BR>
	 */
	public void pauseAll() {
		mDownladManager.pauseAll();
		Log.d(TAG, "pauseAllByNetwork");
	}

	/**
	 * [删除所有的网络任务]<BR>
	 * 删除任务记录以及本地文件
	 */
	public void deleteAllTask() {
		mDownladManager.deleteAllTask(true);
		FileUtil.deleteFolder(mDownloadPath);
			Log.d(TAG, "deleteAllTask");
	}
}
