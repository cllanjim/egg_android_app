package com.lingyang.base.utils;

import com.lingyang.base.utils.executor.ThreadPoolManager;


public class Thread extends java.lang.Thread {

	public static final int TYPE_NORMAL = ThreadPoolManager.TYPE_NORMAL;
	public static final int TYPE_QUICK = ThreadPoolManager.TYPE_QUICK;
	public static final int TYPE_CAN_CLEAR = ThreadPoolManager.TYPE_CAN_CLEAR;
	
	private int threadType = TYPE_NORMAL;
	
	private Runnable runnable;
	
	public Thread() {
		create(this);
	}
	
    public Thread(Runnable runnable) {
    	create(runnable);
    }
    
    private void create(Runnable runnable) {
    	this.runnable = runnable;
    }
    
    @Override
    public void start() {
    	if (runnable == null) {
    		throw new RuntimeException("Thread 中 Runnble 为空");
    	}
    	
    	ThreadPoolManager.getInstance(threadType).execute(runnable);
    }

	public int getThreadType() {
		return threadType;
	}

	public void setThreadType(int threadType) {
		this.threadType = threadType;
	}
}

