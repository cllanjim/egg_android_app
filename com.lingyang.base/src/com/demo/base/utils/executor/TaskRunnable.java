package com.lingyang.base.utils.executor;
public abstract class TaskRunnable implements Runnable {

	protected String taskId;
	
	public void setTaskId(String id) {
		taskId = id;
	}
	
	public String getTaskId() {
		if (taskId == null) {
			return toString();
		}
		return taskId;
	}
}

