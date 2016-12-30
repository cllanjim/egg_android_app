package com.lingyang.base.utils.executor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.lingyang.base.utils.executor.queue.LinkedBlockingQueue;
public class ThreadPoolManager {
	
	private final static String TAG = "ThreadPoolManager";

	
	/**
	 * 正常的请求
	 * 需要正常排队，处理速度一般，一般的Http请求
	 */
	public static final int TYPE_NORMAL = 0;
	/**
	 * 需要快速响应，但是一般不会时间太长的请求
	 * 例如：Db的间歇性打开关闭、加载Bitmap、短暂的AIDL（一般最长不超过10秒）
	 */
	public static final int TYPE_QUICK = 1;
	/**
	 * 可以被清除的请求
	 * 例如：进入另一个Activity的请求，退出该Activity时里面的请求可以被清除
	 */
	public static final int TYPE_CAN_CLEAR = 2;
	
	private static final int CAN_CLEAR_MIN_POOL_SIZE = 0;
	private static final int CAN_CLEAR_MAX_POOL_SIZE = 3;
	private static final int CAN_CLEAR_KEEP_ALIVE = 3;
	
	private static final int QUICK_MIN_POOL_SIZE = 3;
	private static final int QUICK_MAX_POOL_SIZE = 5;
	private static final int QUICK_KEEP_ALIVE = 10;
	
	private static final int NORMAL_MIN_POOL_SIZE = 0;
	private static final int NORMAL_MAX_POOL_SIZE = 10;
	private static final int NORMAL_KEEP_ALIVE = 8;
	
	private static final int KEEP_ALIVE = 1;
	private static final int MAXIMUM_POOL_SIZE = 128;
	private static final int THREAD_QUEUE_SIZE = Integer.MAX_VALUE;
	
	private static HashMap<Integer, ThreadPoolManager> POOL_MANAGER_MAP;
	
	private ThreadFactory sThreadFactory;

	private LinkedBlockingQueue<Runnable> sPoolWorkQueue;

	private ThreadPoolExecutor THREAD_POOL_EXECUTOR;
	
	public final AutoCloseExecutor SERIAL_EXECUTOR = new AutoCloseExecutor();

	private static final int MESSAGE_RESIZE_CORE_SIZE = 0x0;

	private Handler mHandler;
	
	private int mManagerType;
	
	private int mMaxPoolSize;
	private int mMinPoolSize;
	private long mKeepAlive;
	
	private String mThreadName;
	
	private ThreadPoolManager(int type) {
		mManagerType = type;
		
		sThreadFactory = new ThreadFactory() {
			private final AtomicInteger mCount = new AtomicInteger(1);
			public java.lang.Thread newThread(Runnable r) {
				return new java.lang.Thread(r, mThreadName + mCount.getAndIncrement());
			}
		};
		
		sPoolWorkQueue = new LinkedBlockingQueue<Runnable>(THREAD_QUEUE_SIZE);
		
		initThreadPoll(type);
	}

	public void initThreadPoll(int type) {
		int maxPoolSize = mMaxPoolSize;
		int minPoolSize = mMinPoolSize;
		long keepAlive = mKeepAlive;
		String threadName = mThreadName;
		if (maxPoolSize == 0) {
			switch (type) {
			case TYPE_NORMAL:
				maxPoolSize = NORMAL_MAX_POOL_SIZE;
				minPoolSize = NORMAL_MIN_POOL_SIZE;
				keepAlive = NORMAL_KEEP_ALIVE;
				threadName = "LingYang_N #";
				break;
			case TYPE_QUICK:
				maxPoolSize = QUICK_MAX_POOL_SIZE;
				minPoolSize = QUICK_MIN_POOL_SIZE;
				keepAlive = QUICK_KEEP_ALIVE;
				threadName = "LingYang_Q #";
				break;
			case TYPE_CAN_CLEAR:
				maxPoolSize = CAN_CLEAR_MAX_POOL_SIZE;
				minPoolSize = CAN_CLEAR_MIN_POOL_SIZE;
				keepAlive = CAN_CLEAR_KEEP_ALIVE;
				threadName = "LingYang_C #";
				break;
			default:
//				throw new RuntimeException("未设置初始值且不支持该类型的ThreadPoolManager - " + type);
//				Log.e(TAG, "未设置初始值且不支持该类型的ThreadPoolManager - " + type);
				return;
			}
			
			mThreadName = threadName;
			mMaxPoolSize = maxPoolSize;
			mMinPoolSize = minPoolSize;
			mKeepAlive = keepAlive * 1000;
		}
		
		THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(
				mMaxPoolSize, MAXIMUM_POOL_SIZE, KEEP_ALIVE, TimeUnit.SECONDS,
				sPoolWorkQueue, sThreadFactory);
	}
	
	public void init(Context context) {
		if (mHandler == null) {
			mHandler = new ThreadHandler(context.getMainLooper());
		}
	}
	
	public static void initThreadPoolManager(Context context) {
		ThreadPoolManager.getInstance(TYPE_NORMAL).init(context);
		ThreadPoolManager.getInstance(TYPE_QUICK).init(context);
		ThreadPoolManager.getInstance(TYPE_CAN_CLEAR).init(context);
	}
	
	public void execute(Runnable runnable) {
		try {
			SERIAL_EXECUTOR.execute(runnable);
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void clearTask(){ 
		THREAD_POOL_EXECUTOR.clearTaskList();
	}
	
	public Executor getExecutor() {
		return SERIAL_EXECUTOR;
	}
	
	public synchronized static ThreadPoolManager getInstance(int type) {
		if (POOL_MANAGER_MAP == null) {
			POOL_MANAGER_MAP = new HashMap<Integer, ThreadPoolManager>();
		}
		ThreadPoolManager manager = POOL_MANAGER_MAP.get(type);
		if (manager == null) {
			manager = new ThreadPoolManager(type);
			POOL_MANAGER_MAP.put(type, manager);
		}
		return manager;
	}
	
	private void resizeCorePool(int size) {
		if (getCorePoolSize() == size) {
			return;
		}
    	int count = getActiveCount();
    	if (count != 0) {
    		return;
    	}
//		Log.v(TAG, "resizeCorePool Time Dis: " + (System.currentTimeMillis() - t));
		ThreadPoolExecutor executor = this.THREAD_POOL_EXECUTOR;
		if (executor != null) {
			executor.setCorePoolSize(size);
		}
//    	Log.w(TAG, "resizeCoreSize :" + size);
    }
	
    public int getActiveCount() {
    	return THREAD_POOL_EXECUTOR.getActiveCount();
    }
    
    public long getTaskCount() {
    	return THREAD_POOL_EXECUTOR.getTaskCount();
    }
    
    public int getCorePoolSize() {
    	return THREAD_POOL_EXECUTOR.getCorePoolSize();
    }
    
    public int getQueueSize() {
    	return THREAD_POOL_EXECUTOR.getQueue().size();
    }
    
    public long getCompletedTaskCount() {
    	return THREAD_POOL_EXECUTOR.getCompletedTaskCount();
    }
    
    public boolean removeTask(String taskId) {
    	if (taskId == null) {
    		return false;
    	}
    	THREAD_POOL_EXECUTOR.queueLock();
        try {
        	BlockingQueue<Runnable> queue = THREAD_POOL_EXECUTOR.getQueue();
	    	BlockingQueue<Runnable> removeList = new LinkedBlockingQueue<Runnable>();
	    	for(Runnable r : queue) {
	    		String id = null;
	    		if (r instanceof TaskRunnable) {
	    			TaskRunnable taskRunnable = (TaskRunnable)r;
	    			id = taskRunnable.getTaskId();
	    		} else {
	    			id = r.toString();
	    		}
	    		if (id == null) {
	    			continue;
	    		}
	    		if (taskId.equals(id)) {
	    			removeList.add(r);
	    		}
	    	}
	    	Iterator<Runnable> it = removeList.iterator();
	    	while(it.hasNext()) {
	    		Runnable runnable = it.next();
//	    		Log.v(TAG, "removeTask " + taskId + " - " + runnable.toString());
	    		queue.remove(runnable);
	    	}
	    	if (removeList.size() > 0) {
	    		return true;
	    	}
        	
        } finally {
        	THREAD_POOL_EXECUTOR.queueUnlock();
        }
    	return false;
    }
	
    long t;
	private class AutoCloseExecutor implements Executor {
		public synchronized void execute(final Runnable r) {
			if (mHandler == null) {
				throw new RuntimeException("ThreadPoolManager未初始化 " + mManagerType);
			}
			mHandler.removeMessages(MESSAGE_RESIZE_CORE_SIZE);
			Runnable runnable = new PoolTaskRunnable(r);
			
			resizeCorePool(mMaxPoolSize);
			THREAD_POOL_EXECUTOR.execute(runnable);
		}
	}
	
	class PoolTaskRunnable extends TaskRunnable {
		
		Runnable runnable;
		
		PoolTaskRunnable(Runnable r) {
			this.runnable = r;
		}

		@Override
		public void run() {
			mHandler.removeMessages(MESSAGE_RESIZE_CORE_SIZE);
			try {
				if (runnable != null) {
					runnable.run();
				}
			} finally {
//				Log.v(TAG, "End Time Dis: " + (System.currentTimeMillis() - t));
				t = System.currentTimeMillis();
				mHandler.removeMessages(MESSAGE_RESIZE_CORE_SIZE);
				if (getTaskCount() - getCompletedTaskCount() <= mMaxPoolSize) {
//					Log.i(TAG, "send resize msg");
					mHandler.sendEmptyMessageDelayed(MESSAGE_RESIZE_CORE_SIZE, CAN_CLEAR_KEEP_ALIVE * 1000);
				}
//				Log.v(TAG,
//						" QueueSize: "
//						+ THREAD_POOL_EXECUTOR.getQueue().size()
//						+ " TaskCount: "
//						+ getTaskCount()
//						+ " ActiveCount: "
//						+ THREAD_POOL_EXECUTOR.getActiveCount()
//						+ " PoolSize: "
//						+ THREAD_POOL_EXECUTOR.getPoolSize()
//						+ " CompletedTask: "
//						+ THREAD_POOL_EXECUTOR
//								.getCompletedTaskCount()
//						+ " CorePoolSize: "
//						+ THREAD_POOL_EXECUTOR
//								.getCorePoolSize());
			}
		}

		@Override
		public String getTaskId() {
			if (runnable instanceof TaskRunnable) {
				return ((TaskRunnable)runnable).getTaskId();
			} else {
				return runnable.toString();
			}
		}
	}
	
	class ThreadHandler extends Handler {
		
		 public ThreadHandler(Looper looper) {
			 super(looper);
		 }

		@Override
		public void handleMessage(Message msg) {
//			Log.d(TAG, "rev msg resizeCorePool");
			switch (msg.what) {
			   case MESSAGE_RESIZE_CORE_SIZE:
				resizeCorePool(mMinPoolSize);
		       	break;
			}
		}
	}
		
	public synchronized void clear() {
		clearTask();
		if (mHandler != null) {
			mHandler.removeMessages(MESSAGE_RESIZE_CORE_SIZE);
		}
	}
	
	public void destroy() {
		ThreadPoolExecutor executor = this.THREAD_POOL_EXECUTOR;
		if (executor != null) {
			executor.setCorePoolSize(0);
		}
		clear();
	}
	
	public static synchronized void release() {
		if (POOL_MANAGER_MAP == null) {
			return;
		}
		HashMap<Integer, ThreadPoolManager> threadPoolMap = POOL_MANAGER_MAP;
		POOL_MANAGER_MAP = null;
		if (threadPoolMap != null) {
			Iterator<Entry<Integer, ThreadPoolManager>> it = threadPoolMap.entrySet().iterator();
			while(it.hasNext()) {
				ThreadPoolManager poolManager = it.next().getValue();
				poolManager.destroy();
			}
		}
	}
	
	public int getMaxPoolSize() {
		return mMaxPoolSize;
	}

	public void setMaxPoolSize(int maxPoolSize) {
		this.mMaxPoolSize = maxPoolSize;
	}

	public int getMinPoolSize() {
		return mMinPoolSize;
	}

	public void setMinPoolSize(int minPoolSize) {
		this.mMinPoolSize = minPoolSize;
	}

	public long getKeepAlive() {
		return mKeepAlive;
	}

	public void setKeepAlive(long keepAlive) {
		this.mKeepAlive = keepAlive;
	}

	public String getThreadName() {
		return mThreadName;
	}

	public void setThreadName(String threadName) {
		this.mThreadName = threadName;
	}
}
