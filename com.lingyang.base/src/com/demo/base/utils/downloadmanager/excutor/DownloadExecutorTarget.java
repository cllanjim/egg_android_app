package com.lingyang.base.utils.downloadmanager.excutor;

import java.util.List;
import java.util.Map;

public class DownloadExecutorTarget {
	/**
	 * 下载次数为重复尝试
	 */
	public static int RETRY_REPEAT = -1;
	
	public String downloadId;
	public String url;
	public String method;
	public String postParam;
	public byte[] postContent;
	/**
	 * 当前暂不支持传入和存储该参数到db
	 * 暂不能使用
	 */
	public Map<String, String> httpHead;
	public int connectTimeout;
	public int readTimeout;
	public String savePath;
	public long createTime;
	public List<String> notAcceptTypes;
	public int retryCount;
	
	/**
	 * 是否私有
	 */
	public boolean isPrivate = false;
	
	/**
	 * 私有所属人
	 * 如果isPrivate为true,则需要设置相应的所属者
	 */
	public String onwer;
	
	/**
	 * 文件类型
	 */
	public int fileType;
	
	/**
	 * 文件版本
	 */
	public int fileVersion;
	
	public long fileLength;
	public long downloadLength;
	public DownloadExecutor executor;
	public DownloadTaskState state = new DownloadTaskState();
	
	public static class DownloadTaskState {
		public boolean isRun;
		/**
		 * {@link DownloadState#STATE_SUC}
		 * {@link DownloadState#STATE_*}
		 */
		public int downloadState = DownloadState.STATE_NONE;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		}
		DownloadExecutorTarget info = (DownloadExecutorTarget)o;
		if (info.downloadId.equals(downloadId)) {
			return true;
		}
		return false;
	}
}

