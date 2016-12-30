package com.lingyang.base.utils.task;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.lingyang.base.utils.Log;


public class AutoRunTaskQueue<T extends AutoTask> {
	
	private final static String TAG = "AutoRunTaskQueue";

	private boolean mIsRun;
	
	private BlockingQueue<T> workQueue;
	
	protected AutoTaskRunner<T> mAutoTaskRunner;
	
	private OnAutoRunTaskListener<T> mRunListener;
	
	private Thread mRunThread;
	
	public AutoRunTaskQueue(AutoTaskRunner<T> runner) {
		if (runner == null) {
			throw new RuntimeException("AutoRunTaskQueue AutoTaskRunner can not null");
		}
		this.workQueue = new LinkedBlockingQueue<T>();
		this.mAutoTaskRunner = runner;
	}
	
	public void start() {
		if (mIsRun) {
			return;
		}
		this.mIsRun = true;
		if (mAutoTaskRunner != null) {
			mAutoTaskRunner.start();
		}
		mRunThread = new Thread() {
			public void run() {
				runWorker();
			}
		};
		mRunThread.start();
	}
	
	final void runWorker() {
		T task = null;
		while(mIsRun && (task = getTask()) != null) {
			boolean isSuc = runTask(task);
			if (mRunListener != null) {
				mRunListener.onRunTaskEnd(isSuc, task);
			}
		} 
		Log.v(TAG, "runWorker End");
	}
	
	private boolean runTask(T task) {
		//根据未来拓展，进行消息重发
		boolean isSuc = mAutoTaskRunner.runTask(task);
		if (! isSuc && task.currentRetryCount < task.maxRetryCount) {
			task.currentRetryCount++;
			isSuc = runTask(task);
			Log.w(TAG, "runTask retry " + task.currentRetryCount + " - " + task.toString());
		}
		return isSuc;
	}
	
	public boolean isRun() {
		return mIsRun;
	}
	
	public void addTask(T task) {
		if (task == null) {
			return;
		}
		try {
 			workQueue.put(task);
		} catch (InterruptedException e) {}
	}
	
	public T getTask() {
		try {
			T task = workQueue.take();
			return task;
		} catch (InterruptedException e) {}
		return null;
	}
	
	public void stopRun() {
		mIsRun = false;
		workQueue.clear();
		if (mAutoTaskRunner != null) {
			mAutoTaskRunner.stop();
		}
		if (mRunThread != null) {
			mRunThread.interrupt();
		}
	}
	
	public void clearTasks() {
		workQueue.clear();
	}
	
	public int getWorkSize() {
		return workQueue.size();
	}
	
	public void setOnAutoRunTaskListener(OnAutoRunTaskListener<T> l) {
		this.mRunListener = l;
	}
	
	public interface AutoTaskRunner<T> {
		boolean runTask(T task);
		void start();
		void stop();
	}
	
	public interface OnAutoRunTaskListener<T> {
		void onRunTaskEnd(boolean isSuc, T task);
	}
}
