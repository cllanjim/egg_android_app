package com.lingyang.base.utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.SoundPool;

public class SoundEngine {
	private final static String TAG = "SoundEngine";
	
	HashMap<String, Integer> effectsMap = new HashMap<String, Integer>();
	
	SoundPool sp = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
	int lastSndId = -1;
	
    static SoundEngine _sharedEngine = null;

    public static SoundEngine sharedEngine() {
        synchronized(SoundEngine.class) {
            if (_sharedEngine == null) {
            	Log.v(TAG, "初始声音引擎");
                _sharedEngine = new SoundEngine();
            }
        }
        return _sharedEngine;
    }

    public static void purgeSharedEngine() {
        synchronized(SoundEngine.class) {
            _sharedEngine = null;
        }
    }

	public void preloadEffect(String filePath){
		synchronized(effectsMap) {
			Integer sndId = effectsMap.get(filePath);
			if (sndId != null)
				return;
			
			sndId = sp.load(filePath, 0);
			effectsMap.put(filePath, sndId);
		}
	}
		
	public void preloadEffect(Context context, String assetFilePath){
		try {
			synchronized(effectsMap) {
				String soundKey = "(Asset)" + assetFilePath;
				Integer sndId = effectsMap.get(soundKey);
				if (sndId != null)
					return;
				Log.v(TAG, "load Sound:" + assetFilePath);
				AssetFileDescriptor assetFile = context.getAssets().openFd(assetFilePath);
				sndId = sp.load(assetFile, 0);
				effectsMap.put(soundKey, sndId);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void preloadEffect(Context context, int resId){
		synchronized(effectsMap) {
			String soundKey = "(Raw)" + resId;
			Integer sndId = effectsMap.get(soundKey);
			if (sndId != null)
				return;
			Log.v(TAG, "load Sound:" + resId);
			sndId = sp.load(context, resId, 0);
			effectsMap.put(soundKey, sndId);
		}
	}
	
	public void playEffect(String filePath) {
		Integer sndId = -1;
		synchronized (effectsMap) {
			sndId = effectsMap.get(filePath);
			if (sndId == null) {
				sndId = sp.load(filePath, 0);
				effectsMap.put(filePath, sndId);
			}
		}
		sp.play(sndId, 1.0f, 1.0f, 0, 0, 1.0f);
	}
	
	public void playEffect(Context context, String assetFilePath) {
		playEffect(context, assetFilePath, 0);
	}
	
	public void playEffect(Context context, String assetFilePath, int priority) {
		if (context == null) {
			// 禁止播放操作音效
			return;
		}
		Integer sndId = -1;
		try {
			synchronized (effectsMap) {
				String soundKey = "(Asset)" + assetFilePath;
				Log.v(TAG, "play sound:" + soundKey);
				sndId = effectsMap.get(soundKey);
				if (sndId == null) {
					AssetFileDescriptor assetFile = context.getAssets().openFd(assetFilePath);
					sndId = sp.load(assetFile, 0);
					effectsMap.put(soundKey, sndId);
				}
			}
			sp.play(sndId, 0.7f, 0.7f, priority, 0, 1.0f);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void playEffect(Context context, int resId, int priority) {
		if (context == null) {
			// 禁止播放操作音效
			return;
		}
		Integer sndId = -1;
		synchronized (effectsMap) {
			String soundKey = "(Raw)" + resId;
			Log.v(TAG, "play sound:" + soundKey);
			sndId = effectsMap.get(soundKey);
			if (sndId == null) {
				sndId = sp.load(context, resId, 0);
				effectsMap.put(soundKey, sndId);
			}
		}
		sp.play(sndId, 0.7f, 0.7f, priority, 0, 1.0f);
	}
	
	public int getSoundSize(){
		return effectsMap.size();
	}
	
	public void releaseAllSound(){
		 synchronized (effectsMap) {
		   Set<String> keys = effectsMap.keySet();
		   Iterator<String> keyIterator = keys.iterator();
		   while(keyIterator.hasNext())
		   {
			   String key = keyIterator.next();
			   int sndId = effectsMap.get(key);
			   sp.unload(sndId);
		   }
		   effectsMap.clear();
		 }
	}
	
   public void destory() {
	   releaseAllSound();
	   sp.release();
	   Log.v(TAG, "(SoundEngine)release All sound");
	   _sharedEngine = null;
   }
   
   public static void clearStaticResource() {
	   _sharedEngine = null;
   }
   
}
