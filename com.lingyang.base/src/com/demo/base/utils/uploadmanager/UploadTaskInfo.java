package com.lingyang.base.utils.uploadmanager;

import java.io.Serializable;
import java.util.Map;

import com.lingyang.base.utils.downloadmanager.excutor.DownloadExecutor;
import com.lingyang.base.utils.uploadmanager.storage.UploadBean;

public class UploadTaskInfo implements Serializable {
	
	private static final long serialVersionUID = -7878183203702173853L;
	
	public String uploadId;
    public String name;
    public int state = UploadState.STATE_NONE;

	/**
	 * 上传地址
	 */
	public String uri;
	
	/**
	 * post参数
	 */
	public String postParam;
	
	/**
	 * 存储文件名
	 * 如果不设置文件名则直接用uri中的文件名
	 */
	public String fileName;
	
	/**
	 * 存储文件夹
	 * 如果不设置存储文件夹则使用默认文件夹
	 */
	public String filePath;
	
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
	 * 最多重试下载次数
	 */
	public int retryCount = KeyConstants.UNKOWN_KEY;
	
	/**
	 * 连接超时时长
	 */
	public int connectionTimeout = KeyConstants.DEFAULT_CONNECT_TIME_OUT;
	
	/**
	 * 数据读取超时时长
	 */
	public int readTimeout = KeyConstants.DEFAULT_READ_TIME_OUT;
	
	/**
	 * 文件类型
	 */
	public int fileType;
	
	/**
	 * 文件版本
	 */
	public int fileVersion;
	
	/**
	 * HTTP头信息
	 */
	public Map<String, String> httpHead;
	
	
	 /**
     * 文件总大小 
     * 字节
     */
    public long filelen;
    
    /**
     * 已上传大小
     */
    public long uploadLen;
    /**
     * 创建时间
     */
    public long createTime;
    
    public int uploadRate;
    @Override
       public boolean equals(Object o) {
           if (o == null) {
               return false;
           }
           UploadTaskInfo info = (UploadTaskInfo) o;
           if (info.uploadId.equals(uploadId)) {
               return true;
           }
           return false;
       }
	
}