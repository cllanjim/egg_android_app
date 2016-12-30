package com.lingyang.base.utils;

import java.util.List;
import java.util.Map;

import android.content.Context;

import com.lingyang.base.utils.downloadmanager.DownloadServiceFactory;
import com.lingyang.base.utils.downloadmanager.DownloadTaskInfo;
import com.lingyang.base.utils.downloadmanager.DownloadTaskParam;
import com.lingyang.base.utils.downloadmanager.IDownloadListListener;
import com.lingyang.base.utils.downloadmanager.IDownloadService;
import com.lingyang.base.utils.downloadmanager.KeyConstants;
import com.lingyang.base.utils.downloadmanager.DownloadConfiguration.DownloadTable;
import com.lingyang.base.utils.downloadmanager.excutor.IDownloadListener;
import com.lingyang.base.utils.downloadmanager.storage.DownloadTaskStorageFractory;
import com.lingyang.base.utils.executor.ThreadPoolManager;

/**
 * [下载管理器]
 */
public class FileDownloadManager {

	private final static String TAG = FileDownloadManager.class.getSimpleName();

	private IDownloadService mDownladManager;
	
	private String mDownloadPath;
	
	private OnDownloadUpdateListener mDownloadUpdateListener;
	
	private static FileDownloadManager sInstance;

	public static final int RUN_TYPE_NORMAL = 0;
	public static final int RUN_TYPE_INSERT_QUEUE = 1;
	public static final int RUN_TYPE_INSERT_QUEUE_AND_RUN = 2;
	
	private final long DOWNLOAD_RETRY_INTERVALS[] = new long[] {
		300, 700, 1000	
	};
	
	private FileDownloadManager(Context context, String instanceName) {
		ThreadPoolManager.initThreadPoolManager(context);
		mDownladManager = DownloadServiceFactory.getDownloadService(context,
				DownloadServiceFactory.TYPE_NORMAL, instanceName);
		mDownladManager.addDownloadListListener(mDownloadListListener);
		mDownladManager.setRetryIntervals(DOWNLOAD_RETRY_INTERVALS);
	}
	
	public static FileDownloadManager getInstance(Context context) {
		if (sInstance == null) {
			sInstance = new FileDownloadManager(context, TAG);
		}
		return sInstance;
	}
	
	/**
	 * [初始化]
	 * 耗时操作
	 * @param savePath 下载文件的保存位置
	 */
	public void init(String savePath) {
		mDownloadPath = savePath;
		
		IDownloadService downloadService = getDownloadService();
		downloadService.setMaxActiveTaskCount(1);
		downloadService.setStorageName(DownloadTable.DOWNLOAD_FILE_TABLE.name());
		downloadService.setDownloadTaskStorage(DownloadTaskStorageFractory.STORAGE_TYPE_DB_FILE_MANAGER);
		downloadService.setUseStorageTask(true);
		downloadService.init();
	}

	/**
	 * [设置下载数据是否允许能在Wifi/非Wifi环境下的下载]<BR>
	 * 
	 * @param b
	 */
	public void setAllowDownloadNoWifi(boolean b) {
		NetworkStatus status[] = null;
		if (b) {
			status = new NetworkStatus[] {
					NetworkStatus.NETWORK_2G,
					NetworkStatus.NETWORK_3G,
					NetworkStatus.NETWORK_WIFI
				};
		} else {
			status = new NetworkStatus[] {
					NetworkStatus.NETWORK_WIFI,
			};
		}
		IDownloadService downloadService = getDownloadService();
		downloadService.setAllowDownloadState(status);
	}
	
	/**
	 * [获得下载操作源]<BR>
	 * 
	 * @return
	 */
	public IDownloadService getDownloadService() {
		return mDownladManager;
	}

	public int downloadFile(String url, String name, int insertType) {
		return downloadFile(url, null, name, insertType);
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
	public int downloadFile(String url, Map<String, String> httpHeader, 
			String name, int insertType) {
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
				return runTask(url, insertType);
			}
		}
		DownloadTaskParam param = new DownloadTaskParam();
		param.fileName = name;
		param.saveFolder = cache;
		param.uri = url;
		param.httpHead = httpHeader;
		param.retryCount = DOWNLOAD_RETRY_INTERVALS.length;
		int result = mDownladManager.createTask(url, 0, param, false);
		Log.d(TAG, "downloadFiles createTask result: " + result);
		
		if (result == KeyConstants.CREATE_TASK_SUC) {
			runTask(url, insertType);
		}
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
	public int runTask(String url, int insertType) {
		if (url == null) {
			return -1;
		}
		if (insertType == RUN_TYPE_INSERT_QUEUE) {
			mDownladManager.runTask(url, true);
		} else if (insertType == RUN_TYPE_INSERT_QUEUE_AND_RUN) {
			mDownladManager.insertRunTaskById(url);
		} else {
			mDownladManager.runTask(url, false);
		}
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
		sInstance = null;
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
	
	public void pauseAllAndRecordTask() {
		mDownladManager.pauseAllAndRecordTask();
	}
	
	public void resumeAllRecordTask() {
		mDownladManager.resumeAllTaskState();
	}
	
	private List<DownloadTaskInfo> mListDownloadings;
	
	IDownloadListListener mDownloadListListener = new IDownloadListListener() {

		@Override
		public void onDownloadListStateChanged() {
			List<DownloadTaskInfo> downloadings = mDownladManager.getDownloading();
			if (downloadings == null || downloadings.size() == 0) {
				return;
			}
			for(DownloadTaskInfo task : downloadings) {
				updateDownloadSpeed(task);
			}
			mListDownloadings = downloadings;
			if (mDownloadUpdateListener != null) {
				mDownloadUpdateListener.onDownloadUpdate(downloadings);
			}
		}
	};
	
	private void updateDownloadSpeed(DownloadTaskInfo task) {
		task.downloadrate = 0;
		if (mListDownloadings != null) {
			for(DownloadTaskInfo lastTask : mListDownloadings) {
				if (lastTask.downloadId != task.downloadId) {
					continue;
				}
				task.downloadrate = task.downloadLen - lastTask.downloadLen;
			}
		}
	}
	
	public void setOnDownloadUpdateListener(OnDownloadUpdateListener l) {
		mDownloadUpdateListener = l;
	}
	
	public interface OnDownloadUpdateListener {
		void onDownloadUpdate(List<DownloadTaskInfo> downloadings);
	}
}
