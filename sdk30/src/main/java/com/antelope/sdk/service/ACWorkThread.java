package com.antelope.sdk.service;

import com.antelope.sdk.utils.CLog;

import android.os.Handler;
import android.os.HandlerThread;

public class ACWorkThread {
	private static ACWorkThread instance=null;
	private Handler mHandler=null;
	private HandlerThread  mHandlerThread=null;
	
	//取消构造函数权限
	private ACWorkThread(){
		mHandlerThread=new HandlerThread("ACWorkThread",Thread.NORM_PRIORITY);
//		mHandlerThread.setDaemon(true);
		mHandlerThread.start();
		mHandler=new Handler(mHandlerThread.getLooper());
	} 
	
	public static ACWorkThread getInstance(){
		if(instance==null)
			instance=new ACWorkThread();
		return instance;
	}
	
	public void executeTask(Runnable r){
		mHandler.post(r);
	}
	
	/**
	 * @param r     执行的任务
	 * @param delayMillis  多久之后执行  单位是毫秒
	 */
	public void executeTaskDelayed(Runnable r,int delayMillis){
		mHandler.postDelayed(r, delayMillis);
	}
	
	public void stop(){
		if(mHandlerThread!=null){
			mHandlerThread.quit();
			instance=null;
			mHandler=null;
			mHandlerThread=null;
		}
	}
}
