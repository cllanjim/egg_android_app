package com.lingyang.base.utils.uploadmanager;

import com.lingyang.base.utils.downloadmanager.KeyConstants;


public class UploadExecutorTarget {

    public String uploadId;
    public String name;
    public boolean isRun;
    public int state = UploadState.STATE_NONE;
    //上传服务器的地址
    public String url;
    
    public String postParam;
    
    public String httpMethod;
    
    /**
     * 路径
     */
    public String filePath;
    
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
     * 已上传大小
     */
    public long uploadLen;
    
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
    public int uploadState = KeyConstants.DOWNLOAD_STATE_UNKOWN;
    
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
    /**
     * 上传速率
     */
	public int uploadRate;
    
    
    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        UploadExecutorTarget info = (UploadExecutorTarget) o;
        if (info.uploadId.equals(uploadId)) {
            return true;
        }
        return false;
    }
    
    

}
