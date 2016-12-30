package com.lingyang.base.utils.downloadmanager;

import java.io.Serializable;
import java.util.Map;

import com.lingyang.base.utils.downloadmanager.excutor.DownloadExecutor;

public class DownloadTaskParam implements Serializable {
	
	private static final long serialVersionUID = -7878183203702173853L;

	public static final String METHOD_POST = DownloadExecutor.METHOD_POST;
	public static final String METHOD_GET = DownloadExecutor.METHOD_GET;
	
	/**
	 * 下载地址
	 */
	public String uri;
	
	/**
	 * post参数
	 */
	public String postParam;
	
	/**
	 * http请求方式
	 */
	public String httpMethod = METHOD_GET;
	
	/**
	 * 存储文件名
	 * 如果不设置文件名则直接用uri中的文件名
	 */
	public String fileName;
	
	/**
	 * 存储文件夹
	 * 如果不设置存储文件夹则使用默认文件夹
	 */
	public String saveFolder;
	
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
}