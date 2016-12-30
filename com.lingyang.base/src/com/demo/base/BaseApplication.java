package com.lingyang.base;

import android.app.Application;
import android.content.Context;

import com.lingyang.base.utils.JacksonUtils;
import com.lingyang.base.utils.Log;
import com.lingyang.base.utils.ThreadPoolManagerQuick;
import com.lingyang.base.utils.executor.ThreadPoolManager;


public class BaseApplication extends Application {

	private final String TAG = BaseApplication.class.getSimpleName();
	
	private static Context sContext;

	@Override
	public void onCreate() {
		super.onCreate();
		sContext = this;
		ThreadPoolManager.initThreadPoolManager(this);
		ThreadPoolManagerQuick.execute(new Runnable() {
			@Override
			public void run() {
				long t1 = System.currentTimeMillis();
				JacksonUtils.shareJacksonUtils();	
				long t2 = System.currentTimeMillis();
				Log.v(TAG, "JacksonUtils 耗时： " + (t2-t1));
			}
		});
	}
	
	public static Context getContext() {
		return sContext;
	}
}
