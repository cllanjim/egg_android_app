package com.lingyang.base.utils.downloadmanager;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReentrantLock;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.lingyang.base.utils.Log;
import com.lingyang.base.utils.NetworkStatus;
import com.lingyang.base.utils.downloadmanager.excutor.DownloadExecutor;
import com.lingyang.base.utils.downloadmanager.excutor.DownloadExecutorManager;
import com.lingyang.base.utils.downloadmanager.excutor.DownloadExecutorTarget;
import com.lingyang.base.utils.downloadmanager.excutor.DownloadState;
import com.lingyang.base.utils.downloadmanager.excutor.IDownloadListener;
import com.lingyang.base.utils.downloadmanager.excutor.DownloadExecutorTarget.DownloadTaskState;
import com.lingyang.base.utils.downloadmanager.storage.DownloadBean;
import com.lingyang.base.utils.downloadmanager.storage.DownloadTaskStorageFractory;
import com.lingyang.base.utils.downloadmanager.storage.IDownloadTaskStorage;

/**
 * [下载管理器]<br/>
 */
public class DownloaderBridge implements IDownloadService{
	
	private final static String TAG = "DownloaderBridge";
	
	public final static int DEFAULT_CONNECT_TIME_OUT = KeyConstants.DEFAULT_CONNECT_TIME_OUT;
	public final static int DEFAULT_READ_TIME_OUT = KeyConstants.DEFAULT_READ_TIME_OUT;
	public final static int DEFAULT_RETRY_COUNT = 0;
	public final static int DEFAULT_STORAGE_TYPE = DownloadTaskStorageFractory.STORAGE_TYPE_NONE;
	public final static String DEFAULT_NOT_ACCEPT_CONTENT_TYPE = DownloadExecutor.CONTENT_TYPE_HTML;
	
	private int mConnectionTimeout = -1;
	private int mReadTimeout = -1; 
	private int mRetryCount = -1;
	
	private final static int AUTO_UPDATE_STORAGE = 5 * 1000;
	
	public List<String> mNotAcceptTypeList;
	
	private Context mContext;
	
	private String mDownloadInstanceId;
	
	private static Map<String, DownloaderBridge> mInstances = new HashMap<String, DownloaderBridge>();
	
	private final ReentrantLock mDownloadLock = new ReentrantLock();
	
	//flag 操作同步问题
	private Map<String, DownloadExecutorTarget> mDownloadFiles = new HashMap<String, DownloadExecutorTarget>();
	
	private DownloadExecutorManager mDownloader;
	
	private IDownloadTaskStorage mDownloadStorage;
	
	private ReentrantLock mListenerLock = new ReentrantLock();
	private List<IDownloadListListener> mDownloadListListeners;
	
	private IDownloadNotifier mDownloadNotifier;
	
	private boolean mIsAutoUpdateNotify = true;
	
	private Handler mWorkHandler;
	
	private boolean mIsUseStorage = true;
	
	private String mStorageName;
	
	private int mDownloadStorageType = DEFAULT_STORAGE_TYPE;
	
	private boolean mIsInit = false;
	
	//flag 需要每一分钟刷新一次下载任务的下载信息，保存已下载的长度
	
	private DownloaderBridge(Context context, String instanceId) {
		this.mContext = context;
		this.mDownloadInstanceId = instanceId;
		this.mDownloader = new DownloadExecutorManager(context);
		this.mDownloader.setRootDownloadListener(new DownloadListener());
		runWorker();
	}
	
	/**
	 * @param context
	 * @param instanceId 可以为任意值，用于标示示例对象
	 * @return
	 */
	public static DownloaderBridge getInstance(Context context, String instanceId) {
		synchronized (mInstances) {
			DownloaderBridge downloaderBridge = mInstances.get(instanceId);
			if (downloaderBridge == null) { 
				downloaderBridge = new DownloaderBridge(context, instanceId);
				mInstances.put(instanceId, downloaderBridge);
			}
			return downloaderBridge;
		}
	}
	
	@Override
	public void setDownloadTaskStorage(int type) {
		this.mDownloadStorageType = type;
	}
	
	@Override
	public void setDownloadTaskStorage(IDownloadTaskStorage taskStorage) {
		this.mDownloadStorage = taskStorage;
	}
	
	@Override
	public void setNotAcceptTypeList(List<String> list) {
		this.mNotAcceptTypeList = list;
	}
	
	/**
	 * [设置允许进行下载的网络状态]
	 * @param status
	 */
	public void setAllowDownloadState(NetworkStatus[] status) {
		this.mDownloader.setAllowDownloadState(status);
	}
	
	@Override
	public synchronized void init() {
		if (mIsInit) {
			Log.w(TAG, "init 已经被初始化过，请勿重复初始化");
			return;
		}
		mIsInit = true;
		if (mConnectionTimeout == -1) {
			mConnectionTimeout = DEFAULT_CONNECT_TIME_OUT;
		}
		
		if (mReadTimeout == -1) {
			mReadTimeout = DEFAULT_READ_TIME_OUT;
		}
		
		if (mRetryCount == -1) {
			mRetryCount = DEFAULT_RETRY_COUNT;
		}
		
		if (mNotAcceptTypeList == null) {
			mNotAcceptTypeList = new ArrayList<String>();
			mNotAcceptTypeList.add(DEFAULT_NOT_ACCEPT_CONTENT_TYPE);
		}
		
		if (! mIsUseStorage) {
			return;
		}
		if (mDownloadStorage == null) {
			mDownloadStorage = DownloadTaskStorageFractory.createDownloadStorage(mContext, mDownloadStorageType);
		}
		
		try {
			mDownloadStorage.init(mContext, mStorageName);
			
			if (mDownloadStorage != null) {
				List<DownloadBean> downloadBeans = mDownloadStorage.queryAllTask();
				if (downloadBeans != null && downloadBeans.size() > 0) {
					for(DownloadBean downloadBean : downloadBeans) {
						DownloadExecutorTarget target = parseDownloadExecutorTarget(downloadBean);
						mDownloadFiles.put(target.downloadId, target);
	//					Log.v(TAG, "init " + downloadBean.downloadState + " - name:" + downloadBean.fileName);
					}
				}
			}
		}catch (Exception e) {
			e.printStackTrace();
			mIsInit = false;
			Log.e(TAG, "init 初始化出错");
		}
	}
	
	private void runWorker() {
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				Looper.prepare();
				mWorkHandler = new WorkHandler();
				Looper.loop();
			}
		};
		Thread thread = new Thread(runnable);
		thread.start();
	}
	
	@Override
	public int createTask(String downloadId, int flag, DownloadTaskParam param, boolean autoRun) {
		//查询数据库中的任务，是否已存在相同任务
		long createTime = new Date().getTime();
		DownloadExecutorTarget target = null;
		mDownloadLock.lock(); 
		try {
			if(mDownloadFiles.containsKey(downloadId)) {
				//检查内存中的任务
				return KeyConstants.CREATE_TASK_ERROR_EXIST;
			}
			target = parseDownloadExecutorTarget(param, createTime, downloadId);
			mDownloadFiles.put(downloadId, target);
		} finally {
			mDownloadLock.unlock();
		}
		
		if (mIsUseStorage && mDownloadStorage != null) {
			//存储下载任务到本地
			DownloadBean downloadBean = parseDownloadBean(param, createTime, downloadId);
			boolean isSuc = mDownloadStorage.addTask(downloadBean);
			if (! isSuc) {
				Log.e(TAG, "createTask 失败：存储任务到本地");
			}
		}
		
		if (autoRun) {
			//开始进入下载队列准备下载
			mDownloader.runTask(target);
			notifyDownloadState(target, target.state.downloadState);
			checkAutoUpdateDownloading();
		}
		
		requestNotifyDownlaodListState();
		return KeyConstants.CREATE_TASK_SUC;
	}
	
	@Override
	public int runTask(String url, boolean needJumpQueue) {
		CompareCondition compareCondition = new CompareCondition<String>() {
			@Override
			public boolean isSameCondition(String url,
					DownloadExecutorTarget target) {
				if (url.equals(target.url)) {
					return true;
				}
				return false;
			}
		};
		return runTask(url, compareCondition, needJumpQueue);
	}
	
	@Override
	public int runTaskByType(int type, boolean needJumpQueue) {
		CompareCondition compareCondition = new CompareCondition<Integer>() {
			@Override
			public boolean isSameCondition(Integer type,
					DownloadExecutorTarget target) {
				if (type == target.fileType) {
					return true;
				}
				return false;
			}
		};
		return runTask(type, compareCondition, needJumpQueue);
	}
	
	@Override
	public int insertRunTaskById(String downloadId) {
		int result = runTaskById(downloadId, true);
		if (result != KeyConstants.OPERATE_SUC) {
			return result;
		}
		List<DownloadExecutorTarget> downloading = mDownloader.getDownloading();
		if (downloading != null && downloading.size() >= mDownloader.getMaxRunningSize()
				&& !DownloadUtil.containsDownloadExecutor(downloading, downloadId)) {
			DownloadExecutorTarget target = downloading.get(0);
			synchronized (target) {
				mDownloader.pauseTask(target);
				//进入队列末尾
				runTask(target.url, false);
			}
		}
		return KeyConstants.OPERATE_SUC;
	}
	
	private int runTask(Object value, CompareCondition compare, boolean needJumpQueue) {
		mDownloadLock.lock();
		try {
			boolean hasRunTask = false;
			Iterator<Entry<String, DownloadExecutorTarget>> it = mDownloadFiles.entrySet().iterator();
			while(it.hasNext()) {
				Entry<String, DownloadExecutorTarget> entry = it.next();
				DownloadExecutorTarget info = entry.getValue();
				if (! compare.isSameCondition(value, info)) {
					continue;
				}
				if (isTargetRun(info)) {
					continue;
				}
				if (isDownloadComplete(info)) {
					Log.v(TAG, "runTask 已下载完成: " + info.url);
					continue;
				}
				if (needJumpQueue) {
					mDownloader.runTaskPriority(info);
				} else {
					mDownloader.runTask(info);
				}
				updateTargetState(info.downloadId, info.state.downloadState);
				notifyDownloadState(info, info.state.downloadState);
				hasRunTask = true;
			}
			if (hasRunTask) {
				checkAutoUpdateDownloading();
			}
		}finally {
			mDownloadLock.unlock();
		}
		requestNotifyDownlaodListState();
		return 0;
	}
	
	@Override
	public int runAllTask() {
		Log.d(TAG, "runAllTask");
		mDownloadLock.lock();
		try {
			boolean hasRunTask = false;
			Iterator<Entry<String, DownloadExecutorTarget>> it = mDownloadFiles.entrySet().iterator();
			while(it.hasNext()) {
				Entry<String, DownloadExecutorTarget> entry = it.next();
				DownloadExecutorTarget info = entry.getValue();
				if (isTargetRun(info)) {
					continue;
				}
				if (isDownloadComplete(info)) {
					Log.v(TAG, "runAllTask 已下载完成: " + info.url);
					continue;
				}
				mDownloader.runTask(info);
				notifyDownloadState(info, info.state.downloadState);
				updateTargetState(info.downloadId, info.state.downloadState);
				hasRunTask = true;
			}
			if (hasRunTask) {
				checkAutoUpdateDownloading();
			}
		}finally {
			mDownloadLock.unlock();
		}
		requestNotifyDownlaodListState();
		return 0;
	}
	
	@Override
	public int runTaskById(String id, boolean needJumpQueue) {
		mDownloadLock.lock();
		try {
			DownloadExecutorTarget info = getDownloadExecutorTarget(id);
			if (info == null) {
				Log.w(TAG, "runTaskById 未找到指定的任务");
				return KeyConstants.OPERATE_NO_TASK;
			}
			if (isTargetRun(info)) {
				Log.e(TAG, "正在运行中 - " + info.url);
				return KeyConstants.OPERATE_RUNNING;
			}
			if (isDownloadComplete(info)) {
				Log.v(TAG, "runTask 已下载完成: " + info.url);
				return KeyConstants.OPERATE_SUC;
			}
			if (needJumpQueue) {
				mDownloader.runTaskPriority(info);
			} else {
				mDownloader.runTask(info);
			}
			notifyDownloadState(info, info.state.downloadState);
			updateTargetState(info.downloadId, info.state.downloadState);
		}finally {
			mDownloadLock.unlock();
		}
		checkAutoUpdateDownloading();
		requestNotifyDownlaodListState();

		return KeyConstants.OPERATE_SUC;
	}
	
	private boolean isTargetRun(DownloadExecutorTarget info) {
		DownloadExecutor executor = info.executor;
		if (executor != null && executor.isTaskRun()) {
			return true;
		}
		return false;	
	}
	
	@Override
	public int pauseTask(String url) {
		CompareCondition compareCondition = new CompareCondition<String>() {
			@Override
			public boolean isSameCondition(String url,
					DownloadExecutorTarget target) {
				if (url.equals(target.url)) {
					return true;
				}
				return false;
			}
		};
		return pauseTask(url, compareCondition);
	}
	
	@Override
	public int pauseTaskByType(int type) {
		CompareCondition compareCondition = new CompareCondition<Integer>() {
			@Override
			public boolean isSameCondition(Integer type,
					DownloadExecutorTarget target) {
				if (type == target.fileType) {
					return true;
				}
				return false;
			}
		};
		return pauseTask(type, compareCondition);
	}
	
	private int pauseTask(Object value, CompareCondition compare) {
		mDownloadLock.lock();
		try {
			Iterator<Entry<String, DownloadExecutorTarget>> it = mDownloadFiles.entrySet().iterator();
			while(it.hasNext()) {
				Entry<String, DownloadExecutorTarget> entry = it.next();
				DownloadExecutorTarget info = entry.getValue();
				if (! compare.isSameCondition(value, info)) {
					continue;
				}
				if (isDownloadComplete(info)) {
					Log.v(TAG, "pauseTask 已下载完成: " + info.url);
					continue;
				}
				mDownloader.pauseTask(info);
				notifyDownloadState(info, DownloadState.STATE_PAUSE);
				updateTargetState(info.downloadId, DownloadState.STATE_PAUSE);
			}
		}finally{
			mDownloadLock.unlock();
		}
		requestNotifyDownlaodListState();
		return 0;
	}
	
	@Override
	public void pauseAll() {
		pauseAll(true, true);
	}
	
	private void pauseAll(boolean updateState, boolean isNotify) {
		mDownloadLock.lock();
		Log.v(TAG, "pauseAll updateState:" + updateState);
		try {
			mDownloader.removeAllTask();
			Iterator<Entry<String, DownloadExecutorTarget>> it = mDownloadFiles.entrySet().iterator();
			while(it.hasNext()) {
				Entry<String, DownloadExecutorTarget> entry = it.next();
				DownloadExecutorTarget info = entry.getValue();
				if (isDownloadComplete(info)) {
					Log.v(TAG, "pauseTask 已下载完成: " + info.url);
					continue;
				}
				switch (info.state.downloadState) {
				case DownloadState.STATE_PENDING:
				case DownloadState.STATE_INTO_DOWNLOADING_QUEUE:
				case DownloadState.STATE_PREPARE:
				case DownloadState.STATE_START:
				case DownloadState.STATE_DOWNLOADING:
				case DownloadState.STATE_FAIL:
					break;
				default:
					continue;
				}
				mDownloader.pauseTask(info, false);
				mDownloader.refreshDownloadTarget(info);
				if (isNotify) {
					notifyDownloadState(info, DownloadState.STATE_PAUSE, updateState);
				}
				if (updateState) {
					updateTargetInfo(info, true);
				}
			}
		}finally{
			mDownloadLock.unlock();
		}
		if (isNotify) {
			requestNotifyDownlaodListState();
		}
	}
	
	@Override
	public void runAllStateTask(int state) {
		List<DownloadExecutorTarget> list = new ArrayList<DownloadExecutorTarget>();
		mDownloadLock.lock();
		try {
			Map<String, DownloadExecutorTarget> downloadFiles = mDownloadFiles;
			Iterator<Map.Entry<String,DownloadExecutorTarget>> it = downloadFiles.entrySet().iterator();
			while(it.hasNext()) {
				DownloadExecutorTarget target = it.next().getValue();
				if (isDownloadComplete(target)) {
					Log.v(TAG, "runTask 已下载完成: " + target.url);
					continue;
				}
				if (state == target.state.downloadState) {
					list.add(target);
				}
			}
		}finally{
			mDownloadLock.unlock();
		}
		for(DownloadExecutorTarget target : list) {
			runTaskById(target.downloadId, false);
		}
	}
	
	@Override
	public int pauseTaskById(String id) {
		DownloadExecutorTarget target = null;
		mDownloadLock.lock();
		try {
			target = mDownloadFiles.get(id);
			if (target == null) {
				Log.w(TAG, "pauseTaskById 未找到指定的任务");
				return KeyConstants.OPERATE_NO_TASK;
			}
			if (isDownloadComplete(target)) {
				Log.v(TAG, "pauseTaskById 已下载完成: " + target.url);
				return KeyConstants.OPERATE_SUC;
			}
			mDownloader.pauseTask(target);
			mDownloader.refreshDownloadTarget(target);
			notifyDownloadState(target, DownloadState.STATE_PAUSE);
			updateTargetInfo(target, true);
			requestNotifyDownlaodListState();
			Log.e(TAG, "pauseTask");
		}finally{
			mDownloadLock.unlock();
		}
		return KeyConstants.OPERATE_SUC;
	}
	
	@Override
	public boolean deleteTaskById(String downloadId, boolean deleteFile) {
		if (downloadId == null) {
			return false;
		}
		DownloadExecutorTarget info = null;
		mDownloadLock.lock();
		try {
			//删除内存中储存的任务
			info = mDownloadFiles.remove(downloadId);
			if (info == null) {
				return false;
			}
			
			//移除下载队列
			mDownloader.pauseTask(info);
			
			if(mIsUseStorage && mDownloadStorage != null) {
				//delete 数据存储
				mDownloadStorage.deleteTask(downloadId);
			}
			
			//flag 可能造成正在写入文件的时候删除文件情况，是否存在问题
			deleteTaskFile(info, deleteFile);
		}finally {
			mDownloadLock.unlock();
		}
		requestNotifyDownlaodListState();
		return true;
	}
	
	@Override
	public boolean deleteTask(String url, boolean deleteFile) {
		CompareCondition compareCondition = new CompareCondition<String>() {
			@Override
			public boolean isSameCondition(String url,
					DownloadExecutorTarget target) {
				if (url.equals(target.url)) {
					return true;
				}
				return false;
			}
		};
		
		return deleteTask(url, deleteFile, compareCondition);
	}
	
	@Override
	public boolean deleteTaskByType(int type, boolean deleteFile) {
		CompareCondition compareCondition = new CompareCondition<Integer>() {
			@Override
			public boolean isSameCondition(Integer type,
					DownloadExecutorTarget target) {
				if (target.fileType == type) {
					return true;
				}
				return false;
			}
		};
		
		return deleteTask(type, deleteFile, compareCondition);
	}
	
	public boolean deleteTask(Object value, boolean deleteFile, CompareCondition compareCondition) {
		List<DownloadExecutorTarget> deleteTargets = null;
		mDownloadLock.lock();
		try {
			Iterator<Entry<String, DownloadExecutorTarget>> it = mDownloadFiles.entrySet().iterator();
			while(it.hasNext()) {
				Entry<String, DownloadExecutorTarget> entry = it.next();
				DownloadExecutorTarget info = entry.getValue();
				if (! compareCondition.isSameCondition(value, info)) {
					continue;
				}
				it.remove();
				
				if (deleteTargets == null) {
					deleteTargets = new ArrayList<DownloadExecutorTarget>();
				}
				deleteTargets.add(info);
			}
		
			if (deleteTargets == null) {
				return false;
			}
			
			for(DownloadExecutorTarget target : deleteTargets) {
				//移除下载队列
				mDownloader.pauseTask(target);
				if (mIsUseStorage && mDownloadStorage != null) {
					mDownloadStorage.deleteTask(target.downloadId);
				}
				//flag 可能造成正在写入文件的时候删除文件情况，是否存在问题
				deleteTaskFile(target, deleteFile);
			}
		}finally {
			mDownloadLock.unlock();
		}
		requestNotifyDownlaodListState();
		return true;
	}
	
	@Override
	public boolean deleteAllTask(boolean deleteFile) {
		List<DownloadExecutorTarget> deleteTargets = new ArrayList<DownloadExecutorTarget>();
		mDownloadLock.lock();
		try {
			Iterator<Entry<String, DownloadExecutorTarget>> it = mDownloadFiles.entrySet().iterator();
			while(it.hasNext()) {
				Entry<String, DownloadExecutorTarget> entry = it.next();
				DownloadExecutorTarget info = entry.getValue();
				deleteTargets.add(info);
			}
			mDownloadFiles.clear();
		
			if (deleteTargets.size() == 0) {
				return false;
			}
	
			for(DownloadExecutorTarget target : deleteTargets) {
				mDownloader.pauseTask(target);
				if (mIsUseStorage && mDownloadStorage != null) {
					//delete 数据库
					mDownloadStorage.deleteTask(target.downloadId);
				}
				//flag 可能造成正在写入文件的时候删除文件情况，是否存在问题
				deleteTaskFile(target, deleteFile);
			}
		} finally {
			mDownloadLock.unlock();
		}
		requestNotifyDownlaodListState();
		return true;
	}
	
	@Override
	public DownloadTaskInfo queryTask(String downloadId) {
		DownloadExecutorTarget target = getDownloadExecutorTarget(downloadId);
		if (target == null) {
			return null;
		}
		DownloadTaskInfo taskInfo = parseDownloadTaskInfo(null, target);
		return taskInfo;
	}

	private DownloadExecutorTarget getDownloadExecutorTarget(String downloadId) {
		DownloadExecutorTarget target = null;
		mDownloadLock.lock();
		try {
			target = mDownloadFiles.get(downloadId);
		} finally {
			mDownloadLock.unlock();
		}
		return target;
	}
	
	@Override
	public List<DownloadTaskInfo> queryAllTask(String owner) {
		CompareCondition compareCondition = new CompareCondition<String>() {
			@Override
			public boolean isSameCondition(String owner,
					DownloadExecutorTarget target) {
				if (owner == null) {
					if (target.isPrivate) {
						return false;
					} else {
						return true;
					}
				}
				if (owner.equals(target.onwer)) {
					return true;
				}
				return false;
			}
		};
		return queryTask(owner, compareCondition);
	}
	
	@Override
	public List<DownloadTaskInfo> queryTaskByType(int type) {
		CompareCondition compareCondition = new CompareCondition<Integer>() {
			@Override
			public boolean isSameCondition(Integer type,
					DownloadExecutorTarget target) {
				if (target.fileType == type) {
					return true;
				}
				return false;
			}
		};
		return queryTask(type, compareCondition);
	}
	
	private List<DownloadTaskInfo> queryTask(Object value, CompareCondition compare) {
		List<DownloadTaskInfo> retTaskInfoList = new ArrayList<DownloadTaskInfo>();
		mDownloadLock.lock();
		try {
			Iterator<Entry<String, DownloadExecutorTarget>> it = mDownloadFiles.entrySet().iterator();
			while(it.hasNext()) {
				Entry<String, DownloadExecutorTarget> entry = it.next();
				DownloadExecutorTarget target = entry.getValue();
				if (! compare.isSameCondition(value, target)) {
					continue;
				}
				DownloadTaskInfo taskInfo = parseDownloadTaskInfo(null, target);
				if (taskInfo != null) {
					retTaskInfoList.add(taskInfo);
				}
			}
		}finally{
			mDownloadLock.unlock();
		}
		
		return retTaskInfoList;
	}
	
	@Override
	public List<DownloadTaskInfo> queryAllComplete() {
		List<DownloadTaskInfo> retTaskInfoList = new ArrayList<DownloadTaskInfo>();
		mDownloadLock.lock();
		try {
			Iterator<Entry<String, DownloadExecutorTarget>> it = mDownloadFiles.entrySet().iterator();
			while(it.hasNext()) {
				Entry<String, DownloadExecutorTarget> entry = it.next();
				DownloadExecutorTarget target = entry.getValue();
				if (! isDownloadComplete(target)) {
					continue;
				}
				DownloadTaskInfo taskInfo = parseDownloadTaskInfo(null, target);
				if (taskInfo != null) {
					retTaskInfoList.add(taskInfo);
				}
			}
		}finally {
			mDownloadLock.unlock();
		}
		return retTaskInfoList;
	}
	
	@Override
	public List<DownloadTaskInfo> getPending() {
		List<DownloadTaskInfo> taskInfos = new ArrayList<DownloadTaskInfo>();
		List<DownloadExecutorTarget> list = mDownloader.getPending();
		for(DownloadExecutorTarget target : list) {
			DownloadTaskInfo taskInfo = parseDownloadTaskInfo(target);
			taskInfo.status = DownloadTaskInfo.DOWNLOAD_STATE_PENDING;
			taskInfo.downloadrate = 0;
			taskInfos.add(taskInfo);
		}
		return taskInfos;
	}

	@Override
	public List<DownloadTaskInfo> getDownloading() {
		List<DownloadTaskInfo> taskInfos = new ArrayList<DownloadTaskInfo>();
		List<DownloadExecutorTarget> list = mDownloader.getDownloading();
		for(DownloadExecutorTarget target : list) {
			DownloadTaskInfo taskInfo = parseDownloadTaskInfo(target);
			taskInfo.status = DownloadTaskInfo.DOWNLOAD_STATE_DOWNLOADING;
			taskInfos.add(taskInfo);
		}
		return taskInfos;
	}

	@Override
	public int getPendingTaskCount() {
		return mDownloader.getPendingTaskCount();
	}

	@Override
	public int getRunningTaskCount() {
		return mDownloader.getRunningTaskCount();
	}
	
	@Override
	public boolean hasTasks() {
		return mDownloader.hashDownloadTask();
	}

	@Override
	public boolean setMaxActiveTaskCount(int count) {
		mDownloader.setMaxRunningSize(count);
		return true;
	}
	
	@Override
	public int getMaxActiveTaskCount() {
		return mDownloader.getMaxRunningSize();
	}
	
	@Override
	public boolean addDownloadListener(IDownloadListener listener) {
		return mDownloader.addDownloadListener(listener);
	}

	@Override
	public boolean removeDownloadListener(IDownloadListener listener) {
		return mDownloader.removeDownloadListener(listener);
	}
	
	@Override
	public boolean addDownloadListListener(IDownloadListListener listener) {
		if (listener == null) {
			return false;
		}
		mListenerLock.lock();
		try {
			if (mDownloadListListeners == null) {
				mDownloadListListeners = new ArrayList<IDownloadListListener>();
			}
			if (mDownloadListListeners.contains(listener)) {
				return false;
			}
			mDownloadListListeners.add(listener);
			return true;
		} finally {
			mListenerLock.unlock();
		}
	}
	
	@Override
	public boolean removeDownloadListListener(IDownloadListListener listener) {
		if (listener == null) {
			return false;
		}
		mListenerLock.lock();
		try {
			if (mDownloadListListeners == null) {
				return false;
			}
			return mDownloadListListeners.remove(listener);
		} finally {
			mListenerLock.unlock();
		}
	}

	@Override
	public void release(boolean stopTask) {
		mIsInit = false;
		mDownloader.release();
		mListenerLock.lock();
		try {
			if (mDownloadListListeners != null) {
				mDownloadListListeners.clear();
			}
		} finally {
			mListenerLock.unlock();
		}
		mWorkHandler.getLooper().quit();
		pauseAll(false, false);
		
		synchronized (mInstances) {
			mInstances.remove(mDownloadInstanceId);
		}
		
		Log.v(TAG, "release");
	}
	
	@Override
	public void setDownloadNotifier(IDownloadNotifier notifier) {
		Log.e(TAG, "Not Support Method");
		this.mDownloadNotifier = notifier;
	}

	@Override
	public <T extends IDownloadNotifier> void setNotifationClass(Class<T> clz) {
		try {
			this.mDownloadNotifier = clz.newInstance();
		} catch (InstantiationException e) {
			Log.d(TAG, "setNotifactionService error :" + e.getMessage());
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			Log.d(TAG, "setNotifactionService error :" + e.getMessage());
		}
	}
	
	@Override
	public void setUpdateDownloadNotify(boolean isUpdateNotify) {
		this.mIsAutoUpdateNotify = isUpdateNotify;
	}
		
	@Override
	public void showDownloadNotify() {
		updateNotification();
	} 

	@Override
	public void dismissDownloadNotify() {
		if (mDownloadNotifier == null) {
			return;
		}
		mDownloadNotifier.dismissNotification(mContext);
	}
	
	@Override
	public boolean setDownloadThreadCount(int count) {
		Log.e(TAG, "Not Support Method");
		return false;
	}

	@Override
	public boolean setRateLimit(int speed) {
		Log.e(TAG, "Not Support Method");
		return false;
	}

	@Override
	public void setUseStorageTask(boolean isUse) {
		this.mIsUseStorage = isUse;
	}
	
	public void setStorageName(String storageName) {
		this.mStorageName = storageName;
	}
	
	public void setRetryIntervals(long[] intervals) {
		this.mDownloader.setRetryIntervals(intervals);
	}
	
	@Override
	public boolean addServiceStateListener(IServiceStateListener listener) {
		Log.e(TAG, "Not Support Method");
		return false;
	}

	@Override
	public boolean removeServiceStateListener(IServiceStateListener listener) {
		Log.e(TAG, "Not Support Method");
		return false;
	}

	@Override
	public int getServiceState() {
		Log.e(TAG, "Not Support Method");
		return 0;
	}
	
	class DownloadListener implements IDownloadListener {

		@Override
		public boolean onDownloadStateChanged(DownloadState item) {
			Log.v(TAG, "onDownloadStateChanged state:" + item.getState() 
					+ " url:" + item.getUri() + " - ThreadID:" + Thread.currentThread().getId());
			int state = item.getState();
			switch (state) {
			//STATE_DOWNLOADING 有专门的Handle事件做周期性DB记录
			case DownloadState.STATE_DOWNLOADING:
			//STATE_PAUSE 和 STATE_PENDING 都由调用方主动触发，触发时候已经进行状态记录
			case DownloadState.STATE_PAUSE:
			case DownloadState.STATE_PENDING:
				return false;
			default:
				break;
			}
			
			if (state == DownloadState.STATE_FAIL) {
				int error = item.getErrorReason();
				if ((error == 404 || error == DownloadExecutor.RESULT_NOT_SUPPORT_CONTENT_TYPE_ERROR)
						&& item.getDownloadLen() == 0) {
					deleteTaskById(item.getDownloadId(), true);
				}
			}
			
			DownloadExecutorTarget target = item.getDownloadExecutorTarget();
			if (target == null) {
				return false;
			}
			updateTargetInfo(target, true);
			mDownloader.refreshDownloadTarget(target);
			return false;
		}
	}
	
	private static final int MAX_WAIT_DOWNLOADING_COUNT = 3;
	private int mWaitDownloadingCount = 0;
	/**
	 * [更新下载任务状态]<BR>
	 * 每N秒更新一次下载中的网络任务状态，N为AUTO_UPDATE_STORAGE
	 */
	private void updateDownloadingTargetInfo() {
		if (!mIsUseStorage || mDownloadStorage == null) {
			return;
		}
		List<DownloadExecutorTarget> list = mDownloader.getDownloading();
		if (list == null || list.size() == 0) {
			Log.w(TAG, "updateAllTargetInfo 下载列表为空");
			mWaitDownloadingCount++;
			if (mWaitDownloadingCount >= MAX_WAIT_DOWNLOADING_COUNT) {
				Log.w(TAG, "updateAllTargetInfo 下载列表为空,超出等待次数，停止自动更新");
				stopAutoUpdateDownloading();
			}
			return;
		}
		mWaitDownloadingCount = 0;
		for(DownloadExecutorTarget target : list) {
			mDownloader.refreshDownloadTarget(target);
			updateTargetInfo(target, true);
		}
	} 
	
	private void updateTargetInfo(DownloadExecutorTarget target, boolean updateState) {
		if (!mIsUseStorage || target == null || mDownloadStorage == null) {
			return;
		}
		DownloadBean updateDownloadBean = new DownloadBean();
		updateDownloadBean.downloadId = target.downloadId;
		updateDownloadBean.filelen = target.fileLength;
		updateDownloadBean.isPriv = target.isPrivate ? 1 : 0;
		updateDownloadBean.owner = target.onwer;
		updateDownloadBean.downloadLen = target.downloadLength;
		if (updateState) {
			updateDownloadBean.downloadState = getRealDownloadState(target.state);
		}
		mDownloadStorage.updateTask(updateDownloadBean);
		Log.v(TAG, "updateTargetInfo - 更新下载信息:" + 
				updateDownloadBean.downloadState + " - total:" + target.downloadLength
				+ " - updateState:" + updateDownloadBean.downloadState + " - state:" + target.state.downloadState
				+ " - url:" + target.url);
	}
	
	private void updateTargetState(String downloadID, int downloadState) {
		if (!mIsUseStorage  || mDownloadStorage == null) {
			return;
		}
		DownloadBean updateDownloadBean = new DownloadBean();
		updateDownloadBean.downloadId = downloadID;
		updateDownloadBean.downloadState = downloadState;
		mDownloadStorage.updateTask(updateDownloadBean);
		Log.v(TAG, "updateTargetState - 更新下载状态:" + updateDownloadBean.downloadState);
	}
	
	private int getRealDownloadState(DownloadTaskState state) {
		if (!state.isRun) {
			switch (state.downloadState) {
			case KeyConstants.DOWNLOAD_STATE_FAIL:
			case KeyConstants.DOWNLOAD_STATE_SUC:
				return state.downloadState;
			default:
				return KeyConstants.DOWNLOAD_STATE_PAUSE;
			}
		} else {
			switch (state.downloadState) {
			case KeyConstants.DOWNLOAD_STATE_NONE:
			case KeyConstants.DOWNLOAD_STATE_UNKOWN:
				return KeyConstants.DOWNLOAD_STATE_PENDING;
			default:
				return state.downloadState;
			}
		}
	}
	
	@Override
	public void pauseAllAndRecordTask() {
		Log.v(TAG, "pauseAllAndRecordTask");
		recordAllTaskState();
		pauseAll(false, true);
	}
	
	@Override
	public void recordAllTaskState() {
		if (mDownloadStorage == null) {
			return;
		}
		Log.d(TAG, "recordAllTaskState");
		List<DownloadExecutorTarget> downloadinglist = mDownloader.getDownloading();
		List<DownloadExecutorTarget> pendinglist = mDownloader.getPending();
		List<DownloadExecutorTarget> recordlist = new ArrayList<DownloadExecutorTarget>();
		if (downloadinglist != null && downloadinglist.size() > 0) {
			recordlist.addAll(downloadinglist);
		}
		if (pendinglist != null && pendinglist.size() > 0) {
			recordlist.addAll(pendinglist);
		}
		for(DownloadExecutorTarget target : recordlist) {
			mDownloader.refreshDownloadTarget(target);
			updateTargetInfo(target, true);
		}
	}
	
	@Override
	public void resumeAllTaskState() {
		if (mDownloadStorage == null) {
			return;
		}
		Log.v(TAG, "resumeAllTaskState");
		pauseAll(false, true);
		List<DownloadBean> downloadBeans = mDownloadStorage.queryAllTask();
		for(DownloadBean downloadBean : downloadBeans) {
			switch (downloadBean.downloadState) {
			case KeyConstants.DOWNLOAD_STATE_INTO_DOWNLOADING_QUEUE:
			case KeyConstants.DOWNLOAD_STATE_PREPARE:
			case KeyConstants.DOWNLOAD_STATE_START:
			case KeyConstants.DOWNLOAD_STATE_DOWNLOADING:
			case KeyConstants.DOWNLOAD_STATE_FAIL:
				Log.v(TAG, "resumeAllTaskState state:" + downloadBean.downloadState + " - name:" + downloadBean.fileName);
				runTaskById(downloadBean.downloadId, false);
				continue;
			}
		}
		for(DownloadBean downloadBean : downloadBeans) {
			switch (downloadBean.downloadState) {
			case KeyConstants.DOWNLOAD_STATE_PENDING:
				Log.v(TAG, "resumeAllTaskState state:" + downloadBean.downloadState + " - name:" + downloadBean.fileName);
				runTaskById(downloadBean.downloadId, false);
				continue;
			}
		}

	}
	
	@Override
	public int getAllTaskCount() {
		return mDownloadFiles.size();
	}
	
	private void notifyDownloadState(DownloadExecutorTarget info, int state) {
		notifyDownloadState(info, state, true);
	}
	
	private void notifyDownloadState(DownloadExecutorTarget info, int state, boolean isNotifyRoot) {
		DownloadState downloadState = DownloadExecutorManager.parseDownloadState(info);
		downloadState.setState(state);
		mDownloader.notifyDownloadListener(downloadState, isNotifyRoot);
	}
	
	/**
	 * [删除未下载完成的网络任务]<BR>
	 * 
	 * @param info
	 * @return
	 */
	private boolean deleteTaskFile(DownloadExecutorTarget info, boolean deleteFile) {
		if (!deleteFile) {
			return false;
		}
		if (info == null || info.savePath == null) {
			return false;
		}
		File file = new File(info.savePath);
		if (file.exists()) {
			file.delete();
		}
		File tempFile = new File(info.savePath+DownloadExecutor.SUFFIX_TEMP);
		if (tempFile.exists()) {
			tempFile.delete();
		}
		return true;
	}
	
	public void checkAutoUpdateDownloading() {
		Log.v(TAG, "checkUpdateDownloadingInfo");
		mWorkHandler.sendEmptyMessage(MSG_AUTO_UPDATE_DOWNLOADING_INFO_CHECK);
	}
	
	public void stopAutoUpdateDownloading() {
		Log.v(TAG, "stopUpdateDownloadingInfo");
		mWorkHandler.sendEmptyMessage(MSG_STOP_UPDATE_DOWNLOADING_INFO);
	}
	
	private static final int AUTO_NOTIFY_INTERVAL_TIME = 1 * 1000;
	private static final int MAX_WAIT_DOWNLOAD_COUNT = 3;
	private int mWaitDownloadCount = 0;
	private void notifyDownloadListState() {
		List<IDownloadListListener> downloadListListeners = mDownloadListListeners;
		if (downloadListListeners == null) {
			mWorkHandler.removeMessages(MSG_UPDATE_DOWNLOAD_LIST_STATE);
			return;
		}
		mListenerLock.lock();
		try {
			for(IDownloadListListener listener : downloadListListeners) {
				listener.onDownloadListStateChanged();
			}
		} finally {
			mListenerLock.unlock();
		}
		boolean hasTask = mDownloader.hashDownloadTask();
		if (! hasTask) {
			mWaitDownloadCount++;
		} else {
			mWaitDownloadCount = 0;
		}
		if (mWaitDownloadCount < MAX_WAIT_DOWNLOAD_COUNT) {
			mWorkHandler.sendEmptyMessageDelayed(MSG_UPDATE_DOWNLOAD_LIST_STATE, AUTO_NOTIFY_INTERVAL_TIME);
		}
	}
	
	private void requestNotifyDownlaodListState() {
		if (mDownloadListListeners == null) {
			return;
		}
		if (mWorkHandler.hasMessages(MSG_UPDATE_DOWNLOAD_LIST_STATE)) {
			return;
		}
		mWorkHandler.sendEmptyMessage(MSG_UPDATE_DOWNLOAD_LIST_STATE);
	}
	
	private static final int AUTO_UPDATE_NOTIFICATION_INTERVAL_TIME = 1 * 1000;
	private static final int MAX_WAIT_NOTIFICATION_COUNT = 3;
	private int mWaitNotificationCount = 0;
	private void updateNotification() {
		if (mDownloadNotifier == null) {
			return;
		}
		mDownloadNotifier.onDownloadStateChanged(mContext, this);
		if (! mIsAutoUpdateNotify) {
			return;
		}
		boolean hasTask = mDownloader.hashDownloadTask();
		if (! hasTask) {
			mWaitNotificationCount++;
		} else {
			mWaitNotificationCount = 0;
		}
		if (mWaitNotificationCount < MAX_WAIT_NOTIFICATION_COUNT) {
			mWorkHandler.sendEmptyMessageDelayed(MSG_UPDATE_NOTIFICATION, AUTO_UPDATE_NOTIFICATION_INTERVAL_TIME);
		}
	}
	
	private static final int MSG_AUTO_UPDATE_DOWNLOADING_INFO_CHECK = 1;
	private static final int MSG_AUTO_UPDATE_DOWNLOADING_INFO = 2;
	private static final int MSG_STOP_UPDATE_DOWNLOADING_INFO = 3;
	private static final int MSG_UPDATE_DOWNLOAD_LIST_STATE = 4;
	private static final int MSG_UPDATE_NOTIFICATION = 5;
	
	class WorkHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_AUTO_UPDATE_DOWNLOADING_INFO_CHECK:
				synchronized (this) {
					if (mWorkHandler.hasMessages(MSG_AUTO_UPDATE_DOWNLOADING_INFO)) {
						return;
					}
					Log.v(TAG, "Handler MSG_AUTO_UPDATE_DOWNLOADING_INFO_CHECK");
					mWorkHandler.sendEmptyMessageDelayed(MSG_AUTO_UPDATE_DOWNLOADING_INFO, AUTO_UPDATE_STORAGE);
				}
				break;
			case MSG_AUTO_UPDATE_DOWNLOADING_INFO:
				Log.v(TAG, "Handler MSG_AUTO_UPDATE_DOWNLOADING_INFO hasMore:" + mWorkHandler.hasMessages(MSG_AUTO_UPDATE_DOWNLOADING_INFO));
				updateDownloadingTargetInfo();
				mWorkHandler.sendEmptyMessageDelayed(MSG_AUTO_UPDATE_DOWNLOADING_INFO, AUTO_UPDATE_STORAGE);
				break;
			case MSG_STOP_UPDATE_DOWNLOADING_INFO:
				mWorkHandler.removeMessages(MSG_AUTO_UPDATE_DOWNLOADING_INFO);
				break;
			case MSG_UPDATE_DOWNLOAD_LIST_STATE:
				notifyDownloadListState();
				break;
			case MSG_UPDATE_NOTIFICATION:
				updateNotification();
				break;
			default:
				break;
			}
		}
	}

	private DownloadExecutorTarget parseDownloadExecutorTarget(DownloadBean downloadBean) {
		DownloadExecutorTarget target = new DownloadExecutorTarget();
		target.url = downloadBean.url;
		target.method = downloadBean.httpMethod;
		target.postParam = downloadBean.postParam;
		target.savePath = downloadBean.path;
		target.fileLength = downloadBean.filelen;
		target.downloadLength = downloadBean.downloadLen;
		target.notAcceptTypes = mNotAcceptTypeList;
		target.downloadId = downloadBean.downloadId;
		target.createTime = downloadBean.createTime;
		target.onwer = downloadBean.owner;
		target.isPrivate = downloadBean.isPriv == 0 ? false : true;
		target.readTimeout = downloadBean.readTimeout != -1 ? downloadBean.readTimeout : mReadTimeout;
		target.connectTimeout = downloadBean.connectionTimeout != -1 ? downloadBean.connectionTimeout : mConnectionTimeout;
		target.retryCount = downloadBean.retryCount != -1 ? downloadBean.retryCount : mRetryCount;
		target.httpHead = DownloadUtil.parseStringToMap(downloadBean.httpHead);
		target.fileType = downloadBean.fileType;
		target.fileVersion = downloadBean.fileVersion;
		return target;
	}
	
	private DownloadBean parseDownloadBean(DownloadTaskParam param, 
			long createTime, String downloadId) {
		DownloadBean downloadBean = new DownloadBean();
		downloadBean.url = param.uri;
		downloadBean.httpMethod = param.httpMethod; 
		downloadBean.postParam = param.postParam;
		downloadBean.path = param.saveFolder + File.separator + param.fileName;
		downloadBean.folder = param.saveFolder;
		downloadBean.fileName = param.fileName;
		downloadBean.downloadId = downloadId;
		downloadBean.createTime = createTime;
		downloadBean.owner = param.onwer;
		downloadBean.isPriv = param.isPrivate ? 1 : 0;
		downloadBean.readTimeout = param.readTimeout != -1 ? param.readTimeout : mReadTimeout;
		downloadBean.connectionTimeout = param.connectionTimeout != -1 ? param.connectionTimeout : mConnectionTimeout;
		downloadBean.retryCount = param.retryCount != -1 ? param.retryCount : mRetryCount;
		downloadBean.httpHead = DownloadUtil.parseMapToString(param.httpHead);
		downloadBean.fileType = param.fileType;
		downloadBean.fileVersion = param.fileVersion;
		return downloadBean;
	}
	
	private DownloadExecutorTarget parseDownloadExecutorTarget(DownloadTaskParam param, 
			long createTime, String downloadId) {
		DownloadExecutorTarget info = new DownloadExecutorTarget();
		info.url = param.uri;
		info.method = param.httpMethod; 
		info.postParam = param.postParam;
		info.savePath = param.saveFolder + File.separator + param.fileName;
		info.notAcceptTypes = mNotAcceptTypeList;
		info.downloadId = downloadId;
		info.createTime = createTime;
		info.onwer = param.onwer;
		info.isPrivate = param.isPrivate;
		info.readTimeout = param.readTimeout != -1 ? param.readTimeout : mReadTimeout;
		info.connectTimeout = param.connectionTimeout != -1 ? param.connectionTimeout : mConnectionTimeout;
		info.retryCount = param.retryCount != -1 ? param.retryCount : mRetryCount;
		info.httpHead = param.httpHead;
		info.fileType = param.fileType;
		info.fileVersion = param.fileVersion;
		return info;
	}
	
	private DownloadTaskInfo parseDownloadTaskInfo(DownloadTaskInfo taskInfo, DownloadExecutorTarget target) {
		if (target == null) {
			return taskInfo;
		}
		
		if (taskInfo == null) {
			taskInfo = parseDownloadTaskInfo(target);
		} else if (target.executor != null) {
			taskInfo.status = target.executor.getDownloadState();
			taskInfo.downloadLen = target.executor.getCurrentDownloadSize();
			int downloadState = target.executor.getDownloadState();
			switch (downloadState) {
			case DownloadState.STATE_DOWNLOADING:
				//下载中的任务会有下载速度
				taskInfo.downloadrate = target.executor.getDownloadRate();
			case DownloadState.STATE_PREPARE:
			case DownloadState.STATE_START:
			case DownloadState.STATE_SUC:
				taskInfo.currentRetryCount = target.executor.getCurrentRetryCount();
			default:
				break;
			}
		}
		
		if (taskInfo.status == DownloadTaskInfo.DOWNLOAD_STATE_NONE) { 
			boolean isPending = mDownloader.constainsPendingTask(target);
			if (isPending) {
				taskInfo.status = DownloadTaskInfo.DOWNLOAD_STATE_PENDING;
			}
		}
		return taskInfo;
	}
	
	private DownloadTaskInfo parseDownloadTaskInfo(DownloadExecutorTarget target) {
		DownloadTaskInfo taskInfo = new DownloadTaskInfo();
		taskInfo.uri = target.url;
		taskInfo.httpMethod = target.method;
		taskInfo.postParam = target.postParam;
		taskInfo.filelen = target.fileLength;
		taskInfo.fileName = DownloadUtil.getFileName(target.savePath);
		taskInfo.folder = DownloadUtil.getPreDirPath(target.savePath);
		taskInfo.path = target.savePath;
		taskInfo.createTime = target.createTime;
		taskInfo.downloadId = target.downloadId;
		taskInfo.downloadLen = target.downloadLength;
		taskInfo.fileVersion = target.fileVersion;
		taskInfo.fileType = target.fileType;
		File file = new File(target.savePath);
//		if (target.fileLength > 0 && target.fileLength == target.downloadLength) {
		if (target.fileLength > 0 && file.exists() && target.fileLength == file.length()) {
			taskInfo.status = DownloadState.STATE_SUC;
		}
		
		if (target.executor != null) {
			taskInfo.status = target.executor.getDownloadState();
			taskInfo.downloadLen = target.executor.getCurrentDownloadSize();
			int downloadState = target.executor.getDownloadState();
			switch (downloadState) {
			case DownloadState.STATE_DOWNLOADING:
				//下载中的任务会有下载速度
				taskInfo.downloadrate = target.executor.getDownloadRate();
			case DownloadState.STATE_PREPARE:
			case DownloadState.STATE_START:
			case DownloadState.STATE_SUC:
				taskInfo.currentRetryCount = target.executor.getCurrentRetryCount();
			default:
				break;
			}
		}
		
		return taskInfo;
	}
	
	private boolean isDownloadComplete(DownloadExecutorTarget info) {
		File file = new File(info.savePath);
		if (! file.exists()) {
			return false;
		}
		if (info.fileLength == file.length()) {
			return true;
		}
		return false;
	}

	interface CompareCondition<T> {
		boolean isSameCondition(T value, DownloadExecutorTarget target);
	}
}

