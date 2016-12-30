package com.lingyang.base.utils.executor.queue;

import java.util.concurrent.BlockingQueue;

/**
 * @author 李理
 * @date 2013-8-10
 */
public interface LockBlockingQueue<E> extends BlockingQueue<E>{
	
	  public void fullyLock();
	  
	  public void fullyUnlock();
}

