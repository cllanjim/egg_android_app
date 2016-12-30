package com.lingyang.base.utils.task;

public class AutoTask {

	public final static int DEFAULT_RETRY_MAX_COUNT = 3;
	
	public int maxRetryCount = DEFAULT_RETRY_MAX_COUNT;
	
	public int currentRetryCount;
	
	public Object tag;
}
