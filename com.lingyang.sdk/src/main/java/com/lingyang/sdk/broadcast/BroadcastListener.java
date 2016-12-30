package com.lingyang.sdk.broadcast;

import com.lingyang.sdk.exception.LYException;

/**
 * 广播状态变化监听
 * @author liaolei
 *
 */
public interface BroadcastListener {
	
	 public void onBroadcastStart();

     void onBroadcastLive();

     void onBroadcastStop();

     void onBroadcastError(LYException exception);

}
