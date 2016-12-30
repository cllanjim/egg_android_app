package com.lingyang.base.utils.downloadmanager.excutor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;

import com.lingyang.base.utils.Log;
import com.lingyang.base.utils.NetworkStatus;
import com.lingyang.base.utils.ThreadPoolManagerQuick;
import com.lingyang.base.utils.downloadmanager.DownloadUtil;
import com.lingyang.base.utils.downloadmanager.excutor.DownloadExecutor.IExecutorTargetNotify;
import com.lingyang.base.utils.executor.ThreadPoolManager;

public class DownloadExecutorManager extends Thread {
	
	private static final String TAG = "DownloadExecutorManager";
	
	public static final int THREAD_POOL_TYPE = -999;
	public static final int MAX_POOL_SIZE = 10;
	public static final int MIN_POOL_SIZE = 0;
	public static final int KEEP_ALIVE = 60 * 1000;
	public static final String THREAD_NAME = "Downloader";

	private Context mContext;
	
	private boolean mIsRun;
	
	private volatile TaskQueue<DownloadExecutorTarget> queue;
	
	private ThreadPoolManager threadPoolManager;
	
	private IDownloadListener rootDownloadListener;
	private IDownloadListener taskDownloadListener = new DownloadListener();
	private List<IDownloadListener> downloadListeners = new ArrayList<IDownloadListener>();
	private ReentrantLock listenerLock = new ReentrantLock();
	
	private ReentrantLock networkLock = new ReentrantLock();
    private final Condition networkCondition = networkLock.newCondition();
    private boolean isWaitNetwork = false;
    
    private NetworkStatus allowDownloadState[];
    
	private long retryIntervals[];
    
	public DownloadExecutorManager(Context context) {
		mIsRun = false;
		mContext = context;
		queue = new TaskQueue<DownloadExecutorTarget>();
		threadPoolManager = ThreadPoolManager.getInstance(THREAD_POOL_TYPE);
		threadPoolManager.init(context);
		threadPoolManager.setMaxPoolSize(MAX_POOL_SIZE);
		threadPoolManager.setMinPoolSize(MIN_POOL_SIZE);
		threadPoolManager.setKeepAlive(KEEP_ALIVE);
		threadPoolManager.setThreadName(THREAD_NAME);
		threadPoolManager.initThreadPoll(THREAD_POOL_TYPE);
		registReceiver();
	}
	
	public void start() {
		if (mIsRun) {
			return;
		}
		this.mIsRun = true;
//		DownloaderThread thread = new DownloaderThread();
//		thread.start();
		super.start();
	}
	
	public void runTask(DownloadExecutorTarget info) {
		Log.v(TAG, "runTask " + info.url);
		synchronized (info) {
			info.state.isRun = true;
			if (! queue.contains(info)) {
				info.state.downloadState = DownloadState.STATE_PENDING;
				queue.offer(info);
				Log.v("pause", "runTask " + info.url);
			}
		}
		start();
		signalNetwork();
	}
	
	public void runTaskPriority(DownloadExecutorTarget info) {
		Log.v(TAG, "runTaskPriority " + info.url);
		synchronized (info) {
			if (queue.containsRunning(info)) {
				DownloadExecutor executor = info.executor;
				if (executor == null || !executor.isTaskRun()) {
					info.state.isRun = true;
					info.state.downloadState = DownloadState.STATE_PENDING;
					queue.offer(info);
					Log.v(TAG, "runTaskPriority 再次排入队列");
				} else {
					Log.v(TAG, "runTaskPriority 正在运行中，忽视");
				}
			} else {
				info.state.isRun = true;
				info.state.downloadState = DownloadState.STATE_PENDING;
				queue.insertFirst(info);
				Log.v(TAG, "runTaskPriority 插队, url:" + info.url);
			}
			Log.v("pause", "runTaskPriority " + info.url);
			info.state.isRun = true;
		}
		start();
		signalNetwork();
	}
	
	public boolean pauseTask(DownloadExecutorTarget info) {
		return pauseTask(info, true);
	}
	
	public boolean pauseTask(DownloadExecutorTarget info, boolean isNotify) {
		if (info == null) {
			return false;
		}
		boolean isSuc = false;
		synchronized (info) {
			isSuc = queue.removeQueue(info);
			info.state.isRun = false;
			if (info.executor != null) {
				info.executor.setDownloadNotify(isNotify);
				info.executor.pauseDownload();
			}
			Log.d(TAG, "pauseTask " + info.url + " - isSuc:" + isSuc + " - executor:" + info.executor);
			info.executor = null;
		}
		signalNetwork();
		return isSuc;
	}
	
	public boolean pauseAllTask() {
		List<DownloadExecutorTarget> collection = queue.clearTask();
		if (collection == null) {
			return false;
		}
		
		Iterator<DownloadExecutorTarget> it = collection.iterator();
		while(it.hasNext()) {
			DownloadExecutorTarget target = it.next();
			synchronized (target) {
				target.state.isRun = false;
				if (target.executor != null) {
					target.executor.pauseDownload();
					Log.v(TAG, "pauseAllTask url:" + target.url);
				}
				target.executor = null;
			}
		}
		Log.d(TAG, "pauseAllTask queueHashCode:" + queue.hashCode() + " - queueSize:" + queue.size());
		return true;
	}
	
	/**
	 * [暂停所有正在下载的任务但保持当前的队列状态]
	 */
	private void pauseAllDownloadingHoldQueue() {
		Log.v(TAG, "pauseAllDownloadingHoldQueue");
		List<DownloadExecutorTarget> list =	queue.getRunningQueue();
		if (list == null || list.size() == 0) {
			return;
		}
		for(DownloadExecutorTarget target : list) {
			synchronized (target) {
				if (target.executor != null) {
					target.executor.setDownloadNotify(false);
					target.executor.pauseDownload();
					Log.v(TAG, "pauseAllDownloadingHoldQueue url:" + target.url);
				}
				target.executor = null;
			}
		}
	}
	
	public List<DownloadExecutorTarget> removeAllTask() {
		List<DownloadExecutorTarget> collection = queue.clearTask();
		return collection;
	}
	
	public void refreshDownloadTarget(DownloadExecutorTarget target) {
		if (target == null) {
			return;
		}
		DownloadExecutor executor = target.executor;
		if (executor == null) {
			return;
		}
		target.fileLength = executor.getTotalDownloadSize();
		long currentDownloadSize = executor.getCurrentDownloadSize();
		if (currentDownloadSize != -1) {
			target.downloadLength = currentDownloadSize;
		}
	}
	
	private void finishDownloadTask(DownloadExecutorTarget params) {
		queue.removeRunningTask(params);
	}
	
	public boolean constainsPendingTask(DownloadExecutorTarget info) {
		if (info == null) {
			return false;
		}
		DownloadExecutorTarget executorInfo = queue.getPendingTask(info);
		if (executorInfo != null) {
			return true;
		}
		return false;
	}
	
	public List<DownloadExecutorTarget> getPending() {
		return queue.getPendingQueue();
	}

	public List<DownloadExecutorTarget> getDownloading() {
		return queue.getRunningQueue();
	}
	
	public int getPendingTaskCount() {
		return queue.getPendingSize();
	}

	public int getRunningTaskCount() {
		return queue.getRunningSize();
	}
	
	public int getMaxRunningSize() {
		return queue.getMaxRunningSize();
	}
	
	public boolean hashDownloadTask() {
		return !queue.isEmpty();
	}

	public void setMaxRunningSize(int maxRunningSize) {
		queue.setMaxRunningSize(maxRunningSize);
	}

	public void setRootDownloadListener(IDownloadListener listener) {
		this.rootDownloadListener = listener;
	}
	
	public void setAllowDownloadState(NetworkStatus[] status) {
		this.allowDownloadState = status;
		Log.v(TAG, "setAllowDownloadState");
		if (status == null) {
			return;
		}
		handleDownloadNetworkState();
	}
	
	public NetworkStatus[] getAllowDownloadState() {
		return this.allowDownloadState;
	}
	
	public void setRetryIntervals(long[] intervals) {
		this.retryIntervals = intervals;
	}
	
	public boolean addDownloadListener(IDownloadListener listener) {
		if (listener == null) {
			return false;
		}
		listenerLock.lock();
		try {
			if (downloadListeners.contains(listener)) {
				return false;
			}
			downloadListeners.add(listener);
		}finally{
			listenerLock.unlock();
		}
		return true;
	}
	
	public boolean removeDownloadListener(IDownloadListener listener) {
		if (listener == null) {
			return false;
		}
		listenerLock.lock();
		try {
			return downloadListeners.remove(listener);
		}finally {
			listenerLock.unlock();
		}
	}
	
	public void clearDownloadListener() {
		listenerLock.lock();
		try {
			downloadListeners.clear();
		}finally {
			listenerLock.unlock();
		}
	}
	
	public void release() {
		unRegistReceiver();
		clearDownloadListener();
	}
	
	/**
	 * [唤醒下载队列]
	 */
	public void awakenDownloadQueue() {
		Log.v(TAG, "awakenDownloadQueue");
		//唤醒正在等待网络的 networkLock
		signalNetwork();
		//queue.take() 的阻塞状态将被打断
		this.interrupt();
	}
	
	@Override
	public void run() {
		DownloadExecutorTarget downloadOptions;
		while(mIsRun) {
			downloadOptions = getTask();
			
			NetworkStatus status = NetworkStatus.getNetworkStatus(mContext);
			if (! isAllowDownload(status)) {
				awaitNetwork();
				continue;
			}
			
			if (downloadOptions == null) {
				continue;
			}
			download(downloadOptions);
		}
	}
	
	public DownloadExecutorTarget getTask() {
		DownloadExecutorTarget task = null;
		if (task == null) {
			task = getWaitDownloadingTarget();
		}
		
		try {
			if (task == null) {
				task = queue.take();
				
				if (task != null) {
					synchronized (task) {
						if (task.state.isRun) {
							task.state.downloadState = DownloadState.STATE_INTO_DOWNLOADING_QUEUE;
							DownloadState state = parseDownloadState(task);
							state.setState(DownloadState.STATE_INTO_DOWNLOADING_QUEUE);
							notifyDownloadListener(state, true);
						}
					}
				}
			}
			
		} catch (InterruptedException e) {
			Log.d(TAG, "getTask InterruptedException");
		}
		
		if (task != null) {
			Log.v(TAG, "getTask:" + task.downloadId 
					+ " - pendding:" + queue.getPendingSize() + " - downloading:" + queue.getRunningSize());
		} else {
			Log.v(TAG, "getTask is null");
		}
		
		return task;
	}
	
	private DownloadExecutorTarget getWaitDownloadingTarget() {
		if (queue.getRunningSize() == 0) {
			return null;
		}
		List<DownloadExecutorTarget> list = queue.getRunningQueue();
		for(DownloadExecutorTarget target : list) {
			synchronized (target) {
				//处于正在下载队列中的任务没有对应绑定的下载器，且该任务标记为需要下载的任务，则返回此类任务提供再次下载
				if (target.executor != null) {
					continue;
				}
				if (! target.state.isRun) {
					continue;
				}
				Log.v(TAG, "getWaitDownloadingTarget url:" + target.url);
				return target;
			}
		}
		return null;
	}
	
	private void download(final DownloadExecutorTarget target) {
		final DownloadExecutor task = new DownloadExecutor(target);
		synchronized (target) {
			if (!target.state.isRun || !isAllowDownload(NetworkStatus.getNetworkStatus(mContext))) {
				finishDownloadTask(target);
				DownloadState state = parseDownloadState(target);
				state.setState(DownloadState.STATE_PAUSE);
				notifyDownloadListener(state);
				return;
			}
			target.executor = task;
		}
		Log.d(TAG,"download  " + target.url);
		
		threadPoolManager.execute(new Runnable() {
			@Override
			public void run() {
				task.setIsNotifyDownloadingListener(true);
				task.setDownloadListener(taskDownloadListener);
				task.setExecutorTargetNotify(targetUpdater);
				task.setCurrentThread(Thread.currentThread());
				task.setRetryIntervals(retryIntervals);
				int result = task.startDownload();
				
				synchronized (target) {
					if (target.executor == task) {
						target.executor = null;
					}

					NetworkStatus status = NetworkStatus.getNetworkStatus(mContext);
					if (result != DownloadExecutor.RESULT_SUC && ! isAllowDownload(status)) {
						//当前网络不允许进下载
						Log.v(TAG, "download end 当前网络不允许进下载 result:" + result + " - status:" + status + " - url:" + target.url);
						return;
					}
					
					Log.v(TAG, "download end networkStatus:" + status);
					
					if (target.state.isRun && ! task.isDownloadRunning()) {
						/**
						 * 当前Task被暂停，但该下载任务状态任为“需要被下载的任务”
						 * 此时的Task已经被之前的操作暂停，不以当前的操作状态为标准去移除下载任务队列
						 */
					} else {
						Log.v(TAG, "download end TargetIsRun:" + target.state.isRun 
								+ " - taskDownloadRunning:" + task.isDownloadRunning()
								+ " - url:" + target.url);
						finishDownloadTask(target);
						target.state.isRun = false;
					}
				}
			}
		});
	}
	
	public void notifyDownloadListener(DownloadState state) {
		notifyDownloadListener(state, true);
	}
	
	public void notifyDownloadListener(DownloadState state, boolean isNotifyRoot) {
		if (rootDownloadListener != null && isNotifyRoot) {
			rootDownloadListener.onDownloadStateChanged(state);
		}
//		Log.v(TAG, "notifyDownloadListener state:" + state.getState() + " - url: " + state.getUri() + " - obj:" + state);
		List<IDownloadListener> downloadListeners = getDownloadListeners();
		for(IDownloadListener listener : downloadListeners) {
			boolean isInterrupt = listener.onDownloadStateChanged(state);
			if (isInterrupt) {
				Log.i(TAG, "notifyDownloadListener 事件被截获, " + state.getDownloadId());
				break;
			}
		}
	}
	
	public List<IDownloadListener> getDownloadListeners() {
		List<IDownloadListener> downloadListeners = new ArrayList<IDownloadListener>();
		listenerLock.lock();
		try {
			downloadListeners.addAll(DownloadExecutorManager.this.downloadListeners);
		}finally {
			listenerLock.unlock();
		}
		return downloadListeners;
	}
	
	public static DownloadState parseDownloadState(DownloadExecutorTarget info) {
		DownloadState downloadState = new DownloadState();
		downloadState.setDownloadId(info.downloadId);
		downloadState.setUri(info.url);
		downloadState.setDownloadLen(info.downloadLength);
		downloadState.setTotalLen(info.fileLength);
		downloadState.setDownloadExecutorTarget(info);
		DownloadExecutor executor = info.executor;
		if (executor != null) {
			downloadState.setState(executor.getDownloadState());
			downloadState.setProgress(executor.getDownloadProgress());
			downloadState.setRetryCount(executor.getCurrentRetryCount());
			downloadState.setDownloadLen(executor.getCurrentDownloadSize());
		} else {
			int progress = DownloadUtil.getDownloadProgress(info.fileLength, info.downloadLength);
			downloadState.setProgress(progress);
		}
		return downloadState;
	}
	
	private void awaitNetwork() {
		try {
			networkLock.lock();
			Log.d(TAG, "awaitNetwork");
			isWaitNetwork = true;
			networkCondition.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			networkLock.unlock();
		}
	}
	
	private void signalNetwork() {
		networkLock.lock();
		try {
			if (! isWaitNetwork) {
				return;
			}
//			NetworkStatus status = NetworkStatus.getNetworkStatus(mContext);
//			if (! isAllowDownload(status)) {
//				return;
//			}
			Log.v(TAG, "signalNetwork");
			networkCondition.signalAll();
		} finally {
			networkLock.unlock();
		}
	}
	
	IExecutorTargetNotify targetUpdater = new IExecutorTargetNotify() {

		@Override
		public boolean isCanNotifyState(int state, DownloadExecutorTarget target) {
			if (state == DownloadState.STATE_PAUSE) {
				boolean isConstantsDownload = DownloadUtil.containsDownloadExecutor(getPending(), target.downloadId);
				if (isConstantsDownload) {
					//下载暂停监听被截获，该任务正在Pedding队列中
					Log.i(TAG, "isCanNotifyState 下载暂停监听被截获，该任务正在Pedding队列中");
					return false;
				}
				isConstantsDownload = DownloadUtil.containsDownloadExecutor(getDownloading(), target.downloadId);
				if (isConstantsDownload) {
					//下载暂停监听被截获，该任务正在Downloading队列中
					Log.i(TAG, "isCanNotifyState 下载暂停监听被截获，该任务正在Downloading队列中");
					return false;
				}
			}
			
			if (state == DownloadState.STATE_FAIL) {
				NetworkStatus status = NetworkStatus.getNetworkStatus(mContext);
				Log.v(TAG, "isCanNotifyState 下载失败，当前网络 status:" + status + " - url:" + target.url);
				boolean isAllowDownload = isAllowDownload(status);
				if (! isAllowDownload) {
					Log.i(TAG, "isCanNotifyState 下载失败，当前网络不允许下载，不进行网络状态通知 status:" + status + " - url:" + target.url);
					return false;
				}
			}
			
			return true;
		}
	};
	
	class DownloadListener implements IDownloadListener {
		@Override
		public boolean onDownloadStateChanged(DownloadState state) {
			notifyDownloadListener(state);
			return false;
		}
	}
	
	
	private NetworkChangerReceiver mNetworkChangeReceiver;
	private void registReceiver() {
		if (mNetworkChangeReceiver != null) {
			return;
		}
		mNetworkChangeReceiver = new NetworkChangerReceiver();
		IntentFilter networkFilter = new IntentFilter(
				ConnectivityManager.CONNECTIVITY_ACTION);
		mContext.registerReceiver(mNetworkChangeReceiver, networkFilter);
	}
	
	private void unRegistReceiver() {
		if (mNetworkChangeReceiver != null) {
			try {
				mContext.unregisterReceiver(mNetworkChangeReceiver);
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private void handleDownloadNetworkState() {
		NetworkStatus status = NetworkStatus.getNetworkStatus(mContext);
		Log.v(TAG, "handleDownloadNetworkState status:" + status.name());
		if (isAllowDownload(status)) {
			awakenDownloadQueue();
		} else {
			//当前不允许下载，暂停下载队列
			pauseAllDownloadingHoldQueue();
		}
	}
	
	class NetworkChangerReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			ThreadPoolManagerQuick.execute(new Runnable() {
				@Override
				public void run() {
					handleDownloadNetworkState();
				}
			});
		}
	}
	
	private boolean isAllowDownload(NetworkStatus status) {
		if (allowDownloadState == null) {
			return true;
		}
		for(NetworkStatus s : allowDownloadState) {
			if (s == status) {
				return true;
			}
		}
		return false;
	}
}

