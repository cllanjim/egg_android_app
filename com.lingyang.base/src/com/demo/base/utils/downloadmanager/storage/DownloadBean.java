package com.lingyang.base.utils.downloadmanager.storage;

import com.lingyang.base.utils.downloadmanager.KeyConstants;

/**
 * [下载任务Bean]<br/>
 * 
 */
public class DownloadBean {

	public String downloadId;
	
	public String url;
	
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
	 * 已下载大小
	 */
	public long downloadLen;
	
	/**
	 * 创建时间
	 */
	public long createTime;
	
	public String owner;
	
	/**
	 * 0 正常模式
	 * 1 隐私模式
	 */
	public int isPriv;
	
	/**
	 * {@link com.qvod.player.utils.downloadmanager.KeyConstants#DOWNLOAD_STATE_START}
	 * {@link com.qvod.player.utils.downloadmanager.KeyConstants#DOWNLOAD_STATE_*}
	 */
	public int downloadState = KeyConstants.DOWNLOAD_STATE_UNKOWN;
	
	/**
	 * 下载重试次数
	 */
	public int retryCount = -1;
	
	/**
	 * 读取超时时长
	 */
	public int readTimeout = -1;
	
	/**
	 * 连接超时时长
	 */
	public int connectionTimeout = -1;
	
	/**
	 * Http请求头信息
	 */
	public String httpHead;
	
	/**
	 * 文件类型
	 */
	public int fileType;
	
	/**
	 * 文件版本
	 */
	public int fileVersion;
}

