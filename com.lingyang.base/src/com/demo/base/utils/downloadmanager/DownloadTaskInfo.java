package com.lingyang.base.utils.downloadmanager;

import java.io.Serializable;

import com.lingyang.base.utils.downloadmanager.excutor.DownloadExecutor;

public class DownloadTaskInfo implements Serializable {
	
	private static final long serialVersionUID = -2063632444408490417L;
	
	public static final String METHOD_POST = DownloadExecutor.METHOD_POST;
	public static final String METHOD_GET = DownloadExecutor.METHOD_GET;
	
	/**
	 * @see com.lingyang.base.utils.downloadmanager.KeyConstants#DOWNLOAD_STATE_NONE
	 */
	public final static int DOWNLOAD_STATE_NONE = KeyConstants.DOWNLOAD_STATE_NONE;
	/**
	 * @see com.lingyang.base.utils.downloadmanager.KeyConstants#DOWNLOAD_STATE_FAIL
	 */
	public final static int DOWNLOAD_STATE_FAIL = KeyConstants.DOWNLOAD_STATE_FAIL;
	/**
	 * @see com.lingyang.base.utils.downloadmanager.KeyConstants#DOWNLOAD_STATE_SUC
	 */
	public final static int DOWNLOAD_STATE_SUC = KeyConstants.DOWNLOAD_STATE_SUC;
	/**
	 * @see com.lingyang.base.utils.downloadmanager.KeyConstants#DOWNLOAD_STATE_PREPARE
	 */
	public final static int DOWNLOAD_STATE_PREPARE = KeyConstants.DOWNLOAD_STATE_PREPARE;
	/**
	 * @see com.lingyang.base.utils.downloadmanager.KeyConstants#DOWNLOAD_STATE_START
	 */
	public final static int DOWNLOAD_STATE_START = KeyConstants.DOWNLOAD_STATE_START;
	/**
	 * @see com.lingyang.base.utils.downloadmanager.KeyConstants#DOWNLOAD_STATE_DOWNLOADING
	 */
	public final static int DOWNLOAD_STATE_DOWNLOADING = KeyConstants.DOWNLOAD_STATE_DOWNLOADING;
	/**
	 * @see com.lingyang.base.utils.downloadmanager.KeyConstants#DOWNLOAD_STATE_PENDING
	 */
	public final static int DOWNLOAD_STATE_PENDING = KeyConstants.DOWNLOAD_STATE_PENDING;
	/**
	 * @see com.lingyang.base.utils.downloadmanager.KeyConstants#DOWNLOAD_STATE_PAUSE
	 */
	public final static int DOWNLOAD_STATE_PAUSE = KeyConstants.DOWNLOAD_STATE_PAUSE;

	public String downloadId;
	
	public String uri;
	
	public String postParam;
	
	public String httpMethod;
	
	/**
	 * 存储路径
	 */
	public String path;
	
	/**
	 * 存储文件夹
	 */
	public String folder;
	
	/**
	 * 文件名
	 */
	public String fileName;
	
	/**
	 * 文件总大小 
	 * 字节
	 */
	public long filelen;
	
	/**
	 * 创建时间
	 */
	public long createTime;
	
	/** 
	 * 下载状态
	 */
	public int status = DOWNLOAD_STATE_NONE; 
	
	/**
	 * 下载速度 字节/秒
	 */
	public long downloadrate;
	
	/**
	 * 已下载大小
	 */
	public long downloadLen;
	
	/**
	 * 当前连接线程数
	 */
	public byte npeers;
	
	/**
	 * 当前重试下载次数
	 * 只有当前下载状态为STATE_DOWNLOADING、STATE_PREPARE、STATE_START、STATE_SUC的时候才会有当前重试次数
	 */
	public int currentRetryCount;
	
	public String owner;
	
	public int isPriv;
	
	/**
	 * 文件类型
	 */
	public int fileType;
	
	/**
	 * 文件版本
	 */
	public int fileVersion;
}