package com.lingyang.base.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.os.FileObserver;

import com.lingyang.base.utils.filetype.FileTypes;

/**
 * [SD卡监听]
 */
public class SDCardListener {
	
	private final static String TAG = "SDCardListener";
	
	private static final String DEFAULT_INSTANCE_KEY = "default";
	
	private static final int DEFAULT_SCAN_MASK = FileObserver.MOVED_TO  | FileObserver.CREATE;
	
	private static final Object lock = new Object();
	
	private static Map<String, SDCardListener> instances = new HashMap<String, SDCardListener>();
	
	private int mWatchingMask = DEFAULT_SCAN_MASK;
	
	private String mInstanceKey;
	
	private List<OnSdcardChangeListener> mListeners;
	
	private HashMap<String, DirFileObserver> mObserverMap;

	private SDCardListener(String instanceKey) {
		mObserverMap = new HashMap<String, SDCardListener.DirFileObserver>();
		mListeners = new ArrayList<SDCardListener.OnSdcardChangeListener>();
		mInstanceKey = instanceKey;
	}
	
	public static SDCardListener getInstance() {
		return getInstance(DEFAULT_INSTANCE_KEY);
	}
	
	public static SDCardListener getInstance(String key) {
		synchronized (lock) {
			SDCardListener listener = instances.get(key);
			if (listener == null) {
				listener = new SDCardListener(key);
				instances.put(key, listener);
			}
			return listener;
		}
	}
	
	public void setWathingMask(int mask) {
		this.mWatchingMask = mask;
	}
	
	public synchronized void addWatchingListener(OnSdcardChangeListener listener) {
		mListeners.add(listener);
	}
	
	public synchronized void release() {
		mListeners.clear();
		
		if (mObserverMap.size() > 0) {
			//停止文件监听
			Iterator<Entry<String, DirFileObserver>> it = mObserverMap.entrySet().iterator();
			while(it.hasNext()) {
				DirFileObserver observer = it.next().getValue();
				observer.stopWatching();
			}
		}
		mObserverMap.clear();
		synchronized (lock) {
			instances.remove(mInstanceKey);
		}
	}
	
	/**
	 * [SDCARD路径监听]<BR>
	 * @param filePath
	 * @return
	 */
	public boolean startWatching(String filePath) {
		if (filePath == null) {
			return false;
		}
		
		DirFileObserver observer = mObserverMap.get(filePath);
		if (observer != null) {
			return true;
		}
		
		File file = new File(filePath);
		if (! file.exists()) {
			return false;
		}
		
		observer = new DirFileObserver(filePath);
		observer.startWatching();
		mObserverMap.put(filePath, observer);
		
		Log.i(TAG, "开始监听路径： " + filePath);
		
		return true;
	}
	
	public boolean removeWatching(String filePath) {
		if (filePath == null) {
			return false;
		}
		
		DirFileObserver observer = mObserverMap.get(filePath);
		if (observer != null) {
			return true;
		}
		
		mObserverMap.remove(filePath);
		return true;
	}
	
	/**
	 * [重新进行文件夹监听]<BR>
	 */
	public void restartWating() {
		Log.i(TAG, "重新开始监听:" + 	mObserverMap.size());
		if (mObserverMap.size() == 0) {
			return;
		}
		
		Iterator<Entry<String, DirFileObserver>> iterator = mObserverMap.entrySet().iterator();
		while(iterator.hasNext()) {
			Entry<String, DirFileObserver> entry = iterator.next();
			DirFileObserver fileObserver = entry.getValue();
			fileObserver.stopWatching();
			fileObserver.startWatching();
			
			Log.i(TAG, "重新开始监听:" + 	entry.getKey());
		}
	}

	class DirFileObserver extends FileObserver {
		
		String filePath;
		
		public DirFileObserver(String path) {
			//只监听文件创建的事件（当每次读取缩略图的时候都会触发一次ACCESS事件）
			super(path, mWatchingMask);
			filePath = path;
		}
		
		@Override
		public void onEvent(int event, String name) {
			if (mListeners == null || name == null) {
				return;
			}
//			switch (event) {
//			case FileObserver.CREATE:
//				Log.i(TAG, "CREATE " + name + " - Path:" + filePath);
//				break;
//			case FileObserver.MODIFY:
//				Log.i(TAG, "MODIFY " + name + " - Path:" + filePath);
//				break;
//			case FileObserver.MOVED_FROM:
//				Log.i(TAG, "MOVED_FROM " + name + " - Path:" + filePath);
//				break;
//			case FileObserver.MOVED_TO:
//				Log.i(TAG, "MOVED_TO " + name + " - Path:" + filePath);
//				break;
//			default:
//				Log.i(TAG, event + " " + name + " - Path:" + filePath);
//				break;
//			}
			
			List<OnSdcardChangeListener> notifyList = new ArrayList<SDCardListener.OnSdcardChangeListener>();
			synchronized (SDCardListener.this) {
				notifyList.addAll(mListeners);
			}
			int mediaType = FileTypes.getFileType(name);
			for(int i = 0;i < notifyList.size();i++) {
				OnSdcardChangeListener listener = notifyList.get(i);
				boolean isEvent = listener.onEvent(mediaType, event, filePath, name);
				if (isEvent) {
					break;
				}
			}
		}
	}
	
	public interface OnSdcardChangeListener {
		public boolean onEvent(int mediaType, int event, String path, String name);
	}
}